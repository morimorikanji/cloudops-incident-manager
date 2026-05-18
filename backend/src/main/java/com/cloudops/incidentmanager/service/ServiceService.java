package com.cloudops.incidentmanager.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudops.incidentmanager.dto.ServiceRequest;
import com.cloudops.incidentmanager.dto.ServiceResponse;
import com.cloudops.incidentmanager.dto.UpdateServiceRequest;
import com.cloudops.incidentmanager.exception.DuplicateNameException;
import com.cloudops.incidentmanager.exception.ResourceNotFoundException;
import com.cloudops.incidentmanager.model.IncidentStatus;
import com.cloudops.incidentmanager.model.ServiceStatus;
import com.cloudops.incidentmanager.model.ServiceTier;
import com.cloudops.incidentmanager.model.Team;
import com.cloudops.incidentmanager.repository.IncidentRepository;
import com.cloudops.incidentmanager.repository.ServiceRepository;
import com.cloudops.incidentmanager.repository.TeamRepository;

@Service
public class ServiceService {

    private static final List<IncidentStatus> CLOSED_STATUSES =
            List.of(IncidentStatus.RESOLVED, IncidentStatus.CLOSED);

    private final ServiceRepository serviceRepository;
    private final TeamRepository teamRepository;
    private final IncidentRepository incidentRepository;

    public ServiceService(ServiceRepository serviceRepository,
                          TeamRepository teamRepository,
                          IncidentRepository incidentRepository) {
        this.serviceRepository = serviceRepository;
        this.teamRepository = teamRepository;
        this.incidentRepository = incidentRepository;
    }

    @Transactional(readOnly = true)
    public Page<ServiceResponse> getServices(ServiceStatus status, ServiceTier tier,
                                              UUID teamId, Pageable pageable) {
        return serviceRepository.findWithFilters(status, tier, teamId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ServiceResponse getService(UUID id) {
        com.cloudops.incidentmanager.model.Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + id));
        return toResponse(service);
    }

    @Transactional
    public ServiceResponse createService(ServiceRequest request) {
        if (serviceRepository.existsByName(request.name())) {
            throw new DuplicateNameException("Service name already exists: " + request.name());
        }

        com.cloudops.incidentmanager.model.Service service =
                new com.cloudops.incidentmanager.model.Service();
        service.setName(request.name());
        service.setDescription(request.description());
        service.setTier(request.tier());
        service.setStatus(ServiceStatus.OPERATIONAL);
        service.setEndpointUrl(request.endpointUrl());
        service.setRepositoryUrl(request.repositoryUrl());

        if (request.teamId() != null) {
            Team team = teamRepository.findById(request.teamId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Team not found: " + request.teamId()));
            service.setTeam(team);
        }

        return toResponse(serviceRepository.save(service));
    }

    @Transactional
    public ServiceResponse updateService(UUID id, UpdateServiceRequest request) {
        com.cloudops.incidentmanager.model.Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + id));

        if (request.name() != null
                && !request.name().equals(service.getName())
                && serviceRepository.existsByName(request.name())) {
            throw new DuplicateNameException("Service name already exists: " + request.name());
        }

        if (request.name() != null) service.setName(request.name());
        if (request.description() != null) service.setDescription(request.description());
        if (request.tier() != null) service.setTier(request.tier());
        if (request.status() != null) service.setStatus(request.status());
        if (request.endpointUrl() != null) service.setEndpointUrl(request.endpointUrl());
        if (request.repositoryUrl() != null) service.setRepositoryUrl(request.repositoryUrl());

        if (request.teamId() != null) {
            Team team = teamRepository.findById(request.teamId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Team not found: " + request.teamId()));
            service.setTeam(team);
        }

        return toResponse(serviceRepository.save(service));
    }

    private ServiceResponse toResponse(com.cloudops.incidentmanager.model.Service service) {
        ServiceResponse.TeamInfo teamInfo = null;
        if (service.getTeam() != null) {
            teamInfo = new ServiceResponse.TeamInfo(
                    service.getTeam().getId(), service.getTeam().getName());
        }

        long openCount = incidentRepository.countByServiceIdAndStatusNotIn(
                service.getId(), CLOSED_STATUSES);

        return new ServiceResponse(
                service.getId(),
                service.getName(),
                service.getDescription(),
                teamInfo,
                service.getTier(),
                service.getStatus(),
                service.getEndpointUrl(),
                service.getRepositoryUrl(),
                openCount,
                service.getCreatedAt(),
                service.getUpdatedAt()
        );
    }
}
