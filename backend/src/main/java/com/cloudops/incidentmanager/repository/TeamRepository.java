package com.cloudops.incidentmanager.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cloudops.incidentmanager.model.Team;

public interface TeamRepository extends JpaRepository<Team, UUID> {

    Optional<Team> findByName(String name);

    boolean existsByName(String name);
}
