package com.cloudops.incidentmanager.dto;

import com.cloudops.incidentmanager.model.IncidentStatus;

import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(
        @NotNull(message = "Status is required")
        IncidentStatus status
) {}
