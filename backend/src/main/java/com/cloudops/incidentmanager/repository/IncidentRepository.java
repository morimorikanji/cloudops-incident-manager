package com.cloudops.incidentmanager.repository;

import java.util.Collection;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cloudops.incidentmanager.model.Incident;
import com.cloudops.incidentmanager.model.IncidentSeverity;
import com.cloudops.incidentmanager.model.IncidentStatus;

public interface IncidentRepository extends JpaRepository<Incident, UUID> {

    Page<Incident> findByStatus(IncidentStatus status, Pageable pageable);

    Page<Incident> findBySeverity(IncidentSeverity severity, Pageable pageable);

    Page<Incident> findByServiceId(UUID serviceId, Pageable pageable);

    Page<Incident> findByAssigneeId(UUID assigneeId, Pageable pageable);

    @Query("""
            SELECT i FROM Incident i
            JOIN FETCH i.service
            JOIN FETCH i.createdBy
            WHERE i.id = :id
            """)
    java.util.Optional<Incident> findByIdWithDetails(@Param("id") UUID id);

    long countByStatus(IncidentStatus status);

    long countByServiceIdAndStatusNotIn(UUID serviceId, Collection<IncidentStatus> statuses);
}
