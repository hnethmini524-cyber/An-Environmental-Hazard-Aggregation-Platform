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

    // Optimized single-query fetch for latest records across multiple location IDs (Solves N+1 problem)
    @Query("""
        SELECT r FROM EnvironmentalRecord r 
        WHERE r.id IN (
            SELECT MAX(r2.id) FROM EnvironmentalRecord r2 
            WHERE r2.location.id IN :locationIds 
            GROUP BY r2.location.id
        )
    """)
    List<EnvironmentalRecord> findLatestRecordsForLocations(@Param("locationIds") List<Long> locationIds);
}