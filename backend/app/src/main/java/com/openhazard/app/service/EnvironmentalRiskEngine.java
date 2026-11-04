package com.openhazard.app.service;

import com.openhazard.app.dto.NormalizedEnvironmentData;
import com.openhazard.app.dto.RiskAssessmentResult;
import com.openhazard.app.enums.RiskLevel;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentalRiskEngine {

    public RiskAssessmentResult evaluate(NormalizedEnvironmentData data) {
        RiskLevel heatRisk = calculateHeatRisk(data.getTemperatureCelsius(), data.getHumidityPercentage());
        RiskLevel airRisk = calculateAirQualityRisk(data.getAqiValue());
        RiskLevel floodRisk = calculateFloodRisk(data.getRainfallMmPerHour(), data.getRiverLevelMeters());

        RiskLevel overallRisk = RiskLevel.LOW;
        overallRisk = max(overallRisk, heatRisk);
        overallRisk = max(overallRisk, airRisk);
        overallRisk = max(overallRisk, floodRisk);

        return RiskAssessmentResult.builder()
                .heatRisk(heatRisk)
                .airQualityRisk(airRisk)
                .floodRisk(floodRisk)
                .overallRisk(overallRisk)
                .build();
    }

    private RiskLevel calculateHeatRisk(Double tempC, Double humidity) {
        if (tempC == null) return RiskLevel.LOW;

        double heatIndex = tempC;
        if (humidity != null && tempC >= 27.0) {
            // Simplified Rothfusz Heat Index regression approximation (°C)
            double e = (humidity / 100.0) * 6.105 * Math.exp((17.27 * tempC) / (237.7 + tempC));
            heatIndex = tempC + 0.33 * e - 0.70 * 4.0 - 4.00;
        }

        if (heatIndex >= 45.0) return RiskLevel.CRITICAL; // Extreme Danger
        if (heatIndex >= 38.0) return RiskLevel.HIGH;     // Danger
        if (heatIndex >= 32.0) return RiskLevel.MODERATE; // Extreme Caution
        return RiskLevel.LOW;
    }

    private RiskLevel calculateAirQualityRisk(Integer aqi) {
        if (aqi == null) return RiskLevel.LOW;

        if (aqi >= 201) return RiskLevel.CRITICAL; // Hazardous / Very Unhealthy
        if (aqi >= 151) return RiskLevel.HIGH;     // Unhealthy
        if (aqi >= 101) return RiskLevel.MODERATE; // Unhealthy for Sensitive Groups
        return RiskLevel.LOW;
    }

    private RiskLevel calculateFloodRisk(Double rainfallMm, Double riverLevelMeters) {
        double rain = rainfallMm != null ? rainfallMm : 0.0;
        double river = riverLevelMeters != null ? riverLevelMeters : 0.0;

        // Real-world intensity bands for hourly precipitation
        if (rain >= 50.0 || river >= 6.0) return RiskLevel.CRITICAL; // Torrential Downpour / Major Inundation
        if (rain >= 25.0 || river >= 4.0) return RiskLevel.HIGH;     // Heavy Rainfall / Minor Flood Alert
        if (rain >= 10.0 || river >= 2.5) return RiskLevel.MODERATE; // Moderate Rain
        return RiskLevel.LOW;
    }

    private RiskLevel max(RiskLevel a, RiskLevel b) {
        return a.ordinal() > b.ordinal() ? a : b;
    }
}