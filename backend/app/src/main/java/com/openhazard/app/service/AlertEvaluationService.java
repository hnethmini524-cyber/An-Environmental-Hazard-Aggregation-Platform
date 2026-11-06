package com.openhazard.app.service;

import com.openhazard.app.dto.AlertDTO;
import com.openhazard.app.dto.NormalizedEnvironmentData;
import com.openhazard.app.dto.RiskAssessmentResult;
import com.openhazard.app.entities.Alert;
import com.openhazard.app.enums.HazardType;
import com.openhazard.app.entities.Location;
import com.openhazard.app.enums.RiskLevel;
import com.openhazard.app.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertEvaluationService {

 private final AlertRepository alertRepository;
 private final SseAlertService sseAlertService;

 @Transactional
 public void processHazardAlerts(Location location, NormalizedEnvironmentData data, RiskAssessmentResult riskResult) {
     if (isElevated(riskResult.getHeatRisk())) {
         String msg = String.format("Elevated Heat Index detected in %s: %.1f°C with %.0f%% humidity.",
                 location.getName(), data.getTemperatureCelsius(), data.getHumidityPercentage());
         triggerAlert(location, HazardType.HEAT, riskResult.getHeatRisk(), msg);
         return;
     }

     if (isElevated(riskResult.getAirQualityRisk())) {
         String msg = String.format("Poor Air Quality detected in %s: US AQI reached %d.",
                 location.getName(), data.getAqiValue());
         triggerAlert(location, HazardType.AIR_QUALITY, riskResult.getAirQualityRisk(), msg);
         return;
     }

     if (isElevated(riskResult.getFloodRisk())) {
         String msg = String.format("Heavy Rainfall detected in %s: %.1f mm/hr precipitation.",
                 location.getName(), data.getRainfallMmPerHour());
         triggerAlert(location, HazardType.FLOOD, riskResult.getFloodRisk(), msg);
         return;
     }

     if (riskResult.getOverallRisk() == RiskLevel.CRITICAL) {
         String msg = String.format("Compounding environmental hazards detected in %s.", location.getName());
         triggerAlert(location, HazardType.OVERALL, riskResult.getOverallRisk(), msg);
     }
 }

 private boolean isElevated(RiskLevel level) {
     return level == RiskLevel.HIGH || level == RiskLevel.CRITICAL;
 }

 private void triggerAlert(Location location, HazardType hazardType, RiskLevel severity, String message) {
     Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
     if (alertRepository.existsByLocationIdAndHazardTypeAndCreatedAtAfter(location.getId(), hazardType, oneHourAgo)) {
         return;
     }

     Alert alert = alertRepository.save(Alert.builder()
             .location(location)
             .hazardType(hazardType)
             .severity(severity)
             .message(message)
             .build());

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
}