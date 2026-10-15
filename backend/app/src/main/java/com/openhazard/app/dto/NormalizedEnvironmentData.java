package com.openhazard.app.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class NormalizedEnvironmentData {
    private Double temperatureCelsius;
    private Double humidityPercentage;
    private Integer aqiValue;
    private Double rainfallMmPerHour;
    private Double riverLevelMeters;
    private Instant timestamp;
}