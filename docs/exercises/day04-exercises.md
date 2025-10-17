# Day 4 Exercises: Consumer Implementation

## Exercise 1: Basic Consumer Implementation

### Objective
Master basic consumer configuration, offset management, and message processing.

### Steps

1. **Run the Simple Consumer**
   ```bash
   # Make sure you have messages to consume (run producer first)
   mvn exec:java -Dexec.mainClass="com.training.kafka.Day03Producers.SimpleProducer"
   
   # Now run the basic consumer
   mvn exec:java -Dexec.mainClass="com.training.kafka.Day04Consumers.SimpleConsumer"
   ```

2. **Observe Consumer Behavior**
   - Manual offset commits
   - Message processing patterns
   - Error handling

3. **Test Consumer Group Scaling**
   ```bash
   # Run consumer group demo
   mvn exec:java -Dexec.mainClass="com.training.kafka.Day04Consumers.SimpleConsumer" -Dexec.args="group-demo"
   ```

### Expected Results
- Messages consumed and processed successfully
- Understanding of offset management
- Consumer group load balancing observed

### Questions to Answer
1. What happens when you restart the consumer?
2. How does manual offset commit differ from auto-commit?
3. How are partitions distributed among consumer group members?

---

## Exercise 2: Consumer Group Dynamics

### Objective
Understand how consumer groups scale and handle rebalancing.

### Setup
```bash
# Create topic with multiple partitions
docker-compose exec kafka kafka-topics --create --topic consumer-scaling-demo --
  --partitions 6 \
  --replication-factor 1

# Generate test data
for i in {1..50}; do
  echo "user$((i % 5)):message-$i" | docker-compose exec kafka kafka-console-producer --bootstrap-server localhost:9092 --topic consumer-scaling-demo \
    --property "parse.key=true" \
    --property "key.separator=:"
done
```

### Part A: Single Consumer vs Consumer Group

1. **Single Consumer (Gets All Partitions)**
   ```bash
   # Terminal 1: Single consumer
   docker-compose exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic consumer-scaling-demo \
     --group single-consumer-group \
     --property print.partition=true \
     --property print.key=true \
     --from-beginning
   ```

2. **Multiple Consumers (Share Partitions)**
   ```bash
   # Terminal 1: First consumer
   docker-compose exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic consumer-scaling-demo \
     --group multi-consumer-group \
     --property print.partition=true \
     --from-beginning
   
   # Terminal 2: Second consumer (start after 10 seconds)
   docker-compose exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic consumer-scaling-demo \
     --group multi-consumer-group \
     --property print.partition=true \
     --from-beginning
   
   # Terminal 3: Third consumer (start after another 10 seconds)
   docker-compose exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic consumer-scaling-demo \
     --group multi-consumer-group \
     --property print.partition=true \
     --from-beginning
   ```

### Part B: Rebalancing Demonstration

1. **Monitor Partition Assignment**
   ```bash
   # Check consumer group status
   docker-compose exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group multi-consumer-group
   ```

2. **Simulate Consumer Failure**
   - Stop one consumer (Ctrl+C)
   - Observe rebalancing
   - Check new partition assignments

### Expected Results
- Single consumer processes all partitions
- Multiple consumers share partitions automatically
- Rebalancing occurs when consumers join/leave

---

## Exercise 3: Offset Management Strategies

### Objective
Compare auto-commit vs manual commit and understand their implications.

### Part A: Auto-Commit Testing

1. **Consumer with Auto-Commit**
   ```bash
   docker-compose exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic consumer-scaling-demo \
     --group auto-commit-group \
     --property enable.auto.commit=true \
     --property auto.commit.interval.ms=5000 \
     --max-messages 10
   ```

2. **Restart Consumer**
   ```bash
   # Should not re-read processed messages
   docker-compose exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic consumer-scaling-demo \
     --group auto-commit-group \
     --property enable.auto.commit=true
   ```

### Part B: Manual Commit Testing

1. **Consumer with Manual Commit**
   ```bash
   docker-compose exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic consumer-scaling-demo \
     --group manual-commit-group \
     --property enable.auto.commit=false \
     --max-messages 10
   ```

2. **Simulate Processing Failure**
   ```bash
   # Start consumer and kill it before committing
   docker-compose exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic consumer-scaling-demo \
     --group manual-commit-group \
     --property enable.auto.commit=false \
     --max-messages 5
   # Kill with Ctrl+C immediately
   
   # Restart - should re-read messages
   docker-compose exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic consumer-scaling-demo \
     --group manual-commit-group \
     --property enable.auto.commit=false
   ```

### Expected Results
- Auto-commit: May lose messages or duplicate processing
- Manual commit: More control but requires careful handling

---

## Exercise 4: Error Handling and Retry Patterns

### Objective
Implement robust error handling for production consumer applications.

### Steps

1. **Create an Error-Prone Consumer**
   ```java
   public class ErrorHandlingConsumer {
       private final Random random = new Random();
       
       private void processMessage(ConsumerRecord<String, String> record) {
           // Simulate random processing errors
           if (random.nextDouble() < 0.3) { // 30% failure rate
               throw new RuntimeException("Simulated processing error");
           }
           
           logger.info("Successfully processed: {}", record.value());
       }
   }
   ```

2. **Implement Retry Logic**
   ```java
   private void processWithRetry(ConsumerRecord<String, String> record) {
       int maxRetries = 3;
       int attempt = 0;
       
       while (attempt < maxRetries) {
           try {
               processMessage(record);
               return; // Success
               
           } catch (Exception e) {
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
           }
       }
   }
   ```

3. **Dead Letter Queue Implementation**
   ```java
   private void sendToDeadLetterQueue(ConsumerRecord<String, String> record, Exception error) {
       ProducerRecord<String, String> dlqRecord = new ProducerRecord<>(
           "dlq-consumer-scaling-demo",
           record.key(),
           record.value()
       );
       
       dlqRecord.headers().add("original-topic", record.topic().getBytes());
       dlqRecord.headers().add("original-partition", String.valueOf(record.partition()).getBytes());
       dlqRecord.headers().add("original-offset", String.valueOf(record.offset()).getBytes());
       dlqRecord.headers().add("error-message", error.getMessage().getBytes());
       
       dlqProducer.send(dlqRecord);
       logger.info("Sent to DLQ: {}", record.value());
   }
   ```

### Expected Results
- Robust error handling with retries
- Failed messages sent to dead letter queue
- No message loss or infinite retry loops

---

## Exercise 5: Performance Optimization

### Objective
Optimize consumer performance for different scenarios.

### Part A: High Throughput Configuration

1. **Batch Processing Consumer**
   ```java
   Properties props = new Properties();
   props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, "50000"); // 50KB minimum
   props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "500"); // Wait max 500ms
   props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1000"); // Large batches
   
   // Process in batches
   ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
   
   List<ConsumerRecord<String, String>> batch = new ArrayList<>();
   for (ConsumerRecord<String, String> record : records) {
       batch.add(record);
       
       if (batch.size() >= 100) {
           processBatch(batch);
           batch.clear();
       }
   }
   if (!batch.isEmpty()) {
       processBatch(batch);
   }
   ```

2. **Multi-threaded Processing**
   ```java
   public class ParallelConsumer {
       private final ExecutorService executor = Executors.newFixedThreadPool(10);
       
       public void processRecords(ConsumerRecords<String, String> records) {
           // Group by partition to maintain ordering
           Map<Integer, List<ConsumerRecord<String, String>>> partitionedRecords = 
               groupByPartition(records);
           
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

### Part B: Low Latency Configuration

1. **Fast Processing Consumer**
   ```java
   Properties props = new Properties();
   props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, "1"); // Process immediately
   props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "100"); // Short wait
   props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "100"); // Smaller batches
   props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "10000"); // Fast failure detection
   props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "3000");
   ```

### Expected Results
- High throughput: Higher messages/second, efficient batching
- Low latency: Faster individual message processing

---

## Exercise 6: Consumer Lag Monitoring

### Objective
Implement comprehensive consumer lag monitoring and alerting.

### Steps

1. **Create Lag Monitoring Consumer**
   ```java
   public class LagMonitoringConsumer {
       public void monitorLag() {
           Map<MetricName, ? extends Metric> metrics = consumer.metrics();
           
           // Key lag metrics
           double recordsLagMax = getMetricValue(metrics, "records-lag-max");
           double recordsLagAvg = getMetricValue(metrics, "records-lag-avg");
           
           logger.info("Consumer Lag Metrics:");
           logger.info("  Max Lag: {} records", recordsLagMax);
           logger.info("  Avg Lag: {} records", recordsLagAvg);
           
           // Alert on high lag
           if (recordsLagMax > 1000) {
               logger.warn("HIGH LAG ALERT: {} records behind", recordsLagMax);
           }
       }
   }
   ```

2. **Administrative Lag Monitoring**
   ```bash
   # Check consumer group lag
   docker-compose exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group multi-consumer-group
   
   # Monitor continuously
   watch -n 5 "docker-compose exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group multi-consumer-group"
   ```

### Expected Results
- Real-time lag monitoring
- Understanding of lag causes and solutions
- Alerting on high lag situations

---

## Exercise 7: Graceful Shutdown and Health Checks

### Objective
Implement production-ready consumer lifecycle management.

### Steps

1. **Graceful Shutdown Implementation**
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
   ```

2. **Health Check Implementation**
   ```java
   public boolean isHealthy() {
       try {
           // Check if consumer can poll
           ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
           
           // Check partition assignment
           Set<TopicPartition> assignment = consumer.assignment();
           if (assignment.isEmpty()) {
               return false;
           }
           
           // Check lag
           Map<MetricName, ? extends Metric> metrics = consumer.metrics();
           double maxLag = getMetricValue(metrics, "records-lag-max");
           return maxLag < 10000; // Acceptable lag threshold
           
       } catch (Exception e) {
           return false;
       }
   }
   ```

### Expected Results
- Clean consumer shutdown with no message loss
- Health monitoring for operational visibility
- Production-ready consumer implementation

---

## Solutions and Analysis

### Exercise 1 Solution: Consumer Group Scaling
```bash
# Expected partition distribution:
# 1 consumer:  Gets all 6 partitions
# 2 consumers: Each gets 3 partitions  
# 3 consumers: Each gets 2 partitions
# 6 consumers: Each gets 1 partition
# 7+ consumers: Some consumers idle
```

### Exercise 2 Solution: Offset Management
```java
// Auto-commit pros/cons:
// + Simple, no code changes
// - May lose messages on crash
// - May reprocess messages

// Manual commit pros/cons:
// + Precise control, can commit after processing
// + Better durability guarantees
// - More complex code
// - Must handle commit failures
```

### Exercise 3 Solution: Error Handling
```java
// Production pattern:
1. Try processing with retries
2. If still fails, send to DLQ
3. Continue with next message
4. Monitor DLQ for pattern analysis
5. Implement circuit breaker for cascading failures
```

## Common Issues and Solutions

### Issue 1: Consumer Lag Building Up
**Symptoms**: `records-lag-max` increasing
**Solutions**:
```bash
# Scale up consumers (up to partition count)
# Optimize processing logic
# Increase fetch size for better batching
# Use parallel processing within consumer
```

### Issue 2: Frequent Rebalancing
**Symptoms**: Logs showing "Revoke" and "Assign" frequently
**Solutions**:
```java
props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "45000");
props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "15000");
props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "300000");
// Reduce processing time per poll
```

### Issue 3: Duplicate Processing
**Symptoms**: Same messages processed multiple times
**Solutions**:
```java
// Implement idempotent processing
// Use manual offset commits
// Implement exactly-once semantics with transactions
```

## Key Learning Outcomes

After completing these exercises, you should understand:

1. **Consumer Groups**: Load balancing and fault tolerance
2. **Offset Management**: Auto vs manual commit trade-offs
3. **Error Handling**: Retries, DLQ patterns, circuit breakers
4. **Performance**: Batching, parallel processing, configuration tuning
5. **Monitoring**: Lag tracking, health checks, alerting
6. **Production Patterns**: Graceful shutdown, lifecycle management

## Cleanup
```bash
# Clean up exercise topics
docker-compose exec kafka kafka-topics --delete --topic consumer-scaling-demo --bootstrap-server localhost:9092

# Clean up consumer groups
docker-compose exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --delete --group single-consumer-group
docker-compose exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --delete --group multi-consumer-group
docker-compose exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --delete --group auto-commit-group
docker-compose exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --delete --group manual-commit-group
```

---

**Next**: [Day 5 Exercises: Stream Processing](./day05-exercises.md)
