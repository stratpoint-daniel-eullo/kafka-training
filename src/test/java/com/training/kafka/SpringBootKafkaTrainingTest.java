package com.training.kafka;

import com.training.kafka.services.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
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
 * Spring Boot integration tests for the Kafka Training Application
 * 
 * This test verifies that the Spring Boot application starts correctly
 * and all services are properly initialized with Kafka integration.
 * 
 * @author Kafka Training Course
 * @version 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class SpringBootKafkaTrainingTest {
    
    @Container
    @SuppressWarnings("resource") // Testcontainers manages lifecycle
    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.7.0"))
            .withEmbeddedZookeeper();
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private Day01FoundationService foundationService;

    @Autowired
    private Day03ProducerService producerService;

    @Autowired
    private Day04ConsumerService consumerService;

    @Autowired
    private Day05StreamsService streamsService;

    @Autowired
    private EventMartService eventMartService;
    
    /**
     * Configure Spring Boot properties dynamically from TestContainers
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("training.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("training.kafka.client-id", () -> "spring-boot-test-client");
    }
    
    @Test
    @DisplayName("Spring Boot application should start successfully with all services")
    void shouldStartApplicationWithAllServices() {
        // Given: Spring Boot application is running with TestContainers Kafka
        
        // Then: All services should be properly initialized
        assertNotNull(foundationService, "Foundation service should be initialized");
        assertNotNull(producerService, "Producer service should be initialized");
        assertNotNull(consumerService, "Consumer service should be initialized");
        assertNotNull(streamsService, "Streams service should be initialized");
        assertNotNull(eventMartService, "EventMart service should be initialized");
        
        // Verify services can perform basic operations
        assertDoesNotThrow(() -> foundationService.listTopics(), 
            "Foundation service should be able to list topics");
        
        assertDoesNotThrow(() -> foundationService.getClusterInfo(), 
            "Foundation service should be able to get cluster info");
    }
    
    @Test
    @DisplayName("Web interface should be accessible")
    void shouldAccessWebInterface() {
        // When: Accessing the main web interface
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/", String.class);
        
        // Then: Should return the web interface successfully
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Kafka Training"), 
            "Web interface should contain Kafka Training content");
    }
    
    @Test
    @DisplayName("API should list all training modules")
    void shouldListAllTrainingModules() {
        // When: Accessing the modules API
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/training/modules",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then: Should return all training modules
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, Object> modules = response.getBody();
        assertTrue(modules.containsKey("Day01Foundation"), "Should contain Day01Foundation module");
        assertTrue(modules.containsKey("Day03Producers"), "Should contain Day03Producers module");
        assertTrue(modules.containsKey("Day04Consumers"), "Should contain Day04Consumers module");
        assertTrue(modules.containsKey("Day05Streams"), "Should contain Day05Streams module");
        assertTrue(modules.containsKey("EventMart"), "Should contain EventMart module");
    }
    
    @Test
    @DisplayName("Day01 Foundation API endpoints should work")
    void shouldWorkWithDay01FoundationAPI() {
        // When: Running foundation demo via API
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/training/day01/demo",
            HttpMethod.POST,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then: Should complete successfully
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().get("status"));
    }
    
    @Test
    @DisplayName("Day03 Producer API endpoints should work")
    void shouldWorkWithDay03ProducerAPI() {
        // When: Running producer demo via API
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/training/day03/demo",
            HttpMethod.POST,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then: Should complete successfully
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().get("status"));
    }
    
    @Test
    @DisplayName("Day04 Consumer API endpoints should work")
    void shouldWorkWithDay04ConsumerAPI() {
        // When: Running consumer demo via API
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/training/day04/demo",
            HttpMethod.POST,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then: Should complete successfully
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().get("status"));
    }

    @Test
    @DisplayName("Day05 Streams API endpoints should work")
    void shouldWorkWithDay05StreamsAPI() {
        // When: Running streams demo via API
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/training/day05/demo",
            HttpMethod.POST,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then: Should complete successfully
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().get("status"));

        // When: Getting streams status via API
        ResponseEntity<Map<String, Object>> statusResponse = restTemplate.exchange(
            "http://localhost:" + port + "/api/training/day05/streams/status",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then: Should return status successfully
        assertEquals(HttpStatus.OK, statusResponse.getStatusCode());
        assertEquals("success", statusResponse.getBody().get("status"));
        assertTrue(statusResponse.getBody().containsKey("streams"));
    }

    @Test
    @DisplayName("EventMart API endpoints should work")
    void shouldWorkWithEventMartAPI() {
        // When: Creating EventMart topics via API
        ResponseEntity<Map<String, Object>> topicsResponse = restTemplate.exchange(
            "http://localhost:" + port + "/api/training/eventmart/topics",
            HttpMethod.POST,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then: Should create topics successfully
        assertEquals(HttpStatus.OK, topicsResponse.getStatusCode());
        assertEquals("success", topicsResponse.getBody().get("status"));

        // When: Getting EventMart status via API
        ResponseEntity<Map<String, Object>> statusResponse = restTemplate.exchange(
            "http://localhost:" + port + "/api/training/eventmart/status",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then: Should return status successfully
        assertEquals(HttpStatus.OK, statusResponse.getStatusCode());
        assertEquals("success", statusResponse.getBody().get("status"));
        assertTrue(statusResponse.getBody().containsKey("topicsCreated"));
    }
    
    @Test
    @DisplayName("EventMart simulation endpoints should work")
    void shouldWorkWithEventMartSimulationAPI() {
        // Given: EventMart topics exist
        eventMartService.createEventMartTopics();

        // When: Simulating user registration via API
        ResponseEntity<Map<String, Object>> userResponse = restTemplate.exchange(
            "http://localhost:" + port + "/api/training/eventmart/simulate/user" +
            "?userId=test-user&email=test@example.com&name=Test User",
            HttpMethod.POST,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then: Should simulate successfully
        assertEquals(HttpStatus.OK, userResponse.getStatusCode());
        assertEquals("success", userResponse.getBody().get("status"));

        // When: Simulating product creation via API
        ResponseEntity<Map<String, Object>> productResponse = restTemplate.exchange(
            "http://localhost:" + port + "/api/training/eventmart/simulate/product" +
            "?productId=test-product&name=Test Product&category=Electronics&price=99.99",
            HttpMethod.POST,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then: Should simulate successfully
        assertEquals(HttpStatus.OK, productResponse.getStatusCode());
        assertEquals("success", productResponse.getBody().get("status"));

        // When: Simulating order placement via API
        ResponseEntity<Map<String, Object>> orderResponse = restTemplate.exchange(
            "http://localhost:" + port + "/api/training/eventmart/simulate/order" +
            "?orderId=test-order&userId=test-user&amount=99.99",
            HttpMethod.POST,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then: Should simulate successfully
        assertEquals(HttpStatus.OK, orderResponse.getStatusCode());
        assertEquals("success", orderResponse.getBody().get("status"));
    }
    
    @Test
    @DisplayName("Foundation service should work with Kafka operations")
    void shouldWorkWithFoundationServiceKafkaOperations() {
        // When: Creating a test topic
        boolean topicCreated = foundationService.createTopic("spring-test-topic", 3, (short) 1);
        
        // Then: Topic should be created successfully
        assertTrue(topicCreated, "Topic should be created successfully");
        
        // When: Checking if topic exists
        boolean exists = foundationService.topicExists("spring-test-topic");
        
        // Then: Topic should exist
        assertTrue(exists, "Topic should exist after creation");
        
        // When: Listing topics
        var topics = foundationService.listTopics();
        
        // Then: Should contain our test topic
        assertNotNull(topics, "Topics list should not be null");
        assertTrue(topics.contains("spring-test-topic"), "Topics should contain our test topic");
    }
    
    @Test
    @DisplayName("EventMart service should work with topic creation")
    void shouldWorkWithEventMartServiceTopicCreation() {
        // When: Creating EventMart topics
        boolean result = eventMartService.createEventMartTopics();
        
        // Then: Should create topics successfully
        assertTrue(result, "EventMart topics should be created successfully");
        
        // When: Getting EventMart status
        EventMartService.EventMartStatus status = eventMartService.getEventMartStatus();
        
        // Then: Should return valid status
        assertNotNull(status, "EventMart status should not be null");
        assertNotNull(status.timestamp, "Status timestamp should not be null");
        assertTrue(status.topicsCreated, "Topics should be marked as created");
    }
    
    @Test
    @DisplayName("Error handling should work correctly")
    void shouldHandleErrorsCorrectly() {
        // When: Accessing non-existent endpoint
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/api/training/nonexistent", String.class);
        
        // Then: Should return 404
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
    
    @Test
    @DisplayName("Foundation service should run demonstration without errors")
    void shouldRunFoundationDemonstrationWithoutErrors() {
        // When: Running foundation demonstration
        assertDoesNotThrow(() -> foundationService.runDay01Demonstration(),
            "Foundation demonstration should run without throwing exceptions");
    }
}
