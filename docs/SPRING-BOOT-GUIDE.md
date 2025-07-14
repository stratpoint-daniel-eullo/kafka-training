# Spring Boot + Kafka Training Guide

## 🎯 Overview

This guide covers the Spring Boot implementation of the Kafka Training Course. The application provides a modern, production-ready approach to learning Apache Kafka with Spring Boot integration.

## 🏗️ Architecture

### Spring Boot Application Structure
```
KafkaTrainingApplication (Main)
├── Controllers (REST API + Web Interface)
├── Services (Business Logic)
├── Configuration (Auto-configuration)
└── EventMart (Progressive Project)
```

### Key Components

#### 1. Main Application
- **KafkaTrainingApplication.java**: Spring Boot main class with comprehensive startup logging
- **Auto-configuration**: Automatic Kafka client setup based on profiles
- **Profile Support**: Environment-specific configurations

#### 2. Controllers
- **TrainingController.java**: REST API endpoints for all training modules
- **WebController.java**: Web interface serving HTML pages
- **Profile endpoint**: `/api/training/profile` for environment information

#### 3. Services
- **Day01FoundationService**: Kafka fundamentals and topic operations
- **Day03ProducerService**: Producer patterns and configurations
- **Day04ConsumerService**: Consumer groups and message processing
- **EventMartService**: Progressive project simulation

#### 4. Configuration
- **TrainingKafkaAutoConfiguration**: Automatic Kafka client beans
- **ProfileConfiguration**: Environment-specific initialization
- **Multiple profiles**: dev, test, staging, prod, docker

## 🌐 Web Interface

### Main Features
- **Interactive Dashboard**: Overview of all training modules
- **Real-time Demonstrations**: Execute Kafka operations through web UI
- **API Documentation**: Built-in endpoint documentation
- **Profile Management**: Switch between environments
- **EventMart Simulation**: Complete e-commerce demo

### Available Endpoints

#### Web Interface
- `GET /` - Main dashboard
- `GET /training` - Training modules overview
- `GET /eventmart` - EventMart simulation interface

#### REST API
- `GET /api/training/modules` - List all training modules
- `POST /api/training/day01/demo` - Run Day 1 demonstration
- `POST /api/training/day03/demo` - Run Day 3 producer demo
- `POST /api/training/day04/demo` - Run Day 4 consumer demo
- `GET /api/training/profile` - Current profile information

#### EventMart API
- `POST /api/training/eventmart/topics` - Create EventMart topics
- `GET /api/training/eventmart/status` - Get EventMart status
- `POST /api/training/eventmart/simulate/user` - Simulate user registration
- `POST /api/training/eventmart/simulate/product` - Simulate product creation
- `POST /api/training/eventmart/simulate/order` - Simulate order placement

## 🔧 Configuration Profiles

### Development Profile (`dev`)
**File**: `application-dev.yml`
**Purpose**: Local development with enhanced debugging

**Features**:
- Enhanced logging (DEBUG level for training packages)
- Auto-restart with DevTools
- Debug features enabled
- Demo data generation enabled
- Detailed error messages
- Local Kafka connection (localhost:9092)

**Usage**:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Test Profile (`test`)
**File**: `application-test.yml`
**Purpose**: Automated testing with TestContainers

**Features**:
- TestContainers integration
- Reduced logging for cleaner test output
- Fast startup optimizations
- In-memory database configured
- Random port assignment
- Optimized for CI/CD

**Usage**:
```bash
mvn test -Dspring.profiles.active=test
```

### Staging Profile (`staging`)
**File**: `application-staging.yml`
**Purpose**: Production-like environment for testing

**Features**:
- Production-like settings
- Enhanced monitoring enabled
- Debug features available for testing
- External Kafka cluster connection
- Comprehensive logging
- Environment variable support

### Production Profile (`prod`)
**File**: `application-prod.yml`
**Purpose**: Production deployment

**Features**:
- Optimized for performance and security
- Comprehensive monitoring enabled
- Debug features disabled
- External configuration required
- Security headers enabled
- Prometheus metrics export

### Docker Profile (`docker`)
**File**: `application-docker.yml`
**Purpose**: Container deployment

**Features**:
- Container-optimized settings
- Docker Compose integration
- Service discovery via container names
- Health checks enabled
- Volume-based logging

## 🐳 Docker Support

### Dockerfile
Multi-stage build with security best practices:
- **Build stage**: Maven with JDK 11
- **Runtime stage**: JRE 11 Alpine
- **Security**: Non-root user execution
- **Health checks**: Built-in health monitoring
- **Optimization**: Container-aware JVM settings

### Docker Compose
Complete development environment:
- **Kafka + Zookeeper**: Confluent Platform 7.7.0
- **Spring Boot App**: Auto-built from source
- **Kafka UI**: Management interface
- **Monitoring Stack**: Prometheus + Grafana (optional)
- **Networking**: Isolated network for services

**Usage**:
```bash
# Start complete environment
docker-compose up -d

# Start with monitoring
docker-compose --profile monitoring up -d

# View logs
docker-compose logs -f kafka-training-app
```

## 🧪 Testing Strategy

### TestContainers Integration
- **Real Kafka Testing**: Authentic Kafka integration without manual setup
- **Isolated Tests**: Fresh containers for each test run
- **CI/CD Ready**: Works in any environment with Docker
- **Comprehensive Coverage**: All Spring Boot components tested

### Test Structure
- **SpringBootKafkaTrainingTest**: Main integration test
- **12 Test Cases**: Comprehensive coverage of all features
- **Web Interface Testing**: REST API and web endpoints
- **Service Testing**: All Spring Boot services validated
- **EventMart Testing**: Complete simulation workflow

### Running Tests
```bash
# All tests with TestContainers
mvn test

# Specific Spring Boot test
mvn test -Dtest=SpringBootKafkaTrainingTest

# Generate coverage report
mvn jacoco:report
```

## 📊 Monitoring & Observability

### Spring Boot Actuator
Built-in monitoring endpoints:
- `/actuator/health` - Application health
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics
- `/actuator/env` - Environment properties
- `/actuator/configprops` - Configuration properties

### Prometheus Integration
- Metrics export enabled in production profile
- Custom application metrics
- Kafka client metrics
- JVM and system metrics

### Logging
- **Structured logging**: JSON format in production
- **Profile-specific levels**: Debug in dev, warn in prod
- **File rotation**: Size and time-based rotation
- **Centralized logging**: Ready for ELK stack integration

## 🚀 Deployment Options

### Local Development
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Docker Deployment
```bash
docker-compose up -d
```

### Kubernetes Deployment
Ready for Kubernetes with:
- Health checks configured
- Environment variable support
- ConfigMap integration
- Secret management support

### Cloud Deployment
Compatible with:
- AWS ECS/EKS
- Google Cloud Run/GKE
- Azure Container Instances/AKS
- Heroku
- Cloud Foundry

## 🎯 Best Practices Implemented

### Spring Boot
- Auto-configuration for Kafka clients
- Profile-based configuration management
- Comprehensive error handling
- Graceful shutdown handling
- Health checks and monitoring

### Kafka Integration
- Connection pooling and reuse
- Proper serialization/deserialization
- Error handling and retry logic
- Consumer group management
- Producer optimization

### Security
- Non-root container execution
- Environment variable configuration
- Security headers in production
- Input validation and sanitization

### Performance
- Connection pooling
- Batch processing optimization
- Memory-efficient operations
- Container-aware JVM settings

## 🔄 Development Workflow

### 1. Setup
```bash
git clone <repository>
cd kafka-training-java
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 2. Development
- Use development profile for enhanced debugging
- Leverage DevTools for auto-restart
- Access web interface at http://localhost:8080
- Use REST API for programmatic access

### 3. Testing
```bash
mvn test  # Run all tests with TestContainers
```

### 4. Deployment
```bash
docker-compose up -d  # Local Docker deployment
```

## 📚 Learning Path

### Phase 1: Spring Boot Basics
1. Understand Spring Boot auto-configuration
2. Explore profile-based configuration
3. Use web interface for demonstrations
4. Test with TestContainers

### Phase 2: Kafka Integration
1. Study service implementations
2. Understand Spring Kafka configuration
3. Practice with REST API endpoints
4. Build EventMart progressive project

### Phase 3: Production Readiness
1. Configure production profile
2. Set up monitoring and logging
3. Deploy with Docker Compose
4. Implement security best practices

This Spring Boot implementation provides a modern, production-ready approach to learning Apache Kafka while following enterprise best practices.
