# Technology Stack

## Core Technologies

### Java 21
- Modern Java features (records, pattern matching, virtual threads)
- Long-term support (LTS)
- Production-ready

### Spring Boot 3.3.4
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

### Confluent Platform 7.7.0
- **Schema Registry** - Avro schema management
- **Kafka Connect** - Data integration
- **Kafka UI** - Visual management

## Track-Specific Technologies

This training supports two learning tracks with different technology stacks:

=== "Python Data Engineers"

    ### Python 3.8+
    - Modern Python features (async/await, type hints)
    - Production-ready for data pipelines
    - Wide ecosystem for data engineering

    ### Kafka Python Clients
    - **confluent-kafka** - Production-grade C-based client
    - **kafka-python** - Pure Python implementation
    - Both support all Kafka features

    ### Stream Processing
    - **Faust-streaming** - Python stream processing library
    - RocksDB backend for stateful processing
    - Async/await native support

    ### Schema Management
    - **avro-python3** - Apache Avro for Python
    - Schema Registry integration via confluent-kafka
    - Full schema evolution support

    ### Python Dependencies
    ```bash
    # Install Python Kafka libraries
    pip install confluent-kafka[avro]
    pip install kafka-python
    pip install faust-streaming
    pip install avro-python3
    ```

=== "Java Developers"

    ### Java 21
    - Modern Java features (records, pattern matching, virtual threads)
    - Long-term support (LTS)
    - Production-ready

    ### Spring Boot 3.3.4
    - Dependency injection and auto-configuration
    - Spring Kafka integration
    - Spring Boot Actuator for monitoring
    - Easy testing with `@SpringBootTest`

    ### Spring Kafka
    - `KafkaTemplate` abstraction for producers
    - `@KafkaListener` for consumers
    - Automatic configuration and setup
    - Spring-native patterns

    ### Kafka Streams
    - Java-native stream processing
    - Spring Cloud Stream integration
    - Stateful and stateless operations
    - RocksDB state stores

    ### Maven Dependencies
    ```xml
    <!-- Key dependencies -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>
    <dependency>
        <groupId>org.apache.kafka</groupId>
        <artifactId>kafka-clients</artifactId>
        <version>3.8.0</version>
    </dependency>
    <dependency>
        <groupId>io.confluent</groupId>
        <artifactId>kafka-avro-serializer</artifactId>
        <version>7.7.0</version>
    </dependency>
    ```

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
