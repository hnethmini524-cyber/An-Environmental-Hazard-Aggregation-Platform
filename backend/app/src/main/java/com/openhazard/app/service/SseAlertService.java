package com.openhazard.app.service;

import com.openhazard.app.dto.AlertDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.CopyOnWriteArrayList;

@Service
@Slf4j
public class SseAlertService {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(60_000L * 30); // 30-minute timeout

        emitters.add(emitter);

        emitter.onCompletion(() -> this.emitters.remove(emitter));
        emitter.onTimeout(() -> {
            emitter.complete();
            this.emitters.remove(emitter);
        });
        emitter.onError(e -> {
            log.warn("SSE connection error: {}", e.getMessage());
            this.emitters.remove(emitter);
        });

        // Send connection acknowledgment event
        try {
            emitter.send(SseEmitter.event()
                    .name("INIT")
                    .data("Connected to OpenHazard SSE Alert Stream"));
        } catch (IOException e) {
            this.emitters.remove(emitter);
        }

        return emitter;
    }

    public void broadcastAlert(AlertDTO alert) {
        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("HAZARD_ALERT")
                        .data(alert));
            } catch (Exception ex) {
                deadEmitters.add(emitter);
            }
        }

        this.emitters.removeAll(deadEmitters);
    }
}
