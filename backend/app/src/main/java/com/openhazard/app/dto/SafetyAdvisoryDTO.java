package com.openhazard.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SafetyAdvisoryDTO implements Serializable {
    private String locationName;
    private String summaryHeadline;
    private List<String> keyActionSteps;
}