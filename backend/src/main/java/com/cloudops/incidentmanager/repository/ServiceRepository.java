package com.cloudops.incidentmanager.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.cloudops.incidentmanager.model.Service;
import com.cloudops.incidentmanager.model.ServiceStatus;
import com.cloudops.incidentmanager.model.ServiceTier;

public interface ServiceRepository extends JpaRepository<Service, UUID> {

    Optional<Service> findByName(String name);

    boolean existsByName(String name);

    Page<Service> findByStatus(ServiceStatus status, Pageable pageable);

    Page<Service> findByTier(ServiceTier tier, Pageable pageable);

    List<Service> findByTeamId(UUID teamId);
}
