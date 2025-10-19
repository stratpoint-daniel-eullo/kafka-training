# Docker Compose Setup

!!! info "Training Track Information"
    **Applicable to**: Platform-agnostic track, Spring Boot track
    Docker Compose works with any Kafka client (Java, Python, Node.js, Go) and is framework-independent.

## Overview

Docker Compose provides a declarative way to define and run multi-container applications. For Kafka development, it orchestrates Kafka brokers, ZooKeeper, Schema Registry, and other infrastructure components.

## Core Concepts

### Services

Each service in `docker-compose.yml` represents a container:

```yaml
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.7.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181

  kafka:
    image: confluentinc/cp-kafka:7.7.0
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
```

### Networks

Docker Compose creates a default network for service communication:

```yaml
networks:
  kafka-training-network:
    driver: bridge
```

Services communicate using service names as hostnames (e.g., `kafka:29092`).

### Volumes

Persist data across container restarts:

```yaml
volumes:
  kafka-data:/var/lib/kafka/data
  postgres-data:/var/lib/postgresql/data
```

## Development Configuration

The project includes `docker-compose-dev.yml` for local development:

```yaml
version: '3.8'

services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.7.0
    container_name: kafka-training-zookeeper
    ports:
      - "2181:2181"
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000

  kafka:
    image: confluentinc/cp-kafka:7.7.0
    container_name: kafka-training-kafka
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
      - "9093:9093"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: 'true'

  schema-registry:
    image: confluentinc/cp-schema-registry:7.7.0
    container_name: kafka-training-schema-registry
    depends_on:
      - kafka
    ports:
      - "8082:8081"
    environment:
      SCHEMA_REGISTRY_HOST_NAME: schema-registry
      SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS: kafka:29092

  postgres:
    image: postgres:15-alpine
    container_name: kafka-training-postgres
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: eventmart
      POSTGRES_USER: eventmart_user
      POSTGRES_PASSWORD: eventmart_pass
    volumes:
      - postgres-data:/var/lib/postgresql/data

volumes:
  postgres-data:

networks:
  default:
    name: kafka-training-network
```

## Basic Commands

### Start Services

```bash
# Start all services in background
docker-compose -f docker-compose-dev.yml up -d

# Start specific service
docker-compose -f docker-compose-dev.yml up -d kafka

# View logs while starting (foreground)
docker-compose -f docker-compose-dev.yml up
```

### Stop Services

```bash
# Stop all services
docker-compose -f docker-compose-dev.yml down

# Stop and remove volumes (clean state)
docker-compose -f docker-compose-dev.yml down -v

# Stop specific service
docker-compose -f docker-compose-dev.yml stop kafka
```

### View Status

```bash
# List running services
docker-compose -f docker-compose-dev.yml ps

# View logs
docker-compose -f docker-compose-dev.yml logs -f

# View logs for specific service
docker-compose -f docker-compose-dev.yml logs -f kafka
```

## Testing Kafka Setup

### Verify Kafka is Running

```bash
# List topics
docker exec kafka-training-kafka \
  kafka-topics --bootstrap-server localhost:9092 --list

# Create test topic
docker exec kafka-training-kafka \
  kafka-topics --bootstrap-server localhost:9092 \
  --create --topic test --partitions 3 --replication-factor 1

# Produce messages
docker exec -it kafka-training-kafka \
  kafka-console-producer --bootstrap-server localhost:9092 --topic test

# Consume messages
docker exec kafka-training-kafka \
  kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic test --from-beginning
```

### Test Schema Registry

```bash
# Check Schema Registry health
curl http://localhost:8082/subjects

# Register test schema
curl -X POST http://localhost:8082/subjects/test-value/versions \
  -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  -d '{"schema": "{\"type\":\"string\"}"}'
```

### Test PostgreSQL

```bash
# Connect to PostgreSQL
docker exec -it kafka-training-postgres \
  psql -U eventmart_user -d eventmart

# Inside psql
\dt              # List tables
\q               # Quit
```

## Environment Variables

Create `.env` file for environment-specific configuration:

```bash
# .env
KAFKA_VERSION=7.7.0
POSTGRES_PASSWORD=secure_password
SCHEMA_REGISTRY_PORT=8082
```

Reference in `docker-compose.yml`:

```yaml
kafka:
  image: confluentinc/cp-kafka:${KAFKA_VERSION}

postgres:
  environment:
    POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
```

Load with:

```bash
docker-compose --env-file .env up -d
```

## Production Configuration

For production, use separate `docker-compose.prod.yml`:

```yaml
version: '3.8'

services:
  kafka:
    image: confluentinc/cp-kafka:7.7.0
    restart: unless-stopped
    deploy:
      resources:
        limits:
          memory: 4G
          cpus: '2'
        reservations:
          memory: 2G
          cpus: '1'
    environment:
      KAFKA_HEAP_OPTS: "-Xms2g -Xmx4g"
      KAFKA_JMX_PORT: 9999
    volumes:
      - kafka-data:/var/lib/kafka/data
    healthcheck:
      test: ["CMD", "kafka-broker-api-versions", "--bootstrap-server", "localhost:9092"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  kafka-data:
```

## Troubleshooting

### Kafka Won't Start

```bash
# Check ZooKeeper is running first
docker-compose ps

# View Kafka logs
docker-compose logs kafka

# Restart in correct order
docker-compose down
docker-compose up -d zookeeper
sleep 10
docker-compose up -d kafka
```

### Port Conflicts

```bash
# Find process using port
lsof -i :9092

# Change port in docker-compose.yml
ports:
  - "19092:9092"  # Use different host port
```

### Out of Disk Space

```bash
# Check Docker disk usage
docker system df

# Clean up
docker system prune -a
docker volume prune
```

## Next Steps

- [Development Workflow](development-workflow.md) - Local development patterns
- [Testcontainers](testcontainers.md) - Integration testing with containers
- [Best Practices](best-practices.md) - Production-ready container practices

## Resources

- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Confluent Docker Images](https://docs.confluent.io/platform/current/installation/docker/image-reference.html)
- [Docker Networking](https://docs.docker.com/network/)

---

## Spring Boot Integration (Optional)

This section is specific to the Spring Boot training track.

### Application Container

Add Spring Boot application to docker-compose:

```yaml
services:
  app:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: kafka-training-app
    depends_on:
      - kafka
      - postgres
      - schema-registry
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:29092
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/eventmart
      SPRING_DATASOURCE_USERNAME: eventmart_user
      SPRING_DATASOURCE_PASSWORD: eventmart_pass
    networks:
      - kafka-training-network
```

### Spring Boot Profiles

Configure `application-docker.properties`:

```properties
# Kafka Configuration
spring.kafka.bootstrap-servers=kafka:29092
spring.kafka.properties.schema.registry.url=http://schema-registry:8081

# Database Configuration
spring.datasource.url=jdbc:postgresql://postgres:5432/eventmart
spring.datasource.username=eventmart_user
spring.datasource.password=eventmart_pass

# Logging
logging.level.com.training.kafka=INFO
```

### Build and Run

```bash
# Build Spring Boot image
docker-compose build app

# Start everything including app
docker-compose up -d

# View app logs
docker-compose logs -f app

# Test REST API
curl http://localhost:8080/api/training/modules
```
