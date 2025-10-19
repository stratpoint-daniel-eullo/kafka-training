# Apache Kafka Training for Data Engineers

![Kafka Training](https://img.shields.io/badge/Apache%20Kafka-3.8.0-orange)
![Java](https://img.shields.io/badge/Java-21-blue)
![Container First](https://img.shields.io/badge/Container-First-green)
![Platform Agnostic](https://img.shields.io/badge/Platform-Agnostic-purple)

## 🎯 **Designed for Data Engineers**

This is the **pure Kafka** learning track - focused on core Kafka concepts without framework abstractions. Perfect for data engineers who need to understand Kafka fundamentals for use with **any** platform: Spark, Flink, Airflow, Python, Scala, or Java.

### 🚀 **What You'll Learn**

- ✅ **Pure Kafka APIs** - Raw KafkaProducer, KafkaConsumer, no abstractions
- ✅ **Language-Agnostic Concepts** - Transferable to Python, Scala, Go, etc.
- ✅ **Container-First** - Docker and Kubernetes from day one
- ✅ **Production Patterns** - Real-world data pipeline patterns
- ✅ **Platform Integration** - Connect Kafka to Spark, Flink, databases

### ❌ **What This is NOT**

- ❌ Spring Boot web application development
- ❌ Java-specific microservices patterns
- ❌ REST API or web UI development

> **Note**: If you want Spring Boot integration, see [README.md](./README.md) for the Java Developer track.

---

## 📚 **Learning Path for Data Engineers**

### **Phase 1: Pure Kafka Fundamentals (Days 1-4)**

Learn Kafka using **only** the raw Kafka APIs - no Spring Boot, no frameworks.

```
Day 1: Kafka Architecture & CLI Tools
Day 2: Data Flow, Partitions, Offsets
Day 3: Pure Java KafkaProducer API
Day 4: Pure Java KafkaConsumer API
```

### **Phase 2: Stream Processing & Schemas (Days 5-6)**

```
Day 5: Kafka Streams API (pure Java)
Day 6: Avro Schemas & Schema Registry
```

### **Phase 3: Integration & Production (Days 7-8)**

```
Day 7: Kafka Connect, Database CDC
Day 8: Security, Monitoring, K8s Deployment
```

---

## 🚀 **Quick Start (5 Minutes)**

### **Option 1: CLI-Only (Recommended for Data Engineers)**

```bash
# 1. Start Kafka with Docker
docker-compose up -d

# 2. Run pure Kafka examples (no Spring Boot)
./bin/kafka-training-cli.sh --day 1 --demo foundation
./bin/kafka-training-cli.sh --day 3 --demo producer
./bin/kafka-training-cli.sh --day 4 --demo consumer

# 3. Or run Java directly
java -cp target/kafka-training-java-1.0.0.jar \
  com.training.kafka.cli.Day03ProducerCLI
```

### **Option 2: Docker Container (Fully Isolated)**

```bash
# Run everything in containers
docker-compose up -d

# Execute training inside container
docker exec -it kafka-training-app \
  java -cp /app/kafka-training.jar com.training.kafka.cli.Day03ProducerCLI
```

### **Option 3: Build and Run Maven**

```bash
# Build the project
mvn clean package

# Run CLI examples (no web UI, no Spring Boot)
mvn exec:java -Dexec.mainClass="com.training.kafka.cli.Day03ProducerCLI"
```

---

## 📖 **Day-by-Day Guide**

### **Day 1: Kafka Fundamentals**

**What You'll Learn**: Topics, partitions, brokers, replication

**CLI Examples**:
```bash
# Create topic with CLI
./bin/kafka-training-cli.sh create-topic \
  --name user-events \
  --partitions 3 \
  --replication-factor 1

# Or use Java AdminClient directly
java -cp target/kafka-training-java-1.0.0.jar \
  com.training.kafka.cli.Day01FoundationCLI
```

**Java Code** (Pure Kafka - No Spring):
```java
// src/main/java/com/training/kafka/cli/Day01FoundationCLI.java
Properties props = new Properties();
props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

try (AdminClient admin = AdminClient.create(props)) {
    NewTopic topic = new NewTopic("user-events", 3, (short) 1);
    admin.createTopics(Collections.singleton(topic)).all().get();
}
```

**Python Equivalent**:
```python
# examples/python/day01_foundation.py
from kafka.admin import KafkaAdminClient, NewTopic

admin = KafkaAdminClient(bootstrap_servers='localhost:9092')
topic = NewTopic(name='user-events', num_partitions=3, replication_factor=1)
admin.create_topics([topic])
```

---

### **Day 3: Pure Kafka Producer**

**What You'll Learn**: Producer configs, batching, error handling, idempotence

**CLI Example**:
```bash
# Run pure Kafka producer
./bin/kafka-training-cli.sh produce \
  --topic user-events \
  --key user-123 \
  --value '{"action":"login","timestamp":"2025-01-19T10:00:00Z"}'
```

**Java Code** (Pure Kafka):
```bash
# Run the standalone Java example (no Spring Boot)
java -cp target/kafka-training-java-1.0.0.jar \
  com.training.kafka.Day03Producers.AdvancedProducer
```

**Code Location**: `src/main/java/com/training/kafka/Day03Producers/AdvancedProducer.java`

This is a **pure Kafka** example:
- Direct `KafkaProducer` usage
- Raw `Properties` configuration
- No Spring dependencies
- Production-ready patterns (idempotence, error handling, metrics)

**Python Equivalent**:
```python
# examples/python/day03_producer.py
from kafka import KafkaProducer
import json

producer = KafkaProducer(
    bootstrap_servers='localhost:9092',
    value_serializer=lambda v: json.dumps(v).encode('utf-8'),
    acks='all',
    enable_idempotence=True
)

event = {'user': 'user-123', 'action': 'login'}
producer.send('user-events', key=b'user-123', value=event)
producer.flush()
```

---

### **Day 4: Pure Kafka Consumer**

**What You'll Learn**: Consumer groups, offset management, rebalancing

**CLI Example**:
```bash
# Run pure Kafka consumer
./bin/kafka-training-cli.sh consume \
  --topic user-events \
  --group data-pipeline-consumers
```

**Java Code** (Pure Kafka):
```bash
java -cp target/kafka-training-java-1.0.0.jar \
  com.training.kafka.Day04Consumers.AdvancedConsumer
```

**Python Equivalent**:
```python
# examples/python/day04_consumer.py
from kafka import KafkaConsumer
import json

consumer = KafkaConsumer(
    'user-events',
    bootstrap_servers='localhost:9092',
    group_id='data-pipeline-consumers',
    value_deserializer=lambda v: json.loads(v.decode('utf-8')),
    enable_auto_commit=False
)

for message in consumer:
    print(f"Partition: {message.partition}, Offset: {message.offset}")
    print(f"Data: {message.value}")
    consumer.commit()  # Manual offset commit
```

---

## 🏗️ **Project Structure (CLI Track)**

```
kafka-training-java/
├── src/main/java/com/training/kafka/
│   ├── cli/                              # ← NEW: Pure Kafka CLI examples
│   │   ├── Day01FoundationCLI.java       # AdminClient, topic management
│   │   ├── Day03ProducerCLI.java         # Raw KafkaProducer
│   │   └── Day04ConsumerCLI.java         # Raw KafkaConsumer
│   │
│   ├── Day01Foundation/                  # Original Kafka examples (no Spring)
│   ├── Day03Producers/
│   │   ├── AdvancedProducer.java         # ✅ Pure Kafka (USE THIS)
│   │   └── SimpleProducer.java           # ✅ Pure Kafka (USE THIS)
│   ├── Day04Consumers/
│   ├── Day05Streams/
│   ├── Day06Schemas/
│   ├── Day07Connect/
│   └── Day08Advanced/
│
├── examples/
│   ├── python/                           # ← NEW: Python Kafka examples
│   │   ├── day03_producer.py
│   │   └── day04_consumer.py
│   └── scala/                            # ← FUTURE: Scala examples
│
├── bin/
│   └── kafka-training-cli.sh             # ← NEW: CLI wrapper script
│
├── documentations/                       # Day-by-day documentation
│   ├── day01-foundation.md
│   ├── day03-producers.md
│   └── day04-consumers.md
│
└── docker-compose.yml                    # Kafka cluster setup
```

---

## 🔧 **Environment Setup**

### **Prerequisites**

- **Java 11+** (Java 21 recommended)
- **Docker** and Docker Compose
- **Maven 3.8+**
- **Python 3.8+** (optional, for Python examples)

### **Start Kafka Cluster**

```bash
# Start Kafka, Zookeeper, Schema Registry, Kafka Connect
docker-compose up -d

# Verify all services are running
docker-compose ps

# Check Kafka is ready
docker exec -it kafka kafka-topics --bootstrap-server localhost:9092 --list
```

### **Build the Project**

```bash
# Build Java examples
mvn clean package

# This creates: target/kafka-training-java-1.0.0.jar
```

### **Install Python Dependencies (Optional)**

```bash
cd examples/python
pip install -r requirements.txt
```

---

## 🎓 **Learning Approach**

### **1. Understand Kafka Fundamentals First**

Focus on **core Kafka concepts** that apply to ANY language/platform:
- Topics, partitions, offsets
- Producer/consumer patterns
- Consumer groups and rebalancing
- Delivery guarantees (at-least-once, exactly-once)

### **2. Use Raw Kafka APIs**

Learn with **direct Kafka client APIs**:
- Java: `KafkaProducer`, `KafkaConsumer`, `AdminClient`
- Python: `kafka-python` library
- Configuration via `Properties` (Java) or dictionaries (Python)

### **3. Container-First Development**

All examples run in **Docker containers**:
- Matches production environments
- Easy to scale and test
- Platform-independent

### **4. Language-Agnostic Mindset**

Every Java example has a **Python equivalent**:
- Same Kafka concepts
- Different language syntax
- Transferable knowledge

---

## 📊 **Key Differences: Data Engineer vs Java Developer Track**

| Aspect | Data Engineer Track (THIS) | Java Developer Track |
|--------|----------------------------|----------------------|
| **Focus** | Pure Kafka fundamentals | Spring Boot integration |
| **Primary API** | Raw `KafkaProducer/Consumer` | `KafkaTemplate` abstraction |
| **Run Method** | CLI / Java main() | `mvn spring-boot:run` + Web UI |
| **Code Examples** | `Day03Producers/AdvancedProducer.java` | `services/Day03ProducerService.java` |
| **Dependencies** | Kafka clients only | Spring Boot + Spring Kafka |
| **Platform** | Language-agnostic concepts | Java/Spring-specific |
| **Use Cases** | Data pipelines, ETL, streaming | Microservices, web apps |
| **Python Examples** | ✅ Included | ❌ Not included |
| **Entry Point** | `main()` method or CLI | Web browser `http://localhost:8080` |

---

## 🐍 **Python Examples**

All core concepts have **Python equivalents** for platform-agnostic learning.

### **Day 3: Producer**
```bash
# Java version
java -cp target/kafka-training-java-1.0.0.jar \
  com.training.kafka.Day03Producers.AdvancedProducer

# Python version (same concepts!)
python examples/python/day03_producer.py
```

### **Day 4: Consumer**
```bash
# Java version
java -cp target/kafka-training-java-1.0.0.jar \
  com.training.kafka.Day04Consumers.AdvancedConsumer

# Python version (same concepts!)
python examples/python/day04_consumer.py
```

---

## 🔍 **Production Patterns for Data Engineers**

### **Pattern 1: Exactly-Once Semantics**

**Java**:
```java
props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
props.put(ProducerConfig.ACKS_CONFIG, "all");
props.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
```

**Python**:
```python
producer = KafkaProducer(
    enable_idempotence=True,
    acks='all',
    retries=9999
)
```

### **Pattern 2: Manual Offset Management**

**Java**:
```java
ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
for (ConsumerRecord<String, String> record : records) {
    processRecord(record);
}
consumer.commitSync(); // Commit after processing
```

**Python**:
```python
for message in consumer:
    process_record(message)
    consumer.commit()  # Manual commit
```

---

## 🚀 **Integration with Data Platforms**

### **Apache Spark Integration**

```scala
// Spark Structured Streaming with Kafka
val df = spark.readStream
  .format("kafka")
  .option("kafka.bootstrap.servers", "localhost:9092")
  .option("subscribe", "user-events")
  .load()

df.selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
  .writeStream
  .format("console")
  .start()
```

### **Apache Flink Integration**

```java
// Flink DataStream API with Kafka
KafkaSource<String> source = KafkaSource.<String>builder()
    .setBootstrapServers("localhost:9092")
    .setTopics("user-events")
    .setValueOnlyDeserializer(new SimpleStringSchema())
    .build();

DataStream<String> stream = env.fromSource(source, ...);
```

---

## 📚 **Documentation**

- **[START-HERE.md](./START-HERE.md)** - Quick start guide
- **[documentations/](./documentations/)** - Day-by-day learning guides
- **[exercises/](./exercises/)** - Hands-on practice exercises
- **[docs/](./docs/)** - Complete MkDocs site

---

## 🎯 **Next Steps**

1. **Start Kafka**: `docker-compose up -d`
2. **Day 1**: Learn fundamentals - `./bin/kafka-training-cli.sh --day 1 --demo foundation`
3. **Day 3**: Build producer - `java -cp target/kafka-training-java-1.0.0.jar com.training.kafka.Day03Producers.AdvancedProducer`
4. **Day 4**: Build consumer - `java -cp target/kafka-training-java-1.0.0.jar com.training.kafka.Day04Consumers.AdvancedConsumer`

---

## 🤝 **Want Spring Boot Instead?**

If you're a **Java developer** building Spring Boot microservices, see:
- **[README.md](./README.md)** - Spring Boot integration track
- **[WEB-UI-GETTING-STARTED.md](./WEB-UI-GETTING-STARTED.md)** - Web interface guide

---

**Built for data engineers. Pure Kafka. Platform-agnostic. Production-ready.**
