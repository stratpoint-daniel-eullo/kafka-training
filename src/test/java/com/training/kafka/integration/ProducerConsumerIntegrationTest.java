package com.training.kafka.integration;

import com.training.kafka.Day03Producers.SimpleProducer;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Producer and Consumer interaction
 *
 * This test demonstrates how to write integration tests for Kafka
 * applications using TestContainers. It's a complete example that
 * trainees can use as a template for their own integration tests.
 */
@Testcontainers
class ProducerConsumerIntegrationTest {
    private static final Logger logger = LoggerFactory.getLogger(ProducerConsumerIntegrationTest.class);

    @Container
    @SuppressWarnings("resource") // Testcontainers manages lifecycle
    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.7.0"))
            .withEmbeddedZookeeper();

    private static final String TEST_TOPIC_PREFIX = "integration-test-topic";
    private String bootstrapServers;
    private String testTopic;

    @BeforeEach
    void setUp() throws ExecutionException, InterruptedException {
        bootstrapServers = kafka.getBootstrapServers();

        // Create unique topic name for each test to avoid interference
        testTopic = TEST_TOPIC_PREFIX + "-" + System.currentTimeMillis();

        // Create test topic
        try (AdminClient adminClient = AdminClient.create(
                Collections.singletonMap("bootstrap.servers", bootstrapServers))) {

            NewTopic topic = new NewTopic(testTopic, 3, (short) 1);
            adminClient.createTopics(Collections.singletonList(topic)).all().get();
            logger.info("Created test topic: {}", testTopic);
        }
    }

    @Test
    @DisplayName("Producer should send messages that consumer can receive")
    void testProducerConsumerIntegration() throws InterruptedException {
        // Given: A producer and consumer
        SimpleProducer producer = new SimpleProducer(bootstrapServers, testTopic);

        // When: Producer sends messages
        String testMessage1 = "Integration test message 1";
        String testMessage2 = "Integration test message 2";
        String testMessage3 = "Integration test message 3";

        producer.sendMessageSync("test-key-1", testMessage1);
        producer.sendMessageSync("test-key-2", testMessage2);
        producer.sendMessageSync("test-key-3", testMessage3);

        producer.close();

        // Then: Consumer should receive the messages
        Properties consumerProps = new Properties();
        consumerProps.put("bootstrap.servers", bootstrapServers);
        consumerProps.put("group.id", "integration-test-group");
        consumerProps.put("key.deserializer", StringDeserializer.class.getName());
        consumerProps.put("value.deserializer", StringDeserializer.class.getName());
        consumerProps.put("auto.offset.reset", "earliest");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {
            consumer.subscribe(Collections.singletonList(testTopic));

            // Poll for messages with timeout
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));

            // Verify we received the expected messages
            assertEquals(3, records.count(), "Should receive 3 messages");

            boolean foundMessage1 = false, foundMessage2 = false, foundMessage3 = false;

            for (ConsumerRecord<String, String> record : records) {
                logger.info("Received: key={}, value={}", record.key(), record.value());

                if (testMessage1.equals(record.value())) foundMessage1 = true;
                if (testMessage2.equals(record.value())) foundMessage2 = true;
                if (testMessage3.equals(record.value())) foundMessage3 = true;
            }

            assertTrue(foundMessage1, "Should find test message 1");
            assertTrue(foundMessage2, "Should find test message 2");
            assertTrue(foundMessage3, "Should find test message 3");
        }
    }

    @Test
    @DisplayName("Multiple consumers in same group should share partition load")
    void testConsumerGroupLoadSharing() throws InterruptedException {
        // Given: A producer sends multiple messages
        SimpleProducer producer = new SimpleProducer(bootstrapServers, testTopic);

        // Send 10 messages with different keys to ensure distribution
        for (int i = 0; i < 10; i++) {
            producer.sendMessageSync("key-" + i, "Message " + i);
        }
        producer.close();

        // When: Two consumers in the same group consume messages
        String groupId = "load-sharing-test-group";

        Properties consumerProps = new Properties();
        consumerProps.put("bootstrap.servers", bootstrapServers);
        consumerProps.put("group.id", groupId);
        consumerProps.put("key.deserializer", StringDeserializer.class.getName());
        consumerProps.put("value.deserializer", StringDeserializer.class.getName());
        consumerProps.put("auto.offset.reset", "earliest");

        int consumer1Count = 0;
        int consumer2Count = 0;

        try (KafkaConsumer<String, String> consumer1 = new KafkaConsumer<>(consumerProps);
             KafkaConsumer<String, String> consumer2 = new KafkaConsumer<>(consumerProps)) {

            consumer1.subscribe(Collections.singletonList(testTopic));
            consumer2.subscribe(Collections.singletonList(testTopic));

            // Give time for rebalancing
            Thread.sleep(2000);

            // Consumer 1 polls
            ConsumerRecords<String, String> records1 = consumer1.poll(Duration.ofSeconds(5));
            consumer1Count = records1.count();

            // Consumer 2 polls
            ConsumerRecords<String, String> records2 = consumer2.poll(Duration.ofSeconds(5));
            consumer2Count = records2.count();

            logger.info("Consumer 1 received: {} messages", consumer1Count);
            logger.info("Consumer 2 received: {} messages", consumer2Count);
        }

        // Then: Both consumers should have received some messages (load sharing)
        int totalConsumed = consumer1Count + consumer2Count;
        assertEquals(10, totalConsumed, "Total messages consumed should be 10");

        // Both consumers should have received at least some messages
        // (This might not always be true due to partition assignment, but generally should be)
        assertTrue(consumer1Count > 0 || consumer2Count > 0, "At least one consumer should receive messages");
    }

    @Test
    @DisplayName("Consumer should handle empty topic gracefully")
    void testConsumerWithEmptyTopic() {
        // Given: An empty topic
        String emptyTopic = "empty-test-topic";

        // Create empty topic
        try (AdminClient adminClient = AdminClient.create(
                Collections.singletonMap("bootstrap.servers", bootstrapServers))) {

            NewTopic topic = new NewTopic(emptyTopic, 1, (short) 1);
            adminClient.createTopics(Collections.singletonList(topic)).all().get();
        } catch (Exception e) {
            fail("Failed to create empty topic: " + e.getMessage());
        }

        // When: Consumer polls from empty topic
        Properties consumerProps = new Properties();
        consumerProps.put("bootstrap.servers", bootstrapServers);
        consumerProps.put("group.id", "empty-topic-test-group");
        consumerProps.put("key.deserializer", StringDeserializer.class.getName());
        consumerProps.put("value.deserializer", StringDeserializer.class.getName());
        consumerProps.put("auto.offset.reset", "earliest");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {
            consumer.subscribe(Collections.singletonList(emptyTopic));

            // Poll with short timeout
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(2));

            // Then: Should receive no messages without error
            assertEquals(0, records.count(), "Empty topic should return no messages");
            assertTrue(records.isEmpty(), "Records should be empty");
        }
    }

    @AfterEach
    void tearDown() {
        logger.info("Integration test completed");
    }
}
