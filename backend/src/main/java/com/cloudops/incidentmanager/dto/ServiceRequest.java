package com.cloudops.incidentmanager.dto;

import java.util.UUID;

import com.cloudops.incidentmanager.model.ServiceTier;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ServiceRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,

        String description,

        UUID teamId,

        @NotNull(message = "Tier is required")
        ServiceTier tier,

        @Size(max = 500, message = "Endpoint URL must not exceed 500 characters")
        String endpointUrl,

        @Size(max = 500, message = "Repository URL must not exceed 500 characters")
        String repositoryUrl
) {}
