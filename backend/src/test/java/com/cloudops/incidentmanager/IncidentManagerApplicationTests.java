package com.cloudops.incidentmanager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

// Docker なしで実行可能な H2 インメモリ構成。
// Testcontainers を使った統合テストは @ActiveProfiles("test") で別途実行する（Docker 必要）。
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class IncidentManagerApplicationTests {

    @Test
    void contextLoads() {
    }
}
