package com.openhazard.app.config;

import com.openhazard.app.entities.Location;
import com.openhazard.app.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final LocationRepository locationRepository;

    @Override
    public void run(String... args) {
        log.info("Checking and seeding monitored hubs...");

        List<Location> targetHubs = List.of(
            createHub("Colombo", "Colombo", "Western", "6.9271", "79.8612"),
            createHub("Kurunegala", "Kurunegala", "North Western", "7.4863", "80.3647"),
            createHub("Anuradhapura", "Anuradhapura", "North Central", "8.3114", "80.4037"),
            createHub("Kandy", "Kandy", "Central", "7.2906", "80.6337"),
            createHub("Galle", "Galle", "Southern", "6.0535", "80.2210"),
            createHub("Jaffna", "Jaffna", "Northern", "9.6615", "80.0255"),
            createHub("Trincomalee", "Trincomalee", "Eastern", "8.5874", "81.2152"),
            createHub("Negombo", "Gampaha", "Western", "7.2008", "79.8737"),
            createHub("Nuwara Eliya", "Nuwara Eliya", "Central", "6.9497", "80.7891"),
            createHub("Ratnapura", "Ratnapura", "Sabaragamuwa", "6.6828", "80.3992")
        );

        for (Location hub : targetHubs) {
            locationRepository.findByNameIgnoreCase(hub.getName())
                .ifPresentOrElse(
                    existing -> {
                        if (existing.getIsMonitored() == null || !existing.getIsMonitored() || !existing.getIsActive()) {
                            existing.setIsMonitored(true);
                            existing.setIsActive(true);
                            locationRepository.save(existing);
                        }
                    },
                    () -> locationRepository.save(hub)
                );
        }

        log.info("Monitored hubs initialization complete. Total active locations in DB: {}", locationRepository.count());
    }

    private Location createHub(String name, String district, String province, String lat, String lon) {
        return Location.builder()
                .name(name)
                .district(district)
                .province(province)
                .latitude(new BigDecimal(lat))
                .longitude(new BigDecimal(lon))
                .isActive(true)
                .isMonitored(true)
                .build();
    }
}