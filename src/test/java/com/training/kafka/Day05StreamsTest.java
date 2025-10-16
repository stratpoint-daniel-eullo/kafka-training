package com.training.kafka;

import com.training.kafka.services.Day05StreamsService;
import org.junit.jupiter.api.AfterEach;
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

import java.time.Duration;
import java.util.Map;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for Day 5 Kafka Streams functionality
 *
 * Tests cover:
 * - Stream application lifecycle (start/stop)
 * - Stream processing with test data
 * - REST API endpoints
 * - Error handling
 * - Multiple stream applications running concurrently
 *
 * @author Kafka Training Course
 * @version 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class Day05StreamsTest {

    @Container
    @SuppressWarnings("resource") // Testcontainers manages lifecycle
    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.7.0"))
            .withEmbeddedZookeeper();

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private Day05StreamsService streamsService;

    /**
     * Configure Spring Boot properties dynamically from TestContainers
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("training.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.streams.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("training.kafka.client-id", () -> "streams-test-client");
    }

    /**
     * Stop all streams after each test to ensure clean state
     */
    @AfterEach
    void stopAllStreams() {
        streamsService.stopUserActivityStream();
        streamsService.stopOrderAnalyticsStream();
        streamsService.stopFraudDetectionStream();
        streamsService.stopEventMartRealtimeAnalytics();

        // Wait a bit for streams to fully stop
        await().atMost(Duration.ofSeconds(5)).until(() -> {
            Map<String, Object> status = streamsService.getStreamsStatus();
            return !((Map<?, ?>) status.get("userActivityStream")).get("running").equals(true) &&
                   !((Map<?, ?>) status.get("orderAnalyticsStream")).get("running").equals(true) &&
                   !((Map<?, ?>) status.get("fraudDetectionStream")).get("running").equals(true) &&
                   !((Map<?, ?>) status.get("eventMartRealtimeStream")).get("running").equals(true);
        });
    }

    // ===== Service Tests =====

    @Test
    @DisplayName("Day05StreamsService should be initialized")
    void shouldInitializeStreamsService() {
        assertNotNull(streamsService, "Streams service should be initialized");
    }

    @Test
    @DisplayName("Should demonstrate Kafka Streams without errors")
    void shouldDemonstrateKafkaStreamsWithoutErrors() {
        // When: Running streams demonstration
        assertDoesNotThrow(() -> streamsService.demonstrateKafkaStreams(),
            "Streams demonstration should run without throwing exceptions");
    }

    @Test
    @DisplayName("Should get streams status")
    void shouldGetStreamsStatus() {
        // When: Getting streams status
        Map<String, Object> status = streamsService.getStreamsStatus();

        // Then: Should return status for all streams
        assertNotNull(status, "Status should not be null");
        assertTrue(status.containsKey("userActivityStream"), "Should contain user activity stream status");
        assertTrue(status.containsKey("orderAnalyticsStream"), "Should contain order analytics stream status");
        assertTrue(status.containsKey("fraudDetectionStream"), "Should contain fraud detection stream status");
        assertTrue(status.containsKey("eventMartRealtimeStream"), "Should contain EventMart realtime stream status");

        // All streams should be NOT_CREATED or NOT_RUNNING initially
        Map<?, ?> userActivityStatus = (Map<?, ?>) status.get("userActivityStream");
        assertFalse((Boolean) userActivityStatus.get("running"), "User activity stream should not be running initially");
    }

    // ===== User Activity Stream Tests =====

    @Test
    @DisplayName("Should start and stop User Activity Stream")
    void shouldStartAndStopUserActivityStream() {
        // When: Starting user activity stream
        boolean started = streamsService.startUserActivityStream();

        // Then: Should start successfully
        assertTrue(started, "User activity stream should start successfully");

        // Wait for stream to be running
        await().atMost(Duration.ofSeconds(10)).until(() -> {
            Map<String, Object> status = streamsService.getStreamsStatus();
            Map<?, ?> streamStatus = (Map<?, ?>) status.get("userActivityStream");
            return (Boolean) streamStatus.get("running");
        });

        // When: Trying to start again
        boolean startedAgain = streamsService.startUserActivityStream();

        // Then: Should not start (already running)
        assertFalse(startedAgain, "Should not start stream that is already running");

        // When: Stopping stream
        boolean stopped = streamsService.stopUserActivityStream();

        // Then: Should stop successfully
        assertTrue(stopped, "User activity stream should stop successfully");
    }

    // ===== Order Analytics Stream Tests =====

    @Test
    @DisplayName("Should start and stop Order Analytics Stream")
    void shouldStartAndStopOrderAnalyticsStream() {
        // When: Starting order analytics stream
        boolean started = streamsService.startOrderAnalyticsStream();

        // Then: Should start successfully
        assertTrue(started, "Order analytics stream should start successfully");

        // Wait for stream to be running
        await().atMost(Duration.ofSeconds(10)).until(() -> {
            Map<String, Object> status = streamsService.getStreamsStatus();
            Map<?, ?> streamStatus = (Map<?, ?>) status.get("orderAnalyticsStream");
            return (Boolean) streamStatus.get("running");
        });

        // When: Stopping stream
        boolean stopped = streamsService.stopOrderAnalyticsStream();

        // Then: Should stop successfully
        assertTrue(stopped, "Order analytics stream should stop successfully");
    }

    // ===== Fraud Detection Stream Tests =====

    @Test
    @DisplayName("Should start and stop Fraud Detection Stream")
    void shouldStartAndStopFraudDetectionStream() {
        // When: Starting fraud detection stream
        boolean started = streamsService.startFraudDetectionStream();

        // Then: Should start successfully
        assertTrue(started, "Fraud detection stream should start successfully");

        // Wait for stream to be running
        await().atMost(Duration.ofSeconds(10)).until(() -> {
            Map<String, Object> status = streamsService.getStreamsStatus();
            Map<?, ?> streamStatus = (Map<?, ?>) status.get("fraudDetectionStream");
            return (Boolean) streamStatus.get("running");
        });

        // When: Stopping stream
        boolean stopped = streamsService.stopFraudDetectionStream();

        // Then: Should stop successfully
        assertTrue(stopped, "Fraud detection stream should stop successfully");
    }

    // ===== EventMart Real-time Analytics Tests =====

    @Test
    @DisplayName("Should start and stop EventMart Real-time Analytics")
    void shouldStartAndStopEventMartRealtimeAnalytics() {
        // When: Starting EventMart real-time analytics
        boolean started = streamsService.startEventMartRealtimeAnalytics();

        // Then: Should start successfully
        assertTrue(started, "EventMart real-time analytics should start successfully");

        // Wait for stream to be running
        await().atMost(Duration.ofSeconds(10)).until(() -> {
            Map<String, Object> status = streamsService.getStreamsStatus();
            Map<?, ?> streamStatus = (Map<?, ?>) status.get("eventMartRealtimeStream");
            return (Boolean) streamStatus.get("running");
        });

        // When: Stopping stream
        boolean stopped = streamsService.stopEventMartRealtimeAnalytics();

        // Then: Should stop successfully
        assertTrue(stopped, "EventMart real-time analytics should stop successfully");
    }

    // ===== Multiple Streams Tests =====

    @Test
    @DisplayName("Should run multiple streams concurrently")
    void shouldRunMultipleStreamsConcurrently() {
        // When: Starting multiple streams
        boolean userStarted = streamsService.startUserActivityStream();
        boolean orderStarted = streamsService.startOrderAnalyticsStream();
        boolean fraudStarted = streamsService.startFraudDetectionStream();

        // Then: All should start successfully
        assertTrue(userStarted, "User activity stream should start");
        assertTrue(orderStarted, "Order analytics stream should start");
        assertTrue(fraudStarted, "Fraud detection stream should start");

        // Wait for all streams to be running
        await().atMost(Duration.ofSeconds(15)).until(() -> {
            Map<String, Object> status = streamsService.getStreamsStatus();
            return ((Map<?, ?>) status.get("userActivityStream")).get("running").equals(true) &&
                   ((Map<?, ?>) status.get("orderAnalyticsStream")).get("running").equals(true) &&
                   ((Map<?, ?>) status.get("fraudDetectionStream")).get("running").equals(true);
        });

        // When: Getting status
        Map<String, Object> status = streamsService.getStreamsStatus();

        // Then: All should be running
        assertTrue((Boolean) ((Map<?, ?>) status.get("userActivityStream")).get("running"));
        assertTrue((Boolean) ((Map<?, ?>) status.get("orderAnalyticsStream")).get("running"));
        assertTrue((Boolean) ((Map<?, ?>) status.get("fraudDetectionStream")).get("running"));
    }

    // ===== REST API Tests =====

    @Test
    @DisplayName("Day05 demo API endpoint should work")
    void shouldWorkWithDay05DemoAPI() {
        // When: Running Day 5 demo via API
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/training/day05/demo",
            HttpMethod.POST,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then: Should complete successfully
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().get("status"));
        assertEquals("Day05Streams", response.getBody().get("module"));
    }

    @Test
    @DisplayName("Streams status API endpoint should work")
    void shouldWorkWithStreamsStatusAPI() {
        // When: Getting streams status via API
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/training/day05/streams/status",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then: Should return status successfully
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().get("status"));
        assertTrue(response.getBody().containsKey("streams"));
    }

    @Test
    @DisplayName("User Activity Stream API endpoints should work")
    void shouldWorkWithUserActivityStreamAPI() {
        // When: Starting user activity stream via API
        ResponseEntity<Map<String, Object>> startResponse = restTemplate.exchange(
            "http://localhost:" + port + "/api/training/day05/streams/user-activity/start",
            HttpMethod.POST,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then: Should start successfully
        assertEquals(HttpStatus.OK, startResponse.getStatusCode());
        assertNotNull(startResponse.getBody());
        assertTrue(startResponse.getBody().get("status").equals("success") ||
                   startResponse.getBody().get("status").equals("warning"));

        // Wait for stream to start
        await().atMost(Duration.ofSeconds(10)).until(() -> {
            Map<String, Object> status = streamsService.getStreamsStatus();
            Map<?, ?> streamStatus = (Map<?, ?>) status.get("userActivityStream");
            return (Boolean) streamStatus.get("running");
        });

        // When: Stopping user activity stream via API
        ResponseEntity<Map<String, Object>> stopResponse = restTemplate.exchange(
            "http://localhost:" + port + "/api/training/day05/streams/user-activity/stop",
            HttpMethod.POST,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then: Should stop successfully
        assertEquals(HttpStatus.OK, stopResponse.getStatusCode());
        assertNotNull(stopResponse.getBody());
        assertTrue(stopResponse.getBody().get("status").equals("success") ||
                   stopResponse.getBody().get("status").equals("warning"));
    }

    @Test
    @DisplayName("Order Analytics Stream API endpoints should work")
    void shouldWorkWithOrderAnalyticsStreamAPI() {
        // When: Starting order analytics stream via API
        ResponseEntity<Map<String, Object>> startResponse = restTemplate.exchange(
            "http://localhost:" + port + "/api/training/day05/streams/order-analytics/start",
            HttpMethod.POST,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then: Should start successfully
        assertEquals(HttpStatus.OK, startResponse.getStatusCode());
        assertEquals("order-analytics", startResponse.getBody().get("stream"));

        // When: Stopping via API
        ResponseEntity<Map<String, Object>> stopResponse = restTemplate.exchange(
            "http://localhost:" + port + "/api/training/day05/streams/order-analytics/stop",
            HttpMethod.POST,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then: Should stop successfully
        assertEquals(HttpStatus.OK, stopResponse.getStatusCode());
    }

    @Test
    @DisplayName("Fraud Detection Stream API endpoints should work")
    void shouldWorkWithFraudDetectionStreamAPI() {
        // When: Starting fraud detection stream via API
        ResponseEntity<Map<String, Object>> startResponse = restTemplate.exchange(
            "http://localhost:" + port + "/api/training/day05/streams/fraud-detection/start",
            HttpMethod.POST,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then: Should start successfully
        assertEquals(HttpStatus.OK, startResponse.getStatusCode());
        assertEquals("fraud-detection", startResponse.getBody().get("stream"));

        // When: Stopping via API
        ResponseEntity<Map<String, Object>> stopResponse = restTemplate.exchange(
            "http://localhost:" + port + "/api/training/day05/streams/fraud-detection/stop",
            HttpMethod.POST,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then: Should stop successfully
        assertEquals(HttpStatus.OK, stopResponse.getStatusCode());
    }

    @Test
    @DisplayName("EventMart Real-time Analytics API endpoints should work")
    void shouldWorkWithEventMartRealtimeAPI() {
        // When: Starting EventMart real-time analytics via API
        ResponseEntity<Map<String, Object>> startResponse = restTemplate.exchange(
            "http://localhost:" + port + "/api/training/day05/streams/eventmart/start",
            HttpMethod.POST,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then: Should start successfully
        assertEquals(HttpStatus.OK, startResponse.getStatusCode());
        assertEquals("eventmart-realtime", startResponse.getBody().get("stream"));

        // When: Stopping via API
        ResponseEntity<Map<String, Object>> stopResponse = restTemplate.exchange(
            "http://localhost:" + port + "/api/training/day05/streams/eventmart/stop",
            HttpMethod.POST,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then: Should stop successfully
        assertEquals(HttpStatus.OK, stopResponse.getStatusCode());
    }

    @Test
    @DisplayName("Should handle stopping streams that are not running")
    void shouldHandleStoppingNonRunningStreams() {
        // Given: Streams are not running

        // When: Trying to stop streams
        boolean userStopped = streamsService.stopUserActivityStream();
        boolean orderStopped = streamsService.stopOrderAnalyticsStream();
        boolean fraudStopped = streamsService.stopFraudDetectionStream();
        boolean eventMartStopped = streamsService.stopEventMartRealtimeAnalytics();

        // Then: Should return false (not running)
        assertFalse(userStopped, "Should not stop stream that is not running");
        assertFalse(orderStopped, "Should not stop stream that is not running");
        assertFalse(fraudStopped, "Should not stop stream that is not running");
        assertFalse(eventMartStopped, "Should not stop stream that is not running");
    }

    @Test
    @DisplayName("Modules API should include Day05Streams")
    void shouldIncludeDay05StreamsInModulesAPI() {
        // When: Getting all modules
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/training/modules",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then: Should include Day05Streams
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> modules = response.getBody();
        assertTrue(modules.containsKey("Day05Streams"), "Should contain Day05Streams module");

        Map<?, ?> day05Module = (Map<?, ?>) modules.get("Day05Streams");
        assertEquals("Kafka Streams", day05Module.get("name"));
        assertTrue(day05Module.get("description").toString().contains("stream processing"));
    }
}
