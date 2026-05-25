package com.cloudops.incidentmanager.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudops.incidentmanager.dto.IncidentRequest;
import com.cloudops.incidentmanager.dto.IncidentResponse;
import com.cloudops.incidentmanager.dto.IncidentUpdateRequest;
import com.cloudops.incidentmanager.dto.IncidentUpdateResponse;
import com.cloudops.incidentmanager.dto.StatusUpdateRequest;
import com.cloudops.incidentmanager.exception.InvalidStatusTransitionException;
import com.cloudops.incidentmanager.exception.ResourceNotFoundException;
import com.cloudops.incidentmanager.model.Incident;
import com.cloudops.incidentmanager.model.IncidentSeverity;
import com.cloudops.incidentmanager.model.IncidentStatus;
import com.cloudops.incidentmanager.model.IncidentUpdate;
import com.cloudops.incidentmanager.model.UpdateType;
import com.cloudops.incidentmanager.model.User;
import com.cloudops.incidentmanager.repository.IncidentRepository;
import com.cloudops.incidentmanager.repository.IncidentUpdateRepository;
import com.cloudops.incidentmanager.repository.ServiceRepository;
import com.cloudops.incidentmanager.repository.UserRepository;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final IncidentUpdateRepository incidentUpdateRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;

    public IncidentService(IncidentRepository incidentRepository,
                           IncidentUpdateRepository incidentUpdateRepository,
                           ServiceRepository serviceRepository,
                           UserRepository userRepository) {
        this.incidentRepository = incidentRepository;
        this.incidentUpdateRepository = incidentUpdateRepository;
        this.serviceRepository = serviceRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<IncidentResponse> getIncidents(IncidentStatus status, IncidentSeverity severity,
                                               UUID serviceId, Pageable pageable) {
        return incidentRepository.findWithFilters(status, severity, serviceId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public IncidentResponse getIncident(UUID id) {
        Incident incident = incidentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found: " + id));
        List<IncidentUpdate> updates =
                incidentUpdateRepository.findByIncidentIdOrderByCreatedAtAsc(id);
        return toDetailResponse(incident, updates);
    }

    @Transactional
    public IncidentResponse createIncident(IncidentRequest request, String userEmail) {
        User creator = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        com.cloudops.incidentmanager.model.Service service =
                serviceRepository.findById(request.serviceId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Service not found: " + request.serviceId()));

        User assignee = null;
        if (request.assigneeId() != null) {
            assignee = userRepository.findById(request.assigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Assignee not found: " + request.assigneeId()));
        }

        Incident incident = new Incident();
        incident.setTitle(request.title());
        incident.setDescription(request.description());
        incident.setService(service);
        incident.setSeverity(request.severity());
        incident.setStatus(IncidentStatus.OPEN);
        incident.setAssignee(assignee);
        incident.setCreatedBy(creator);
        incident.setStartedAt(request.startedAt());

        return toResponse(incidentRepository.save(incident));
    }

    @Transactional
    public IncidentResponse updateStatus(UUID id, StatusUpdateRequest request, String userEmail) {
        Incident incident = incidentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found: " + id));

        validateStatusTransition(incident.getStatus(), request.status());

        IncidentStatus previousStatus = incident.getStatus();
        incident.setStatus(request.status());
        if (request.status() == IncidentStatus.RESOLVED
                || request.status() == IncidentStatus.CLOSED) {
            incident.setResolvedAt(OffsetDateTime.now());
        }

        User actor = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        IncidentUpdate statusUpdate = new IncidentUpdate();
        statusUpdate.setIncident(incident);
        statusUpdate.setAuthor(actor);
        statusUpdate.setUpdateType(UpdateType.STATUS_CHANGE);
        statusUpdate.setMessage(previousStatus.name() + " → " + request.status().name());
        incidentUpdateRepository.save(statusUpdate);

        return toResponse(incidentRepository.save(incident));
    }

    @Transactional
    public IncidentUpdateResponse addUpdate(UUID id, IncidentUpdateRequest request, String userEmail) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found: " + id));

        User author = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        IncidentUpdate update = new IncidentUpdate();
        update.setIncident(incident);
        update.setAuthor(author);
        update.setUpdateType(request.updateType());
        update.setMessage(request.message());

        return toUpdateResponse(incidentUpdateRepository.save(update));
    }

    private void validateStatusTransition(IncidentStatus current, IncidentStatus next) {
        if (current == next) return;
        if (current == IncidentStatus.CLOSED) {
            throw new InvalidStatusTransitionException(
                    "Cannot transition from CLOSED to " + next);
        }
        if (current == IncidentStatus.RESOLVED && next == IncidentStatus.OPEN) {
            throw new InvalidStatusTransitionException(
                    "Cannot transition from RESOLVED to OPEN");
        }
    }

    private IncidentResponse toResponse(Incident incident) {
        return toDetailResponse(incident, List.of());
    }

    private IncidentResponse toDetailResponse(Incident incident, List<IncidentUpdate> updates) {
        IncidentResponse.ServiceInfo serviceInfo = null;
        if (incident.getService() != null) {
            serviceInfo = new IncidentResponse.ServiceInfo(
                    incident.getService().getId(), incident.getService().getName());
        }

        IncidentResponse.UserInfo createdByInfo = null;
        if (incident.getCreatedBy() != null) {
            createdByInfo = new IncidentResponse.UserInfo(
                    incident.getCreatedBy().getId(),
                    incident.getCreatedBy().getEmail(),
                    incident.getCreatedBy().getDisplayName());
        }

        IncidentResponse.UserInfo assigneeInfo = null;
        if (incident.getAssignee() != null) {
            assigneeInfo = new IncidentResponse.UserInfo(
                    incident.getAssignee().getId(),
                    incident.getAssignee().getEmail(),
                    incident.getAssignee().getDisplayName());
        }

        List<IncidentUpdateResponse> updateResponses = updates.stream()
                .map(this::toUpdateResponse)
                .toList();

        return new IncidentResponse(
                incident.getId(),
                incident.getTitle(),
                incident.getDescription(),
                serviceInfo,
                incident.getSeverity(),
                incident.getStatus(),
                assigneeInfo,
                createdByInfo,
                incident.getStartedAt(),
                incident.getResolvedAt(),
                incident.getCreatedAt(),
                incident.getUpdatedAt(),
                updateResponses
        );
    }

    private IncidentUpdateResponse toUpdateResponse(IncidentUpdate update) {
        IncidentUpdateResponse.UserInfo authorInfo = null;
        if (update.getAuthor() != null) {
            authorInfo = new IncidentUpdateResponse.UserInfo(
                    update.getAuthor().getId(),
                    update.getAuthor().getEmail(),
                    update.getAuthor().getDisplayName());
        }

        return new IncidentUpdateResponse(
                update.getId(),
                update.getIncident().getId(),
                authorInfo,
                update.getUpdateType(),
                update.getMessage(),
                update.getCreatedAt()
        );
    }
}
