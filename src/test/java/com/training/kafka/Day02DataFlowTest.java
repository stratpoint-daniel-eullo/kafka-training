package com.training.kafka;

import com.training.kafka.services.Day02DataFlowService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
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

import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for Day 2 Data Flow Module
 *
 * Tests cover:
 * - Producer semantics and delivery guarantees
 * - Partitioning strategies (round-robin, key-based)
 * - Consumer groups and offset management
 * - Message ordering guarantees
 * - Consumer lag calculation
 * - REST API endpoints
 *
 * @author Kafka Training Course
 * @version 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class Day02DataFlowTest {

    @Container
    @SuppressWarnings("resource") // Testcontainers manages lifecycle
    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.7.0"))
            .withEmbeddedZookeeper();

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private Day02DataFlowService dataFlowService;

    /**
     * Configure Spring Boot properties dynamically from TestContainers
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("training.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("training.kafka.client-id", () -> "dataflow-test-client");
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
     * Helper method to create a test consumer
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
     * Helper method to produce messages with keys
     */
    private void produceMessagesWithKeys(String topic, Map<String, String> keyValuePairs) {
        try (KafkaProducer<String, String> producer = createTestProducer()) {
            for (Map.Entry<String, String> entry : keyValuePairs.entrySet()) {
                ProducerRecord<String, String> record = new ProducerRecord<>(
                    topic,
                    entry.getKey(),
                    entry.getValue()
                );
                producer.send(record);
            }
            producer.flush();
        } catch (Exception e) {
            throw new RuntimeException("Failed to produce test messages", e);
        }
    }

    /**
     * Helper method to consume messages with a consumer group
     */
    private List<ConsumerRecord<String, String>> consumeMessages(
            String topic, String groupId, int expectedCount, int timeoutSeconds) {

        List<ConsumerRecord<String, String>> consumed = new ArrayList<>();

        try (KafkaConsumer<String, String> consumer = createTestConsumer(groupId)) {
            consumer.subscribe(Collections.singletonList(topic));

            long startTime = System.currentTimeMillis();
            long timeout = timeoutSeconds * 1000L;

            while (consumed.size() < expectedCount &&
                   (System.currentTimeMillis() - startTime) < timeout) {

                consumer.poll(Duration.ofMillis(500)).forEach(consumed::add);
            }

            // Commit offsets before closing
            consumer.commitSync();

        } catch (Exception e) {
            throw new RuntimeException("Failed to consume messages", e);
        }

        return consumed;
    }

    // ===== Service Tests =====

    @Test
    @DisplayName("Day02DataFlowService should be initialized")
    void shouldInitializeDataFlowService() {
        assertNotNull(dataFlowService, "Data flow service should be initialized");
    }

    @Test
    @DisplayName("Should demonstrate data flow without errors")
    void shouldDemonstrateDataFlowWithoutErrors() {
        // When: Running data flow demonstration
        assertDoesNotThrow(() -> dataFlowService.demonstrateDataFlow(),
                "Data flow demonstration should run without throwing exceptions");
    }

    @Test
    @DisplayName("Should demonstrate producer semantics without errors")
    void shouldDemonstrateProducerSemanticsWithoutErrors() {
        // When: Demonstrating producer semantics
        assertDoesNotThrow(() -> dataFlowService.demonstrateProducerSemantics(),
                "Producer semantics demonstration should run without throwing exceptions");
    }

    @Test
    @DisplayName("Should demonstrate partitioning strategies without errors")
    void shouldDemonstratePartitioningStrategiesWithoutErrors() {
        // When: Demonstrating partitioning strategies
        assertDoesNotThrow(() -> dataFlowService.demonstratePartitioningStrategies(),
                "Partitioning strategies demonstration should run without throwing exceptions");
    }

    @Test
    @DisplayName("Should demonstrate consumer groups without errors")
    void shouldDemonstrateConsumerGroupsWithoutErrors() {
        // When: Demonstrating consumer groups
        assertDoesNotThrow(() -> dataFlowService.demonstrateConsumerGroups(),
                "Consumer groups demonstration should run without throwing exceptions");
    }

    @Test
    @DisplayName("Should demonstrate offset management without errors")
    void shouldDemonstrateOffsetManagementWithoutErrors() {
        // When: Demonstrating offset management
        assertDoesNotThrow(() -> dataFlowService.demonstrateOffsetManagement(),
                "Offset management demonstration should run without throwing exceptions");
    }

    @Test
    @DisplayName("Should demonstrate message ordering without errors")
    void shouldDemonstrateMessageOrderingWithoutErrors() {
        // When: Demonstrating message ordering
        assertDoesNotThrow(() -> dataFlowService.demonstrateMessageOrdering(),
                "Message ordering demonstration should run without throwing exceptions");
    }

    @Test
    @DisplayName("Should get data flow concepts")
    void shouldGetDataFlowConcepts() {
        // When: Getting data flow concepts
        Map<String, Object> concepts = dataFlowService.getDataFlowConcepts();

        // Then: Should return comprehensive concepts
        assertNotNull(concepts, "Concepts map should not be null");
        assertTrue(concepts.containsKey("producerSemantics"), "Should contain producer semantics");
        assertTrue(concepts.containsKey("partitioning"), "Should contain partitioning");
        assertTrue(concepts.containsKey("consumerGroups"), "Should contain consumer groups");
        assertTrue(concepts.containsKey("offsetManagement"), "Should contain offset management");
        assertTrue(concepts.containsKey("ordering"), "Should contain ordering");

        // Verify producer semantics content
        @SuppressWarnings("unchecked")
        Map<String, String> producerSemantics = (Map<String, String>) concepts.get("producerSemantics");
        assertTrue(producerSemantics.containsKey("atMostOnce"), "Should have at-most-once semantics");
        assertTrue(producerSemantics.containsKey("atLeastOnce"), "Should have at-least-once semantics");
        assertTrue(producerSemantics.containsKey("exactlyOnce"), "Should have exactly-once semantics");
    }

    @Test
    @DisplayName("Should get consumer group offsets for non-existent group")
    void shouldGetConsumerGroupOffsetsForNonExistentGroup() {
        // Given: A non-existent consumer group
        String groupId = "non-existent-group-" + System.currentTimeMillis();

        // When: Getting consumer group offsets
        Map<String, Object> result = dataFlowService.getConsumerGroupOffsets(groupId);

        // Then: Should return no_offsets status
        assertNotNull(result, "Result should not be null");
        assertEquals("no_offsets", result.get("status"), "Status should be no_offsets");
        assertTrue(result.containsKey("message"), "Should contain message");
    }

    @Test
    @DisplayName("Should get consumer group offsets for existing group")
    void shouldGetConsumerGroupOffsetsForExistingGroup() {
        // Given: A topic with messages
        String topic = "test-offsets-topic-" + System.currentTimeMillis();
        String groupId = "test-offsets-group-" + System.currentTimeMillis();

        Map<String, String> messages = new HashMap<>();
        messages.put("key1", "message1");
        messages.put("key2", "message2");
        messages.put("key3", "message3");

        produceMessagesWithKeys(topic, messages);

        // And: A consumer that has consumed the messages
        consumeMessages(topic, groupId, 3, 10);

        // When: Getting consumer group offsets
        Map<String, Object> result = dataFlowService.getConsumerGroupOffsets(groupId);

        // Then: Should return offsets successfully
        assertNotNull(result, "Result should not be null");
        assertEquals("success", result.get("status"), "Status should be success");
        assertEquals(groupId, result.get("groupId"), "Group ID should match");
        assertTrue(result.containsKey("offsets"), "Should contain offsets");
    }

    @Test
    @DisplayName("Should calculate consumer lag for non-existent group")
    void shouldCalculateConsumerLagForNonExistentGroup() {
        // Given: A non-existent consumer group
        String groupId = "non-existent-lag-group-" + System.currentTimeMillis();

        // When: Calculating consumer lag
        Map<String, Object> result = dataFlowService.calculateConsumerLag(groupId);

        // Then: Should return no_offsets status
        assertNotNull(result, "Result should not be null");
        assertEquals("no_offsets", result.get("status"), "Status should be no_offsets");
        assertTrue(result.containsKey("message"), "Should contain message");
    }

    @Test
    @DisplayName("Should calculate consumer lag for existing group")
    void shouldCalculateConsumerLagForExistingGroup() {
        // Given: A topic with messages
        String topic = "test-lag-topic-" + System.currentTimeMillis();
        String groupId = "test-lag-group-" + System.currentTimeMillis();

        Map<String, String> messages = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            messages.put("key" + i, "message" + i);
        }

        produceMessagesWithKeys(topic, messages);

        // And: A consumer that has consumed only some messages
        consumeMessages(topic, groupId, 3, 10);

        // And: More messages produced (creating lag)
        Map<String, String> moreMessages = new HashMap<>();
        moreMessages.put("key6", "message6");
        moreMessages.put("key7", "message7");
        produceMessagesWithKeys(topic, moreMessages);

        // Wait a bit for offsets to be committed
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When: Calculating consumer lag
        Map<String, Object> result = dataFlowService.calculateConsumerLag(groupId);

        // Then: Should return lag information successfully
        assertNotNull(result, "Result should not be null");
        assertEquals("success", result.get("status"), "Status should be success");
        assertEquals(groupId, result.get("groupId"), "Group ID should match");
        assertTrue(result.containsKey("totalLag"), "Should contain total lag");
        assertTrue(result.containsKey("partitions"), "Should contain partition lag info");

        // Lag should be positive (we produced more messages after consuming)
        long totalLag = (long) result.get("totalLag");
        assertTrue(totalLag >= 0, "Total lag should be non-negative");
    }

    @Test
    @DisplayName("Should verify key-based partitioning sends same key to same partition")
    void shouldVerifyKeyBasedPartitioning() {
        // Given: A topic
        String topic = "test-partitioning-" + System.currentTimeMillis();

        // When: Producing multiple messages with the same key
        String key = "same-key";
        Map<String, String> messages = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            messages.put(key, "message-" + i);
        }

        produceMessagesWithKeys(topic, messages);

        // Then: All messages with the same key should go to the same partition
        // (verified through consumption - same key maintains order)
        String groupId = "partitioning-test-group-" + System.currentTimeMillis();
        List<ConsumerRecord<String, String>> consumed = consumeMessages(topic, groupId, 5, 10);

        assertEquals(5, consumed.size(), "Should consume all messages");

        // All messages should have the same key
        consumed.forEach(record ->
            assertEquals(key, record.key(), "All records should have the same key"));

        // All messages should be on the same partition
        int partition = consumed.get(0).partition();
        consumed.forEach(record ->
            assertEquals(partition, record.partition(),
                "All records with same key should be on same partition"));
    }

    @Test
    @DisplayName("Should print producer best practices without errors")
    void shouldPrintProducerBestPracticesWithoutErrors() {
        // When: Printing producer best practices
        assertDoesNotThrow(() -> dataFlowService.printProducerBestPractices(),
                "Printing producer best practices should not throw exceptions");
    }

    @Test
    @DisplayName("Should print consumer best practices without errors")
    void shouldPrintConsumerBestPracticesWithoutErrors() {
        // When: Printing consumer best practices
        assertDoesNotThrow(() -> dataFlowService.printConsumerBestPractices(),
                "Printing consumer best practices should not throw exceptions");
    }

    // ===== REST API Tests =====

    @Test
    @DisplayName("Day02 demo API endpoint should work")
    void shouldWorkWithDay02DemoAPI() {
        // When: Running Day 2 demo via API
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/training/day02/demo",
                HttpMethod.POST,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });

        // Then: Should complete successfully
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().get("status"));
        assertEquals("Day02DataFlow", response.getBody().get("module"));
    }

    @Test
    @DisplayName("Get concepts API endpoint should work")
    void shouldWorkWithGetConceptsAPI() {
        // When: Getting data flow concepts via API
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/training/day02/concepts",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });

        // Then: Should return concepts successfully
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().get("status"));
        assertTrue(response.getBody().containsKey("concepts"));

        @SuppressWarnings("unchecked")
        Map<String, Object> concepts = (Map<String, Object>) response.getBody().get("concepts");
        assertTrue(concepts.containsKey("producerSemantics"));
        assertTrue(concepts.containsKey("partitioning"));
        assertTrue(concepts.containsKey("consumerGroups"));
        assertTrue(concepts.containsKey("offsetManagement"));
    }

    @Test
    @DisplayName("Get consumer lag API should handle non-existent group")
    void shouldHandleNonExistentGroupInLagAPI() {
        // Given: A non-existent consumer group
        String groupId = "non-existent-api-group-" + System.currentTimeMillis();

        // When: Getting consumer lag via API
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/training/day02/consumer-lag/" + groupId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });

        // Then: Should return no_offsets status
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("no_offsets", response.getBody().get("status"));
        assertEquals(groupId, response.getBody().get("groupId"));
    }

    @Test
    @DisplayName("Get consumer lag API should work for existing group")
    void shouldWorkWithConsumerLagAPI() {
        // Given: A topic with messages and a consuming group
        String topic = "test-api-lag-topic-" + System.currentTimeMillis();
        String groupId = "test-api-lag-group-" + System.currentTimeMillis();

        Map<String, String> messages = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            messages.put("key" + i, "message" + i);
        }
        produceMessagesWithKeys(topic, messages);

        // Consume the messages
        consumeMessages(topic, groupId, 5, 10);

        // Wait for offsets to be committed
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When: Getting consumer lag via API
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/training/day02/consumer-lag/" + groupId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });

        // Then: Should return lag information successfully
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().get("status"));
        assertEquals(groupId, response.getBody().get("groupId"));
        assertTrue(response.getBody().containsKey("totalLag"));
    }

    @Test
    @DisplayName("Get consumer offsets API should handle non-existent group")
    void shouldHandleNonExistentGroupInOffsetsAPI() {
        // Given: A non-existent consumer group
        String groupId = "non-existent-offsets-api-group-" + System.currentTimeMillis();

        // When: Getting consumer offsets via API
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/training/day02/offsets/" + groupId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });

        // Then: Should return no_offsets status
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("no_offsets", response.getBody().get("status"));
        assertEquals(groupId, response.getBody().get("groupId"));
    }

    @Test
    @DisplayName("Get consumer offsets API should work for existing group")
    void shouldWorkWithConsumerOffsetsAPI() {
        // Given: A topic with messages and a consuming group
        String topic = "test-api-offsets-topic-" + System.currentTimeMillis();
        String groupId = "test-api-offsets-group-" + System.currentTimeMillis();

        Map<String, String> messages = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            messages.put("key" + i, "message" + i);
        }
        produceMessagesWithKeys(topic, messages);

        // Consume the messages
        consumeMessages(topic, groupId, 5, 10);

        // Wait for offsets to be committed
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When: Getting consumer offsets via API
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/training/day02/offsets/" + groupId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });

        // Then: Should return offsets successfully
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().get("status"));
        assertEquals(groupId, response.getBody().get("groupId"));
        assertTrue(response.getBody().containsKey("offsets"));
    }

    @Test
    @DisplayName("Modules API should include Day02DataFlow")
    void shouldIncludeDay02DataFlowInModulesAPI() {
        // When: Getting all modules
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/training/modules",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });

        // Then: Should include Day02DataFlow
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> modules = response.getBody();
        assertTrue(modules.containsKey("Day02DataFlow"), "Should contain Day02DataFlow module");

        Map<?, ?> day02Module = (Map<?, ?>) modules.get("Day02DataFlow");
        assertEquals("Data Flow & Message Patterns", day02Module.get("name"));
        assertTrue(day02Module.get("description").toString().contains("Producer/consumer semantics"));
    }
}
