package com.cloudops.incidentmanager.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.cloudops.incidentmanager.model.ServiceStatus;
import com.cloudops.incidentmanager.model.ServiceTier;

public record ServiceResponse(
        UUID id,
        String name,
        String description,
        TeamInfo team,
        ServiceTier tier,
        ServiceStatus status,
        String endpointUrl,
        String repositoryUrl,
        long openIncidentCount,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public record TeamInfo(UUID id, String name) {}
}
