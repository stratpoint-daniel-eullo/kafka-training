package com.training.kafka.services;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.admin.TopicListing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Spring Service for Day 1 Foundation Training - Basic Topic Operations
 *
 * This service demonstrates basic Kafka topic operations using Spring-managed AdminClient.
 * It shows how to create, list, describe, and delete topics programmatically in a Spring Boot context.
 *
 * @author Kafka Training Course
 * @version 1.0.0
 */
@Service
public class Day01FoundationService {

    private static final Logger logger = LoggerFactory.getLogger(Day01FoundationService.class);

    private final AdminClient adminClient;

    /**
     * Constructor with dependency injection of Spring-managed AdminClient
     */
    public Day01FoundationService(@Qualifier("trainingAdminClient") AdminClient adminClient) {
        this.adminClient = adminClient;
        logger.info("🎓 Day01FoundationService initialized with Spring-managed AdminClient");
    }

    /**
     * Create a new topic with specified configuration
     */
    public boolean createTopic(String topicName, int numPartitions, short replicationFactor) {
        try {
            NewTopic newTopic = new NewTopic(topicName, numPartitions, replicationFactor);

            // Optional: Set topic-specific configurations
            Map<String, String> configs = new HashMap<>();
            configs.put("retention.ms", "86400000"); // 24 hours
            configs.put("compression.type", "snappy");
            newTopic.configs(configs);

            CreateTopicsResult result = adminClient.createTopics(Collections.singletonList(newTopic));

            // Wait for creation to complete
            result.all().get();
            logger.info("✅ Topic '{}' created successfully with {} partitions", topicName, numPartitions);
            return true;

        } catch (ExecutionException | InterruptedException e) {
            if (e.getCause() instanceof org.apache.kafka.common.errors.TopicExistsException) {
                logger.warn("⚠️  Topic '{}' already exists", topicName);
                return false;
            } else {
                logger.error("❌ Failed to create topic '{}': {}", topicName, e.getMessage());
                return false;
            }
        }
    }

    /**
     * List all topics in the cluster
     */
    public Set<String> listTopics() {
        try {
            ListTopicsResult result = adminClient.listTopics();
            Set<String> topicNames = result.names().get();

            logger.info("📋 Found {} topics in the cluster:", topicNames.size());
            for (String topicName : topicNames) {
                logger.info("  - {}", topicName);
            }

            return topicNames;

        } catch (ExecutionException | InterruptedException e) {
            logger.error("❌ Failed to list topics: {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    /**
     * List topics with detailed information
     */
    public Map<String, TopicListing> listTopicsWithDetails() {
        try {
            ListTopicsResult result = adminClient.listTopics();
            Map<String, TopicListing> topics = result.namesToListings().get();

            logger.info("📋 Found {} topics with details:", topics.size());
            for (Map.Entry<String, TopicListing> entry : topics.entrySet()) {
                TopicListing listing = entry.getValue();
                logger.info("  - {} (internal: {})", listing.name(), listing.isInternal());
            }

            return topics;

        } catch (ExecutionException | InterruptedException e) {
            logger.error("❌ Failed to list topics with details: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * Describe a specific topic
     */
    public TopicDescription describeTopic(String topicName) {
        try {
            DescribeTopicsResult result = adminClient.describeTopics(Collections.singletonList(topicName));
            TopicDescription description = result.topicNameValues().get(topicName).get();

            logger.info("📊 Topic '{}' details:", topicName);
            logger.info("  - Partitions: {}", description.partitions().size());
            logger.info("  - Authorized operations: {}", description.authorizedOperations());

            description.partitions().forEach(partition -> {
                logger.info("  - Partition {}: leader={}, replicas={}, isr={}",
                    partition.partition(),
                    partition.leader().id(),
                    partition.replicas().stream().map(node -> node.id()).collect(java.util.stream.Collectors.toList()),
                    partition.isr().stream().map(node -> node.id()).collect(java.util.stream.Collectors.toList()));
            });

            return description;

        } catch (ExecutionException | InterruptedException e) {
            logger.error("❌ Failed to describe topic '{}': {}", topicName, e.getMessage());
            return null;
        }
    }

    /**
     * Delete a topic
     */
    public boolean deleteTopic(String topicName) {
        try {
            adminClient.deleteTopics(Collections.singletonList(topicName)).all().get();
            logger.info("🗑️  Topic '{}' deleted successfully", topicName);
            return true;

        } catch (ExecutionException | InterruptedException e) {
            logger.error("❌ Failed to delete topic '{}': {}", topicName, e.getMessage());
            return false;
        }
    }

    /**
     * Run Day 1 demonstration - creates sample topics and shows operations
     */
    public void runDay01Demonstration() {
        logger.info("🎓 Starting Day 1 Foundation Demonstration");

        try {
            // Create demonstration topics
            createTopic("demo-topic-1", 3, (short) 1);
            createTopic("demo-topic-2", 6, (short) 1);
            createTopic("user-events", 4, (short) 1);

            // List all topics
            listTopics();

            // Describe a topic
            describeTopic("demo-topic-1");

            logger.info("✅ Day 1 Foundation Demonstration completed successfully");

        } catch (Exception e) {
            logger.error("❌ Day 1 Foundation Demonstration failed: {}", e.getMessage(), e);
        }
    }

    /**
     * Check if a topic exists
     */
    public boolean topicExists(String topicName) {
        Set<String> topics = listTopics();
        return topics.contains(topicName);
    }

    /**
     * Get cluster information
     */
    public void getClusterInfo() {
        try {
            var clusterInfo = adminClient.describeCluster();
            var clusterId = clusterInfo.clusterId().get();
            var controller = clusterInfo.controller().get();
            var nodes = clusterInfo.nodes().get();

            logger.info("🏢 Cluster Information:");
            logger.info("  - Cluster ID: {}", clusterId);
            logger.info("  - Controller: {} ({}:{})", controller.id(), controller.host(), controller.port());
            logger.info("  - Nodes: {}", nodes.size());

            nodes.forEach(node -> {
                logger.info("    - Node {}: {}:{} (rack: {})",
                    node.id(), node.host(), node.port(), node.rack());
            });

        } catch (ExecutionException | InterruptedException e) {
            logger.error("❌ Failed to get cluster information: {}", e.getMessage());
        }
    }
}
