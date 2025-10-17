# Docker Deployment Guide

## 🐳 Overview

This guide covers deploying the Kafka Training Spring Boot application using Docker and Docker Compose. The setup provides a complete development and production-ready environment.

## 🏗️ Architecture

### Docker Compose Services

```yaml
Services:
├── zookeeper          # Kafka coordination service
├── kafka              # Apache Kafka broker
├── kafka-ui           # Kafka management interface
├── kafka-training-app # Spring Boot application
├── prometheus         # Metrics collection (optional)
└── grafana           # Metrics visualization (optional)
```

### Network Architecture
- **Isolated Network**: `kafka-training-network`
- **Service Discovery**: Container name-based DNS
- **Port Mapping**: External access to key services
- **Volume Persistence**: Data persistence across restarts

## 🚀 Quick Start

### Prerequisites
- Docker 20.10+
- Docker Compose 2.0+
- 4GB+ available RAM
- 10GB+ available disk space

### Basic Deployment
```bash
# Clone repository
git clone <repository>
cd kafka-training-java

# Start all services
docker-compose up -d

# Verify services are running
docker-compose ps

# Access services
# - Spring Boot App: http://localhost:8080
# - Kafka UI: http://localhost:8081
```

### With Monitoring Stack
```bash
# Start with Prometheus and Grafana
docker-compose --profile monitoring up -d

# Access additional services
# - Prometheus: http://localhost:9090
# - Grafana: http://localhost:3000 (admin/admin)
```

## 📋 Service Details

### Zookeeper
- **Image**: `confluentinc/cp-zookeeper:7.7.0`
- **Port**: 2181
- **Purpose**: Kafka cluster coordination
- **Data Persistence**: `zookeeper-data` and `zookeeper-logs` volumes

### Kafka Broker
- **Image**: `confluentinc/cp-kafka:7.7.0`
- **Ports**: 9092 (external), 29092 (internal)
- **Purpose**: Message broker
- **Data Persistence**: `kafka-data` volume
- **Health Check**: Broker API version check

### Kafka UI
- **Image**: `provectuslabs/kafka-ui:latest`
- **Port**: 8081
- **Purpose**: Kafka cluster management and monitoring
- **Features**: Topic management, consumer group monitoring, message browsing

### Spring Boot Application
- **Build**: Multi-stage Dockerfile
- **Port**: 8080
- **Profile**: `docker`
- **Health Check**: Spring Boot Actuator endpoint
- **Dependencies**: Waits for Kafka health check

### Prometheus (Optional)
- **Image**: `prom/prometheus:latest`
- **Port**: 9090
- **Purpose**: Metrics collection
- **Configuration**: `monitoring/prometheus.yml`
- **Profile**: `monitoring`

### Grafana (Optional)
- **Image**: `grafana/grafana:latest`
- **Port**: 3000
- **Purpose**: Metrics visualization
- **Default Login**: admin/admin
- **Profile**: `monitoring`

## 🔧 Configuration

### Environment Variables

#### Spring Boot Application
```yaml
SPRING_PROFILES_ACTIVE: docker
KAFKA_BOOTSTRAP_SERVERS: kafka:29092
TRAINING_KAFKA_BOOTSTRAP_SERVERS: kafka:29092
TRAINING_FEATURES_DEBUG_MODE: 'true'
TRAINING_FEATURES_WEB_INTERFACE_ENABLED: 'true'
LOGGING_LEVEL_COM_TRAINING_KAFKA: DEBUG
```

#### Kafka Broker
```yaml
KAFKA_BROKER_ID: 1
KAFKA_ZOOKEEPER_CONNECT: 'zookeeper:2181'
KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
KAFKA_AUTO_CREATE_TOPICS_ENABLE: 'true'
```

### Volume Mounts
- **Application Logs**: `app-logs:/var/log/kafka-training`
- **Kafka Data**: `kafka-data:/var/lib/kafka/data`
- **Zookeeper Data**: `zookeeper-data:/var/lib/zookeeper/data`
- **Prometheus Data**: `prometheus-data:/prometheus`
- **Grafana Data**: `grafana-data:/var/lib/grafana`

## 🔍 Monitoring & Troubleshooting

### Service Health Checks
```bash
# Check all services status
docker-compose ps

# View service logs
docker-compose logs -f kafka-training-app
docker-compose logs -f kafka
docker-compose logs -f zookeeper

# Check specific service health
docker-compose exec kafka-training-app curl -f http://localhost:8080/actuator/health
```

### Common Commands
```bash
# Start services
docker-compose up -d

# Stop services
docker-compose down

# Restart specific service
docker-compose restart kafka-training-app

# View real-time logs
docker-compose logs -f

# Scale services (if needed)
docker-compose up -d --scale kafka-training-app=2
```

### Troubleshooting

#### Service Won't Start
```bash
# Check logs for errors
docker-compose logs <service-name>

# Verify network connectivity
docker-compose exec kafka-training-app ping kafka

# Check port conflicts
lsof -i :8080
lsof -i :9092
```

#### Kafka Connection Issues
```bash
# Test Kafka connectivity from app container
docker-compose exec kafka-training-app curl -f http://kafka:29092

# Check Kafka broker status
docker-compose exec kafka kafka-broker-api-versions --bootstrap-server localhost:9092
```

#### Performance Issues
```bash
# Check resource usage
docker stats

# Increase memory limits in docker-compose.yml
services:
  kafka:
    mem_limit: 2g
  kafka-training-app:
    mem_limit: 1g
```

## 🔒 Security Considerations

### Production Deployment
```yaml
# Use specific image tags
image: confluentinc/cp-kafka:7.7.0  # Not 'latest'

# Set resource limits
deploy:
  resources:
    limits:
      memory: 2G
      cpus: '1.0'

# Use secrets for sensitive data
secrets:
  kafka_password:
    external: true
```

### Network Security
```yaml
# Restrict external access
ports:
  - "127.0.0.1:8080:8080"  # Only localhost access

# Use custom networks
networks:
  kafka-training-network:
    driver: bridge
    internal: true  # No external access
```

## 📊 Monitoring Setup

### Prometheus Configuration
Create `monitoring/prometheus.yml`:
```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'spring-boot'
    static_configs:
      - targets: ['kafka-training-app:8080']
    metrics_path: '/actuator/prometheus'
```

### Grafana Dashboards
1. **Spring Boot Dashboard**: JVM metrics, HTTP requests, custom metrics
2. **Kafka Dashboard**: Broker metrics, topic metrics, consumer lag
3. **Application Dashboard**: EventMart simulation metrics

### Accessing Monitoring
```bash
# Start with monitoring
docker-compose --profile monitoring up -d

# Access Grafana
open http://localhost:3000
# Login: admin/admin

# Access Prometheus
open http://localhost:9090
```

## 🚀 Production Deployment

### Environment-Specific Overrides
Create `docker-compose.prod.yml`:
```yaml
version: '3.8'
services:
  kafka-training-app:
    environment:
      SPRING_PROFILES_ACTIVE: prod
      LOGGING_LEVEL_ROOT: WARN
      MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE: health,info,metrics
```

Deploy with:
```bash
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

### Kubernetes Deployment
Convert to Kubernetes manifests:
```bash
# Install kompose
curl -L https://github.com/kubernetes/kompose/releases/download/v1.28.0/kompose-linux-amd64 -o kompose

# Convert docker-compose to k8s
kompose convert

# Deploy to Kubernetes
kubectl apply -f .
```

### Cloud Deployment
- **AWS ECS**: Use task definitions based on docker-compose
- **Google Cloud Run**: Deploy Spring Boot container directly
- **Azure Container Instances**: Use container groups
- **DigitalOcean App Platform**: Deploy from Docker Hub

## 📈 Scaling Considerations

### Horizontal Scaling
```yaml
# Scale Spring Boot application
docker-compose up -d --scale kafka-training-app=3

# Add load balancer
services:
  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
    depends_on:
      - kafka-training-app
```

### Kafka Scaling
```yaml
# Add more Kafka brokers
services:
  kafka-2:
    image: confluentinc/cp-kafka:7.7.0
    environment:
      KAFKA_BROKER_ID: 2
      # ... other config
```

## 🔄 CI/CD Integration

### GitHub Actions Example
```yaml
name: Deploy to Docker
on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Deploy with Docker Compose
        run: |
          docker-compose up -d
          docker-compose exec -T kafka-training-app curl -f http://localhost:8080/actuator/health
```

### Jenkins Pipeline
```groovy
pipeline {
    agent any
    stages {
        stage('Deploy') {
            steps {
                sh 'docker-compose up -d'
                sh 'docker-compose exec -T kafka-training-app curl -f http://localhost:8080/actuator/health'
            }
        }
    }
}
```

This Docker deployment provides a complete, production-ready environment for the Kafka Training application with comprehensive monitoring and scaling capabilities.
