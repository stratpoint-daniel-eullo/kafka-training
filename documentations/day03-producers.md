# Day 3: Java Producer Development

## Learning Objectives
By the end of Day 3, you will:
- Master Kafka producer configuration and tuning
- Implement both simple and advanced producer patterns
- Handle errors, retries, and delivery guarantees
- Understand batching, compression, and performance optimization
- Implement metrics and monitoring for producers
- Build production-ready producer applications

## Morning Session (3 hours): Producer Fundamentals

### 1. Producer Architecture

**Key Components:**
- **Producer Client**: Application interface for sending messages
- **Partitioner**: Determines which partition to send messages to
- **Record Accumulator**: Batches messages for efficiency
- **Sender Thread**: Background thread that sends batches to brokers

### 2. Producer Configuration

#### Essential Properties
```java
// Connection
props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
props.put(ProducerConfig.CLIENT_ID_CONFIG, "my-producer");

// Serialization
props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

// Reliability
props.put(ProducerConfig.ACKS_CONFIG, "all"); // 0, 1, or all
props.put(ProducerConfig.RETRIES_CONFIG, 3);
props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

// Performance
props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384); // 16KB
props.put(ProducerConfig.LINGER_MS_CONFIG, 10); // Wait 10ms to batch
props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
```

#### Delivery Guarantees
1. **At-most-once** (`acks=0`): Fast, may lose messages
2. **At-least-once** (`acks=1`): Leader acknowledgment, may duplicate
3. **Exactly-once** (`acks=all` + `enable.idempotence=true`): Strongest guarantee

### 3. Producer Patterns

#### Synchronous Send
```java
ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
RecordMetadata metadata = producer.send(record).get(); // Blocks
```

#### Asynchronous Send
```java
producer.send(record, (metadata, exception) -> {
    if (exception == null) {
        // Success
        logger.info("Sent: partition={}, offset={}", metadata.partition(), metadata.offset());
    } else {
        // Handle error
        logger.error("Send failed: {}", exception.getMessage());
    }
});
```

#### Fire-and-Forget
```java
producer.send(record); // No callback, fastest but no error handling
```

## Afternoon Session (3 hours): Hands-on Implementation

### Exercise 1: Simple Producer
Run the basic producer example:

```bash
# Create topic for exercises
confluent local kafka topic create producer-exercises \
  --partitions 3 \
  --replication-factor 1

# Run Simple Producer
mvn exec:java -Dexec.mainClass="com.training.kafka.Day03Producers.SimpleProducer"
```

**Key Learning Points:**
- Basic producer configuration
- Synchronous vs asynchronous sending
- Error handling with callbacks
- Producer lifecycle management

### Exercise 2: Advanced Producer
Run the advanced producer with comprehensive features:

```bash
# Run Advanced Producer
mvn exec:java -Dexec.mainClass="com.training.kafka.Day03Producers.AdvancedProducer"
```

**Key Learning Points:**
- Complex JSON serialization
- Custom headers and metadata
- Idempotent production
- Metrics collection and monitoring
- Batch processing patterns

### Exercise 3: Producer Configuration Tuning

#### High Throughput Configuration
```java
// Optimize for throughput
props.put(ProducerConfig.BATCH_SIZE_CONFIG, 65536); // 64KB
props.put(ProducerConfig.LINGER_MS_CONFIG, 100); // Wait longer to batch
props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4"); // Fast compression
props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 67108864); // 64MB buffer
```

#### Low Latency Configuration
```java
// Optimize for latency
props.put(ProducerConfig.BATCH_SIZE_CONFIG, 1); // No batching
props.put(ProducerConfig.LINGER_MS_CONFIG, 0); // Send immediately
props.put(ProducerConfig.ACKS_CONFIG, "1"); // Leader only
props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "none"); // No compression
```

#### High Reliability Configuration
```java
// Optimize for reliability
props.put(ProducerConfig.ACKS_CONFIG, "all"); // All replicas
props.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
```

### Exercise 4: Error Handling Patterns

#### Retry with Exponential Backoff
```java
public void sendWithRetry(ProducerRecord<String, String> record, int maxRetries) {
    int attempt = 0;
    while (attempt < maxRetries) {
        try {
            RecordMetadata metadata = producer.send(record).get(5, TimeUnit.SECONDS);
            logger.info("Successfully sent after {} attempts", attempt + 1);
            return;
        } catch (Exception e) {
            attempt++;
            if (attempt >= maxRetries) {
                logger.error("Failed to send after {} attempts", maxRetries);
                throw new RuntimeException("Send failed", e);
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
```

### Exercise 5: Monitoring Producer Metrics

#### Key Metrics to Monitor
```java
Map<MetricName, ? extends Metric> metrics = producer.metrics();

// Throughput metrics
double recordSendRate = getMetricValue(metrics, "record-send-rate");
double bytesSentRate = getMetricValue(metrics, "outgoing-byte-rate");

// Latency metrics
double requestLatencyAvg = getMetricValue(metrics, "request-latency-avg");
double requestLatencyMax = getMetricValue(metrics, "request-latency-max");

// Error metrics
double recordErrorRate = getMetricValue(metrics, "record-error-rate");
double recordRetryRate = getMetricValue(metrics, "record-retry-rate");

// Efficiency metrics
double batchSizeAvg = getMetricValue(metrics, "batch-size-avg");
double compressionRateAvg = getMetricValue(metrics, "compression-rate-avg");
```

## Production Best Practices

### 1. Configuration Management
```java
// Use configuration files
Properties props = new Properties();
try (InputStream input = getClass().getResourceAsStream("/producer.properties")) {
    props.load(input);
}

// Override with environment variables
props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, 
    System.getenv().getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"));
```

### 2. Connection Pooling
```java
// Reuse producer instances - they are thread-safe
public class ProducerManager {
    private static final KafkaProducer<String, String> INSTANCE = createProducer();
    
    public static KafkaProducer<String, String> getInstance() {
        return INSTANCE;
    }
    
    // Shutdown hook
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            INSTANCE.close(Duration.ofSeconds(10));
        }));
    }
}
```

### 3. Graceful Shutdown
```java
public void shutdown() {
    logger.info("Shutting down producer...");
    
    // Stop accepting new messages
    accepting.set(false);
    
    // Flush pending messages
    producer.flush();
    
    // Close with timeout
    producer.close(Duration.ofSeconds(30));
    
    logger.info("Producer shutdown complete");
}
```

### 4. Health Checks
```java
public boolean isHealthy() {
    try {
        // Send a test message with short timeout
        ProducerRecord<String, String> testRecord = 
            new ProducerRecord<>("health-check", "ping");
        
        RecordMetadata metadata = producer.send(testRecord).get(5, TimeUnit.SECONDS);
        return metadata != null;
        
    } catch (Exception e) {
        logger.warn("Producer health check failed: {}", e.getMessage());
        return false;
    }
}
```

## Common Issues and Solutions

### 1. OutOfMemoryError
**Problem**: Producer buffer memory exhausted
**Solution**: 
```java
props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 134217728L); // 128MB
props.put(ProducerConfig.BLOCK_ON_BUFFER_FULL_CONFIG, false); // Don't block
props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 5000); // 5 second timeout
```

### 2. High Latency
**Problem**: Messages taking too long to send
**Solution**:
```java
props.put(ProducerConfig.LINGER_MS_CONFIG, 0); // Send immediately
props.put(ProducerConfig.BATCH_SIZE_CONFIG, 1); // No batching
props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "none"); // No compression
```

### 3. Message Loss
**Problem**: Messages being lost
**Solution**:
```java
props.put(ProducerConfig.ACKS_CONFIG, "all"); // Wait for all replicas
props.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE); // Retry forever
props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // Prevent duplicates
```

### 4. Poor Throughput
**Problem**: Low message throughput
**Solution**:
```java
props.put(ProducerConfig.BATCH_SIZE_CONFIG, 65536); // 64KB batches
props.put(ProducerConfig.LINGER_MS_CONFIG, 100); // Wait to batch
props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4"); // Compression
```

## Hands-on Exercises

### Exercise A: Build a Real-time Event Producer
Create a producer that simulates real-time user events:

```java
// Simulate user activity
String[] actions = {"login", "page_view", "purchase", "logout"};
String[] users = {"user1", "user2", "user3", "user4", "user5"};

Random random = new Random();
for (int i = 0; i < 100; i++) {
    String user = users[random.nextInt(users.length)];
    String action = actions[random.nextInt(actions.length)];
    
    UserEvent event = new UserEvent(user, action);
    producer.send(new ProducerRecord<>("user-events", user, 
        objectMapper.writeValueAsString(event)));
    
    Thread.sleep(100); // 10 events per second
}
```

### Exercise B: Implement Circuit Breaker Pattern
```java
public class CircuitBreakerProducer {
    private final AtomicInteger failures = new AtomicInteger(0);
    private final AtomicLong lastFailureTime = new AtomicLong(0);
    private final int threshold = 5;
    private final long timeout = 30000; // 30 seconds
    
    public void send(ProducerRecord<String, String> record) {
        if (isCircuitOpen()) {
            throw new RuntimeException("Circuit breaker is OPEN");
        }
        
        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                failures.incrementAndGet();
                lastFailureTime.set(System.currentTimeMillis());
            } else {
                failures.set(0); // Reset on success
            }
        });
    }
    
    private boolean isCircuitOpen() {
        int currentFailures = failures.get();
        long timeSinceLastFailure = System.currentTimeMillis() - lastFailureTime.get();
        
        return currentFailures >= threshold && timeSinceLastFailure < timeout;
    }
}
```

## Key Takeaways

1. **Producer configuration** directly impacts throughput, latency, and reliability
2. **Asynchronous sending** with callbacks provides best performance with error handling
3. **Idempotent producers** prevent message duplication in retry scenarios
4. **Batching and compression** significantly improve throughput
5. **Monitoring metrics** is essential for production operations
6. **Error handling patterns** ensure robust message delivery
7. **Connection pooling** and reuse improves resource efficiency

## Next Steps

Tomorrow we'll explore:
- Consumer implementation patterns
- Consumer groups and partition assignment
- Offset management strategies
- Consumer performance optimization

---

**🚀 Ready for Day 4?** Continue with [Day 4: Java Consumers](./day04-consumers.md)