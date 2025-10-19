# Day 6 Exercises: Schema Management with Avro

> **Learning Tracks:** These exercises focus on Avro serialization and Schema Registry - platform-agnostic skills. Data engineers should complete all exercises.

## Data Engineer Track Exercises (Recommended)

### Exercise 1: Basic Avro Implementation - Schema Definition & Code Gen

### Objective
Learn Avro schema definition, code generation, and basic producer/consumer implementation.

### Setup
```bash
# Make sure Schema Registry is running
# Schema Registry starts automatically with docker-compose up -d

# Create Avro topic
docker-compose exec kafka kafka-topics --create --topic avro-exercises --
  --partitions 3 \
  --replication-factor 1

# Generate Avro classes from schema
mvn avro:schema
```

### Part A: Run Avro Examples

1. **Start Avro Producer**
   ```bash
   mvn exec:java -Dexec.mainClass="com.training.kafka.Day06Schemas.AvroProducer"
   ```

2. **Start Avro Consumer**
   ```bash
   # In another terminal
   mvn exec:java -Dexec.mainClass="com.training.kafka.Day06Schemas.AvroConsumer"
   ```

3. **Observe Schema Registry**
   ```bash
   # Check registered schemas
   curl http://localhost:8081/subjects
   
   # Get specific schema
   curl http://localhost:8081/subjects/avro-user-events-value/versions/latest
   ```

### Expected Results
- Type-safe message serialization/deserialization
- Schema automatically registered in Schema Registry
- Backward compatibility maintained

### Questions to Answer
1. How does Avro serialization differ from JSON?
2. What happens when you try to send invalid data?
3. How does Schema Registry prevent incompatible changes?

---

### Exercise 2: Schema Evolution - Compatibility Testing

### Objective
Understand schema evolution patterns and compatibility testing.

### Part A: Adding Optional Fields (Backward Compatible)

1. **Modify the User Event Schema**
   Create `src/main/resources/schemas/user-event-v2.avsc`:
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
       {"name": "deviceInfo", "type": ["null", {"type": "record", "name": "DeviceInfo", "fields": [{"name": "deviceType", "type": "string"}, {"name": "userAgent", "type": "string"}, {"name": "ipAddress", "type": "string"}, {"name": "location", "type": ["null", "string"], "default": null}]}], "default": null},
       {"name": "version", "type": "int", "default": 1},
       {"name": "experimentId", "type": ["null", "string"], "default": null},
       {"name": "campaignId", "type": ["null", "string"], "default": null}
     ]
   }
   ```

2. **Test Compatibility**
   ```bash
   # Check compatibility before registering
   curl -X POST \
     -H "Content-Type: application/vnd.schemaregistry.v1+json" \
     --data @schema-v2.json \
     http://localhost:8081/compatibility/subjects/avro-user-events-value/versions/latest
   ```

3. **Generate New Classes and Test**
   ```bash
   mvn avro:schema
   mvn exec:java -Dexec.mainClass="com.training.kafka.Day06Schemas.AvroProducer"
   ```

### Part B: Breaking Changes (Forward Compatible)

1. **Test Adding Required Field** 
   ```json
   {
     "name": "requiredField",
     "type": "string"
   }
   ```

2. **Test Removing Field**
   ```bash
   # This should fail compatibility check
   curl -X POST \
     -H "Content-Type: application/vnd.schemaregistry.v1+json" \
     --data @breaking-schema.json \
     http://localhost:8081/compatibility/subjects/avro-user-events-value/versions/latest
   ```

### Expected Results
- Optional fields can be added (backward compatible)
- Required fields cannot be added without defaults
- Field removal may break compatibility
- Understanding of evolution strategies

---

### Exercise 3: Schema Registry Management - CLI & REST API

### Objective
Learn to manage schemas programmatically and understand compatibility levels.

### Implementation

1. **Schema Registry Client Usage**
   ```java
   public class SchemaRegistryManager {
       private final CachedSchemaRegistryClient schemaRegistry;
       
       public SchemaRegistryManager(String url) {
           this.schemaRegistry = new CachedSchemaRegistryClient(url, 100);
       }
       
       public void registerSchema(String subject, String schemaString) throws Exception {
           Schema.Parser parser = new Schema.Parser();
           Schema schema = parser.parse(schemaString);
           schemaRegistry.register(subject, schema);
           System.out.println("Schema registered for subject: " + subject);
       }
       
       public void checkCompatibility(String subject, String schemaString) throws Exception {
           Schema.Parser parser = new Schema.Parser();
           Schema schema = parser.parse(schemaString);
           boolean compatible = schemaRegistry.testCompatibility(subject, schema);
           System.out.println("Schema compatibility: " + compatible);
       }
       
       public void listSubjects() throws Exception {
           Collection<String> subjects = schemaRegistry.getAllSubjects();
           System.out.println("Registered subjects: " + subjects);
       }
   }
   ```

2. **Set Compatibility Levels**
   ```bash
   # Set global compatibility
   curl -X PUT \
     -H "Content-Type: application/vnd.schemaregistry.v1+json" \
     --data '{"compatibility": "BACKWARD"}' \
     http://localhost:8081/config
   
   # Set subject-specific compatibility
   curl -X PUT \
     -H "Content-Type: application/vnd.schemaregistry.v1+json" \
     --data '{"compatibility": "FULL"}' \
     http://localhost:8081/config/avro-user-events-value
   ```

### Expected Results
- Programmatic schema management
- Understanding of compatibility levels
- Schema versioning strategies

---

## Exercise 4: Multiple Schema Formats

### Objective
Work with different schema formats and complex nested structures.

### Implementation

1. **Create Complex Schema**
   ```json
   {
     "type": "record",
     "name": "OrderEvent",
     "namespace": "com.training.kafka.avro",
     "fields": [
       {"name": "orderId", "type": "string"},
       {"name": "customerId", "type": "string"},
       {"name": "orderDate", "type": "long", "logicalType": "timestamp-millis"},
       {"name": "items", "type": {"type": "array", "items": {
         "type": "record",
         "name": "OrderItem",
         "fields": [
           {"name": "productId", "type": "string"},
           {"name": "quantity", "type": "int"},
           {"name": "price", "type": {"type": "bytes", "logicalType": "decimal", "precision": 10, "scale": 2}}
         ]
       }}},
       {"name": "shippingAddress", "type": {
         "type": "record",
         "name": "Address",
         "fields": [
           {"name": "street", "type": "string"},
           {"name": "city", "type": "string"},
           {"name": "zipCode", "type": "string"},
           {"name": "country", "type": "string"}
         ]
       }},
       {"name": "status", "type": {"type": "enum", "name": "OrderStatus", "symbols": ["PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED"]}}
     ]
   }
   ```

2. **Implement Complex Producer/Consumer**
   ```java
   public class ComplexAvroExample {
       public void sendOrderEvent() {
           // Create order items
           List<OrderItem> items = Arrays.asList(
               OrderItem.newBuilder()
                   .setProductId("prod-123")
                   .setQuantity(2)
                   .setPrice(ByteBuffer.wrap(new BigDecimal("29.99").unscaledValue().toByteArray()))
                   .build()
           );
           
           // Create address
           Address address = Address.newBuilder()
               .setStreet("123 Main St")
               .setCity("San Francisco")
               .setZipCode("94105")
               .setCountry("USA")
               .build();
           
           // Create order event
           OrderEvent order = OrderEvent.newBuilder()
               .setOrderId("order-" + System.currentTimeMillis())
               .setCustomerId("customer-123")
               .setOrderDate(Instant.now().toEpochMilli())
               .setItems(items)
               .setShippingAddress(address)
               .setStatus(OrderStatus.PENDING)
               .build();
           
           producer.send(new ProducerRecord<>("order-events", order.getOrderId(), order));
       }
   }
   ```

### Expected Results
- Complex nested Avro structures
- Logical types (decimals, timestamps)
- Arrays and maps in Avro
- Real-world schema design patterns

---

## Exercise 5: Schema Evolution Strategies

### Objective
Implement production-ready schema evolution patterns.

### Part A: Gradual Migration Strategy

1. **Version 1: Initial Schema**
   ```bash
   # Deploy v1 producers and consumers
   mvn exec:java -Dexec.mainClass="SchemaV1Producer"
   mvn exec:java -Dexec.mainClass="SchemaV1Consumer"
   ```

2. **Version 2: Add Optional Fields**
   ```bash
   # Deploy v2 producers (can write v2, v1 consumers can still read)
   mvn exec:java -Dexec.mainClass="SchemaV2Producer"
   # v1 consumers still work
   ```

3. **Version 3: Migrate Consumers**
   ```bash
   # Deploy v2 consumers (can read v1 and v2)
   mvn exec:java -Dexec.mainClass="SchemaV2Consumer"
   # Now all components support v2
   ```

### Part B: Schema Evolution Best Practices

1. **Compatibility Testing Pipeline**
   ```java
   @Test
   public void testSchemaCompatibility() throws Exception {
       String oldSchema = loadSchema("user-event-v1.avsc");
       String newSchema = loadSchema("user-event-v2.avsc");
       
       CachedSchemaRegistryClient client = new CachedSchemaRegistryClient("http://localhost:8081", 100);
       
       // Test backward compatibility
       boolean backward = client.testCompatibility("test-subject", new Schema.Parser().parse(newSchema));
       assertTrue("New schema should be backward compatible", backward);
       
       // Test forward compatibility
       // Register new schema first, then test old against it
       client.register("test-subject", new Schema.Parser().parse(newSchema));
       boolean forward = client.testCompatibility("test-subject", new Schema.Parser().parse(oldSchema));
       assertTrue("Old schema should be forward compatible", forward);
   }
   ```

### Expected Results
- Production deployment strategies
- Zero-downtime schema evolution
- Automated compatibility testing

---

## Solutions and Analysis

### Exercise 1 Solution: Avro Benefits
```java
// Benefits observed:
// 1. Type safety at compile time
// 2. Schema validation prevents bad data
// 3. Compact binary serialization
// 4. Built-in schema evolution support
// 5. Cross-language compatibility
```

### Exercise 2 Solution: Evolution Patterns
```java
// Safe changes (backward compatible):
// - Add optional fields with defaults
// - Remove optional fields
// - Add enum values (at end)

// Breaking changes:
// - Remove required fields
// - Change field types
// - Rename fields
// - Reorder enum values
```

### Exercise 3 Solution: Schema Registry
```bash
# Compatibility levels:
# BACKWARD: New schema can read old data
# FORWARD: Old schema can read new data  
# FULL: Both backward and forward
# NONE: No compatibility checking
```

## Common Issues and Solutions

### Issue 1: Schema Not Found
**Problem**: `SchemaNotFoundException` when consuming
**Solution**:
```java
// Ensure schema is registered before consuming
// Check Schema Registry connectivity
// Verify subject naming convention
```

### Issue 2: Incompatible Schema
**Problem**: Schema evolution breaks compatibility
**Solution**:
```java
// Always test compatibility before deployment
// Use optional fields with defaults
// Plan evolution path carefully
```

### Issue 3: Performance Issues
**Problem**: Slow serialization/deserialization
**Solution**:
```java
// Enable schema caching
props.put("schema.registry.cache.capacity", "1000");
// Use specific readers when possible
props.put("specific.avro.reader", "true");
```

## Java Developer Track Exercises (Optional)

> **Java Developer Track Only**
>
> Avro is already platform-agnostic. Spring Boot can add convenience features:

### Spring Boot Avro Integration

1. **Spring Kafka Avro Serializer Configuration**
    ```bash
    # Review Spring configuration for Avro
    cat src/main/resources/application.yml | grep -A 10 avro
    ```

2. **Auto-Configuration Benefits**
    - Schema Registry client auto-configured
    - Serializer/deserializer beans
    - Properties from application.yml

3. **Service Integration**
    ```bash
    # Review Spring Boot Avro service
    cat src/main/java/com/training/kafka/services/Day06SchemaService.java
    ```

**Note:** Avro serialization and Schema Registry are platform-agnostic. Spring Boot mainly simplifies configuration. The core Avro knowledge from Data Engineer track is essential.

---

## Key Learning Outcomes

After completing the Data Engineer track exercises, you should understand:

1. **Schema Definition**: Avro schema syntax and data types
2. **Code Generation**: Maven plugin integration
3. **Schema Evolution**: Compatible and breaking changes
4. **Schema Registry**: Centralized schema management
5. **Compatibility Levels**: BACKWARD, FORWARD, FULL, NONE
6. **Production Patterns**: Deployment and migration strategies
7. **Performance**: Optimization techniques
8. **Testing**: Schema compatibility validation

## Cleanup
```bash
# Clean up topics
docker-compose exec kafka kafka-topics --delete --topic avro-exercises --bootstrap-server localhost:9092

# Clean up schemas (optional)
curl -X DELETE http://localhost:8081/subjects/avro-user-events-value
```

---

**Next**: [Day 7 Exercises: Kafka Connect](./day07-exercises.md)
