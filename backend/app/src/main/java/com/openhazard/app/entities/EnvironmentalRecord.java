package com.openhazard.app.entities;

import com.openhazard.app.enums.RiskLevel;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "environmental_records", indexes = {
    @Index(name = "idx_record_location_timestamp", columnList = "location_id, recorded_at DESC"),
    @Index(name = "idx_record_timestamp", columnList = "recorded_at DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnvironmentalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "temperature_celsius")
    private Double temperatureCelsius;

    @Column(name = "humidity_percentage")
    private Double humidityPercentage;

    @Column(name = "aqi_value")
    private Integer aqiValue;

    @Column(name = "rainfall_mm_per_hour")
    private Double rainfallMmPerHour;

    @Column(name = "river_level_meters")
    private Double riverLevelMeters;

    @Enumerated(EnumType.STRING)
    @Column(name = "heat_risk", nullable = false, length = 20)
    private RiskLevel heatRisk;

    @Enumerated(EnumType.STRING)
    @Column(name = "air_quality_risk", nullable = false, length = 20)
    private RiskLevel airQualityRisk;

    @Enumerated(EnumType.STRING)
    @Column(name = "flood_risk", nullable = false, length = 20)
    private RiskLevel floodRisk;

    @Enumerated(EnumType.STRING)
    @Column(name = "overall_risk", nullable = false, length = 20)
    private RiskLevel overallRisk;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;
}