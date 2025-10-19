# Day 3 Exercises: Producer Development

> **Learning Tracks:** These exercises focus on pure Kafka Producer API patterns for platform-agnostic skills. Data engineers should complete all exercises.

## Data Engineer Track Exercises (Recommended)

### Exercise 1: Simple Producer Implementation - Pure Kafka API

### Objective
Master basic producer configuration and understand message sending patterns.

### Steps
1. **Run the Simple Producer**
   ```bash
   # Create topic for exercises  
   docker-compose exec kafka kafka-topics --create --topic producer-exercises --
     --partitions 3 \
     --replication-factor 1

   # Run Simple Producer
   mvn exec:java -Dexec.mainClass="com.training.kafka.Day03Producers.SimpleProducer"
   ```

2. **Observe Different Sending Patterns**
   - Synchronous sending (blocks until complete)
   - Asynchronous sending (non-blocking with callbacks)
   - Batch sending (multiple messages efficiently)

3. **Monitor Message Distribution**
   ```bash
   # In another terminal, consume to see the results
   docker-compose exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic producer-exercises \
     --group exercise-group \
     --property print.partition=true \
     --property print.offset=true \
     --property print.key=true
   ```

### Expected Results
- Messages sent successfully to all partitions
- Understand difference between sync/async sending
- See how keys affect partitioning

### Questions to Answer
1. Which approach is faster: sync or async sending?
2. How are messages distributed across partitions?
3. What happens if you don't provide a key?

---

### Exercise 2: Advanced Producer Features - Error Handling & Metrics

### Objective
Explore advanced producer configurations and error handling.

### Steps
1. **Run the Advanced Producer**
   ```bash
   mvn exec:java -Dexec.mainClass="com.training.kafka.Day03Producers.AdvancedProducer"
   ```

2. **Analyze the Features Demonstrated**
   - JSON serialization with Jackson
   - Custom headers and metadata
   - Comprehensive error handling
   - Metrics collection
   - Batch processing

3. **Experiment with Error Scenarios**
   ```bash
   # Stop Kafka to simulate broker failure
   docker-compose stop kafka
   
   # Run producer (should see retry behavior)
   mvn exec:java -Dexec.mainClass="com.training.kafka.Day03Producers.AdvancedProducer"
   
   # Restart Kafka
   docker-compose up -d kafka
   ```

### Expected Results
- JSON messages properly serialized
- Error handling and retry mechanisms in action
- Metrics showing producer performance
- Understanding of idempotent production

---

### Exercise 3: Producer Configuration Tuning - Performance Optimization

### Objective
Learn how different configurations affect producer behavior.

### Part A: High Throughput Configuration

1. **Create a High Throughput Producer**
   ```java
   // In a new class or modify existing
   props.put(ProducerConfig.BATCH_SIZE_CONFIG, 65536); // 64KB
   props.put(ProducerConfig.LINGER_MS_CONFIG, 100); // Wait longer to batch
   props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
   props.put(ProducerConfig.ACKS_CONFIG, "1"); // Leader only
   ```

2. **Test Throughput**
   ```bash
   # Send 1000 messages and measure time
   time mvn exec:java -Dexec.mainClass="YourHighThroughputProducer"
   ```

### Part B: Low Latency Configuration

1. **Create a Low Latency Producer**
   ```java
   props.put(ProducerConfig.BATCH_SIZE_CONFIG, 1); // No batching
   props.put(ProducerConfig.LINGER_MS_CONFIG, 0); // Send immediately
   props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "none");
   props.put(ProducerConfig.ACKS_CONFIG, "1");
   ```

2. **Measure Latency**
   ```bash
   # Time each message send
   mvn exec:java -Dexec.mainClass="YourLowLatencyProducer"
   ```

### Expected Results
- High throughput config: Higher messages/second, higher latency
- Low latency config: Lower throughput, faster individual sends
- Understanding of throughput vs latency trade-offs

---

### Exercise 4: Error Handling and Reliability - Production Patterns

### Objective
Implement robust error handling patterns for production use.

### Steps

1. **Create a Reliable Producer with Retry Logic**
   ```java
   public class ReliableProducer {
       private static final int MAX_RETRIES = 3;
       
       public void sendWithRetry(ProducerRecord<String, String> record) {
           int attempt = 0;
           while (attempt < MAX_RETRIES) {
               try {
                   RecordMetadata metadata = producer.send(record).get(5, TimeUnit.SECONDS);
                   logger.info("Successfully sent after {} attempts", attempt + 1);
                   return;
               } catch (Exception e) {
                   attempt++;
                   if (attempt >= MAX_RETRIES) {
                       handleFailedMessage(record, e);
                       throw new RuntimeException("Send failed after retries", e);
                   }
                   
                   // Exponential backoff
                   long delay = (long) Math.pow(2, attempt) * 1000;
                   try {
                       Thread.sleep(delay);
                   } catch (InterruptedException ie) {
                       Thread.currentThread().interrupt();
                       throw new RuntimeException("Interrupted during retry", ie);
                   }
               }
           }
       }
       
       private void handleFailedMessage(ProducerRecord<String, String> record, Exception e) {
           // Send to dead letter queue or database for later retry
           logger.error("Failed to send message after {} retries: {}", MAX_RETRIES, record);
       }
   }
   ```

2. **Test Failure Scenarios**
   - Network timeouts
   - Broker unavailability
   - Invalid configurations

### Expected Results
- Robust retry mechanism implemented
- Failed messages handled gracefully
- Understanding of production error patterns

---

### Exercise 5: Monitoring and Metrics - JMX & Client Metrics

### Objective
Implement comprehensive producer monitoring.

### Steps

1. **Create a Monitoring Producer**
   ```java
   public class MonitoringProducer {
       private final AtomicLong messagesSent = new AtomicLong(0);
       private final AtomicLong messagesSucceeded = new AtomicLong(0);
       private final AtomicLong messagesFailed = new AtomicLong(0);
       
       public void printMetrics() {
           Map<MetricName, ? extends Metric> metrics = producer.metrics();
           
           // Key metrics to track
           double recordSendRate = getMetricValue(metrics, "record-send-rate");
           double batchSizeAvg = getMetricValue(metrics, "batch-size-avg");
           double requestLatencyAvg = getMetricValue(metrics, "request-latency-avg");
           
           logger.info("Producer Metrics:");
           logger.info("  Messages sent: {}", messagesSent.get());
           logger.info("  Success rate: {:.2f}%", 
               (double) messagesSucceeded.get() / messagesSent.get() * 100);
           logger.info("  Send rate: {:.2f} records/sec", recordSendRate);
           logger.info("  Avg batch size: {:.2f} bytes", batchSizeAvg);
           logger.info("  Avg latency: {:.2f} ms", requestLatencyAvg);
       }
   }
   ```

2. **Set up Periodic Monitoring**
   ```java
   ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
   scheduler.scheduleAtFixedRate(() -> printMetrics(), 0, 30, TimeUnit.SECONDS);
   ```

### Expected Results
- Real-time producer metrics
- Performance monitoring dashboard
- Understanding of key metrics for production

---

### Exercise 6: Production Best Practices - Resource Management

### Objective
Implement producer patterns suitable for production environments.

### Steps

1. **Connection Pool Management**
   ```java
   public class ProducerPool {
       private static final KafkaProducer<String, String> INSTANCE = createProducer();
       
       public static KafkaProducer<String, String> getInstance() {
           return INSTANCE;
       }
       
       // Graceful shutdown
       static {
           Runtime.getRuntime().addShutdownHook(new Thread(() -> {
               INSTANCE.close(Duration.ofSeconds(10));
           }));
       }
   }
   ```

2. **Configuration Management**
   ```java
   // Load from external configuration
   Properties props = new Properties();
   try (InputStream input = getClass().getResourceAsStream("/producer.properties")) {
       props.load(input);
   }
   
   // Override with environment variables
   props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, 
       System.getenv().getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"));
   ```

3. **Health Checks**
   ```java
   public boolean isHealthy() {
       try {
           ProducerRecord<String, String> testRecord = 
               new ProducerRecord<>("health-check", "ping");
           RecordMetadata metadata = producer.send(testRecord).get(5, TimeUnit.SECONDS);
           return metadata != null;
       } catch (Exception e) {
           return false;
       }
   }
   ```

### Expected Results
- Production-ready producer implementation
- Proper resource management
- Health monitoring capabilities

---

## Solutions and Analysis

### Exercise 1 Solution: Basic Patterns
```java
// Synchronous: Blocks until complete - slower but guaranteed order
RecordMetadata metadata = producer.send(record).get();

// Asynchronous: Non-blocking - faster but need callbacks for error handling
producer.send(record, (metadata, exception) -> {
    if (exception == null) {
        // Success
    } else {
        // Handle error
    }
});

// Batch: Most efficient for high throughput
for (int i = 0; i < 1000; i++) {
    producer.send(new ProducerRecord<>(topic, "key" + i, "message" + i));
}
producer.flush(); // Ensure all are sent
```

### Exercise 2 Solution: Configuration Impact
```java
// High Throughput: Trades latency for throughput
// - Larger batches (64KB)
// - Wait time for batching (100ms)
// - Compression (lz4)
// Result: ~10x higher throughput, ~100ms higher latency

// Low Latency: Trades throughput for speed
// - No batching (size=1)
// - Send immediately (linger=0)
// - No compression
// Result: ~50% lower throughput, ~90% lower latency
```

### Exercise 3 Solution: Error Handling
```java
// Production pattern: Exponential backoff with circuit breaker
private boolean isCircuitOpen() {
    return failureCount.get() >= threshold && 
           (System.currentTimeMillis() - lastFailureTime.get()) < timeout;
}

// Dead letter queue for failed messages
private void sendToDeadLetterQueue(ProducerRecord<String, String> record, Exception error) {
    ProducerRecord<String, String> dlqRecord = new ProducerRecord<>(
        "dlq-" + record.topic(), record.key(), record.value());
    dlqRecord.headers().add("original-topic", record.topic().getBytes());
    dlqRecord.headers().add("error-message", error.getMessage().getBytes());
    dlqProducer.send(dlqRecord);
}
```

## Common Issues and Solutions

### Issue 1: OutOfMemoryError
**Problem**: Producer buffer memory exhausted
**Solution**: 
```java
props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 134217728L); // 128MB
props.put(ProducerConfig.BLOCK_ON_BUFFER_FULL_CONFIG, false);
props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 5000);
```

### Issue 2: Messages Not Delivered
**Problem**: Network issues or broker failures
**Solution**:
```java
props.put(ProducerConfig.ACKS_CONFIG, "all"); // Wait for all replicas
props.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
```

### Issue 3: Poor Performance
**Problem**: Inefficient batching or compression
**Solution**:
```java
props.put(ProducerConfig.BATCH_SIZE_CONFIG, 65536); // 64KB
props.put(ProducerConfig.LINGER_MS_CONFIG, 100); // 100ms batching
props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4"); // Fast compression
```

## Java Developer Track Exercises (Optional)

> **Java Developer Track Only**
>
> The exercises above use pure Kafka Producer API. For Spring Boot integration:

### Spring Kafka Producer Examples

1. **KafkaTemplate Usage**
    ```bash
    # Review Spring Boot producer service
    cat src/main/java/com/training/kafka/services/Day03ProducerService.java
    ```

2. **Spring Boot REST API**
    ```bash
    # Start Spring Boot application
    mvn spring-boot:run

    # Send via REST endpoint
    curl -X POST http://localhost:8080/api/training/day03/send \
      -H "Content-Type: application/json" \
      -d '{"topic":"test","key":"k1","value":"v1"}'
    ```

3. **Spring Configuration**
    - KafkaTemplate auto-configuration
    - Properties from application.yml
    - Transaction support with @Transactional

**Note:** KafkaTemplate is a wrapper around KafkaProducer. Understanding the core API from Data Engineer exercises is crucial for advanced use cases.

---

## Key Learning Outcomes

After completing the Data Engineer track exercises, you should understand:

1. **Producer Patterns**: Sync vs async vs fire-and-forget
2. **Configuration Impact**: How settings affect throughput and latency
3. **Error Handling**: Retries, circuit breakers, dead letter queues
4. **Monitoring**: Key metrics and health checks
5. **Production Readiness**: Resource management and best practices
6. **Performance Tuning**: Batch sizes, compression, acknowledgments

## Cleanup
```bash
# Clean up exercise topics
docker-compose exec kafka kafka-topics --delete --topic producer-exercises --bootstrap-server localhost:9092

# Stop monitoring
# Ctrl+C any running processes
```

---

**Next**: [Day 4 Exercises: Consumer Implementation](./day04-exercises.md)
