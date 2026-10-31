package com.openhazard.app.repository;

import com.openhazard.app.entities.Alert;
import com.openhazard.app.enums.HazardType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
	
	@Query("SELECT a FROM Alert a JOIN FETCH a.location ORDER BY a.createdAt DESC")
    List<Alert> findTop50ByOrderByCreatedAtDesc();
	
	boolean existsByLocationIdAndHazardTypeAndCreatedAtAfter(Long locationId, HazardType hazardType, Instant timestamp);
}