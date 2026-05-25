package com.cloudops.incidentmanager.dto;

import com.cloudops.incidentmanager.model.UpdateType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record IncidentUpdateRequest(
        @NotNull(message = "Update type is required")
        UpdateType updateType,

        @NotBlank(message = "Message is required")
        @Size(max = 5000, message = "Message must not exceed 5000 characters")
        String message
) {}
