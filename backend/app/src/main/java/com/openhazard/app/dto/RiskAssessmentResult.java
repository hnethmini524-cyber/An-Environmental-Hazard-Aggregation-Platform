package com.openhazard.app.dto;

import com.openhazard.app.enums.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskAssessmentResult {
    private RiskLevel heatRisk;
    private RiskLevel airQualityRisk;
    private RiskLevel floodRisk;
    private RiskLevel overallRisk;
}
