# Testing Guidelines

All code contributions must include tests. This project uses TestContainers for integration testing with real Kafka.

## Running Tests

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=Day01FoundationTest
```

### Run Specific Test Method
```bash
mvn test -Dtest=Day01FoundationTest#shouldCreateTopic
```

### Run Tests with Coverage
```bash
mvn test jacoco:report
```

View coverage report at `target/site/jacoco/index.html`.

## Test Structure

### Integration Tests with TestContainers

All test classes use TestContainers to spin up real Kafka:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class Day01FoundationTest {

    @Container
    static final KafkaContainer kafka = new KafkaContainer(
        DockerImageName.parse("confluentinc/cp-kafka:7.7.0"))
        .withEmbeddedZookeeper();

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("training.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Test
    void shouldCreateTopic() {
        // Test with REAL Kafka running in Docker
    }
}
```

### Test Naming Convention

- Class: `<Day/Feature>Test.java` (e.g., `Day01FoundationTest.java`)
- Method: `should<DoSomething>` (e.g., `shouldCreateTopic`)

### Test Organization

```
src/test/java/com/training/kafka/
├── Day01FoundationTest.java      # Day 1 tests
├── Day02DataFlowTest.java        # Day 2 tests
├── Day03ProducerTest.java        # Day 3 tests
├── Day04ConsumerTest.java        # Day 4 tests
├── Day05SchemaTest.java          # Day 5 tests
├── Day06StreamsTest.java         # Day 6 tests
├── Day07ConnectTest.java         # Day 7 tests
└── Day08AdvancedTest.java        # Day 8 tests
```

## Writing Tests

### Service Tests

Test service methods directly:

```java
@Test
void shouldCreateTopicSuccessfully() {
    // Arrange
    String topicName = "test-topic-" + UUID.randomUUID();
    int partitions = 3;
    short replication = 1;

    // Act
    Map<String, Object> result = foundationService.createTopic(
        topicName, partitions, replication);

    // Assert
    assertEquals("success", result.get("status"));
    assertEquals(topicName, result.get("topic"));
}
```

### REST API Tests

Test API endpoints using TestRestTemplate:

```java
@Autowired
private TestRestTemplate restTemplate;

@Test
void shouldCreateTopicViaAPI() {
    // Act
    ResponseEntity<Map> response = restTemplate.postForEntity(
        "/api/training/day01/create-topic?name=test&partitions=3&replication=1",
        null,
        Map.class);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("success", response.getBody().get("status"));
}
```

### Consumer Tests with Awaitility

Use Awaitility for async consumer testing:

```java
@Test
void shouldConsumeMessages() throws InterruptedException {
    // Arrange
    consumerService.resetMessageCounter();
    produceMessages("test-topic", 5);

    // Act & Assert
    await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
        int count = consumerService.getProcessedMessageCount();
        assertTrue(count >= 5, "Expected at least 5 messages, got " + count);
    });
}
```

## Test Coverage Goals

- **Minimum**: 80% line coverage
- **Target**: 90% line coverage
- **Critical paths**: 100% coverage (e.g., producer/consumer logic)

## Testing Best Practices

### 1. Isolated Tests
Each test should be independent:

```java
@AfterEach
void cleanup() {
    // Delete test topics
    foundationService.deleteTopic(testTopic);
}
```

### 2. Descriptive Names
```java
// Good
@Test void shouldCreateTopicWithMultiplePartitions()

// Bad
@Test void test1()
```

### 3. Arrange-Act-Assert Pattern
```java
@Test
void shouldProduceMessage() {
    // Arrange
    String topic = "test-topic";
    String message = "test-message";

    // Act
    RecordMetadata metadata = producer.send(topic, message);

    // Assert
    assertNotNull(metadata);
    assertEquals(topic, metadata.topic());
}
```

### 4. Use TestContainers for Real Kafka
Don't mock Kafka! Use TestContainers for realistic testing:

```java
// Good: Real Kafka
@Container
static final KafkaContainer kafka = new KafkaContainer(...);

// Bad: Mocked Kafka
@MockBean
private KafkaTemplate mockKafka;
```

### 5. Clean Up Resources
```java
@AfterAll
static void stopContainers() {
    kafka.stop();
}
```

## Debugging Tests

### Enable Debug Logging
```properties
# src/test/resources/application-test.properties
logging.level.com.training.kafka=DEBUG
logging.level.org.apache.kafka=INFO
```

### Run Tests in Debug Mode (IDE)
1. Set breakpoint in test
2. Right-click test method
3. Select "Debug Test"

### View TestContainers Logs
```java
@Container
static final KafkaContainer kafka = new KafkaContainer(...)
    .withLogConsumer(new Slf4jLogConsumer(log));
```

## CI/CD Integration

Tests run automatically on every push via GitHub Actions:

```yaml
name: Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-java@v2
        with:
          java-version: '11'
      - run: mvn test
```

## Next Steps

- [Development Setup](development-setup.md) - Set up your environment
- [Code Style](code-style.md) - Follow coding standards
