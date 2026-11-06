package com.openhazard.app.service;

import com.openhazard.app.dto.*;
import com.openhazard.app.entities.EnvironmentalRecord;
import com.openhazard.app.entities.Location;
import com.openhazard.app.enums.RiskLevel;
import com.openhazard.app.integration.OpenMeteoClient;
import com.openhazard.app.repository.EnvironmentalRecordRepository;
import com.openhazard.app.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnvironmentalService {

    private final LocationRepository locationRepository;
    private final EnvironmentalRecordRepository recordRepository;
    private final OpenMeteoClient openMeteoClient;
    private final OpenMeteoNormalizer normalizer;
    private final EnvironmentalRiskEngine riskEngine;
    private final AlertEvaluationService alertEvaluationService;
    private final AdvisoryService advisoryService;

    @Cacheable(value = "locations-overview")
    @Transactional(readOnly = true)
    public LocationOverviewListResponse getAllLocationsOverview() {
        List<Location> monitoredLocations = locationRepository.findByIsActiveTrueAndIsMonitoredTrue();
        List<Long> locationIds = monitoredLocations.stream().map(Location::getId).toList();

        // Optimized single DB call for all latest records (Solves N+1 Query Problem)
        Map<Long, EnvironmentalRecord> latestRecordMap = recordRepository.findLatestRecordsForLocations(locationIds)
                .stream()
                .collect(Collectors.toMap(r -> r.getLocation().getId(), Function.identity()));

        List<LocationOverviewDTO> dtos = monitoredLocations.stream().map(location -> {
            EnvironmentalRecord latest = latestRecordMap.get(location.getId());

            return LocationOverviewDTO.builder()
                    .id(location.getId())
                    .name(location.getName())
                    .district(location.getDistrict())
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude())
                    .overallRisk(latest != null ? latest.getOverallRisk() : RiskLevel.LOW)
                    .heatRisk(latest != null ? latest.getHeatRisk() : RiskLevel.LOW)
                    .airQualityRisk(latest != null ? latest.getAirQualityRisk() : RiskLevel.LOW)
                    .floodRisk(latest != null ? latest.getFloodRisk() : RiskLevel.LOW)
                    .updatedAt(latest != null ? latest.getRecordedAt() : Instant.now())
                    .build();
        }).toList();

        return new LocationOverviewListResponse(dtos);
    }

    public LocationDetailDTO exploreLocationOnDemand(String name, BigDecimal latitude, BigDecimal longitude) {
        Optional<OpenMeteoResponseDTO> rawMeteo = openMeteoClient.fetchCurrentWeather(latitude, longitude);

        NormalizedEnvironmentData normalized = rawMeteo
                .map(normalizer::normalize)
                .orElseGet(() -> NormalizedEnvironmentData.builder().timestamp(Instant.now()).build());

        RiskAssessmentResult riskResult = riskEngine.evaluate(normalized);

        EnvironmentalRecordDTO currentRecord = mapNormalizedToRecordDTO(normalized, riskResult);
        SafetyAdvisoryDTO aiAdvisory = advisoryService.generateAdvisory(name, currentRecord);

        return LocationDetailDTO.builder()
                .name(name)
                .district("Explored Area")
                .latitude(latitude)
                .longitude(longitude)
                .currentRecord(currentRecord)
                .history(List.of(currentRecord))
                .aiAdvisory(aiAdvisory)
                .build();
    }

    @Transactional(readOnly = true)
    public LocationDetailDTO getLocationDetails(String locationName) {
        Location location = locationRepository.findByNameIgnoreCase(locationName)
                .orElseThrow(() -> new IllegalArgumentException("Location not found: " + locationName));

        EnvironmentalRecord latest = recordRepository.findTopByLocationIdOrderByRecordedAtDesc(location.getId())
                .orElseGet(() -> fetchAndIngestLocationData(location));

        Instant last24Hours = Instant.now().minus(24, ChronoUnit.HOURS);
        List<EnvironmentalRecord> history = recordRepository.findHistoricalTrends(location.getId(), last24Hours);

        EnvironmentalRecordDTO recordDTO = mapToRecordDTO(latest);
        SafetyAdvisoryDTO aiAdvisory = advisoryService.generateAdvisory(location.getName(), recordDTO);

        return LocationDetailDTO.builder()
                .id(location.getId())
                .name(location.getName())
                .district(location.getDistrict())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .currentRecord(recordDTO)
                .history(history.stream().map(this::mapToRecordDTO).toList())
                .aiAdvisory(aiAdvisory)
                .build();
    }

    @Transactional
    @CacheEvict(value = "locations-overview", allEntries = true)
    public EnvironmentalRecord fetchAndIngestLocationData(Location location) {
        log.info("Ingesting environmental data for location: {}", location.getName());

        Optional<OpenMeteoResponseDTO> rawMeteo = openMeteoClient.fetchCurrentWeather(location.getLatitude(), location.getLongitude());
        NormalizedEnvironmentData normalized = rawMeteo
                .map(normalizer::normalize)
                .orElseGet(() -> NormalizedEnvironmentData.builder().timestamp(Instant.now()).build());

        RiskAssessmentResult riskResult = riskEngine.evaluate(normalized);

        EnvironmentalRecord record = EnvironmentalRecord.builder()
                .location(location)
                .temperatureCelsius(normalized.getTemperatureCelsius())
                .humidityPercentage(normalized.getHumidityPercentage())
                .aqiValue(normalized.getAqiValue())
                .rainfallMmPerHour(normalized.getRainfallMmPerHour())
                .riverLevelMeters(normalized.getRiverLevelMeters())
                .heatRisk(riskResult.getHeatRisk())
                .airQualityRisk(riskResult.getAirQualityRisk())
                .floodRisk(riskResult.getFloodRisk())
                .overallRisk(riskResult.getOverallRisk())
                .recordedAt(normalized.getTimestamp())
                .build();

        recordRepository.save(record);

        // Delegate alert evaluations
        alertEvaluationService.processHazardAlerts(location, normalized, riskResult);

        return record;
    }

    private EnvironmentalRecordDTO mapToRecordDTO(EnvironmentalRecord record) {
        return EnvironmentalRecordDTO.builder()
                .temperatureCelsius(record.getTemperatureCelsius())
                .humidityPercentage(record.getHumidityPercentage())
                .aqiValue(record.getAqiValue())
                .rainfallMmPerHour(record.getRainfallMmPerHour())
                .riverLevelMeters(record.getRiverLevelMeters())
                .heatRisk(record.getHeatRisk())
                .airQualityRisk(record.getAirQualityRisk())
                .floodRisk(record.getFloodRisk())
                .overallRisk(record.getOverallRisk())
                .recordedAt(record.getRecordedAt())
                .build();
    }

    private EnvironmentalRecordDTO mapNormalizedToRecordDTO(NormalizedEnvironmentData data, RiskAssessmentResult risk) {
        return EnvironmentalRecordDTO.builder()
                .temperatureCelsius(data.getTemperatureCelsius())
                .humidityPercentage(data.getHumidityPercentage())
                .aqiValue(data.getAqiValue())
                .rainfallMmPerHour(data.getRainfallMmPerHour())
                .riverLevelMeters(data.getRiverLevelMeters())
                .heatRisk(risk.getHeatRisk())
                .airQualityRisk(risk.getAirQualityRisk())
                .floodRisk(risk.getFloodRisk())
                .overallRisk(risk.getOverallRisk())
                .recordedAt(data.getTimestamp())
                .build();
    }
}