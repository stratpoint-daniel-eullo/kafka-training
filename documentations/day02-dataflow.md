# Day 2: Data Flow and Message Patterns

## Learning Objectives
By the end of Day 2, you will:
- Understand producer and consumer semantics
- Master offset management and consumer groups
- Learn about message ordering and delivery guarantees
- Implement different partitioning strategies
- Handle consumer rebalancing

## Morning Session (3 hours): Producer and Consumer Fundamentals

### 1. Producer Semantics

#### Message Delivery Guarantees
- **At most once**: Messages may be lost but never duplicated
- **At least once**: Messages are never lost but may be duplicated
- **Exactly once**: Messages are delivered exactly once (requires idempotent producer + transactions)

#### Key Producer Configurations
```properties
# Reliability settings
acks=all                    # Wait for all replicas to acknowledge
retries=3                   # Number of retry attempts
retry.backoff.ms=100       # Time between retries

# Performance settings
linger.ms=10               # Wait time to batch messages
batch.size=16384           # Batch size in bytes
compression.type=snappy    # Compression algorithm

# Idempotence
enable.idempotence=true    # Exactly-once semantics
```

#### Partitioning Strategies
1. **Round-robin**: No key specified, messages distributed evenly
2. **Key-based**: Same key always goes to same partition
3. **Custom partitioner**: Custom logic for partition assignment

### 2. Consumer Semantics

#### Consumer Groups
- Consumers in same group share partition assignment
- Each partition consumed by only one consumer in group
- Automatic rebalancing when consumers join/leave

#### Offset Management
- **Auto-commit**: Offsets committed automatically at intervals
- **Manual commit**: Application controls when to commit offsets
- **Commit strategies**: Sync vs Async, per-message vs batch

### 3. Message Ordering

#### Partition-Level Ordering
- Messages within a partition are ordered
- Cross-partition ordering not guaranteed
- Use keys to ensure related messages go to same partition

#### Consumer Ordering
- Single consumer per partition maintains order
- Multiple consumers may process out of order
- Use single-threaded processing for strict ordering

## Afternoon Session (3 hours): Hands-on Exercises

### Exercise 1: Producer Patterns

Let's explore different producer configurations and their effects:

```bash
# Terminal 1: Start a consumer to observe messages
confluent local kafka topic consume user-events --from-beginning

# Terminal 2: Producer experiments
```

#### Experiment 1: Round-Robin Partitioning
```bash
# Produce messages without keys
for i in {1..10}; do
  echo "Message $i without key" | confluent local kafka topic produce user-events
done
```

#### Experiment 2: Key-Based Partitioning
```bash
# Produce messages with keys
for user in user1 user2 user3; do
  for i in {1..5}; do
    echo "$user:Action $i" | confluent local kafka topic produce user-events \
      --property "parse.key=true" \
      --property "key.separator=:"
  done
done
```

### Exercise 2: Consumer Groups

#### Setup Multiple Consumer Groups
```bash
# Terminal 1: Consumer Group A
confluent local kafka topic consume user-events \
  --group consumer-group-a \
  --from-beginning

# Terminal 2: Consumer Group B  
confluent local kafka topic consume user-events \
  --group consumer-group-b \
  --from-beginning

# Terminal 3: Second consumer in Group A
confluent local kafka topic consume user-events \
  --group consumer-group-a \
  --from-beginning
```

Observe how:
- Each group receives all messages independently
- Consumers within a group share the load
- Rebalancing occurs when consumers join/leave

### Exercise 3: Offset Management

#### Manual Offset Control
```bash
# Consumer with manual offset commits (no auto-commit)
confluent local kafka topic consume user-events \
  --group manual-commit-group \
  --from-beginning \
  --property "enable.auto.commit=false"
```

### Exercise 4: Consumer Lag Monitoring

```bash
# Check consumer group status
confluent local kafka consumer group describe consumer-group-a

# List all consumer groups
confluent local kafka consumer group list
```

## Practical Scenarios

### Scenario 1: Order Processing System

**Requirements:**
- Orders from same customer must be processed in sequence
- Multiple customers can be processed in parallel
- No order should be lost

**Solution:**
- Use customer ID as message key
- Configure producer with `acks=all` and `retries=3`
- Use manual offset commits in consumer

### Scenario 2: Real-time Analytics

**Requirements:**
- High throughput is priority
- Some message loss acceptable
- Low latency required

**Solution:**
- Use `acks=1` for faster acknowledgment
- Larger batch sizes and linger time
- Auto-commit offsets for simplicity

### Scenario 3: Financial Transactions

**Requirements:**
- Exactly-once processing required
- No message loss or duplication
- Audit trail needed

**Solution:**
- Enable idempotent producer
- Use transactions for exactly-once semantics
- Manual offset management with database transactions

## CLI Commands Reference

### Topic Management
```bash
# Create topic with specific configuration
confluent local kafka topic create orders \
  --partitions 6 \
  --replication-factor 1 \
  --config retention.ms=86400000 \
  --config compression.type=snappy

# Update topic configuration
confluent local kafka topic update orders \
  --config segment.ms=3600000

# View topic configuration
confluent local kafka topic configuration list orders
```

### Producer Operations
```bash
# Basic producer
confluent local kafka topic produce orders

# Producer with properties
confluent local kafka topic produce orders \
  --property "parse.key=true" \
  --property "key.separator=:" \
  --property "compression.type=snappy"

# Producer with headers
echo "key:value" | confluent local kafka topic produce orders \
  --property "parse.key=true" \
  --property "key.separator=:" \
  --property "parse.headers=true" \
  --property "headers.separator=|" \
  --property "headers.key.separator=:"
```

### Consumer Operations
```bash
# Basic consumer
confluent local kafka topic consume orders

# Consumer with specific offset
confluent local kafka topic consume orders \
  --from-beginning \
  --max-messages 10

# Consumer with properties
confluent local kafka topic consume orders \
  --group my-group \
  --property "auto.offset.reset=earliest" \
  --property "session.timeout.ms=30000"
```

### Consumer Group Management
```bash
# List consumer groups
confluent local kafka consumer group list

# Describe consumer group
confluent local kafka consumer group describe my-group

# Reset consumer group offsets
confluent local kafka consumer group reset my-group \
  --topic orders \
  --to-earliest

# Delete consumer group
confluent local kafka consumer group delete my-group
```

## Performance Considerations

### Producer Performance
- **Batching**: Increase `batch.size` and `linger.ms` for higher throughput
- **Compression**: Use `snappy` or `lz4` for better performance
- **Async sending**: Use callbacks instead of blocking sends
- **Connection pooling**: Reuse producer instances

### Consumer Performance
- **Fetch size**: Tune `fetch.min.bytes` and `fetch.max.wait.ms`
- **Processing parallelism**: Scale consumer instances
- **Commit frequency**: Balance between performance and data loss risk
- **Deserialization**: Use efficient serialization formats

## Error Handling Patterns

### Producer Error Handling
```bash
# Simulate broker failure
confluent local kafka stop

# Try to produce (will retry and eventually fail)
echo "test message" | confluent local kafka topic produce orders

# Check producer metrics and logs
```

### Consumer Error Handling
```bash
# Simulate processing failure
# (Consumer should continue from last committed offset)
```

## Monitoring and Observability

### Key Metrics to Monitor

**Producer Metrics:**
- Message send rate
- Batch size
- Error rate
- Request latency

**Consumer Metrics:**
- Consumer lag
- Processing rate
- Commit rate
- Rebalance frequency

### Using CLI for Monitoring
```bash
# Check topic details
confluent local kafka topic describe orders

# Monitor consumer lag
watch -n 1 "confluent local kafka consumer group describe my-group"

# Check broker logs
confluent local services kafka log
```

## Key Takeaways

1. **Message ordering** is guaranteed only within partitions
2. **Consumer groups** enable horizontal scaling and fault tolerance
3. **Offset management** strategy depends on durability requirements
4. **Partitioning strategy** affects both performance and ordering
5. **Delivery semantics** must match application requirements
6. **Monitoring** is essential for production operations

## Troubleshooting Guide

### Common Issues

1. **Consumer lag building up**
   - Scale up consumer instances
   - Optimize processing logic
   - Increase partition count

2. **Messages out of order**
   - Check partitioning strategy
   - Ensure single consumer per partition for ordering
   - Use message keys appropriately

3. **Rebalancing issues**
   - Tune session timeout and heartbeat interval
   - Reduce processing time per message
   - Monitor consumer group stability

## Next Steps

Tomorrow we'll dive into:
- Java Producer implementation patterns
- Advanced producer configurations
- Error handling and retry mechanisms
- Production-ready producer examples

---

**🚀 Ready for Day 3?** Continue with [Day 3: Java Producers](./day03-producers.md)