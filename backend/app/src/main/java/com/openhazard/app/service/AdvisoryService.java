package com.openhazard.app.service;

import com.openhazard.app.dto.EnvironmentalRecordDTO;
import com.openhazard.app.dto.SafetyAdvisoryDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdvisoryService {

    private final ChatClient.Builder chatClientBuilder;

    @Cacheable(value = "safety-advisories", key = "#locationName")
    public SafetyAdvisoryDTO generateAdvisory(String locationName, EnvironmentalRecordDTO record) {
        log.info("Generating GenAI Safety Advisory for location: {}", locationName);

        ChatClient chatClient = chatClientBuilder.build();

        String prompt = String.format("""
            You are an emergency environmental hazard response AI for Sri Lanka.
            Analyze the following weather & risk metrics for location '%s':
            - Temperature: %s °C
            - Humidity: %s %%
            - Air Quality (AQI): %s
            - Rainfall: %s mm/hr
            - Overall Risk Level: %s
            - Heat Risk: %s
            - Air Quality Risk: %s
            - Flood Risk: %s

            Provide concise, highly actionable public safety guidance.
            Format your response STRICTLY as follows:
            HEADLINE: [One sentence summary]
            ACTIONS: [3 bullet points separated by pipe '|' symbol]
            
            Example output format:
            HEADLINE: Severe rainfall in Ratnapura increases immediate localized flash flood risks.
            ACTIONS: Avoid crossing low-lying river paths near Kalu Ganga.|Prepare emergency power supplies and move valuable items to higher ground.|Stay tuned to local disaster management alerts for evacuation notices.
            """,
            locationName,
            record.getTemperatureCelsius() != null ? record.getTemperatureCelsius() : "N/A",
            record.getHumidityPercentage() != null ? record.getHumidityPercentage() : "N/A",
            record.getAqiValue() != null ? record.getAqiValue() : "N/A",
            record.getRainfallMmPerHour() != null ? record.getRainfallMmPerHour() : "N/A",
            record.getOverallRisk(),
            record.getHeatRisk(),
            record.getAirQualityRisk(),
            record.getFloodRisk()
        );

        try {
            String response = chatClient.prompt().user(prompt).call().content();
            return parseLlmResponse(locationName, response);
        } catch (Exception e) {
            log.error("Failed to generate GenAI advisory for {}. Falling back to default advisory. Cause: {}", locationName, e.getMessage());
            return fallbackAdvisory(locationName, record);
        }
    }

    private SafetyAdvisoryDTO parseLlmResponse(String locationName, String rawLlmOutput) {
        String headline = "Environmental conditions monitored in " + locationName + ".";
        List<String> actions = List.of(
            "Monitor local weather forecasts for updates.",
            "Stay hydrated and avoid peak sun exposure.",
            "Follow instructions from local disaster management authorities."
        );

        String[] lines = rawLlmOutput.split("\n");
        for (String line : lines) {
            if (line.startsWith("HEADLINE:")) {
                headline = line.replace("HEADLINE:", "").trim();
            } else if (line.startsWith("ACTIONS:")) {
                String actionsRaw = line.replace("ACTIONS:", "").trim();
                actions = Arrays.stream(actionsRaw.split("\\|"))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();
            }
        }

        return SafetyAdvisoryDTO.builder()
                .locationName(locationName)
                .summaryHeadline(headline)
                .keyActionSteps(actions)
                .build();
    }

    private SafetyAdvisoryDTO fallbackAdvisory(String locationName, EnvironmentalRecordDTO record) {
        return SafetyAdvisoryDTO.builder()
                .locationName(locationName)
                .summaryHeadline(String.format("Standard monitoring active for %s. Overall Risk: %s.", locationName, record.getOverallRisk()))
                .keyActionSteps(List.of(
                        "Stay hydrated during warm afternoon hours.",
                        "Check local road conditions before heavy travel.",
                        "Follow guidance from Sri Lanka Disaster Management Centre (DMC)."
                ))
                .build();
    }
}