package com.openhazard.app.dto;

import com.openhazard.app.enums.RiskLevel;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class EnvironmentalRecordDTO {
    private Double temperatureCelsius;
    private Double humidityPercentage;
    private Integer aqiValue;
    private Double rainfallMmPerHour;
    private Double riverLevelMeters;
    private RiskLevel heatRisk;
    private RiskLevel airQualityRisk;
    private RiskLevel floodRisk;
    private RiskLevel overallRisk;
    private Instant recordedAt;
}