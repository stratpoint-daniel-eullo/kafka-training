package com.training.kafka.Day03Producers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Day 3: Advanced Kafka Producer
 *
 * This class demonstrates advanced producer patterns including:
 * - Error handling and retries
 * - Metrics and monitoring
 * - Idempotent production
 * - Custom partitioning
 * - JSON serialization
 */
public class AdvancedProducer {
    private static final Logger logger = LoggerFactory.getLogger(AdvancedProducer.class);

    private final KafkaProducer<String, String> producer;
    private final ObjectMapper objectMapper;
    private final String topicName;
    private final AtomicLong messagesSent = new AtomicLong(0);
    private final AtomicLong messagesSucceeded = new AtomicLong(0);
    private final AtomicLong messagesFailed = new AtomicLong(0);

    public AdvancedProducer(String bootstrapServers, String topicName) {
        this.topicName = topicName;
        this.objectMapper = new ObjectMapper().findAndRegisterModules();

        Properties props = new Properties();

        // Basic configuration
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "advanced-producer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Reliability configuration
        props.put(ProducerConfig.ACKS_CONFIG, "all"); // Wait for all replicas
        props.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE); // Retry indefinitely
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 100);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000);

        // Idempotence configuration
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);

        // Performance configuration
        props.put(ProducerConfig.LINGER_MS_CONFIG, 10); // Wait 10ms to batch
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 32768); // 32KB batch size
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 67108864); // 64MB buffer
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");

        // Monitoring
        props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG,
            "io.confluent.monitoring.clients.interceptor.MonitoringProducerInterceptor");

        this.producer = new KafkaProducer<>(props);

        logger.info("Advanced producer initialized with idempotence enabled");
    }

    /**
     * Send a user event with comprehensive error handling
     */
    public Future<RecordMetadata> sendUserEvent(UserEvent event) {
        try {
            String key = event.getUserId();
            String value = objectMapper.writeValueAsString(event);

            ProducerRecord<String, String> record = new ProducerRecord<>(topicName, key, value);

            // Add headers for tracing and metadata
            record.headers().add("event-type", event.getAction().getBytes());
            record.headers().add("producer-id", "advanced-producer".getBytes());
            record.headers().add("timestamp", String.valueOf(System.currentTimeMillis()).getBytes());

            messagesSent.incrementAndGet();

            return producer.send(record, new ProducerCallback(event));

        } catch (Exception e) {
            logger.error("Failed to serialize user event: {}", event, e);
            messagesFailed.incrementAndGet();
            throw new RuntimeException("Failed to send user event", e);
        }
    }

    /**
     * Send user event synchronously with timeout
     */
    public RecordMetadata sendUserEventSync(UserEvent event, long timeoutMs) {
        try {
            Future<RecordMetadata> future = sendUserEvent(event);
            return future.get(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            logger.error("Failed to send user event synchronously: {}", event, e.getCause());
            throw new RuntimeException("Failed to send user event", e.getCause());
        } catch (Exception e) {
            logger.error("Timeout or interruption while sending user event: {}", event, e);
            throw new RuntimeException("Failed to send user event", e);
        }
    }

    /**
     * Batch send multiple events efficiently
     */
    public void sendUserEventsBatch(java.util.List<UserEvent> events) {
        logger.info("Sending batch of {} user events", events.size());

        java.util.List<Future<RecordMetadata>> futures = new java.util.ArrayList<>();

        for (UserEvent event : events) {
            futures.add(sendUserEvent(event));
        }

        // Optional: Wait for all messages to be sent
        for (Future<RecordMetadata> future : futures) {
            try {
                future.get(); // Wait for completion
            } catch (Exception e) {
                logger.error("Failed to send message in batch", e);
            }
        }

        // Ensure all messages are sent
        producer.flush();
        logger.info("Batch send completed");
    }

    /**
     * Send events with custom partitioning
     */
    public void sendToSpecificPartition(UserEvent event, int partition) {
        try {
            String value = objectMapper.writeValueAsString(event);

            ProducerRecord<String, String> record = new ProducerRecord<>(
                topicName,
                partition,
                event.getUserId(),
                value
            );

            producer.send(record, (metadata, exception) -> {
                if (exception == null) {
                    logger.info("Message sent to specific partition {}: offset={}",
                        metadata.partition(), metadata.offset());
                } else {
                    logger.error("Failed to send to partition {}", partition, exception);
                }
            });

        } catch (Exception e) {
            logger.error("Failed to send to specific partition {}", partition, e);
        }
    }

    /**
     * Get producer metrics
     */
    public ProducerMetrics getMetrics() {
        return new ProducerMetrics(
            messagesSent.get(),
            messagesSucceeded.get(),
            messagesFailed.get(),
            producer.metrics()
        );
    }

    /**
     * Flush and close producer
     */
    public void close() {
        logger.info("Closing producer. Final metrics: sent={}, succeeded={}, failed={}",
            messagesSent.get(), messagesSucceeded.get(), messagesFailed.get());

        producer.flush();
        producer.close(java.time.Duration.ofSeconds(10));
    }

    /**
     * Custom callback for handling producer results
     */
    private class ProducerCallback implements Callback {
        private final UserEvent event;

        public ProducerCallback(UserEvent event) {
            this.event = event;
        }

        @Override
        public void onCompletion(RecordMetadata metadata, Exception exception) {
            if (exception == null) {
                messagesSucceeded.incrementAndGet();
                logger.debug("Successfully sent event for user {}: partition={}, offset={}",
                    event.getUserId(), metadata.partition(), metadata.offset());
            } else {
                messagesFailed.incrementAndGet();
                logger.error("Failed to send event for user {}: {}",
                    event.getUserId(), exception.getMessage());

                // In production, you might want to:
                // 1. Send to dead letter queue
                // 2. Store in database for retry
                // 3. Alert monitoring system
                // 4. Implement circuit breaker pattern
            }
        }
    }

    /**
     * User event data class
     */
    public static class UserEvent {
        private String userId;
        private String action;
        private LocalDateTime timestamp;
        private String sessionId;
        private java.util.Map<String, Object> properties;

        public UserEvent() {
            this.timestamp = LocalDateTime.now();
            this.properties = new java.util.HashMap<>();
        }

        public UserEvent(String userId, String action) {
            this();
            this.userId = userId;
            this.action = action;
            this.sessionId = "session_" + System.currentTimeMillis();
        }

        // Getters and setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }

        public java.util.Map<String, Object> getProperties() { return properties; }
        public void setProperties(java.util.Map<String, Object> properties) { this.properties = properties; }

        public void addProperty(String key, Object value) {
            this.properties.put(key, value);
        }

        @Override
        public String toString() {
            return String.format("UserEvent{userId='%s', action='%s', timestamp=%s}",
                userId, action, timestamp);
        }
    }

    /**
     * Producer metrics wrapper
     */
    public static class ProducerMetrics {
        private final long messagesSent;
        private final long messagesSucceeded;
        private final long messagesFailed;
        private final java.util.Map<org.apache.kafka.common.MetricName, ? extends org.apache.kafka.common.Metric> kafkaMetrics;

        public ProducerMetrics(long sent, long succeeded, long failed,
                             java.util.Map<org.apache.kafka.common.MetricName, ? extends org.apache.kafka.common.Metric> kafkaMetrics) {
            this.messagesSent = sent;
            this.messagesSucceeded = succeeded;
            this.messagesFailed = failed;
            this.kafkaMetrics = kafkaMetrics;
        }

        public long getMessagesSent() { return messagesSent; }
        public long getMessagesSucceeded() { return messagesSucceeded; }
        public long getMessagesFailed() { return messagesFailed; }
        public double getSuccessRate() {
            return messagesSent > 0 ? (double) messagesSucceeded / messagesSent : 0.0;
        }

        public void printMetrics() {
            logger.info("Producer Metrics:");
            logger.info("  Messages sent: {}", messagesSent);
            logger.info("  Messages succeeded: {}", messagesSucceeded);
            logger.info("  Messages failed: {}", messagesFailed);
            logger.info("  Success rate: {:.2f}%", getSuccessRate() * 100);

            // Print key Kafka metrics
            kafkaMetrics.entrySet().stream()
                .filter(entry -> entry.getKey().name().contains("record-send-rate") ||
                               entry.getKey().name().contains("batch-size-avg") ||
                               entry.getKey().name().contains("request-latency-avg"))
                .forEach(entry -> logger.info("  {}: {}",
                    entry.getKey().name(), entry.getValue().metricValue()));
        }
    }

    /**
     * Main method for demonstration
     */
    public static void main(String[] args) {
        String bootstrapServers = "localhost:9092";
        String topicName = "user-events";

        AdvancedProducer producer = new AdvancedProducer(bootstrapServers, topicName);

        try {
            // Send individual events
            UserEvent event1 = new UserEvent("user1", "login");
            event1.addProperty("ip_address", "192.168.1.100");
            event1.addProperty("user_agent", "Mozilla/5.0");

            producer.sendUserEvent(event1);

            // Send synchronously
            UserEvent event2 = new UserEvent("user2", "purchase");
            event2.addProperty("amount", 99.99);
            event2.addProperty("product_id", "prod_123");

            RecordMetadata metadata = producer.sendUserEventSync(event2, 5000);
            logger.info("Sync send completed: partition={}, offset={}",
                metadata.partition(), metadata.offset());

            // Send batch
            java.util.List<UserEvent> events = new java.util.ArrayList<>();
            for (int i = 0; i < 10; i++) {
                UserEvent event = new UserEvent("user" + (i % 3), "page_view");
                event.addProperty("page", "/product/" + i);
                events.add(event);
            }
            producer.sendUserEventsBatch(events);

            // Wait a bit for async sends to complete
            Thread.sleep(2000);

            // Print metrics
            producer.getMetrics().printMetrics();

        } catch (Exception e) {
            logger.error("Error in main", e);
        } finally {
            producer.close();
        }
    }
}
