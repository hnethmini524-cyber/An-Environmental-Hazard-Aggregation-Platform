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
        if (locationRepository.count() == 0) {
            log.info("Seeding initial Sri Lankan locations into database...");

            List<Location> locations = List.of(
                    Location.builder().name("Colombo").district("Colombo").province("Western").latitude(new BigDecimal("6.9271")).longitude(new BigDecimal("79.8612")).build(),
                    Location.builder().name("Kandy").district("Kandy").province("Central").latitude(new BigDecimal("7.2906")).longitude(new BigDecimal("80.6337")).build(),
                    Location.builder().name("Galle").district("Galle").province("Southern").latitude(new BigDecimal("6.0535")).longitude(new BigDecimal("80.2210")).build(),
                    Location.builder().name("Jaffna").district("Jaffna").province("Northern").latitude(new BigDecimal("9.6615")).longitude(new BigDecimal("80.0255")).build(),
                    Location.builder().name("Trincomalee").district("Trincomalee").province("Eastern").latitude(new BigDecimal("8.5874")).longitude(new BigDecimal("81.2152")).build(),
                    Location.builder().name("Negombo").district("Gampaha").province("Western").latitude(new BigDecimal("7.2008")).longitude(new BigDecimal("79.8737")).build()
            );

            locationRepository.saveAll(locations);
            log.info("Seeded {} locations successfully.", locations.size());
        }
    }
}