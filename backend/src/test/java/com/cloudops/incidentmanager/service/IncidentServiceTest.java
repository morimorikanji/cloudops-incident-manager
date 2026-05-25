package com.cloudops.incidentmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
import com.cloudops.incidentmanager.model.UserRole;
import com.cloudops.incidentmanager.repository.IncidentRepository;
import com.cloudops.incidentmanager.repository.IncidentUpdateRepository;
import com.cloudops.incidentmanager.repository.ServiceRepository;
import com.cloudops.incidentmanager.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class IncidentServiceTest {

    @Mock IncidentRepository incidentRepository;
    @Mock IncidentUpdateRepository incidentUpdateRepository;
    @Mock ServiceRepository serviceRepository;
    @Mock UserRepository userRepository;

    @InjectMocks IncidentService incidentService;

    private User adminUser;
    private com.cloudops.incidentmanager.model.Service testService;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(UUID.randomUUID());
        adminUser.setEmail("admin@test.com");
        adminUser.setDisplayName("Admin");
        adminUser.setPasswordHash("$2a$10$hash");
        adminUser.setRole(UserRole.ADMIN);

        testService = new com.cloudops.incidentmanager.model.Service();
        testService.setId(UUID.randomUUID());
        testService.setName("Payment Service");
    }

    // ── createIncident ──────────────────────────────────────────────────

    @Test
    void createIncident_success_returnsOpenIncident() {
        IncidentRequest request = new IncidentRequest(
                "DB unreachable", "Cannot connect to primary DB",
                testService.getId(), IncidentSeverity.P1,
                null, OffsetDateTime.now());

        Incident saved = buildIncident(UUID.randomUUID(), "DB unreachable",
                IncidentSeverity.P1, IncidentStatus.OPEN);

        when(userRepository.findByEmail(adminUser.getEmail())).thenReturn(Optional.of(adminUser));
        when(serviceRepository.findById(testService.getId())).thenReturn(Optional.of(testService));
        when(incidentRepository.save(any())).thenReturn(saved);

        IncidentResponse response = incidentService.createIncident(request, adminUser.getEmail());

        assertThat(response.title()).isEqualTo("DB unreachable");
        assertThat(response.status()).isEqualTo(IncidentStatus.OPEN);
        assertThat(response.severity()).isEqualTo(IncidentSeverity.P1);
    }

    @Test
    void createIncident_userNotFound_throwsResourceNotFound() {
        IncidentRequest request = new IncidentRequest(
                "title", null, testService.getId(),
                IncidentSeverity.P2, null, OffsetDateTime.now());

        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> incidentService.createIncident(request, "unknown@test.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void createIncident_serviceNotFound_throwsResourceNotFound() {
        IncidentRequest request = new IncidentRequest(
                "title", null, UUID.randomUUID(),
                IncidentSeverity.P2, null, OffsetDateTime.now());

        when(userRepository.findByEmail(adminUser.getEmail())).thenReturn(Optional.of(adminUser));
        when(serviceRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> incidentService.createIncident(request, adminUser.getEmail()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Service not found");
    }

    // ── updateStatus ────────────────────────────────────────────────────

    @Test
    void updateStatus_openToInvestigating_success() {
        Incident existing = buildIncident(UUID.randomUUID(), "Test",
                IncidentSeverity.P2, IncidentStatus.OPEN);
        Incident updated = buildIncident(existing.getId(), "Test",
                IncidentSeverity.P2, IncidentStatus.INVESTIGATING);

        when(incidentRepository.findByIdWithDetails(existing.getId()))
                .thenReturn(Optional.of(existing));
        when(userRepository.findByEmail(adminUser.getEmail())).thenReturn(Optional.of(adminUser));
        when(incidentUpdateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(incidentRepository.save(any())).thenReturn(updated);

        IncidentResponse response = incidentService.updateStatus(
                existing.getId(), new StatusUpdateRequest(IncidentStatus.INVESTIGATING),
                adminUser.getEmail());

        assertThat(response.status()).isEqualTo(IncidentStatus.INVESTIGATING);
    }

    @Test
    void updateStatus_resolvedSetsResolvedAt() {
        Incident existing = buildIncident(UUID.randomUUID(), "Test",
                IncidentSeverity.P3, IncidentStatus.MITIGATED);
        existing.setResolvedAt(null);

        when(incidentRepository.findByIdWithDetails(existing.getId()))
                .thenReturn(Optional.of(existing));
        when(userRepository.findByEmail(adminUser.getEmail())).thenReturn(Optional.of(adminUser));
        when(incidentUpdateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(incidentRepository.save(any())).thenAnswer(inv -> {
            Incident i = inv.getArgument(0);
            assertThat(i.getResolvedAt()).isNotNull();
            return i;
        });

        incidentService.updateStatus(existing.getId(),
                new StatusUpdateRequest(IncidentStatus.RESOLVED), adminUser.getEmail());
    }

    @Test
    void updateStatus_fromClosed_throwsInvalidTransition() {
        Incident closed = buildIncident(UUID.randomUUID(), "Closed",
                IncidentSeverity.P4, IncidentStatus.CLOSED);

        when(incidentRepository.findByIdWithDetails(closed.getId()))
                .thenReturn(Optional.of(closed));

        assertThatThrownBy(() -> incidentService.updateStatus(
                closed.getId(), new StatusUpdateRequest(IncidentStatus.OPEN), adminUser.getEmail()))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("CLOSED");
    }

    @Test
    void updateStatus_resolvedToOpen_throwsInvalidTransition() {
        Incident resolved = buildIncident(UUID.randomUUID(), "Resolved",
                IncidentSeverity.P2, IncidentStatus.RESOLVED);

        when(incidentRepository.findByIdWithDetails(resolved.getId()))
                .thenReturn(Optional.of(resolved));

        assertThatThrownBy(() -> incidentService.updateStatus(
                resolved.getId(), new StatusUpdateRequest(IncidentStatus.OPEN), adminUser.getEmail()))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void updateStatus_incidentNotFound_throwsResourceNotFound() {
        UUID id = UUID.randomUUID();
        when(incidentRepository.findByIdWithDetails(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> incidentService.updateStatus(
                id, new StatusUpdateRequest(IncidentStatus.INVESTIGATING), adminUser.getEmail()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Incident not found");
    }

    // ── getIncident ─────────────────────────────────────────────────────

    @Test
    void getIncident_withUpdates_returnsDetailResponse() {
        UUID id = UUID.randomUUID();
        Incident incident = buildIncident(id, "Test", IncidentSeverity.P1, IncidentStatus.OPEN);

        IncidentUpdate update = new IncidentUpdate();
        update.setId(UUID.randomUUID());
        update.setIncident(incident);
        update.setAuthor(adminUser);
        update.setUpdateType(UpdateType.COMMENT);
        update.setMessage("Investigating now");

        when(incidentRepository.findByIdWithDetails(id)).thenReturn(Optional.of(incident));
        when(incidentUpdateRepository.findByIncidentIdOrderByCreatedAtAsc(id))
                .thenReturn(List.of(update));

        IncidentResponse response = incidentService.getIncident(id);

        assertThat(response.updates()).hasSize(1);
        assertThat(response.updates().get(0).message()).isEqualTo("Investigating now");
    }

    @Test
    void getIncident_notFound_throwsResourceNotFound() {
        UUID id = UUID.randomUUID();
        when(incidentRepository.findByIdWithDetails(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> incidentService.getIncident(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Incident not found");
    }

    // ── addUpdate ────────────────────────────────────────────────────────

    @Test
    void addUpdate_success_returnsUpdateResponse() {
        Incident incident = buildIncident(UUID.randomUUID(), "Test",
                IncidentSeverity.P2, IncidentStatus.INVESTIGATING);
        IncidentUpdateRequest request =
                new IncidentUpdateRequest(UpdateType.COMMENT, "Rolling back deployment");

        IncidentUpdate saved = new IncidentUpdate();
        saved.setId(UUID.randomUUID());
        saved.setIncident(incident);
        saved.setAuthor(adminUser);
        saved.setUpdateType(UpdateType.COMMENT);
        saved.setMessage(request.message());

        when(incidentRepository.findById(incident.getId())).thenReturn(Optional.of(incident));
        when(userRepository.findByEmail(adminUser.getEmail())).thenReturn(Optional.of(adminUser));
        when(incidentUpdateRepository.save(any())).thenReturn(saved);

        IncidentUpdateResponse response =
                incidentService.addUpdate(incident.getId(), request, adminUser.getEmail());

        assertThat(response.message()).isEqualTo("Rolling back deployment");
        assertThat(response.updateType()).isEqualTo(UpdateType.COMMENT);
    }

    // ── helpers ─────────────────────────────────────────────────────────

    private Incident buildIncident(UUID id, String title,
                                   IncidentSeverity severity, IncidentStatus status) {
        Incident i = new Incident();
        i.setId(id);
        i.setTitle(title);
        i.setSeverity(severity);
        i.setStatus(status);
        i.setService(testService);
        i.setCreatedBy(adminUser);
        i.setStartedAt(OffsetDateTime.now());
        return i;
    }
}
