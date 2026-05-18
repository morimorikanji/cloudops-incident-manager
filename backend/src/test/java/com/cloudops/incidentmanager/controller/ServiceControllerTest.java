package com.cloudops.incidentmanager.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.cloudops.incidentmanager.model.ServiceStatus;
import com.cloudops.incidentmanager.model.ServiceTier;
import com.cloudops.incidentmanager.repository.ServiceRepository;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:servicetest;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "app.jwt.secret=test-secret-key-for-service-controller-test-min-32chars",
    "app.jwt.expiration-ms=3600000",
    "app.jwt.refresh-expiration-ms=604800000"
})
class ServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ServiceRepository serviceRepository;

    @BeforeEach
    void setUp() {
        serviceRepository.deleteAll();
    }

    private com.cloudops.incidentmanager.model.Service saveService(String name, ServiceTier tier) {
        com.cloudops.incidentmanager.model.Service s =
                new com.cloudops.incidentmanager.model.Service();
        s.setName(name);
        s.setTier(tier);
        s.setStatus(ServiceStatus.OPERATIONAL);
        return serviceRepository.save(s);
    }

    // ── GET /services ──────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "VIEWER")
    void getServices_returnsEmptyPage() throws Exception {
        mockMvc.perform(get("/api/v1/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void getServices_withStatusFilter_returnsFiltered() throws Exception {
        saveService("Service A", ServiceTier.TIER1);
        saveService("Service B", ServiceTier.TIER2);

        mockMvc.perform(get("/api/v1/services").param("status", "OPERATIONAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getServices_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/services"))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /services/{id} ─────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "VIEWER")
    void getService_whenNotFound_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/services/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getService_whenExists_returns200() throws Exception {
        String id = extractIdFromCreate(createServiceViaApi("My Service", "TIER1"));

        mockMvc.perform(get("/api/v1/services/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("My Service"))
                .andExpect(jsonPath("$.tier").value("TIER1"))
                .andExpect(jsonPath("$.status").value("OPERATIONAL"))
                .andExpect(jsonPath("$.openIncidentCount").value(0));
    }

    // ── POST /services ─────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void createService_asAdmin_returns201() throws Exception {
        mockMvc.perform(post("/api/v1/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Payment Service",
                                  "description": "決済処理",
                                  "tier": "TIER1",
                                  "endpointUrl": "https://payment.internal",
                                  "repositoryUrl": "https://github.com/org/payment"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Payment Service"))
                .andExpect(jsonPath("$.tier").value("TIER1"))
                .andExpect(jsonPath("$.status").value("OPERATIONAL"))
                .andExpect(jsonPath("$.openIncidentCount").value(0));
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void createService_asOperator_returns201() throws Exception {
        mockMvc.perform(post("/api/v1/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Inventory Service",
                                  "tier": "TIER2"
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void createService_asViewer_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Forbidden Service",
                                  "tier": "TIER1"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void createService_missingName_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tier": "TIER1"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void createService_missingTier_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "No Tier Service"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createService_duplicateName_returns409() throws Exception {
        createServiceViaApi("Duplicate Service", "TIER1");

        mockMvc.perform(post("/api/v1/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Duplicate Service",
                                  "tier": "TIER2"
                                }
                                """))
                .andExpect(status().isConflict());
    }

    // ── PATCH /services/{id} ───────────────────────────────────────────

    @Test
    @WithMockUser(roles = "OPERATOR")
    void updateService_asOperator_changesStatus() throws Exception {
        String id = extractIdFromCreate(createServiceViaApi("Update Target", "TIER1"));

        mockMvc.perform(patch("/api/v1/services/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "DEGRADED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DEGRADED"))
                .andExpect(jsonPath("$.name").value("Update Target"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateService_changeTierAndEndpoint_returns200() throws Exception {
        String id = extractIdFromCreate(createServiceViaApi("Tier Updater", "TIER3"));

        mockMvc.perform(patch("/api/v1/services/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tier": "TIER1",
                                  "endpointUrl": "https://updated.internal"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tier").value("TIER1"))
                .andExpect(jsonPath("$.endpointUrl").value("https://updated.internal"));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void updateService_asViewer_returns403() throws Exception {
        mockMvc.perform(patch("/api/v1/services/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "DOWN"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateService_whenNotFound_returns404() throws Exception {
        mockMvc.perform(patch("/api/v1/services/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "DOWN"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    // ── helpers ────────────────────────────────────────────────────────

    private String createServiceViaApi(String name, String tier) throws Exception {
        return mockMvc.perform(post("/api/v1/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                  "name": "%s",
                                  "tier": "%s"
                                }
                                """, name, tier)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }

    private String extractIdFromCreate(String jsonResponse) {
        return jsonResponse.split("\"id\":\"")[1].split("\"")[0];
    }
}
