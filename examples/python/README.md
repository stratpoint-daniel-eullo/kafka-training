# Kafka Training - Python Examples

> **Platform-Agnostic Kafka Training for Data Engineers**

This directory contains Python implementations of all Kafka training concepts. These examples demonstrate that Kafka knowledge is transferable across languages - the same concepts work whether you use Python, Java, Scala, Go, or any other language.

## 📋 Overview

| Day | Topic | Python Files | Key Libraries |
|-----|-------|-------------|---------------|
| Day 1 | Kafka Foundation | `day01_admin.py` | kafka-python |
| Day 2 | Data Flow | `day02_dataflow.py` | kafka-python |
| Day 3 | Producers | `day03_producer.py` | kafka-python |
| Day 4 | Consumers | `day04_consumer.py` | kafka-python |
| Day 5 | Schema Registry & Avro | `day05_avro_producer.py`<br>`day05_avro_consumer.py` | confluent-kafka[avro] |
| Day 6 | Stream Processing | `day06_streams_faust.py` | faust-streaming |
| Day 7 | Kafka Connect | `day07_connect_client.py` | requests |
| Day 8 | Security | `day08_security.py` | kafka-python, confluent-kafka |

---

## 🚀 Quick Start

### Prerequisites

- Python 3.7+ installed
- Kafka running on `localhost:9092`
- For Day 5: Schema Registry on `localhost:8081`
- For Day 7: Kafka Connect on `localhost:8083`

### Installation

```bash
# Navigate to examples/python directory
cd examples/python

# Install all dependencies
pip install -r requirements.txt

# Or install minimal dependencies for Days 1-4 only
pip install kafka-python
```

### Run Your First Example

```bash
# Day 1: Kafka AdminClient (topic management)
python day01_admin.py

# Day 2: Complete data flow (end-to-end)
python day02_dataflow.py

# Day 3: Producer patterns
python day03_producer.py

# Day 4: Consumer patterns
python day04_consumer.py
```

---

## 📚 Detailed Examples

### Day 1: Kafka Foundation (`day01_admin.py`)

**What you'll learn:**
- Create, list, and describe topics
- Get cluster metadata
- Manage topic configurations
- Delete topics

**Dependencies:** `kafka-python`

**Run:**
```bash
python examples/python/day01_admin.py
```

**Compare to Java:** `AdminClient.create()`

---

### Day 2: Kafka Data Flow (`day02_dataflow.py`)

**What you'll learn:**
- Complete Kafka data pipeline
- AdminClient → Producer → Consumer flow
- Message keys and partitioning
- Platform-agnostic patterns

**Dependencies:** `kafka-python`

**Run:**
```bash
python examples/python/day02_dataflow.py
```

**Interactive demo** shows complete flow from topic creation to consumption.

---

### Day 3: Kafka Producers (`day03_producer.py`)

**What you'll learn:**
- Synchronous vs asynchronous sending
- Batch message production
- Producer callbacks
- Error handling

**Dependencies:** `kafka-python`

**Run:**
```bash
python examples/python/day03_producer.py
```

**Compare to Java:** `KafkaProducer<String, String>`

---

### Day 4: Kafka Consumers (`day04_consumer.py`)

**What you'll learn:**
- Consumer groups and offset management
- Manual vs auto commit
- Polling and message processing
- At-least-once semantics

**Dependencies:** `kafka-python`

**Run:**
```bash
python examples/python/day04_consumer.py
```

**Compare to Java:** `KafkaConsumer<String, String>`

---

### Day 5: Schema Registry & Avro

**Two files:**
- `day05_avro_producer.py` - Produce Avro-serialized messages
- `day05_avro_consumer.py` - Consume Avro-serialized messages

**What you'll learn:**
- Avro serialization/deserialization
- Schema Registry integration
- Schema evolution
- Type-safe data

**Dependencies:** `confluent-kafka[avro]`

**Prerequisites:** Schema Registry must be running on `localhost:8081`

**Run:**
```bash
# Terminal 1: Producer
python examples/python/day05_avro_producer.py

# Terminal 2: Consumer
python examples/python/day05_avro_consumer.py
```

**Compare to Java:** `KafkaAvroSerializer` / `KafkaAvroDeserializer`

---

### Day 6: Stream Processing (`day06_streams_faust.py`)

**What you'll learn:**
- Stream processing in Python (Faust)
- Filtering, mapping, aggregating streams
- Windowed aggregations
- Stateful operations

**Dependencies:** `faust-streaming`

**Run:**
```bash
# Start the Faust worker (stream processor)
faust -A day06_streams_faust worker -l info

# In another terminal, send test data
faust -A day06_streams_faust produce-test-data

# View stats in browser
open http://localhost:6066/stats/
```

**Compare to Java:** Kafka Streams `StreamsBuilder`

**Faust vs Kafka Streams:**
- No JVM required (lower memory footprint)
- Python ecosystem integration
- Async/await for concurrency
- Built-in web server for monitoring

---

### Day 7: Kafka Connect (`day07_connect_client.py`)

**What you'll learn:**
- Manage connectors via REST API
- Create, monitor, control connectors
- Connector lifecycle operations
- Platform-agnostic integration

**Dependencies:** `requests`

**Prerequisites:** Kafka Connect running on `localhost:8083`

**Run:**
```bash
python examples/python/day07_connect_client.py
```

**Key Point:** Kafka Connect uses same REST API regardless of language!

**Compare to Java:** Same REST API calls

---

### Day 8: Security (`day08_security.py`)

**What you'll learn:**
- SSL/TLS configuration
- SASL authentication (PLAIN, SCRAM-SHA-512)
- Production security patterns
- Environment variable best practices

**Dependencies:** `kafka-python`, `confluent-kafka`

**Run:**
```bash
python examples/python/day08_security.py
```

**Note:** This is a configuration reference. To actually run with security, you need a Kafka cluster with security enabled.

**Compare to Java:** Same concepts, different syntax

---

## 🔧 Installation Details

### Option 1: Install Everything

```bash
pip install -r requirements.txt
```

Installs:
- `kafka-python` - Core Kafka client
- `confluent-kafka[avro]` - High-performance client with Avro
- `faust-streaming` - Stream processing
- `requests` - REST API client

### Option 2: Install By Day

```bash
# Days 1-4: Basic Kafka
pip install kafka-python

# Day 5: Avro support
pip install confluent-kafka[avro]

# Day 6: Stream processing
pip install faust-streaming

# Day 7: Kafka Connect
pip install requests

# Day 8: Security
# (uses kafka-python and confluent-kafka already installed)
```

### Troubleshooting

**confluent-kafka installation fails:**
```bash
# Ubuntu/Debian
sudo apt-get install build-essential python3-dev

# macOS
xcode-select --install

# Then retry
pip install confluent-kafka[avro]
```

**faust-streaming installation fails:**
```bash
# Ensure Python 3.7+
python --version

# Upgrade pip and setuptools
pip install --upgrade pip setuptools wheel

# Retry
pip install faust-streaming
```

---

## 🎯 Learning Path

### For Data Engineers (Recommended)

1. **Start here:** `day01_admin.py` - Learn topic management
2. **End-to-end:** `day02_dataflow.py` - Understand complete flow
3. **Producers:** `day03_producer.py` - Learn to send data
4. **Consumers:** `day04_consumer.py` - Learn to receive data
5. **Schema:** `day05_avro_producer.py` + `day05_avro_consumer.py` - Type-safe data
6. **Streaming:** `day06_streams_faust.py` - Real-time processing
7. **Integration:** `day07_connect_client.py` - Connect to other systems
8. **Production:** `day08_security.py` - Secure your data

### For Java Developers (Optional Comparison)

If you know Java Kafka, these Python examples show how the **same concepts** work in Python:

| Java Kafka | Python kafka-python | Python confluent-kafka |
|------------|---------------------|------------------------|
| `AdminClient.create()` | `KafkaAdminClient()` | Same |
| `KafkaProducer<K,V>` | `KafkaProducer()` | `Producer()` |
| `KafkaConsumer<K,V>` | `KafkaConsumer()` | `Consumer()` |
| `KafkaAvroSerializer` | N/A | `AvroSerializer()` |
| Kafka Streams | N/A | Faust |
| Connect REST API | Same | Same |

**Key Insight:** Kafka is platform-agnostic! Learn once, use everywhere.

---

## 📖 Python Libraries Compared

### kafka-python vs confluent-kafka

| Feature | kafka-python | confluent-kafka |
|---------|-------------|-----------------|
| **Implementation** | Pure Python | C-based (librdkafka) |
| **Installation** | Easy (no dependencies) | Requires C compiler |
| **Performance** | Good (moderate loads) | Excellent (high throughput) |
| **Avro Support** | No | Yes (`confluent-kafka[avro]`) |
| **Schema Registry** | No | Yes |
| **API Style** | Pythonic | Java-like |
| **Use Case** | Development, learning | Production, performance-critical |

**Recommendation:**
- Learning? Start with `kafka-python`
- Production? Use `confluent-kafka`
- Need Avro? Must use `confluent-kafka[avro]`

---

## 🌟 Key Concepts (Platform-Agnostic)

These concepts apply regardless of language:

1. **Producers** send messages to topics
2. **Consumers** read messages from topics in consumer groups
3. **Topics** are divided into **partitions** for parallelism
4. **Messages** with the same **key** go to the same partition (ordering)
5. **Offsets** track which messages have been consumed
6. **Schema Registry** manages Avro schemas
7. **Kafka Connect** integrates external systems via REST API
8. **Security** uses SSL/TLS + SASL (same across languages)

**These concepts work in:**
- Python (these examples)
- Java (see `src/main/java/...`)
- Scala
- Go
- Node.js
- Rust
- ANY language with a Kafka client!

---

## 🔗 Related Documentation

- [Training Docs](../../docs/training/) - Detailed explanations
- [Java Examples](../../src/main/java/com/training/kafka/) - Java implementations
- [Exercises](../../docs/exercises/) - Hands-on exercises

---

## 💡 Next Steps

After completing these examples, you can:

1. **Apply to your data engineering work:**
   - Build data pipelines with Airflow + Kafka
   - Stream data to data lakes (S3, HDFS)
   - Real-time analytics with Spark + Kafka
   - Change data capture (CDC) with Debezium

2. **Expand your knowledge:**
   - Learn Kafka Streams in depth (or use Faust)
   - Explore Kafka Connect connectors
   - Set up production Kafka clusters
   - Implement security and monitoring

3. **Compare with Java:**
   - See how same concepts work in `src/main/java/...`
   - Understand when to use Java vs Python
   - Learn Spring Kafka (if needed)

---

## 🤝 Contributing

Found a bug or have a suggestion? Open an issue!

Want to add more examples? Pull requests welcome!

---

## 📜 License

These examples are part of the Kafka Training repository. See root LICENSE file.

---

## 🎓 About This Training

These Python examples are part of a comprehensive Kafka training program designed for:

**Primary Audience:** Data Engineers who need platform-agnostic Kafka knowledge

**Secondary Audience:** Java developers who want to see Spring Boot integration (optional)

**Goal:** Teach Kafka concepts that transfer across languages and frameworks

**Philosophy:** Learn core Kafka, not just one framework

---

**Happy Learning! 🚀**

For questions or help, see the main [README](../../README.md) or [documentation](../../docs/).
