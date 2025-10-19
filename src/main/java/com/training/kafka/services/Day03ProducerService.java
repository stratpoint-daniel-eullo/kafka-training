package com.training.kafka.services;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Spring Service for Day 3 Producer Training
 * 
 * This service demonstrates Kafka message production using both Spring Kafka templates
 * and raw Kafka producers for educational purposes.
 * 
 * @author Kafka Training Course
 * @version 1.0.0
 */
@Service
public class Day03ProducerService {
    
    private static final Logger logger = LoggerFactory.getLogger(Day03ProducerService.class);
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaProducer<String, String> rawProducer;
    
    /**
     * Constructor with dependency injection of Spring-managed Kafka clients
     */
    public Day03ProducerService(
            @Qualifier("trainingKafkaTemplate") KafkaTemplate<String, String> kafkaTemplate,
            @Qualifier("trainingRawProducer") KafkaProducer<String, String> rawProducer) {
        this.kafkaTemplate = kafkaTemplate;
        this.rawProducer = rawProducer;
        logger.info("🎓 Day03ProducerService initialized with Spring-managed Kafka clients");
    }
    
    /**
     * Send a message synchronously using Spring Kafka Template
     */
    public boolean sendMessageSyncSpring(String topic, String key, String message) {
        try {
            SendResult<String, String> result = kafkaTemplate.send(topic, key, message).get();
            RecordMetadata metadata = result.getRecordMetadata();
            
            logger.info("✅ Message sent successfully (Spring): topic={}, partition={}, offset={}, key={}", 
                metadata.topic(), metadata.partition(), metadata.offset(), key);
            return true;
            
        } catch (ExecutionException | InterruptedException e) {
            logger.error("❌ Failed to send message (Spring): {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Send a message asynchronously using Spring Kafka Template
     */
    public void sendMessageAsyncSpring(String topic, String key, String message) {
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, message);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                RecordMetadata metadata = result.getRecordMetadata();
                logger.info("✅ Message sent successfully (Spring Async): topic={}, partition={}, offset={}, key={}",
                    metadata.topic(), metadata.partition(), metadata.offset(), key);
            } else {
                logger.error("❌ Failed to send message (Spring Async): {}", ex.getMessage(), ex);
            }
        });
    }
    
    /**
     * Send a message synchronously using raw Kafka Producer
     */
    public boolean sendMessageSyncRaw(String topic, String key, String message) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, message);
        
        try {
            RecordMetadata metadata = rawProducer.send(record).get();
            logger.info("✅ Message sent successfully (Raw): topic={}, partition={}, offset={}, key={}", 
                metadata.topic(), metadata.partition(), metadata.offset(), key);
            return true;
            
        } catch (ExecutionException | InterruptedException e) {
            logger.error("❌ Failed to send message (Raw): {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Send a message asynchronously using raw Kafka Producer
     */
    public void sendMessageAsyncRaw(String topic, String key, String message) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, message);
        
        rawProducer.send(record, (metadata, exception) -> {
            if (exception == null) {
                logger.info("✅ Message sent successfully (Raw Async): topic={}, partition={}, offset={}, key={}", 
                    metadata.topic(), metadata.partition(), metadata.offset(), key);
            } else {
                logger.error("❌ Failed to send message (Raw Async): {}", exception.getMessage(), exception);
            }
        });
    }
    
    /**
     * Send multiple messages in batch using Spring Kafka Template
     */
    public void sendBatchMessagesSpring(String topic, int count) {
        logger.info("📤 Sending {} messages using Spring Kafka Template...", count);
        
        for (int i = 0; i < count; i++) {
            String key = "user-" + (i % 5); // 5 different users for partitioning
            String message = String.format(
                "{\"user_id\":\"%s\", \"action\":\"login\", \"timestamp\":\"%s\", \"session_id\":\"%d\"}", 
                key, LocalDateTime.now(), System.currentTimeMillis() + i);
            
            sendMessageAsyncSpring(topic, key, message);
        }
        
        // Flush to ensure all messages are sent
        kafkaTemplate.flush();
        logger.info("✅ All {} messages sent successfully (Spring)", count);
    }
    
    /**
     * Send multiple messages in batch using raw Kafka Producer
     */
    public void sendBatchMessagesRaw(String topic, int count) {
        logger.info("📤 Sending {} messages using Raw Kafka Producer...", count);
        
        for (int i = 0; i < count; i++) {
            String key = "user-" + (i % 5); // 5 different users for partitioning
            String message = String.format(
                "{\"user_id\":\"%s\", \"action\":\"purchase\", \"timestamp\":\"%s\", \"order_id\":\"%d\"}", 
                key, LocalDateTime.now(), System.currentTimeMillis() + i);
            
            sendMessageAsyncRaw(topic, key, message);
        }
        
        // Flush to ensure all messages are sent
        rawProducer.flush();
        logger.info("✅ All {} messages sent successfully (Raw)", count);
    }
    
    /**
     * Demonstrate different producer patterns
     */
    public void demonstrateProducerPatterns(String topic) {
        logger.info("Starting Day 3 Producer Demonstration for topic: {}", topic);

        try {
            // 1. Synchronous sending with Spring
            logger.info("Step 1: Demonstrating synchronous sending with Spring Kafka Template");
            boolean syncSuccess = sendMessageSyncSpring(topic, "sync-spring", "Synchronous message via Spring");
            logger.info("Synchronous send result: {}", syncSuccess);

            // 2. Asynchronous sending with Spring
            logger.info("Step 2: Demonstrating asynchronous sending with Spring Kafka Template");
            sendMessageAsyncSpring(topic, "async-spring", "Asynchronous message via Spring");

            // 3. Synchronous sending with raw producer
            logger.info("Step 3: Demonstrating synchronous sending with Raw Kafka Producer");
            boolean rawSuccess = sendMessageSyncRaw(topic, "sync-raw", "Synchronous message via Raw Producer");
            logger.info("Raw synchronous send result: {}", rawSuccess);

            // 4. Asynchronous sending with raw producer
            logger.info("Step 4: Demonstrating asynchronous sending with Raw Kafka Producer");
            sendMessageAsyncRaw(topic, "async-raw", "Asynchronous message via Raw Producer");

            // 5. Batch sending
            logger.info("Step 5: Demonstrating batch sending");
            sendBatchMessagesSpring(topic, 5);

            // Wait for async operations to complete
            Thread.sleep(500);

            logger.info("Day 3 Producer Demonstration completed successfully");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Day 3 Producer Demonstration interrupted", e);
        } catch (Exception e) {
            logger.error("Day 3 Producer Demonstration encountered an error: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Send a structured user event message
     */
    public void sendUserEvent(String topic, String userId, String action, String details) {
        String message = String.format(
            "{\"user_id\":\"%s\", \"action\":\"%s\", \"details\":\"%s\", \"timestamp\":\"%s\"}", 
            userId, action, details, LocalDateTime.now());
        
        sendMessageAsyncSpring(topic, userId, message);
    }
    
    /**
     * Send messages with different partitioning strategies
     */
    public void demonstratePartitioning(String topic) {
        logger.info("🎯 Demonstrating partitioning strategies");
        
        // Messages with same key go to same partition
        for (int i = 0; i < 3; i++) {
            sendMessageAsyncSpring(topic, "same-key", "Message " + i + " with same key");
        }
        
        // Messages with different keys may go to different partitions
        for (int i = 0; i < 3; i++) {
            sendMessageAsyncSpring(topic, "key-" + i, "Message " + i + " with different key");
        }
        
        // Messages with null key are distributed round-robin
        for (int i = 0; i < 3; i++) {
            sendMessageAsyncSpring(topic, null, "Message " + i + " with null key");
        }
        
        kafkaTemplate.flush();
        logger.info("✅ Partitioning demonstration completed");
    }
    
    /**
     * Get producer metrics (for educational purposes)
     */
    public void showProducerMetrics() {
        logger.info("📊 Raw Producer Metrics:");
        rawProducer.metrics().forEach((metricName, metric) -> {
            if (metricName.name().contains("record-send-rate") || 
                metricName.name().contains("batch-size-avg") ||
                metricName.name().contains("request-latency-avg")) {
                logger.info("  - {}: {}", metricName.name(), metric.metricValue());
            }
        });
    }
}
