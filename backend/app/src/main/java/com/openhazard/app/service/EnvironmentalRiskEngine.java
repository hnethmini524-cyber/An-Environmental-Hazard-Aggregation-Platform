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

        // Overall risk is derived from the maximum severity among sub-hazards
        RiskLevel overallRisk = RiskLevel.LOW;
        overallRisk = RiskLevel.max(overallRisk, heatRisk);
        overallRisk = RiskLevel.max(overallRisk, airRisk);
        overallRisk = RiskLevel.max(overallRisk, floodRisk);

        return RiskAssessmentResult.builder()
                .heatRisk(heatRisk)
                .airQualityRisk(airRisk)
                .floodRisk(floodRisk)
                .overallRisk(overallRisk)
                .build();
    }

    private RiskLevel calculateHeatRisk(Double tempC, Double humidity) {
        if (tempC == null) return RiskLevel.LOW;

        // Calculate Heat Index approximation if humidity is available
        double effectiveTemp = tempC;
        if (humidity != null && tempC >= 27.0) {
            effectiveTemp = tempC + (0.5555 * ((6.11 * Math.exp(5417.7530 * ((1/273.16) - (1/(273.15 + tempC))))) - 10.0));
        }

        if (effectiveTemp >= 40.0) return RiskLevel.CRITICAL;
        if (effectiveTemp >= 35.0) return RiskLevel.HIGH;
        if (effectiveTemp >= 30.0) return RiskLevel.MODERATE;
        return RiskLevel.LOW;
    }

    private RiskLevel calculateAirQualityRisk(Integer aqi) {
        if (aqi == null) return RiskLevel.LOW;

        if (aqi >= 200) return RiskLevel.CRITICAL;
        if (aqi >= 151) return RiskLevel.HIGH;
        if (aqi >= 101) return RiskLevel.MODERATE;
        return RiskLevel.LOW;
    }

    private RiskLevel calculateFloodRisk(Double rainfallMm, Double riverLevelMeters) {
        boolean highRain = rainfallMm != null && rainfallMm > 50.0;
        boolean moderateRain = rainfallMm != null && rainfallMm > 20.0;
        boolean highRiver = riverLevelMeters != null && riverLevelMeters > 4.0;

        if (highRain || highRiver) return RiskLevel.HIGH;
        if (moderateRain) return RiskLevel.MODERATE;
        return RiskLevel.LOW;
    }
}