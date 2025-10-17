# Day 4: Java Consumer Implementation

## Learning Objectives
By the end of Day 4, you will:
- Master Kafka consumer configuration and optimization
- Implement consumer groups for scalable processing
- Handle offset management strategies effectively
- Implement error handling and retry patterns
- Understand partition assignment and rebalancing
- Build production-ready consumer applications

## Morning Session (3 hours): Consumer Fundamentals

### 1. Consumer Architecture

**Key Components:**
- **Consumer Client**: Application interface for reading messages
- **Group Coordinator**: Manages consumer group membership
- **Partition Assignment**: Distributes partitions among consumers
- **Offset Management**: Tracks reading progress per partition

### 2. Consumer Configuration

#### Essential Properties
```java
// Connection
props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
props.put(ConsumerConfig.GROUP_ID_CONFIG, "my-consumer-group");
props.put(ConsumerConfig.CLIENT_ID_CONFIG, "my-consumer");

// Deserialization
props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

// Offset Management
props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // latest, earliest, none
props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"); // Manual control
props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "500");

// Session Management
props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000"); // 30 seconds
props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "10000"); // 10 seconds
```

#### Offset Reset Strategies
1. **earliest**: Start from beginning of topic
2. **latest**: Start from newest messages
3. **none**: Throw exception if no offset found

### 3. Consumer Patterns

#### Basic Consumer Loop
```java
consumer.subscribe(Collections.singletonList("my-topic"));

while (running) {
    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
    
    for (ConsumerRecord<String, String> record : records) {
        processMessage(record);
    }
    
    consumer.commitSync(); // Manual commit
}
```

#### Batch Processing
```java
ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

if (!records.isEmpty()) {
    List<Message> batch = new ArrayList<>();
    
    for (ConsumerRecord<String, String> record : records) {
        batch.add(parseMessage(record));
    }
    
    processBatch(batch); // Process as batch
    consumer.commitSync(); // Commit after successful batch processing
}
```

#### Per-Partition Processing
```java
ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

for (TopicPartition partition : records.partitions()) {
    List<ConsumerRecord<String, String>> partitionRecords = records.records(partition);
    
    for (ConsumerRecord<String, String> record : partitionRecords) {
        processMessage(record);
    }
    
    // Commit per partition
    long lastOffset = partitionRecords.get(partitionRecords.size() - 1).offset();
    consumer.commitSync(Collections.singletonMap(partition, 
        new OffsetAndMetadata(lastOffset + 1)));
}
```

## Afternoon Session (3 hours): Advanced Consumer Patterns

### Exercise 1: Basic Consumer Implementation

```bash
# Run Simple Consumer
mvn exec:java -Dexec.mainClass="com.training.kafka.Day04Consumers.SimpleConsumer"

# Run with consumer group demo
mvn exec:java -Dexec.mainClass="com.training.kafka.Day04Consumers.SimpleConsumer" -Dexec.args="group-demo"
```

**Key Learning Points:**
- Consumer subscription and polling
- Manual vs automatic offset commits
- Consumer group coordination
- Graceful shutdown handling

### Exercise 2: Consumer Group Scaling

#### Single Consumer (Gets All Partitions)
```java
public void demonstrateSingleConsumer() {
    KafkaConsumer<String, String> consumer = createConsumer("single-consumer-group");
    consumer.subscribe(Collections.singletonList("user-events"));
    
    // This consumer will get all partitions assigned
    consumeMessages(consumer, "SingleConsumer", 60000); // Run for 1 minute
}
```

#### Multiple Consumers (Share Partitions)
```java
public void demonstrateConsumerGroup() {
    ExecutorService executor = Executors.newFixedThreadPool(3);
    
    // Start 3 consumers in same group
    for (int i = 1; i <= 3; i++) {
        final int consumerId = i;
        executor.submit(() -> {
            KafkaConsumer<String, String> consumer = createConsumer("multi-consumer-group");
            consumer.subscribe(Collections.singletonList("user-events"));
            consumeMessages(consumer, "Consumer-" + consumerId, 60000);
        });
    }
    
    // Partitions will be distributed among the 3 consumers
}
```

### Exercise 3: Offset Management Strategies

#### Auto-Commit (Simple but Less Control)
```java
Properties props = new Properties();
props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "5000"); // 5 seconds

// Messages may be lost if consumer crashes between auto-commits
// Messages may be reprocessed if consumer crashes after processing but before commit
```

#### Manual Commit (Full Control)
```java
props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

// Commit after each message (safest but slowest)
for (ConsumerRecord<String, String> record : records) {
    processMessage(record);
    
    Map<TopicPartition, OffsetAndMetadata> offsets = Collections.singletonMap(
        new TopicPartition(record.topic(), record.partition()),
        new OffsetAndMetadata(record.offset() + 1)
    );
    consumer.commitSync(offsets);
}

// Commit after batch (faster but may reprocess on failure)
for (ConsumerRecord<String, String> record : records) {
    processMessage(record);
}
consumer.commitSync(); // Commit all at once
```

#### Async Commit (Best Performance)
```java
consumer.commitAsync((offsets, exception) -> {
    if (exception != null) {
        logger.error("Failed to commit offsets: {}", offsets, exception);
        // Handle commit failure (maybe retry with commitSync)
    } else {
        logger.debug("Successfully committed offsets: {}", offsets);
    }
});
```

### Exercise 4: Error Handling Patterns

#### Retry with Dead Letter Queue
```java
public void processWithRetry(ConsumerRecord<String, String> record) {
    int maxRetries = 3;
    int attempt = 0;
    
    while (attempt < maxRetries) {
        try {
            processMessage(record);
            return; // Success
            
        } catch (RetryableException e) {
            attempt++;
            logger.warn("Processing failed (attempt {}): {}", attempt, e.getMessage());
            
            if (attempt >= maxRetries) {
                sendToDeadLetterQueue(record, e);
                return;
            }
            
            // Exponential backoff
            try {
                Thread.sleep((long) Math.pow(2, attempt) * 1000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            }
            
        } catch (NonRetryableException e) {
            logger.error("Non-retryable error: {}", e.getMessage());
            sendToDeadLetterQueue(record, e);
            return;
        }
    }
}

private void sendToDeadLetterQueue(ConsumerRecord<String, String> record, Exception error) {
    // Send to DLQ topic for manual investigation
    ProducerRecord<String, String> dlqRecord = new ProducerRecord<>(
        "dlq-user-events",
        record.key(),
        record.value()
    );
    
    dlqRecord.headers().add("original-topic", record.topic().getBytes());
    dlqRecord.headers().add("original-partition", String.valueOf(record.partition()).getBytes());
    dlqRecord.headers().add("original-offset", String.valueOf(record.offset()).getBytes());
    dlqRecord.headers().add("error-message", error.getMessage().getBytes());
    dlqRecord.headers().add("error-timestamp", String.valueOf(System.currentTimeMillis()).getBytes());
    
    dlqProducer.send(dlqRecord);
    logger.info("Sent message to DLQ: topic={}, partition={}, offset={}", 
        record.topic(), record.partition(), record.offset());
}
```

#### Circuit Breaker Pattern
```java
public class CircuitBreakerConsumer {
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicLong lastFailureTime = new AtomicLong(0);
    private final int threshold = 5;
    private final long timeout = 30000; // 30 seconds
    
    public void processMessage(ConsumerRecord<String, String> record) {
        if (isCircuitOpen()) {
            logger.warn("Circuit breaker OPEN - skipping message processing");
            return;
        }
        
        try {
            doProcessMessage(record);
            failureCount.set(0); // Reset on success
            
        } catch (Exception e) {
            failureCount.incrementAndGet();
            lastFailureTime.set(System.currentTimeMillis());
            
            logger.error("Processing failed (failure count: {}): {}", 
                failureCount.get(), e.getMessage());
            
            if (isCircuitOpen()) {
                logger.warn("Circuit breaker opened due to failures");
            }
            
            throw e;
        }
    }
    
    private boolean isCircuitOpen() {
        int failures = failureCount.get();
        long timeSinceLastFailure = System.currentTimeMillis() - lastFailureTime.get();
        
        return failures >= threshold && timeSinceLastFailure < timeout;
    }
}
```

## Performance Optimization

### 1. Batch Processing
```java
// Configure for higher throughput
props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, "50000"); // 50KB minimum
props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "500"); // Wait max 500ms
props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1000"); // Larger batches
```

### 2. Multi-threaded Processing
```java
public class MultiThreadedConsumer {
    private final ExecutorService executor;
    private final int numThreads;
    
    public MultiThreadedConsumer(int numThreads) {
        this.numThreads = numThreads;
        this.executor = Executors.newFixedThreadPool(numThreads);
    }
    
    public void processRecords(ConsumerRecords<String, String> records) {
        // Distribute records across threads by partition
        Map<Integer, List<ConsumerRecord<String, String>>> partitionedRecords = 
            new HashMap<>();
            
        for (ConsumerRecord<String, String> record : records) {
            partitionedRecords.computeIfAbsent(record.partition(), k -> new ArrayList<>())
                .add(record);
        }
        
        List<Future<?>> futures = new ArrayList<>();
        
        for (List<ConsumerRecord<String, String>> partitionRecords : partitionedRecords.values()) {
            Future<?> future = executor.submit(() -> {
                for (ConsumerRecord<String, String> record : partitionRecords) {
                    processMessage(record);
                }
            });
            futures.add(future);
        }
        
        // Wait for all threads to complete
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                logger.error("Error in processing thread", e);
            }
        }
    }
}
```

### 3. Consumer Metrics Monitoring
```java
public void monitorConsumerMetrics(KafkaConsumer<String, String> consumer) {
    Map<MetricName, ? extends Metric> metrics = consumer.metrics();
    
    // Key metrics to monitor
    double recordsConsumedRate = getMetricValue(metrics, "records-consumed-rate");
    double bytesConsumedRate = getMetricValue(metrics, "bytes-consumed-rate");
    double recordsLagMax = getMetricValue(metrics, "records-lag-max");
    double fetchLatencyAvg = getMetricValue(metrics, "fetch-latency-avg");
    double commitRate = getMetricValue(metrics, "commit-rate");
    
    logger.info("Consumer Metrics:");
    logger.info("  Records/sec: {:.2f}", recordsConsumedRate);
    logger.info("  Bytes/sec: {:.2f}", bytesConsumedRate);
    logger.info("  Max Lag: {:.0f} records", recordsLagMax);
    logger.info("  Fetch Latency: {:.2f} ms", fetchLatencyAvg);
    logger.info("  Commit Rate: {:.2f}/sec", commitRate);
    
    // Alert on high lag
    if (recordsLagMax > 1000) {
        logger.warn("HIGH LAG ALERT: {} records behind", recordsLagMax);
    }
}
```

## Production Best Practices

### 1. Graceful Shutdown
```java
public class GracefulConsumer {
    private volatile boolean running = true;
    private final KafkaConsumer<String, String> consumer;
    
    public void shutdown() {
        logger.info("Shutdown signal received");
        running = false;
        consumer.wakeup(); // Interrupt poll()
    }
    
    public void run() {
        try {
            consumer.subscribe(Collections.singletonList("my-topic"));
            
            while (running) {
                try {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                    processRecords(records);
                    consumer.commitSync();
                    
                } catch (WakeupException e) {
                    if (running) {
                        throw e; // Unexpected wakeup
                    }
                    // Expected wakeup during shutdown
                }
            }
            
        } catch (Exception e) {
            logger.error("Unexpected error in consumer", e);
        } finally {
            try {
                consumer.commitSync(); // Final commit
            } finally {
                consumer.close();
                logger.info("Consumer closed gracefully");
            }
        }
    }
}

// Usage with shutdown hook
GracefulConsumer consumer = new GracefulConsumer();
Runtime.getRuntime().addShutdownHook(new Thread(consumer::shutdown));
consumer.run();
```

### 2. Consumer Health Checks
```java
public boolean isHealthy() {
    try {
        // Check if consumer is able to poll
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
        
        // Check assignment
        Set<TopicPartition> assignment = consumer.assignment();
        if (assignment.isEmpty()) {
            logger.warn("Consumer has no partition assignments");
            return false;
        }
        
        // Check lag
        Map<MetricName, ? extends Metric> metrics = consumer.metrics();
        double maxLag = getMetricValue(metrics, "records-lag-max");
        if (maxLag > 10000) { // Configurable threshold
            logger.warn("Consumer lag too high: {}", maxLag);
            return false;
        }
        
        return true;
        
    } catch (Exception e) {
        logger.error("Consumer health check failed", e);
        return false;
    }
}
```

## Common Issues and Solutions

### 1. Consumer Lag
**Problem**: Consumer falling behind
**Solutions**:
```java
// 1. Scale up consumers (up to partition count)
// 2. Optimize processing logic
// 3. Increase fetch size
props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, "100000");
props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1000");

// 4. Use async processing
```

### 2. Rebalancing Issues
**Problem**: Frequent rebalancing
**Solutions**:
```java
// Increase session timeout
props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "45000");
props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "15000");

// Reduce processing time per poll
props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "300000"); // 5 minutes
```

### 3. Duplicate Processing
**Problem**: Messages processed multiple times
**Solutions**:
```java
// 1. Idempotent processing logic
// 2. Manual offset management
// 3. Transactional processing (advanced)
```

## Key Takeaways

1. **Consumer groups** enable horizontal scaling and fault tolerance
2. **Offset management** strategy affects durability and performance
3. **Error handling** patterns prevent message loss and infinite loops
4. **Monitoring** is essential for detecting lag and performance issues
5. **Graceful shutdown** ensures no message loss during deployments
6. **Partition assignment** affects parallelism and ordering guarantees

## Next Steps

Tomorrow we'll explore:
- Stream processing with Kafka Streams
- Stateful processing and aggregations
- Stream-stream joins
- Event-driven architectures

---

**🚀 Ready for Day 5?** Continue with [Day 5: Stream Processing](./day05-streams.md)