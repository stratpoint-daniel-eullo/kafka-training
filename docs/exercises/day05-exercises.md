# Day 5 Exercises: Stream Processing with Kafka Streams

## Exercise 1: Basic Stream Processing

### Objective
Learn fundamental stream processing concepts with Kafka Streams.

### Setup
```bash
# Create input and output topics
docker-compose exec kafka kafka-topics --create --topic stream-input --
  --partitions 3 \
  --replication-factor 1

docker-compose exec kafka kafka-topics --create --topic stream-output --
  --partitions 3 \
  --replication-factor 1

docker-compose exec kafka kafka-topics --create --topic word-count-output --
  --partitions 3 \
  --replication-factor 1
```

### Part A: Run the Stream Processor

1. **Start the Stream Processor**
   ```bash
   mvn exec:java -Dexec.mainClass="com.training.kafka.Day05Streams.StreamProcessor"
   ```

2. **Generate Test Data**
   ```bash
   # Terminal 2: Send user events
   echo "user1:login" | docker-compose exec kafka kafka-console-producer --bootstrap-server localhost:9092 --topic user-events \
     --property "parse.key=true" \
     --property "key.separator=:"
   
   echo "user1:purchase" | docker-compose exec kafka kafka-console-producer --bootstrap-server localhost:9092 --topic user-events \
     --property "parse.key=true" \
     --property "key.separator=:"
     
   echo "user2:login" | docker-compose exec kafka kafka-console-producer --bootstrap-server localhost:9092 --topic user-events \
     --property "parse.key=true" \
     --property "key.separator=:"
   ```

3. **Observe Stream Processing**
   ```bash
   # Terminal 3: Watch processed events
   docker-compose exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic user-activity-summary \
     --property print.key=true \
     --from-beginning
   ```

### Expected Results
- Real-time event processing
- Stream transformations applied
- Aggregated results in output topics

### Questions to Answer
1. How does stream processing differ from batch processing?
2. What happens when you restart the stream application?
3. How are events processed in real-time?

---

## Exercise 2: Word Count Stream Application

### Objective
Build a classic word count application using Kafka Streams.

### Implementation

1. **Create Word Count Streams App**
   ```java
   public class WordCountStreams {
       public static void main(String[] args) {
           Properties props = new Properties();
           props.put(StreamsConfig.APPLICATION_ID_CONFIG, "word-count-app");
           props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
           props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
           props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
           
           StreamsBuilder builder = new StreamsBuilder();
           
           KStream<String, String> source = builder.stream("stream-input");
           
           KTable<String, Long> wordCounts = source
               .flatMapValues(value -> Arrays.asList(value.toLowerCase().split("\\s+")))
               .groupBy((key, word) -> word)
               .count();
           
           wordCounts.toStream().to("word-count-output");
           
           KafkaStreams streams = new KafkaStreams(builder.build(), props);
           streams.start();
           
           Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
       }
   }
   ```

2. **Test Word Count**
   ```bash
   # Send text messages
   echo "hello world" | docker-compose exec kafka kafka-console-producer --bootstrap-server localhost:9092 --topic stream-input
   echo "hello kafka streams" | docker-compose exec kafka kafka-console-producer --bootstrap-server localhost:9092 --topic stream-input
   echo "world of streaming" | docker-compose exec kafka kafka-console-producer --bootstrap-server localhost:9092 --topic stream-input
   
   # Watch word counts
   docker-compose exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic word-count-output \
     --property print.key=true \
     --from-beginning
   ```

### Expected Results
```
hello	1
world	1
hello	2
kafka	1
streams	1
world	2
of	1
streaming	1
```

---

## Exercise 3: Windowed Aggregations

### Objective
Implement time-based windowed aggregations for analytics.

### Implementation

1. **Create Windowed Analytics App**
   ```java
   public class WindowedAnalytics {
       public static void main(String[] args) {
           StreamsBuilder builder = new StreamsBuilder();
           
           KStream<String, String> events = builder.stream("user-events");
           
           // Parse JSON events
           KStream<String, UserEvent> userEvents = events
               .mapValues(value -> parseUserEvent(value))
               .filter((key, event) -> event != null);
           
           // Count events per user in 5-minute windows
           KTable<Windowed<String>, Long> windowedCounts = userEvents
               .groupBy((key, event) -> event.getUserId())
               .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(5)))
               .count();
           
           // Output windowed results
           windowedCounts
               .toStream()
               .map((windowedKey, count) -> KeyValue.pair(
                   windowedKey.key() + "@" + windowedKey.window().start(),
                   count.toString()
               ))
               .to("windowed-user-counts");
           
           KafkaStreams streams = new KafkaStreams(builder.build(), props);
           streams.start();
       }
       
       private static UserEvent parseUserEvent(String json) {
           // JSON parsing logic
           try {
               return objectMapper.readValue(json, UserEvent.class);
           } catch (Exception e) {
               return null;
           }
       }
   }
   ```

2. **Generate Time-Series Data**
   ```bash
   # Send events with timestamps
   for i in {1..20}; do
     echo "{\"userId\":\"user$((i % 3))\",\"action\":\"click\",\"timestamp\":$(date +%s)000}" | \
       docker-compose exec kafka kafka-console-producer --bootstrap-server localhost:9092 --topic user-events
     sleep 2
   done
   ```

3. **Monitor Windowed Results**
   ```bash
   docker-compose exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic windowed-user-counts \
     --property print.key=true \
     --from-beginning
   ```

### Expected Results
- Events grouped by 5-minute windows
- Counts per user per window
- Understanding of windowing concepts

---

## Exercise 4: Stream-Stream Joins

### Objective
Implement joins between different streams for event correlation.

### Setup
```bash
# Create topics for join exercise
docker-compose exec kafka kafka-topics --create --topic user-clicks --
  --partitions 3 \
  --replication-factor 1

docker-compose exec kafka kafka-topics --create --topic user-purchases --
  --partitions 3 \
  --replication-factor 1

docker-compose exec kafka kafka-topics --create --topic click-purchase-correlation --
  --partitions 3 \
  --replication-factor 1
```

### Implementation

1. **Create Stream Join Application**
   ```java
   public class StreamJoinExample {
       public static void main(String[] args) {
           StreamsBuilder builder = new StreamsBuilder();
           
           KStream<String, String> clicks = builder.stream("user-clicks");
           KStream<String, String> purchases = builder.stream("user-purchases");
           
           // Join clicks and purchases within 10-minute window
           KStream<String, String> correlatedEvents = clicks.join(
               purchases,
               (clickValue, purchaseValue) -> correlateEvents(clickValue, purchaseValue),
               JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofMinutes(10))
           );
           
           correlatedEvents.to("click-purchase-correlation");
           
           KafkaStreams streams = new KafkaStreams(builder.build(), props);
           streams.start();
       }
       
       private static String correlateEvents(String click, String purchase) {
           return String.format("{\"click\":%s,\"purchase\":%s,\"correlation_time\":%d}", 
               click, purchase, System.currentTimeMillis());
       }
   }
   ```

2. **Test Stream Joins**
   ```bash
   # Send coordinated events
   echo "user1:{\"page\":\"product-123\",\"timestamp\":$(date +%s)000}" | \
     docker-compose exec kafka kafka-console-producer --bootstrap-server localhost:9092 --topic user-clicks \
     --property "parse.key=true" \
     --property "key.separator=:"
   
   sleep 5
   
   echo "user1:{\"product\":\"product-123\",\"amount\":99.99,\"timestamp\":$(date +%s)000}" | \
     docker-compose exec kafka kafka-console-producer --bootstrap-server localhost:9092 --topic user-purchases \
     --property "parse.key=true" \
     --property "key.separator=:"
   
   # Watch correlations
   docker-compose exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic click-purchase-correlation \
     --property print.key=true \
     --from-beginning
   ```

### Expected Results
- Successful join of related events
- Understanding of join windows
- Event correlation patterns

---

## Exercise 5: Exactly-Once Processing

### Objective
Implement exactly-once semantics for critical stream processing.

### Implementation

1. **Configure Exactly-Once**
   ```java
   Properties props = new Properties();
   props.put(StreamsConfig.APPLICATION_ID_CONFIG, "exactly-once-app");
   props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
   props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2);
   props.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 1);
   props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10000);
   
   StreamsBuilder builder = new StreamsBuilder();
   
   KStream<String, String> source = builder.stream("financial-transactions");
   
   // Critical financial processing
   KStream<String, String> processedTransactions = source
       .filter((key, value) -> isValidTransaction(value))
       .mapValues(value -> processTransaction(value))
       .filter((key, value) -> value != null);
   
   processedTransactions.to("processed-transactions");
   
   KafkaStreams streams = new KafkaStreams(builder.build(), props);
   streams.start();
   ```

2. **Test Exactly-Once Behavior**
   ```bash
   # Create transaction topic
   docker-compose exec kafka kafka-topics --create --topic financial-transactions --
     --partitions 3 \
     --replication-factor 1
   
   # Send test transactions
   echo "txn1:{\"amount\":100.00,\"from\":\"acc1\",\"to\":\"acc2\"}" | \
     docker-compose exec kafka kafka-console-producer --bootstrap-server localhost:9092 --topic financial-transactions \
     --property "parse.key=true" \
     --property "key.separator=:"
   
   # Verify no duplicates even with restarts
   ```

### Expected Results
- No duplicate processing even with failures
- Transactional semantics maintained
- Understanding of exactly-once guarantees

---

## Exercise 6: Real-Time Analytics Dashboard

### Objective
Build a complete real-time analytics pipeline.

### Implementation

1. **Create Analytics Streams**
   ```java
   public class RealTimeAnalytics {
       public static void main(String[] args) {
           StreamsBuilder builder = new StreamsBuilder();
           
           KStream<String, String> events = builder.stream("user-events");
           
           // Real-time metrics
           
           // 1. Events per minute
           KTable<Windowed<String>, Long> eventsPerMinute = events
               .groupBy((key, value) -> "global")
               .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(1)))
               .count();
           
           // 2. Top users by activity
           KTable<String, Long> userActivity = events
               .groupByKey()
               .count();
           
           // 3. Action type distribution
           KTable<String, Long> actionCounts = events
               .mapValues(value -> extractAction(value))
               .filter((key, action) -> action != null)
               .groupBy((key, action) -> action)
               .count();
           
           // Output metrics
           eventsPerMinute
               .toStream()
               .map((windowedKey, count) -> KeyValue.pair(
                   "events_per_minute@" + windowedKey.window().start(),
                   count.toString()
               ))
               .to("analytics-events-per-minute");
           
           userActivity
               .toStream()
               .to("analytics-user-activity");
           
           actionCounts
               .toStream()
               .to("analytics-action-counts");
           
           KafkaStreams streams = new KafkaStreams(builder.build(), props);
           streams.start();
       }
   }
   ```

2. **Monitor Real-Time Analytics**
   ```bash
   # Watch metrics in real-time
   docker-compose exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic analytics-events-per-minute \
     --property print.key=true &
   
   docker-compose exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic analytics-user-activity \
     --property print.key=true &
   
   docker-compose exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic analytics-action-counts \
     --property print.key=true &
   ```

### Expected Results
- Real-time analytics dashboard
- Multiple metric streams
- Understanding of stream analytics patterns

---

## Exercise 7: Testing Stream Applications

### Objective
Learn to test stream processing applications effectively.

### Implementation

1. **Create Test Class**
   ```java
   public class StreamProcessingTest {
       
       @Test
       public void testWordCount() {
           StreamsBuilder builder = new StreamsBuilder();
           KStream<String, String> source = builder.stream("input");
           
           KTable<String, Long> wordCounts = source
               .flatMapValues(value -> Arrays.asList(value.toLowerCase().split(" ")))
               .groupBy((key, word) -> word)
               .count();
           
           wordCounts.toStream().to("output");
           
           Properties props = new Properties();
           props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test-app");
           props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
           props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
           props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
           
           try (TopologyTestDriver testDriver = new TopologyTestDriver(builder.build(), props)) {
               TestInputTopic<String, String> inputTopic = 
                   testDriver.createInputTopic("input", 
                       Serdes.String().serializer(), 
                       Serdes.String().serializer());
               
               TestOutputTopic<String, Long> outputTopic = 
                   testDriver.createOutputTopic("output", 
                       Serdes.String().deserializer(), 
                       Serdes.Long().deserializer());
               
               // Send test data
               inputTopic.pipeInput("key1", "hello world hello");
               
               // Verify output
               KeyValue<String, Long> result1 = outputTopic.readKeyValue();
               assertEquals("hello", result1.key);
               assertEquals(1L, result1.value);
               
               KeyValue<String, Long> result2 = outputTopic.readKeyValue();
               assertEquals("world", result2.key);
               assertEquals(1L, result2.value);
               
               KeyValue<String, Long> result3 = outputTopic.readKeyValue();
               assertEquals("hello", result3.key);
               assertEquals(2L, result3.value);
           }
       }
   }
   ```

2. **Run Stream Tests**
   ```bash
   mvn test -Dtest=StreamProcessingTest
   ```

### Expected Results
- Comprehensive stream application testing
- Understanding of TopologyTestDriver
- Test-driven stream development

---

## Solutions and Analysis

### Exercise 1 Solution: Stream Processing Basics
```java
// Key concepts demonstrated:
// - Real-time processing vs batch
// - Stream transformations (filter, map, groupBy)
// - Fault tolerance through state restoration
// - Exactly-once processing guarantees
```

### Exercise 2 Solution: Word Count Analysis
```java
// Expected word count progression:
// Input: "hello world" → Output: hello:1, world:1
// Input: "hello kafka" → Output: hello:2, kafka:1
// Key insight: KTable maintains running counts
```

### Exercise 3 Solution: Windowed Aggregations
```java
// Window behavior:
// 5-minute tumbling windows
// Each user's events counted per window
// Late events handled gracefully
// Windows close after grace period
```

### Exercise 4 Solution: Stream Joins
```java
// Join semantics:
// Inner join: Both streams must have matching key
// Time window: Events must occur within specified window
// Result: Combined data from both streams
```

## Common Issues and Solutions

### Issue 1: Stream Application Won't Start
**Symptoms**: Application fails to start or gets stuck
**Solutions**:
```java
// Check application.id uniqueness
props.put(StreamsConfig.APPLICATION_ID_CONFIG, "unique-app-id");

// Verify topic existence
// Ensure proper serializers configured
```

### Issue 2: High Memory Usage
**Symptoms**: OutOfMemoryError, high heap usage
**Solutions**:
```java
// Configure state store caching
props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 10 * 1024 * 1024); // 10MB

// Set commit interval
props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 30000); // 30 seconds
```

### Issue 3: Processing Lag
**Symptoms**: High processing latency, growing lag
**Solutions**:
```java
// Scale up stream threads
props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 4);

// Optimize processing logic
// Add more instances of the application
```

## Performance Optimization Tips

### 1. Threading Configuration
```java
// Optimize thread count
props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 
    Math.min(Runtime.getRuntime().availableProcessors(), partitionCount));
```

### 2. State Store Tuning
```java
// RocksDB tuning
Map<String, Object> rocksDBConfig = new HashMap<>();
rocksDBConfig.put("block_cache_size", 50 * 1024 * 1024L); // 50MB
props.put(StreamsConfig.ROCKSDB_CONFIG_SETTER_CLASS_CONFIG, CustomRocksDBConfig.class);
```

### 3. Windowing Optimization
```java
// Use grace period for late events
TimeWindows.ofSizeAndGrace(Duration.ofMinutes(5), Duration.ofMinutes(1));

// Retention settings
Materialized.<String, Long, WindowStore<Bytes, byte[]>>as("windowed-store")
    .withRetention(Duration.ofHours(24));
```

## Key Learning Outcomes

After completing these exercises, you should understand:

1. **Stream Processing Fundamentals**: Real-time vs batch processing
2. **Kafka Streams DSL**: High-level API for stream transformations
3. **Windowing**: Time-based aggregations and late event handling
4. **Joins**: Stream-stream and stream-table join patterns
5. **State Management**: Stateful operations and state stores
6. **Exactly-Once**: Transactional processing guarantees
7. **Testing**: TopologyTestDriver for unit testing streams
8. **Monitoring**: Stream application health and performance

## Cleanup
```bash
# Clean up exercise topics
docker-compose exec kafka kafka-topics --delete --topic stream-input --bootstrap-server localhost:9092
docker-compose exec kafka kafka-topics --delete --topic stream-output --bootstrap-server localhost:9092
docker-compose exec kafka kafka-topics --delete --topic word-count-output --bootstrap-server localhost:9092
docker-compose exec kafka kafka-topics --delete --topic user-clicks --bootstrap-server localhost:9092
docker-compose exec kafka kafka-topics --delete --topic user-purchases --bootstrap-server localhost:9092
docker-compose exec kafka kafka-topics --delete --topic click-purchase-correlation --bootstrap-server localhost:9092
docker-compose exec kafka kafka-topics --delete --topic financial-transactions --bootstrap-server localhost:9092
docker-compose exec kafka kafka-topics --delete --topic processed-transactions --bootstrap-server localhost:9092
docker-compose exec kafka kafka-topics --delete --topic windowed-user-counts --bootstrap-server localhost:9092

# Stop stream applications (Ctrl+C)
```

---

**Next**: [Day 6 Exercises: Schema Management](./day06-exercises.md)
