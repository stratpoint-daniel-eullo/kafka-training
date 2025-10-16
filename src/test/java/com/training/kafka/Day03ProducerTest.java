package com.training.kafka;

import com.training.kafka.services.Day03ProducerService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for Day 3 Producer Module
 *
 * Tests cover:
 * - Synchronous and asynchronous message sending
 * - Spring Kafka Template and raw Kafka Producer
 * - Batch message sending
 * - Message partitioning strategies
 * - Producer metrics
 * - REST API endpoints
 *
 * @author Kafka Training Course
 * @version 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class Day03ProducerTest {

    @Container
    @SuppressWarnings("resource") // Testcontainers manages lifecycle
    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.7.0"))
            .withEmbeddedZookeeper();

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private Day03ProducerService producerService;

    /**
     * Configure Spring Boot properties dynamically from TestContainers
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("training.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("training.kafka.client-id", () -> "producer-test-client");
    }

    /**
     * Helper method to create a consumer for testing
     */
    private KafkaConsumer<String, String> createTestConsumer(String groupId) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");

        return new KafkaConsumer<>(props);
    }

    /**
     * Helper method to consume messages and verify they were received
     */
    private List<ConsumerRecord<String, String>> consumeMessages(String topic, int expectedCount, int timeoutSeconds) {
        KafkaConsumer<String, String> consumer = createTestConsumer("test-consumer-" + System.currentTimeMillis());
        consumer.subscribe(Collections.singletonList(topic));

        List<ConsumerRecord<String, String>> receivedRecords = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        long timeout = timeoutSeconds * 1000L;

        try {
            while (receivedRecords.size() < expectedCount &&
                   (System.currentTimeMillis() - startTime) < timeout) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                records.forEach(receivedRecords::add);
            }
        } finally {
            consumer.close();
        }

        return receivedRecords;
    }

    // ===== Service Tests =====

    @Test
    @DisplayName("Day03ProducerService should be initialized")
    void shouldInitializeProducerService() {
        assertNotNull(producerService, "Producer service should be initialized");
    }

    @Test
    @DisplayName("Should send message synchronously using Spring Kafka Template")
    void shouldSendMessageSyncSpring() {
        // Given: A topic and message
        String topic = "test-sync-spring-" + System.currentTimeMillis();
        String key = "test-key";
        String message = "Test message via Spring sync";

        // When: Sending message synchronously
        boolean sent = producerService.sendMessageSyncSpring(topic, key, message);

        // Then: Message should be sent successfully
        assertTrue(sent, "Message should be sent successfully");

        // Verify message was received
        List<ConsumerRecord<String, String>> records = consumeMessages(topic, 1, 5);
        assertEquals(1, records.size(), "Should receive exactly 1 message");
        assertEquals(key, records.get(0).key(), "Message key should match");
        assertEquals(message, records.get(0).value(), "Message value should match");
    }

    @Test
    @DisplayName("Should send message asynchronously using Spring Kafka Template")
    void shouldSendMessageAsyncSpring() throws InterruptedException {
        // Given: A topic and message
        String topic = "test-async-spring-" + System.currentTimeMillis();
        String key = "async-key";
        String message = "Test message via Spring async";

        // When: Sending message asynchronously
        producerService.sendMessageAsyncSpring(topic, key, message);

        // Wait a bit for async operation to complete
        Thread.sleep(1000);

        // Then: Message should be received
        List<ConsumerRecord<String, String>> records = consumeMessages(topic, 1, 5);
        assertEquals(1, records.size(), "Should receive exactly 1 message");
        assertEquals(key, records.get(0).key(), "Message key should match");
        assertEquals(message, records.get(0).value(), "Message value should match");
    }

    @Test
    @DisplayName("Should send message synchronously using raw Kafka Producer")
    void shouldSendMessageSyncRaw() {
        // Given: A topic and message
        String topic = "test-sync-raw-" + System.currentTimeMillis();
        String key = "raw-key";
        String message = "Test message via raw producer sync";

        // When: Sending message synchronously with raw producer
        boolean sent = producerService.sendMessageSyncRaw(topic, key, message);

        // Then: Message should be sent successfully
        assertTrue(sent, "Message should be sent successfully");

        // Verify message was received
        List<ConsumerRecord<String, String>> records = consumeMessages(topic, 1, 5);
        assertEquals(1, records.size(), "Should receive exactly 1 message");
        assertEquals(key, records.get(0).key(), "Message key should match");
        assertEquals(message, records.get(0).value(), "Message value should match");
    }

    @Test
    @DisplayName("Should send message asynchronously using raw Kafka Producer")
    void shouldSendMessageAsyncRaw() throws InterruptedException {
        // Given: A topic and message
        String topic = "test-async-raw-" + System.currentTimeMillis();
        String key = "raw-async-key";
        String message = "Test message via raw producer async";

        // When: Sending message asynchronously with raw producer
        producerService.sendMessageAsyncRaw(topic, key, message);

        // Wait a bit for async operation to complete
        Thread.sleep(1000);

        // Then: Message should be received
        List<ConsumerRecord<String, String>> records = consumeMessages(topic, 1, 5);
        assertEquals(1, records.size(), "Should receive exactly 1 message");
        assertEquals(key, records.get(0).key(), "Message key should match");
        assertEquals(message, records.get(0).value(), "Message value should match");
    }

    @Test
    @DisplayName("Should send batch messages using Spring Kafka Template")
    void shouldSendBatchMessagesSpring() throws InterruptedException {
        // Given: A topic and batch size
        String topic = "test-batch-spring-" + System.currentTimeMillis();
        int batchSize = 10;

        // When: Sending batch messages
        producerService.sendBatchMessagesSpring(topic, batchSize);

        // Wait a bit for async operations to complete
        Thread.sleep(2000);

        // Then: All messages should be received
        List<ConsumerRecord<String, String>> records = consumeMessages(topic, batchSize, 10);
        assertEquals(batchSize, records.size(), "Should receive all batch messages");

        // Verify messages contain expected data
        records.forEach(record -> {
            assertNotNull(record.key(), "Each record should have a key");
            assertTrue(record.key().startsWith("user-"), "Keys should start with 'user-'");
            assertTrue(record.value().contains("login"), "Messages should contain 'login' action");
        });
    }

    @Test
    @DisplayName("Should send batch messages using raw Kafka Producer")
    void shouldSendBatchMessagesRaw() throws InterruptedException {
        // Given: A topic and batch size
        String topic = "test-batch-raw-" + System.currentTimeMillis();
        int batchSize = 10;

        // When: Sending batch messages
        producerService.sendBatchMessagesRaw(topic, batchSize);

        // Wait a bit for async operations to complete
        Thread.sleep(2000);

        // Then: All messages should be received
        List<ConsumerRecord<String, String>> records = consumeMessages(topic, batchSize, 10);
        assertEquals(batchSize, records.size(), "Should receive all batch messages");

        // Verify messages contain expected data
        records.forEach(record -> {
            assertNotNull(record.key(), "Each record should have a key");
            assertTrue(record.key().startsWith("user-"), "Keys should start with 'user-'");
            assertTrue(record.value().contains("purchase"), "Messages should contain 'purchase' action");
        });
    }

    @Test
    @DisplayName("Should demonstrate producer patterns without errors")
    void shouldDemonstrateProducerPatternsWithoutErrors() {
        // Given: A test topic
        String topic = "test-patterns-" + System.currentTimeMillis();

        // When: Running producer patterns demonstration
        assertDoesNotThrow(() -> producerService.demonstrateProducerPatterns(topic),
                "Producer patterns demonstration should run without throwing exceptions");
    }

    @Test
    @DisplayName("Should send user event")
    void shouldSendUserEvent() throws InterruptedException {
        // Given: User event data
        String topic = "test-user-event-" + System.currentTimeMillis();
        String userId = "user-123";
        String action = "login";
        String details = "Login from mobile app";

        // When: Sending user event
        producerService.sendUserEvent(topic, userId, action, details);

        // Wait a bit for async operation to complete
        Thread.sleep(1000);

        // Then: User event should be received
        List<ConsumerRecord<String, String>> records = consumeMessages(topic, 1, 5);
        assertEquals(1, records.size(), "Should receive user event");
        assertEquals(userId, records.get(0).key(), "User ID should match key");
        assertTrue(records.get(0).value().contains(userId), "Message should contain user ID");
        assertTrue(records.get(0).value().contains(action), "Message should contain action");
        assertTrue(records.get(0).value().contains(details), "Message should contain details");
    }

    @Test
    @DisplayName("Should demonstrate partitioning strategies")
    void shouldDemonstratePartitioning() throws InterruptedException {
        // Given: A topic with multiple partitions
        String topic = "test-partitioning-" + System.currentTimeMillis();

        // When: Demonstrating partitioning
        producerService.demonstratePartitioning(topic);

        // Wait for messages to be sent
        Thread.sleep(2000);

        // Then: All messages should be received
        List<ConsumerRecord<String, String>> records = consumeMessages(topic, 9, 10);
        assertEquals(9, records.size(), "Should receive all partitioning test messages");

        // Verify messages with same key go to same partition
        Map<String, Set<Integer>> keyToPartitions = new HashMap<>();
        records.forEach(record -> {
            String key = record.key() == null ? "null" : record.key();
            keyToPartitions.computeIfAbsent(key, k -> new HashSet<>()).add(record.partition());
        });

        // Messages with "same-key" should all go to the same partition
        if (keyToPartitions.containsKey("same-key")) {
            assertEquals(1, keyToPartitions.get("same-key").size(),
                "Messages with same key should go to the same partition");
        }
    }

    @Test
    @DisplayName("Should show producer metrics without errors")
    void shouldShowProducerMetricsWithoutErrors() {
        // When: Showing producer metrics
        assertDoesNotThrow(() -> producerService.showProducerMetrics(),
                "Showing producer metrics should not throw exceptions");
    }

    // ===== REST API Tests =====

    @Test
    @DisplayName("Day03 demo API endpoint should work")
    void shouldWorkWithDay03DemoAPI() {
        // Given: A test topic
        String topic = "test-api-demo-" + System.currentTimeMillis();

        // When: Running Day 3 demo via API
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/training/day03/demo?topic=" + topic,
                HttpMethod.POST,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });

        // Then: Should complete successfully
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().get("status"));
        assertEquals("Day03Producers", response.getBody().get("module"));
    }

    @Test
    @DisplayName("Send message API endpoint should work")
    void shouldWorkWithSendMessageAPI() {
        // Given: Message data
        String topic = "test-api-send-" + System.currentTimeMillis();
        String key = "api-key";
        String message = "Test message from API";

        // When: Sending message via API
        String url = "http://localhost:" + port + "/api/training/day03/send-message" +
                "?topic=" + topic + "&key=" + key + "&message=" + message;

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });

        // Then: Should send successfully
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().get("status"));
        assertEquals(topic, response.getBody().get("topic"));
        assertEquals(key, response.getBody().get("key"));
    }

    @Test
    @DisplayName("Send message API should reject empty key")
    void shouldRejectEmptyKey() {
        // Given: Empty key
        String topic = "test-api-empty-key-" + System.currentTimeMillis();
        String message = "Test message";

        // When: Sending message with empty key via API
        String url = "http://localhost:" + port + "/api/training/day03/send-message" +
                "?topic=" + topic + "&key=&message=" + message;

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
    @DisplayName("Send batch API endpoint should work")
    void shouldWorkWithSendBatchAPI() {
        // Given: Batch parameters
        String topic = "test-api-batch-" + System.currentTimeMillis();
        int count = 5;

        // When: Sending batch via API
        String url = "http://localhost:" + port + "/api/training/day03/send-batch" +
                "?topic=" + topic + "&count=" + count;

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });

        // Then: Should send successfully
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().get("status"));
        assertEquals(String.valueOf(count), response.getBody().get("count"));
    }

    @Test
    @DisplayName("Send batch API should reject invalid count")
    void shouldRejectInvalidBatchCount() {
        // Given: Invalid count (0)
        String topic = "test-api-invalid-count-" + System.currentTimeMillis();

        // When: Sending batch with invalid count via API
        String url = "http://localhost:" + port + "/api/training/day03/send-batch" +
                "?topic=" + topic + "&count=0";

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
    @DisplayName("Send batch API should reject excessive count")
    void shouldRejectExcessiveBatchCount() {
        // Given: Excessive count (over 10000)
        String topic = "test-api-excessive-count-" + System.currentTimeMillis();

        // When: Sending batch with excessive count via API
        String url = "http://localhost:" + port + "/api/training/day03/send-batch" +
                "?topic=" + topic + "&count=20000";

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
    @DisplayName("Modules API should include Day03Producers")
    void shouldIncludeDay03ProducersInModulesAPI() {
        // When: Getting all modules
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/training/modules",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });

        // Then: Should include Day03Producers
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> modules = response.getBody();
        assertTrue(modules.containsKey("Day03Producers"), "Should contain Day03Producers module");

        Map<?, ?> day03Module = (Map<?, ?>) modules.get("Day03Producers");
        assertEquals("Message Producers", day03Module.get("name"));
        assertTrue(day03Module.get("description").toString().contains("Publishing messages"));
    }
}
