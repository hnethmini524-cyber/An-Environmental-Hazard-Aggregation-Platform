package com.openhazard.app.controller;

import com.openhazard.app.dto.LocationDetailDTO;
import com.openhazard.app.dto.LocationOverviewDTO;
import com.openhazard.app.service.EnvironmentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
public class LocationController {

    private final EnvironmentalService environmentalService;

    @GetMapping
    public List<LocationOverviewDTO> getAllLocationsOverview() {
    	return environmentalService.getAllLocationsOverview().getLocations();
    }

    @GetMapping("/{name}")
    public ResponseEntity<LocationDetailDTO> getLocationDetails(@PathVariable String name) {
        return ResponseEntity.ok(environmentalService.getLocationDetails(name));
    }
}
