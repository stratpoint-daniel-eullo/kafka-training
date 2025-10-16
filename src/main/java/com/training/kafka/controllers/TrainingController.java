package com.training.kafka.controllers;

import com.training.kafka.services.Day01FoundationService;
import com.training.kafka.services.Day02DataFlowService;
import com.training.kafka.services.Day03ProducerService;
import com.training.kafka.services.Day04ConsumerService;
import com.training.kafka.services.Day05StreamsService;
import com.training.kafka.services.Day06SchemaService;
import com.training.kafka.services.Day07ConnectService;
import com.training.kafka.services.Day08AdvancedService;
import com.training.kafka.services.EventMartService;
import com.training.kafka.config.ProfileConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * REST Controller for Kafka Training Course
 *
 * Provides web endpoints to trigger training examples and demonstrations
 * for interactive learning through a web browser.
 *
 * @author Kafka Training Course
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/training")
@CrossOrigin(origins = "*")
public class TrainingController {

    private static final Logger logger = LoggerFactory.getLogger(TrainingController.class);

    private final Day01FoundationService day01Service;
    private final Day02DataFlowService day02Service;
    private final Day03ProducerService day03Service;
    private final Day04ConsumerService day04Service;
    private final Day05StreamsService day05Service;
    private final Day06SchemaService day06Service;
    private final Day07ConnectService day07Service;
    private final Day08AdvancedService day08Service;
    private final EventMartService eventMartService;
    private final ProfileConfiguration profileConfiguration;

    public TrainingController(
            Day01FoundationService day01Service,
            Day02DataFlowService day02Service,
            Day03ProducerService day03Service,
            Day04ConsumerService day04Service,
            Day05StreamsService day05Service,
            Day06SchemaService day06Service,
            Day07ConnectService day07Service,
            Day08AdvancedService day08Service,
            EventMartService eventMartService,
            ProfileConfiguration profileConfiguration) {
        this.day01Service = day01Service;
        this.day02Service = day02Service;
        this.day03Service = day03Service;
        this.day04Service = day04Service;
        this.day05Service = day05Service;
        this.day06Service = day06Service;
        this.day07Service = day07Service;
        this.day08Service = day08Service;
        this.eventMartService = eventMartService;
        this.profileConfiguration = profileConfiguration;
        logger.info("🌐 TrainingController initialized - Web API ready");
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Kafka Training Course");
        response.put("version", "1.0.0");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * Get available training modules
     */
    @GetMapping("/modules")
    public ResponseEntity<Map<String, Object>> getModules() {
        Map<String, Object> modules = new HashMap<>();

        modules.put("Day01Foundation", Map.of(
            "name", "Kafka Fundamentals",
            "description", "Basic topic operations and AdminClient usage",
            "endpoints", new String[]{"/day01/demo", "/day01/topics", "/day01/create-topic"}
        ));

        modules.put("Day02DataFlow", Map.of(
            "name", "Data Flow & Message Patterns",
            "description", "Producer/consumer semantics, offset management, partitioning strategies, message ordering",
            "endpoints", new String[]{"/day02/demo", "/day02/concepts", "/day02/consumer-lag/*", "/day02/offsets/*"}
        ));

        modules.put("Day03Producers", Map.of(
            "name", "Message Producers",
            "description", "Publishing messages to Kafka topics",
            "endpoints", new String[]{"/day03/demo", "/day03/send-message", "/day03/send-batch"}
        ));

        modules.put("Day04Consumers", Map.of(
            "name", "Message Consumers",
            "description", "Processing messages from Kafka topics",
            "endpoints", new String[]{"/day04/demo", "/day04/stats", "/day04/consume-raw"}
        ));

        modules.put("Day05Streams", Map.of(
            "name", "Kafka Streams",
            "description", "Real-time stream processing with Kafka Streams",
            "endpoints", new String[]{"/day05/demo", "/day05/streams/status", "/day05/streams/*/start", "/day05/streams/*/stop"}
        ));

        modules.put("Day06Schemas", Map.of(
            "name", "Schema Management",
            "description", "Avro schemas and Schema Registry integration",
            "endpoints", new String[]{"/day06/demo", "/day06/avro/produce", "/day06/avro/consume", "/day06/schemas/list"}
        ));

        modules.put("Day07Connect", Map.of(
            "name", "Kafka Connect",
            "description", "Data integration with JDBC source and sink connectors",
            "endpoints", new String[]{"/day07/demo", "/day07/connectors/list", "/day07/connectors/create/*", "/day07/connectors/*/status"}
        ));

        modules.put("Day08Advanced", Map.of(
            "name", "Advanced Topics",
            "description", "Security (SSL/TLS, SASL, ACLs), Monitoring (JMX metrics, consumer lag), Performance Optimization, Production Operations",
            "endpoints", new String[]{"/day08/demo", "/day08/status", "/day08/metrics/cluster", "/day08/metrics/consumer-lag", "/day08/config/security", "/day08/config/production"}
        ));

        modules.put("EventMart", Map.of(
            "name", "EventMart Progressive Project",
            "description", "Complete e-commerce event streaming platform",
            "endpoints", new String[]{"/eventmart/demo", "/eventmart/status", "/eventmart/topics", "/eventmart/simulate"}
        ));

        return ResponseEntity.ok(modules);
    }

    // ===== Day 01 Foundation Endpoints =====

    @PostMapping("/day01/demo")
    public ResponseEntity<Map<String, String>> runDay01Demo() {
        logger.info("🎓 Running Day 1 Foundation Demo via Web API");

        try {
            day01Service.runDay01Demonstration();

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Day 1 Foundation demonstration completed successfully");
            response.put("module", "Day01Foundation");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Day 1 demo failed: {}", e.getMessage(), e);

            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Day 1 demonstration failed: " + e.getMessage());
            response.put("module", "Day01Foundation");

            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/day01/topics")
    public ResponseEntity<Map<String, Object>> listTopics() {
        logger.info("📋 Listing topics via Web API");

        Set<String> topics = day01Service.listTopics();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("topics", topics);
        response.put("count", topics.size());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/day01/create-topic")
    public ResponseEntity<Map<String, String>> createTopic(
            @RequestParam String name,
            @RequestParam(defaultValue = "3") int partitions,
            @RequestParam(defaultValue = "1") short replicationFactor) {

        // Validate topic name
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("status", "error",
                           "message", "Topic name cannot be empty",
                           "hint", "Provide a valid topic name"));
        }

        // Validate topic name format (Kafka naming rules)
        if (!name.matches("^[a-zA-Z0-9._-]+$")) {
            return ResponseEntity.badRequest()
                .body(Map.of("status", "error",
                           "message", "Invalid topic name format",
                           "hint", "Topic names can only contain letters, numbers, dots, hyphens, and underscores"));
        }

        if (name.startsWith("_")) {
            return ResponseEntity.badRequest()
                .body(Map.of("status", "error",
                           "message", "Topic names cannot start with underscore",
                           "hint", "Internal Kafka topics use underscore prefix"));
        }

        // Validate partitions
        if (partitions < 1 || partitions > 1000) {
            return ResponseEntity.badRequest()
                .body(Map.of("status", "error",
                           "message", "Invalid partition count: " + partitions,
                           "hint", "Partitions must be between 1 and 1000 for training"));
        }

        // Validate replication factor
        if (replicationFactor < 1 || replicationFactor > 5) {
            return ResponseEntity.badRequest()
                .body(Map.of("status", "error",
                           "message", "Invalid replication factor: " + replicationFactor,
                           "hint", "Replication factor must be between 1 and 5 for training"));
        }

        logger.info("🔧 Creating topic '{}' via Web API", name);

        boolean success = day01Service.createTopic(name, partitions, replicationFactor);

        Map<String, String> response = new HashMap<>();
        if (success) {
            response.put("status", "success");
            response.put("message", "Topic '" + name + "' created successfully");
        } else {
            response.put("status", "error");
            response.put("message", "Failed to create topic '" + name + "' - it may already exist");
        }
        response.put("topic", name);

        return ResponseEntity.ok(response);
    }

    // ===== Day 02 Data Flow Endpoints =====

    @PostMapping("/day02/demo")
    public ResponseEntity<Map<String, String>> runDay02Demo() {
        logger.info("🎓 Running Day 2 Data Flow Demo via Web API");

        try {
            day02Service.demonstrateDataFlow();

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Day 2 Data Flow demonstration completed successfully");
            response.put("module", "Day02DataFlow");
            response.put("info", "Producer semantics, partitioning, consumer groups, and offset management demonstrated");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Day 2 demo failed: {}", e.getMessage(), e);

            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Day 2 demonstration failed: " + e.getMessage());
            response.put("module", "Day02DataFlow");

            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/day02/concepts")
    public ResponseEntity<Map<String, Object>> getDay02Concepts() {
        logger.info("📚 Getting data flow concepts via Web API");

        try {
            Map<String, Object> concepts = day02Service.getDataFlowConcepts();

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("concepts", concepts);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Failed to get data flow concepts: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to get data flow concepts: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/day02/consumer-lag/{groupId}")
    public ResponseEntity<Map<String, Object>> getDay02ConsumerLag(@PathVariable String groupId) {
        logger.info("📊 Getting consumer lag for group '{}' via Web API", groupId);

        // Validate groupId
        if (groupId == null || groupId.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("status", "error",
                           "message", "Group ID cannot be empty",
                           "hint", "Provide a valid consumer group ID"));
        }

        try {
            Map<String, Object> lagInfo = day02Service.calculateConsumerLag(groupId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", lagInfo.get("status"));
            response.put("groupId", groupId);

            if ("success".equals(lagInfo.get("status"))) {
                response.put("totalLag", lagInfo.get("totalLag"));
                response.put("partitions", lagInfo.get("partitions"));
            } else {
                response.put("message", lagInfo.get("message"));
            }

            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Failed to calculate consumer lag: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to calculate consumer lag: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/day02/offsets/{groupId}")
    public ResponseEntity<Map<String, Object>> getDay02ConsumerOffsets(@PathVariable String groupId) {
        logger.info("📋 Getting consumer offsets for group '{}' via Web API", groupId);

        // Validate groupId
        if (groupId == null || groupId.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("status", "error",
                           "message", "Group ID cannot be empty",
                           "hint", "Provide a valid consumer group ID"));
        }

        try {
            Map<String, Object> offsetsInfo = day02Service.getConsumerGroupOffsets(groupId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", offsetsInfo.get("status"));
            response.put("groupId", groupId);

            if ("success".equals(offsetsInfo.get("status"))) {
                response.put("offsets", offsetsInfo.get("offsets"));
            } else {
                response.put("message", offsetsInfo.get("message"));
            }

            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Failed to get consumer offsets: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to get consumer offsets: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    // ===== Day 03 Producer Endpoints =====

    @PostMapping("/day03/demo")
    public ResponseEntity<Map<String, String>> runDay03Demo(
            @RequestParam(defaultValue = "user-events") String topic) {

        logger.info("🎓 Running Day 3 Producer Demo via Web API");

        try {
            day03Service.demonstrateProducerPatterns(topic);

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Day 3 Producer demonstration completed successfully");
            response.put("module", "Day03Producers");
            response.put("topic", topic);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Day 3 demo failed: {}", e.getMessage(), e);

            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Day 3 demonstration failed: " + e.getMessage());
            response.put("module", "Day03Producers");

            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/day03/send-message")
    public ResponseEntity<Map<String, String>> sendMessage(
            @RequestParam(defaultValue = "user-events") String topic,
            @RequestParam String key,
            @RequestParam String message) {

        // Validate inputs
        if (topic == null || topic.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("status", "error",
                           "message", "Topic cannot be empty",
                           "hint", "Provide a valid topic name"));
        }

        if (key == null || key.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("status", "error",
                           "message", "Message key cannot be empty",
                           "hint", "Keys are used for partitioning in Kafka"));
        }

        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("status", "error",
                           "message", "Message content cannot be empty",
                           "hint", "Provide message content to send"));
        }

        logger.info("📤 Sending message via Web API: topic={}, key={}", topic, key);

        boolean success = day03Service.sendMessageSyncSpring(topic, key, message);

        Map<String, String> response = new HashMap<>();
        response.put("status", success ? "success" : "error");
        response.put("message", success ? "Message sent successfully" : "Failed to send message");
        response.put("topic", topic);
        response.put("key", key);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/day03/send-batch")
    public ResponseEntity<Map<String, String>> sendBatchMessages(
            @RequestParam(defaultValue = "user-events") String topic,
            @RequestParam(defaultValue = "10") int count) {

        // Validate count for training safety
        if (count < 1) {
            return ResponseEntity.badRequest()
                .body(Map.of("status", "error",
                           "message", "Count must be at least 1",
                           "hint", "Specify a positive number of messages"));
        }

        if (count > 10000) {
            return ResponseEntity.badRequest()
                .body(Map.of("status", "error",
                           "message", "Count too large: " + count,
                           "hint", "For training, limit to 10,000 messages to avoid overwhelming Kafka"));
        }

        logger.info("📤 Sending {} batch messages via Web API", count);

        try {
            day03Service.sendBatchMessagesSpring(topic, count);

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", count + " messages sent successfully");
            response.put("topic", topic);
            response.put("count", String.valueOf(count));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Batch send failed: {}", e.getMessage(), e);

            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to send batch messages: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    // ===== Day 04 Consumer Endpoints =====

    @PostMapping("/day04/demo")
    public ResponseEntity<Map<String, String>> runDay04Demo() {
        logger.info("🎓 Running Day 4 Consumer Demo via Web API");

        try {
            day04Service.demonstrateConsumerPatterns();

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Day 4 Consumer demonstration completed successfully");
            response.put("module", "Day04Consumers");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Day 4 demo failed: {}", e.getMessage(), e);

            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Day 4 demonstration failed: " + e.getMessage());
            response.put("module", "Day04Consumers");

            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/day04/stats")
    public ResponseEntity<Map<String, Object>> getConsumerStats() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("processedMessages", day04Service.getProcessedMessageCount());
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/day04/consume-raw")
    public ResponseEntity<Map<String, String>> consumeRawMessages(
            @RequestParam(defaultValue = "user-events") String topic,
            @RequestParam(defaultValue = "raw-web-group") String groupId,
            @RequestParam(defaultValue = "5") int maxMessages) {

        // Validate maxMessages for training safety
        if (maxMessages < 1) {
            return ResponseEntity.badRequest()
                .body(Map.of("status", "error",
                           "message", "maxMessages must be at least 1",
                           "hint", "Specify a positive number of messages to consume"));
        }

        if (maxMessages > 1000) {
            return ResponseEntity.badRequest()
                .body(Map.of("status", "error",
                           "message", "maxMessages too large: " + maxMessages,
                           "hint", "For training, limit to 1,000 messages to avoid long waits"));
        }

        logger.info("📥 Starting raw consumer via Web API");

        try {
            day04Service.consumeMessagesRaw(topic, groupId, maxMessages);

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Raw consumer processed " + maxMessages + " messages");
            response.put("topic", topic);
            response.put("groupId", groupId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Raw consumer failed: {}", e.getMessage(), e);

            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Raw consumer failed: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    // ===== Day 05 Kafka Streams Endpoints =====

    @PostMapping("/day05/demo")
    public ResponseEntity<Map<String, String>> runDay05Demo() {
        logger.info("🎓 Running Day 5 Kafka Streams Demo via Web API");

        try {
            day05Service.demonstrateKafkaStreams();

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Day 5 Kafka Streams demonstration completed successfully");
            response.put("module", "Day05Streams");
            response.put("info", "Use /day05/streams/* endpoints to start/stop individual stream applications");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Day 5 demo failed: {}", e.getMessage(), e);

            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Day 5 demonstration failed: " + e.getMessage());
            response.put("module", "Day05Streams");

            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/day05/streams/status")
    public ResponseEntity<Map<String, Object>> getStreamsStatus() {
        logger.info("📊 Getting Kafka Streams status via Web API");

        try {
            Map<String, Object> streamsStatus = day05Service.getStreamsStatus();

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("streams", streamsStatus);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Failed to get streams status: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to get streams status: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/day05/streams/user-activity/start")
    public ResponseEntity<Map<String, String>> startUserActivityStream() {
        logger.info("🌊 Starting User Activity Stream via Web API");

        boolean success = day05Service.startUserActivityStream();

        Map<String, String> response = new HashMap<>();
        response.put("status", success ? "success" : "warning");
        response.put("message", success
            ? "User Activity Stream started successfully"
            : "User Activity Stream is already running or failed to start");
        response.put("stream", "user-activity");
        response.put("description", "Aggregates user events in 1-hour windows");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/day05/streams/user-activity/stop")
    public ResponseEntity<Map<String, String>> stopUserActivityStream() {
        logger.info("🛑 Stopping User Activity Stream via Web API");

        boolean success = day05Service.stopUserActivityStream();

        Map<String, String> response = new HashMap<>();
        response.put("status", success ? "success" : "warning");
        response.put("message", success
            ? "User Activity Stream stopped successfully"
            : "User Activity Stream was not running");
        response.put("stream", "user-activity");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/day05/streams/order-analytics/start")
    public ResponseEntity<Map<String, String>> startOrderAnalyticsStream() {
        logger.info("🌊 Starting Order Analytics Stream via Web API");

        boolean success = day05Service.startOrderAnalyticsStream();

        Map<String, String> response = new HashMap<>();
        response.put("status", success ? "success" : "warning");
        response.put("message", success
            ? "Order Analytics Stream started successfully"
            : "Order Analytics Stream is already running or failed to start");
        response.put("stream", "order-analytics");
        response.put("description", "Real-time order metrics in 15-minute windows");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/day05/streams/order-analytics/stop")
    public ResponseEntity<Map<String, String>> stopOrderAnalyticsStream() {
        logger.info("🛑 Stopping Order Analytics Stream via Web API");

        boolean success = day05Service.stopOrderAnalyticsStream();

        Map<String, String> response = new HashMap<>();
        response.put("status", success ? "success" : "warning");
        response.put("message", success
            ? "Order Analytics Stream stopped successfully"
            : "Order Analytics Stream was not running");
        response.put("stream", "order-analytics");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/day05/streams/fraud-detection/start")
    public ResponseEntity<Map<String, String>> startFraudDetectionStream() {
        logger.info("🌊 Starting Fraud Detection Stream via Web API");

        boolean success = day05Service.startFraudDetectionStream();

        Map<String, String> response = new HashMap<>();
        response.put("status", success ? "success" : "warning");
        response.put("message", success
            ? "Fraud Detection Stream started successfully"
            : "Fraud Detection Stream is already running or failed to start");
        response.put("stream", "fraud-detection");
        response.put("description", "Joins user events with orders for suspicious pattern detection");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/day05/streams/fraud-detection/stop")
    public ResponseEntity<Map<String, String>> stopFraudDetectionStream() {
        logger.info("🛑 Stopping Fraud Detection Stream via Web API");

        boolean success = day05Service.stopFraudDetectionStream();

        Map<String, String> response = new HashMap<>();
        response.put("status", success ? "success" : "warning");
        response.put("message", success
            ? "Fraud Detection Stream stopped successfully"
            : "Fraud Detection Stream was not running");
        response.put("stream", "fraud-detection");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/day05/streams/eventmart/start")
    public ResponseEntity<Map<String, String>> startEventMartRealtimeAnalytics() {
        logger.info("🌊 Starting EventMart Real-time Analytics Stream via Web API");

        boolean success = day05Service.startEventMartRealtimeAnalytics();

        Map<String, String> response = new HashMap<>();
        response.put("status", success ? "success" : "warning");
        response.put("message", success
            ? "EventMart Real-time Analytics started successfully"
            : "EventMart Real-time Analytics is already running or failed to start");
        response.put("stream", "eventmart-realtime");
        response.put("description", "Comprehensive real-time business analytics for EventMart");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/day05/streams/eventmart/stop")
    public ResponseEntity<Map<String, String>> stopEventMartRealtimeAnalytics() {
        logger.info("🛑 Stopping EventMart Real-time Analytics Stream via Web API");

        boolean success = day05Service.stopEventMartRealtimeAnalytics();

        Map<String, String> response = new HashMap<>();
        response.put("status", success ? "success" : "warning");
        response.put("message", success
            ? "EventMart Real-time Analytics stopped successfully"
            : "EventMart Real-time Analytics was not running");
        response.put("stream", "eventmart-realtime");

        return ResponseEntity.ok(response);
    }

    // ===== Day 06 Schema Management Endpoints =====

    @PostMapping("/day06/demo")
    public ResponseEntity<Map<String, String>> runDay06Demo() {
        logger.info("🎓 Running Day 6 Schema Management Demo via Web API");

        try {
            day06Service.demonstrateSchemaManagement();

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Day 6 Schema Management demonstration completed successfully");
            response.put("module", "Day06Schemas");
            response.put("info", "Avro events produced and consumed with Schema Registry integration");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Day 6 demo failed: {}", e.getMessage(), e);

            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Day 6 demonstration failed: " + e.getMessage());
            response.put("module", "Day06Schemas");

            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/day06/avro/produce")
    public ResponseEntity<Map<String, String>> produceAvroEvents() {
        logger.info("📤 Producing Avro events via Web API");

        try {
            day06Service.produceAvroEvents();

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Avro events produced successfully");
            response.put("topics", "user-events-avro, product-events-avro, order-events-avro, payment-events-avro");
            response.put("schemaRegistry", "Schemas automatically registered");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Failed to produce Avro events: {}", e.getMessage(), e);

            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to produce Avro events: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/day06/avro/consume")
    public ResponseEntity<Map<String, String>> consumeAvroEvents() {
        logger.info("📥 Consuming Avro events via Web API");

        try {
            day06Service.consumeAvroEvents();

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Avro events consumed successfully");
            response.put("topics", "user-events-avro, product-events-avro, order-events-avro, payment-events-avro");
            response.put("info", "Check logs for consumed event details");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Failed to consume Avro events: {}", e.getMessage(), e);

            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to consume Avro events: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/day06/schemas/list")
    public ResponseEntity<Map<String, Object>> listRegisteredSchemas() {
        logger.info("📋 Listing registered schemas via Web API");

        try {
            Map<String, Object> schemasInfo = day06Service.listRegisteredSchemas();

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("schemaRegistry", "http://localhost:8082");
            response.put("data", schemasInfo);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Failed to list schemas: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to list schemas: " + e.getMessage());
            response.put("hint", "Ensure Schema Registry is running at http://localhost:8082");

            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/day06/schemas/compatibility")
    public ResponseEntity<Map<String, Object>> checkSchemaCompatibility(
            @RequestParam String subject,
            @RequestParam String schema) {

        // Validate inputs
        if (subject == null || subject.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("status", "error",
                           "message", "Subject cannot be empty",
                           "hint", "Provide a valid schema subject (e.g., 'user-events-avro-value')"));
        }

        if (schema == null || schema.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("status", "error",
                           "message", "Schema cannot be empty",
                           "hint", "Provide a valid Avro schema in JSON format"));
        }

        logger.info("🔍 Checking schema compatibility for subject: {}", subject);

        try {
            Map<String, Object> compatibilityInfo = day06Service.checkSchemaCompatibility(subject, schema);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", compatibilityInfo);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Failed to check compatibility: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to check schema compatibility: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    // ===== Day 07 Kafka Connect Endpoints =====

    @PostMapping("/day07/demo")
    public ResponseEntity<Map<String, String>> runDay07Demo() {
        logger.info("🎓 Running Day 7 Kafka Connect Demo via Web API");

        try {
            day07Service.demonstrateKafkaConnect();

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Day 7 Kafka Connect demonstration completed successfully");
            response.put("module", "Day07Connect");
            response.put("info", "JDBC source and sink connectors created for EventMart data integration");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Day 7 demo failed: {}", e.getMessage(), e);

            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Day 7 demonstration failed: " + e.getMessage());
            response.put("module", "Day07Connect");

            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/day07/connect/info")
    public ResponseEntity<Map<String, Object>> getConnectClusterInfo() {
        logger.info("ℹ️  Getting Kafka Connect cluster info via Web API");

        try {
            Map<String, Object> clusterInfo = day07Service.getConnectClusterInfo();

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("connectUrl", "http://localhost:8083");
            response.put("data", clusterInfo);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Failed to get Connect cluster info: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to get Connect cluster info: " + e.getMessage());
            response.put("hint", "Ensure Kafka Connect is running at http://localhost:8083");

            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/day07/connectors/plugins")
    public ResponseEntity<Map<String, Object>> listConnectorPlugins() {
        logger.info("🔌 Listing connector plugins via Web API");

        try {
            Map<String, Object> pluginsInfo = day07Service.listConnectorPlugins();

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", pluginsInfo);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Failed to list connector plugins: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to list connector plugins: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/day07/connectors/list")
    public ResponseEntity<Map<String, Object>> listConnectors() {
        logger.info("📋 Listing connectors via Web API");

        try {
            Map<String, Object> connectorsInfo = day07Service.listConnectors();

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", connectorsInfo);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Failed to list connectors: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to list connectors: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/day07/connectors/create/user-activity-source")
    public ResponseEntity<Map<String, Object>> createUserActivitySourceConnector() {
        logger.info("🔗 Creating User Activity JDBC Source Connector via Web API");

        try {
            Map<String, Object> result = day07Service.createUserActivitySourceConnector();

            Map<String, Object> response = new HashMap<>();
            response.put("data", result);
            response.put("connectorType", "JDBC Source");
            response.put("source", "PostgreSQL user_activity_log table");
            response.put("destination", "Kafka topic: jdbc-user_activity_log");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Failed to create connector: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to create connector: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/day07/connectors/create/product-events-source")
    public ResponseEntity<Map<String, Object>> createProductEventsSourceConnector() {
        logger.info("🔗 Creating Product Events JDBC Source Connector via Web API");

        try {
            Map<String, Object> result = day07Service.createProductEventsSourceConnector();

            Map<String, Object> response = new HashMap<>();
            response.put("data", result);
            response.put("connectorType", "JDBC Source");
            response.put("source", "PostgreSQL product_events table");
            response.put("destination", "Kafka topic: jdbc-product_events");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Failed to create connector: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to create connector: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/day07/connectors/create/order-events-sink")
    public ResponseEntity<Map<String, Object>> createOrderEventsSinkConnector() {
        logger.info("🔗 Creating Order Events JDBC Sink Connector via Web API");

        try {
            Map<String, Object> result = day07Service.createOrderEventsSinkConnector();

            Map<String, Object> response = new HashMap<>();
            response.put("data", result);
            response.put("connectorType", "JDBC Sink");
            response.put("source", "Kafka topic: order-events-avro");
            response.put("destination", "PostgreSQL orders table");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Failed to create connector: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to create connector: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/day07/connectors/{name}/status")
    public ResponseEntity<Map<String, Object>> getConnectorStatus(@PathVariable String name) {
        logger.info("📊 Getting connector status for '{}' via Web API", name);

        try {
            Map<String, Object> statusInfo = day07Service.checkConnectorStatus(name);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("connector", name);
            response.put("data", statusInfo);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Failed to get connector status: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to get connector status: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/day07/connectors/{name}/pause")
    public ResponseEntity<Map<String, Object>> pauseConnector(@PathVariable String name) {
        logger.info("⏸️  Pausing connector '{}' via Web API", name);

        try {
            Map<String, Object> result = day07Service.pauseConnector(name);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("❌ Failed to pause connector: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to pause connector: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/day07/connectors/{name}/resume")
    public ResponseEntity<Map<String, Object>> resumeConnector(@PathVariable String name) {
        logger.info("▶️  Resuming connector '{}' via Web API", name);

        try {
            Map<String, Object> result = day07Service.resumeConnector(name);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("❌ Failed to resume connector: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to resume connector: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/day07/connectors/{name}/restart")
    public ResponseEntity<Map<String, Object>> restartConnector(@PathVariable String name) {
        logger.info("🔄 Restarting connector '{}' via Web API", name);

        try {
            Map<String, Object> result = day07Service.restartConnector(name);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("❌ Failed to restart connector: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to restart connector: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    @DeleteMapping("/day07/connectors/{name}/delete")
    public ResponseEntity<Map<String, Object>> deleteConnector(@PathVariable String name) {
        logger.info("🗑️  Deleting connector '{}' via Web API", name);

        try {
            Map<String, Object> result = day07Service.deleteConnector(name);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("❌ Failed to delete connector: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to delete connector: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    // ===== Day 08 Advanced Topics Endpoints =====

    @PostMapping("/day08/demo")
    public ResponseEntity<Map<String, String>> runDay08Demo() {
        logger.info("🎓 Running Day 8 Advanced Topics Demo via Web API");

        try {
            day08Service.demonstrateAdvancedTopics();

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Day 8 Advanced Topics demonstration completed successfully");
            response.put("module", "Day08Advanced");
            response.put("info", "Security, Monitoring, and Production Operations demonstrated");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Day 8 demo failed: {}", e.getMessage(), e);

            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Day 8 demonstration failed: " + e.getMessage());
            response.put("module", "Day08Advanced");

            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/day08/status")
    public ResponseEntity<Map<String, Object>> getDay08SystemStatus() {
        logger.info("📊 Getting system status via Web API");

        try {
            Map<String, Object> systemStatus = day08Service.getSystemStatus();

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("systemStatus", systemStatus);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Failed to get system status: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to get system status: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/day08/metrics/cluster")
    public ResponseEntity<Map<String, Object>> getDay08ClusterMetrics() {
        logger.info("📊 Getting cluster metrics via Web API");

        try {
            Map<String, Object> metrics = day08Service.getClusterMetrics();

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("metrics", metrics);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Failed to get cluster metrics: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to get cluster metrics: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/day08/metrics/consumer-lag")
    public ResponseEntity<Map<String, Object>> getDay08ConsumerLag() {
        logger.info("📊 Getting consumer lag via Web API");

        try {
            Map<String, Object> consumerLag = day08Service.monitorConsumerLag();

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("consumerLag", consumerLag);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Failed to get consumer lag: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to get consumer lag: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/day08/config/security")
    public ResponseEntity<Map<String, Object>> getDay08SecurityConfig() {
        logger.info("🔒 Getting security configuration via Web API");

        try {
            Map<String, Object> ssl = day08Service.getSslConfiguration();
            Map<String, Object> sasl = day08Service.getSaslConfiguration();

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("ssl", ssl);
            response.put("sasl", sasl);
            response.put("info", "Security configurations for SSL/TLS and SASL authentication");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Failed to get security configuration: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to get security configuration: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/day08/config/production")
    public ResponseEntity<Map<String, Object>> getDay08ProductionConfig() {
        logger.info("⚙️  Getting production configuration via Web API");

        try {
            Map<String, Object> producer = day08Service.getProductionProducerConfig();
            Map<String, Object> consumer = day08Service.getProductionConsumerConfig();

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("producer", producer);
            response.put("consumer", consumer);
            response.put("info", "Production-ready configurations for maximum reliability and performance");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Failed to get production configuration: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to get production configuration: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    // ===== EventMart Progressive Project Endpoints =====

    @PostMapping("/eventmart/demo")
    public ResponseEntity<Map<String, String>> startEventMartDemo() {
        logger.info("🎭 Starting EventMart Progressive Project Demo via Web API");

        try {
            boolean success = eventMartService.startEventMartDemo();

            Map<String, String> response = new HashMap<>();
            if (success) {
                response.put("status", "success");
                response.put("message", "EventMart demo started successfully");
                response.put("info", "Watch the logs for real-time event streaming");
            } else {
                response.put("status", "warning");
                response.put("message", "EventMart demo is already running or failed to start");
            }
            response.put("module", "EventMart");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ EventMart demo failed: {}", e.getMessage(), e);

            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "EventMart demo failed: " + e.getMessage());
            response.put("module", "EventMart");

            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/eventmart/stop")
    public ResponseEntity<Map<String, String>> stopEventMartDemo() {
        logger.info("🛑 Stopping EventMart demo via Web API");

        boolean success = eventMartService.stopEventMartDemo();

        Map<String, String> response = new HashMap<>();
        response.put("status", success ? "success" : "warning");
        response.put("message", success ? "EventMart demo stopped successfully" : "EventMart demo was not running");
        response.put("module", "EventMart");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/eventmart/status")
    public ResponseEntity<Map<String, Object>> getEventMartStatus() {
        EventMartService.EventMartStatus status = eventMartService.getEventMartStatus();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("demoRunning", status.demoRunning);
        response.put("topicsCreated", status.topicsCreated);
        response.put("metrics", Map.of(
            "usersRegistered", status.usersRegistered,
            "productsCreated", status.productsCreated,
            "ordersPlaced", status.ordersPlaced,
            "paymentsCompleted", status.paymentsCompleted,
            "totalRevenue", String.format("$%.2f", status.totalRevenue)
        ));
        response.put("timestamp", status.timestamp);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/eventmart/topics")
    public ResponseEntity<Map<String, String>> createEventMartTopics() {
        logger.info("🏗️  Creating EventMart topics via Web API");

        try {
            boolean success = eventMartService.createEventMartTopics();

            Map<String, String> response = new HashMap<>();
            response.put("status", success ? "success" : "error");
            response.put("message", success ? "EventMart topics created successfully" : "Failed to create EventMart topics");
            response.put("module", "EventMart");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ EventMart topic creation failed: {}", e.getMessage(), e);

            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "EventMart topic creation failed: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/eventmart/simulate/user")
    public ResponseEntity<Map<String, String>> simulateUserRegistration(
            @RequestParam String userId,
            @RequestParam String email,
            @RequestParam String name) {

        // Validate inputs
        if (userId == null || userId.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("status", "error",
                           "message", "userId cannot be empty",
                           "hint", "Provide a unique user identifier"));
        }

        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            return ResponseEntity.badRequest()
                .body(Map.of("status", "error",
                           "message", "Invalid email format",
                           "hint", "Provide a valid email address"));
        }

        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("status", "error",
                           "message", "name cannot be empty",
                           "hint", "Provide a user name"));
        }

        logger.info("👤 Simulating user registration via Web API: {}", userId);

        boolean success = eventMartService.simulateUserRegistration(userId, email, name);

        Map<String, String> response = new HashMap<>();
        response.put("status", success ? "success" : "error");
        response.put("message", success ? "User registration event sent" : "Failed to send user registration event");
        response.put("userId", userId);
        response.put("email", email);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/eventmart/simulate/product")
    public ResponseEntity<Map<String, String>> simulateProductCreation(
            @RequestParam String productId,
            @RequestParam String name,
            @RequestParam String category,
            @RequestParam double price) {

        // Validate inputs
        if (productId == null || productId.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("status", "error",
                           "message", "productId cannot be empty",
                           "hint", "Provide a unique product identifier"));
        }

        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("status", "error",
                           "message", "Product name cannot be empty",
                           "hint", "Provide a product name"));
        }

        if (category == null || category.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("status", "error",
                           "message", "Category cannot be empty",
                           "hint", "Provide a product category"));
        }

        if (price < 0) {
            return ResponseEntity.badRequest()
                .body(Map.of("status", "error",
                           "message", "Price cannot be negative: " + price,
                           "hint", "Provide a valid positive price"));
        }

        logger.info("📦 Simulating product creation via Web API: {}", productId);

        boolean success = eventMartService.simulateProductCreation(productId, name, category, price);

        Map<String, String> response = new HashMap<>();
        response.put("status", success ? "success" : "error");
        response.put("message", success ? "Product creation event sent" : "Failed to send product creation event");
        response.put("productId", productId);
        response.put("name", name);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/eventmart/simulate/order")
    public ResponseEntity<Map<String, String>> simulateOrderPlacement(
            @RequestParam String orderId,
            @RequestParam String userId,
            @RequestParam double amount) {

        // Validate inputs
        if (orderId == null || orderId.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("status", "error",
                           "message", "orderId cannot be empty",
                           "hint", "Provide a unique order identifier"));
        }

        if (userId == null || userId.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("status", "error",
                           "message", "userId cannot be empty",
                           "hint", "Provide a valid user identifier"));
        }

        if (amount <= 0) {
            return ResponseEntity.badRequest()
                .body(Map.of("status", "error",
                           "message", "Order amount must be positive: " + amount,
                           "hint", "Provide a valid positive amount"));
        }

        logger.info("🛒 Simulating order placement via Web API: {}", orderId);

        boolean success = eventMartService.simulateOrderPlacement(orderId, userId, amount);

        Map<String, String> response = new HashMap<>();
        response.put("status", success ? "success" : "error");
        response.put("message", success ? "Order placement event sent" : "Failed to send order placement event");
        response.put("orderId", orderId);
        response.put("userId", userId);
        response.put("amount", String.format("$%.2f", amount));

        return ResponseEntity.ok(response);
    }

    // ===== Profile Information Endpoint =====

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfileInfo() {
        logger.info("📋 Getting profile information via Web API");

        try {
            ProfileConfiguration.ProfileInfo profileInfo = profileConfiguration.getProfileInfo();

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("applicationName", profileInfo.applicationName);
            response.put("activeProfiles", profileInfo.activeProfiles);
            response.put("defaultProfiles", profileInfo.defaultProfiles);
            response.put("features", Map.of(
                "debugMode", profileInfo.debugMode,
                "autoTopicCreation", profileInfo.autoTopicCreation,
                "webInterfaceEnabled", profileInfo.webInterfaceEnabled
            ));
            response.put("timestamp", java.time.Instant.now().toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Failed to get profile information: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to get profile information: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    // ===== Training Reset Endpoint =====

    /**
     * Reset training environment - useful when students want to start fresh
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetTrainingEnvironment(
            @RequestParam(defaultValue = "false") boolean deleteAllTopics) {

        logger.info("🔄 Resetting training environment (deleteAllTopics={})", deleteAllTopics);

        Map<String, Object> response = new HashMap<>();
        Map<String, String> actions = new HashMap<>();

        try {
            // Stop EventMart demo if running
            if (eventMartService.isDemoRunning()) {
                eventMartService.stopEventMartDemo();
                actions.put("eventMartDemo", "stopped");
            } else {
                actions.put("eventMartDemo", "not running");
            }

            // Reset consumer stats
            day04Service.resetMessageCounter();
            actions.put("consumerStats", "reset");

            // Optionally delete all training topics
            if (deleteAllTopics) {
                Set<String> topics = day01Service.listTopics();
                int deletedCount = 0;

                for (String topic : topics) {
                    // Only delete non-system topics (don't delete internal Kafka topics)
                    if (!topic.startsWith("_") &&
                        (topic.contains("training") || topic.contains("user-events") ||
                         topic.contains("eventmart") || topic.contains("test"))) {

                        if (day01Service.deleteTopic(topic)) {
                            deletedCount++;
                        }
                    }
                }
                actions.put("topicsDeleted", String.valueOf(deletedCount));
            } else {
                actions.put("topicsDeleted", "0 (deleteAllTopics=false)");
            }

            response.put("status", "success");
            response.put("message", "Training environment reset successfully");
            response.put("actions", actions);
            response.put("hint", "Use deleteAllTopics=true to also remove all training topics");
            response.put("timestamp", java.time.Instant.now().toString());

            logger.info("✅ Training environment reset completed");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Failed to reset training environment: {}", e.getMessage(), e);

            response.put("status", "error");
            response.put("message", "Failed to reset training environment: " + e.getMessage());
            response.put("partialActions", actions);

            return ResponseEntity.status(500).body(response);
        }
    }
}
