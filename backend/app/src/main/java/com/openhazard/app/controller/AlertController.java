package com.openhazard.app.controller;

import com.openhazard.app.dto.AlertDTO;
import com.openhazard.app.repository.AlertRepository;
import com.openhazard.app.service.SseAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final SseAlertService sseAlertService;
    private final AlertRepository alertRepository;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeToAlerts() {
        return sseAlertService.subscribe();
    }

    @GetMapping
    public ResponseEntity<List<AlertDTO>> getRecentAlerts() {
        List<AlertDTO> alerts = alertRepository.findTop50ByOrderByCreatedAtDesc().stream()
                .map(a -> AlertDTO.builder()
                        .id(a.getId())
                        .locationName(a.getLocation().getName())
                        .hazardType(a.getHazardType())
                        .severity(a.getSeverity())
                        .message(a.getMessage())
                        .createdAt(a.getCreatedAt())
                        .build())
                .toList();

        return ResponseEntity.ok(alerts);
    }
}