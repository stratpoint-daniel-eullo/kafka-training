package com.training.kafka.services;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListConsumerGroupOffsetsResult;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Day 2: Data Flow and Message Patterns
 *
 * This service demonstrates core Kafka data flow concepts:
 * - Producer semantics and delivery guarantees
 * - Consumer groups and offset management
 * - Partitioning strategies
 * - Message ordering guarantees
 * - Consumer rebalancing
 *
 * @author Kafka Training Course
 * @version 1.0.0
 */
@Service
public class Day02DataFlowService {

    private static final Logger logger = LoggerFactory.getLogger(Day02DataFlowService.class);

    private final AdminClient adminClient;
    private final KafkaProducer<String, String> producer;

    public Day02DataFlowService(
            @Qualifier("trainingAdminClient") AdminClient adminClient,
            @Qualifier("trainingRawProducer") KafkaProducer<String, String> producer) {
        this.adminClient = adminClient;
        this.producer = producer;
        logger.info("🎓 Day02DataFlowService initialized");
    }

    /**
     * Demonstrate Day 2 data flow concepts
     */
    public void demonstrateDataFlow() {
        logger.info("=" + "=".repeat(79));
        logger.info("Day 2: Data Flow and Message Patterns");
        logger.info("=" + "=".repeat(79));

        try {
            // 1. Producer Semantics
            logger.info("\n1. Demonstrating Producer Semantics...");
            demonstrateProducerSemantics();

            // 2. Partitioning Strategies
            logger.info("\n2. Demonstrating Partitioning Strategies...");
            demonstratePartitioningStrategies();

            // 3. Consumer Groups
            logger.info("\n3. Demonstrating Consumer Groups...");
            demonstrateConsumerGroups();

            // 4. Offset Management
            logger.info("\n4. Demonstrating Offset Management...");
            demonstrateOffsetManagement();

            logger.info("\nDay 2 demonstration complete!");

        } catch (Exception e) {
            logger.error("Error in Day 2 demonstration", e);
        }
    }

    /**
     * Demonstrate producer delivery semantics
     */
    public void demonstrateProducerSemantics() {
        String topic = "dataflow-producer-semantics";

        logger.info("Producer Delivery Guarantees:");
        logger.info("  - At most once: acks=0 (may lose messages)");
        logger.info("  - At least once: acks=1 (may duplicate messages)");
        logger.info("  - Exactly once: acks=all + idempotence=true");

        try {
            // Demonstrate at-least-once with acks=all
            logger.info("\nSending with acks=all for at-least-once delivery...");
            ProducerRecord<String, String> record = new ProducerRecord<>(
                topic,
                "key1",
                "Message with acks=all for reliability"
            );

            RecordMetadata metadata = producer.send(record).get();
            logger.info("✅ Message sent: partition={}, offset={}",
                metadata.partition(), metadata.offset());

            // Log idempotence info
            logger.info("\n💡 Idempotent Producer:");
            logger.info("  - Prevents duplicate messages on retry");
            logger.info("  - Producer assigns sequence numbers to messages");
            logger.info("  - Broker deduplicates based on sequence numbers");

        } catch (ExecutionException | InterruptedException e) {
            logger.error("Failed to send message", e);
        }
    }

    /**
     * Demonstrate different partitioning strategies
     */
    public void demonstratePartitioningStrategies() {
        String topic = "dataflow-partitioning";

        logger.info("Partitioning Strategies:");
        logger.info("  1. Round-robin: null key, messages distributed evenly");
        logger.info("  2. Key-based: same key -> same partition");
        logger.info("  3. Custom: application-defined logic");

        try {
            // Strategy 1: Round-robin (null key)
            logger.info("\nStrategy 1: Round-robin distribution");
            Map<Integer, Integer> roundRobinCounts = new HashMap<>();
            for (int i = 0; i < 5; i++) {
                ProducerRecord<String, String> record = new ProducerRecord<>(
                    topic,
                    null,
                    "Round-robin message " + i
                );
                RecordMetadata metadata = producer.send(record).get();
                roundRobinCounts.merge(metadata.partition(), 1, Integer::sum);
                logger.debug("  Message {} -> partition {}", i, metadata.partition());
            }
            logger.info("  Distribution: {}", roundRobinCounts);

            // Strategy 2: Key-based partitioning
            logger.info("\nStrategy 2: Key-based partitioning");
            Map<String, Set<Integer>> keyToPartitions = new HashMap<>();
            String[] keys = {"user-1", "user-2", "user-3", "user-1", "user-2"};

            for (int i = 0; i < keys.length; i++) {
                ProducerRecord<String, String> record = new ProducerRecord<>(
                    topic,
                    keys[i],
                    "Message " + i + " from " + keys[i]
                );
                RecordMetadata metadata = producer.send(record).get();
                keyToPartitions.computeIfAbsent(keys[i], k -> new HashSet<>())
                    .add(metadata.partition());
                logger.debug("  {} -> partition {}", keys[i], metadata.partition());
            }

            logger.info("\n💡 Key -> Partition mapping:");
            keyToPartitions.forEach((key, partitions) ->
                logger.info("  {} -> partitions: {}", key, partitions));

            logger.info("\n✅ Same key always goes to same partition (ordering guarantee)");

        } catch (ExecutionException | InterruptedException e) {
            logger.error("Failed to demonstrate partitioning", e);
        }
    }

    /**
     * Demonstrate consumer group concepts
     */
    public void demonstrateConsumerGroups() {
        logger.info("Consumer Group Concepts:");
        logger.info("  - Consumers in same group share partition load");
        logger.info("  - Each partition consumed by one consumer in group");
        logger.info("  - Different groups consume all messages independently");
        logger.info("  - Rebalancing occurs when consumers join/leave");

        try {
            // List existing consumer groups
            Set<String> groups = adminClient.listConsumerGroups().all().get()
                .stream()
                .map(listing -> listing.groupId())
                .collect(java.util.stream.Collectors.toSet());

            logger.info("\nActive consumer groups: {}", groups.size());
            groups.forEach(group -> logger.info("  - {}", group));

            logger.info("\n💡 Consumer Group Best Practices:");
            logger.info("  1. Number of consumers <= number of partitions");
            logger.info("  2. Use unique group IDs for independent processing");
            logger.info("  3. Monitor consumer lag regularly");
            logger.info("  4. Handle rebalancing gracefully");

        } catch (ExecutionException | InterruptedException e) {
            logger.error("Failed to list consumer groups", e);
        }
    }

    /**
     * Demonstrate offset management strategies
     */
    public void demonstrateOffsetManagement() {
        String topic = "dataflow-offsets";
        String groupId = "offset-demo-group";

        logger.info("Offset Management Strategies:");
        logger.info("  1. Auto-commit: Offsets committed automatically");
        logger.info("  2. Manual commit: Application controls commits");
        logger.info("  3. Commit frequency: Trade-off between performance and safety");

        try {
            // Produce some test messages
            logger.info("\nProducing test messages...");
            for (int i = 0; i < 5; i++) {
                ProducerRecord<String, String> record = new ProducerRecord<>(
                    topic,
                    "key-" + i,
                    "Offset demo message " + i
                );
                producer.send(record).get();
            }
            producer.flush();

            logger.info("✅ Test messages produced");

            // Show offset information
            logger.info("\n💡 Offset Management:");
            logger.info("  - Committed offset: Last processed message + 1");
            logger.info("  - Current offset: Next message to read");
            logger.info("  - Lag: Difference between log-end and committed offset");

            logger.info("\n💡 Commit Strategies:");
            logger.info("  - Sync commit: Blocks until acknowledged (safer)");
            logger.info("  - Async commit: Non-blocking (faster, less safe)");
            logger.info("  - Per-message: Commit after each message (slowest, safest)");
            logger.info("  - Batch: Commit after processing batch (balanced)");

        } catch (ExecutionException | InterruptedException e) {
            logger.error("Failed to demonstrate offset management", e);
        }
    }

    /**
     * Get consumer group offset information
     */
    public Map<String, Object> getConsumerGroupOffsets(String groupId) {
        Map<String, Object> result = new HashMap<>();

        try {
            ListConsumerGroupOffsetsResult offsetsResult =
                adminClient.listConsumerGroupOffsets(groupId);

            Map<TopicPartition, OffsetAndMetadata> offsets = offsetsResult.partitionsToOffsetAndMetadata().get();

            if (offsets.isEmpty()) {
                result.put("status", "no_offsets");
                result.put("message", "No committed offsets found for group: " + groupId);
                return result;
            }

            List<Map<String, Object>> offsetInfo = new ArrayList<>();

            for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : offsets.entrySet()) {
                Map<String, Object> info = new HashMap<>();
                info.put("topic", entry.getKey().topic());
                info.put("partition", entry.getKey().partition());
                info.put("offset", entry.getValue().offset());
                info.put("metadata", entry.getValue().metadata());
                offsetInfo.add(info);
            }

            result.put("status", "success");
            result.put("groupId", groupId);
            result.put("offsets", offsetInfo);

            logger.info("Consumer group '{}' offsets: {} partitions", groupId, offsets.size());

        } catch (ExecutionException | InterruptedException e) {
            logger.error("Failed to get consumer group offsets", e);
            result.put("status", "error");
            result.put("message", e.getMessage());
        }

        return result;
    }

    /**
     * Calculate consumer lag for a group
     */
    public Map<String, Object> calculateConsumerLag(String groupId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // Get committed offsets
            Map<TopicPartition, OffsetAndMetadata> offsets =
                adminClient.listConsumerGroupOffsets(groupId)
                    .partitionsToOffsetAndMetadata().get();

            if (offsets.isEmpty()) {
                result.put("status", "no_offsets");
                result.put("message", "No committed offsets for group: " + groupId);
                return result;
            }

            // Get end offsets for the partitions
            Map<TopicPartition, OffsetSpec> offsetSpecs = new HashMap<>();
            offsets.keySet().forEach(tp -> offsetSpecs.put(tp, OffsetSpec.latest()));

            Map<TopicPartition, org.apache.kafka.clients.admin.ListOffsetsResult.ListOffsetsResultInfo> endOffsets =
                adminClient.listOffsets(offsetSpecs).all().get();

            // Calculate lag
            long totalLag = 0;
            List<Map<String, Object>> lagInfo = new ArrayList<>();

            for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : offsets.entrySet()) {
                TopicPartition tp = entry.getKey();
                long committedOffset = entry.getValue().offset();
                long endOffset = endOffsets.get(tp).offset();
                long lag = endOffset - committedOffset;

                Map<String, Object> info = new HashMap<>();
                info.put("topic", tp.topic());
                info.put("partition", tp.partition());
                info.put("committedOffset", committedOffset);
                info.put("endOffset", endOffset);
                info.put("lag", lag);
                lagInfo.add(info);

                totalLag += lag;
            }

            result.put("status", "success");
            result.put("groupId", groupId);
            result.put("totalLag", totalLag);
            result.put("partitions", lagInfo);

            logger.info("Consumer group '{}' total lag: {} messages", groupId, totalLag);

        } catch (ExecutionException | InterruptedException e) {
            logger.error("Failed to calculate consumer lag", e);
            result.put("status", "error");
            result.put("message", e.getMessage());
        }

        return result;
    }

    /**
     * Demonstrate message ordering guarantees
     */
    public void demonstrateMessageOrdering() {
        String topic = "dataflow-ordering";

        logger.info("Message Ordering Guarantees:");
        logger.info("  ✅ Within a partition: Strict ordering maintained");
        logger.info("  ❌ Across partitions: No ordering guarantee");
        logger.info("  💡 Solution: Use keys to route related messages to same partition");

        try {
            // Demonstrate ordered messages within partition
            logger.info("\nSending ordered sequence to same partition (using same key)...");
            String key = "order-sequence";

            for (int i = 1; i <= 5; i++) {
                ProducerRecord<String, String> record = new ProducerRecord<>(
                    topic,
                    key,
                    "Order step " + i
                );
                RecordMetadata metadata = producer.send(record).get();
                logger.info("  Step {} sent to partition {}, offset {}",
                    i, metadata.partition(), metadata.offset());
            }

            logger.info("\n✅ Messages with same key went to same partition");
            logger.info("✅ Order is guaranteed within that partition");

        } catch (ExecutionException | InterruptedException e) {
            logger.error("Failed to demonstrate ordering", e);
        }
    }

    /**
     * Print producer configuration best practices
     */
    public void printProducerBestPractices() {
        logger.info("\n📚 Producer Configuration Best Practices:");
        logger.info("\n1. Reliability (at-least-once):");
        logger.info("   acks=all");
        logger.info("   retries=Integer.MAX_VALUE");
        logger.info("   max.in.flight.requests.per.connection=5");

        logger.info("\n2. Performance (high throughput):");
        logger.info("   linger.ms=10");
        logger.info("   batch.size=32768");
        logger.info("   compression.type=lz4");

        logger.info("\n3. Exactly-once semantics:");
        logger.info("   enable.idempotence=true");
        logger.info("   transactional.id=<unique-id>");
    }

    /**
     * Print consumer configuration best practices
     */
    public void printConsumerBestPractices() {
        logger.info("\n📚 Consumer Configuration Best Practices:");
        logger.info("\n1. Reliability:");
        logger.info("   enable.auto.commit=false");
        logger.info("   isolation.level=read_committed");

        logger.info("\n2. Performance:");
        logger.info("   fetch.min.bytes=1024");
        logger.info("   fetch.max.wait.ms=500");
        logger.info("   max.poll.records=500");

        logger.info("\n3. Consumer Group:");
        logger.info("   session.timeout.ms=30000");
        logger.info("   heartbeat.interval.ms=3000");
        logger.info("   max.poll.interval.ms=300000");
    }

    /**
     * Get data flow concepts summary
     */
    public Map<String, Object> getDataFlowConcepts() {
        Map<String, Object> concepts = new LinkedHashMap<>();

        concepts.put("producerSemantics", Map.of(
            "atMostOnce", "acks=0 (may lose messages)",
            "atLeastOnce", "acks=1 or acks=all (may duplicate)",
            "exactlyOnce", "acks=all + idempotence=true + transactions"
        ));

        concepts.put("partitioning", Map.of(
            "roundRobin", "null key, even distribution",
            "keyBased", "same key -> same partition (ordering)",
            "custom", "custom partitioner logic"
        ));

        concepts.put("consumerGroups", Map.of(
            "concept", "consumers in group share partition load",
            "rule", "one partition per consumer in group",
            "independence", "different groups consume all messages",
            "rebalancing", "occurs when consumers join/leave"
        ));

        concepts.put("offsetManagement", Map.of(
            "autoCommit", "automatic at intervals (simple, less safe)",
            "manualCommit", "application controls (complex, safer)",
            "syncCommit", "blocks until acknowledged (reliable)",
            "asyncCommit", "non-blocking (faster, less safe)"
        ));

        concepts.put("ordering", Map.of(
            "withinPartition", "strict ordering guaranteed",
            "acrossPartitions", "no ordering guarantee",
            "solution", "use keys for related messages"
        ));

        return concepts;
    }
}
