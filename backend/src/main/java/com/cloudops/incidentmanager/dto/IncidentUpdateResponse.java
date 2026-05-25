package com.cloudops.incidentmanager.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.cloudops.incidentmanager.model.UpdateType;

public record IncidentUpdateResponse(
        UUID id,
        UUID incidentId,
        UserInfo author,
        UpdateType updateType,
        String message,
        OffsetDateTime createdAt
) {
    public record UserInfo(UUID id, String email, String displayName) {}
}
