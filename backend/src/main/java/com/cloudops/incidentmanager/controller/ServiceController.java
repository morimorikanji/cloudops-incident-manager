package com.cloudops.incidentmanager.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudops.incidentmanager.dto.ServiceRequest;
import com.cloudops.incidentmanager.dto.ServiceResponse;
import com.cloudops.incidentmanager.dto.UpdateServiceRequest;
import com.cloudops.incidentmanager.model.ServiceStatus;
import com.cloudops.incidentmanager.model.ServiceTier;
import com.cloudops.incidentmanager.service.ServiceService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/services")
public class ServiceController {

    private final ServiceService serviceService;

    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @GetMapping
    public ResponseEntity<Page<ServiceResponse>> getServices(
            @RequestParam(required = false) ServiceStatus status,
            @RequestParam(required = false) ServiceTier tier,
            @RequestParam(required = false) UUID teamId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return ResponseEntity.ok(serviceService.getServices(status, tier, teamId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponse> getService(@PathVariable UUID id) {
        return ResponseEntity.ok(serviceService.getService(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ServiceResponse> createService(
            @Valid @RequestBody ServiceRequest request) {
        ServiceResponse response = serviceService.createService(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ServiceResponse> updateService(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateServiceRequest request) {
        return ResponseEntity.ok(serviceService.updateService(id, request));
    }
}
