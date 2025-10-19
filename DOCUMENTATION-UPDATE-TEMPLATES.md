# Documentation Update Templates

Quick reference templates for completing the documentation overhaul.

## Warning Header Template

For Spring Boot specific files (WEB-UI, EventMart, API docs):

```markdown
> **Java Developer Track Only**
>
> This guide is for the **Spring Boot integration track**. If you're a data engineer looking for CLI-based, platform-agnostic Kafka training, see [README-DATA-ENGINEERS.md](./README-DATA-ENGINEERS.md).
>
> **Choose your track**: [START-HERE.md](./START-HERE.md)
```

Adjust paths based on file location:
- Root level: `./README-DATA-ENGINEERS.md` and `./START-HERE.md`
- docs/ subdirs: `../../README-DATA-ENGINEERS.md` and `../../START-HERE.md`

---

## Training Day File Template

Pattern used in day01-foundation.md:

### 1. Learning Objectives

```markdown
## Learning Objectives

By the end of Day X, you will:

- [x] Understand [core concept] (both tracks)
- [x] [Action] using CLI or Spring Boot (based on your track)
```

### 2. Code Examples Section

```markdown
## [Feature Name]

### Pure Java Approach (Data Engineer Track - Recommended)

Use the raw Kafka [API name] to [action]:

```java
// Pure Kafka - no Spring dependencies
import org.apache.kafka.clients.[package].*;
import java.util.*;

public class ExampleClass {

    public static void main(String[] args) throws Exception {
        // Configure client
        Properties props = new Properties();
        props.put([Config].BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        try (KafkaClient client = KafkaClient.create(props)) {
            // Example code
        }
    }
}
```

**Location**: `src/main/java/com/training/kafka/DayXX[Topic]/[ClassName].java`

**Run**:
```bash
java -cp target/kafka-training-java-1.0.0.jar \
  com.training.kafka.DayXX[Topic].[ClassName]
```

### [Feature Name] Examples

=== "CLI (Data Engineer Track)"

    ```bash
    # Using custom training CLI
    ./bin/kafka-training-cli.sh [command] \
      --option value

    # Or using native Kafka tools
    docker exec kafka-training-kafka kafka-[tool] \
      --bootstrap-server localhost:9092 \
      [options]
    ```

=== "Pure Java (Data Engineer Track)"

    ```java
    // Raw Kafka API - no Spring
    import org.apache.kafka.clients.[package].*;

    public class Example {
        public static void [action]() throws Exception {
            Properties props = new Properties();
            props.put([Config].BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

            try (KafkaClient client = KafkaClient.create(props)) {
                // Implementation
            }
        }
    }
    ```

=== "Spring Boot (Java Developer Track)"

    ```java
    @Service
    public class DayXX[Topic]Service {

        @Autowired
        private [SpringComponent] component;

        public [ReturnType] [methodName]() {
            // Spring Boot implementation
        }
    }
    ```

=== "REST API (Java Developer Track)"

    ```bash
    # REST endpoint
    curl -X POST http://localhost:8080/api/training/dayXX/[endpoint]
    ```
```

### 3. Exercises Section

```markdown
## Hands-On Exercises

### Exercise 1: [Title]

**Data Engineer Track**:
```bash
# CLI approach
./bin/kafka-training-cli.sh [command]

# Or pure Java
java -cp target/*.jar com.training.kafka.DayXX.[Class]
```

**Java Developer Track**:
```
1. Go to http://localhost:8080
2. Navigate to Day X section
3. Click [button]
4. [Expected result]
```
```

### 4. Project Integration Section

```markdown
## Project Integration

### Data Engineer Track

Apply concepts with pure Java examples:

```bash
# Run individual examples
java -cp target/kafka-training-java-1.0.0.jar \
  com.training.kafka.DayXX[Topic].[ExampleClass]

# Practice variations:
# - [Variation 1]
# - [Variation 2]
```

### Java Developer Track - EventMart Integration

Apply today's concepts to the EventMart project:

```bash
# EventMart specific operations
curl -X POST http://localhost:8080/api/training/eventmart/[endpoint]
```
```

### 5. Track Guidance Section (add before Next Steps)

```markdown
## Learning Track Guidance

This training supports two distinct approaches:

### Data Engineer Track (Recommended)
- **Focus**: Pure Kafka concepts, CLI tools, platform-agnostic
- **Examples**: `src/main/java/com/training/kafka/DayXX[Topic]/`
- **Run**: `./bin/kafka-training-cli.sh` or `java -cp target/*.jar`
- **Best for**: Data pipelines, platform integration, transferable skills

### Java Developer Track
- **Focus**: Spring Boot integration, web UI, REST APIs
- **Examples**: `src/main/java/com/training/kafka/services/DayXX[Topic]Service.java`
- **Run**: `mvn spring-boot:run` then use `http://localhost:8080`
- **Best for**: Microservices, Java-specific development, EventMart project

**Both tracks cover the same Kafka concepts** - choose based on your role and goals.

See [LEARNING-PATHS.md](../../LEARNING-PATHS.md) for detailed comparison.
```

### 6. Next Steps Section

```markdown
## Next Steps

Ready for Day [X+1]? Continue to [Day X+1: Topic](day0X-topic.md)

Or explore:

- **[README-DATA-ENGINEERS.md](../../README-DATA-ENGINEERS.md)** - CLI-first track
- **[WEB-UI-GETTING-STARTED.md](../../WEB-UI-GETTING-STARTED.md)** - Spring Boot track
- [Related Topic 1](../path/file.md)
- [Related Topic 2](../path/file.md)

---

**Practice is key!** Spend time experimenting with [topic concepts] (either via CLI or Spring Boot) before moving to Day [X+1].
```

---

## Exercise File Template

For files in docs/exercises/ and exercises/:

### Header Note

```markdown
# Day X Exercises

> **Note**: Exercises can be completed using either learning track:
> - **Data Engineer Track**: Use CLI tools or pure Java examples
> - **Java Developer Track**: Use Spring Boot web UI
>
> Choose based on your track: [LEARNING-PATHS.md](../LEARNING-PATHS.md)
```

### Exercise Format

```markdown
## Exercise [N]: [Title]

### Objective
[What the learner will accomplish]

### Data Engineer Track Approach

**Using CLI**:
```bash
# Step 1: [Action]
./bin/kafka-training-cli.sh [command] \
  --option value

# Step 2: [Action]
[command]
```

**Using Pure Java**:
```bash
# Run the example
java -cp target/kafka-training-java-1.0.0.jar \
  com.training.kafka.DayXX[Topic].[Class]

# Expected output:
# [output description]
```

**Expected Results**:
- [Result 1]
- [Result 2]

### Java Developer Track Approach

**Using Web UI**:
```
1. Start Spring Boot: mvn spring-boot:run
2. Open http://localhost:8080
3. Navigate to Day X section
4. [Specific steps]
```

**Using REST API**:
```bash
curl -X POST http://localhost:8080/api/training/dayXX/[endpoint]
```

**Expected Results**:
- [Result 1]
- [Result 2]

### Verification

**Both Tracks**:
```bash
# Verify using native Kafka tools
docker exec kafka-training-kafka kafka-[tool] \
  --bootstrap-server localhost:9092 \
  [verification command]
```

### Challenge (Optional)

[Advanced variation of the exercise]
```

---

## API Documentation Template

For docs/api/ files:

```markdown
# [API Topic]

> **Java Developer Track Only**
>
> This API documentation is for the Spring Boot web interface. Data engineers using the CLI track should focus on raw Kafka APIs.
>
> **Choose your track**: [START-HERE.md](../../START-HERE.md)

## Overview

[Existing API documentation content]
```

---

## Container Documentation Template

For docs/containers/ files:

### Balanced Approach

```markdown
## [Container Topic]

### Overview (All Tracks)

[Platform-agnostic container concepts]

### Data Engineer Track Usage

**Direct Kafka Containers**:
```bash
# Run Kafka examples in containers
docker-compose up -d

# Execute CLI commands
docker exec kafka-training-kafka kafka-[tool] [options]

# Run pure Java in container
docker exec kafka-training-app \
  java -cp /app/kafka-training.jar [MainClass]
```

### Java Developer Track Usage

**Spring Boot Container**:
```bash
# Start everything including Spring Boot
docker-compose up -d

# Access web UI
http://localhost:8080
```

### Common Patterns (Both Tracks)

[Shared Docker concepts and commands]
```

---

## Architecture Documentation Template

For docs/architecture/ files:

```markdown
## [Architecture Topic]

### Pure Kafka Architecture (All Tracks)

[Platform-agnostic architecture diagrams and explanations]

### Implementation Approaches

**Data Engineer Track**:
- Pure Kafka clients (KafkaProducer, KafkaConsumer)
- Raw API usage
- Direct configuration via Properties
- CLI-based management

**Java Developer Track**:
- Spring Boot integration
- KafkaTemplate abstractions
- Auto-configuration
- REST API layer

### Example: [Specific Pattern]

**Raw Kafka Approach**:
```java
// Pure Kafka implementation
```

**Spring Boot Approach**:
```java
// Spring Boot implementation
```
```

---

## Deployment Documentation Template

For docs/deployment/ files:

```markdown
## [Deployment Topic]

### Deploying Pure Kafka Applications (Data Engineer Track)

**Container Deployment**:
```bash
# Kafka cluster and pure Java applications
docker-compose -f docker-compose.kafka.yml up -d
```

**Kubernetes Deployment**:
```yaml
# Pure Kafka application deployment
apiVersion: apps/v1
kind: Deployment
metadata:
  name: kafka-consumer
spec:
  [Kafka consumer deployment spec]
```

### Deploying Spring Boot Kafka Applications (Java Developer Track - Optional)

**Container Deployment**:
```bash
# Spring Boot application with Kafka
docker-compose up -d
```

**Kubernetes Deployment**:
```yaml
# Spring Boot deployment
apiVersion: apps/v1
kind: Deployment
metadata:
  name: kafka-spring-app
spec:
  [Spring Boot deployment spec]
```
```

---

## Quick Checklist for Each File

Before marking a file as complete:

- [ ] Data Engineer examples shown FIRST
- [ ] CLI approach shown BEFORE Spring Boot
- [ ] Pure Java code has NO Spring imports
- [ ] Spring Boot sections labeled "Java Developer Track"
- [ ] Track guidance included (for training/exercise files)
- [ ] Warning header added (for Spring Boot specific files)
- [ ] Links updated to reference both track READMEs
- [ ] Examples tested or verified as accurate
- [ ] No assumptions about Spring Boot usage
- [ ] Terminology consistent with other updated files

---

## File-Specific Notes

### day03-producers.md

Key sections to update:
- Lead with `KafkaProducer` raw API (not `KafkaTemplate`)
- Show producer configuration via `Properties` first
- Move Spring Boot `@KafkaListener` to optional section
- Add CLI producer examples

### day04-consumers.md

Key sections to update:
- Lead with `KafkaConsumer` raw API (not Spring `@KafkaListener`)
- Show manual offset management first
- Consumer group management via raw API
- Move Spring Boot consumers to optional section

### day05-schema-registry.md

Key sections to update:
- Show Avro serialization without Spring
- Raw Schema Registry client usage
- CLI schema management
- Spring Boot as optional integration

### day06-streams.md

Key sections to update:
- Pure Kafka Streams API (StreamsBuilder)
- No Spring Cloud Stream in main examples
- Topology building without Spring
- Spring Cloud Stream as optional section

### day07-connect.md

Key sections to update:
- Kafka Connect REST API (platform-agnostic)
- Connector configuration files
- Running Connect standalone or distributed
- Spring Boot Connect integration as optional

### day08-advanced.md

Key sections to update:
- Security configuration (JAAS, SSL) for pure Kafka
- Monitoring with JMX (framework-agnostic)
- Production configuration via Properties
- Spring Boot security/monitoring as one option

---

## Common Patterns to Replace

### FROM (Spring Boot First)
```java
@Service
public class KafkaService {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void send(String topic, String message) {
        kafkaTemplate.send(topic, message);
    }
}
```

### TO (Pure Kafka First, Spring Optional)

**Pure Kafka (Recommended)**:
```java
import org.apache.kafka.clients.producer.*;
import java.util.Properties;

public class KafkaProducerExample {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            StringSerializer.class.getName());

        try (KafkaProducer<String, String> producer =
            new KafkaProducer<>(props)) {

            ProducerRecord<String, String> record =
                new ProducerRecord<>("topic", "message");

            producer.send(record).get();
        }
    }
}
```

**Spring Boot Alternative (Java Developer Track)**:
```java
@Service
public class KafkaService {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void send(String topic, String message) {
        kafkaTemplate.send(topic, message);
    }
}
```

---

## Completion Tracking

Use this checklist in DOCUMENTATION-UPDATE-SUMMARY.md to track progress:

```markdown
### docs/training/
- [x] day01-foundation.md (COMPLETED)
- [ ] day02-dataflow.md
- [ ] day03-producers.md
- [ ] day04-consumers.md
- [ ] day05-schema-registry.md
- [ ] day06-streams.md
- [ ] day07-connect.md
- [ ] day08-advanced.md

### docs/exercises/
- [ ] day01-exercises.md
- [ ] day02-exercises.md
[... etc]
```

---

## Tips for Efficiency

1. **Use day01-foundation.md as reference** - it has the complete pattern
2. **Search and replace** common patterns (KafkaTemplate → KafkaProducer, etc.)
3. **Keep Spring Boot content** - just reorder and relabel, don't delete
4. **Test one file fully** before batch processing
5. **Use consistent terminology** from the templates above
6. **Update table of contents** if the file has one

---

This template guide ensures consistency across all documentation updates while maintaining the "Data Engineer First" positioning throughout the training materials.
