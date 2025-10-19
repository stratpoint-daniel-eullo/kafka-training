# Kafka Training Learning Tracks

## Choose Your Learning Track

This training offers **two distinct learning tracks** based on your role and goals:

---

## Track 1: Data Engineer Track (RECOMMENDED)

### Who is this for?

- **Data Engineers** building data pipelines and streaming platforms
- **Platform Engineers** managing Kafka infrastructure
- **Data Scientists** integrating Kafka with ML pipelines
- **Developers** using Kafka with Spark, Flink, Airflow, or Python
- **Anyone** wanting pure, transferable Kafka knowledge

### What You'll Learn

- **Pure Kafka APIs**: Raw KafkaProducer, KafkaConsumer, AdminClient
- **Platform-Agnostic**: Concepts that work with ANY language (Java, Python, Scala, Go)
- **CLI-First**: Command-line tools and direct API usage
- **Production Patterns**: Real-world data pipeline patterns
- **Container-Based**: Docker and Kubernetes from day one

### Learning Approach

**CLI and Pure Java Examples**:
```bash
# Day 1: Topic management
./bin/kafka-training-cli.sh --day 1 --demo foundation

# Day 3: Pure Kafka producer
java -cp target/kafka-training-java-1.0.0.jar \
  com.training.kafka.Day03Producers.AdvancedProducer

# Day 4: Pure Kafka consumer
java -cp target/kafka-training-java-1.0.0.jar \
  com.training.kafka.Day04Consumers.AdvancedConsumer

# Day 5: Kafka Streams
./bin/kafka-training-cli.sh --day 5 --demo streams
```

### Daily Structure

```
Day 1: Kafka Architecture & CLI Tools
Day 2: Data Flow, Partitions, Offsets
Day 3: Pure Java KafkaProducer API
Day 4: Pure Java KafkaConsumer API
Day 5: Kafka Streams API (pure Java)
Day 6: Avro Schemas & Schema Registry
Day 7: Kafka Connect, Database CDC
Day 8: Security, Monitoring, K8s Deployment
```

### Code Examples Location

- **Pure Java**: `src/main/java/com/training/kafka/Day03Producers/AdvancedProducer.java`
- **CLI Tools**: `./bin/kafka-training-cli.sh`
- **Python Examples**: `examples/python/day03_producer.py`
- **Documentation**: `docs/training/dayXX-*.md`

### Final Deliverable

**Production-Ready Data Pipeline**:
- Reliable producers with idempotence
- Scalable consumers with manual offset management
- Stream processing applications
- Schema-managed data flows
- Kafka Connect integration with databases
- Monitoring and alerting setup

### Start Here

1. Read: [README-DATA-ENGINEERS.md](./README-DATA-ENGINEERS.md)
2. Setup: `docker-compose up -d`
3. Run: `./bin/kafka-training-cli.sh --day 1 --demo foundation`

---

## Track 2: Java Developer Track (Alternative)

### Who is this for?

- **Java/Spring Boot Developers** building microservices
- **Backend Developers** creating REST APIs with Kafka
- **Teams** using Spring ecosystem
- **Developers** preferring web UI for learning
- **Projects** requiring Spring Boot integration

### What You'll Learn

- **Spring Boot Integration**: KafkaTemplate, Spring Kafka abstractions
- **REST API Development**: HTTP endpoints for Kafka operations
- **Web UI**: Interactive learning with browser-based demos
- **EventMart Project**: Complete e-commerce event streaming platform
- **Spring Patterns**: Spring Cloud Stream, Spring Boot best practices

### Learning Approach

**Web UI and Spring Boot Services**:
```bash
# Start Spring Boot application
mvn spring-boot:run

# Open browser
http://localhost:8080

# Click "Run Demo" buttons for each day
# Day 1 → Day 2 → Day 3 → ... → Day 8
```

### Daily Structure

```
Day 1: Spring Boot Topic Management Service
Day 2: REST API for Data Flow Operations
Day 3: Producer Microservices with KafkaTemplate
Day 4: Consumer Microservices with Spring Kafka
Day 5: Stream Processing with Spring Cloud Stream
Day 6: Schema Registry Integration
Day 7: Kafka Connect Pipelines
Day 8: Secured, Monitored Spring Boot Services
EventMart: Progressive E-commerce Platform Project
```

### Code Examples Location

- **Spring Services**: `src/main/java/com/training/kafka/services/Day03ProducerService.java`
- **REST Controllers**: `src/main/java/com/training/kafka/controllers/TrainingController.java`
- **EventMart**: `src/main/java/com/training/kafka/eventmart/`
- **Documentation**: `docs/training/dayXX-*.md`

### Final Deliverable

**EventMart E-commerce Platform**:
- User service (registrations, updates)
- Product service (catalog, inventory)
- Order service (lifecycle events)
- Payment service (transactions)
- Real-time analytics with Kafka Streams
- Schema Registry for data quality
- External system integration
- Production-ready Spring Boot deployment

### Start Here

1. Read: [WEB-UI-GETTING-STARTED.md](./WEB-UI-GETTING-STARTED.md)
2. Setup: `docker-compose up -d && mvn spring-boot:run`
3. Browse: `http://localhost:8080`
4. Build: [EVENTMART-PROJECT-GUIDE.md](./EVENTMART-PROJECT-GUIDE.md)

---

## Track Comparison

| Aspect | Data Engineer Track | Java Developer Track |
|--------|---------------------|----------------------|
| **Primary Focus** | Pure Kafka fundamentals | Spring Boot integration |
| **Audience** | Data engineers, platform engineers | Java developers, backend devs |
| **Entry Point** | CLI commands, terminal | Web browser, GUI |
| **APIs Used** | Raw Kafka APIs (KafkaProducer/Consumer) | Spring abstractions (KafkaTemplate) |
| **Run Method** | `./bin/kafka-training-cli.sh` | `mvn spring-boot:run` |
| **Code Examples** | `DayXX*/AdvancedProducer.java` | `services/DayXXService.java` |
| **Language Focus** | Platform-agnostic (Java + Python) | Java/Spring-specific |
| **Use Cases** | Data pipelines, ETL, streaming | Microservices, REST APIs |
| **Final Project** | Individual production examples | EventMart e-commerce platform |
| **Transferability** | Works with ANY platform/language | Spring Boot specific |
| **Learning Style** | Command-line, code-first | Visual, interactive |
| **Documentation** | README-DATA-ENGINEERS.md | WEB-UI-GETTING-STARTED.md |

---

## Recommended Paths by Role

### I'm a Data Engineer
**Recommended**: Data Engineer Track

```
1. Start with README-DATA-ENGINEERS.md
2. Use CLI tools and pure Java examples
3. Focus on production patterns and data pipeline design
4. Integrate with Spark/Flink/Airflow later
5. Optional: Explore Spring Boot track if needed
```

### I'm a Java/Spring Boot Developer
**Recommended**: Java Developer Track

```
1. Start with WEB-UI-GETTING-STARTED.md
2. Use web UI for interactive learning
3. Build EventMart progressive project
4. Learn Spring Boot Kafka integration
5. Optional: Study Data Engineer track for deeper fundamentals
```

### I'm New to Kafka
**Recommended**: Data Engineer Track (more foundational)

```
1. Start with pure Kafka concepts (Data Engineer track)
2. Learn CLI tools and raw APIs first
3. Understand fundamentals before abstractions
4. Then explore Spring Boot if building Java apps
```

### I Want Production-Ready Skills
**Recommended**: Data Engineer Track

```
1. Pure Kafka knowledge is transferable
2. CLI skills work in any environment
3. Raw API understanding is essential
4. Framework abstractions come and go
5. Platform-agnostic skills are future-proof
```

### I'm Building Spring Boot Microservices
**Recommended**: Java Developer Track

```
1. Start with Spring Boot integration
2. Build EventMart project
3. Learn Spring Kafka patterns
4. Reference Data Engineer track for fundamentals when needed
```

---

## Hybrid Approach (Best of Both Worlds)

### Week 1: Data Engineer Track
Learn pure Kafka fundamentals:
```bash
# CLI and raw Kafka APIs
./bin/kafka-training-cli.sh --day 1 --demo foundation
java -cp target/*.jar com.training.kafka.Day03Producers.AdvancedProducer
```

**Why**: Understand core concepts without abstractions

### Week 2: Java Developer Track
Apply with Spring Boot:
```bash
# Spring Boot integration
mvn spring-boot:run
# Build EventMart project
```

**Why**: Learn practical Spring Boot patterns

### Result
- Deep understanding of Kafka fundamentals
- Practical Spring Boot skills
- Best of both worlds

---

## Daily Learning Structure (Both Tracks)

### 1. Theory (1 hour)
Read day's documentation in `docs/training/dayXX-*.md`

**Content is the same for both tracks** - covers core Kafka concepts

### 2. Hands-On Practice (1 hour)

**Data Engineer Track**:
```bash
# Run CLI demos
./bin/kafka-training-cli.sh --day X --demo <name>

# Or run pure Java
java -cp target/*.jar com.training.kafka.DayXX.<Example>
```

**Java Developer Track**:
```bash
# Start Spring Boot app
mvn spring-boot:run

# Use web interface
http://localhost:8080
```

### 3. Exercises (1 hour)
Complete hands-on exercises in `exercises/dayXX-exercises.md`

**Exercises available for both tracks** - choose your approach

---

## Course Structure (8 Days)

### Phase 1: Foundation (Days 1-2)
**All Tracks**: Learn Kafka architecture, topics, partitions, data flow

- **Data Engineer Track**: CLI tools, AdminClient API
- **Java Developer Track**: Spring Boot admin service, web UI

### Phase 2: Core Development (Days 3-4)
**All Tracks**: Producers and consumers, the heart of Kafka

- **Data Engineer Track**: Raw KafkaProducer/Consumer APIs
- **Java Developer Track**: KafkaTemplate, Spring Kafka

### Phase 3: Stream Processing (Day 5)
**All Tracks**: Real-time data processing with Kafka Streams

- **Data Engineer Track**: Pure Kafka Streams API
- **Java Developer Track**: Spring Cloud Stream

### Phase 4: Advanced Topics (Days 6-8)
**All Tracks**: Schemas, integration, production hardening

- **Data Engineer Track**: Schema Registry, Kafka Connect, K8s
- **Java Developer Track**: Spring integration, EventMart completion

---

## Assessment & Deliverables

### Data Engineer Track Assessment

**Daily Checkpoints** (8 days):
- Working CLI commands and pure Java examples
- Understanding of core Kafka concepts
- Ability to explain partitioning, offsets, consumer groups
- Production-ready code patterns

**Final Deliverable**:
- Production data pipeline demonstration
- Raw Kafka API usage
- Error handling and reliability patterns
- Schema management and Connect integration

### Java Developer Track Assessment

**Daily Checkpoints** (8 days):
- Working Spring Boot services
- REST API endpoints functional
- Web UI demonstrations
- EventMart progressive build

**Final Deliverable**:
- EventMart platform demo (30 minutes)
- Live event generation and processing
- Real-time analytics dashboard
- Spring Boot best practices

---

## Quick Start Guide

### Data Engineer Track Quick Start

```bash
# 1. Start Kafka
docker-compose up -d

# 2. Run Day 1
./bin/kafka-training-cli.sh --day 1 --demo foundation

# 3. Read documentation
cat docs/training/day01-foundation.md

# 4. Practice exercises
cat exercises/day01-exercises.md
```

### Java Developer Track Quick Start

```bash
# 1. Start everything
docker-compose up -d && mvn spring-boot:run

# 2. Open browser
http://localhost:8080

# 3. Click "Run Demo" for Day 1

# 4. Explore EventMart
cat EVENTMART-PROJECT-GUIDE.md
```

---

## Documentation Resources

### Core Documentation
- **[README-DATA-ENGINEERS.md](./README-DATA-ENGINEERS.md)** - Data Engineer track
- **[README.md](./README.md)** - Java Developer track (Spring Boot)
- **[START-HERE.md](./START-HERE.md)** - Quick start for both tracks
- **[GETTING-STARTED.md](./GETTING-STARTED.md)** - Detailed setup

### Training Documentation
- **[docs/training/](./docs/training/)** - Day-by-day learning (both tracks)
- **[exercises/](./exercises/)** - Hands-on practice (both tracks)

### Java Developer Specific
- **[WEB-UI-GETTING-STARTED.md](./WEB-UI-GETTING-STARTED.md)** - Web interface guide
- **[EVENTMART-PROJECT-GUIDE.md](./EVENTMART-PROJECT-GUIDE.md)** - Progressive project
- **[PROJECT-FRAMEWORK.md](./PROJECT-FRAMEWORK.md)** - EventMart architecture

### Specialized Topics
- **[docs/containers/](./docs/containers/)** - Docker and Kubernetes (both tracks)
- **[docs/api/](./docs/api/)** - REST API reference (Java track only)
- **[docs/architecture/](./docs/architecture/)** - System design (both tracks)

---

## Summary

**Data Engineer Track**: Pure Kafka, CLI-first, platform-agnostic, transferable skills

**Java Developer Track**: Spring Boot, web UI, EventMart project, Java-specific

**Both tracks teach the same Kafka concepts - just different approaches to learning.**

**Recommendation**: If unsure, start with Data Engineer track. It's more foundational and transferable. You can always explore Spring Boot integration later.

**Choose your track and start learning!**
