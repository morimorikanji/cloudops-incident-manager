package com.cloudops.incidentmanager.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.cloudops.incidentmanager.model.IncidentSeverity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record IncidentRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,

        String description,

        @NotNull(message = "Service ID is required")
        UUID serviceId,

        @NotNull(message = "Severity is required")
        IncidentSeverity severity,

        UUID assigneeId,

        @NotNull(message = "Start time is required")
        OffsetDateTime startedAt
) {}
