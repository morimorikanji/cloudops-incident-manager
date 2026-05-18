package com.cloudops.incidentmanager.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cloudops.incidentmanager.model.Service;
import com.cloudops.incidentmanager.model.ServiceStatus;
import com.cloudops.incidentmanager.model.ServiceTier;

public interface ServiceRepository extends JpaRepository<Service, UUID> {

    Optional<Service> findByName(String name);

    boolean existsByName(String name);

    Page<Service> findByStatus(ServiceStatus status, Pageable pageable);

    Page<Service> findByTier(ServiceTier tier, Pageable pageable);

    List<Service> findByTeamId(UUID teamId);

    @Query(value = """
            SELECT s FROM Service s LEFT JOIN s.team t WHERE
            (:status IS NULL OR s.status = :status) AND
            (:tier IS NULL OR s.tier = :tier) AND
            (:teamId IS NULL OR t.id = :teamId)
            """,
           countQuery = """
            SELECT COUNT(s) FROM Service s LEFT JOIN s.team t WHERE
            (:status IS NULL OR s.status = :status) AND
            (:tier IS NULL OR s.tier = :tier) AND
            (:teamId IS NULL OR t.id = :teamId)
            """)
    Page<Service> findWithFilters(@Param("status") ServiceStatus status,
                                   @Param("tier") ServiceTier tier,
                                   @Param("teamId") UUID teamId,
                                   Pageable pageable);
}
