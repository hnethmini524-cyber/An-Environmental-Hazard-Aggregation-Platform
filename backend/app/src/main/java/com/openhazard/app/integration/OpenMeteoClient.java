package com.openhazard.app.integration;

import com.openhazard.app.dto.OpenMeteoResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;

@Component
@Slf4j
public class OpenMeteoClient {

    private final RestClient restClient;

    public OpenMeteoClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl("https://api.open-meteo.com/v1")
                .build();
    }

    public Optional<OpenMeteoResponseDTO> fetchCurrentWeather(BigDecimal latitude, BigDecimal longitude) {
        try {
            OpenMeteoResponseDTO response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/forecast")
                            .queryParam("latitude", latitude)
                            .queryParam("longitude", longitude)
                            .queryParam("current", "temperature_2m,relative_humidity_2m,precipitation")
                            .queryParam("timezone", "Asia/Colombo")
                            .build())
                    .retrieve()
                    .body(OpenMeteoResponseDTO.class);

            return Optional.ofNullable(response);
        } catch (Exception ex) {
            log.error("Failed to fetch weather data from Open-Meteo for lat: {}, lon: {}. Cause: {}", 
                    latitude, longitude, ex.getMessage());
            // returns empty Optional without crashing ingestion pipeline
            return Optional.empty();
        }
    }
}
