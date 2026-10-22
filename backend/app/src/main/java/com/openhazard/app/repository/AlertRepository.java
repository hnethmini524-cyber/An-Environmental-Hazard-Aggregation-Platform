package com.openhazard.app.repository;

import com.openhazard.app.entities.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
	
	@Query("SELECT a FROM Alert a JOIN FETCH a.location ORDER BY a.createdAt DESC")
    List<Alert> findTop50ByOrderByCreatedAtDesc();
}