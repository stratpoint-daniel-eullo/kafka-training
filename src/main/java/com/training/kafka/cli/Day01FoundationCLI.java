package com.training.kafka.cli;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.admin.DescribeTopicsResult;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Day 1: Foundation CLI - Pure Kafka AdminClient
 *
 * This is a PURE KAFKA example - NO SPRING BOOT, NO FRAMEWORKS
 * Perfect for data engineers learning Kafka fundamentals
 *
 * What you'll learn:
 * - How to create AdminClient using raw Kafka API
 * - How to create topics programmatically
 * - How to list and describe topics
 * - Configuration using Properties (platform-agnostic)
 *
 * Run: java -cp target/kafka-training-java-1.0.0.jar com.training.kafka.cli.Day01FoundationCLI
 */
public class Day01FoundationCLI {

    private static final String BOOTSTRAP_SERVERS =
        System.getenv().getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");

    public static void main(String[] args) {
        System.out.println("╔═════════════════════════════════════════════════════════╗");
        System.out.println("║  Day 1: Kafka Foundation - Pure AdminClient API        ║");
        System.out.println("║  No Spring Boot • No Frameworks • Pure Kafka           ║");
        System.out.println("╚═════════════════════════════════════════════════════════╝\n");

        // Step 1: Create AdminClient configuration
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(AdminClientConfig.CLIENT_ID_CONFIG, "foundation-cli");
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);

        System.out.println("✓ AdminClient Configuration:");
        System.out.println("  Bootstrap Servers: " + BOOTSTRAP_SERVERS);
        System.out.println("  Client ID: foundation-cli\n");

        // Step 2: Create AdminClient and run demonstrations
        try (AdminClient admin = AdminClient.create(props)) {

            // Demo 1: List existing topics
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("Demo 1: List All Topics");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            listAllTopics(admin);

            // Demo 2: Create a new topic
            System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("Demo 2: Create New Topic");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            createTopic(admin, "training-day01-cli", 3, (short) 1);

            // Demo 3: Describe the topic
            System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("Demo 3: Describe Topic Details");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            describeTopic(admin, "training-day01-cli");

            System.out.println("\n╔═════════════════════════════════════════════════════════╗");
            System.out.println("║              Day 1 Foundation Demo Complete            ║");
            System.out.println("╚═════════════════════════════════════════════════════════╝");
            System.out.println("\nKey Takeaways:");
            System.out.println("  ✓ Created AdminClient using raw Kafka API");
            System.out.println("  ✓ Listed existing topics");
            System.out.println("  ✓ Created new topic with partitions and replication");
            System.out.println("  ✓ Described topic configuration and metadata");
            System.out.println("\nNext Steps:");
            System.out.println("  → Day 3: Learn to produce messages to this topic");
            System.out.println("  → Day 4: Learn to consume messages from this topic");

        } catch (Exception e) {
            System.err.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * List all topics in the Kafka cluster
     */
    private static void listAllTopics(AdminClient admin) throws ExecutionException, InterruptedException {
        ListTopicsResult topics = admin.listTopics();
        Set<String> topicNames = topics.names().get();

        if (topicNames.isEmpty()) {
            System.out.println("  No topics found in cluster");
        } else {
            System.out.println("  Found " + topicNames.size() + " topic(s):");
            topicNames.forEach(name -> System.out.println("    • " + name));
        }
    }

    /**
     * Create a new topic with specified configuration
     */
    private static void createTopic(AdminClient admin, String topicName, int partitions, short replicationFactor) {
        try {
            NewTopic newTopic = new NewTopic(topicName, partitions, replicationFactor);

            System.out.println("  Creating topic: " + topicName);
            System.out.println("    Partitions: " + partitions);
            System.out.println("    Replication Factor: " + replicationFactor);

            CreateTopicsResult result = admin.createTopics(Collections.singleton(newTopic));
            result.all().get(); // Wait for creation to complete

            System.out.println("  ✓ Topic created successfully!");

        } catch (Exception e) {
            if (e.getMessage().contains("already exists")) {
                System.out.println("  ⚠ Topic already exists (using existing topic)");
            } else {
                System.err.println("  ❌ Failed to create topic: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Describe topic details including partitions and replicas
     */
    private static void describeTopic(AdminClient admin, String topicName) throws ExecutionException, InterruptedException {
        DescribeTopicsResult result = admin.describeTopics(Collections.singleton(topicName));
        Map<String, TopicDescription> descriptions = result.all().get();

        TopicDescription description = descriptions.get(topicName);
        if (description == null) {
            System.out.println("  Topic not found: " + topicName);
            return;
        }

        System.out.println("  Topic: " + description.name());
        System.out.println("  Internal: " + description.isInternal());
        System.out.println("  Partitions: " + description.partitions().size());

        System.out.println("\n  Partition Details:");
        description.partitions().forEach(partition -> {
            System.out.println("    Partition " + partition.partition() + ":");
            System.out.println("      Leader: " + partition.leader().id());
            System.out.println("      Replicas: " + partition.replicas().size());
            System.out.println("      ISR: " + partition.isr().size());
        });
    }
}
