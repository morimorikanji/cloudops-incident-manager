package com.cloudops.incidentmanager.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.cloudops.incidentmanager.model.Alert;
import com.cloudops.incidentmanager.model.AlertSeverity;
import com.cloudops.incidentmanager.model.AlertStatus;

public interface AlertRepository extends JpaRepository<Alert, UUID> {

    Page<Alert> findByStatus(AlertStatus status, Pageable pageable);

    Page<Alert> findBySeverity(AlertSeverity severity, Pageable pageable);

    List<Alert> findByServiceId(UUID serviceId);

    List<Alert> findByIncidentId(UUID incidentId);

    long countByStatus(AlertStatus status);
}
