package com.cloudops.incidentmanager.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cloudops.incidentmanager.model.IncidentUpdate;

public interface IncidentUpdateRepository extends JpaRepository<IncidentUpdate, UUID> {

    List<IncidentUpdate> findByIncidentIdOrderByCreatedAtAsc(UUID incidentId);
}
