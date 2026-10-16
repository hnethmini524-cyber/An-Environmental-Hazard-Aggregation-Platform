package com.openhazard.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OpenMeteoResponseDTO {

    private Double latitude;
    private Double longitude;
    private CurrentUnits currentUnits;
    private CurrentData current;

    @Data
    public static class CurrentUnits {
        private String time;
        private String interval;

        @JsonProperty("temperature_2m")
        private String temperature2m;

        @JsonProperty("relative_humidity_2m")
        private String relativeHumidity2m;

        private String precipitation;
    }

    @Data
    public static class CurrentData {
        private String time;
        private Integer interval;

        @JsonProperty("temperature_2m")
        private Double temperature2m;

        @JsonProperty("relative_humidity_2m")
        private Double relativeHumidity2m;

        private Double precipitation;
    }
}