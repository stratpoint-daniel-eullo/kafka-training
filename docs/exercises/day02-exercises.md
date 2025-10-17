# Day 2 Exercises: Data Flow and Message Patterns

## Exercise 1: Producer Partitioning Strategies

### Objective
Understand how message keys affect partitioning and learn different partitioning strategies.

### Setup
```bash
# Create topic for partitioning experiments
docker-compose exec kafka kafka-topics --create --topic partitioning-lab --
  --partitions 4 \
  --replication-factor 1

# Start a consumer to observe partition assignment
docker-compose exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic partitioning-lab \
  --group partitioning-observers \
  --property print.partition=true \
  --property print.offset=true \
  --property print.key=true
```

### Part A: Round-Robin vs Key-Based Partitioning

1. **Run the Interactive Producer**
   ```bash
   mvn exec:java -Dexec.mainClass="com.training.kafka.Day02DataFlow.InteractiveProducer"
   ```

2. **Test Round-Robin (No Keys)**
   - The demo will automatically show round-robin partitioning
   - Observe how messages without keys are distributed evenly

3. **Test Key-Based Partitioning**
   - The demo will show how messages with same keys go to same partitions
   - Notice the consistent partition assignment for each key

### Part B: Interactive Experimentation
1. Use the interactive mode in the producer
2. Try these patterns:
   ```
   # Messages without keys (round-robin)
   message 1
   message 2  
   message 3
   
   # Messages with keys (key-based)
   user1:login
   user2:login
   user1:purchase
   user2:logout
   user1:logout
   ```

3. **Observe the Results**
   - Messages without keys should be distributed across partitions
   - Messages with same keys should go to the same partition
   - Different keys may go to different partitions

### Expected Results
- Understanding of how partitioning affects message distribution
- Recognition that key consistency is important for ordering
- Appreciation for the trade-offs between distribution and ordering

### Questions to Answer
1. Which partitions did each key map to?
2. What happens if you send many messages with the same key?
3. How would you ensure related messages are processed in order?

---

## Exercise 2: Consumer Groups and Load Balancing

### Objective
Explore how consumer groups distribute work and handle rebalancing.

### Setup
```bash
# Make sure we have data to consume
mvn exec:java -Dexec.mainClass="com.training.kafka.Day02DataFlow.InteractiveProducer"
# (Let it generate some demo data, then quit)
```

### Part A: Single Consumer vs Consumer Group

1. **Single Consumer**
   ```bash
   # Terminal 1: Single consumer reads all partitions
   docker-compose exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic partitioning-lab \
     --group single-consumer-group \
     --property print.partition=true \
     --from-beginning
   ```

2. **Multiple Consumers in Same Group**
   ```bash
   # Terminal 1: First consumer
   docker-compose exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic partitioning-lab \
     --group multi-consumer-group \
     --property print.partition=true \
     --from-beginning
   
   # Terminal 2: Second consumer (start after 10 seconds)
   docker-compose exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic partitioning-lab \
     --group multi-consumer-group \
     --property print.partition=true \
     --from-beginning
   
   # Terminal 3: Third consumer (start after another 10 seconds)
   docker-compose exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic partitioning-lab \
     --group multi-consumer-group \
     --property print.partition=true \
     --from-beginning
   ```

3. **Multiple Consumer Groups**
   ```bash
   # Terminal 4: Different group (gets all messages)
   docker-compose exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic partitioning-lab \
     --group different-consumer-group \
     --property print.partition=true \
     --from-beginning
   ```

### Part B: Consumer Data Flow Demo

1. **Run the Consumer Demo**
   ```bash
   mvn exec:java -Dexec.mainClass="com.training.kafka.Day02DataFlow.ConsumerDataFlowDemo"
   ```

2. **Observe the Different Demonstrations**
   - Offset management (auto vs manual commit)
   - Message ordering within partitions
   - Offset seeking capabilities
   - Consumer group behavior

### Expected Results
- Understand how consumer groups share partition load
- See rebalancing in action when consumers join/leave
- Recognize that each group gets independent copies of data
- Understand offset management strategies

### Questions to Answer
1. How were partitions distributed among consumers?
2. What happened when you added/removed consumers?
3. How do different consumer groups relate to each other?
4. When would you use auto-commit vs manual commit?

---

## Exercise 3: Message Ordering and Delivery Guarantees

### Objective
Understand ordering guarantees and delivery semantics in Kafka.

### Part A: Ordering Analysis

1. **Generate Test Data with Keys**
   ```bash
   # Use interactive producer to create ordered sequences
   mvn exec:java -Dexec.mainClass="com.training.kafka.Day02DataFlow.InteractiveProducer"
   ```

2. **In interactive mode, send this sequence:**
   ```
   order-123:created
   order-123:validated
   order-123:paid
   order-123:shipped
   order-123:delivered
   order-456:created
   order-456:validated
   order-456:paid
   order-456:cancelled
   ```

3. **Consume and Verify Ordering**
   ```bash
   docker-compose exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic partitioning-lab \
     --group ordering-test-group \
     --property print.partition=true \
     --property print.offset=true \
     --property print.key=true \
     --from-beginning
   ```

### Part B: Delivery Semantics Testing

1. **Test Different Acknowledgment Levels**
   The Interactive Producer demo includes delivery semantics demonstration
   - At-most-once (acks=0)
   - At-least-once (acks=all with retries)

2. **Simulate Failures**
   ```bash
   # While producer is running, stop Kafka briefly
   docker-compose stop kafka
   
   # Restart after 10 seconds
   docker-compose up -d kafka
   
   # Observe producer retry behavior
   ```

### Expected Results
- Messages with same keys maintain order within partitions
- Cross-partition ordering is not guaranteed
- Understanding of delivery guarantee trade-offs
- Recognition of retry and failure handling importance

### Questions to Answer
1. Were the order-123 messages processed in sequence?
2. What happened to the order of messages across different keys?
3. How did the producer handle the Kafka restart?

---

## Exercise 4: Consumer Offset Management

### Objective
Master different offset management strategies and their implications.

### Part A: Auto-Commit vs Manual Commit

1. **Create Test Data**
   ```bash
   # Generate some test messages
   echo "auto-commit-test-1" | docker-compose exec kafka kafka-console-producer --bootstrap-server localhost:9092 --topic partitioning-lab
   echo "auto-commit-test-2" | docker-compose exec kafka kafka-console-producer --bootstrap-server localhost:9092 --topic partitioning-lab
   echo "auto-commit-test-3" | docker-compose exec kafka kafka-console-producer --bootstrap-server localhost:9092 --topic partitioning-lab
   ```

2. **Test Auto-Commit Behavior**
   ```bash
   # Consumer with auto-commit (default)
   docker-compose exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic partitioning-lab \
     --group auto-commit-test \
     --property enable.auto.commit=true \
     --property auto.commit.interval.ms=5000 \
     --max-messages 2
   
   # Restart same consumer - should not re-read messages
   docker-compose exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic partitioning-lab \
     --group auto-commit-test \
     --property enable.auto.commit=true
   ```

3. **Test Manual Commit Behavior**
   ```bash
   # Consumer with manual commit
   docker-compose exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic partitioning-lab \
     --group manual-commit-test \
     --property enable.auto.commit=false \
     --max-messages 2
   
   # Restart same consumer - may re-read messages
   docker-compose exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic partitioning-lab \
     --group manual-commit-test \
     --property enable.auto.commit=false
   ```

### Part B: Offset Seeking

1. **Use the Consumer Demo's Seeking Feature**
   ```bash
   mvn exec:java -Dexec.mainClass="com.training.kafka.Day02DataFlow.ConsumerDataFlowDemo"
   ```
   
   The demo will show:
   - Seeking to beginning
   - Seeking to end
   - Seeking to specific offsets

2. **Manual Offset Seeking**
   ```bash
   # Reset consumer group to beginning
   docker-compose exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --group seeking-test-group --reset-offsets --
     --topic partitioning-lab \
     --to-earliest
   
   # Reset to specific offset
   docker-compose exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --group seeking-test-group --reset-offsets --
     --topic partitioning-lab \
     --to-offset 5
   ```

### Expected Results
- Understanding of commit timing and durability
- Knowledge of when messages might be reprocessed
- Ability to control consumer starting position
- Recognition of offset management trade-offs

---

## Solutions and Analysis

### Exercise 1 Solution: Partitioning Analysis
```java
// Expected partitioning results (will vary based on hash function):
// - Messages without keys: Round-robin across partitions 0,1,2,3
// - user1 messages: All go to same partition (e.g., partition 2)
// - user2 messages: All go to same partition (e.g., partition 0)
// Key insight: Same key = same partition = ordering guarantee
```

### Exercise 2 Solution: Consumer Group Behavior
```bash
# Expected behavior:
# 1 consumer:  Gets all 4 partitions (0,1,2,3)
# 2 consumers: Each gets 2 partitions (Consumer1: 0,1; Consumer2: 2,3)
# 3 consumers: Distribution like (Consumer1: 0,1; Consumer2: 2; Consumer3: 3)
# 4+ consumers: Some consumers idle (max consumers = partition count)
```

### Exercise 3 Solution: Message Ordering
```
# Expected ordering within partitions:
Partition X: order-123:created → order-123:validated → order-123:paid → order-123:shipped → order-123:delivered
Partition Y: order-456:created → order-456:validated → order-456:paid → order-456:cancelled

# Key insight: Ordering guaranteed within partition, not across partitions
```

### Exercise 4 Solution: Offset Management
```java
// Auto-commit: Offsets committed automatically every 5 seconds
// - Pro: Simple, no code changes needed
// - Con: May lose messages or reprocess on failure

// Manual commit: Application controls when to commit
// - Pro: Precise control, can commit after processing
// - Con: More complex code, must handle failures
```

## Real-World Scenarios

### Scenario 1: E-commerce Order Processing
```
Challenge: Process orders in sequence per customer
Solution: Use customer ID as message key
Result: All orders for customer123 go to same partition = ordered processing
```

### Scenario 2: IoT Sensor Data
```
Challenge: High volume sensor data, some loss acceptable
Solution: Use acks=0 or acks=1 for speed
Result: High throughput with minimal latency
```

### Scenario 3: Financial Transactions
```
Challenge: No message loss, no duplicates
Solution: Use acks=all + idempotent producer + manual commits
Result: Exactly-once processing semantics
```

## Troubleshooting Guide

### Issue 1: Consumer Not Receiving Messages
**Check:**
```bash
# Verify topic has data
docker-compose exec kafka kafka-topics --describe --topic partitioning-lab --bootstrap-server localhost:9092

# Check consumer group status
docker-compose exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group your-group-name

# Verify consumer group hasn't already consumed all messages
```

### Issue 2: Messages Not Ordered
**Solution:**
- Ensure related messages use same key
- Verify single consumer per partition for strict ordering
- Check that producer isn't using async sending incorrectly

### Issue 3: Consumer Lag Building Up
**Solutions:**
```bash
# Check lag
docker-compose exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group your-group-name

# Solutions:
# 1. Add more consumers (up to partition count)
# 2. Optimize consumer processing speed
# 3. Increase partition count (requires topic recreation)
```

## Key Learning Outcomes

After completing these exercises, you should understand:

1. **Partitioning Strategy Impact**: How keys affect message distribution and ordering
2. **Consumer Group Dynamics**: Load balancing and rebalancing behavior
3. **Ordering Guarantees**: Partition-level ordering vs global ordering
4. **Delivery Semantics**: Trade-offs between speed, reliability, and complexity
5. **Offset Management**: Auto vs manual commit implications
6. **Practical Trade-offs**: Real-world scenarios and solution patterns

## Cleanup
```bash
# Clean up exercise topics
docker-compose exec kafka kafka-topics --delete --topic partitioning-lab --bootstrap-server localhost:9092

# Clean up consumer groups
docker-compose exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --delete --group single-consumer-group
docker-compose exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --delete --group multi-consumer-group
docker-compose exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --delete --group different-consumer-group
docker-compose exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --delete --group ordering-test-group
docker-compose exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --delete --group auto-commit-test
docker-compose exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --delete --group manual-commit-test
```

---

**Next**: [Day 3 Exercises: Producer Development](./day03-exercises.md)