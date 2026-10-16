package com.openhazard.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NormalizedEnvironmentData {
    private Double temperatureCelsius;
    private Double humidityPercentage;
    private Integer aqiValue;
    private Double rainfallMmPerHour;
    private Double riverLevelMeters;
    private Instant timestamp;
}