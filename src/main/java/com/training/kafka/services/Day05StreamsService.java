package com.training.kafka.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.training.kafka.config.TrainingKafkaProperties;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Spring Service for Day 5 Kafka Streams Training
 *
 * This service demonstrates real-time stream processing with Kafka Streams:
 * - Stream transformations and filtering
 * - Aggregations and windowing
 * - Stream-stream joins
 * - EventMart real-time analytics
 *
 * @author Kafka Training Course
 * @version 1.0.0
 */
@Service
public class Day05StreamsService {

    private static final Logger logger = LoggerFactory.getLogger(Day05StreamsService.class);

    private final TrainingKafkaProperties kafkaProperties;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private KafkaStreams userActivityStream;
    private KafkaStreams orderAnalyticsStream;
    private KafkaStreams fraudDetectionStream;
    private KafkaStreams eventMartRealtimeStream;

    private final AtomicBoolean userActivityRunning = new AtomicBoolean(false);
    private final AtomicBoolean orderAnalyticsRunning = new AtomicBoolean(false);
    private final AtomicBoolean fraudDetectionRunning = new AtomicBoolean(false);
    private final AtomicBoolean eventMartRunning = new AtomicBoolean(false);

    /**
     * Constructor with dependency injection
     */
    public Day05StreamsService(TrainingKafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
        logger.info("🌊 Day05StreamsService initialized with Spring-managed configuration");
    }

    /**
     * Initialize Kafka Streams configurations
     */
    @PostConstruct
    public void initialize() {
        logger.info("🚀 Initializing Kafka Streams applications");
        // Streams will be started on-demand via REST API
    }

    // ===== User Activity Stream =====

    /**
     * Start user activity aggregation stream
     * Aggregates user events in 1-hour windows
     */
    public boolean startUserActivityStream() {
        if (userActivityRunning.get()) {
            logger.warn("⚠️  User activity stream is already running");
            return false;
        }

        try {
            logger.info("🌊 Starting user activity aggregation stream");

            Properties props = createStreamProperties("user-activity-stream");
            StreamsBuilder builder = new StreamsBuilder();

            // Build topology
            buildUserActivityTopology(builder);

            userActivityStream = new KafkaStreams(builder.build(), props);
            setupStreamHandlers(userActivityStream, "UserActivity");

            userActivityStream.start();
            userActivityRunning.set(true);

            logger.info("✅ User activity stream started successfully");
            return true;

        } catch (Exception e) {
            logger.error("❌ Failed to start user activity stream", e);
            userActivityRunning.set(false);
            return false;
        }
    }

    /**
     * Build user activity aggregation topology
     */
    private void buildUserActivityTopology(StreamsBuilder builder) {
        KStream<String, String> userEvents = builder.stream("user-events");

        userEvents
            // Filter valid JSON events
            .filter((key, value) -> isValidJson(value))

            // Extract user ID as key
            .selectKey((key, value) -> extractUserId(value))

            // Group by user and window (1 hour)
            .groupByKey()
            .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofHours(1)))

            // Count events per user per window
            .count(Materialized.as("user-activity-counts"))

            // Convert to stream
            .toStream()

            // Log results
            .peek((windowedKey, count) ->
                logger.info("📊 User activity: user={}, window={}, events={}",
                    windowedKey.key(),
                    formatWindow(windowedKey.window()),
                    count))

            // Send to output topic
            .map((windowedKey, count) -> {
                String output = String.format(
                    "{\"user_id\":\"%s\",\"window_start\":%d,\"window_end\":%d,\"event_count\":%d,\"timestamp\":%d}",
                    windowedKey.key(),
                    windowedKey.window().start(),
                    windowedKey.window().end(),
                    count,
                    System.currentTimeMillis()
                );
                return KeyValue.pair(windowedKey.key(), output);
            })
            .to("user-activity-summary");
    }

    // ===== Order Analytics Stream =====

    /**
     * Start order analytics stream
     * Calculates real-time order metrics in 15-minute windows
     */
    public boolean startOrderAnalyticsStream() {
        if (orderAnalyticsRunning.get()) {
            logger.warn("⚠️  Order analytics stream is already running");
            return false;
        }

        try {
            logger.info("🌊 Starting order analytics stream");

            Properties props = createStreamProperties("order-analytics-stream");
            StreamsBuilder builder = new StreamsBuilder();

            // Build topology
            buildOrderAnalyticsTopology(builder);

            orderAnalyticsStream = new KafkaStreams(builder.build(), props);
            setupStreamHandlers(orderAnalyticsStream, "OrderAnalytics");

            orderAnalyticsStream.start();
            orderAnalyticsRunning.set(true);

            logger.info("✅ Order analytics stream started successfully");
            return true;

        } catch (Exception e) {
            logger.error("❌ Failed to start order analytics stream", e);
            orderAnalyticsRunning.set(false);
            return false;
        }
    }

    /**
     * Build order analytics topology
     */
    private void buildOrderAnalyticsTopology(StreamsBuilder builder) {
        KStream<String, String> orderEvents = builder.stream("eventmart-orders");

        orderEvents
            .filter((key, value) -> isValidJson(value))

            // Extract order amount
            .mapValues(this::enrichWithAmount)

            // Group all orders together
            .groupBy((key, value) -> "all-orders")
            .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(15)))

            // Aggregate: count and sum
            .aggregate(
                () -> new OrderMetrics(),
                (key, value, metrics) -> {
                    double amount = extractAmount(value);
                    metrics.incrementCount();
                    metrics.addRevenue(amount);
                    return metrics;
                },
                Materialized.with(Serdes.String(), new OrderMetricsSerde())
            )

            .toStream()

            // Log and alert on high volumes
            .peek((windowedKey, metrics) -> {
                logger.info("📊 Order metrics: window={}, orders={}, revenue=${:.2f}, avg=${:.2f}",
                    formatWindow(windowedKey.window()),
                    metrics.getOrderCount(),
                    metrics.getTotalRevenue(),
                    metrics.getAverageOrderValue());

                if (metrics.getTotalRevenue() > 10000.0) {
                    logger.warn("💰 High revenue window detected: ${:.2f}", metrics.getTotalRevenue());
                }
            })

            // Send to output topic
            .map((windowedKey, metrics) -> {
                try {
                    String output = objectMapper.writeValueAsString(Map.of(
                        "window_start", windowedKey.window().start(),
                        "window_end", windowedKey.window().end(),
                        "order_count", metrics.getOrderCount(),
                        "total_revenue", metrics.getTotalRevenue(),
                        "average_order_value", metrics.getAverageOrderValue(),
                        "timestamp", System.currentTimeMillis()
                    ));
                    return KeyValue.pair("analytics", output);
                } catch (Exception e) {
                    logger.error("Failed to serialize metrics", e);
                    return KeyValue.pair("analytics", "{}");
                }
            })
            .to("order-analytics");
    }

    // ===== Fraud Detection Stream =====

    /**
     * Start fraud detection stream
     * Joins user events with orders to detect suspicious patterns
     */
    public boolean startFraudDetectionStream() {
        if (fraudDetectionRunning.get()) {
            logger.warn("⚠️  Fraud detection stream is already running");
            return false;
        }

        try {
            logger.info("🌊 Starting fraud detection stream");

            Properties props = createStreamProperties("fraud-detection-stream");
            StreamsBuilder builder = new StreamsBuilder();

            // Build topology
            buildFraudDetectionTopology(builder);

            fraudDetectionStream = new KafkaStreams(builder.build(), props);
            setupStreamHandlers(fraudDetectionStream, "FraudDetection");

            fraudDetectionStream.start();
            fraudDetectionRunning.set(true);

            logger.info("✅ Fraud detection stream started successfully");
            return true;

        } catch (Exception e) {
            logger.error("❌ Failed to start fraud detection stream", e);
            fraudDetectionRunning.set(false);
            return false;
        }
    }

    /**
     * Build fraud detection topology with stream joins
     */
    private void buildFraudDetectionTopology(StreamsBuilder builder) {
        KStream<String, String> userEvents = builder.stream("user-events");
        KStream<String, String> orderEvents = builder.stream("eventmart-orders");

        // Rekey both streams by user_id
        KStream<String, String> usersByUserId = userEvents
            .filter((key, value) -> isValidJson(value))
            .selectKey((key, value) -> extractUserId(value));

        KStream<String, String> ordersByUserId = orderEvents
            .filter((key, value) -> isValidJson(value))
            .selectKey((key, value) -> extractUserId(value));

        // Join within 5-minute window
        usersByUserId
            .join(
                ordersByUserId,
                this::joinUserWithOrder,
                JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofMinutes(5)),
                StreamJoined.with(Serdes.String(), Serdes.String(), Serdes.String())
            )

            // Detect suspicious patterns
            .filter(this::isSuspiciousActivity)

            // Create fraud alerts
            .mapValues(this::createFraudAlert)

            // Log alerts
            .peek((key, alert) ->
                logger.warn("🚨 FRAUD ALERT: user={}, details={}", key, alert))

            // Send to fraud alerts topic
            .to("fraud-alerts");
    }

    // ===== EventMart Real-time Analytics =====

    /**
     * Start EventMart comprehensive real-time analytics
     * Combines all EventMart topics for complete business insights
     */
    public boolean startEventMartRealtimeAnalytics() {
        if (eventMartRunning.get()) {
            logger.warn("⚠️  EventMart real-time analytics is already running");
            return false;
        }

        try {
            logger.info("🌊 Starting EventMart real-time analytics stream");

            Properties props = createStreamProperties("eventmart-realtime-analytics");
            StreamsBuilder builder = new StreamsBuilder();

            // Build comprehensive EventMart topology
            buildEventMartRealtimeTopology(builder);

            eventMartRealtimeStream = new KafkaStreams(builder.build(), props);
            setupStreamHandlers(eventMartRealtimeStream, "EventMartRealtime");

            eventMartRealtimeStream.start();
            eventMartRunning.set(true);

            logger.info("✅ EventMart real-time analytics started successfully");
            return true;

        } catch (Exception e) {
            logger.error("❌ Failed to start EventMart real-time analytics", e);
            eventMartRunning.set(false);
            return false;
        }
    }

    /**
     * Build comprehensive EventMart real-time analytics topology
     */
    private void buildEventMartRealtimeTopology(StreamsBuilder builder) {
        // Read all EventMart streams
        KStream<String, String> users = builder.stream("eventmart-users");
        KStream<String, String> orders = builder.stream("eventmart-orders");
        KStream<String, String> payments = builder.stream("eventmart-payments");

        // 1. Real-time revenue tracking (5-minute windows)
        orders
            .filter((key, value) -> isValidJson(value))
            .groupBy((key, value) -> "total")
            .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(5)))
            .aggregate(
                () -> 0.0,
                (key, value, total) -> total + extractAmount(value),
                Materialized.with(Serdes.String(), Serdes.Double())
            )
            .toStream()
            .peek((windowedKey, revenue) ->
                logger.info("💰 Real-time revenue: window={}, revenue=${:.2f}",
                    formatWindow(windowedKey.window()), revenue))
            .map((windowedKey, revenue) -> KeyValue.pair("revenue", String.format("{\"revenue\":%.2f}", revenue)))
            .to("eventmart-analytics");

        // 2. User registration rate (hourly)
        users
            .filter((key, value) -> isValidJson(value))
            .groupBy((key, value) -> "registrations")
            .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofHours(1)))
            .count()
            .toStream()
            .peek((windowedKey, count) ->
                logger.info("👥 User registrations: window={}, count={}",
                    formatWindow(windowedKey.window()), count))
            .to("user-registration-metrics");

        // 3. Payment success rate
        payments
            .filter((key, value) -> isValidJson(value))
            .groupBy((key, value) -> "payment-metrics")
            .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(15)))
            .aggregate(
                () -> new PaymentMetrics(),
                (key, value, metrics) -> {
                    metrics.incrementTotal();
                    if (isPaymentSuccessful(value)) {
                        metrics.incrementSuccessful();
                    }
                    return metrics;
                },
                Materialized.with(Serdes.String(), new PaymentMetricsSerde())
            )
            .toStream()
            .peek((windowedKey, metrics) ->
                logger.info("💳 Payment metrics: window={}, success_rate={:.2f}%",
                    formatWindow(windowedKey.window()), metrics.getSuccessRate()))
            .to("payment-metrics");
    }

    // ===== Stop Methods =====

    public boolean stopUserActivityStream() {
        return stopStream(userActivityStream, userActivityRunning, "User Activity");
    }

    public boolean stopOrderAnalyticsStream() {
        return stopStream(orderAnalyticsStream, orderAnalyticsRunning, "Order Analytics");
    }

    public boolean stopFraudDetectionStream() {
        return stopStream(fraudDetectionStream, fraudDetectionRunning, "Fraud Detection");
    }

    public boolean stopEventMartRealtimeAnalytics() {
        return stopStream(eventMartRealtimeStream, eventMartRunning, "EventMart Realtime");
    }

    private boolean stopStream(KafkaStreams stream, AtomicBoolean running, String name) {
        if (!running.get()) {
            logger.warn("⚠️  {} stream is not running", name);
            return false;
        }

        try {
            logger.info("🛑 Stopping {} stream", name);
            if (stream != null) {
                stream.close(Duration.ofSeconds(10));
            }
            running.set(false);
            logger.info("✅ {} stream stopped successfully", name);
            return true;
        } catch (Exception e) {
            logger.error("❌ Failed to stop {} stream", name, e);
            return false;
        }
    }

    // ===== Status Methods =====

    public Map<String, Object> getStreamsStatus() {
        Map<String, Object> status = new HashMap<>();

        status.put("userActivityStream", Map.of(
            "running", userActivityRunning.get(),
            "state", userActivityStream != null ? userActivityStream.state().toString() : "NOT_CREATED"
        ));

        status.put("orderAnalyticsStream", Map.of(
            "running", orderAnalyticsRunning.get(),
            "state", orderAnalyticsStream != null ? orderAnalyticsStream.state().toString() : "NOT_CREATED"
        ));

        status.put("fraudDetectionStream", Map.of(
            "running", fraudDetectionRunning.get(),
            "state", fraudDetectionStream != null ? fraudDetectionStream.state().toString() : "NOT_CREATED"
        ));

        status.put("eventMartRealtimeStream", Map.of(
            "running", eventMartRunning.get(),
            "state", eventMartRealtimeStream != null ? eventMartRealtimeStream.state().toString() : "NOT_CREATED"
        ));

        return status;
    }

    /**
     * Run comprehensive Day 5 demonstration
     */
    public void demonstrateKafkaStreams() {
        logger.info("🎓 Starting Day 5 Kafka Streams Demonstration");

        try {
            logger.info("1️⃣  Kafka Streams Overview");
            logger.info("   - Real-time stream processing");
            logger.info("   - Stateful transformations with windowing");
            logger.info("   - Stream-stream joins");
            logger.info("   - Exactly-once processing guarantees");

            logger.info("2️⃣  Available Stream Applications:");
            logger.info("   - User Activity Stream: Aggregates user events in 1-hour windows");
            logger.info("   - Order Analytics Stream: Real-time order metrics in 15-minute windows");
            logger.info("   - Fraud Detection Stream: Joins user+order events for suspicious patterns");
            logger.info("   - EventMart Realtime: Comprehensive business analytics");

            logger.info("3️⃣  Start streams via REST API:");
            logger.info("   POST /api/training/day05/streams/user-activity/start");
            logger.info("   POST /api/training/day05/streams/order-analytics/start");
            logger.info("   POST /api/training/day05/streams/fraud-detection/start");
            logger.info("   POST /api/training/day05/streams/eventmart/start");

            logger.info("✅ Day 5 Kafka Streams Demonstration completed");
            logger.info("💡 Tip: Start streams and send events to see real-time processing!");

        } catch (Exception e) {
            logger.error("❌ Day 5 Kafka Streams Demonstration failed", e);
        }
    }

    // ===== Helper Methods =====

    private Properties createStreamProperties(String applicationId) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getKafka().getBootstrapServers());
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        // Performance and reliability settings
        props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 2);
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2);
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10000);
        props.put(StreamsConfig.STATESTORE_CACHE_MAX_BYTES_CONFIG, 10 * 1024 * 1024L);

        return props;
    }

    private void setupStreamHandlers(KafkaStreams streams, String name) {
        streams.setUncaughtExceptionHandler((exception) -> {
            logger.error("❌ Uncaught exception in {} stream", name, exception);
            return org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_CLIENT;
        });

        streams.setStateListener((newState, oldState) -> {
            logger.info("📊 {} stream state changed: {} → {}", name, oldState, newState);
        });
    }

    private boolean isValidJson(String value) {
        try {
            objectMapper.readTree(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String extractUserId(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            JsonNode userIdNode = node.path("user_id");
            return userIdNode.isMissingNode() ? "unknown" : userIdNode.asText();
        } catch (Exception e) {
            return "unknown";
        }
    }

    private double extractAmount(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            return node.path("amount").asDouble(0.0);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private String enrichWithAmount(String json) {
        // Already contains amount, just pass through
        return json;
    }

    private String formatWindow(org.apache.kafka.streams.kstream.Window window) {
        return String.format("[%d - %d]", window.start(), window.end());
    }

    private String joinUserWithOrder(String userEvent, String orderEvent) {
        try {
            JsonNode user = objectMapper.readTree(userEvent);
            JsonNode order = objectMapper.readTree(orderEvent);

            Map<String, Object> joined = new HashMap<>();
            joined.put("user_event", user);
            joined.put("order_event", order);
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
            double orderAmount = joined.path("order_event").path("amount").asDouble(0.0);

            // Simple fraud detection: orders > $1000 are suspicious
            return orderAmount > 1000.0;

        } catch (Exception e) {
            return false;
        }
    }

    private String createFraudAlert(String joinedEvent) {
        try {
            JsonNode joined = objectMapper.readTree(joinedEvent);

            Map<String, Object> alert = new HashMap<>();
            alert.put("alert_type", "SUSPICIOUS_ACTIVITY");
            alert.put("severity", "HIGH");
            alert.put("timestamp", System.currentTimeMillis());
            alert.put("details", joined);

            return objectMapper.writeValueAsString(alert);
        } catch (Exception e) {
            logger.error("Failed to create fraud alert", e);
            return "{}";
        }
    }

    private boolean isPaymentSuccessful(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            String status = node.path("status").asText();
            return "SUCCESS".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Cleanup on shutdown
     */
    @PreDestroy
    public void cleanup() {
        logger.info("🧹 Cleaning up Kafka Streams");

        stopUserActivityStream();
        stopOrderAnalyticsStream();
        stopFraudDetectionStream();
        stopEventMartRealtimeAnalytics();

        logger.info("✅ Kafka Streams cleanup completed");
    }

    // ===== Inner Classes for Aggregations =====

    public static class OrderMetrics {
        private long orderCount = 0;
        private double totalRevenue = 0.0;

        public void incrementCount() { orderCount++; }
        public void addRevenue(double amount) { totalRevenue += amount; }

        public long getOrderCount() { return orderCount; }
        public double getTotalRevenue() { return totalRevenue; }
        public double getAverageOrderValue() {
            return orderCount > 0 ? totalRevenue / orderCount : 0.0;
        }

        public void setOrderCount(long count) { this.orderCount = count; }
        public void setTotalRevenue(double revenue) { this.totalRevenue = revenue; }
    }

    public static class PaymentMetrics {
        private long totalPayments = 0;
        private long successfulPayments = 0;

        public void incrementTotal() { totalPayments++; }
        public void incrementSuccessful() { successfulPayments++; }

        public long getTotalPayments() { return totalPayments; }
        public long getSuccessfulPayments() { return successfulPayments; }
        public double getSuccessRate() {
            return totalPayments > 0 ? (successfulPayments * 100.0) / totalPayments : 0.0;
        }

        public void setTotalPayments(long total) { this.totalPayments = total; }
        public void setSuccessfulPayments(long successful) { this.successfulPayments = successful; }
    }

    // Serdes for custom types
    private static class OrderMetricsSerde extends org.apache.kafka.common.serialization.Serdes.WrapperSerde<OrderMetrics> {
        public OrderMetricsSerde() {
            super(new org.apache.kafka.common.serialization.Serializer<OrderMetrics>() {
                private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
                @Override
                public byte[] serialize(String topic, OrderMetrics data) {
                    try {
                        return mapper.writeValueAsBytes(data);
                    } catch (Exception e) {
                        return new byte[0];
                    }
                }
            }, new org.apache.kafka.common.serialization.Deserializer<OrderMetrics>() {
                private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
                @Override
                public OrderMetrics deserialize(String topic, byte[] data) {
                    try {
                        return mapper.readValue(data, OrderMetrics.class);
                    } catch (Exception e) {
                        return new OrderMetrics();
                    }
                }
            });
        }
    }

    private static class PaymentMetricsSerde extends org.apache.kafka.common.serialization.Serdes.WrapperSerde<PaymentMetrics> {
        public PaymentMetricsSerde() {
            super(new org.apache.kafka.common.serialization.Serializer<PaymentMetrics>() {
                private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
                @Override
                public byte[] serialize(String topic, PaymentMetrics data) {
                    try {
                        return mapper.writeValueAsBytes(data);
                    } catch (Exception e) {
                        return new byte[0];
                    }
                }
            }, new org.apache.kafka.common.serialization.Deserializer<PaymentMetrics>() {
                private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
                @Override
                public PaymentMetrics deserialize(String topic, byte[] data) {
                    try {
                        return mapper.readValue(data, PaymentMetrics.class);
                    } catch (Exception e) {
                        return new PaymentMetrics();
                    }
                }
            });
        }
    }
}
