package com.training.kafka.Day05Streams;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Properties;

/**
 * Day 5: Kafka Streams Processing
 *
 * This class demonstrates real-time stream processing with Kafka Streams:
 * - Stream transformations and filtering
 * - Aggregations and windowing
 * - Stream-stream joins
 * - State stores and fault tolerance
 */
public class StreamProcessor {
    private static final Logger logger = LoggerFactory.getLogger(StreamProcessor.class);

    private static final String USER_EVENTS_TOPIC = "user-events";
    private static final String ORDER_EVENTS_TOPIC = "order-events";
    private static final String USER_ACTIVITY_SUMMARY_TOPIC = "user-activity-summary";
    private static final String FRAUD_ALERTS_TOPIC = "fraud-alerts";

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private KafkaStreams streams;

    public StreamProcessor(String bootstrapServers) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "stream-processor-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        // Performance and reliability settings
        props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 2);
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2);
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10000);
        props.put(StreamsConfig.STATESTORE_CACHE_MAX_BYTES_CONFIG, 10 * 1024 * 1024L);

        // Build the topology
        StreamsBuilder builder = new StreamsBuilder();
        buildTopology(builder);

        this.streams = new KafkaStreams(builder.build(), props);
    }

    /**
     * Build the stream processing topology
     */
    private void buildTopology(StreamsBuilder builder) {
        // Create streams from topics
        KStream<String, String> userEvents = builder.stream(USER_EVENTS_TOPIC);
        KStream<String, String> orderEvents = builder.stream(ORDER_EVENTS_TOPIC);

        // Process user events
        processUserEvents(userEvents);

        // Process order events
        processOrderEvents(orderEvents);

        // Join streams for fraud detection
        detectFraudulentActivity(userEvents, orderEvents);

        // Aggregate user activity
        aggregateUserActivity(userEvents);
    }

    /**
     * Process and filter user events
     */
    private void processUserEvents(KStream<String, String> userEvents) {
        userEvents
            // Filter valid JSON events
            .filter((key, value) -> isValidJson(value))

            // Transform and enrich events
            .mapValues(this::enrichUserEvent)

            // Filter high-value events
            .filter((key, value) -> isHighValueEvent(value))

            // Log processed events
            .peek((key, value) ->
                logger.info("Processed high-value user event: key={}, value={}", key, value))

            // Split stream by event type
            .split(Named.as("user-events-"))
            .branch((key, value) -> getEventType(value).equals("login"),
                   Branched.as("login"))
            .branch((key, value) -> getEventType(value).equals("purchase"),
                   Branched.as("purchase"))
            .defaultBranch(Branched.as("other"));
    }

    /**
     * Process order events and detect patterns
     */
    private void processOrderEvents(KStream<String, String> orderEvents) {
        orderEvents
            .filter((key, value) -> isValidJson(value))
            .mapValues(this::enrichOrderEvent)

            // Group by user and create windowed aggregations
            .groupBy((key, value) -> getUserIdFromOrder(value))
            .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(15)))
            .aggregate(
                () -> 0.0, // Initial value
                (aggKey, newValue, aggValue) -> aggValue + getOrderAmount(newValue), // Aggregator
                Materialized.with(Serdes.String(), Serdes.Double())
            )

            // Convert back to stream
            .toStream()

            // Filter high-value windows
            .filter((windowedKey, totalAmount) -> totalAmount > 1000.0)

            // Alert on high spending
            .peek((windowedKey, totalAmount) ->
                logger.warn("High spending detected for user {} in window {}: ${}",
                    windowedKey.key(), windowedKey.window(), totalAmount));
    }

    /**
     * Join user events and order events for fraud detection
     */
    private void detectFraudulentActivity(KStream<String, String> userEvents,
                                        KStream<String, String> orderEvents) {

        // Create windowed joins to detect suspicious patterns
        userEvents
            .filter((key, value) -> getEventType(value).equals("login"))
            .join(
                orderEvents.filter((key, value) -> isValidJson(value)),
                this::joinUserEventWithOrder,
                JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofMinutes(5)),
                StreamJoined.with(Serdes.String(), Serdes.String(), Serdes.String())
            )

            // Detect suspicious patterns
            .filter(this::isSuspiciousActivity)

            // Send alerts
            .mapValues(this::createFraudAlert)
            .to(FRAUD_ALERTS_TOPIC);
    }

    /**
     * Aggregate user activity over time windows
     */
    private void aggregateUserActivity(KStream<String, String> userEvents) {
        userEvents
            .filter((key, value) -> isValidJson(value))
            .groupByKey()
            .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofHours(1)))
            .aggregate(
                this::initializeActivitySummary,
                this::aggregateActivity,
                Materialized.with(Serdes.String(), createJsonSerde())
            )
            .toStream()
            .mapValues((windowedKey, activitySummary) -> {
                try {
                    return objectMapper.writeValueAsString(activitySummary);
                } catch (Exception e) {
                    logger.error("Failed to serialize activity summary", e);
                    return "{}";
                }
            })
            .to(USER_ACTIVITY_SUMMARY_TOPIC);
    }

    // Helper methods

    private boolean isValidJson(String value) {
        try {
            objectMapper.readTree(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String enrichUserEvent(String eventJson) {
        try {
            JsonNode event = objectMapper.readTree(eventJson);
            ((com.fasterxml.jackson.databind.node.ObjectNode) event).put("processed_at", System.currentTimeMillis());
            ((com.fasterxml.jackson.databind.node.ObjectNode) event).put("processor", "stream-processor");
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            logger.error("Failed to enrich user event", e);
            return eventJson;
        }
    }

    private String enrichOrderEvent(String eventJson) {
        try {
            JsonNode event = objectMapper.readTree(eventJson);
            ((com.fasterxml.jackson.databind.node.ObjectNode) event).put("processed_at", System.currentTimeMillis());
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            logger.error("Failed to enrich order event", e);
            return eventJson;
        }
    }

    private boolean isHighValueEvent(String eventJson) {
        try {
            JsonNode event = objectMapper.readTree(eventJson);
            String action = event.path("action").asText();
            return "purchase".equals(action) || "login".equals(action);
        } catch (Exception e) {
            return false;
        }
    }

    private String getEventType(String eventJson) {
        try {
            JsonNode event = objectMapper.readTree(eventJson);
            JsonNode actionNode = event.path("action");
            return actionNode.isMissingNode() ? "unknown" : actionNode.asText();
        } catch (Exception e) {
            return "unknown";
        }
    }

    private String getUserIdFromOrder(String orderJson) {
        try {
            JsonNode order = objectMapper.readTree(orderJson);
            JsonNode userIdNode = order.path("user_id");
            return userIdNode.isMissingNode() ? "unknown" : userIdNode.asText();
        } catch (Exception e) {
            return "unknown";
        }
    }

    private double getOrderAmount(String orderJson) {
        try {
            JsonNode order = objectMapper.readTree(orderJson);
            return order.path("amount").asDouble(0.0);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private String joinUserEventWithOrder(String userEvent, String orderEvent) {
        try {
            JsonNode user = objectMapper.readTree(userEvent);
            JsonNode order = objectMapper.readTree(orderEvent);

            com.fasterxml.jackson.databind.node.ObjectNode joined = objectMapper.createObjectNode();
            joined.set("user_event", user);
            joined.set("order_event", order);
            joined.put("join_timestamp", System.currentTimeMillis());

            return objectMapper.writeValueAsString(joined);
        } catch (Exception e) {
            logger.error("Failed to join events", e);
            return "{}";
        }
    }

    private boolean isSuspiciousActivity(String key, String joinedEvent) {
        try {
            JsonNode joined = objectMapper.readTree(joinedEvent);
            JsonNode userEvent = joined.path("user_event");
            JsonNode orderEvent = joined.path("order_event");

            // Simple fraud detection logic
            double orderAmount = orderEvent.path("amount").asDouble(0.0);
            JsonNode ipNode = userEvent.path("properties").path("ip_address");
            String loginIp = ipNode.isMissingNode() ? "" : ipNode.asText();

            // Flag as suspicious if order > $500 and from unknown IP
            return orderAmount > 500.0 && (loginIp.isEmpty() || loginIp.startsWith("10."));

        } catch (Exception e) {
            return false;
        }
    }

    private String createFraudAlert(String joinedEvent) {
        try {
            JsonNode joined = objectMapper.readTree(joinedEvent);

            com.fasterxml.jackson.databind.node.ObjectNode alert = objectMapper.createObjectNode();
            alert.put("alert_type", "FRAUD_SUSPICION");
            alert.put("timestamp", System.currentTimeMillis());
            alert.put("severity", "HIGH");
            alert.set("details", joined);

            return objectMapper.writeValueAsString(alert);
        } catch (Exception e) {
            logger.error("Failed to create fraud alert", e);
            return "{}";
        }
    }

    private ActivitySummary initializeActivitySummary() {
        return new ActivitySummary();
    }

    private ActivitySummary aggregateActivity(String key, String eventJson, ActivitySummary summary) {
        try {
            JsonNode event = objectMapper.readTree(eventJson);
            String action = event.path("action").asText();

            summary.incrementAction(action);
            summary.setLastSeen(System.currentTimeMillis());

            return summary;
        } catch (Exception e) {
            logger.error("Failed to aggregate activity", e);
            return summary;
        }
    }

    private Serde<ActivitySummary> createJsonSerde() {
        return Serdes.serdeFrom(
            new ActivitySummarySerializer(),
            new ActivitySummaryDeserializer()
        );
    }

    /**
     * Start the stream processor
     */
    public void start() {
        logger.info("Starting Kafka Streams processor...");

        streams.setUncaughtExceptionHandler((exception) -> {
            logger.error("Uncaught exception in stream", exception);
            return org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_CLIENT;
        });

        streams.start();
        logger.info("Kafka Streams processor started");
    }

    /**
     * Stop the stream processor
     */
    public void stop() {
        logger.info("Stopping Kafka Streams processor...");
        streams.close(Duration.ofSeconds(10));
        logger.info("Kafka Streams processor stopped");
    }

    /**
     * Activity summary data class
     */
    public static class ActivitySummary {
        private java.util.Map<String, Long> actionCounts = new java.util.HashMap<>();
        private long lastSeen;

        public void incrementAction(String action) {
            actionCounts.merge(action, 1L, Long::sum);
        }

        public java.util.Map<String, Long> getActionCounts() { return actionCounts; }
        public void setActionCounts(java.util.Map<String, Long> actionCounts) { this.actionCounts = actionCounts; }

        public long getLastSeen() { return lastSeen; }
        public void setLastSeen(long lastSeen) { this.lastSeen = lastSeen; }
    }

    /**
     * Custom serializer for ActivitySummary
     */
    private class ActivitySummarySerializer implements Serializer<ActivitySummary> {
        @Override
        public byte[] serialize(String topic, ActivitySummary data) {
            try {
                return objectMapper.writeValueAsBytes(data);
            } catch (Exception e) {
                logger.error("Failed to serialize ActivitySummary", e);
                return new byte[0];
            }
        }
    }

    /**
     * Custom deserializer for ActivitySummary
     */
    private class ActivitySummaryDeserializer implements Deserializer<ActivitySummary> {
        @Override
        public ActivitySummary deserialize(String topic, byte[] data) {
            try {
                return objectMapper.readValue(data, ActivitySummary.class);
            } catch (Exception e) {
                logger.error("Failed to deserialize ActivitySummary", e);
                return new ActivitySummary();
            }
        }
    }

    /**
     * Main method for demonstration
     */
    public static void main(String[] args) {
        String bootstrapServers = "localhost:9092";

        StreamProcessor processor = new StreamProcessor(bootstrapServers);

        // Add shutdown hook for graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(processor::stop));

        try {
            processor.start();

            // Keep running until interrupted
            Thread.currentThread().join();

        } catch (InterruptedException e) {
            logger.info("Stream processor interrupted");
        } finally {
            processor.stop();
        }
    }
}
