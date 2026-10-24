package com.openhazard.app.service;

import com.openhazard.app.dto.*;
import com.openhazard.app.entities.Alert;
import com.openhazard.app.entities.EnvironmentalRecord;
import com.openhazard.app.entities.Location;
import com.openhazard.app.enums.HazardType;
import com.openhazard.app.enums.RiskLevel;
import com.openhazard.app.integration.OpenMeteoClient;
import com.openhazard.app.repository.AlertRepository;
import com.openhazard.app.repository.EnvironmentalRecordRepository;
import com.openhazard.app.repository.LocationRepository;
import com.openhazard.app.service.EnvironmentalRiskEngine;
import com.openhazard.app.service.OpenMeteoNormalizer;
import com.openhazard.app.service.SseAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnvironmentalService {

    private final LocationRepository locationRepository;
    private final EnvironmentalRecordRepository recordRepository;
    private final AlertRepository alertRepository;
    private final OpenMeteoClient openMeteoClient;
    private final OpenMeteoNormalizer normalizer;
    private final EnvironmentalRiskEngine riskEngine;
    private final SseAlertService sseAlertService;

    @Cacheable(value = "locations-overview")
    public LocationOverviewListResponse getAllLocationsOverview() {
        List<LocationOverviewDTO> dtos = locationRepository.findByIsActiveTrue().stream().map(location -> {
            Optional<EnvironmentalRecord> latestOpt = recordRepository.findTopByLocationIdOrderByRecordedAtDesc(location.getId());
            
            RiskLevel overall = latestOpt.map(EnvironmentalRecord::getOverallRisk).orElse(RiskLevel.LOW);
            RiskLevel heat = latestOpt.map(EnvironmentalRecord::getHeatRisk).orElse(RiskLevel.LOW);
            RiskLevel air = latestOpt.map(EnvironmentalRecord::getAirQualityRisk).orElse(RiskLevel.LOW);
            RiskLevel flood = latestOpt.map(EnvironmentalRecord::getFloodRisk).orElse(RiskLevel.LOW);
            Instant updatedAt = latestOpt.map(EnvironmentalRecord::getRecordedAt).orElse(Instant.now());

            return LocationOverviewDTO.builder()
                    .id(location.getId())
                    .name(location.getName())
                    .district(location.getDistrict())
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude())
                    .overallRisk(overall)
                    .heatRisk(heat)
                    .airQualityRisk(air)
                    .floodRisk(flood)
                    .updatedAt(updatedAt)
                    .build();
        }).collect(Collectors.toCollection(ArrayList::new));

        return new LocationOverviewListResponse(dtos);
    }

    public LocationDetailDTO getLocationDetails(String locationName) {
        Location location = locationRepository.findByNameIgnoreCase(locationName)
                .orElseThrow(() -> new IllegalArgumentException("Location not found: " + locationName));

        EnvironmentalRecord latest = recordRepository.findTopByLocationIdOrderByRecordedAtDesc(location.getId())
                .orElseGet(() -> fetchAndIngestLocationData(location));

        Instant last24Hours = Instant.now().minus(24, ChronoUnit.HOURS);
        List<EnvironmentalRecord> history = recordRepository.findHistoricalTrends(location.getId(), last24Hours);

        return LocationDetailDTO.builder()
                .id(location.getId())
                .name(location.getName())
                .district(location.getDistrict())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .currentRecord(mapToRecordDTO(latest))
                .history(history.stream().map(this::mapToRecordDTO).toList())
                .build();
    }

    @Transactional
    @CacheEvict(value = "locations-overview", allEntries = true)
    public EnvironmentalRecord fetchAndIngestLocationData(Location location) {
        log.info("Ingesting environmental data for location: {}", location.getName());

        Optional<OpenMeteoResponseDTO> rawMeteo = openMeteoClient.fetchCurrentWeather(location.getLatitude(), location.getLongitude());
        NormalizedEnvironmentData normalized = rawMeteo
                .map(raw -> normalizer.normalize(raw))
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

        if (riskResult.getOverallRisk() == RiskLevel.HIGH || riskResult.getOverallRisk() == RiskLevel.CRITICAL) {
            triggerAlert(location, HazardType.OVERALL, riskResult.getOverallRisk(), 
                    "Elevated risk condition detected in " + location.getName());
        }

        return record;
    }

    private void triggerAlert(Location location, HazardType hazardType, RiskLevel severity, String message) {
        Alert alert = Alert.builder()
                .location(location)
                .hazardType(hazardType)
                .severity(severity)
                .message(message)
                .build();

        alertRepository.save(alert);

        AlertDTO alertDTO = AlertDTO.builder()
                .id(alert.getId())
                .locationName(location.getName())
                .hazardType(hazardType)
                .severity(severity)
                .message(message)
                .createdAt(alert.getCreatedAt())
                .build();

        sseAlertService.broadcastAlert(alertDTO);
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
}