package com.training.kafka.cli;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Day 4: Consumer CLI - Pure Kafka Consumer
 *
 * This is a PURE KAFKA example - NO SPRING BOOT, NO FRAMEWORKS
 * Perfect for data engineers learning Kafka fundamentals
 *
 * What you'll learn:
 * - How to create KafkaConsumer using raw Kafka API
 * - How to subscribe to topics
 * - How to poll and process messages
 * - Manual offset management
 * - Consumer configuration best practices
 *
 * Run: java -cp target/kafka-training-java-1.0.0.jar com.training.kafka.cli.Day04ConsumerCLI
 */
public class Day04ConsumerCLI {

    private static final String BOOTSTRAP_SERVERS =
        System.getenv().getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");
    private static final String TOPIC_NAME = "training-day01-cli";
    private static final String GROUP_ID = "training-consumer-group-cli";

    public static void main(String[] args) {
        System.out.println("╔═════════════════════════════════════════════════════════╗");
        System.out.println("║  Day 4: Kafka Consumer - Pure KafkaConsumer API        ║");
        System.out.println("║  No Spring Boot • No Frameworks • Pure Kafka           ║");
        System.out.println("╚═════════════════════════════════════════════════════════╝\n");

        // Step 1: Create Consumer configuration
        Properties props = new Properties();

        // Connection settings
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "consumer-cli");

        // Deserializers
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        // Consumer behavior
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // Start from beginning
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"); // Manual commit
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "100"); // Process 100 records at a time

        // Performance tuning
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, "1024"); // Wait for 1KB
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "500"); // Or wait 500ms

        System.out.println("✓ Consumer Configuration:");
        System.out.println("  Bootstrap Servers: " + BOOTSTRAP_SERVERS);
        System.out.println("  Group ID: " + GROUP_ID);
        System.out.println("  Topic: " + TOPIC_NAME);
        System.out.println("  Auto Offset Reset: earliest");
        System.out.println("  Auto Commit: false (manual commit)");
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("Consuming messages... (Press Ctrl+C to stop)");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        // Step 2: Create Consumer and subscribe to topic
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {

            // Subscribe to topic
            consumer.subscribe(Collections.singletonList(TOPIC_NAME));

            AtomicInteger messageCount = new AtomicInteger(0);
            AtomicInteger pollCount = new AtomicInteger(0);

            // Graceful shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n\n╔═════════════════════════════════════════════════════════╗");
                System.out.println("║           Consumer Shutting Down Gracefully             ║");
                System.out.println("╚═════════════════════════════════════════════════════════╝");
                System.out.println("  Total polls: " + pollCount.get());
                System.out.println("  Total messages consumed: " + messageCount.get());
                consumer.close();
            }));

            // Step 3: Poll and process messages
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                pollCount.incrementAndGet();

                if (!records.isEmpty()) {
                    System.out.println("📦 Poll #" + pollCount.get() + " - Received " + records.count() + " record(s)\n");

                    for (ConsumerRecord<String, String> record : records) {
                        messageCount.incrementAndGet();

                        System.out.println("  Message #" + messageCount.get() + ":");
                        System.out.println("    Topic: " + record.topic());
                        System.out.println("    Partition: " + record.partition());
                        System.out.println("    Offset: " + record.offset());
                        System.out.println("    Timestamp: " + record.timestamp());
                        System.out.println("    Key: " + record.key());
                        System.out.println("    Value: " + record.value());
                        System.out.println();

                        // Simulate processing
                        processMessage(record);
                    }

                    // Manual commit after processing batch
                    consumer.commitSync();
                    System.out.println("  ✓ Committed offsets for " + records.count() + " messages\n");
                    System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                }

                // Show heartbeat every 10 polls
                if (pollCount.get() % 10 == 0) {
                    System.out.println("💓 Consumer heartbeat (poll #" + pollCount.get() + ") - Waiting for messages...");
                }
            }

        } catch (Exception e) {
            System.err.println("\n❌ ERROR: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Process individual message
     * In real applications, this would contain your business logic
     */
    private static void processMessage(ConsumerRecord<String, String> record) {
        // Simulate processing time
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Your business logic here
        // For example: parse JSON, store in database, trigger workflows, etc.
    }
}
