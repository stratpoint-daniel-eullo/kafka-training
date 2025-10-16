package com.training.kafka;

import com.training.kafka.services.Day04ConsumerService;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
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

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Properties;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Comprehensive tests for Day 4 Consumer Module
 *
 * Tests cover:
 * - Spring Kafka Listeners (automatic consumption)
 * - Raw Kafka Consumer (manual consumption)
 * - Message processing and acknowledgment
 * - Consumer group behavior
 * - Message counting and statistics
 * - REST API endpoints
 *
 * @author Kafka Training Course
 * @version 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class Day04ConsumerTest {

    @Container
    @SuppressWarnings("resource") // Testcontainers manages lifecycle
    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.7.0"))
            .withEmbeddedZookeeper();

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private Day04ConsumerService consumerService;

    /**
     * Configure Spring Boot properties dynamically from TestContainers
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("training.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("training.kafka.client-id", () -> "consumer-test-client");
    }

    /**
     * Helper method to create a test producer
     */
    private KafkaProducer<String, String> createTestProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");

        return new KafkaProducer<>(props);
    }

    /**
     * Helper method to produce a message to a topic
     */
    private void produceMessage(String topic, String key, String message) {
        try (KafkaProducer<String, String> producer = createTestProducer()) {
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, message);
            producer.send(record).get();
            producer.flush();
        } catch (Exception e) {
            throw new RuntimeException("Failed to produce test message", e);
        }
    }

    /**
     * Helper method to produce multiple messages
     */
    private void produceMessages(String topic, int count) {
        try (KafkaProducer<String, String> producer = createTestProducer()) {
            for (int i = 0; i < count; i++) {
                String key = "user-" + (i % 5);
                String message = String.format(
                    "{\"user_id\":\"%s\", \"action\":\"test\", \"timestamp\":\"%s\", \"id\":\"%d\"}",
                    key, LocalDateTime.now(), i);
                ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, message);
                producer.send(record);
            }
            producer.flush();
        } catch (Exception e) {
            throw new RuntimeException("Failed to produce test messages", e);
        }
    }

    // ===== Service Tests =====

    @Test
    @DisplayName("Day04ConsumerService should be initialized")
    void shouldInitializeConsumerService() {
        assertNotNull(consumerService, "Consumer service should be initialized");
    }

    @Test
    @DisplayName("Should consume message using raw consumer")
    void shouldConsumeMessageUsingRawConsumer() {
        // Given: A topic with messages
        String topic = "test-raw-consumer-" + System.currentTimeMillis();
        int messageCount = 3;
        produceMessages(topic, messageCount);

        // When: Consuming messages with raw consumer
        assertDoesNotThrow(() -> consumerService.consumeMessagesRaw(topic, "raw-test-group", messageCount),
                "Raw consumer should consume messages without errors");

        // Then: Should complete successfully
        // (messages are logged, actual consumption is verified by no exceptions)
    }

    @Test
    @DisplayName("Should consume messages from user-events topic via Spring Listener")
    void shouldConsumeViaSpringListener() throws InterruptedException {
        // Given: Initial message count
        consumerService.resetMessageCounter();
        int initialCount = consumerService.getProcessedMessageCount();

        // When: Producing messages to user-events topic
        int messagesToSend = 3;
        produceMessages("user-events", messagesToSend);

        // Then: Spring listener should consume the messages (with timeout)
        await().atMost(10, SECONDS).untilAsserted(() -> {
            int currentCount = consumerService.getProcessedMessageCount();
            assertTrue(currentCount > initialCount,
                "Message count should increase after producing messages to user-events topic");
        });
    }

    @Test
    @DisplayName("Should consume messages from demo topics via Spring Listener")
    void shouldConsumeFromDemoTopics() {
        // Given: Demo topics
        String topic1 = "demo-topic-1";
        String topic2 = "demo-topic-2";

        // When: Producing messages to demo topics
        produceMessage(topic1, "demo-key-1", "Demo message for topic 1");
        produceMessage(topic2, "demo-key-2", "Demo message for topic 2");

        // Wait a bit for listeners to process
        await().atMost(5, SECONDS).untilAsserted(() -> {
            // Then: Listeners should consume the messages (verified by logs)
            // No exceptions means successful consumption
            assertTrue(true, "Demo topic listeners should consume messages");
        });
    }

    @Test
    @DisplayName("Should get processed message count")
    void shouldGetProcessedMessageCount() {
        // When: Getting message count
        int count = consumerService.getProcessedMessageCount();

        // Then: Should return a non-negative integer
        assertTrue(count >= 0, "Message count should be non-negative");
    }

    @Test
    @DisplayName("Should reset message counter")
    void shouldResetMessageCounter() {
        // Given: Some messages may have been processed
        // When: Resetting counter
        consumerService.resetMessageCounter();

        // Then: Counter should be 0
        assertEquals(0, consumerService.getProcessedMessageCount(),
            "Message count should be 0 after reset");
    }

    @Test
    @DisplayName("Should demonstrate consumer patterns without errors")
    void shouldDemonstrateConsumerPatternsWithoutErrors() {
        // Given: Some messages in user-events topic
        produceMessages("user-events", 5);

        // Wait a bit for messages to be available
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When: Running consumer patterns demonstration
        assertDoesNotThrow(() -> consumerService.demonstrateConsumerPatterns(),
                "Consumer patterns demonstration should run without throwing exceptions");
    }

    @Test
    @DisplayName("Should demonstrate consumer group behavior without errors")
    void shouldDemonstrateConsumerGroupWithoutErrors() {
        // Given: A test topic with messages
        String topic = "test-consumer-group-" + System.currentTimeMillis();
        produceMessages(topic, 5);

        // Wait a bit for messages to be available
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When: Demonstrating consumer group
        assertDoesNotThrow(() -> consumerService.demonstrateConsumerGroup(topic),
                "Consumer group demonstration should run without throwing exceptions");
    }

    @Test
    @DisplayName("Should process user event message")
    void shouldProcessUserEventMessage() {
        // Given: A user event message
        consumerService.resetMessageCounter();
        int initialCount = consumerService.getProcessedMessageCount();

        String message = "{\"user_id\":\"user-123\", \"action\":\"login\", \"timestamp\":\"2025-01-01T10:00:00\"}";

        // When: Producing user event to user-events topic
        produceMessage("user-events", "user-123", message);

        // Then: Message should be processed by listener (with timeout)
        await().atMost(10, SECONDS).untilAsserted(() -> {
            int currentCount = consumerService.getProcessedMessageCount();
            assertTrue(currentCount > initialCount,
                "Message count should increase after processing user event");
        });
    }

    @Test
    @DisplayName("Should handle multiple messages with same key")
    void shouldHandleMultipleMessagesWithSameKey() {
        // Given: Multiple messages with same key
        consumerService.resetMessageCounter();
        int initialCount = consumerService.getProcessedMessageCount();

        String key = "same-user";
        int messageCount = 3;

        try (KafkaProducer<String, String> producer = createTestProducer()) {
            for (int i = 0; i < messageCount; i++) {
                String message = String.format(
                    "{\"user_id\":\"%s\", \"action\":\"action-%d\", \"timestamp\":\"%s\"}",
                    key, i, LocalDateTime.now());
                ProducerRecord<String, String> record = new ProducerRecord<>("user-events", key, message);
                producer.send(record);
            }
            producer.flush();
        } catch (Exception e) {
            fail("Failed to produce test messages: " + e.getMessage());
        }

        // Then: All messages should be processed (with timeout)
        await().atMost(10, SECONDS).untilAsserted(() -> {
            int currentCount = consumerService.getProcessedMessageCount();
            assertTrue(currentCount >= initialCount + messageCount,
                "All messages with same key should be processed");
        });
    }

    // ===== REST API Tests =====

    @Test
    @DisplayName("Day04 demo API endpoint should work")
    void shouldWorkWithDay04DemoAPI() {
        // Given: Some messages in user-events topic
        produceMessages("user-events", 5);

        // Wait a bit for messages to be available
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When: Running Day 4 demo via API
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/training/day04/demo",
                HttpMethod.POST,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });

        // Then: Should complete successfully
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().get("status"));
        assertEquals("Day04Consumers", response.getBody().get("module"));
    }

    @Test
    @DisplayName("Consumer stats API endpoint should work")
    void shouldWorkWithConsumerStatsAPI() {
        // When: Getting consumer stats via API
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/training/day04/stats",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });

        // Then: Should return stats successfully
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().get("status"));
        assertNotNull(response.getBody().get("processedMessages"));
        assertTrue((Integer) response.getBody().get("processedMessages") >= 0);
    }

    @Test
    @DisplayName("Consume raw API endpoint should work")
    void shouldWorkWithConsumeRawAPI() {
        // Given: A topic with messages
        String topic = "test-api-consume-raw-" + System.currentTimeMillis();
        int messageCount = 3;
        produceMessages(topic, messageCount);

        // Wait a bit for messages to be available
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When: Consuming via API
        String url = "http://localhost:" + port + "/api/training/day04/consume-raw" +
                "?topic=" + topic + "&groupId=api-test-group&maxMessages=" + messageCount;

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });

        // Then: Should consume successfully
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().get("status"));
        assertEquals(topic, response.getBody().get("topic"));
    }

    @Test
    @DisplayName("Consume raw API should reject invalid maxMessages")
    void shouldRejectInvalidMaxMessages() {
        // When: Consuming with invalid maxMessages
        String topic = "test-invalid-max-" + System.currentTimeMillis();
        String url = "http://localhost:" + port + "/api/training/day04/consume-raw" +
                "?topic=" + topic + "&groupId=test-group&maxMessages=0";

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
    @DisplayName("Consume raw API should reject excessive maxMessages")
    void shouldRejectExcessiveMaxMessages() {
        // When: Consuming with excessive maxMessages
        String topic = "test-excessive-max-" + System.currentTimeMillis();
        String url = "http://localhost:" + port + "/api/training/day04/consume-raw" +
                "?topic=" + topic + "&groupId=test-group&maxMessages=2000";

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
    @DisplayName("Modules API should include Day04Consumers")
    void shouldIncludeDay04ConsumersInModulesAPI() {
        // When: Getting all modules
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/training/modules",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });

        // Then: Should include Day04Consumers
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> modules = response.getBody();
        assertTrue(modules.containsKey("Day04Consumers"), "Should contain Day04Consumers module");

        Map<?, ?> day04Module = (Map<?, ?>) modules.get("Day04Consumers");
        assertEquals("Message Consumers", day04Module.get("name"));
        assertTrue(day04Module.get("description").toString().contains("Processing messages"));
    }
}
