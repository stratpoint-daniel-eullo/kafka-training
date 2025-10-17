# Apache Kafka Training Course with Spring Boot

![Kafka Training](https://img.shields.io/badge/Apache%20Kafka-Training-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen)
![Java](https://img.shields.io/badge/Java-11+-blue)
![Confluent](https://img.shields.io/badge/Confluent-Platform-green)
![Docker](https://img.shields.io/badge/Docker-Supported-blue)
![License](https://img.shields.io/badge/license-MIT-blue.svg)

## 📚 Documentation

**[View Complete Documentation](https://yourusername.github.io/kafka-training-java)** - Full MkDocs Material site with comprehensive guides, API reference, and architecture diagrams.

## 🎯 Overview

This comprehensive 8-day training course takes you from zero to proficient with Apache Kafka using **Spring Boot and Java**. Designed for developers of all levels, it provides hands-on experience with real-world scenarios and follows industry best practices with modern Spring Boot integration.

### 🚀 **EventMart Progressive Project**
Build a complete **e-commerce event streaming platform** throughout the 8 days using **Spring Boot**! Each day adds new functionality, culminating in a professional demo showcasing all Kafka concepts with Spring Boot best practices. Perfect for portfolios and job interviews.

### 🌐 **Spring Boot Web Interface**
- **Interactive Web UI** at `http://localhost:8080`
- **REST API endpoints** for all training modules
- **Real-time demonstrations** with Spring Boot integration
- **Profile-based configurations** for different environments

### 🎯 **IMPORTANT: Choose Your Learning Path**
This training offers **two approaches** - choose based on your goal:

- **🎭 FOR DEMO & ASSESSMENT**: [EventMart Progressive Project](./EVENTMART-PROJECT-GUIDE.md) ← **RECOMMENDED**
- **📚 FOR CONCEPT STUDY**: [docs/](./docs/) and [exercises/](./exercises/) directories

👉 **[See Complete Learning Path Guide](./LEARNING-PATHS.md)**

## 📚 Course Structure

### Phase 1: Foundation (Days 1-2)
- **Day 1**: Kafka fundamentals, architecture, and setup
- **Day 2**: Data flow, partitioning, and message patterns

### Phase 2: Java Development (Days 3-5)
- **Day 3**: Java Producer development and patterns
- **Day 4**: Java Consumer implementation and groups
- **Day 5**: Stream processing with Kafka Streams

### Phase 3: Advanced Topics (Days 6-8)
- **Day 6**: Schema management with Avro
- **Day 7**: Kafka Connect integration
- **Day 8**: Security, monitoring, and production best practices

## 🚀 Quick Start

### Prerequisites
- Java 11 or higher
- Maven 3.8+
- Git
- Docker (optional, for containerized setup)

### Setup Options

#### Option 1: Spring Boot Development Mode (Recommended)
```bash
git clone <this repo>
cd kafka-training-java

# Start with development profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Access web interface
open http://localhost:8080
```

#### Option 2: Docker Compose Setup (Recommended for Production-like Environment)
```bash
git clone <this repo>
cd kafka-training-java

# Start complete environment with Kafka + Spring Boot
docker-compose up -d

# Access services
# - Spring Boot App: http://localhost:8080
# - Kafka UI: http://localhost:8081
# - Prometheus: http://localhost:9090 (optional)
# - Grafana: http://localhost:3000 (optional)
```

#### Option 3: Testing with TestContainers
```bash
git clone <this repo>
cd kafka-training-java

# Run comprehensive Spring Boot tests
mvn test -Dtest=SpringBootKafkaTrainingTest

# All tests use TestContainers for real Kafka integration
```

#### Option 4: Different Spring Boot Profiles
```bash
# Development profile (enhanced logging, debug features)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Test profile (optimized for testing)
mvn spring-boot:run -Dspring-boot.run.profiles=test

# Docker profile (container-optimized)
mvn spring-boot:run -Dspring-boot.run.profiles=docker

# Production profile (security & performance optimized)
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## 📖 Learning Path with Spring Boot

| Day | Topic | Spring Boot Services | Web Interface | Documentation |
|-----|-------|---------------------|---------------|---------------|
| 1 | [Foundation](./docs/day01-foundation.md) | `Day01FoundationService` | `/api/training/day01/*` | [Exercises](./exercises/day01-exercises.md) |
| 2 | [Data Flow](./docs/day02-dataflow.md) | CLI + Web Concepts | Web UI + API | [Exercises](./exercises/day02-exercises.md) |
| 3 | [Producers](./docs/day03-producers.md) | `Day03ProducerService` | `/api/training/day03/*` | [Exercises](./exercises/day03-exercises.md) |
| 4 | [Consumers](./docs/day04-consumers.md) | `Day04ConsumerService` | `/api/training/day04/*` | [Exercises](./exercises/day04-exercises.md) |
| 5 | [Streams](./docs/day05-streams.md) | `StreamProcessorService` | `/api/training/day05/*` | [Exercises](./exercises/day05-exercises.md) |
| 6 | [Schemas](./docs/day06-schemas.md) | `AvroSchemaService` | `/api/training/day06/*` | [Exercises](./exercises/day06-exercises.md) |
| 7 | [Connect](./docs/day07-connect.md) | `ConnectService` | `/api/training/day07/*` | [Exercises](./exercises/day07-exercises.md) |
| 8 | [Advanced](./docs/day08-advanced.md) | `SecurityService`, `MonitoringService` | `/api/training/day08/*` | [Exercises](./exercises/day08-exercises.md) |

### 🌐 Spring Boot Web Interface Features
- **Interactive Demonstrations**: Run all examples through web UI
- **Real-time Monitoring**: See Kafka operations in action
- **API Documentation**: Built-in Swagger/OpenAPI support
- **Profile Management**: Switch between environments easily
- **EventMart Simulation**: Complete e-commerce demo platform

## 🛠 Spring Boot Project Structure

### 🎭 **EventMart Progressive Project** (For Demo & Assessment)
```
src/main/java/com/training/kafka/
├── KafkaTrainingApplication.java     # Spring Boot main application
├── controllers/
│   ├── TrainingController.java       # REST API endpoints
│   └── WebController.java            # Web interface controller
├── services/                         # Spring Boot services
│   ├── Day01FoundationService.java   # Day 1: Kafka fundamentals
│   ├── Day03ProducerService.java     # Day 3: Producer patterns
│   ├── Day04ConsumerService.java     # Day 4: Consumer groups
│   └── EventMartService.java         # EventMart simulation
├── config/                           # Spring Boot configuration
│   ├── TrainingKafkaAutoConfiguration.java
│   └── ProfileConfiguration.java     # Environment profiles
├── eventmart/                        # Progressive project
│   ├── events/                       # Event schemas (Avro)
│   ├── simulation/                   # Demo data generation
│   └── models/                       # Domain models
└── avro/                            # Avro schema definitions
```

### 📚 **Spring Boot Resources**
```
kafka-training-java/
├── src/main/resources/
│   ├── application.yml               # Default configuration
│   ├── application-dev.yml           # Development profile
│   ├── application-test.yml          # Testing profile
│   ├── application-prod.yml          # Production profile
│   ├── application-docker.yml        # Docker profile
│   └── static/                       # Web interface assets
├── docs/                             # Day-by-day documentation
├── exercises/                        # Practice exercises
├── docker-compose.yml                # Complete Docker setup
├── Dockerfile                        # Spring Boot containerization
└── src/test/java/                    # Comprehensive test suite
```

## 🎮 Running Spring Boot Examples

### 🌐 Web Interface (Recommended)
```bash
# Start Spring Boot application
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Access web interface
open http://localhost:8080

# Available endpoints:
# - GET  /                           # Main web interface
# - GET  /api/training/modules       # List all training modules
# - POST /api/training/day01/demo    # Run Day 1 demonstration
# - POST /api/training/day03/demo    # Run Day 3 producer demo
# - POST /api/training/day04/demo    # Run Day 4 consumer demo
# - GET  /api/training/profile       # Current profile information
```

### 🎭 EventMart Progressive Project
```bash
# Create EventMart topics
curl -X POST http://localhost:8080/api/training/eventmart/topics

# Simulate user registration
curl -X POST "http://localhost:8080/api/training/eventmart/simulate/user?userId=user123&email=user@example.com&name=John Doe"

# Simulate product creation
curl -X POST "http://localhost:8080/api/training/eventmart/simulate/product?productId=prod123&name=Laptop&category=Electronics&price=999.99"

# Simulate order placement
curl -X POST "http://localhost:8080/api/training/eventmart/simulate/order?orderId=order123&userId=user123&amount=999.99"

# Get EventMart status
curl http://localhost:8080/api/training/eventmart/status
```

### 📡 REST API Examples
```bash
# Get all training modules
curl http://localhost:8080/api/training/modules

# Run Day 1 foundation demonstration
curl -X POST http://localhost:8080/api/training/day01/demo

# Run Day 3 producer demonstration
curl -X POST http://localhost:8080/api/training/day03/demo

# Run Day 4 consumer demonstration
curl -X POST http://localhost:8080/api/training/day04/demo
```

## 🧪 Spring Boot Testing

### Comprehensive Test Suite
```bash
# Run all Spring Boot tests (includes TestContainers)
mvn test

# Run specific Spring Boot integration test
mvn test -Dtest=SpringBootKafkaTrainingTest

# Run tests with specific profile
mvn test -Dspring.profiles.active=test

# Generate test coverage report
mvn jacoco:report
```

### TestContainers Integration
- **Real Kafka Testing**: All tests use TestContainers for authentic Kafka integration
- **Automatic Setup**: No manual Kafka installation required for testing
- **Isolated Tests**: Each test runs with fresh Kafka containers
- **CI/CD Ready**: Tests work in any environment with Docker

## 🔧 Spring Boot Development Environment

### Dependencies (Included)
- **Spring Boot 2.7.18**: Core framework
- **Spring Kafka**: Kafka integration
- **Apache Kafka Clients 3.8.0**: Latest Kafka client
- **Confluent Platform 7.7.0**: Schema registry support
- **Avro 1.12.0**: Schema evolution
- **TestContainers**: Integration testing
- **Spring Boot Actuator**: Monitoring endpoints
- **Spring Boot DevTools**: Development productivity

### IDE Setup
1. Import as Maven project
2. Set Java 11+ as project SDK
3. Run `mvn compile` to generate Avro classes
4. Enable annotation processing
5. Configure Spring Boot run configurations with profiles

### Spring Boot Profiles
- **dev**: Development with enhanced logging and debug features
- **test**: Optimized for testing with TestContainers
- **staging**: Production-like environment for testing
- **prod**: Production-ready with security and performance optimizations
- **docker**: Container-optimized settings

## 🔍 Spring Boot Monitoring & Troubleshooting

### Web Interfaces
- **Spring Boot App**: http://localhost:8080 (Main application)
- **Kafka UI**: http://localhost:8081 (Docker Compose setup)
- **Spring Boot Actuator**: http://localhost:8080/actuator
- **Health Check**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/metrics
- **Prometheus**: http://localhost:9090 (Docker Compose with monitoring profile)
- **Grafana**: http://localhost:3000 (Docker Compose with monitoring profile)

### Spring Boot Actuator Endpoints
```bash
# Application health
curl http://localhost:8080/actuator/health

# Application info
curl http://localhost:8080/actuator/info

# Kafka metrics
curl http://localhost:8080/actuator/metrics

# Environment properties
curl http://localhost:8080/actuator/env

# Configuration properties
curl http://localhost:8080/actuator/configprops
```

### Docker Compose Monitoring
```bash
# Start with monitoring stack
docker-compose --profile monitoring up -d

# View logs
docker-compose logs -f kafka-training-app

# Check service status
docker-compose ps
```

### Common Issues & Solutions
1. **Port conflicts**:
   - Spring Boot: Check `lsof -i :8080`
   - Kafka: Check `lsof -i :9092`
2. **Profile issues**: Verify with `/api/training/profile` endpoint
3. **Kafka connection**: Check logs for connection warnings
4. **Docker issues**: Run `docker-compose down && docker-compose up -d`
## 🎯 Spring Boot Learning Objectives

By completing this course, you will:

✅ **Master Spring Boot + Kafka Integration** - Modern enterprise patterns
✅ **Understand Kafka Architecture** - Core concepts, brokers, topics, partitions
✅ **Build Production-Ready Services** - Spring Boot services with Kafka
✅ **Implement Stream Processing** - Real-time data processing with Spring Kafka
✅ **Handle Schema Evolution** - Avro schemas with Spring Boot auto-configuration
✅ **Configure Multiple Environments** - Dev, test, staging, production profiles
✅ **Containerize Applications** - Docker and Docker Compose deployment
✅ **Test with TestContainers** - Real integration testing without manual setup
✅ **Monitor and Optimize** - Spring Boot Actuator and performance tuning
✅ **Build EventMart Demo** - Complete e-commerce platform for portfolio

## 🏆 What You'll Build

### EventMart E-commerce Platform
A complete event-driven e-commerce platform built with Spring Boot:
- **User Management**: Registration, authentication, profile updates
- **Product Catalog**: Product creation, updates, inventory management
- **Order Processing**: Order placement, payment processing, fulfillment
- **Real-time Analytics**: Sales metrics, user behavior, inventory tracking
- **Event Sourcing**: Complete audit trail of all business events
- **Microservices Ready**: Designed for distributed architecture

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring for Apache Kafka](https://spring.io/projects/spring-kafka)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Confluent Platform Documentation](https://docs.confluent.io/)
- [TestContainers Documentation](https://www.testcontainers.org/)

## 👨‍💻 Author

**Kafka Training Course**
- Comprehensive Spring Boot + Kafka training
- Industry best practices and real-world patterns
- Complete with EventMart progressive project

---

🚀 **Ready to start your Spring Boot + Kafka journey?**

1. **Quick Start**: `mvn spring-boot:run -Dspring-boot.run.profiles=dev`
2. **Open Web Interface**: http://localhost:8080
3. **Begin Training**: [Day 1: Foundation](./docs/day01-foundation.md)

**Perfect for**: Developers, DevOps engineers, architects building event-driven systems
