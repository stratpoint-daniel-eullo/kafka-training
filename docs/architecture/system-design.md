# System Design

## Architecture Overview

The Kafka Training application is a Spring Boot application demonstrating real-world Kafka patterns for data engineers.

```mermaid
graph TB
    subgraph "Client Layer"
        CLI[cURL / HTTP Client]
        REST[REST API Clients<br/>Postman, HTTPie]
    end

    subgraph "Application Layer"
        API[REST API<br/>TrainingController]
        S1[Day01FoundationService]
        S2[Day02DataFlowService]
        S3[Day03ProducerService]
        S4[Day04ConsumerService]
        S5[Day05SchemaService]
        S6[Day06StreamsService]
        S7[Day07ConnectService]
        S8[Day08AdvancedService]
    end

    subgraph "Kafka Layer"
        P[Producers]
        KAFKA[(Kafka Cluster)]
        C[Consumers]
        SR[Schema Registry]
        KC[Kafka Connect]
        KS[Kafka Streams]
    end

    subgraph "Data Layer"
        PG[(PostgreSQL)]
    end

    CLI --> API
    REST --> API
    API --> S1
    API --> S2
    API --> S3
    API --> S4
    API --> S5
    API --> S6
    API --> S7
    API --> S8

    S3 --> P
    S4 --> C
    P --> KAFKA
    KAFKA --> C
    S5 --> SR
    S6 --> KS
    S7 --> KC
    KC --> KAFKA
    KC --> PG
    KS --> KAFKA

    style KAFKA fill:#fff3e0
    style API fill:#e3f2fd
    style PG fill:#f3e5f5
```

## Component Responsibilities

### REST API Layer
- **TrainingController** - Exposes 40+ endpoints for training exercises
- Request validation
- Response formatting
- Error handling

### Service Layer
- **Day01-Day08 Services** - Implements Kafka patterns
- Producer operations
- Consumer management
- Schema Registry integration
- Streams processing
- Connect configuration

### Kafka Layer
- **Producers** - Send messages to topics
- **Consumers** - Process messages from topics
- **Schema Registry** - Manage Avro schemas
- **Kafka Connect** - Data integration (PostgreSQL ↔ Kafka)
- **Kafka Streams** - Real-time processing

### Data Layer
- **PostgreSQL** - EventMart database
- Order, customer, product tables
- Kafka Connect source/sink

## Data Flow

```mermaid
sequenceDiagram
    participant Client
    participant API
    participant Service
    participant Producer
    participant Kafka
    participant Consumer
    participant DB

    Client->>API: POST /day03/send-sync
    API->>Service: sendMessageSync()
    Service->>Producer: send(record)
    Producer->>Kafka: ProduceRequest
    Kafka-->>Producer: ProduceResponse
    Producer-->>Service: RecordMetadata
    Service-->>API: Success
    API-->>Client: 200 OK

    Kafka->>Consumer: Fetch
    Consumer->>DB: INSERT
    DB-->>Consumer: OK
```

## Next Steps

- [Technology Stack](tech-stack.md) - Technologies used
- [Data Flow](data-flow.md) - Detailed data flows
- [Container Architecture](container-architecture.md) - Docker/Kubernetes design
