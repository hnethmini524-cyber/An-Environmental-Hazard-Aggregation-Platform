package com.openhazard.app.worker;

import com.openhazard.app.entities.Location;
import com.openhazard.app.repository.LocationRepository;
import com.openhazard.app.service.EnvironmentalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.util.List;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class DataIngestionScheduler {

    private final LocationRepository locationRepository;
    private final EnvironmentalService environmentalService;

    // Runs every 15 minutes (900,000 milliseconds)
    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(fixedRate = 900000)
    public void scheduleEnvironmentalDataIngestion() {
        log.info("Starting scheduled environmental data ingestion cycle...");
        List<Location> activeLocations = locationRepository.findByIsActiveTrueAndIsMonitoredTrue();

        for (Location location : activeLocations) {
            try {
                environmentalService.fetchAndIngestLocationData(location);
            } catch (Exception e) {
                log.error("Failed ingestion cycle for location: {}. Cause: {}", location.getName(), e.getMessage());
            }
        }
        log.info("Ingestion cycle complete for {} locations.", activeLocations.size());
    }
}