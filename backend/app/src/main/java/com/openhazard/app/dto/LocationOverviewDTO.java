package com.openhazard.app.dto;

import com.openhazard.app.enums.RiskLevel;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class LocationOverviewDTO {
    private Long id;
    private String name;
    private String district;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private RiskLevel overallRisk;
    private RiskLevel heatRisk;
    private RiskLevel airQualityRisk;
    private RiskLevel floodRisk;
    private Instant updatedAt;
}
