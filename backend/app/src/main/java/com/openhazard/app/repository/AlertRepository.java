package com.openhazard.app.repository;

import com.openhazard.app.entities.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findTop50ByOrderByCreatedAtDesc();
}