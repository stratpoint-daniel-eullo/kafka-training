# Training Curriculum

Welcome to the 8-day Apache Kafka training curriculum. This comprehensive program takes you from Kafka fundamentals to production-ready implementations using Spring Boot and containers.

## Training Structure

```mermaid
graph TB
    subgraph "Phase 1: Foundation"
        D1[Day 1<br/>Kafka Fundamentals]
        D2[Day 2<br/>Data Flow Patterns]
    end

    subgraph "Phase 2: Development"
        D3[Day 3<br/>Producers]
        D4[Day 4<br/>Consumers]
    end

    subgraph "Phase 3: Advanced"
        D5[Day 5<br/>Schema Registry]
        D6[Day 6<br/>Streams Processing]
    end

    subgraph "Phase 4: Production"
        D7[Day 7<br/>Kafka Connect]
        D8[Day 8<br/>Advanced Topics]
    end

    D1 --> D2
    D2 --> D3
    D3 --> D4
    D4 --> D5
    D5 --> D6
    D6 --> D7
    D7 --> D8

    style D1 fill:#ff6600,stroke:#333,stroke-width:2px,color:#fff
    style D2 fill:#ff7519,stroke:#333,stroke-width:2px,color:#fff
    style D3 fill:#ff8533,stroke:#333,stroke-width:2px,color:#fff
    style D4 fill:#ff944d,stroke:#333,stroke-width:2px,color:#fff
    style D5 fill:#ffa366,stroke:#333,stroke-width:2px,color:#fff
    style D6 fill:#ffb380,stroke:#333,stroke-width:2px,color:#fff
    style D7 fill:#ffc299,stroke:#333,stroke-width:2px,color:#333
    style D8 fill:#ffd1b3,stroke:#333,stroke-width:2px,color:#333
```

## Curriculum Overview

### Phase 1: Foundation (Days 1-2)

<div class="card-grid">

<div class="kafka-container">
<h4><a href="day01-foundation/">Day 1: Kafka Fundamentals</a></h4>
<ul>
<li>Kafka architecture and core concepts</li>
<li>Brokers, topics, and partitions</li>
<li>AdminClient API operations</li>
<li>Cluster metadata management</li>
</ul>
<strong>Time:</strong> 3-4 hours
</div>

<div class="kafka-container">
<h4><a href="day02-dataflow/">Day 2: Data Flow Patterns</a></h4>
<ul>
<li>Event-driven architecture</li>
<li>Message ordering and partitioning</li>
<li>Delivery semantics</li>
<li>Offset management strategies</li>
</ul>
<strong>Time:</strong> 3-4 hours
</div>

</div>

### Phase 2: Development (Days 3-4)

<div class="card-grid">

<div class="kafka-container">
<h4><a href="day03-producers/">Day 3: Producer Development</a></h4>
<ul>
<li>Producer API deep dive</li>
<li>Synchronous vs asynchronous sending</li>
<li>Idempotent producers and transactions</li>
<li>Error handling and retries</li>
</ul>
<strong>Time:</strong> 3-4 hours
</div>

<div class="kafka-container">
<h4><a href="day04-consumers/">Day 4: Consumer Implementation</a></h4>
<ul>
<li>Consumer API fundamentals</li>
<li>Consumer groups and rebalancing</li>
<li>Offset management</li>
<li>Error handling patterns</li>
</ul>
<strong>Time:</strong> 3-4 hours
</div>

</div>

### Phase 3: Advanced (Days 5-6)

<div class="card-grid">

<div class="kafka-container">
<h4><a href="day05-schema-registry/">Day 5: Schema Registry</a></h4>
<ul>
<li>Apache Avro serialization</li>
<li>Confluent Schema Registry</li>
<li>Schema evolution and compatibility</li>
<li>Spring Boot integration</li>
</ul>
<strong>Time:</strong> 3-4 hours
</div>

<div class="kafka-container">
<h4><a href="day06-streams/">Day 6: Streams Processing</a></h4>
<ul>
<li>Kafka Streams API</li>
<li>Stateless and stateful operations</li>
<li>Windowing and aggregations</li>
<li>Stream topologies</li>
</ul>
<strong>Time:</strong> 3-4 hours
</div>

</div>

### Phase 4: Production (Days 7-8)

<div class="card-grid">

<div class="kafka-container">
<h4><a href="day07-connect/">Day 7: Kafka Connect</a></h4>
<ul>
<li>Kafka Connect architecture</li>
<li>Source and sink connectors</li>
<li>JDBC connector configuration</li>
<li>Custom connector development</li>
</ul>
<strong>Time:</strong> 3-4 hours
</div>

<div class="kafka-container">
<h4><a href="day08-advanced/">Day 8: Advanced Topics</a></h4>
<ul>
<li>Security (SSL/SASL)</li>
<li>Monitoring and observability</li>
<li>Performance tuning</li>
<li>Production best practices</li>
</ul>
<strong>Time:</strong> 3-4 hours
</div>

</div>

## Daily Learning Pattern

Each day follows a structured approach:

### 1. Theory (45-60 minutes)

- Core concepts introduction
- Architecture diagrams
- Best practices overview

### 2. Hands-On Examples (60-90 minutes)

- Live coding demonstrations
- Spring Boot implementations
- Docker container exercises

### 3. Practice Exercises (45-60 minutes)

- Independent coding tasks
- Problem-solving challenges
- Code review and discussion

### 4. EventMart Integration (30 minutes)

- Apply concepts to real project
- Build progressive features
- Integration testing

## REST API Endpoints by Day

Each training day has corresponding REST API endpoints:

| Day | Endpoint | Description |
|-----|----------|-------------|
| Day 1 | `POST /api/training/day01/demo` | Foundation demonstration |
| Day 2 | `GET /api/training/day02/concepts` | Data flow concepts |
| Day 3 | `POST /api/training/day03/demo` | Producer patterns demo |
| Day 4 | `POST /api/training/day04/demo` | Consumer groups demo |
| Day 5 | `POST /api/training/day05/schema` | Schema Registry demo |
| Day 6 | `POST /api/training/day06/stream` | Streams processing demo |
| Day 7 | `POST /api/training/day07/connect` | Kafka Connect demo |
| Day 8 | `GET /api/training/day08/metrics` | Advanced monitoring |

## Skills Progression

Track your learning progress:

<div class="card-grid">

<div class="success-box">
<strong>Days 1-2: Beginner</strong><br/>
Understanding Kafka basics, topics, and partitions
</div>

<div class="success-box">
<strong>Days 3-4: Intermediate</strong><br/>
Building producers and consumers with Spring Boot
</div>

<div class="success-box">
<strong>Days 5-6: Advanced</strong><br/>
Schema management and stream processing
</div>

<div class="success-box">
<strong>Days 7-8: Expert</strong><br/>
Integration patterns and production deployment
</div>

</div>

## Learning Resources

### Documentation

- **Day Guides** - Comprehensive daily documentation
- **API Reference** - Complete REST API documentation
- **Code Examples** - 90+ integration tests with TestContainers
- **EventMart Project** - Progressive real-world application

### Container Resources

- **Docker Compose** - Complete development environment
- **Kubernetes Manifests** - Production deployment configs
- **TestContainers** - Integration testing framework
- **Monitoring Stack** - Prometheus and Grafana setup

## Recommended Schedule

### Full-Time Track (8 Days)

- **Daily Time**: 3-4 hours
- **Schedule**: One day per training day
- **Best For**: Bootcamps, dedicated learning periods

### Part-Time Track (4 Weeks)

- **Weekly Time**: 6-8 hours
- **Schedule**: 2 days per week
- **Best For**: Working professionals

### Self-Paced Track

- **Flexible Schedule**: Your own pace
- **Recommended**: 2-4 hours per session
- **Best For**: Independent learners

## Next Steps

<div class="card-grid">

<div class="info-box">
<strong>Ready to Start?</strong><br/>
Begin with <a href="day01-foundation/">Day 1: Foundation</a>
</div>

<div class="info-box">
<strong>Need Prerequisites?</strong><br/>
Check <a href="../getting-started/prerequisites/">Prerequisites Guide</a>
</div>

<div class="info-box">
<strong>Want Quick Setup?</strong><br/>
Follow <a href="../getting-started/quick-start/">5-Minute Quick Start</a>
</div>

<div class="info-box">
<strong>Explore API?</strong><br/>
Review <a href="../api/training-endpoints/">API Reference</a>
</div>

</div>

---

Start your Kafka journey with [Day 1: Kafka Fundamentals and Foundation](day01-foundation/)
