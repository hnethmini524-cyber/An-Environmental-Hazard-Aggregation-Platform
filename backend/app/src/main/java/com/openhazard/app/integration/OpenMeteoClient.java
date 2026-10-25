package com.openhazard.app.integration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openhazard.app.dto.OpenMeteoResponseDTO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Optional;

@Component
@Slf4j
public class OpenMeteoClient {

    private final RestClient weatherClient;
    private final RestClient airQualityClient;

    public OpenMeteoClient(RestClient.Builder restClientBuilder) {
        this.weatherClient = restClientBuilder
                .baseUrl("https://api.open-meteo.com/v1")
                .build();

        this.airQualityClient = restClientBuilder
                .baseUrl("https://air-quality-api.open-meteo.com/v1")
                .build();
    }

    public Optional<OpenMeteoResponseDTO> fetchCurrentWeather(BigDecimal latitude, BigDecimal longitude) {
        OpenMeteoResponseDTO weatherResponse = null;

        try {
            // Fetch Weather Data (Temperature, Humidity, Precipitation)
            weatherResponse = weatherClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/forecast")
                            .queryParam("latitude", latitude)
                            .queryParam("longitude", longitude)
                            .queryParam("current", "temperature_2m,relative_humidity_2m,precipitation")
                            .queryParam("timezone", "Asia/Colombo")
                            .build())
                    .retrieve()
                    .body(OpenMeteoResponseDTO.class);

        } catch (Exception ex) {
            log.error("Failed to fetch weather data from Open-Meteo for lat: {}, lon: {}. Cause: {}", 
                    latitude, longitude, ex.getMessage());
            return Optional.empty();
        }

        try {
            // Fetch Air Quality Data 
            AirQualityApiResponse airResponse = airQualityClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/air-quality")
                            .queryParam("latitude", latitude)
                            .queryParam("longitude", longitude)
                            .queryParam("current", "us_aqi,pm2_5")
                            .queryParam("timezone", "Asia/Colombo")
                            .build())
                    .retrieve()
                    .body(AirQualityApiResponse.class);

            // Merge Air Quality 
            if (weatherResponse != null && airResponse != null && airResponse.getCurrent() != null) {
                OpenMeteoResponseDTO.CurrentAirQuality airQuality = new OpenMeteoResponseDTO.CurrentAirQuality();
                airQuality.setUsAqi(airResponse.getCurrent().getUsAqi());
                airQuality.setPm25(airResponse.getCurrent().getPm25());

                weatherResponse.setCurrentAirQuality(airQuality);
            }
        } catch (Exception ex) {
            log.warn("Failed to fetch air quality data for lat: {}, lon: {}. Continuing with weather data only. Cause: {}", 
                    latitude, longitude, ex.getMessage());
        }

        return Optional.ofNullable(weatherResponse);
    }

    @Data
    public static class AirQualityApiResponse {
        private AirQualityCurrent current;

        @Data
        public static class AirQualityCurrent {
            @JsonProperty("us_aqi")
            private Integer usAqi;

            @JsonProperty("pm2_5")
            private Double pm25;
        }
    }
}