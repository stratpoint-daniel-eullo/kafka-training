# Hands-On Exercises

This training provides exercises for both learning tracks:

## Learning Tracks Overview

### Data Engineer Track (Recommended)

Focus on platform-agnostic Kafka skills:

- CLI tools (kafka-console-producer, kafka-console-consumer, kafka-topics, etc.)
- Pure Kafka API (KafkaProducer, KafkaConsumer, AdminClient, Streams API)
- Configuration and tuning with Properties objects
- Schema Registry CLI and REST API
- Kafka Connect REST API
- Performance optimization and production patterns

**Why Data Engineer Track?**

- Platform-independent skills transferable to any Kafka environment
- Deep understanding of Kafka fundamentals
- Direct control over configurations and behavior
- Industry-standard approach for data platform engineers

### Java Developer Track (Optional)

Additional exercises for Spring Boot developers:

- Spring Boot integration patterns
- Spring Kafka (@KafkaListener, KafkaTemplate)
- REST API development with Spring Web
- Spring configuration and profiles
- Web UI examples

**When to use Java Developer Track?**

- Building microservices with Spring Boot
- Rapid application development
- Teams already using Spring ecosystem
- REST API-driven architectures

## Exercise Structure by Day

Each day's exercises are organized into two sections:

1. **Data Engineer Track Exercises** (Recommended) - CLI and pure Kafka API focused
2. **Java Developer Track Exercises** (Optional) - Spring Boot integration focused

Choose exercises based on your learning goals and team requirements.

## Capstone Presentation Goal

At the end of the 8-day training, you'll present your work demonstrating mastery of:

### Technical Skills

- Apache Kafka architecture and operations
- Producer and consumer implementation
- Stream processing with Kafka Streams
- Schema management with Avro
- Data integration with Kafka Connect
- Container deployment and monitoring

### Presentation Components

1. **Architecture Overview** (5 min)
    - System design and component interaction
    - Topic structure and data flow
    - Technology choices and justification

2. **Live Demo** (10 min)
    - Running system demonstration
    - Key features in action
    - Real-time data processing
    - Monitoring and observability

3. **Technical Deep Dive** (5 min)
    - Code walkthrough of key components
    - Kafka patterns implemented
    - Challenges overcome
    - Performance considerations

4. **Q&A** (5 min)
    - Answer questions about implementation
    - Discuss design decisions
    - Explain troubleshooting approaches

## Exercise Structure by Day

### Phase 1: Foundation (Days 1-2)

**[Day 1: Kafka Fundamentals](day01-exercises.md)**

- Topic operations and AdminClient API
- Partition distribution and leadership
- Configuration management
- CLI and Java integration

**[Day 2: Data Flow Patterns](day02-exercises.md)**

- Message ordering and partitioning
- Producer and consumer patterns
- Offset management
- Delivery guarantees

### Phase 2: Development (Days 3-4)

**[Day 3: Producer Development](day03-exercises.md)**

- Producer configurations
- Synchronous vs asynchronous sending
- Error handling and retries
- Idempotent producers

**[Day 4: Consumer Implementation](day04-exercises.md)**

- Consumer groups and scaling
- Offset management strategies
- Rebalancing and partition assignment
- Error handling patterns

### Phase 3: Streams & Schema (Days 5-6)

**[Day 5: Stream Processing](day05-exercises.md)**

- Kafka Streams topologies
- Stateless transformations
- Stateful operations
- Windowing and aggregations

**[Day 6: Schema Registry & Avro](day06-exercises.md)**

- Schema definition and registration
- Avro serialization
- Schema evolution
- Compatibility checking

### Phase 4: Integration & Production (Days 7-8)

**[Day 7: Kafka Connect](day07-exercises.md)**

- Source and sink connectors
- JDBC connector configuration
- Custom transformations
- Error handling and DLQ

**[Day 8: Production Patterns](day08-exercises.md)**

- Security (SSL/SASL)
- Monitoring and metrics
- Performance tuning
- Deployment best practices

## Getting Started

### Prerequisites

```bash
# Start the Kafka environment
docker-compose up -d

# Verify services are running
docker-compose ps

# Compile the project
mvn clean compile
```

### Environment Setup

Ensure you have access to:

| Service | URL | Purpose |
|---------|-----|---------|
| Kafka Broker | localhost:9092 | Apache Kafka |
| Training REST API | http://localhost:8080/api/training/* | Spring Boot API endpoints (curl/Postman) |
| Kafka UI | http://localhost:8081 | Visual management (web browser) |
| Schema Registry | http://localhost:8082 | Schema management |
| Kafka Connect | http://localhost:8083 | Data integration |

### Exercise Workflow

1. **Read** the training day documentation first
2. **Choose your track** - Data Engineer (recommended) or Java Developer
3. **Start** with Exercise 1 in each day's exercises
4. **Complete** each exercise before moving to the next
5. **Verify** results match expected output
6. **Experiment** with variations
7. **Document** learnings for your capstone

## Capstone Preparation Tips

### Throughout the Training

- **Document your progress** - Take screenshots, save logs
- **Note challenges** - Record problems solved and lessons learned
- **Build incrementally** - Add features day by day
- **Test thoroughly** - Verify each component works
- **Monitor everything** - Track metrics and performance

### Week Before Presentation

1. **Polish your implementation** - Clean up code, add comments
2. **Prepare slides** - Architecture diagrams, key metrics
3. **Practice demo** - Ensure reliable environment
4. **Prepare talking points** - Key technical decisions
5. **Test backup plans** - Have recorded demo ready

### Presentation Day

- **Start with architecture** - Big picture first
- **Show live system** - Real data flowing
- **Highlight complexity** - Kafka-specific features
- **Discuss tradeoffs** - Design decisions made
- **Be ready to dive deep** - Code review if asked

## Assessment Criteria

Your capstone will be evaluated on:

### Technical Implementation (40%)

- Correct Kafka usage patterns
- Proper error handling
- Appropriate configurations
- Code quality and structure

### System Design (30%)

- Appropriate architecture choices
- Scalability considerations
- Monitoring and observability
- Production readiness

### Demonstration (20%)

- Clear presentation
- Working live demo
- Effective communication
- Problem-solving approach

### Understanding (10%)

- Answer technical questions
- Explain design decisions
- Discuss alternatives
- Show troubleshooting skills

## Resources

### Documentation

- [Training Day Guides](../training/) - Comprehensive documentation
- [Container Development](../containers/) - Docker and Kubernetes
- [Deployment Guide](../deployment/) - Production patterns

### Code Examples

- [EventMart Project](../../src/main/java/com/training/kafka/eventmart/) - Reference implementation
- [Integration Tests](../../src/test/java/com/training/kafka/) - 90+ test examples

### Support

- Review troubleshooting sections in each exercise
- Check container logs: `docker-compose logs -f`
- Use Kafka UI for visual debugging
- Refer to training day documentation

## Progress Tracking

Track your completion:

- [ ] Day 1: Foundation exercises
- [ ] Day 2: Data flow exercises
- [ ] Day 3: Producer exercises
- [ ] Day 4: Consumer exercises
- [ ] Day 5: Streams exercises
- [ ] Day 6: Schema exercises
- [ ] Day 7: Connect exercises
- [ ] Day 8: Advanced exercises
- [ ] Capstone: Presentation prepared

---

## Next Steps

**Option A: Start Data Engineer Track**

Begin with [Day 1 Exercises](day01-exercises.md) - recommended for platform-agnostic Kafka skills

**Option B: Start Java Developer Track**

Begin with [Day 1 Exercises](day01-exercises.md) - focus on Spring Boot integration exercises

**Option C: Build EventMart** (Recommended for Capstone)

Follow the [EventMart Project Guide](../training/) to build your presentation project

**Ready to begin?** Choose your path and start learning!
