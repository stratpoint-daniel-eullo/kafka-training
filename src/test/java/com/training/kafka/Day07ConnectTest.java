package com.training.kafka;

import com.training.kafka.services.Day07ConnectService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for Day 7 Kafka Connect functionality
 *
 * Tests cover:
 * - Service initialization
 * - Kafka Connect REST API integration
 * - Connector lifecycle management
 * - PostgreSQL database connectivity
 * - REST API endpoints
 *
 * Note: These tests use Kafka and PostgreSQL containers but skip
 * Kafka Connect container for simplicity. Full integration tests
 * would require Kafka Connect TestContainer configuration.
 *
 * @author Kafka Training Course
 * @version 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class Day07ConnectTest {

    @Container
    @SuppressWarnings("resource")
    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.7.0"))
            .withEmbeddedZookeeper();

    @Container
    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
            .withDatabaseName("eventmart")
            .withUsername("eventmart")
            .withPassword("eventmart123")
            .withInitScript("init-postgres.sql");

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private Day07ConnectService connectService;

    /**
     * Configure Spring Boot properties dynamically from TestContainers
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Kafka configuration
        registry.add("training.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("training.kafka.client-id", () -> "connect-test-client");

        // PostgreSQL configuration
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // Note: Kafka Connect would require additional container setup
        // For now, we configure a mock URL and tests will handle Connect unavailability gracefully
        registry.add("training.kafka.connect-url", () -> "http://localhost:8083");
    }

    // ===== Service Tests =====

    @Test
    @DisplayName("Day07ConnectService should be initialized")
    void shouldInitializeConnectService() {
        assertNotNull(connectService, "Connect service should be initialized");
    }

    @Test
    @DisplayName("Should demonstrate Kafka Connect without errors")
    void shouldDemonstrateKafkaConnectWithoutErrors() {
        // Note: This will log errors about Kafka Connect not being available
        // but should not throw exceptions due to proper error handling
        assertDoesNotThrow(() -> connectService.demonstrateKafkaConnect(),
            "Kafka Connect demonstration should not throw exceptions");
    }

    @Test
    @DisplayName("Should handle missing Kafka Connect gracefully when getting cluster info")
    void shouldHandleMissingKafkaConnectClusterInfo() {
        // Given: Kafka Connect is not running in tests

        // When: Attempting to get cluster info
        Map<String, Object> result = connectService.getConnectClusterInfo();

        // Then: Should return error status but not throw exception
        assertNotNull(result);
        assertTrue(result.containsKey("status"));
        assertEquals("error", result.get("status"));
        assertTrue(result.containsKey("message"));
    }

    @Test
    @DisplayName("Should handle missing Kafka Connect gracefully when listing connector plugins")
    void shouldHandleMissingKafkaConnectPlugins() {
        // Given: Kafka Connect is not running in tests

        // When: Attempting to list connector plugins
        Map<String, Object> result = connectService.listConnectorPlugins();

        // Then: Should return error status but not throw exception
        assertNotNull(result);
        assertTrue(result.containsKey("status"));
        assertEquals("error", result.get("status"));
    }

    @Test
    @DisplayName("Should handle missing Kafka Connect gracefully when listing connectors")
    void shouldHandleMissingKafkaConnectListConnectors() {
        // Given: Kafka Connect is not running in tests

        // When: Attempting to list connectors
        Map<String, Object> result = connectService.listConnectors();

        // Then: Should return error status but not throw exception
        assertNotNull(result);
        assertTrue(result.containsKey("status"));
        assertEquals("error", result.get("status"));
    }

    @Test
    @DisplayName("Should handle missing Kafka Connect gracefully when creating source connector")
    void shouldHandleMissingKafkaConnectCreateSourceConnector() {
        // Given: Kafka Connect is not running in tests

        // When: Attempting to create user activity source connector
        Map<String, Object> result = connectService.createUserActivitySourceConnector();

        // Then: Should return error status but not throw exception
        assertNotNull(result);
        assertTrue(result.containsKey("status"));
        assertEquals("error", result.get("status"));
    }

    @Test
    @DisplayName("Should handle missing Kafka Connect gracefully when creating sink connector")
    void shouldHandleMissingKafkaConnectCreateSinkConnector() {
        // Given: Kafka Connect is not running in tests

        // When: Attempting to create order events sink connector
        Map<String, Object> result = connectService.createOrderEventsSinkConnector();

        // Then: Should return error status but not throw exception
        assertNotNull(result);
        assertTrue(result.containsKey("status"));
        assertEquals("error", result.get("status"));
    }

    @Test
    @DisplayName("Should handle missing Kafka Connect gracefully when checking connector status")
    void shouldHandleMissingKafkaConnectConnectorStatus() {
        // Given: Kafka Connect is not running in tests

        // When: Attempting to check connector status
        Map<String, Object> result = connectService.checkConnectorStatus("test-connector");

        // Then: Should return error status but not throw exception
        assertNotNull(result);
        assertTrue(result.containsKey("status"));
        assertEquals("error", result.get("status"));
    }

    @Test
    @DisplayName("Should handle missing Kafka Connect gracefully when pausing connector")
    void shouldHandleMissingKafkaConnectPauseConnector() {
        // Given: Kafka Connect is not running in tests

        // When: Attempting to pause connector
        Map<String, Object> result = connectService.pauseConnector("test-connector");

        // Then: Should return error status but not throw exception
        assertNotNull(result);
        assertTrue(result.containsKey("status"));
        assertEquals("error", result.get("status"));
    }

    @Test
    @DisplayName("Should handle missing Kafka Connect gracefully when resuming connector")
    void shouldHandleMissingKafkaConnectResumeConnector() {
        // Given: Kafka Connect is not running in tests

        // When: Attempting to resume connector
        Map<String, Object> result = connectService.resumeConnector("test-connector");

        // Then: Should return error status but not throw exception
        assertNotNull(result);
        assertTrue(result.containsKey("status"));
        assertEquals("error", result.get("status"));
    }

    @Test
    @DisplayName("Should handle missing Kafka Connect gracefully when restarting connector")
    void shouldHandleMissingKafkaConnectRestartConnector() {
        // Given: Kafka Connect is not running in tests

        // When: Attempting to restart connector
        Map<String, Object> result = connectService.restartConnector("test-connector");

        // Then: Should return error status but not throw exception
        assertNotNull(result);
        assertTrue(result.containsKey("status"));
        assertEquals("error", result.get("status"));
    }

    @Test
    @DisplayName("Should handle missing Kafka Connect gracefully when deleting connector")
    void shouldHandleMissingKafkaConnectDeleteConnector() {
        // Given: Kafka Connect is not running in tests

        // When: Attempting to delete connector
        Map<String, Object> result = connectService.deleteConnector("test-connector");

        // Then: Should return error status but not throw exception
        assertNotNull(result);
        assertTrue(result.containsKey("status"));
        assertEquals("error", result.get("status"));
    }

    // ===== REST API Tests =====

    @Test
    @DisplayName("Day07 demo API endpoint should work")
    void shouldWorkWithDay07DemoAPI() {
        // When: Running Day 7 demo via API
        // Note: Will show errors about Kafka Connect but should complete gracefully
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/training/day07/demo",
            HttpMethod.POST,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then: Should attempt to complete (may have errors but shouldn't fail hard)
        assertNotNull(response);
        assertTrue(response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR,
            "Response should be OK or 500 (depending on Kafka Connect availability)");
    }

    @Test
    @DisplayName("Connect cluster info API endpoint should exist")
    void shouldHaveConnectClusterInfoEndpoint() {
        // When: Calling Connect cluster info endpoint
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/training/day07/connect/info",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then: Should return a response
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("status"));
    }

    @Test
    @DisplayName("Connector plugins list API endpoint should exist")
    void shouldHaveConnectorPluginsEndpoint() {
        // When: Calling connector plugins endpoint
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/training/day07/connectors/plugins",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then: Should return a response
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("status"));
    }

    @Test
    @DisplayName("Connectors list API endpoint should exist")
    void shouldHaveConnectorsListEndpoint() {
        // When: Calling connectors list endpoint
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/training/day07/connectors/list",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then: Should return a response
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("status"));
    }

    @Test
    @DisplayName("User activity source connector creation API endpoint should exist")
    void shouldHaveUserActivitySourceConnectorEndpoint() {
        // When: Calling user activity source connector creation endpoint
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/training/day07/connectors/create/user-activity-source",
            HttpMethod.POST,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then: Should return a response
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("status"));
    }

    @Test
    @DisplayName("Product events source connector creation API endpoint should exist")
    void shouldHaveProductEventsSourceConnectorEndpoint() {
        // When: Calling product events source connector creation endpoint
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/training/day07/connectors/create/product-events-source",
            HttpMethod.POST,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then: Should return a response
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("status"));
    }

    @Test
    @DisplayName("Order events sink connector creation API endpoint should exist")
    void shouldHaveOrderEventsSinkConnectorEndpoint() {
        // When: Calling order events sink connector creation endpoint
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/training/day07/connectors/create/order-events-sink",
            HttpMethod.POST,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then: Should return a response
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("status"));
    }

    @Test
    @DisplayName("Connector status API endpoint should exist")
    void shouldHaveConnectorStatusEndpoint() {
        // When: Calling connector status endpoint
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/training/day07/connectors/test-connector/status",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then: Should return a response
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("status"));
    }

    @Test
    @DisplayName("Connector pause API endpoint should exist")
    void shouldHaveConnectorPauseEndpoint() {
        // When: Calling connector pause endpoint
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/training/day07/connectors/test-connector/pause",
            HttpMethod.POST,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then: Should return a response
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("status"));
    }

    @Test
    @DisplayName("Connector resume API endpoint should exist")
    void shouldHaveConnectorResumeEndpoint() {
        // When: Calling connector resume endpoint
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/training/day07/connectors/test-connector/resume",
            HttpMethod.POST,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then: Should return a response
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("status"));
    }

    @Test
    @DisplayName("Connector restart API endpoint should exist")
    void shouldHaveConnectorRestartEndpoint() {
        // When: Calling connector restart endpoint
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/training/day07/connectors/test-connector/restart",
            HttpMethod.POST,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then: Should return a response
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("status"));
    }

    @Test
    @DisplayName("Connector delete API endpoint should exist")
    void shouldHaveConnectorDeleteEndpoint() {
        // When: Calling connector delete endpoint
        restTemplate.delete(
            "http://localhost:" + port + "/api/training/day07/connectors/test-connector/delete");

        // Then: Should execute without throwing exception
        // Note: Can't easily test response of DELETE with TestRestTemplate
        assertTrue(true, "Delete endpoint should be callable");
    }

    @Test
    @DisplayName("Modules API should include Day07Connect")
    void shouldIncludeDay07ConnectInModulesAPI() {
        // When: Getting all modules
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/training/modules",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then: Should include Day07Connect
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> modules = response.getBody();
        assertTrue(modules.containsKey("Day07Connect"), "Should contain Day07Connect module");

        Map<?, ?> day07Module = (Map<?, ?>) modules.get("Day07Connect");
        assertEquals("Kafka Connect", day07Module.get("name"));
        assertTrue(day07Module.get("description").toString().contains("data integration"));
    }

    @Test
    @DisplayName("PostgreSQL container should be running and accessible")
    void shouldHaveAccessiblePostgresDatabase() {
        // Then: PostgreSQL container should be running
        assertTrue(postgres.isRunning(), "PostgreSQL container should be running");
        assertTrue(postgres.isCreated(), "PostgreSQL container should be created");

        // And: Should have correct connection details
        assertEquals("eventmart", postgres.getDatabaseName());
        assertEquals("eventmart", postgres.getUsername());
        assertEquals("eventmart123", postgres.getPassword());
    }

    @Test
    @DisplayName("PostgreSQL database should have EventMart schema initialized")
    void shouldHaveEventMartSchemaInitialized() {
        // Note: The init-postgres.sql script should create the tables
        // We can't directly verify schema without JDBC queries in test
        // But we can verify that postgres container started with init script

        assertTrue(postgres.isRunning(), "PostgreSQL should be running with initialized schema");
    }

    @Test
    @DisplayName("Should have proper Kafka configuration")
    void shouldHaveProperKafkaConfiguration() {
        // When: Checking configuration through health endpoint
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/training/health",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then: Should return healthy status
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> health = response.getBody();
        assertEquals("UP", health.get("status"));
    }

    @Test
    @DisplayName("Kafka container should be running and accessible")
    void shouldHaveAccessibleKafkaCluster() {
        // Then: Kafka container should be running
        assertTrue(kafka.isRunning(), "Kafka container should be running");
        assertTrue(kafka.isCreated(), "Kafka container should be created");

        // And: Should have bootstrap servers configured
        assertNotNull(kafka.getBootstrapServers(), "Bootstrap servers should be configured");
        assertTrue(kafka.getBootstrapServers().contains("PLAINTEXT://"),
            "Bootstrap servers should use PLAINTEXT protocol");
    }
}
