# Capstone Project

## Choose Your Learning Track

The capstone project is your opportunity to demonstrate mastery of Apache Kafka by building a complete, production-ready event streaming platform. Choose the capstone guide that matches your learning track.

## Available Capstone Projects

<div class="card-grid">

<div class="capstone-card">
<h3>Real-Time E-Commerce Analytics Platform</h3>
<p><strong>Track:</strong> Python Data Engineers</p>
<p><strong>Focus:</strong> Data pipelines, stream processing, real-time analytics</p>

<h4>What You'll Build</h4>
<ul>
    <li>Event producers using confluent-kafka with Avro serialization</li>
    <li>Real-time aggregations with Faust-streaming</li>
    <li>Data integration using Kafka Connect (PostgreSQL sink)</li>
    <li>Dead letter queue (DLQ) error handling patterns</li>
    <li>Monitoring with Prometheus and Grafana</li>
    <li>Production-ready Python services in Docker</li>
</ul>

<h4>Technology Stack</h4>
<ul>
    <li>Python 3.8+</li>
    <li>confluent-kafka[avro]</li>
    <li>Faust-streaming</li>
    <li>Apache Avro schemas</li>
    <li>Kafka Connect JDBC Sink</li>
    <li>PostgreSQL</li>
    <li>Docker Compose</li>
</ul>

<p><a href="capstone-guide-python.md" class="btn-primary">Python Capstone Guide</a></p>
</div>

<div class="capstone-card">
<h3>EventMart E-Commerce Platform</h3>
<p><strong>Track:</strong> Java Developers</p>
<p><strong>Focus:</strong> Microservices, Spring Boot, event-driven architecture</p>

<h4>What You'll Build</h4>
<ul>
    <li>Spring Boot microservices with Spring Kafka</li>
    <li>REST API endpoints for event management</li>
    <li>Kafka Streams for real-time processing</li>
    <li>Web UI for monitoring and management</li>
    <li>Avro schema integration with Spring Boot</li>
    <li>Production-ready Java services</li>
</ul>

<h4>Technology Stack</h4>
<ul>
    <li>Java 21</li>
    <li>Spring Boot 3.3.4</li>
    <li>Spring Kafka</li>
    <li>Kafka Streams</li>
    <li>Apache Avro with Spring Boot</li>
    <li>PostgreSQL with Spring Data JPA</li>
    <li>Docker and Kubernetes</li>
</ul>

<p><a href="capstone-guide-java.md" class="btn-primary">Java Capstone Guide</a></p>
</div>

</div>

## Project Comparison

| Aspect | Python Data Engineers | Java Developers |
|--------|----------------------|-----------------|
| **Project Name** | Real-Time E-Commerce Analytics Platform | EventMart E-Commerce Platform |
| **Primary Language** | Python | Java |
| **Development Style** | CLI-based, script-driven | Web UI, REST API driven |
| **Kafka Integration** | confluent-kafka (pure Kafka client) | Spring Kafka (framework abstraction) |
| **Stream Processing** | Faust-streaming | Kafka Streams |
| **Entry Point** | Python scripts, Docker containers | Spring Boot application, Web browser |
| **Demo Format** | Terminal demonstrations, Kafka UI | Web interface, REST API calls |
| **Architecture** | Data pipeline focused | Microservices focused |
| **Monitoring** | Prometheus metrics, Grafana dashboards | Spring Boot Actuator, custom dashboards |
| **Deployment** | Docker Compose | Docker Compose + Kubernetes |

## Common Requirements (Both Tracks)

Regardless of which track you choose, your capstone must demonstrate:

### Core Kafka Concepts

- Topic design and partition strategy
- Producer configuration and patterns
- Consumer groups and offset management
- Schema Registry with Avro schemas
- Kafka Connect integration
- Security configuration (SASL/SSL)
- Monitoring and observability

### Production Readiness

- Error handling and dead letter queues
- Idempotent processing
- At-least-once delivery guarantees
- Schema evolution (backward compatibility)
- Logging and metrics
- Containerization with Docker
- Documentation and architecture diagrams

### Presentation Requirements

- 25-minute presentation (5-min architecture, 10-min demo, 5-min deep dive, 5-min Q&A)
- Live demonstration or recorded video
- Architecture diagrams (Mermaid or similar)
- Code walkthrough of key patterns
- Ability to answer technical questions

## Assessment Criteria

Both capstone projects are evaluated on:

### Technical Implementation (40 points)

- Correct Kafka patterns and configurations
- Robust error handling
- Proper schema design and evolution
- Idempotent producers and consumers
- Comprehensive monitoring

### System Design (30 points)

- Well-reasoned architecture decisions
- Appropriate partition and replication strategy
- Scalability considerations
- Security implementation
- Data quality checks

### Demonstration (20 points)

- Smooth, rehearsed demo
- Clear explanations
- All components working together
- Good storytelling
- Handles questions during demo

### Understanding (10 points)

- Deep understanding of Kafka concepts
- Explains design tradeoffs
- Answers technical questions confidently
- Shows mastery of chosen technology stack

## Timeline

### 1 Week Before Presentation

- Complete all core features
- Set up monitoring
- Write architecture documentation
- Create initial presentation slides

### 3 Days Before

- Polish implementation
- Rehearse demo flow
- Prepare demo data
- Test on clean environment
- Record backup video

### 1 Day Before

- Final rehearsal (3x minimum)
- Test all demo scenarios
- Review key concepts
- Prepare Q&A answers
- Get good sleep

### Presentation Day

- Arrive 15 minutes early
- Start all services
- Verify connectivity
- Load demo data
- Be confident

## Not Sure Which Track to Choose?

If you haven't selected your learning track yet:

1. **Review the Track Selection Guide**: [Track Selection](../getting-started/track-selection.md)

2. **Compare side-by-side examples**: [Rosetta Stone](../learning/rosetta-stone.md)

3. **Check your career goals**: Data Engineer vs Software Engineer

4. **Consider your tech stack**: Python/Spark vs Java/Spring Boot

## Optional Extensions

After completing your core capstone, consider these advanced extensions:

### For Python Data Engineers

- Add Apache Flink for complex event processing
- Integrate with Apache Airflow for orchestration
- Add ClickHouse for high-performance analytics
- Implement exactly-once semantics with transactions
- Add machine learning model serving (MLflow)

See [Capstone Extensions](capstone-extensions.md) for details.

### For Java Developers

- Add Spring Cloud Stream for abstraction layer
- Implement CQRS pattern with event sourcing
- Add API Gateway (Spring Cloud Gateway)
- Implement distributed tracing (Spring Cloud Sleuth)
- Add GraphQL API layer

See [Capstone Extensions](capstone-extensions.md) for details.

## Resources

### Training Materials

- [Day 1: Foundation](../training/day01-foundation.md)
- [Day 2: Data Flow](../training/day02-dataflow.md)
- [Day 3: Producers](../training/day03-producers.md)
- [Day 4: Consumers](../training/day04-consumers.md)
- [Day 5: Schema Registry](../training/day05-schema-registry.md)
- [Day 6: Kafka Streams](../training/day06-streams.md)
- [Day 7: Kafka Connect](../training/day07-connect.md)
- [Day 8: Advanced Topics](../training/day08-advanced.md)

### Reference Documentation

- [Architecture Overview](../architecture/index.md)
- [Technology Stack](../architecture/tech-stack.md)
- [Deployment Guide](../deployment/deployment-guide.md)

### External Resources

- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Confluent Documentation](https://docs.confluent.io/)
- [Spring Kafka Reference](https://docs.spring.io/spring-kafka/reference/) (Java track)
- [Confluent Kafka Python](https://docs.confluent.io/kafka-clients/python/current/overview.html) (Python track)
- [Faust Documentation](https://faust.readthedocs.io/) (Python track)

## Getting Started

Ready to begin your capstone project?

=== "Python Data Engineers"

    ```bash
    # Start Kafka infrastructure
    docker-compose -f docker-compose-dev.yml up -d

    # Navigate to capstone guide
    cat docs/exercises/capstone-guide-python.md

    # Start building your analytics platform
    ```

    **Next Step**: [Python Capstone Guide](capstone-guide-python.md)

=== "Java Developers"

    ```bash
    # Start Kafka infrastructure
    docker-compose -f docker-compose-dev.yml up -d

    # Start Spring Boot application
    mvn spring-boot:run -Dspring-boot.run.profiles=dev

    # Open capstone guide
    cat docs/exercises/capstone-guide-java.md
    ```

    **Next Step**: [Java Capstone Guide](capstone-guide-java.md)

## Success Tips

**For Both Tracks:**

- Start early - don't wait until the last minute
- Rehearse your demo at least 3 times
- Prepare for technical questions
- Document your design decisions
- Test in a clean environment before presenting
- Have a backup plan if live demo fails
- Show your problem-solving process
- Be confident - you built this system

**Remember:** The capstone is your opportunity to showcase everything you've learned across all 8 days of training. Make it count!

Good luck with your capstone project!
