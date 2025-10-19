# System Architecture

## Overview

The Kafka Training platform is a container-first environment designed to teach Apache Kafka concepts through hands-on practice with pure APIs and CLI tools.

!!! tip "Platform Architecture"
    The architecture supports **both learning tracks**: pure Kafka fundamentals (recommended for data engineers) and Spring Boot integration (optional for Java developers).

```mermaid
graph TB
    subgraph "Client Layer"
        HTTP[HTTP Clients<br/>curl, Postman]
        API[REST API Clients]
        CLI[CLI Tools]
    end

    subgraph "Application Layer"
        SPRING[Spring Boot App<br/>Port 8080]
        CTRL[REST Controllers]
        SVC[Training Services]
        CONFIG[Configuration<br/>Profiles]
    end

    subgraph "Kafka Ecosystem"
        KAFKA[Kafka Broker<br/>Port 9092]
        SR[Schema Registry<br/>Port 8082]
        KC[Kafka Connect<br/>Port 8083]
        ZK[Zookeeper<br/>Port 2181]
    end

    subgraph "Data Layer"
        PG[(PostgreSQL<br/>Port 5432)]
        VOL[Docker Volumes]
    end

    subgraph "Monitoring Layer"
        ACT[Spring Boot<br/>Actuator]
        PROM[Prometheus<br/>Port 9090]
        GRAF[Grafana<br/>Port 3000]
        UI[Kafka UI<br/>Port 8081]
    end

    HTTP --> SPRING
    API --> CTRL
    CLI --> SPRING
    CTRL --> SVC
    SVC --> CONFIG

    SPRING --> KAFKA
    SPRING --> SR
    SPRING --> KC
    KAFKA --> ZK
    KC --> PG
    KAFKA --> VOL

    SPRING --> ACT
    ACT --> PROM
    PROM --> GRAF
    UI --> KAFKA

    style KAFKA fill:#000,stroke:#ff6600,stroke-width:3px,color:#fff
    style SPRING fill:#ff6600,stroke:#333,stroke-width:3px,color:#fff
    style SR fill:#0066cc,stroke:#333,stroke-width:2px,color:#fff
    style KC fill:#00cc66,stroke:#333,stroke-width:2px,color:#fff
```

## Architecture Principles

### 1. Container-First Design

All components run in Docker containers for:

- **Development-Production Parity** - Same environment everywhere
- **Easy Onboarding** - `docker-compose up` and you're ready
- **Isolated Dependencies** - No version conflicts
- **Testability** - TestContainers for integration tests

### 2. Event-Driven Architecture

Built on Kafka event streaming:

- **Event Sourcing** - All state changes as events
- **Asynchronous Communication** - Decoupled services
- **Real-time Processing** - Kafka Streams for analytics
- **Scalable** - Partition-based parallelism

### 3. Microservices-Ready

Designed for distributed systems:

- **Independent Deployment** - Each service containerized
- **API-First** - REST endpoints for all operations
- **Resilient** - Circuit breakers and retries
- **Observable** - Comprehensive monitoring

### 4. Cloud-Native

Production-ready from day one:

- **Kubernetes Deployment** - HPA, health checks, secrets
- **Horizontal Scaling** - Stateless design
- **Health Checks** - Startup, liveness, readiness probes
- **Configuration Management** - Spring profiles, ConfigMaps

## Component Architecture

### Spring Boot Application

```mermaid
graph TB
    subgraph "Presentation Layer"
        API_CTRL[Training API<br/>/api/training/*]
        EVENTMART_CTRL[EventMart API<br/>/api/training/eventmart/*]
        ACTUATOR_CTRL[Actuator API<br/>/actuator/*]
    end

    subgraph "Service Layer"
        D01[Day01<br/>Foundation]
        D02[Day02<br/>DataFlow]
        D03[Day03<br/>Producer]
        D04[Day04<br/>Consumer]
        D05[Day05<br/>Schema]
        D06[Day06<br/>Streams]
        D07[Day07<br/>Connect]
        D08[Day08<br/>Advanced]
        EM[EventMart<br/>Service]
    end

    subgraph "Kafka Integration"
        PRODUCER[Kafka<br/>Producer]
        CONSUMER[Kafka<br/>Consumer]
        STREAMS[Kafka<br/>Streams]
        ADMIN[Admin<br/>Client]
    end

    subgraph "Configuration"
        PROPS[Application<br/>Properties]
        PROFILES[Spring<br/>Profiles]
        KAFKA_CFG[Kafka<br/>Config]
    end

    API_CTRL --> D01
    API_CTRL --> D02
    API_CTRL --> D03
    API_CTRL --> D04
    EVENTMART_CTRL --> EM

    D03 --> PRODUCER
    D04 --> CONSUMER
    D06 --> STREAMS
    D01 --> ADMIN

    PRODUCER --> KAFKA_CFG
    CONSUMER --> KAFKA_CFG
    STREAMS --> KAFKA_CFG

    KAFKA_CFG --> PROPS
    PROPS --> PROFILES

    style API_CTRL fill:#ff6600,stroke:#333,stroke-width:2px,color:#fff
    style PRODUCER fill:#00cc66,stroke:#333,stroke-width:2px,color:#fff
    style CONSUMER fill:#0066cc,stroke:#333,stroke-width:2px,color:#fff
```

### Kafka Cluster Architecture

```mermaid
graph TB
    subgraph "Kafka Cluster"
        subgraph "Broker 1"
            B1[Broker 1<br/>localhost:9092]
            P0_L[Partition 0<br/>Leader]
            P1_F[Partition 1<br/>Follower]
        end
    end

    subgraph "Topics"
        T1[eventmart-users<br/>6 partitions]
        T2[eventmart-products<br/>3 partitions]
        T3[eventmart-orders<br/>6 partitions]
        T4[training-topics<br/>3 partitions]
    end

    subgraph "Clients"
        PROD[Producers<br/>Training App]
        CONS[Consumers<br/>Training App]
        STREAM[Streams<br/>Analytics]
    end

    PROD --> T1
    PROD --> T2
    PROD --> T3
    T1 --> CONS
    T2 --> STREAM
    T3 --> CONS

    T1 --> P0_L
    T2 --> P0_L
    T3 --> P1_F

    style B1 fill:#000,stroke:#ff6600,stroke-width:3px,color:#fff
    style PROD fill:#ff6600,stroke:#333,stroke-width:2px,color:#fff
    style CONS fill:#0066cc,stroke:#333,stroke-width:2px,color:#fff
    style STREAM fill:#00cc66,stroke:#333,stroke-width:2px,color:#fff
```

## Data Flow Patterns

### EventMart Data Flow

```mermaid
sequenceDiagram
    participant CLIENT as REST Client
    participant API as REST API
    participant SVC as EventMart Service
    participant KAFKA as Kafka Broker
    participant STREAMS as Streams Processor
    participant DB as PostgreSQL

    CLIENT->>API: POST /simulate/user
    API->>SVC: createUser()
    SVC->>KAFKA: Publish UserCreated Event
    KAFKA-->>SVC: ACK
    SVC-->>API: Success Response
    API-->>CLIENT: 200 OK (JSON)

    KAFKA->>STREAMS: Consume UserCreated
    STREAMS->>STREAMS: Process & Enrich
    STREAMS->>KAFKA: Publish UserAnalytics

    KAFKA->>DB: Sink Connector
    DB->>DB: Store User Data
```

### Training Module Flow

```mermaid
sequenceDiagram
    participant CLIENT as Client
    participant CTRL as Training Controller
    participant SVC as Training Service
    participant KAFKA as Kafka
    participant ADMIN as AdminClient

    CLIENT->>CTRL: POST /api/training/day01/demo
    CTRL->>SVC: demonstrateFoundation()
    SVC->>ADMIN: listTopics()
    ADMIN->>KAFKA: Request Topics
    KAFKA-->>ADMIN: Topic List
    ADMIN-->>SVC: Topics
    SVC->>ADMIN: describeCluster()
    ADMIN->>KAFKA: Request Metadata
    KAFKA-->>ADMIN: Cluster Metadata
    ADMIN-->>SVC: Metadata
    SVC-->>CTRL: Demo Results
    CTRL-->>CLIENT: JSON Response
```

## Container Network Architecture

```mermaid
graph TB
    subgraph "Host Network"
        HOST[Host Machine<br/>macOS/Linux/Windows]
    end

    subgraph "Docker Network: kafka-training-network"
        subgraph "Core Services"
            ZK[zookeeper:2181]
            KAFKA[kafka:29092<br/>localhost:9092]
            SR[schema-registry:8082]
        end

        subgraph "Application Services"
            APP[kafka-training-app:8080]
            KC[kafka-connect:8083]
            UI[kafka-ui:8081]
        end

        subgraph "Data Services"
            PG[postgres:5432]
        end

        subgraph "Monitoring Services"
            PROM[prometheus:9090]
            GRAF[grafana:3000]
        end
    end

    HOST -->|Port 8080| APP
    HOST -->|Port 9092| KAFKA
    HOST -->|Port 8081| UI

    APP --> KAFKA
    APP --> SR
    APP --> KC
    KC --> KAFKA
    KC --> PG
    UI --> KAFKA
    PROM --> APP
    GRAF --> PROM

    style KAFKA fill:#000,stroke:#ff6600,stroke-width:3px,color:#fff
    style APP fill:#ff6600,stroke:#333,stroke-width:2px,color:#fff
```

## Technology Stack

### Backend

| Component | Technology | Version | Purpose |
|-----------|-----------|---------|---------|
| **Framework** | Spring Boot | 2.7.18 | Application framework |
| **Kafka Client** | Apache Kafka | 3.8.0 | Event streaming |
| **Schema Registry** | Confluent | 7.7.0 | Schema management |
| **Serialization** | Apache Avro | 1.12.0 | Data serialization |
| **Database** | PostgreSQL | 15 | Data persistence |
| **Build Tool** | Maven | 3.8+ | Build automation |

### Container Platform

| Component | Technology | Purpose |
|-----------|-----------|---------|
| **Runtime** | Docker | Container runtime |
| **Orchestration** | Docker Compose | Local orchestration |
| **Testing** | TestContainers | Integration testing |
| **Production** | Kubernetes | Production orchestration |
| **Monitoring** | Prometheus | Metrics collection |
| **Visualization** | Grafana | Metrics dashboards |

### Development Tools

| Tool | Purpose |
|------|---------|
| **Spring Boot DevTools** | Hot reload |
| **Spring Boot Actuator** | Health checks & metrics |
| **Kafka UI** | Visual management |
| **Maven** | Build & dependency management |

## Deployment Architectures

### Local Development

```mermaid
graph LR
    DEV[Developer<br/>Laptop] --> DC[Docker Compose<br/>All Services]
    DC --> VOL[Local Volumes<br/>Data Persistence]

    style DEV fill:#ff6600,stroke:#333,stroke-width:2px,color:#fff
    style DC fill:#0066cc,stroke:#333,stroke-width:2px,color:#fff
```

### Kubernetes Production

```mermaid
graph TB
    subgraph "Kubernetes Cluster"
        subgraph "Namespace: data-engineering"
            DEPLOY[Deployment<br/>3 Replicas]
            SVC[Service<br/>ClusterIP]
            HPA[HPA<br/>3-10 Pods]
        end

        subgraph "Kafka Cluster"
            KAFKA_K8S[Strimzi Kafka<br/>Operator]
        end

        subgraph "Configuration"
            CM[ConfigMap<br/>App Config]
            SECRET[Secret<br/>Credentials]
        end

        subgraph "Monitoring"
            PROM_K8S[Prometheus<br/>Operator]
            GRAF_K8S[Grafana]
        end
    end

    DEPLOY --> SVC
    HPA --> DEPLOY
    DEPLOY --> CM
    DEPLOY --> SECRET
    DEPLOY --> KAFKA_K8S
    PROM_K8S --> DEPLOY
    GRAF_K8S --> PROM_K8S

    style DEPLOY fill:#ff6600,stroke:#333,stroke-width:3px,color:#fff
    style KAFKA_K8S fill:#000,stroke:#ff6600,stroke-width:3px,color:#fff
```

## Security Architecture

### Authentication & Authorization

```mermaid
graph LR
    subgraph "Security Layers"
        API[API Gateway<br/>Future]
        AUTH[Spring Security<br/>Future]
        KAFKA_SEC[Kafka SASL/SSL]
        SR_SEC[Schema Registry<br/>Authentication]
    end

    CLIENT[Client] --> API
    API --> AUTH
    AUTH --> KAFKA_SEC
    KAFKA_SEC --> SR_SEC

    style KAFKA_SEC fill:#cc0000,stroke:#333,stroke-width:2px,color:#fff
```

!!! note "Current Security"
    The training environment runs without authentication for simplicity. For production:

    - Enable Spring Security
    - Configure Kafka SSL/SASL
    - Use Kubernetes Secrets
    - Implement API authentication

## Scalability & Performance

### Horizontal Scaling

```mermaid
graph TB
    LB[Load Balancer<br/>Ingress]

    subgraph "Application Pods"
        P1[Pod 1]
        P2[Pod 2]
        P3[Pod 3]
        PN[Pod N...]
    end

    subgraph "Kafka Cluster"
        K1[Broker 1]
        K2[Broker 2]
        K3[Broker 3]
    end

    LB --> P1
    LB --> P2
    LB --> P3
    LB --> PN

    P1 --> K1
    P2 --> K2
    P3 --> K3

    style LB fill:#0066cc,stroke:#333,stroke-width:2px,color:#fff
    style K1 fill:#000,stroke:#ff6600,stroke-width:2px,color:#fff
    style K2 fill:#000,stroke:#ff6600,stroke-width:2px,color:#fff
    style K3 fill:#000,stroke:#ff6600,stroke-width:2px,color:#fff
```

### Performance Characteristics

| Metric | Development | Production |
|--------|-------------|------------|
| **Throughput** | 10K msg/sec | 100K+ msg/sec |
| **Latency** | <100ms | <10ms |
| **Consumers** | 1-3 | Auto-scaled |
| **Partitions** | 3-6 | 12+ |
| **Replication** | 1 | 3 |

## Monitoring & Observability

### Metrics Collection

```mermaid
graph LR
    subgraph "Application"
        APP[Spring Boot<br/>App]
        ACT[Actuator<br/>Endpoints]
    end

    subgraph "Kafka"
        KAFKA_M[Kafka<br/>JMX Metrics]
    end

    subgraph "Collection"
        PROM[Prometheus]
    end

    subgraph "Visualization"
        GRAF[Grafana<br/>Dashboards]
        ALERT[Alertmanager]
    end

    APP --> ACT
    ACT --> PROM
    KAFKA_M --> PROM
    PROM --> GRAF
    PROM --> ALERT

    style PROM fill:#ff6600,stroke:#333,stroke-width:2px,color:#fff
```

### Key Metrics

- **Application Metrics**: Request rate, error rate, latency
- **Kafka Metrics**: Message rate, consumer lag, partition distribution
- **JVM Metrics**: Memory usage, GC pauses, thread count
- **Container Metrics**: CPU, memory, network I/O

## Related Topics

<div class="card-grid">

<div class="info-box">
<a href="system-design/"><strong>System Design</strong></a><br/>
Detailed design decisions and patterns
</div>

<div class="info-box">
<a href="tech-stack/"><strong>Technology Stack</strong></a><br/>
Deep dive into technologies used
</div>

<div class="info-box">
<a href="data-flow/"><strong>Data Flow</strong></a><br/>
Event flow and processing patterns
</div>

<div class="info-box">
<a href="security/"><strong>Security</strong></a><br/>
Security architecture and best practices
</div>

</div>

---

Explore detailed architecture topics or proceed to [Deployment Guide](../deployment/deployment-guide/)
