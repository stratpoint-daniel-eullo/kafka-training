# Day 6: Schema Management with Apache Avro

## Learning Objectives
By the end of Day 6, you will:
- Understand schema evolution and compatibility
- Implement Avro producers and consumers
- Work with Schema Registry
- Handle different schema versions gracefully
- Implement data governance best practices

## 🚀 Quick Start with Spring Boot

### Prerequisites
- Docker and Docker Compose installed
- Maven 3.6+ installed
- Port 8080 (Spring Boot) and 8082 (Schema Registry) available

### Start Infrastructure

```bash
# Start Kafka, Schema Registry, and supporting services
docker-compose up -d

# Verify Schema Registry is running
curl http://localhost:8082/subjects

# Start Spring Boot application
mvn spring-boot:run
```

### EventMart Avro Schemas

The Spring Boot implementation includes **4 production-ready Avro schemas** for the EventMart e-commerce platform:

#### 1. UserEvent Schema
Tracks user activity events with device information and session tracking:
- **Actions**: LOGIN, LOGOUT, PURCHASE, PAGE_VIEW, SEARCH, CART_ADD, CART_REMOVE
- **Fields**: userId, action, timestamp, sessionId, properties, deviceInfo, version
- **Topic**: `user-events-avro`

#### 2. ProductEvent Schema
Manages product lifecycle with vendor information and attributes:
- **Event Types**: CREATED, UPDATED, DELETED, STOCK_CHANGED, PRICE_CHANGED, RESTOCKED
- **Fields**: productId, eventType, timestamp, name, category, price, stockQuantity, description, attributes, tags, vendor, version
- **Topic**: `product-events-avro`

#### 3. OrderEvent Schema
Tracks order processing with shipping and payment details:
- **Event Types**: CREATED, UPDATED, CANCELLED, CONFIRMED, SHIPPED, DELIVERED, RETURNED
- **Fields**: orderId, eventType, timestamp, userId, orderItems, totalAmount, status, shippingAddress, paymentInfo, metadata, version
- **Topic**: `order-events-avro`

#### 4. PaymentEvent Schema
Manages payment processing with risk scoring and card information:
- **Event Types**: AUTHORIZED, CAPTURED, DECLINED, REFUNDED, CANCELLED, PENDING, FAILED
- **Payment Methods**: CREDIT_CARD, DEBIT_CARD, PAYPAL, APPLE_PAY, GOOGLE_PAY
- **Fields**: paymentId, eventType, timestamp, orderId, userId, amount, currency, paymentMethod, status, processorTransactionId, cardInfo, errorDetails, riskScore, metadata, version
- **Topic**: `payment-events-avro`

### REST API Endpoints

All Day 6 features are accessible via REST API at `http://localhost:8080/api/training/`:

#### 1. Run Complete Day 6 Demo
```bash
curl -X POST http://localhost:8080/api/training/day06/demo
```

**Response:**
```json
{
  "status": "success",
  "message": "Day 6 Schema Management demonstration completed successfully",
  "module": "Day06Schemas",
  "info": "Avro events produced and consumed with Schema Registry integration"
}
```

#### 2. Produce Avro Events
```bash
curl -X POST http://localhost:8080/api/training/day06/avro/produce
```

Produces sample events to all 4 Avro topics:
- UserEvent with device information
- ProductEvent with vendor details
- OrderEvent with order items and shipping address
- PaymentEvent with card information and risk score

**Response:**
```json
{
  "status": "success",
  "message": "Avro events produced successfully",
  "topics": ["user-events-avro", "product-events-avro", "order-events-avro", "payment-events-avro"],
  "count": 4
}
```

#### 3. Consume Avro Events
```bash
curl -X POST http://localhost:8080/api/training/day06/avro/consume
```

Consumes events from all 4 Avro topics using specific Avro reader:

**Response:**
```json
{
  "status": "success",
  "message": "Avro events consumed successfully",
  "recordsConsumed": 4,
  "details": [
    "UserEvent: userId=user-001, action=LOGIN",
    "ProductEvent: productId=prod-001, price=$99.99",
    "OrderEvent: orderId=order-001, totalAmount=$199.98",
    "PaymentEvent: paymentId=pay-001, amount=$199.98"
  ]
}
```

#### 4. List Registered Schemas
```bash
curl http://localhost:8080/api/training/day06/schemas/list
```

Lists all schemas registered in Schema Registry:

**Response:**
```json
{
  "status": "success",
  "count": 4,
  "schemas": [
    {"subject": "user-events-avro-value", "version": 1, "id": 1},
    {"subject": "product-events-avro-value", "version": 1, "id": 2},
    {"subject": "order-events-avro-value", "version": 1, "id": 3},
    {"subject": "payment-events-avro-value", "version": 1, "id": 4}
  ]
}
```

#### 5. Check Schema Compatibility
```bash
curl -X POST "http://localhost:8080/api/training/day06/schemas/compatibility?subject=user-events-avro-value&schema=$(cat src/main/resources/schemas/user-event.avsc)"
```

Tests schema compatibility before evolution:

**Response:**
```json
{
  "status": "success",
  "subject": "user-events-avro-value",
  "compatible": true
}
```

### Configuration

Schema Registry and Avro serialization are configured in `application.properties`:

```properties
# Schema Registry Configuration (Day 6)
training.kafka.schema-registry-url=http://localhost:8082
spring.kafka.properties.schema.registry.url=http://localhost:8082

# Avro Serializer Configuration
spring.kafka.producer.properties.value.serializer=io.confluent.kafka.serializers.KafkaAvroSerializer
spring.kafka.consumer.properties.value.deserializer=io.confluent.kafka.serializers.KafkaAvroDeserializer
spring.kafka.consumer.properties.specific.avro.reader=true

# Schema Registry Client Configuration
spring.kafka.properties.auto.register.schemas=true
spring.kafka.properties.use.latest.version=false
spring.kafka.properties.schema.compatibility=BACKWARD
```

### Code Examples

#### Creating Avro Producer with Schema Registry

```java
@Service
public class Day06SchemaService {

    @Autowired
    private TrainingKafkaProperties kafkaProperties;

    private KafkaProducer<String, Object> createAvroProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
            kafkaProperties.getKafka().getBootstrapServers());
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "avro-producer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        props.put("schema.registry.url", kafkaProperties.getKafka().getSchemaRegistryUrl());
        props.put(ProducerConfig.ACKS_CONFIG, "all");

        return new KafkaProducer<>(props);
    }
}
```

#### Producing UserEvent with Avro

```java
public void produceAvroEvents() {
    KafkaProducer<String, Object> producer = createAvroProducer();

    // Create UserEvent using Avro builder
    UserEvent userEvent = UserEvent.newBuilder()
        .setUserId("user-001")
        .setAction(ActionType.LOGIN)
        .setTimestamp(System.currentTimeMillis())
        .setSessionId("session-" + UUID.randomUUID().toString())
        .setProperties(new HashMap<>())
        .setDeviceInfo(DeviceInfo.newBuilder()
            .setDeviceType("Desktop")
            .setUserAgent("Mozilla/5.0")
            .setIpAddress("192.168.1.100")
            .setLocation("San Francisco, CA")
            .build())
        .setVersion(1)
        .build();

    // Produce to Kafka with automatic schema registration
    ProducerRecord<String, Object> record =
        new ProducerRecord<>("user-events-avro", userEvent.getUserId().toString(), userEvent);
    RecordMetadata metadata = producer.send(record).get();

    logger.info("Produced UserEvent: topic={}, partition={}, offset={}",
        metadata.topic(), metadata.partition(), metadata.offset());
}
```

#### Consuming with Specific Avro Reader

```java
private KafkaConsumer<String, Object> createAvroConsumer() {
    Properties props = new Properties();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
        kafkaProperties.getKafka().getBootstrapServers());
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "avro-consumer-group");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
    props.put("schema.registry.url", kafkaProperties.getKafka().getSchemaRegistryUrl());
    props.put("specific.avro.reader", "true");  // Use specific reader for type safety
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    return new KafkaConsumer<>(props);
}

public void consumeAvroEvents() {
    KafkaConsumer<String, Object> consumer = createAvroConsumer();
    consumer.subscribe(Arrays.asList("user-events-avro", "product-events-avro",
        "order-events-avro", "payment-events-avro"));

    ConsumerRecords<String, Object> records = consumer.poll(Duration.ofSeconds(5));

    for (ConsumerRecord<String, Object> record : records) {
        Object value = record.value();

        if (value instanceof UserEvent) {
            UserEvent event = (UserEvent) value;
            logger.info("Consumed UserEvent: userId={}, action={}",
                event.getUserId(), event.getAction());
        } else if (value instanceof ProductEvent) {
            ProductEvent event = (ProductEvent) value;
            logger.info("Consumed ProductEvent: productId={}, price=${}",
                event.getProductId(), event.getPrice());
        }
        // ... handle other event types
    }
}
```

#### Working with Schema Registry Client

```java
@Service
public class Day06SchemaService {

    private SchemaRegistryClient schemaRegistryClient;

    @PostConstruct
    public void init() {
        String schemaRegistryUrl = kafkaProperties.getKafka().getSchemaRegistryUrl();
        schemaRegistryClient = new CachedSchemaRegistryClient(schemaRegistryUrl, 100);
        logger.info("Schema Registry client initialized with URL: {}", schemaRegistryUrl);
    }

    public Map<String, Object> listRegisteredSchemas() {
        Map<String, Object> result = new HashMap<>();

        try {
            Collection<String> subjects = schemaRegistryClient.getAllSubjects();

            for (String subject : subjects) {
                int version = schemaRegistryClient.getLatestSchemaMetadata(subject).getVersion();
                int id = schemaRegistryClient.getLatestSchemaMetadata(subject).getId();
                logger.info("Schema: subject={}, version={}, id={}", subject, version, id);
            }

            result.put("status", "success");
            result.put("count", subjects.size());
        } catch (IOException | RestClientException e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
        }

        return result;
    }

    public Map<String, Object> checkSchemaCompatibility(String subject, String schemaString) {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean compatible = schemaRegistryClient.testCompatibility(subject,
                new org.apache.avro.Schema.Parser().parse(schemaString));

            result.put("status", "success");
            result.put("subject", subject);
            result.put("compatible", compatible);
        } catch (IOException | RestClientException e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
        }

        return result;
    }
}
```

### Testing with TestContainers

Comprehensive tests using TestContainers ensure Day 6 functionality works correctly:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class Day06SchemaTest {

    @Container
    @SuppressWarnings("resource")
    static final KafkaContainer kafka = new KafkaContainer(
        DockerImageName.parse("confluentinc/cp-kafka:7.7.0"))
        .withEmbeddedZookeeper();

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private Day06SchemaService schemaService;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("training.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("training.kafka.schema-registry-url", () -> "http://localhost:8082");
    }

    @Test
    @DisplayName("Day06SchemaService should be initialized")
    void shouldInitializeSchemaService() {
        assertNotNull(schemaService, "Schema service should be initialized");
    }

    @Test
    @DisplayName("Avro produce API endpoint should exist")
    void shouldHaveAvroProduceEndpoint() {
        ResponseEntity<Map> response = restTemplate.postForEntity(
            "http://localhost:" + port + "/api/training/day06/avro/produce",
            null, Map.class);

        assertNotNull(response);
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("status"));
    }

    @Test
    @DisplayName("Avro schemas should be generated from .avsc files")
    void shouldHaveGeneratedAvroClasses() {
        try {
            Class.forName("com.training.kafka.avro.UserEvent");
            Class.forName("com.training.kafka.avro.ProductEvent");
            Class.forName("com.training.kafka.avro.OrderEvent");
            Class.forName("com.training.kafka.avro.PaymentEvent");

            assertTrue(true, "All Avro classes should be generated");
        } catch (ClassNotFoundException e) {
            fail("Avro classes should be generated from .avsc files: " + e.getMessage());
        }
    }
}
```

### Running Tests

```bash
# Run all Day 6 tests
mvn test -Dtest=Day06SchemaTest

# Run specific test
mvn test -Dtest=Day06SchemaTest#shouldHaveAvroProduceEndpoint

# Run all tests with coverage
mvn clean test jacoco:report
```

### Schema Evolution Example

Demonstrate BACKWARD compatibility with schema evolution:

#### Step 1: Original UserEvent Schema (v1)
```json
{
  "type": "record",
  "name": "UserEvent",
  "namespace": "com.training.kafka.avro",
  "fields": [
    {"name": "userId", "type": "string"},
    {"name": "action", "type": {"type": "enum", "name": "ActionType",
      "symbols": ["LOGIN", "LOGOUT", "PURCHASE"]}},
    {"name": "timestamp", "type": "long"}
  ]
}
```

#### Step 2: Evolved Schema (v2) - Add Optional Field
```json
{
  "type": "record",
  "name": "UserEvent",
  "namespace": "com.training.kafka.avro",
  "fields": [
    {"name": "userId", "type": "string"},
    {"name": "action", "type": {"type": "enum", "name": "ActionType",
      "symbols": ["LOGIN", "LOGOUT", "PURCHASE", "PAGE_VIEW", "SEARCH"]}},
    {"name": "timestamp", "type": "long"},
    {"name": "sessionId", "type": "string", "default": "unknown"}
  ]
}
```

#### Step 3: Check Compatibility
```bash
# Test compatibility before registering new schema
curl -X POST "http://localhost:8080/api/training/day06/schemas/compatibility?subject=user-events-avro-value&schema=$(cat src/main/resources/schemas/user-event-v2.avsc)"
```

**BACKWARD Compatible Changes:**
- ✅ Add optional field with default value
- ✅ Delete optional field
- ✅ Add enum symbols

**BACKWARD Incompatible Changes:**
- ❌ Delete required field
- ❌ Change field type
- ❌ Rename field
- ❌ Delete enum symbol

### View Training Modules

```bash
curl http://localhost:8080/api/training/modules
```

**Response includes Day 6:**
```json
{
  "Day06Schemas": {
    "name": "Schema Management",
    "description": "Avro schemas and Schema Registry integration",
    "topics": [
      "Apache Avro serialization",
      "Schema Registry operations",
      "Schema evolution patterns",
      "BACKWARD/FORWARD/FULL compatibility"
    ],
    "endpoints": [
      "POST /api/training/day06/demo",
      "POST /api/training/day06/avro/produce",
      "POST /api/training/day06/avro/consume",
      "GET /api/training/day06/schemas/list",
      "POST /api/training/day06/schemas/compatibility"
    ]
  }
}
```

### Monitoring Schema Registry

```bash
# Check Schema Registry health
curl http://localhost:8082/subjects

# View all schemas
curl http://localhost:8082/subjects

# Get schema details
curl http://localhost:8082/subjects/user-events-avro-value/versions/1

# Check compatibility level
curl http://localhost:8082/config/user-events-avro-value
```

### What You've Learned

After completing this Spring Boot implementation, you've learned:

1. **Avro Schema Design**: Created 4 production-ready schemas for EventMart
2. **Schema Registry Integration**: Automatic schema registration and retrieval
3. **Type-Safe Serialization**: KafkaAvroSerializer/Deserializer usage
4. **Schema Evolution**: BACKWARD compatibility with optional fields
5. **REST API Development**: 5 endpoints for schema management
6. **Testing with TestContainers**: Integration tests with Kafka container
7. **Error Handling**: Graceful handling of Schema Registry unavailability
8. **Production Patterns**: Versioning, documentation, and compatibility checking

### Next Steps

- Run the demo to see all 4 event types in action
- Experiment with schema evolution by adding optional fields
- Test compatibility with different schema versions
- Monitor Schema Registry via Kafka UI at http://localhost:8080

## Morning Session (3 hours): Schema Theory and Avro Basics

### 1. Why Schema Management?

**Benefits of Schema Management:**
- **Data Quality**: Enforce structure and types
- **Evolution**: Handle changes without breaking consumers
- **Documentation**: Schema serves as API contract
- **Efficiency**: Binary serialization is faster and smaller
- **Compatibility**: Ensure forward/backward compatibility

### 2. Apache Avro Overview

**Key Features:**
- Rich data structures
- Compact, fast binary data format
- Container file to store persistent data
- RPC (Remote Procedure Call)
- Simple integration with dynamic languages

**Schema Evolution Types:**
- **Forward Compatibility**: New schema can read old data
- **Backward Compatibility**: Old schema can read new data
- **Full Compatibility**: Both forward and backward compatible

### 3. Schema Registry

**What is Schema Registry?**
- Centralized repository for schemas
- RESTful interface for storing and retrieving schemas
- Schema versioning and compatibility checking
- Integration with Kafka ecosystem

**Key Concepts:**
- **Subject**: Named container for schema versions
- **Schema ID**: Unique identifier for each schema version
- **Compatibility Level**: Rules for schema evolution

## Afternoon Session (3 hours): Hands-on Implementation

### 1. Set Up Schema Registry

```bash
# Start Schema Registry (if using Docker)
docker-compose up -d schema-registry

# Or start with Confluent CLI
confluent local schema-registry start

# Verify Schema Registry is running
curl http://localhost:8081/subjects
```

### 2. Avro Schema Development

#### Our User Event Schema (Already Created)
```json
{
  "type": "record",
  "name": "UserEvent",
  "namespace": "com.training.kafka.avro",
  "fields": [
    {"name": "userId", "type": "string"},
    {"name": "action", "type": {"type": "enum", "name": "ActionType", "symbols": ["LOGIN", "LOGOUT", "PURCHASE", "PAGE_VIEW", "SEARCH", "CART_ADD", "CART_REMOVE"]}},
    {"name": "timestamp", "type": "long", "logicalType": "timestamp-millis"},
    {"name": "sessionId", "type": "string"},
    {"name": "properties", "type": {"type": "map", "values": "string"}, "default": {}},
    {"name": "deviceInfo", "type": ["null", {"type": "record", "name": "DeviceInfo", "fields": [...]}], "default": null},
    {"name": "version", "type": "int", "default": 1}
  ]
}
```

### 3. Generate Avro Classes

```bash
# Generate Java classes from schema
mvn avro:schema

# This creates classes in target/generated-sources/avro/
# - com.training.kafka.avro.UserEvent
# - com.training.kafka.avro.ActionType
# - com.training.kafka.avro.DeviceInfo
```

### 4. Avro Producer Implementation

Run the Avro Producer example:

```bash
# Create Avro topic
confluent local kafka topic create avro-user-events \
  --partitions 3 \
  --replication-factor 1

# Run Avro Producer
mvn exec:java -Dexec.mainClass="com.training.kafka.Day06Schemas.AvroProducer"
```

**Key Implementation Details:**

1. **Schema Registry Configuration**
```java
props.put("schema.registry.url", "http://localhost:8081");
props.put("auto.register.schemas", "true");
props.put("use.latest.version", "true");
```

2. **Avro Serializer**
```java
props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
```

3. **Creating Avro Objects**
```java
UserEvent userEvent = UserEvent.newBuilder()
    .setUserId(userId)
    .setAction(action)
    .setTimestamp(Instant.now().toEpochMilli())
    .setSessionId("session_" + System.currentTimeMillis())
    .setProperties(properties)
    .setDeviceInfo(deviceInfo)
    .setVersion(1)
    .build();
```

### 5. Avro Consumer Implementation

Run the Avro Consumer example:

```bash
# Run Avro Consumer
mvn exec:java -Dexec.mainClass="com.training.kafka.Day06Schemas.AvroConsumer"

# Run with schema evolution demo
mvn exec:java -Dexec.mainClass="com.training.kafka.Day06Schemas.AvroConsumer" -Dexec.args="schema-evolution"
```

**Key Implementation Details:**

1. **Specific Avro Reader**
```java
props.put("specific.avro.reader", "true");
```

2. **Type-Safe Consumption**
```java
KafkaConsumer<String, UserEvent> consumer = new KafkaConsumer<>(props);
UserEvent userEvent = record.value();
```

3. **Handling Optional Fields**
```java
if (userEvent.getDeviceInfo() != null) {
    logger.info("Device Type: {}", userEvent.getDeviceInfo().getDeviceType());
}
```

### 6. Schema Evolution Examples

#### Example 1: Adding Optional Field

**Original Schema (v1):**
```json
{
  "type": "record",
  "name": "UserEvent",
  "fields": [
    {"name": "userId", "type": "string"},
    {"name": "action", "type": "string"}
  ]
}
```

**Evolved Schema (v2) - Adding Optional Field:**
```json
{
  "type": "record", 
  "name": "UserEvent",
  "fields": [
    {"name": "userId", "type": "string"},
    {"name": "action", "type": "string"},
    {"name": "timestamp", "type": "long", "default": 0}
  ]
}
```

#### Example 2: Schema Evolution Best Practices

```bash
# Register schema manually
curl -X POST \
  -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  --data '{"schema": "{\"type\":\"record\",\"name\":\"UserEvent\"...}"}' \
  http://localhost:8081/subjects/avro-user-events-value/versions

# Check compatibility before evolving
curl -X POST \
  -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  --data '{"schema": "{\"type\":\"record\",\"name\":\"UserEvent\"...}"}' \
  http://localhost:8081/compatibility/subjects/avro-user-events-value/versions/latest
```

## Schema Registry Operations

### CLI Commands

```bash
# List all subjects
curl http://localhost:8081/subjects

# Get latest schema for subject
curl http://localhost:8081/subjects/avro-user-events-value/versions/latest

# Get all versions for subject
curl http://localhost:8081/subjects/avro-user-events-value/versions

# Set compatibility level
curl -X PUT \
  -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  --data '{"compatibility": "BACKWARD"}' \
  http://localhost:8081/config/avro-user-events-value

# Delete schema version
curl -X DELETE http://localhost:8081/subjects/avro-user-events-value/versions/1
```

### Compatibility Levels

1. **BACKWARD**: New schema can read data written with previous schema
2. **FORWARD**: Previous schema can read data written with new schema  
3. **FULL**: Both backward and forward compatible
4. **NONE**: No compatibility checking
5. **BACKWARD_TRANSITIVE**: Backward compatible with all previous versions
6. **FORWARD_TRANSITIVE**: Forward compatible with all previous versions
7. **FULL_TRANSITIVE**: Both backward and forward transitive

## Exercises

### Exercise 1: Basic Avro Producer/Consumer
1. Run the AvroProducer to send events
2. Run the AvroConsumer to receive and process events
3. Observe the structured data and type safety

### Exercise 2: Schema Evolution
1. Modify the UserEvent schema to add a new optional field
2. Generate new Java classes
3. Update producer to set the new field
4. Verify old consumers can still read new data
5. Update consumer to handle the new field

### Exercise 3: Schema Registry Operations
1. Register a schema manually via REST API
2. Check compatibility before making changes
3. Evolve the schema with a breaking change
4. Observe what happens when compatibility fails

### Exercise 4: Error Handling
1. Try sending malformed data
2. Handle schema registry unavailability
3. Deal with deserialization errors
4. Implement retry logic for schema operations

## Best Practices

### 1. Schema Design
- Use meaningful field names and documentation
- Design for evolution from the start
- Use unions sparingly (prefer optional fields)
- Include version fields for tracking
- Use logical types for dates, decimals, etc.

### 2. Compatibility Strategy
- Start with BACKWARD compatibility for most use cases
- Use FULL compatibility for critical schemas
- Plan schema evolution carefully
- Test compatibility before deploying changes

### 3. Schema Registry Management
- Use namespaces to organize schemas
- Implement CI/CD for schema changes
- Monitor schema registry health
- Back up schema registry data
- Use meaningful subject naming conventions

### 4. Development Workflow
- Generate classes in build process
- Version control your schemas
- Use schema validation in tests
- Document breaking changes clearly
- Coordinate schema changes across teams

## Performance Considerations

### 1. Serialization Performance
- Avro is faster than JSON for large objects
- Binary format reduces network overhead
- Schema caching improves performance
- Consider compression at topic level

### 2. Schema Registry Caching
- Enable client-side schema caching
- Monitor cache hit rates
- Size caches appropriately
- Handle cache refresh gracefully

### 3. Memory Usage
- Avro objects can be memory intensive
- Consider object pooling for high throughput
- Monitor JVM heap usage
- Use specific readers when possible

## Troubleshooting

### Common Issues

1. **Schema not found**
   - Check Schema Registry connectivity
   - Verify subject name matches
   - Ensure schema is registered

2. **Incompatible schema changes**
   - Check compatibility rules
   - Use schema validation before registration
   - Plan evolution path carefully

3. **Deserialization errors**
   - Verify schema versions match
   - Check for corrupted data
   - Validate schema registry health

4. **Performance issues**
   - Monitor schema caching
   - Check network latency to Schema Registry
   - Optimize schema design

## Key Takeaways

1. **Schema management is crucial** for data governance and evolution
2. **Avro provides excellent schema evolution** capabilities
3. **Schema Registry centralizes** schema management and compatibility
4. **Forward/backward compatibility** enables smooth schema evolution
5. **Type safety and performance** are major benefits of Avro
6. **Plan for evolution** from the beginning of your schema design

## Next Steps

Tomorrow we'll explore:
- Kafka Connect for data integration
- Source and sink connectors
- Custom connector development
- Data pipeline patterns

---

**🚀 Ready for Day 7?** Continue with [Day 7: Kafka Connect](./day07-connect.md)