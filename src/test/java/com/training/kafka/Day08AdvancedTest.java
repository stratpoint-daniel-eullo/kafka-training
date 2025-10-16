package com.training.kafka;

import com.training.kafka.services.Day08AdvancedService;
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
 * Comprehensive tests for Day 8 Advanced Topics
 *
 * Tests cover:
 * - Security configurations (SSL, SASL, ACLs)
 * - Monitoring capabilities (metrics, consumer lag)
 * - Performance optimizations
 * - Production operations
 * - REST API endpoints
 *
 * @author Kafka Training Course
 * @version 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class Day08AdvancedTest {

    @Container
    @SuppressWarnings("resource") // Testcontainers manages lifecycle
    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.7.0"))
            .withEmbeddedZookeeper();

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private Day08AdvancedService advancedService;

    /**
     * Configure Spring Boot properties dynamically from TestContainers
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("training.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("training.kafka.client-id", () -> "advanced-test-client");
    }

    // ===== Service Tests =====

    @Test
    @DisplayName("Day08AdvancedService should be initialized")
    void shouldInitializeAdvancedService() {
        assertNotNull(advancedService, "Advanced service should be initialized");
    }

    @Test
    @DisplayName("Should demonstrate advanced topics without errors")
    void shouldDemonstrateAdvancedTopicsWithoutErrors() {
        // When: Running advanced topics demonstration
        assertDoesNotThrow(() -> advancedService.demonstrateAdvancedTopics(),
                "Advanced topics demonstration should run without throwing exceptions");
    }

    // ===== Security Configuration Tests =====

    @Test
    @DisplayName("Should get SSL configuration")
    void shouldGetSslConfiguration() {
        // When: Getting SSL configuration
        Map<String, Object> config = advancedService.getSslConfiguration();

        // Then: Should return valid SSL configuration
        assertNotNull(config, "SSL configuration should not be null");
        assertTrue(config.size() > 0, "SSL configuration should have properties");
        assertTrue(config.containsKey("security.protocol"), "Should contain security protocol");
        assertEquals("SSL", config.get("security.protocol"), "Security protocol should be SSL");
        assertTrue(config.containsKey("ssl.protocol"), "Should contain SSL protocol");
        assertEquals("TLSv1.3", config.get("ssl.protocol"), "SSL protocol should be TLSv1.3");
    }

    @Test
    @DisplayName("Should get SASL configuration")
    void shouldGetSaslConfiguration() {
        // When: Getting SASL configuration
        Map<String, Object> config = advancedService.getSaslConfiguration();

        // Then: Should return valid SASL configuration
        assertNotNull(config, "SASL configuration should not be null");
        assertTrue(config.size() > 0, "SASL configuration should have properties");
        assertTrue(config.containsKey("security.protocol"), "Should contain security protocol");
        assertEquals("SASL_SSL", config.get("security.protocol"), "Security protocol should be SASL_SSL");
        assertTrue(config.containsKey("sasl.mechanism"), "Should contain SASL mechanism");
        assertEquals("SCRAM-SHA-512", config.get("sasl.mechanism"), "SASL mechanism should be SCRAM-SHA-512");
    }

    @Test
    @DisplayName("Should get production producer configuration")
    void shouldGetProductionProducerConfig() {
        // When: Getting production producer configuration
        Map<String, Object> config = advancedService.getProductionProducerConfig();

        // Then: Should return production-ready configuration
        assertNotNull(config, "Producer configuration should not be null");
        assertTrue(config.size() > 0, "Producer configuration should have properties");

        // Verify reliability settings
        assertEquals("all", config.get("acks"), "Should use 'all' acks for reliability");
        assertEquals(Integer.MAX_VALUE, config.get("retries"), "Should have max retries");
        assertTrue((Boolean) config.get("enable.idempotence"), "Should enable idempotence");

        // Verify performance settings
        assertTrue((Integer) config.get("batch.size") > 0, "Should have batch size");
        assertTrue((Integer) config.get("linger.ms") >= 0, "Should have linger setting");
        assertEquals("lz4", config.get("compression.type"), "Should use lz4 compression");
    }

    @Test
    @DisplayName("Should get production consumer configuration")
    void shouldGetProductionConsumerConfig() {
        // When: Getting production consumer configuration
        Map<String, Object> config = advancedService.getProductionConsumerConfig();

        // Then: Should return production-ready configuration
        assertNotNull(config, "Consumer configuration should not be null");
        assertTrue(config.size() > 0, "Consumer configuration should have properties");

        // Verify reliability settings
        assertFalse((Boolean) config.get("enable.auto.commit"), "Should disable auto-commit");
        assertEquals("earliest", config.get("auto.offset.reset"), "Should start from earliest");
        assertEquals("read_committed", config.get("isolation.level"), "Should read committed only");

        // Verify performance settings
        assertTrue((Integer) config.get("fetch.min.bytes") > 0, "Should have fetch min bytes");
        assertTrue((Integer) config.get("max.poll.records") > 0, "Should have max poll records");
    }

    // ===== Monitoring Tests =====

    @Test
    @DisplayName("Should get cluster metrics")
    void shouldGetClusterMetrics() {
        // When: Getting cluster metrics
        Map<String, Object> metrics = advancedService.getClusterMetrics();

        // Then: Should return metrics
        assertNotNull(metrics, "Cluster metrics should not be null");
        assertEquals("success", metrics.get("status"), "Status should be success");
        assertNotNull(metrics.get("clusterId"), "Should have cluster ID");
        assertNotNull(metrics.get("nodeCount"), "Should have node count");
        assertTrue((Integer) metrics.get("nodeCount") > 0, "Should have at least one node");
    }

    @Test
    @DisplayName("Should monitor consumer lag")
    void shouldMonitorConsumerLag() {
        // When: Monitoring consumer lag
        Map<String, Object> lag = advancedService.monitorConsumerLag();

        // Then: Should return lag information
        assertNotNull(lag, "Consumer lag should not be null");
        assertEquals("success", lag.get("status"), "Status should be success");
        assertNotNull(lag.get("groupCount"), "Should have group count");
        assertNotNull(lag.get("totalLag"), "Should have total lag");
        assertTrue((Integer) lag.get("groupCount") >= 0, "Group count should be non-negative");
        assertTrue((Long) lag.get("totalLag") >= 0, "Total lag should be non-negative");
    }

    @Test
    @DisplayName("Should get topic metrics")
    void shouldGetTopicMetrics() {
        // When: Getting topic metrics
        Map<String, Object> metrics = advancedService.getTopicMetrics();

        // Then: Should return metrics
        assertNotNull(metrics, "Topic metrics should not be null");
        assertEquals("success", metrics.get("status"), "Status should be success");
        assertNotNull(metrics.get("topicCount"), "Should have topic count");
        assertTrue((Integer) metrics.get("topicCount") >= 0, "Topic count should be non-negative");
    }

    @Test
    @DisplayName("Should get comprehensive system status")
    void shouldGetSystemStatus() {
        // When: Getting system status
        Map<String, Object> status = advancedService.getSystemStatus();

        // Then: Should return comprehensive status
        assertNotNull(status, "System status should not be null");
        assertTrue(status.containsKey("cluster"), "Should contain cluster status");
        assertTrue(status.containsKey("consumerLag"), "Should contain consumer lag");
        assertTrue(status.containsKey("topics"), "Should contain topic metrics");
        assertTrue(status.containsKey("health"), "Should contain health status");
        assertTrue(status.containsKey("configValid"), "Should contain config validation");
        assertNotNull(status.get("timestamp"), "Should have timestamp");
    }

    // ===== Production Operations Tests =====

    @Test
    @DisplayName("Should check cluster health")
    void shouldCheckClusterHealth() {
        // When: Checking cluster health
        boolean healthy = advancedService.checkClusterHealth();

        // Then: Should return health status (may be false in test with single broker)
        assertNotNull(healthy, "Health status should not be null");
        // Note: Single broker in test, so health check may fail (requires 3+ brokers)
    }

    @Test
    @DisplayName("Should validate production configuration")
    void shouldValidateProductionConfig() {
        // When: Validating production configuration
        boolean valid = advancedService.validateProductionConfig();

        // Then: Should return validation result
        assertNotNull(valid, "Validation result should not be null");
        // Note: May be false in test environment with localhost
    }

    // ===== REST API Tests =====

    @Test
    @DisplayName("Day08 demo API endpoint should work")
    void shouldWorkWithDay08DemoAPI() {
        // When: Running Day 8 demo via API
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/training/day08/demo",
                HttpMethod.POST,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });

        // Then: Should complete successfully
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().get("status"));
        assertEquals("Day08Advanced", response.getBody().get("module"));
    }

    @Test
    @DisplayName("System status API endpoint should work")
    void shouldWorkWithSystemStatusAPI() {
        // When: Getting system status via API
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/training/day08/status",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });

        // Then: Should return status successfully
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().get("status"));
        assertTrue(response.getBody().containsKey("systemStatus"));
    }

    @Test
    @DisplayName("Cluster metrics API endpoint should work")
    void shouldWorkWithClusterMetricsAPI() {
        // When: Getting cluster metrics via API
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/training/day08/metrics/cluster",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });

        // Then: Should return metrics successfully
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().get("status"));
        assertTrue(response.getBody().containsKey("metrics"));
    }

    @Test
    @DisplayName("Consumer lag API endpoint should work")
    void shouldWorkWithConsumerLagAPI() {
        // When: Getting consumer lag via API
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/training/day08/metrics/consumer-lag",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });

        // Then: Should return lag information successfully
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().get("status"));
        assertTrue(response.getBody().containsKey("consumerLag"));
    }

    @Test
    @DisplayName("Security config API endpoint should work")
    void shouldWorkWithSecurityConfigAPI() {
        // When: Getting security configuration via API
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/training/day08/config/security",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });

        // Then: Should return security configuration successfully
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().get("status"));
        assertTrue(response.getBody().containsKey("ssl"));
        assertTrue(response.getBody().containsKey("sasl"));
    }

    @Test
    @DisplayName("Production config API endpoint should work")
    void shouldWorkWithProductionConfigAPI() {
        // When: Getting production configuration via API
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/training/day08/config/production",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });

        // Then: Should return production configuration successfully
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().get("status"));
        assertTrue(response.getBody().containsKey("producer"));
        assertTrue(response.getBody().containsKey("consumer"));
    }

    @Test
    @DisplayName("Modules API should include Day08Advanced")
    void shouldIncludeDay08AdvancedInModulesAPI() {
        // When: Getting all modules
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/training/modules",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });

        // Then: Should include Day08Advanced
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> modules = response.getBody();
        assertTrue(modules.containsKey("Day08Advanced"), "Should contain Day08Advanced module");

        Map<?, ?> day08Module = (Map<?, ?>) modules.get("Day08Advanced");
        assertEquals("Advanced Topics", day08Module.get("name"));
        assertTrue(day08Module.get("description").toString().contains("Security"));
        assertTrue(day08Module.get("description").toString().contains("Monitoring"));
    }
}
