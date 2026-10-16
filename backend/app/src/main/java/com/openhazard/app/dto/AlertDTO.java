package com.openhazard.app.dto;

import com.openhazard.app.enums.HazardType;
import com.openhazard.app.enums.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertDTO {
    private Long id;
    private String locationName;
    private HazardType hazardType;
    private RiskLevel severity;
    private String message;
    private Instant createdAt;
}