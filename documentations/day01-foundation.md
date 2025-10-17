# Day 1: Kafka Fundamentals and Foundation

## Learning Objectives
By the end of Day 1, you will:
- Understand Apache Kafka architecture and core concepts
- Set up a local Kafka development environment
- Create and manage topics using Confluent CLI
- Understand the role of producers, consumers, and brokers
- Run basic Java examples with AdminClient

## Morning Session (3 hours): Theory and Concepts

### 1. What is Apache Kafka?

Apache Kafka is a distributed event streaming platform that lets you:
- **Publish and subscribe** to streams of events
- **Store** streams of events durably and reliably
- **Process** streams of events as they occur or retrospectively

### 2. Core Concepts

#### Events
An event records the fact that "something happened". Events contain:
- **Key**: Identifies the event (optional)
- **Value**: The event payload
- **Timestamp**: When the event occurred
- **Headers**: Optional metadata

#### Topics
- Named streams of events
- Like folders in a filesystem
- Events are organized and durably stored in topics
- Topics are partitioned for scalability

#### Partitions
- Topics are split into partitions
- Events with the same key go to the same partition
- Partitions enable parallel processing
- Each partition is an ordered, immutable sequence

#### Producers
- Applications that publish events to topics
- Can specify which partition to write to
- Can use keys for automatic partitioning

#### Consumers
- Applications that subscribe to topics
- Read events from partitions
- Can work individually or in consumer groups

#### Brokers
- Kafka servers that store and serve data
- A cluster consists of multiple brokers
- Handle producer requests and consumer fetches

## Afternoon Session (3 hours): Hands-on Setup

### 1. Environment Setup

#### Install Confluent CLI
```bash
# Download and install Confluent CLI
curl -sL --http1.1 https://cnfl.io/cli | sh -s -- latest

# Add to PATH (add to your ~/.bashrc or ~/.zshrc)
export PATH="$HOME/.confluent/bin:$PATH"

# Verify installation
confluent version
```

#### Install Java (if not already installed)
```bash
# On macOS with Homebrew
brew install openjdk@21

# On Ubuntu/Debian
sudo apt update
sudo apt install openjdk-21-jdk

# Verify Java installation
java --version
```

### 2. Start Local Kafka Cluster

```bash
# Start Kafka using Confluent CLI
confluent local kafka start

# This command starts:
# - Zookeeper (on port 2181)
# - Kafka broker (on port 9092)
# - Schema Registry (on port 8081)
```

### 3. Verify Services

```bash
# Check what services are running
confluent local services list

# Check Kafka broker status
confluent local kafka broker list

# View logs if needed
confluent local services kafka log
```

### 4. Basic Topic Operations

#### Create Topics
```bash
# Create your first topic
confluent local kafka topic create my-first-topic \
  --partitions 3 \
  --replication-factor 1

# Create topics for training exercises
confluent local kafka topic create user-events \
  --partitions 6 \
  --replication-factor 1

confluent local kafka topic create order-events \
  --partitions 3 \
  --replication-factor 1

confluent local kafka topic create session-events \
  --partitions 3 \
  --replication-factor 1
```

#### List Topics
```bash
# List all topics
confluent local kafka topic list

# Describe a specific topic
confluent local kafka topic describe my-first-topic
```

### 5. Java Programming with AdminClient

Now let's explore the programmatic way to manage topics using Java:

#### Run the BasicTopicOperations Example
```bash
# Compile and run the Java example
mvn compile exec:java -Dexec.mainClass="com.training.kafka.Day01Foundation.BasicTopicOperations"
```

This example demonstrates:
- Creating topics with custom configurations
- Listing all topics in the cluster
- Describing topic details (partitions, replicas, leaders)
- Getting cluster information

#### Key Java Concepts Learned

1. **AdminClient Configuration**
```java
Properties props = new Properties();
props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
props.put(AdminClientConfig.CLIENT_ID_CONFIG, "admin-client");
AdminClient adminClient = AdminClient.create(props);
```

2. **Creating Topics**
```java
NewTopic newTopic = new NewTopic(topicName, numPartitions, replicationFactor);
CreateTopicsResult result = adminClient.createTopics(Collections.singletonList(newTopic));
```

3. **Listing Topics**
```java
ListTopicsResult result = adminClient.listTopics();
Set<String> topics = result.names().get();
```

## Exercises

### Exercise 1: Topic Creation and Basic Operations
1. Create a topic called `exercise-topic` with 4 partitions
2. Use both CLI and Java AdminClient to create topics
3. List topics and compare outputs
4. Describe the topic structure

### Exercise 2: Understanding Partitioning
1. Create a topic called `partitioned-topic` with 3 partitions
2. Use the CLI to produce messages with and without keys
3. Observe how messages are distributed across partitions
4. Use different keys and see the partitioning behavior

### Exercise 3: Java AdminClient Practice
1. Modify the `BasicTopicOperations.java` example
2. Add functionality to delete topics
3. Add functionality to update topic configurations
4. Create a method to get cluster metadata

## Command Line Exercises

### Basic Producer/Consumer Operations
```bash
# Start a console producer
confluent local kafka topic produce my-first-topic

# Type messages (each line is a separate message):
# Hello Kafka
# This is my first message
# Learning event streaming
# (Press Ctrl+C to exit)

# Start a console consumer from beginning
confluent local kafka topic consume my-first-topic --from-beginning

# Produce with keys
echo "user1:login" | confluent local kafka topic produce user-events \
  --property "parse.key=true" \
  --property "key.separator=:"
```

## Key Takeaways

1. **Kafka is a distributed streaming platform** designed for high-throughput, fault-tolerant event processing
2. **Topics are partitioned** for scalability and parallel processing
3. **Keys determine partition assignment** - same key always goes to same partition
4. **AdminClient provides programmatic access** to cluster management operations
5. **Brokers store and serve data** in a distributed, replicated manner

## Troubleshooting

### Common Issues

1. **Confluent CLI not found**
   ```bash
   # Ensure CLI is in PATH
   export PATH="$HOME/.confluent/bin:$PATH"
   ```

2. **Services not starting**
   ```bash
   # Check if ports are available
   lsof -i :9092
   lsof -i :2181
   
   # Stop any conflicting services
   confluent local kafka stop
   ```

3. **Java compilation issues**
   ```bash
   # Ensure Java 21 is installed
   java --version
   
   # Clean and compile
   mvn clean compile
   ```

## Next Steps

Tomorrow we'll dive into:
- Data flow patterns
- Producer and consumer semantics
- Offset management
- Message ordering and delivery guarantees

---

**🚀 Ready for Day 2?** Continue with [Day 2: Data Flow Basics](./day02-dataflow.md)