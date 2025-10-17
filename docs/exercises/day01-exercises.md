# Day 1 Exercises: Kafka Fundamentals

## Exercise 1: Basic Topic Operations

### Objective
Learn to create, list, and describe topics using both CLI and Java AdminClient.

### Steps
1. **Create topics using Docker Compose and Kafka CLI**
   ```bash
   # Create a topic with specific configuration
   docker-compose exec kafka kafka-topics \
     --create \
     --topic exercise-1-topic \
     --bootstrap-server localhost:9092 \
     --partitions 4 \
     --replication-factor 1 \
     --config retention.ms=86400000 \
     --config compression.type=gzip

   # Create another topic for comparison
   docker-compose exec kafka kafka-topics \
     --create \
     --topic exercise-1-compare \
     --bootstrap-server localhost:9092 \
     --partitions 2 \
     --replication-factor 1
   ```

2. **List and examine topics**
   ```bash
   # List all topics
   docker-compose exec kafka kafka-topics \
     --list \
     --bootstrap-server localhost:9092

   # Describe your topics
   docker-compose exec kafka kafka-topics \
     --describe \
     --topic exercise-1-topic \
     --bootstrap-server localhost:9092

   docker-compose exec kafka kafka-topics \
     --describe \
     --topic exercise-1-compare \
     --bootstrap-server localhost:9092
   ```

3. **Use Java AdminClient**
   ```bash
   # Run the BasicTopicOperations example
   mvn exec:java -Dexec.mainClass="com.training.kafka.Day01Foundation.BasicTopicOperations"
   ```

### Expected Results
- Topics are created with correct partition counts
- Topic configurations are applied properly
- Java AdminClient can list and describe topics
- You understand the relationship between partitions and parallelism

### Questions to Answer
1. How many partitions does each topic have?
2. What happens if you try to create a topic that already exists?
3. Which approach (CLI vs Java) would you use in production automation?

---

## Exercise 2: Understanding Partitions and Leaders

### Objective
Explore how partitions work and understand partition leadership.

### Steps
1. **Create a multi-partition topic**
   ```bash
   docker-compose exec kafka kafka-topics \
     --create \
     --topic partition-demo \
     --bootstrap-server localhost:9092 \
     --partitions 6 \
     --replication-factor 1
   ```

2. **Examine partition details**
   ```bash
   docker-compose exec kafka kafka-topics \
     --describe \
     --topic partition-demo \
     --bootstrap-server localhost:9092
   ```

3. **Modify BasicTopicOperations to show more details**
   Create a new method to analyze partition distribution:
   ```java
   public void analyzePartitionDistribution(String topicName) {
       try {
           DescribeTopicsResult result = adminClient.describeTopics(Collections.singletonList(topicName));
           TopicDescription description = result.topicNameValues().get(topicName).get();
           
           logger.info("=== Partition Analysis for '{}' ===", topicName);
           logger.info("Total partitions: {}", description.partitions().size());
           
           Map<Integer, Integer> brokerPartitionCount = new HashMap<>();
           
           for (TopicPartitionInfo partition : description.partitions()) {
               int leaderId = partition.leader().id();
               brokerPartitionCount.merge(leaderId, 1, Integer::sum);
               
               logger.info("Partition {}: Leader=Broker-{}, Replicas={}", 
                   partition.partition(), 
                   leaderId,
                   partition.replicas().stream()
                       .map(node -> "Broker-" + node.id())
                       .collect(Collectors.joining(", ")));
           }
           
           logger.info("Partition distribution across brokers:");
           brokerPartitionCount.forEach((brokerId, count) -> 
               logger.info("  Broker-{}: {} partitions", brokerId, count));
               
       } catch (Exception e) {
           logger.error("Error analyzing partition distribution", e);
       }
   }
   ```

### Expected Results
- Understand how partitions are distributed across brokers
- See partition leadership assignment
- Recognize how partition count affects parallelism

### Questions to Answer
1. How are partition leaders distributed across brokers?
2. What would happen if you had more partitions than brokers?
3. How does partition count affect consumer parallelism?

---

## Exercise 3: Topic Configuration Management

### Objective
Learn to manage topic configurations and understand their impact.

### Steps
1. **Create topics with different configurations**
   ```bash
   # High retention topic
   docker-compose exec kafka kafka-topics \
     --create \
     --topic high-retention-topic \
     --bootstrap-server localhost:9092 \
     --partitions 3 \
     --replication-factor 1 \
     --config retention.ms=604800000 \
     --config segment.ms=3600000

   # Compacted topic
   docker-compose exec kafka kafka-topics \
     --create \
     --topic compacted-topic \
     --bootstrap-server localhost:9092 \
     --partitions 3 \
     --replication-factor 1 \
     --config cleanup.policy=compact \
     --config min.cleanable.dirty.ratio=0.1
   ```

2. **View and modify configurations**
   ```bash
   # View configurations
   docker-compose exec kafka kafka-topics \
     --describe \
     --topic high-retention-topic \
     --bootstrap-server localhost:9092

   # Update configuration
   docker-compose exec kafka kafka-configs \
     --alter \
     --entity-type topics \
     --entity-name high-retention-topic \
     --add-config retention.ms=86400000 \
     --bootstrap-server localhost:9092
   ```

3. **Implement configuration management in Java**
   ```java
   public void manageTopicConfigurations() {
       String topicName = "config-demo-topic";
       
       // Create topic with custom config
       Map<String, String> configs = new HashMap<>();
       configs.put("retention.ms", "172800000"); // 2 days
       configs.put("compression.type", "lz4");
       configs.put("cleanup.policy", "delete");
       
       NewTopic newTopic = new NewTopic(topicName, 3, (short) 1);
       newTopic.configs(configs);
       
       try {
           CreateTopicsResult result = adminClient.createTopics(Collections.singletonList(newTopic));
           result.all().get();
           logger.info("Topic '{}' created with custom configurations", topicName);
           
           // Later: Update configuration
           Map<ConfigResource, Config> updateConfigs = new HashMap<>();
           ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, topicName);
           
           Collection<AlterConfigOp> ops = Arrays.asList(
               new AlterConfigOp(new ConfigEntry("retention.ms", "86400000"), AlterConfigOp.OpType.SET),
               new AlterConfigOp(new ConfigEntry("compression.type", "snappy"), AlterConfigOp.OpType.SET)
           );
           
           AlterConfigsResult alterResult = adminClient.incrementalAlterConfigs(
               Collections.singletonMap(resource, ops));
           alterResult.all().get();
           
           logger.info("Topic '{}' configuration updated", topicName);
           
       } catch (Exception e) {
           logger.error("Error managing topic configurations", e);
       }
   }
   ```

### Expected Results
- Topics created with specific configurations
- Understand different cleanup policies
- Successfully update topic configurations
- Java code can manage configurations programmatically

### Questions to Answer
1. What's the difference between `delete` and `compact` cleanup policies?
2. When would you use a high retention period?
3. How do different compression types affect performance?

---

## Solutions

### Exercise 1 Solution
```java
// In BasicTopicOperations.java, add this method:
public void exercise1Solution() {
    logger.info("=== Exercise 1 Solution ===");
    
    // Create topics programmatically
    createTopic("exercise-1-java-topic", 4, (short) 1);
    createTopic("exercise-1-java-compare", 2, (short) 1);
    
    // List and compare
    Set<String> topics = listTopics();
    logger.info("Found {} topics total", topics.size());
    
    // Describe both topics
    describeTopic("exercise-1-java-topic");
    describeTopic("exercise-1-java-compare");
    
    logger.info("Exercise 1 completed successfully!");
}
```

### Exercise 2 Solution
The `analyzePartitionDistribution` method shows:
- Partition count and distribution
- Leader assignment per partition
- How partitions map to brokers
- Understanding of load balancing

### Exercise 3 Solution
```java
// Complete configuration management example
public void exercise3Solution() {
    logger.info("=== Exercise 3 Solution ===");
    
    // Create topics with different retention policies
    createTopicWithConfig("short-retention", 2, (short) 1, 
        Map.of("retention.ms", "3600000")); // 1 hour
    createTopicWithConfig("long-retention", 2, (short) 1, 
        Map.of("retention.ms", "2592000000")); // 30 days
    createTopicWithConfig("compacted-demo", 2, (short) 1, 
        Map.of("cleanup.policy", "compact"));
    
    logger.info("Created topics with different configurations");
    
    // Analyze the differences
    describeTopic("short-retention");
    describeTopic("long-retention");
    describeTopic("compacted-demo");
    
    logger.info("Exercise 3 completed - notice the different configurations!");
}

private void createTopicWithConfig(String name, int partitions, short replication, 
                                  Map<String, String> configs) {
    NewTopic newTopic = new NewTopic(name, partitions, replication);
    newTopic.configs(configs);
    
    try {
        CreateTopicsResult result = adminClient.createTopics(Collections.singletonList(newTopic));
        result.all().get();
        logger.info("Topic '{}' created with configs: {}", name, configs);
    } catch (Exception e) {
        logger.error("Failed to create topic '{}': {}", name, e.getMessage());
    }
}
```

## Common Issues and Troubleshooting

### Issue 1: "Topic already exists"
**Problem**: Trying to create a topic that already exists
**Solution**: 
```java
catch (ExecutionException e) {
    if (e.getCause() instanceof TopicExistsException) {
        logger.warn("Topic '{}' already exists", topicName);
    } else {
        logger.error("Failed to create topic: {}", e.getMessage());
    }
}
```

### Issue 2: AdminClient connection timeout
**Problem**: Cannot connect to Kafka cluster
**Solution**:
```java
// Add timeout configuration
props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "10000");
props.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, "30000");
```

### Issue 3: Insufficient replicas
**Problem**: Replication factor too high for available brokers
**Solution**: Use replication factor = 1 for single broker setup

## Verification Commands

```bash
# Verify all exercises completed
docker-compose exec kafka kafka-topics \
  --list \
  --bootstrap-server localhost:9092 | grep exercise

# Clean up exercise topics
docker-compose exec kafka kafka-topics \
  --delete \
  --topic exercise-1-topic \
  --bootstrap-server localhost:9092

docker-compose exec kafka kafka-topics \
  --delete \
  --topic exercise-1-compare \
  --bootstrap-server localhost:9092

docker-compose exec kafka kafka-topics \
  --delete \
  --topic partition-demo \
  --bootstrap-server localhost:9092

docker-compose exec kafka kafka-topics \
  --delete \
  --topic high-retention-topic \
  --bootstrap-server localhost:9092

docker-compose exec kafka kafka-topics \
  --delete \
  --topic compacted-topic \
  --bootstrap-server localhost:9092
```

## Key Learning Outcomes

After completing these exercises, you should understand:
1. **Topic Creation**: Both CLI and programmatic approaches
2. **Partition Distribution**: How partitions are assigned to brokers
3. **Configuration Management**: Different topic configurations and their purposes
4. **AdminClient Usage**: Programmatic cluster management
5. **Error Handling**: Common issues and their solutions

---

**Next**: [Day 2 Exercises: Data Flow Patterns](./day02-exercises.md)