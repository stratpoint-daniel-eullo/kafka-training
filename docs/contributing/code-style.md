# Code Style Guide

Follow these conventions to maintain code consistency.

## Java Coding Standards

We follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) with these highlights:

### Naming Conventions

```java
// Classes: PascalCase
public class Day01FoundationService { }

// Methods: camelCase
public void createTopic() { }

// Variables: camelCase
String topicName = "orders";

// Constants: UPPER_SNAKE_CASE
private static final int DEFAULT_PARTITIONS = 3;

// Packages: lowercase
package com.training.kafka.services;
```

### Formatting

**Indentation**: 4 spaces (no tabs)

```java
// Good
public void method() {
    if (condition) {
        doSomething();
    }
}

// Bad (tabs)
public void method() {
→   if (condition) {
→   →   doSomething();
→   }
}
```

**Line Length**: Maximum 120 characters

**Braces**: K&R style
```java
// Good
if (condition) {
    doSomething();
}

// Bad
if (condition)
{
    doSomething();
}
```

### Import Organization

1. Java standard library
2. Third-party libraries
3. Blank line
4. Project imports

```java
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.apache.kafka.clients.producer.ProducerConfig;

import com.training.kafka.config.TrainingKafkaProperties;
```

**No wildcard imports**:
```java
// Good
import java.util.List;
import java.util.Map;

// Bad
import java.util.*;
```

## Spring Boot Conventions

### Service Classes

```java
@Service
public class Day01FoundationService {
    
    private final AdminClient adminClient;
    private final TrainingKafkaProperties kafkaProperties;
    
    // Constructor injection (preferred)
    public Day01FoundationService(
            AdminClient adminClient,
            TrainingKafkaProperties kafkaProperties) {
        this.adminClient = adminClient;
        this.kafkaProperties = kafkaProperties;
    }
}
```

### REST Controllers

```java
@RestController
@RequestMapping("/api/training")
public class TrainingController {
    
    @PostMapping("/day01/demo")
    public ResponseEntity<Map<String, String>> runDemo() {
        // Implementation
        return ResponseEntity.ok(result);
    }
}
```

### Configuration Properties

```java
@ConfigurationProperties(prefix = "training.kafka")
public class TrainingKafkaProperties {
    private String bootstrapServers;
    private String clientId;
    
    // Getters and setters
}
```

## Documentation

### Class JavaDoc

```java
/**
 * Service for Day 1: Foundation & Admin API training.
 * 
 * <p>Demonstrates AdminClient operations including:
 * - Topic creation and configuration
 * - Topic listing and description
 * - Topic deletion
 * 
 * @see org.apache.kafka.clients.admin.AdminClient
 */
@Service
public class Day01FoundationService {
}
```

### Method JavaDoc

```java
/**
 * Creates a Kafka topic with the specified configuration.
 *
 * @param topicName the name of the topic to create
 * @param partitions the number of partitions (must be > 0)
 * @param replication the replication factor (must be >= 1)
 * @return a map containing the operation status and details
 * @throws IllegalArgumentException if partitions < 1 or replication < 1
 */
public Map<String, Object> createTopic(
        String topicName, int partitions, short replication) {
    // Implementation
}
```

### Inline Comments

```java
// Explain WHY, not WHAT
// Use idempotence to prevent duplicate messages in case of retries
config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
```

## Git Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types**:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting)
- `refactor`: Code refactoring
- `test`: Adding/updating tests
- `chore`: Build, dependencies, etc.

**Examples**:
```
feat(day01): add topic creation service

Implement AdminClient-based topic creation with validation
and error handling.

Closes #123
```

```
fix(day03): handle producer timeout exception

Add timeout configuration and proper exception handling
for producer send operations.
```

```
docs(readme): update installation instructions

Add Docker Compose setup steps and troubleshooting guide.
```

## Code Review Checklist

Before submitting a PR, ensure:

- [ ] Code follows style guide
- [ ] All tests pass (`mvn test`)
- [ ] New code has tests (80%+ coverage)
- [ ] JavaDoc added for public methods
- [ ] No compiler warnings
- [ ] Commit messages follow convention
- [ ] No commented-out code
- [ ] No `System.out.println` (use logger)
- [ ] Proper exception handling
- [ ] Resources closed properly

## Tools

### IntelliJ IDEA
Import code style: `.editorconfig` in project root

### Checkstyle (Optional)
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-checkstyle-plugin</artifactId>
    <configuration>
        <configLocation>google_checks.xml</configLocation>
    </configuration>
</plugin>
```

Run: `mvn checkstyle:check`

## Next Steps

- [Development Setup](development-setup.md) - Set up your environment
- [Testing Guidelines](testing.md) - Write comprehensive tests
