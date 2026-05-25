package com.cloudops.incidentmanager.repository;

import java.util.Collection;
import java.util.Optional;
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

    @Query(value = """
            SELECT i FROM Incident i
            WHERE (:status IS NULL OR i.status = :status)
            AND (:severity IS NULL OR i.severity = :severity)
            AND (:serviceId IS NULL OR i.service.id = :serviceId)
            """,
           countQuery = """
            SELECT COUNT(i) FROM Incident i
            WHERE (:status IS NULL OR i.status = :status)
            AND (:severity IS NULL OR i.severity = :severity)
            AND (:serviceId IS NULL OR i.service.id = :serviceId)
            """)
    Page<Incident> findWithFilters(@Param("status") IncidentStatus status,
                                   @Param("severity") IncidentSeverity severity,
                                   @Param("serviceId") UUID serviceId,
                                   Pageable pageable);

    @Query("""
            SELECT i FROM Incident i
            LEFT JOIN FETCH i.service
            LEFT JOIN FETCH i.createdBy
            LEFT JOIN FETCH i.assignee
            WHERE i.id = :id
            """)
    Optional<Incident> findByIdWithDetails(@Param("id") UUID id);

    long countByStatus(IncidentStatus status);

    long countByServiceIdAndStatusNotIn(UUID serviceId, Collection<IncidentStatus> statuses);
}
