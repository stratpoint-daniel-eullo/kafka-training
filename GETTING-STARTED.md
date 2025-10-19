# Getting Started with Kafka Training

Welcome to the Apache Kafka Training Course! This guide will help you get started quickly based on your role and experience level.

## Choose Your Track

This training offers **two distinct learning tracks**:

### Track 1: Data Engineer (RECOMMENDED - Platform-Agnostic)

**Perfect for**:
- Data engineers building data pipelines
- Platform engineers managing Kafka infrastructure
- Developers using Kafka with Spark, Flink, Airflow, or Python
- Anyone wanting pure, transferable Kafka knowledge

**What you'll learn**:
- Raw Kafka APIs (KafkaProducer, KafkaConsumer, AdminClient)
- Platform-agnostic concepts that work with ANY language
- CLI-first, production-ready patterns
- Container-based development and deployment

**Start here**: [README-DATA-ENGINEERS.md](./README-DATA-ENGINEERS.md)

---

### Track 2: Java Developer (Alternative - Spring Boot Integration)

**Perfect for**:
- Java/Spring Boot developers building microservices
- Backend developers creating REST APIs with Kafka
- Teams using Spring ecosystem
- Developers preferring web UI for learning

**What you'll learn**:
- Spring Boot Kafka integration
- KafkaTemplate and Spring abstractions
- REST API development with Kafka
- Web UI for interactive learning

**Start here**: [WEB-UI-GETTING-STARTED.md](./WEB-UI-GETTING-STARTED.md)

---

## Quick Setup (All Tracks)

### Prerequisites

- **Docker** and Docker Compose (required)
- **Java 11+** (Java 21 recommended)
- **Maven 3.8+** (for Java examples)
- **Python 3.8+** (optional, for Python examples in Data Engineer track)

### 1. Clone and Build

```bash
# Clone repository
git clone https://github.com/rcdelacruz/kafka-training-java.git
cd kafka-training-java

# Build project
mvn clean package
```

### 2. Start Kafka Infrastructure

```bash
# Start Kafka, Zookeeper, Schema Registry, Kafka Connect
docker-compose up -d

# Verify services are running
docker-compose ps
```

### 3. Verify Setup

```bash
# Run verification script
./scripts/verify-setup.sh

# Or manually check Kafka
docker exec kafka-training-kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --list
```

---

## Path-Specific Quick Starts

### Data Engineer Track - Quick Start

**1. Run CLI Examples** (Recommended):
```bash
# Day 1: Topic management
./bin/kafka-training-cli.sh --day 1 --demo foundation

# Day 3: Produce messages
./bin/kafka-training-cli.sh produce \
  --topic user-events \
  --key user-123 \
  --value '{"action":"login"}'

# Day 4: Consume messages
./bin/kafka-training-cli.sh consume \
  --topic user-events \
  --group my-consumers
```

**2. Run Pure Java Examples**:
```bash
# Run producer with raw Kafka APIs
java -cp target/kafka-training-java-1.0.0.jar \
  com.training.kafka.Day03Producers.AdvancedProducer

# Run consumer with raw Kafka APIs
java -cp target/kafka-training-java-1.0.0.jar \
  com.training.kafka.Day04Consumers.AdvancedConsumer
```

**3. Optional: Python Examples**:
```bash
# Install Python dependencies
cd examples/python
pip install -r requirements.txt

# Run Python producer (same concepts as Java!)
python day03_producer.py

# Run Python consumer
python day04_consumer.py
```

**Next**: Read [README-DATA-ENGINEERS.md](./README-DATA-ENGINEERS.md) for complete guide

---

### Java Developer Track - Quick Start

**1. Start Spring Boot Application**:
```bash
# Start the web application
mvn spring-boot:run

# Or run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**2. Open Web Interface**:
```
Open browser: http://localhost:8080
```

**3. Run Interactive Demos**:
```
1. Go to "Day 1: Kafka Fundamentals"
2. Click "Run Demo" button
3. Explore each section and click buttons
4. Check terminal logs for output
```

**Next**: Read [WEB-UI-GETTING-STARTED.md](./WEB-UI-GETTING-STARTED.md) for complete guide

---

## Course Structure Overview

| Phase | Days | Focus | All Tracks |
|-------|------|-------|-----------|
| **Foundation** | 1-2 | Topics, Partitions, Data Flow | 6 hours |
| **Development** | 3-4 | Producers, Consumers | 6 hours |
| **Stream Processing** | 5 | Kafka Streams | 3 hours |
| **Advanced** | 6-8 | Schemas, Connect, Production | 9 hours |

## What You'll Build

### Data Engineer Track

Throughout the course, you'll build:

- **Day 1**: Topic management with AdminClient API
- **Day 2**: Understanding data flow and partitioning strategies
- **Day 3**: Reliable message producers with idempotence and error handling
- **Day 4**: Scalable consumer applications with manual offset management
- **Day 5**: Real-time stream processing with Kafka Streams
- **Day 6**: Schema-managed data pipelines with Avro
- **Day 7**: Data integration with Kafka Connect (CDC, databases)
- **Day 8**: Production-ready configurations and monitoring

**Focus**: Pure Kafka, CLI tools, production patterns, platform integration

### Java Developer Track

Throughout the course, you'll build:

- **Day 1**: Spring Boot topic management service
- **Day 2**: REST API for data flow operations
- **Day 3**: Producer microservices with KafkaTemplate
- **Day 4**: Consumer microservices with Spring Kafka
- **Day 5**: Stream processing with Spring Cloud Stream
- **Day 6**: Schema Registry integration
- **Day 7**: Kafka Connect pipelines
- **Day 8**: Secured, monitored Spring Boot services
- **EventMart Project**: Complete e-commerce platform

**Focus**: Spring Boot, web UI, REST APIs, EventMart project

---

## Learning Approach by Experience Level

### Complete Beginner (New to Kafka)

**Recommended**: Data Engineer Track (pure fundamentals)

```bash
# Step 1: Setup
docker-compose up -d
mvn clean package

# Step 2: Learn basics
./bin/kafka-training-cli.sh --day 1 --demo foundation

# Step 3: Study documentation
# Read docs/training/day01-foundation.md

# Step 4: Practice
# Complete exercises/day01-exercises.md
```

**Time Investment**: Start with Days 1-4 (12 hours)

---

### Experienced Developer (Know Java, new to Kafka)

**Recommended**: Start with Data Engineer Track, then explore Java Track

```bash
# Week 1: Pure Kafka fundamentals
# Use CLI and raw Kafka APIs

# Week 2: Spring Boot integration
# Use web UI and KafkaTemplate

# Result: Deep understanding + practical Spring Boot skills
```

**Fast Track Focus**:
- Skim: Day 1-2 concepts
- Deep dive: Day 3-4 (Producers/Consumers)
- Advanced: Day 5-8

---

### Kafka User (Want to deepen knowledge)

**Recommended**: Data Engineer Track for production patterns

```bash
# Jump to advanced topics
./bin/kafka-training-cli.sh --day 5 --demo streams
./bin/kafka-training-cli.sh --day 6 --demo schemas
./bin/kafka-training-cli.sh --day 8 --demo security
```

**Focus Areas**:
- Day 5: Stream processing patterns
- Day 6: Schema evolution strategies
- Day 7: Kafka Connect at scale
- Day 8: Production hardening

---

## Daily Structure

Each day follows this pattern:

### 1. Theory (1 hour)
Read the day's documentation in `docs/training/dayXX-*.md`

### 2. Examples (1 hour)

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
# http://localhost:8080
```

### 3. Practice (1 hour)
Complete hands-on exercises in `exercises/dayXX-exercises.md`

---

## Verification

### Quick Verification (All Tracks)

```bash
# Check environment
./scripts/verify-setup.sh

# Test Kafka is running
docker exec kafka-training-kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --list

# Check services
docker-compose ps
```

### Track-Specific Verification

**Data Engineer Track**:
```bash
# Test CLI works
./bin/kafka-training-cli.sh --help

# Run a quick producer test
java -cp target/kafka-training-java-1.0.0.jar \
  com.training.kafka.Day03Producers.SimpleProducer
```

**Java Developer Track**:
```bash
# Start Spring Boot
mvn spring-boot:run

# Check health endpoint
curl http://localhost:8080/actuator/health

# Access web UI
open http://localhost:8080
```

---

## Common Setup Issues

### Java Version
```bash
# Check Java version
java --version

# Should be 11 or higher (21 recommended)
# Install: brew install openjdk@21 (macOS)
```

### Kafka Not Starting
```bash
# Check what's using port 9092
lsof -i :9092

# Restart Kafka
docker-compose restart kafka

# Or clean restart
docker-compose down -v
docker-compose up -d
```

### Maven Issues
```bash
# Clean and rebuild
mvn clean compile

# Check Maven version
mvn --version  # Should be 3.8+
```

### Docker Issues
```bash
# Check Docker is running
docker ps

# Restart Docker service if needed
# Check logs
docker-compose logs kafka
```

---

## Getting Help

### During Setup
1. Run `./scripts/verify-setup.sh` for diagnostics
2. Check `docker-compose logs <service>` for errors
3. Review troubleshooting section in README files

### During Learning

**Data Engineer Track**:
- Read `docs/training/dayXX-*.md` documentation
- Check `exercises/dayXX-exercises.md` for solutions
- Review pure Java examples in `src/main/java/com/training/kafka/DayXX*/`

**Java Developer Track**:
- Read `docs/training/dayXX-*.md` documentation
- Use web UI at `http://localhost:8080`
- Review Spring Boot services in `src/main/java/com/training/kafka/services/`

---

## Ready to Start?

### Data Engineers (RECOMMENDED)

**Start here**: [README-DATA-ENGINEERS.md](./README-DATA-ENGINEERS.md)

```bash
# Quick start
docker-compose up -d
./bin/kafka-training-cli.sh --day 1 --demo foundation
```

Then proceed to:
1. [Day 1 Documentation](./docs/training/day01-foundation.md)
2. [Day 1 Exercises](./exercises/day01-exercises.md)

---

### Java Developers

**Start here**: [WEB-UI-GETTING-STARTED.md](./WEB-UI-GETTING-STARTED.md)

```bash
# Quick start
docker-compose up -d
mvn spring-boot:run
open http://localhost:8080
```

Then proceed to:
1. [Day 1 Documentation](./docs/training/day01-foundation.md)
2. [EventMart Project Guide](./EVENTMART-PROJECT-GUIDE.md)

---

## Track Comparison Summary

| Aspect | Data Engineer Track | Java Developer Track |
|--------|---------------------|----------------------|
| **Primary Audience** | Data engineers, platform engineers | Java/Spring Boot developers |
| **Entry Point** | CLI commands | Web browser |
| **Code Examples** | Pure KafkaProducer/Consumer | KafkaTemplate, Spring Kafka |
| **Run Method** | `./bin/kafka-training-cli.sh` | `mvn spring-boot:run` |
| **Language Focus** | Platform-agnostic (Java + Python) | Java/Spring-specific |
| **Use Cases** | Data pipelines, ETL, streaming | Microservices, REST APIs |
| **Project** | Individual examples | EventMart e-commerce platform |
| **Documentation** | README-DATA-ENGINEERS.md | WEB-UI-GETTING-STARTED.md |

---

**Pro Tip**: Not sure which track? Start with the Data Engineer track - it teaches foundational Kafka concepts that apply everywhere. You can always explore Spring Boot integration later!

**Both tracks use the same Kafka infrastructure and cover the same concepts - just different approaches to learning.**
