# Technology Stack

## Core Technologies

### Java 11
- Modern Java features (var, streams, Optional)
- Long-term support (LTS)
- Production-ready

### Spring Boot 2.7.18
- Dependency injection
- Auto-configuration
- Embedded Tomcat server
- Actuator for monitoring
- Easy testing with `@SpringBootTest`

### Apache Kafka 3.8.0
- Distributed event streaming
- High throughput (millions of messages/sec)
- Durable storage
- Horizontal scalability

## Kafka Ecosystem

### Kafka Clients
- **Producer API** - Send messages
- **Consumer API** - Receive messages
- **Admin API** - Topic management
- **Streams API** - Stream processing

### Confluent Platform
- **Schema Registry** - Avro schema management
- **Kafka Connect** - Data integration
- **Kafka UI** - Visual management

## Data Layer

### PostgreSQL 15
- Relational database for EventMart
- JSONB support for flexible schemas
- Robust transaction support

## Container Platform

### Docker
- Container runtime
- Image building
- Local development

### Docker Compose
- Multi-container orchestration
- Development and full-stack environments

### Kubernetes
- Production orchestration
- Auto-scaling
- Self-healing
- Service discovery

## Testing

### TestContainers
- Real Kafka in Docker containers
- Integration testing
- Isolated test environments
- 90+ tests

### JUnit 5
- Modern testing framework
- Parameterized tests
- Lifecycle management

### Awaitility
- Async testing
- Polling assertions
- Consumer testing

## Build Tools

### Maven 3.9+
- Dependency management
- Build lifecycle
- Plugin ecosystem

## Dependencies

Key Maven dependencies:
```xml
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Kafka -->
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>
    
    <!-- Avro -->
    <dependency>
        <groupId>io.confluent</groupId>
        <artifactId>kafka-avro-serializer</artifactId>
    </dependency>
    
    <!-- Kafka Streams -->
    <dependency>
        <groupId>org.apache.kafka</groupId>
        <artifactId>kafka-streams</artifactId>
    </dependency>
    
    <!-- Testing -->
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>kafka</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

## Next Steps

- [System Design](system-design.md) - Overall architecture
- [Data Flow](data-flow.md) - Request/response flows
