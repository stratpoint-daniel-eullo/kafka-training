# Kafka Training Exercises

## 🏋️ **Practice Exercises & Concept Reinforcement**

This directory contains hands-on exercises for the 8-day Apache Kafka training course.

### 🎯 **Purpose**:
- **Skill Practice**: Reinforce individual Kafka concepts
- **Extra Practice**: Additional exercises if you need more practice
- **Concept Validation**: Test your understanding of specific topics

### ❗ **Important**:
- **For Demo/Assessment**: Focus on [EventMart Progressive Project](../EVENTMART-PROJECT-GUIDE.md)
- **For Extra Practice**: Use these exercises/
- **See**: [Learning Paths Guide](../LEARNING-PATHS.md) for complete guidance

## 🎯 Exercise Structure

Each exercise file follows a consistent format:
- **Objective** - What you'll learn
- **Prerequisites** - What you need to know/have setup
- **Steps** - Detailed instructions
- **Expected Output** - What success looks like
- **Troubleshooting** - Common issues and solutions
- **Extension Activities** - Additional challenges

## 📋 Exercise List

### Phase 1: Foundation
- **[Day 1 Exercises](./day01-exercises.md)** - Topic operations, CLI basics, AdminClient
- **[Day 2 Exercises](./day02-exercises.md)** - Data flow, partitioning, consumer groups

### Phase 2: Java Development
- **[Day 3 Exercises](./day03-exercises.md)** - Producer development, error handling
- **[Day 4 Exercises](./day04-exercises.md)** - Consumer implementation, offset management
- **[Day 5 Exercises](./day05-exercises.md)** - Stream processing, transformations

### Phase 3: Advanced Topics
- **[Day 6 Exercises](./day06-exercises.md)** - Schema Registry, Avro serialization
- **[Day 7 Exercises](./day07-exercises.md)** - Kafka Connect, source/sink connectors
- **[Day 8 Exercises](./day08-exercises.md)** - Security, monitoring, production setup

## 🚀 Getting Started

### Prerequisites
1. Complete the setup from the main [README](../README.md)
2. Ensure Kafka is running: `docker-compose ps`
3. Verify Java examples compile: `mvn clean compile`

### Running Exercises

1. **Start the environment**: `docker-compose up -d`
2. **Read the documentation first**: Check the training day documentation
3. **Follow exercise steps**: Each exercise builds on previous knowledge
4. **Run the Java examples**: Use the provided Maven commands
5. **Verify results**: Check output matches expected results

### Example Commands

```bash
# Start the Kafka environment
docker-compose up -d

# Verify services are running
docker-compose ps

# Compile the project
mvn clean compile

# Run Day 1 example
mvn exec:java -Dexec.mainClass="com.training.kafka.Day01Foundation.BasicTopicOperations"

# Run tests (uses TestContainers - no Docker Compose needed)
mvn test

# View Kafka logs
docker-compose logs -f kafka

# Access Kafka container shell
docker-compose exec kafka bash
```

## 🔧 Troubleshooting

### Common Issues

1. **Kafka not running**
   ```bash
   # Start all services
   docker-compose up -d

   # Check service status
   docker-compose ps
   ```

2. **Port conflicts**
   ```bash
   # Check what's using Kafka port
   lsof -i :9092

   # Stop conflicting services
   docker-compose down
   ```

3. **Java compilation errors**
   ```bash
   # Clean and recompile
   mvn clean compile
   ```

4. **Topic already exists errors**
   ```bash
   # Delete a topic
   docker-compose exec kafka kafka-topics \
     --delete \
     --topic <topic-name> \
     --bootstrap-server localhost:9092
   ```

5. **Container issues**
   ```bash
   # Restart services
   docker-compose restart

   # Full reset (⚠️ deletes all data)
   docker-compose down -v
   docker-compose up -d
   ```

### Getting Help

- Check the troubleshooting section in each exercise
- Review the container documentation
- Verify your Docker Compose setup
- Check Kafka logs: `docker-compose logs -f kafka`
- Access Kafka UI at http://localhost:8081 for visual debugging

## 📊 Progress Tracking

Mark your progress as you complete each day:

- [ ] Day 1: Foundation exercises
- [ ] Day 2: Data flow exercises
- [ ] Day 3: Producer exercises
- [ ] Day 4: Consumer exercises
- [ ] Day 5: Streams exercises
- [ ] Day 6: Schema exercises
- [ ] Day 7: Connect exercises
- [ ] Day 8: Advanced exercises

## 💡 Tips for Success

- **Complete exercises in order** - Each builds on the previous
- **Don't skip steps** - Each step teaches important concepts
- **Experiment** - Try variations of the examples
- **Take notes** - Document what you learn
- **Practice regularly** - Repetition builds understanding

---

🎯 **Ready to practice?** Start with [Day 1 Exercises](./day01-exercises.md)!
