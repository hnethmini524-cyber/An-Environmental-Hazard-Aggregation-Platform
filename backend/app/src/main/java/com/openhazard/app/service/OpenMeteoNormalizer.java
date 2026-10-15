package com.openhazard.app.service;

import com.openhazard.app.dto.NormalizedEnvironmentData;
import com.openhazard.app.dto.OpenMeteoResponseDTO;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class OpenMeteoNormalizer {

    public NormalizedEnvironmentData normalize(OpenMeteoResponseDTO rawResponse) {
        if (rawResponse == null || rawResponse.getCurrent() == null) {
            return NormalizedEnvironmentData.builder()
                    .timestamp(Instant.now())
                    .build();
        }

        var current = rawResponse.getCurrent();

        Double temp = sanitizeDouble(current.getTemperature2m(), -50.0, 60.0);
        Double humidity = sanitizeDouble(current.getRelativeHumidity2m(), 0.0, 100.0);
        Double rain = sanitizeDouble(current.getPrecipitation(), 0.0, 500.0);

        return NormalizedEnvironmentData.builder()
                .temperatureCelsius(temp)
                .humidityPercentage(humidity)
                .rainfallMmPerHour(rain)
                .timestamp(Instant.now())
                .build();
    }

    private Double sanitizeDouble(Double value, double minBoundary, double maxBoundary) {
        if (value == null || Double.isNaN(value) || value < minBoundary || value > maxBoundary) {
            return null; // Treated as missing data gracefully by downstream risk engine
        }
        return value;
    }
}