package com.openhazard.app.dto;
import com.openhazard.app.enums.RiskLevel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationOverviewDTO implements Serializable {
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
