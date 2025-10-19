# START HERE - Kafka Training Quick Start

## Choose Your Track

This training serves **two distinct audiences**:

### Data Engineer Track (RECOMMENDED)

**For**: Data engineers, platform engineers, data pipeline developers

**Focus**: Pure Kafka fundamentals, CLI-first, platform-agnostic

**Quick Start**:
```bash
# 1. Start Kafka
docker-compose up -d

# 2. Run CLI examples
./bin/kafka-training-cli.sh --day 1 --demo foundation
./bin/kafka-training-cli.sh --day 3 --demo producer
./bin/kafka-training-cli.sh --day 4 --demo consumer

# 3. Or run Java directly
java -cp target/kafka-training-java-1.0.0.jar \
  com.training.kafka.Day03Producers.AdvancedProducer
```

**Documentation**: See [README-DATA-ENGINEERS.md](./README-DATA-ENGINEERS.md)

---

### Java Developer Track (Alternative)

**For**: Java/Spring Boot developers building microservices

**Focus**: Spring Boot integration, web UI, REST APIs

**Quick Start**:
```bash
# 1. Start Kafka
docker-compose up -d

# 2. Start Spring Boot Application
mvn spring-boot:run

# 3. Open Browser
http://localhost:8080

# 4. Click "Run Demo" buttons
```

**Documentation**: See [WEB-UI-GETTING-STARTED.md](./WEB-UI-GETTING-STARTED.md)

---

## The 3 Most Important Things to Know

### 1. Topics = Mailboxes
   - Day 1 creates mailboxes
   - Day 3 puts letters IN mailboxes
   - Day 4 takes letters OUT of mailboxes

### 2. Everything connects through Topics
   ```
   Producer sends → Topic "user-events" → Consumer receives
   ```

### 3. Two Ways to Learn
   - **CLI-First (Data Engineer Track)**: Direct Kafka APIs, language-agnostic
   - **Spring Boot (Java Track)**: Framework integration, web interface

## Quick Comparison

| Aspect | Data Engineer Track | Java Developer Track |
|--------|---------------------|----------------------|
| **Entry Point** | CLI commands | Web browser |
| **Code Examples** | Pure Kafka APIs | Spring Boot services |
| **Run Command** | `./bin/kafka-training-cli.sh` | `mvn spring-boot:run` |
| **Language Focus** | Platform-agnostic | Java/Spring-specific |
| **Use Cases** | Data pipelines, ETL | Microservices, web apps |

## 5-Minute Getting Started (Data Engineer Track)

### Step 1: Create a Topic (2 minutes)
```bash
# Using CLI
./bin/kafka-training-cli.sh create-topic \
  --name my-first-topic \
  --partitions 3

# Or using native Kafka tools
docker exec kafka-training-kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --create --topic my-first-topic \
  --partitions 3 --replication-factor 1
```

### Step 2: Send Messages (2 minutes)
```bash
# Using CLI
./bin/kafka-training-cli.sh produce \
  --topic my-first-topic \
  --key user-1 \
  --value "Hello Kafka!"

# Or run pure Java producer
java -cp target/kafka-training-java-1.0.0.jar \
  com.training.kafka.Day03Producers.SimpleProducer
```

### Step 3: Consume Messages (1 minute)
```bash
# Using CLI
./bin/kafka-training-cli.sh consume \
  --topic my-first-topic \
  --group my-consumer-group

# Or run pure Java consumer
java -cp target/kafka-training-java-1.0.0.jar \
  com.training.kafka.Day04Consumers.SimpleConsumer
```

## Alternative: 5-Minute Getting Started (Java Developer Track)

### Step 1: Create a Topic (2 minutes)
```
1. Go to http://localhost:8080
2. Find "Day 1: Kafka Fundamentals"
3. Click "Create Topic" (use default values)
4. See success message ✓
```

### Step 2: Send Messages (2 minutes)
```
1. Find "Day 3: Message Producers"
2. Click "Send" (use default values)
3. See success message
```

### Step 3: Consume Messages (1 minute)
```
1. Find "Day 4: Message Consumers"
2. Click "Consume Messages"
3. Check terminal logs to see messages
```

## The Sections Explained (One Sentence Each)

- **Day 1**: Creates topics (containers for messages)
- **Day 2**: Explains how messages flow and are organized
- **Day 3**: Sends messages TO topics
- **Day 4**: Reads messages FROM topics
- **Day 5**: Processes messages in real-time (reads, transforms, writes)
- **Day 6**: Adds data quality checks (schemas)
- **Day 7**: Connects Kafka to databases
- **Day 8**: Makes everything secure and monitored

## How They Connect

```
Day 1: Creates topic "user-events"
          ↓
Day 3: Sends "Hello" to "user-events"
          ↓
Day 4: Reads "Hello" from "user-events"
          ↓
Day 5: Processes "Hello" → outputs to "processed-events"
```

**That's it!** Topics connect everything.

## Most Common Questions

### Q: Which track should I choose?
A:
- **Data Engineer?** Choose CLI-first track (README-DATA-ENGINEERS.md)
- **Java Developer?** Choose Spring Boot track (WEB-UI-GETTING-STARTED.md)
- **Not sure?** Start with Data Engineer track - it's more fundamental

### Q: Where do I see results?
A:
- **CLI Track**: Terminal output where you ran commands
- **Spring Boot Track**: Green/red boxes in web UI + terminal logs

### Q: What should I do first?
A:
- **CLI Track**: Run CLI demos in order: Day 1 → Day 3 → Day 4
- **Spring Boot Track**: Click "Run Demo" buttons in order

### Q: What's a topic again?
A: A topic is like a folder that holds messages. Think of it as:
- A mailbox
- A chat channel
- A message queue

## Learning Path

### Data Engineer Track (Recommended for Production Skills)
```
1. Read README-DATA-ENGINEERS.md
2. Run CLI demos (Day 1 → Day 3 → Day 4)
3. Study pure Kafka code examples
4. Build data pipelines with raw Kafka APIs
5. Integrate with Spark/Flink/Airflow
```

### Java Developer Track (Recommended for Microservices)
```
1. Read WEB-UI-GETTING-STARTED.md
2. Click "Run Demo" on Day 1 through Day 8
3. Study Spring Boot service code
4. Build EventMart project
5. Create REST API services
```

### Hybrid Approach
```
1. Start with CLI Track to learn fundamentals
2. Then explore Spring Boot Track for framework integration
3. Understand both pure Kafka AND Spring abstractions
```

## The Only Commands You Need

### Data Engineer Track
```bash
# Start everything
docker-compose up -d

# Run examples
./bin/kafka-training-cli.sh --day <N> --demo <name>

# Or run Java directly
java -cp target/kafka-training-java-1.0.0.jar <MainClass>

# Stop everything
docker-compose down
```

### Java Developer Track
```bash
# Start everything
docker-compose up -d && mvn spring-boot:run

# Stop everything
Ctrl+C (in terminal)
docker-compose down
```

## Quick Reference Card

| I want to... | Data Engineer Track | Java Developer Track |
|--------------|---------------------|----------------------|
| Create topic | `./bin/kafka-training-cli.sh create-topic` | Click "Create Topic" in Day 1 |
| Send message | `./bin/kafka-training-cli.sh produce` | Click "Send" in Day 3 |
| Read messages | `./bin/kafka-training-cli.sh consume` | Click "Consume Messages" in Day 4 |
| See examples | `java -cp target/*.jar <MainClass>` | Click "Run Demo" buttons |
| Learn concepts | Read `docs/training/dayXX-*.md` | Read `docs/training/dayXX-*.md` |

## That's It!

Really, that's all you need to know to get started. Choose your track and dive in:

**Data Engineers**: Start with [README-DATA-ENGINEERS.md](./README-DATA-ENGINEERS.md)

**Java Developers**: Start with [WEB-UI-GETTING-STARTED.md](./WEB-UI-GETTING-STARTED.md)

---

## Additional Documentation

### Core Documentation
- **[README-DATA-ENGINEERS.md](./README-DATA-ENGINEERS.md)** - CLI-first, pure Kafka track
- **[README.md](./README.md)** - Spring Boot integration track
- **[GETTING-STARTED.md](./GETTING-STARTED.md)** - Detailed setup guide
- **[LEARNING-PATHS.md](./LEARNING-PATHS.md)** - Track comparison

### Specialized Guides
- **[WEB-UI-GETTING-STARTED.md](./WEB-UI-GETTING-STARTED.md)** - Web interface (Java track)
- **[EVENTMART-PROJECT-GUIDE.md](./EVENTMART-PROJECT-GUIDE.md)** - Progressive project (Java track)
- **[HOW-SECTIONS-CONNECT.md](./HOW-SECTIONS-CONNECT.md)** - Architecture overview
- **[CONTAINER-FIRST-QUICKSTART.md](./CONTAINER-FIRST-QUICKSTART.md)** - Docker setup

**Pro Tip**: The CLI-first track teaches you transferable Kafka skills that work with ANY language or platform!
