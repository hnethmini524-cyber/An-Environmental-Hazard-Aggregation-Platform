package com.openhazard.app.repository;

import com.openhazard.app.entities.EnvironmentalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnvironmentalRecordRepository extends JpaRepository<EnvironmentalRecord, Long> {

    Optional<EnvironmentalRecord> findTopByLocationIdOrderByRecordedAtDesc(Long locationId);

    @Query("SELECT r FROM EnvironmentalRecord r WHERE r.location.id = :locationId AND r.recordedAt >= :since ORDER BY r.recordedAt ASC")
    List<EnvironmentalRecord> findHistoricalTrends(@Param("locationId") Long locationId, @Param("since") Instant since);
}