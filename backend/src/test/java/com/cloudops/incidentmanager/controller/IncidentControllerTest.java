package com.cloudops.incidentmanager.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.cloudops.incidentmanager.model.IncidentSeverity;
import com.cloudops.incidentmanager.model.IncidentStatus;
import com.cloudops.incidentmanager.model.ServiceStatus;
import com.cloudops.incidentmanager.model.ServiceTier;
import com.cloudops.incidentmanager.model.User;
import com.cloudops.incidentmanager.model.UserRole;
import com.cloudops.incidentmanager.repository.IncidentRepository;
import com.cloudops.incidentmanager.repository.ServiceRepository;
import com.cloudops.incidentmanager.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:incidenttest;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "app.jwt.secret=test-secret-key-for-incident-controller-test-min-32chars",
    "app.jwt.expiration-ms=3600000",
    "app.jwt.refresh-expiration-ms=604800000"
})
class IncidentControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired IncidentRepository incidentRepository;
    @Autowired UserRepository userRepository;
    @Autowired ServiceRepository serviceRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private static final String ADMIN_EMAIL = "admin@incidenttest.com";
    private static final String VIEWER_EMAIL = "viewer@incidenttest.com";

    private com.cloudops.incidentmanager.model.Service testService;

    @BeforeEach
    void setUp() {
        incidentRepository.deleteAll();
        serviceRepository.deleteAll();
        userRepository.deleteAll();

        User admin = new User();
        admin.setEmail(ADMIN_EMAIL);
        admin.setPasswordHash(passwordEncoder.encode("Password1!"));
        admin.setDisplayName("Admin User");
        admin.setRole(UserRole.ADMIN);
        userRepository.save(admin);

        User viewer = new User();
        viewer.setEmail(VIEWER_EMAIL);
        viewer.setPasswordHash(passwordEncoder.encode("Password1!"));
        viewer.setDisplayName("Viewer User");
        viewer.setRole(UserRole.VIEWER);
        userRepository.save(viewer);

        testService = new com.cloudops.incidentmanager.model.Service();
        testService.setName("Test Service");
        testService.setTier(ServiceTier.TIER1);
        testService.setStatus(ServiceStatus.OPERATIONAL);
        testService = serviceRepository.save(testService);
    }

    // ── GET /incidents ──────────────────────────────────────────────────

    @Test
    @WithMockUser(username = ADMIN_EMAIL, roles = "ADMIN")
    void getIncidents_returnsEmptyPage() throws Exception {
        mockMvc.perform(get("/api/v1/incidents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @WithMockUser(username = VIEWER_EMAIL, roles = "VIEWER")
    void getIncidents_asViewer_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/incidents"))
                .andExpect(status().isOk());
    }

    @Test
    void getIncidents_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/incidents"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = ADMIN_EMAIL, roles = "ADMIN")
    void getIncidents_withStatusFilter_returnsFiltered() throws Exception {
        createIncidentViaApi(ADMIN_EMAIL);

        mockMvc.perform(get("/api/v1/incidents").param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get("/api/v1/incidents").param("status", "RESOLVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // ── GET /incidents/{id} ─────────────────────────────────────────────

    @Test
    @WithMockUser(username = ADMIN_EMAIL, roles = "ADMIN")
    void getIncident_whenExists_returns200WithUpdates() throws Exception {
        String json = createIncidentViaApi(ADMIN_EMAIL);
        String id = extractId(json);

        mockMvc.perform(get("/api/v1/incidents/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("DB is down"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.severity").value("P1"))
                .andExpect(jsonPath("$.updates").isArray());
    }

    @Test
    @WithMockUser(username = VIEWER_EMAIL, roles = "VIEWER")
    void getIncident_whenNotFound_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/incidents/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    // ── POST /incidents ─────────────────────────────────────────────────

    @Test
    @WithMockUser(username = ADMIN_EMAIL, roles = "ADMIN")
    void createIncident_asAdmin_returns201() throws Exception {
        mockMvc.perform(post("/api/v1/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(incidentJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("DB is down"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.severity").value("P1"))
                .andExpect(jsonPath("$.service.name").value("Test Service"))
                .andExpect(jsonPath("$.updates").isArray());
    }

    @Test
    @WithMockUser(username = ADMIN_EMAIL, roles = "OPERATOR")
    void createIncident_asOperator_returns201() throws Exception {
        mockMvc.perform(post("/api/v1/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(incidentJson()))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = VIEWER_EMAIL, roles = "VIEWER")
    void createIncident_asViewer_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(incidentJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    void createIncident_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(incidentJson()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = ADMIN_EMAIL, roles = "ADMIN")
    void createIncident_missingTitle_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                  "serviceId": "%s",
                                  "severity": "P1",
                                  "startedAt": "%s"
                                }
                                """, testService.getId(), OffsetDateTime.now())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = ADMIN_EMAIL, roles = "ADMIN")
    void createIncident_missingSeverity_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                  "title": "Test",
                                  "serviceId": "%s",
                                  "startedAt": "%s"
                                }
                                """, testService.getId(), OffsetDateTime.now())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = ADMIN_EMAIL, roles = "ADMIN")
    void createIncident_serviceNotFound_returns404() throws Exception {
        mockMvc.perform(post("/api/v1/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                  "title": "Unknown service",
                                  "serviceId": "%s",
                                  "severity": "P2",
                                  "startedAt": "%s"
                                }
                                """, UUID.randomUUID(), OffsetDateTime.now())))
                .andExpect(status().isNotFound());
    }

    // ── PATCH /incidents/{id}/status ────────────────────────────────────

    @Test
    @WithMockUser(username = ADMIN_EMAIL, roles = "ADMIN")
    void updateStatus_openToInvestigating_returns200() throws Exception {
        String id = extractId(createIncidentViaApi(ADMIN_EMAIL));

        mockMvc.perform(patch("/api/v1/incidents/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "INVESTIGATING" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INVESTIGATING"));
    }

    @Test
    @WithMockUser(username = VIEWER_EMAIL, roles = "VIEWER")
    void updateStatus_asViewer_returns403() throws Exception {
        mockMvc.perform(patch("/api/v1/incidents/" + UUID.randomUUID() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "INVESTIGATING" }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = ADMIN_EMAIL, roles = "ADMIN")
    void updateStatus_whenNotFound_returns404() throws Exception {
        mockMvc.perform(patch("/api/v1/incidents/" + UUID.randomUUID() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "INVESTIGATING" }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = ADMIN_EMAIL, roles = "ADMIN")
    void updateStatus_missingStatus_returns400() throws Exception {
        String id = extractId(createIncidentViaApi(ADMIN_EMAIL));

        mockMvc.perform(patch("/api/v1/incidents/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = ADMIN_EMAIL, roles = "ADMIN")
    void updateStatus_closedToOpen_returns400() throws Exception {
        String id = extractId(createIncidentViaApi(ADMIN_EMAIL));

        patchStatus(id, IncidentStatus.RESOLVED);
        patchStatus(id, IncidentStatus.CLOSED);

        mockMvc.perform(patch("/api/v1/incidents/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "OPEN" }
                                """))
                .andExpect(status().isBadRequest());
    }

    // ── POST /incidents/{id}/updates ────────────────────────────────────

    @Test
    @WithMockUser(username = ADMIN_EMAIL, roles = "ADMIN")
    void addUpdate_asAdmin_returns201() throws Exception {
        String id = extractId(createIncidentViaApi(ADMIN_EMAIL));

        mockMvc.perform(post("/api/v1/incidents/" + id + "/updates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "updateType": "COMMENT",
                                  "message": "Rolling back the deployment now"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.updateType").value("COMMENT"))
                .andExpect(jsonPath("$.message").value("Rolling back the deployment now"))
                .andExpect(jsonPath("$.author.email").value(ADMIN_EMAIL));
    }

    @Test
    @WithMockUser(username = VIEWER_EMAIL, roles = "VIEWER")
    void addUpdate_asViewer_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/incidents/" + UUID.randomUUID() + "/updates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "updateType": "COMMENT",
                                  "message": "should not work"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = ADMIN_EMAIL, roles = "ADMIN")
    void addUpdate_incidentNotFound_returns404() throws Exception {
        mockMvc.perform(post("/api/v1/incidents/" + UUID.randomUUID() + "/updates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "updateType": "COMMENT",
                                  "message": "missing incident"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = ADMIN_EMAIL, roles = "ADMIN")
    void addUpdate_missingMessage_returns400() throws Exception {
        String id = extractId(createIncidentViaApi(ADMIN_EMAIL));

        mockMvc.perform(post("/api/v1/incidents/" + id + "/updates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "updateType": "COMMENT" }
                                """))
                .andExpect(status().isBadRequest());
    }

    // ── helpers ─────────────────────────────────────────────────────────

    private String incidentJson() {
        return String.format("""
                {
                  "title": "DB is down",
                  "description": "Cannot reach primary database",
                  "serviceId": "%s",
                  "severity": "P1",
                  "startedAt": "%s"
                }
                """, testService.getId(), OffsetDateTime.now());
    }

    private String createIncidentViaApi(String username) throws Exception {
        return mockMvc.perform(post("/api/v1/incidents")
                        .with(org.springframework.security.test.web.servlet.request
                                .SecurityMockMvcRequestPostProcessors.user(username).roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(incidentJson()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }

    private void patchStatus(String id, IncidentStatus status) throws Exception {
        mockMvc.perform(patch("/api/v1/incidents/" + id + "/status")
                        .with(org.springframework.security.test.web.servlet.request
                                .SecurityMockMvcRequestPostProcessors.user(ADMIN_EMAIL).roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{ \"status\": \"%s\" }", status.name())))
                .andExpect(status().isOk());
    }

    private String extractId(String json) {
        return json.split("\"id\":\"")[1].split("\"")[0];
    }
}
