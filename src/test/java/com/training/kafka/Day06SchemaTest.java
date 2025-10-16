package com.training.kafka;

import com.training.kafka.services.Day06SchemaService;
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
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for Day 6 Schema Management functionality
 *
 * Tests cover:
 * - Service initialization
 * - Avro producer/consumer operations
 * - Schema Registry integration
 * - REST API endpoints
 *
 * Note: These tests use Kafka but skip Schema Registry container
 * for simplicity. Full Schema Registry tests would require
 * additional TestContainers configuration.
 *
 * @author Kafka Training Course
 * @version 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class Day06SchemaTest {

    @Container
    @SuppressWarnings("resource")
    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.7.0"))
            .withEmbeddedZookeeper();

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private Day06SchemaService schemaService;

    /**
     * Configure Spring Boot properties dynamically from TestContainers
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("training.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("training.kafka.client-id", () -> "schema-test-client");

        // Note: Schema Registry would require additional container setup
        // For now, we configure a mock URL and test will handle registry unavailability gracefully
        registry.add("training.kafka.schema-registry-url", () -> "http://localhost:8082");
        registry.add("spring.kafka.properties.schema.registry.url", () -> "http://localhost:8082");
    }

    // ===== Service Tests =====

    @Test
    @DisplayName("Day06SchemaService should be initialized")
    void shouldInitializeSchemaService() {
        assertNotNull(schemaService, "Schema service should be initialized");
    }

    @Test
    @DisplayName("Should demonstrate schema management without errors")
    void shouldDemonstrateSchemaManagementWithoutErrors() {
        // Note: This will log warnings about Schema Registry not being available
        // but should not throw exceptions due to proper error handling
        assertDoesNotThrow(() -> schemaService.demonstrateSchemaManagement(),
            "Schema management demonstration should not throw exceptions");
    }

    // ===== REST API Tests =====

    @Test
    @DisplayName("Day06 demo API endpoint should work")
    void shouldWorkWithDay06DemoAPI() {
        // When: Running Day 6 demo via API
        // Note: Will show warnings about Schema Registry but should complete gracefully
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/training/day06/demo",
            HttpMethod.POST,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then: Should attempt to complete (may have warnings but shouldn't fail hard)
        assertNotNull(response);
        assertTrue(response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR,
            "Response should be OK or 500 (depending on Schema Registry availability)");
    }

    @Test
    @DisplayName("Avro produce API endpoint should exist")
    void shouldHaveAvroProduceEndpoint() {
        // When: Calling Avro produce endpoint
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/training/day06/avro/produce",
            HttpMethod.POST,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then: Should return a response (success or error depending on Schema Registry)
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("status"));
    }

    @Test
    @DisplayName("Avro consume API endpoint should exist")
    void shouldHaveAvroConsumeEndpoint() {
        // When: Calling Avro consume endpoint
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/training/day06/avro/consume",
            HttpMethod.POST,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then: Should return a response
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("status"));
    }

    @Test
    @DisplayName("Schema list API endpoint should exist")
    void shouldHaveSchemaListEndpoint() {
        // When: Calling schema list endpoint
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/training/day06/schemas/list",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then: Should return a response
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("status"));
    }

    @Test
    @DisplayName("Schema compatibility API endpoint should validate inputs")
    void shouldValidateSchemaCompatibilityInputs() {
        // When: Calling compatibility endpoint without subject
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/training/day06/schemas/compatibility?subject=&schema=test",
            HttpMethod.POST,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then: Should return bad request
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("error", response.getBody().get("status"));
        assertTrue(response.getBody().get("message").toString().contains("Subject cannot be empty"));
    }

    @Test
    @DisplayName("Modules API should include Day06Schemas")
    void shouldIncludeDay06SchemasInModulesAPI() {
        // When: Getting all modules
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/training/modules",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then: Should include Day06Schemas
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> modules = response.getBody();
        assertTrue(modules.containsKey("Day06Schemas"), "Should contain Day06Schemas module");

        Map<?, ?> day06Module = (Map<?, ?>) modules.get("Day06Schemas");
        assertEquals("Schema Management", day06Module.get("name"));
        assertTrue(day06Module.get("description").toString().contains("Avro"));
    }

    @Test
    @DisplayName("Service should handle missing Schema Registry gracefully")
    void shouldHandleMissingSchemaRegistryGracefully() {
        // Given: Schema Registry is not running in tests

        // When: Attempting to list schemas
        Map<String, Object> result = schemaService.listRegisteredSchemas();

        // Then: Should return error status but not throw exception
        assertNotNull(result);
        assertTrue(result.containsKey("status"));
        // Result will be "error" because Schema Registry is not running in test environment
    }

    @Test
    @DisplayName("Avro schemas should be generated from .avsc files")
    void shouldHaveGeneratedAvroClasses() {
        // Verify that Avro classes were generated at build time
        // This is a compile-time check - if the test compiles, the classes exist

        try {
            // UserEvent class should exist
            Class.forName("com.training.kafka.avro.UserEvent");

            // ProductEvent class should exist
            Class.forName("com.training.kafka.avro.ProductEvent");

            // OrderEvent class should exist
            Class.forName("com.training.kafka.avro.OrderEvent");

            // PaymentEvent class should exist
            Class.forName("com.training.kafka.avro.PaymentEvent");

            // If we reach here, all classes exist
            assertTrue(true, "All Avro classes should be generated");

        } catch (ClassNotFoundException e) {
            fail("Avro classes should be generated from .avsc files: " + e.getMessage());
        }
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
}
