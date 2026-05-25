package com.cloudops.incidentmanager.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.cloudops.incidentmanager.model.IncidentSeverity;
import com.cloudops.incidentmanager.model.IncidentStatus;

public record IncidentResponse(
        UUID id,
        String title,
        String description,
        ServiceInfo service,
        IncidentSeverity severity,
        IncidentStatus status,
        UserInfo assignee,
        UserInfo createdBy,
        OffsetDateTime startedAt,
        OffsetDateTime resolvedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<IncidentUpdateResponse> updates
) {
    public record ServiceInfo(UUID id, String name) {}
    public record UserInfo(UUID id, String email, String displayName) {}
}
