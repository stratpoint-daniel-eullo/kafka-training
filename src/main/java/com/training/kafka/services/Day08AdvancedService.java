package com.training.kafka.services;

import com.training.kafka.config.TrainingKafkaProperties;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.ConsumerGroupState;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Day 8: Advanced Topics - Security, Monitoring & Production Operations
 *
 * This service demonstrates:
 * - Security configurations (SSL, SASL, ACLs)
 * - Monitoring and metrics collection
 * - Performance optimization patterns
 * - Production best practices
 * - Troubleshooting techniques
 *
 * @author Kafka Training Course
 * @version 1.0.0
 */
@Service
public class Day08AdvancedService {

    private static final Logger logger = LoggerFactory.getLogger(Day08AdvancedService.class);

    private final AdminClient adminClient;
    private final TrainingKafkaProperties kafkaProperties;

    public Day08AdvancedService(@Qualifier("trainingAdminClient") AdminClient adminClient,
                                TrainingKafkaProperties kafkaProperties) {
        this.adminClient = adminClient;
        this.kafkaProperties = kafkaProperties;
        logger.info("Day08AdvancedService initialized");
    }

    /**
     * Demonstrate Day 8 Advanced Topics
     */
    public void demonstrateAdvancedTopics() {
        logger.info("=".repeat(80));
        logger.info("Day 8: Advanced Topics - Security, Monitoring & Production");
        logger.info("=".repeat(80));

        try {
            // 1. Security Configurations
            logger.info("\n1. Security Configurations");
            demonstrateSecurityConfigurations();

            // 2. Monitoring and Metrics
            logger.info("\n2. Monitoring and Metrics Collection");
            demonstrateMonitoring();

            // 3. Performance Best Practices
            logger.info("\n3. Performance Optimization Patterns");
            demonstratePerformancePatterns();

            // 4. Production Operations
            logger.info("\n4. Production Operations");
            demonstrateProductionOperations();

            logger.info("\nDay 8 demonstration complete!");

        } catch (Exception e) {
            logger.error("Error in Day 8 demonstration", e);
        }
    }

    /**
     * Demonstrate security configuration patterns
     */
    private void demonstrateSecurityConfigurations() {
        logger.info("Security Configuration Patterns:");

        // SSL Configuration
        Map<String, Object> sslConfig = getSslConfiguration();
        logger.info("  - SSL/TLS: {} properties configured", sslConfig.size());

        // SASL Configuration
        Map<String, Object> saslConfig = getSaslConfiguration();
        logger.info("  - SASL Authentication: {} properties configured", saslConfig.size());

        // ACL Patterns
        logger.info("  - ACL Authorization: Best practices documented");

        // Security Best Practices
        printSecurityBestPractices();
    }

    /**
     * Get SSL/TLS configuration
     */
    public Map<String, Object> getSslConfiguration() {
        Map<String, Object> config = new HashMap<>();

        config.put("security.protocol", "SSL");
        config.put(SslConfigs.SSL_PROTOCOL_CONFIG, "TLSv1.3");
        config.put(SslConfigs.SSL_ENABLED_PROTOCOLS_CONFIG, "TLSv1.3,TLSv1.2");
        config.put(SslConfigs.SSL_CIPHER_SUITES_CONFIG,
                "TLS_AES_256_GCM_SHA384,TLS_CHACHA20_POLY1305_SHA256");
        config.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "https");
        config.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "JKS");
        config.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "JKS");

        return config;
    }

    /**
     * Get SASL configuration
     */
    public Map<String, Object> getSaslConfiguration() {
        Map<String, Object> config = new HashMap<>();

        config.put("security.protocol", "SASL_SSL");
        config.put(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-512");
        config.put("sasl.login.class", "org.apache.kafka.common.security.scram.ScramLoginModule");

        return config;
    }

    /**
     * Get production-ready producer configuration
     */
    public Map<String, Object> getProductionProducerConfig() {
        Map<String, Object> config = new HashMap<>();

        // Reliability
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);

        // Performance
        config.put(ProducerConfig.BATCH_SIZE_CONFIG, 32768); // 32KB
        config.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        config.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        config.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 67108864); // 64MB

        // Timeouts
        config.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        config.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000);

        logger.info("Production producer configuration: {} properties", config.size());
        return config;
    }

    /**
     * Get production-ready consumer configuration
     */
    public Map<String, Object> getProductionConsumerConfig() {
        Map<String, Object> config = new HashMap<>();

        // Reliability
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");

        // Performance
        config.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 50000);
        config.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);

        // Session management
        config.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 45000);
        config.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 15000);
        config.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);

        logger.info("Production consumer configuration: {} properties", config.size());
        return config;
    }

    /**
     * Demonstrate monitoring capabilities
     */
    private void demonstrateMonitoring() {
        logger.info("Monitoring Capabilities:");

        // Cluster metrics
        Map<String, Object> clusterMetrics = getClusterMetrics();
        logger.info("  - Cluster metrics collected: {} items", clusterMetrics.size());

        // Consumer lag monitoring
        Map<String, Object> consumerLag = monitorConsumerLag();
        logger.info("  - Consumer lag monitoring: {} groups analyzed", consumerLag.get("groupCount"));

        // Topic metrics
        Map<String, Object> topicMetrics = getTopicMetrics();
        logger.info("  - Topic metrics: {} topics monitored", topicMetrics.get("topicCount"));
    }

    /**
     * Get cluster-level metrics
     */
    public Map<String, Object> getClusterMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        try {
            // Cluster information
            DescribeClusterResult clusterResult = adminClient.describeCluster();
            String clusterId = clusterResult.clusterId().get();
            int nodeCount = clusterResult.nodes().get().size();

            metrics.put("status", "success");
            metrics.put("clusterId", clusterId);
            metrics.put("nodeCount", nodeCount);
            metrics.put("timestamp", System.currentTimeMillis());

            logger.info("Cluster metrics: {} nodes in cluster {}", nodeCount, clusterId);

        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error collecting cluster metrics", e);
            metrics.put("status", "error");
            metrics.put("message", e.getMessage());
        }

        return metrics;
    }

    /**
     * Monitor consumer lag across all consumer groups
     */
    public Map<String, Object> monitorConsumerLag() {
        Map<String, Object> result = new HashMap<>();

        try {
            ListConsumerGroupsResult groupsResult = adminClient.listConsumerGroups();
            Collection<ConsumerGroupListing> groups = groupsResult.all().get();

            int totalGroups = groups.size();
            List<Map<String, Object>> groupLags = new ArrayList<>();
            long totalLagAcrossAllGroups = 0;

            for (ConsumerGroupListing group : groups) {
                Map<String, Object> groupLag = monitorGroupLag(group.groupId());
                if (groupLag.containsKey("totalLag")) {
                    totalLagAcrossAllGroups += (Long) groupLag.get("totalLag");
                }
                groupLags.add(groupLag);
            }

            result.put("status", "success");
            result.put("groupCount", totalGroups);
            result.put("totalLag", totalLagAcrossAllGroups);
            result.put("groups", groupLags);

            logger.info("Consumer lag: {} groups, total lag {} records", totalGroups, totalLagAcrossAllGroups);

        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error monitoring consumer lag", e);
            result.put("status", "error");
            result.put("message", e.getMessage());
        }

        return result;
    }

    /**
     * Monitor lag for a specific consumer group
     */
    private Map<String, Object> monitorGroupLag(String groupId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // Get consumer group description
            DescribeConsumerGroupsResult groupResult = adminClient.describeConsumerGroups(
                    Collections.singletonList(groupId));
            ConsumerGroupDescription description = groupResult.describedGroups().get(groupId).get();

            result.put("groupId", groupId);
            result.put("state", description.state().toString());
            result.put("members", description.members().size());

            if (description.state() != ConsumerGroupState.STABLE) {
                logger.warn("Consumer group {} is not stable: {}", groupId, description.state());
                result.put("totalLag", 0L);
                return result;
            }

            // Get consumer group offsets
            ListConsumerGroupOffsetsResult offsetsResult = adminClient.listConsumerGroupOffsets(groupId);
            Map<TopicPartition, OffsetAndMetadata> offsets = offsetsResult.partitionsToOffsetAndMetadata().get();

            // Get latest offsets
            Set<TopicPartition> partitions = offsets.keySet();
            if (partitions.isEmpty()) {
                result.put("totalLag", 0L);
                return result;
            }

            Map<TopicPartition, OffsetSpec> latestOffsetSpec = partitions.stream()
                    .collect(Collectors.toMap(tp -> tp, tp -> OffsetSpec.latest()));

            ListOffsetsResult latestOffsetsResult = adminClient.listOffsets(latestOffsetSpec);
            Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> latestOffsets =
                    latestOffsetsResult.all().get();

            // Calculate lag
            long totalLag = 0;
            for (TopicPartition partition : partitions) {
                long currentOffset = offsets.get(partition).offset();
                long latestOffset = latestOffsets.get(partition).offset();
                long lag = latestOffset - currentOffset;
                totalLag += lag;

                if (lag > 1000) {
                    logger.warn("High lag in group {} partition {}: {} records",
                            groupId, partition, lag);
                }
            }

            result.put("totalLag", totalLag);
            result.put("partitionCount", partitions.size());

            if (totalLag > 10000) {
                logger.error("ALERT: High consumer lag in group {}: {} records", groupId, totalLag);
            }

        } catch (Exception e) {
            logger.error("Error monitoring lag for group {}", groupId, e);
            result.put("totalLag", 0L);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * Get topic-level metrics
     */
    public Map<String, Object> getTopicMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        try {
            ListTopicsResult topicsResult = adminClient.listTopics();
            Set<String> topics = topicsResult.names().get();

            metrics.put("status", "success");
            metrics.put("topicCount", topics.size());
            metrics.put("topics", topics);

            logger.info("Topic metrics: {} topics in cluster", topics.size());

        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error collecting topic metrics", e);
            metrics.put("status", "error");
            metrics.put("message", e.getMessage());
        }

        return metrics;
    }

    /**
     * Demonstrate performance optimization patterns
     */
    private void demonstratePerformancePatterns() {
        logger.info("Performance Optimization Patterns:");

        // High throughput configuration
        logger.info("  - High Throughput: Large batches, compression, buffering");

        // Low latency configuration
        logger.info("  - Low Latency: No batching, no compression, immediate send");

        // Balanced configuration
        logger.info("  - Balanced: Moderate batching, light compression");

        printPerformanceBestPractices();
    }

    /**
     * Demonstrate production operations
     */
    private void demonstrateProductionOperations() {
        logger.info("Production Operations:");

        // Cluster health check
        boolean healthy = checkClusterHealth();
        logger.info("  - Cluster health: {}", healthy ? "HEALTHY" : "UNHEALTHY");

        // Configuration validation
        boolean configValid = validateProductionConfig();
        logger.info("  - Configuration: {}", configValid ? "VALID" : "NEEDS_REVIEW");

        printProductionChecklist();
    }

    /**
     * Check cluster health
     */
    public boolean checkClusterHealth() {
        try {
            DescribeClusterResult result = adminClient.describeCluster();
            int nodeCount = result.nodes().get().size();

            if (nodeCount < 3) {
                logger.warn("Cluster has only {} brokers, recommend minimum 3 for production", nodeCount);
                return false;
            }

            logger.info("Cluster health check passed: {} brokers", nodeCount);
            return true;

        } catch (Exception e) {
            logger.error("Cluster health check failed", e);
            return false;
        }
    }

    /**
     * Validate production configuration
     */
    public boolean validateProductionConfig() {
        boolean valid = true;

        // Check bootstrap servers
        String bootstrapServers = kafkaProperties.getKafka().getBootstrapServers();
        if (bootstrapServers.contains("localhost")) {
            logger.warn("Bootstrap servers contain 'localhost' - not recommended for production");
            valid = false;
        }

        // Check client ID
        String clientId = kafkaProperties.getKafka().getClientId();
        if (clientId == null || clientId.isEmpty()) {
            logger.warn("Client ID not set - recommended for production traceability");
            valid = false;
        }

        if (valid) {
            logger.info("Production configuration validation passed");
        } else {
            logger.warn("Production configuration needs review");
        }

        return valid;
    }

    /**
     * Get comprehensive system status
     */
    public Map<String, Object> getSystemStatus() {
        Map<String, Object> status = new HashMap<>();

        status.put("cluster", getClusterMetrics());
        status.put("consumerLag", monitorConsumerLag());
        status.put("topics", getTopicMetrics());
        status.put("health", checkClusterHealth());
        status.put("configValid", validateProductionConfig());
        status.put("timestamp", System.currentTimeMillis());

        return status;
    }

    // ===== Security Best Practices =====

    private void printSecurityBestPractices() {
        logger.info("\nSecurity Best Practices:");
        logger.info("  1. Network: Use SSL/TLS for all communications");
        logger.info("  2. Authentication: Implement SASL/SCRAM-SHA-512");
        logger.info("  3. Authorization: Enable ACLs with least privilege");
        logger.info("  4. Certificates: Rotate regularly (every 90 days)");
        logger.info("  5. Passwords: Use strong passwords, store in vault");
        logger.info("  6. Monitoring: Track authentication failures and violations");
    }

    // ===== Performance Best Practices =====

    private void printPerformanceBestPractices() {
        logger.info("\nPerformance Best Practices:");
        logger.info("  1. Batching: Use appropriate batch.size and linger.ms");
        logger.info("  2. Compression: Use lz4 or snappy for balance");
        logger.info("  3. Partitioning: Distribute load across partitions");
        logger.info("  4. Replication: Balance between reliability and performance");
        logger.info("  5. Hardware: Use fast disks (SSD/NVMe) for logs");
        logger.info("  6. Network: Ensure sufficient bandwidth for peak load");
    }

    // ===== Production Checklist =====

    private void printProductionChecklist() {
        logger.info("\nProduction Deployment Checklist:");
        logger.info("  Infrastructure:");
        logger.info("    ✓ Minimum 3 brokers for fault tolerance");
        logger.info("    ✓ Dedicated hardware/VMs for Kafka");
        logger.info("    ✓ Separate disks for logs and OS");
        logger.info("    ✓ Network bandwidth sufficient for peak load");
        logger.info("\n  Security:");
        logger.info("    ✓ SSL/TLS enabled");
        logger.info("    ✓ SASL authentication configured");
        logger.info("    ✓ ACLs implemented");
        logger.info("    ✓ Network firewalls configured");
        logger.info("\n  Monitoring:");
        logger.info("    ✓ JMX metrics collection enabled");
        logger.info("    ✓ Dashboards for cluster health");
        logger.info("    ✓ Alerting for critical issues");
        logger.info("    ✓ Log aggregation configured");
        logger.info("\n  Operations:");
        logger.info("    ✓ Backup and recovery procedures");
        logger.info("    ✓ Runbooks for common issues");
        logger.info("    ✓ Deployment automation");
        logger.info("    ✓ Incident response plan");
    }
}
