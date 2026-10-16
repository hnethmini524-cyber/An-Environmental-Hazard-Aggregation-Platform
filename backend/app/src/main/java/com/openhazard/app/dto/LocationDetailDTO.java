package com.openhazard.app.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class LocationDetailDTO {
    private Long id;
    private String name;
    private String district;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private EnvironmentalRecordDTO currentRecord;
    private List<EnvironmentalRecordDTO> history;
}