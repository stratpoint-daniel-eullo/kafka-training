package com.training.kafka;

import com.training.kafka.services.Day01FoundationService;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.admin.TopicListing;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for Day 1 Foundation Module
 *
 * Tests cover:
 * - AdminClient operations (create, list, describe, delete topics)
 * - Topic management functionality
 * - Cluster information retrieval
 * - REST API endpoints
 *
 * @author Kafka Training Course
 * @version 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class Day01FoundationTest {

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

    /**
     * Configure Spring Boot properties dynamically from TestContainers
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("training.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("training.kafka.client-id", () -> "foundation-test-client");
    }

    // ===== Service Tests =====

    @Test
    @DisplayName("Day01FoundationService should be initialized")
    void shouldInitializeFoundationService() {
        assertNotNull(foundationService, "Foundation service should be initialized");
    }

    @Test
    @DisplayName("Should create topic successfully")
    void shouldCreateTopic() {
        // Given: A unique topic name
        String topicName = "test-create-topic-" + System.currentTimeMillis();

        // When: Creating the topic
        boolean created = foundationService.createTopic(topicName, 3, (short) 1);

        // Then: Topic should be created successfully
        assertTrue(created, "Topic should be created successfully");
        assertTrue(foundationService.topicExists(topicName), "Topic should exist after creation");
    }

    @Test
    @DisplayName("Should not create duplicate topic")
    void shouldNotCreateDuplicateTopic() {
        // Given: An existing topic
        String topicName = "test-duplicate-topic-" + System.currentTimeMillis();
        foundationService.createTopic(topicName, 3, (short) 1);

        // When: Attempting to create the same topic again
        boolean created = foundationService.createTopic(topicName, 3, (short) 1);

        // Then: Creation should fail
        assertFalse(created, "Duplicate topic creation should return false");
    }

    @Test
    @DisplayName("Should list all topics")
    void shouldListTopics() {
        // Given: At least one topic exists
        String topicName = "test-list-topic-" + System.currentTimeMillis();
        foundationService.createTopic(topicName, 3, (short) 1);

        // When: Listing topics
        Set<String> topics = foundationService.listTopics();

        // Then: Should return topic list
        assertNotNull(topics, "Topics list should not be null");
        assertTrue(topics.size() > 0, "Should have at least one topic");
        assertTrue(topics.contains(topicName), "Should contain the created topic");
    }

    @Test
    @DisplayName("Should list topics with details")
    void shouldListTopicsWithDetails() {
        // Given: At least one topic exists
        String topicName = "test-list-details-topic-" + System.currentTimeMillis();
        foundationService.createTopic(topicName, 3, (short) 1);

        // When: Listing topics with details
        Map<String, TopicListing> topicsWithDetails = foundationService.listTopicsWithDetails();

        // Then: Should return detailed topic information
        assertNotNull(topicsWithDetails, "Topics details map should not be null");
        assertTrue(topicsWithDetails.size() > 0, "Should have at least one topic");
        assertTrue(topicsWithDetails.containsKey(topicName), "Should contain the created topic");

        TopicListing listing = topicsWithDetails.get(topicName);
        assertNotNull(listing, "Topic listing should not be null");
        assertEquals(topicName, listing.name(), "Topic name should match");
    }

    @Test
    @DisplayName("Should describe topic")
    void shouldDescribeTopic() {
        // Given: A topic with known configuration
        String topicName = "test-describe-topic-" + System.currentTimeMillis();
        int numPartitions = 4;
        foundationService.createTopic(topicName, numPartitions, (short) 1);

        // When: Describing the topic
        TopicDescription description = foundationService.describeTopic(topicName);

        // Then: Should return correct topic description
        assertNotNull(description, "Topic description should not be null");
        assertEquals(topicName, description.name(), "Topic name should match");
        assertEquals(numPartitions, description.partitions().size(),
            "Number of partitions should match");

        // Verify each partition has the expected structure
        description.partitions().forEach(partition -> {
            assertNotNull(partition.leader(), "Each partition should have a leader");
            assertTrue(partition.replicas().size() > 0, "Each partition should have replicas");
        });
    }

    @Test
    @DisplayName("Should return null when describing non-existent topic")
    void shouldReturnNullForNonExistentTopic() {
        // Given: A non-existent topic name
        String topicName = "non-existent-topic-" + System.currentTimeMillis();

        // When: Attempting to describe the topic
        TopicDescription description = foundationService.describeTopic(topicName);

        // Then: Should return null
        assertNull(description, "Description of non-existent topic should be null");
    }

    @Test
    @DisplayName("Should delete topic")
    void shouldDeleteTopic() {
        // Given: An existing topic
        String topicName = "test-delete-topic-" + System.currentTimeMillis();
        foundationService.createTopic(topicName, 3, (short) 1);
        assertTrue(foundationService.topicExists(topicName), "Topic should exist before deletion");

        // When: Deleting the topic
        boolean deleted = foundationService.deleteTopic(topicName);

        // Then: Topic should be deleted successfully
        assertTrue(deleted, "Topic should be deleted successfully");
        // Note: Topic deletion is asynchronous, may take a moment
    }

    @Test
    @DisplayName("Should check if topic exists")
    void shouldCheckTopicExists() {
        // Given: A topic that exists
        String existingTopic = "test-exists-topic-" + System.currentTimeMillis();
        foundationService.createTopic(existingTopic, 3, (short) 1);

        // And: A topic that doesn't exist
        String nonExistentTopic = "non-existent-topic-" + System.currentTimeMillis();

        // When: Checking existence
        boolean exists = foundationService.topicExists(existingTopic);
        boolean notExists = foundationService.topicExists(nonExistentTopic);

        // Then: Should return correct existence status
        assertTrue(exists, "Existing topic should return true");
        assertFalse(notExists, "Non-existent topic should return false");
    }

    @Test
    @DisplayName("Should run Day 1 demonstration without errors")
    void shouldRunDay01DemonstrationWithoutErrors() {
        // When: Running Day 1 demonstration
        assertDoesNotThrow(() -> foundationService.runDay01Demonstration(),
                "Day 1 demonstration should run without throwing exceptions");
    }

    @Test
    @DisplayName("Should get cluster information without errors")
    void shouldGetClusterInfoWithoutErrors() {
        // When: Getting cluster information
        assertDoesNotThrow(() -> foundationService.getClusterInfo(),
                "Getting cluster info should not throw exceptions");
    }

    // ===== REST API Tests =====

    @Test
    @DisplayName("Day01 demo API endpoint should work")
    void shouldWorkWithDay01DemoAPI() {
        // When: Running Day 1 demo via API
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/training/day01/demo",
                HttpMethod.POST,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });

        // Then: Should complete successfully
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().get("status"));
        assertEquals("Day01Foundation", response.getBody().get("module"));
    }

    @Test
    @DisplayName("List topics API endpoint should work")
    void shouldWorkWithListTopicsAPI() {
        // Given: At least one topic exists
        String topicName = "test-api-list-topic-" + System.currentTimeMillis();
        foundationService.createTopic(topicName, 3, (short) 1);

        // When: Listing topics via API
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/training/day01/topics",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });

        // Then: Should return topics successfully
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().get("status"));
        assertNotNull(response.getBody().get("topics"));
        assertTrue((Integer) response.getBody().get("count") > 0);
    }

    @Test
    @DisplayName("Create topic API endpoint should work")
    void shouldWorkWithCreateTopicAPI() {
        // Given: A unique topic name
        String topicName = "test-api-create-topic-" + System.currentTimeMillis();

        // When: Creating topic via API
        String url = "http://localhost:" + port + "/api/training/day01/create-topic" +
                "?name=" + topicName + "&partitions=3&replicationFactor=1";

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });

        // Then: Should create topic successfully
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().get("status"));
        assertEquals(topicName, response.getBody().get("topic"));

        // Verify topic was actually created
        assertTrue(foundationService.topicExists(topicName), "Topic should exist after API creation");
    }

    @Test
    @DisplayName("Create topic API should reject empty topic name")
    void shouldRejectEmptyTopicName() {
        // When: Creating topic with empty name via API
        String url = "http://localhost:" + port + "/api/training/day01/create-topic" +
                "?name=&partitions=3&replicationFactor=1";

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });

        // Then: Should reject with bad request
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("error", response.getBody().get("status"));
    }

    @Test
    @DisplayName("Create topic API should reject invalid partition count")
    void shouldRejectInvalidPartitionCount() {
        // When: Creating topic with invalid partition count
        String topicName = "test-invalid-partitions-" + System.currentTimeMillis();
        String url = "http://localhost:" + port + "/api/training/day01/create-topic" +
                "?name=" + topicName + "&partitions=0&replicationFactor=1";

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });

        // Then: Should reject with bad request
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("error", response.getBody().get("status"));
    }

    @Test
    @DisplayName("Create topic API should reject invalid replication factor")
    void shouldRejectInvalidReplicationFactor() {
        // When: Creating topic with invalid replication factor
        String topicName = "test-invalid-replication-" + System.currentTimeMillis();
        String url = "http://localhost:" + port + "/api/training/day01/create-topic" +
                "?name=" + topicName + "&partitions=3&replicationFactor=0";

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });

        // Then: Should reject with bad request
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("error", response.getBody().get("status"));
    }

    @Test
    @DisplayName("Modules API should include Day01Foundation")
    void shouldIncludeDay01FoundationInModulesAPI() {
        // When: Getting all modules
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/training/modules",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });

        // Then: Should include Day01Foundation
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> modules = response.getBody();
        assertTrue(modules.containsKey("Day01Foundation"), "Should contain Day01Foundation module");

        Map<?, ?> day01Module = (Map<?, ?>) modules.get("Day01Foundation");
        assertEquals("Kafka Fundamentals", day01Module.get("name"));
        assertTrue(day01Module.get("description").toString().contains("AdminClient"));
    }
}
