# Kafka Training - Container-First Data Engineering

## Welcome to the Comprehensive Apache Kafka Training Program

This 8-day training program is designed for **data engineers** who want to master Apache Kafka using a **container-first** approach. You'll learn Kafka from fundamentals to production deployment, with hands-on experience using Docker, Kubernetes, and Spring Boot.

```mermaid
graph LR
    A[Day 1-2<br/>Foundation] --> B[Day 3-4<br/>Producers &<br/>Consumers]
    B --> C[Day 5-6<br/>Schema Registry &<br/>Streams]
    C --> D[Day 7-8<br/>Connect &<br/>Advanced]
    D --> E[Production<br/>Ready]

    style A fill:#ff6600,stroke:#333,stroke-width:2px,color:#fff
    style B fill:#ff8533,stroke:#333,stroke-width:2px,color:#fff
    style C fill:#ffaa66,stroke:#333,stroke-width:2px,color:#fff
    style D fill:#ffcc99,stroke:#333,stroke-width:2px,color:#333
    style E fill:#00cc66,stroke:#333,stroke-width:2px,color:#fff
```

## Key Features

<div class="card-grid">

<div class="metrics-card">
<h4>8-Day Curriculum</h4>
Progressive learning from Kafka basics to advanced production patterns with real-world exercises.
</div>

<div class="metrics-card">
<h4>90+ Tests</h4>
Comprehensive test suite using TestContainers for integration testing with real Kafka.
</div>

<div class="metrics-card">
<h4>Container-First</h4>
Learn with Docker and Kubernetes from day one. Production-ready container workflows.
</div>

<div class="metrics-card">
<h4>40+ REST APIs</h4>
Spring Boot REST API for every training module with comprehensive JSON endpoints.
</div>

<div class="metrics-card">
<h4>Production Ready</h4>
Kubernetes deployment manifests, monitoring setup, and production best practices.
</div>

<div class="metrics-card">
<h4>Hands-On Project</h4>
Build EventMart - a complete e-commerce event streaming platform throughout the course.
</div>

</div>

## Quick Start

Get up and running in 5 minutes with Docker Compose:

=== "Docker Compose"

    ```bash
    # Clone the repository
    git clone https://github.com/yourusername/kafka-training-java.git
    cd kafka-training-java

    # Start complete environment
    docker-compose up -d

    # Test the REST API
    curl http://localhost:8080/api/training/modules
    ```

=== "Spring Boot Dev"

    ```bash
    # Clone the repository
    git clone https://github.com/yourusername/kafka-training-java.git
    cd kafka-training-java

    # Run with development profile
    mvn spring-boot:run -Dspring-boot.run.profiles=dev

    # Test the REST API
    curl http://localhost:8080/api/training/modules
    ```

=== "TestContainers"

    ```bash
    # Clone the repository
    git clone https://github.com/yourusername/kafka-training-java.git
    cd kafka-training-java

    # Run comprehensive tests
    mvn test

    # All tests use TestContainers - no manual setup needed!
    ```

## What You'll Learn

### Phase 1: Foundation (Days 1-2)

- **Day 1**: Kafka architecture, brokers, topics, partitions, and AdminClient API
- **Day 2**: Data flow patterns, message ordering, and delivery guarantees

### Phase 2: Development (Days 3-4)

- **Day 3**: Producer patterns, configurations, idempotence, and transactions
- **Day 4**: Consumer groups, offset management, rebalancing, and at-least-once processing

### Phase 3: Schema & Streams (Days 5-6)

- **Day 5**: Avro schemas, Schema Registry, schema evolution, and compatibility
- **Day 6**: Kafka Streams API, stateless/stateful operations, and windowing

### Phase 4: Integration & Advanced (Days 7-8)

- **Day 7**: Kafka Connect, JDBC connectors, source/sink connectors, and data pipelines
- **Day 8**: Security (SSL/SASL), monitoring, performance tuning, and production patterns

## Training Environment

Access multiple services included in the training environment:

| Service | URL | Description |
|---------|-----|-------------|
| **Training REST API** | http://localhost:8080/api/training/* | Spring Boot API endpoints (use curl/Postman) |
| **Kafka UI** | http://localhost:8081 | Visual Kafka management (web browser) |
| **Kafka Broker** | localhost:9092 | Apache Kafka broker |
| **Schema Registry** | http://localhost:8082 | Confluent Schema Registry |
| **Kafka Connect** | http://localhost:8083 | Kafka Connect REST API |
| **PostgreSQL** | localhost:5432 | Demo database for connectors |
| **Prometheus** | http://localhost:9090 | Metrics collection (optional) |
| **Grafana** | http://localhost:3000 | Metrics visualization (optional) |

## Architecture Overview

```mermaid
graph TB
    subgraph "Client Applications"
        CLI[curl / HTTP Clients]
        API[REST API Clients]
    end

    subgraph "Spring Boot Application"
        APP[Kafka Training App<br/>:8080]
        SVC[Training Services]
        CTRL[REST Controllers]
    end

    subgraph "Kafka Ecosystem"
        KAFKA[Kafka Broker<br/>:9092]
        SR[Schema Registry<br/>:8082]
        KC[Kafka Connect<br/>:8083]
        ZK[Zookeeper<br/>:2181]
    end

    subgraph "Data Storage"
        PG[(PostgreSQL<br/>:5432)]
        VOL[Docker Volumes]
    end

    subgraph "Monitoring"
        PROM[Prometheus<br/>:9090]
        GRAF[Grafana<br/>:3000]
    end

    CLI --> APP
    API --> APP
    APP --> SVC
    SVC --> CTRL
    CTRL --> KAFKA
    CTRL --> SR
    CTRL --> KC
    KAFKA --> ZK
    KC --> PG
    KAFKA --> VOL
    APP --> PROM
    PROM --> GRAF

    style APP fill:#ff6600,stroke:#333,stroke-width:3px,color:#fff
    style KAFKA fill:#000,stroke:#ff6600,stroke-width:3px,color:#fff
    style SR fill:#0066cc,stroke:#333,stroke-width:2px,color:#fff
    style KC fill:#00cc66,stroke:#333,stroke-width:2px,color:#fff
```

## Container-First Approach

This training emphasizes a **container-first** methodology because:

1. **Production Parity** - Your development environment matches production exactly
2. **Portability** - Run anywhere: laptop, cloud, on-premises
3. **Isolation** - No dependency conflicts or "works on my machine" issues
4. **Scalability** - Easy to scale with Kubernetes
5. **Fast Onboarding** - New team members productive in minutes
6. **Real Testing** - TestContainers use real Kafka, not mocks

!!! tip "Why Containers for Data Engineers?"
    Modern data platforms run in containers. Learning Kafka with containers from day one prepares you for real-world data engineering roles where Docker and Kubernetes are standard tools.

## EventMart Progressive Project

Throughout the training, you'll build **EventMart** - a complete e-commerce event streaming platform:

- **User Management** - Registration, authentication, profile updates
- **Product Catalog** - Product creation, updates, inventory management
- **Order Processing** - Order placement, payment processing, fulfillment
- **Real-time Analytics** - Sales metrics, user behavior, inventory tracking
- **Event Sourcing** - Complete audit trail of all business events

```mermaid
graph LR
    U[User Events] --> EM[EventMart<br/>Platform]
    P[Product Events] --> EM
    O[Order Events] --> EM
    EM --> A[Analytics]
    EM --> I[Inventory]
    EM --> N[Notifications]

    style EM fill:#ff6600,stroke:#333,stroke-width:3px,color:#fff
    style U fill:#0066cc,stroke:#333,stroke-width:2px,color:#fff
    style P fill:#00cc66,stroke:#333,stroke-width:2px,color:#fff
    style O fill:#ffaa00,stroke:#333,stroke-width:2px,color:#fff
```

## Technology Stack

<div class="card-grid">

<div class="info-box">
<strong>Backend Framework</strong><br/>
Spring Boot 2.7.18 with Spring Kafka
</div>

<div class="info-box">
<strong>Kafka Distribution</strong><br/>
Apache Kafka 3.8.0<br/>
Confluent Platform 7.7.0
</div>

<div class="info-box">
<strong>Containerization</strong><br/>
Docker & Docker Compose<br/>
TestContainers for testing
</div>

<div class="info-box">
<strong>Orchestration</strong><br/>
Kubernetes with HPA<br/>
Helm charts (optional)
</div>

<div class="info-box">
<strong>Schema Management</strong><br/>
Apache Avro 1.12.0<br/>
Confluent Schema Registry
</div>

<div class="info-box">
<strong>Monitoring</strong><br/>
Prometheus & Grafana<br/>
Spring Boot Actuator
</div>

</div>

## Learning Outcomes

By completing this training, you will:

- [x] Master Apache Kafka architecture and core concepts
- [x] Build production-ready producers and consumers with Spring Boot
- [x] Implement real-time stream processing with Kafka Streams
- [x] Manage schemas with Avro and Schema Registry
- [x] Configure data integration pipelines with Kafka Connect
- [x] Deploy Kafka applications to Kubernetes
- [x] Implement monitoring and observability
- [x] Test with TestContainers for reliable integration testing
- [x] Follow container-first development workflows
- [x] Build a complete event-driven platform (EventMart)

## Prerequisites

!!! note "What You Need"
    - **Java 11+** - JDK installed and configured
    - **Docker** - Docker Desktop or Docker Engine
    - **Maven 3.8+** - For building the project
    - **Git** - For cloning the repository
    - **IDE** - IntelliJ IDEA, VS Code, or Eclipse (optional)

## Next Steps

Ready to begin? Choose your path:

<div class="card-grid">

<div class="success-box">
<strong>Complete Beginner?</strong><br/>
Start with <a href="getting-started/overview/">Getting Started Overview</a>
</div>

<div class="success-box">
<strong>Know Java?</strong><br/>
Jump to <a href="getting-started/quick-start/">5-Minute Quick Start</a>
</div>

<div class="success-box">
<strong>Know Kafka?</strong><br/>
Explore <a href="containers/why-containers/">Container-First Approach</a>
</div>

<div class="success-box">
<strong>Ready for Production?</strong><br/>
Check <a href="deployment/kubernetes-overview/">Kubernetes Deployment</a>
</div>

</div>

---

**Built for data engineers, by data engineers.** Start your container-first Kafka journey today!
