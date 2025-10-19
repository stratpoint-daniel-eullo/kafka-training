# Training API Endpoints

> **⚠️ Java Developer Track Only**
> REST API endpoints for Spring Boot web interface. Data engineers use CLI: `./bin/kafka-training-cli.sh` or pure Java examples.

Complete reference for all 40+ training REST API endpoints.

## Day 1: Foundation & Admin API

### List All Modules
```http
GET /api/training/modules
```

Returns all 8 training modules with descriptions and endpoints.

**Response:**
```json
{
  "Day01Foundation": {
    "name": "Foundation & Admin API",
    "description": "Topic management, AdminClient operations",
    "endpoints": ["/day01/demo", "/day01/topics", ...]
  }
}
```

### Run Day 1 Demo
```http
POST /api/training/day01/demo
```

Demonstrates topic creation, listing, and deletion.

### Create Topic
```http
POST /api/training/day01/create-topic?name=test-topic&partitions=3&replication=1
```

**Parameters:**
- `name` - Topic name (required)
- `partitions` - Number of partitions (default: 1)
- `replication` - Replication factor (default: 1)

### List Topics
```http
GET /api/training/day01/list-topics
```

Returns all Kafka topics.

### Describe Topic
```http
GET /api/training/day01/describe-topic/{topicName}
```

Returns detailed topic configuration.

### Delete Topic
```http
DELETE /api/training/day01/delete-topic/{topicName}
```

Deletes the specified topic.

## Day 2: Data Flow & Message Patterns

### Run Day 2 Demo
```http
POST /api/training/day02/demo
```

Demonstrates producer semantics and partitioning.

### Get Data Flow Concepts
```http
GET /api/training/day02/concepts
```

Returns concepts for producer/consumer patterns.

### Get Consumer Lag
```http
GET /api/training/day02/consumer-lag/{groupId}
```

Returns lag for all partitions in consumer group.

### Get Consumer Offsets
```http
GET /api/training/day02/offsets/{groupId}
```

Returns committed offsets for consumer group.

## Day 3: Producers

### Send Message Synchronously
```http
POST /api/training/day03/send-sync
Content-Type: application/json

{
  "topic": "user-events",
  "key": "user123",
  "value": "User logged in"
}
```

Sends message and waits for acknowledgment.

### Send Message Asynchronously
```http
POST /api/training/day03/send-async
Content-Type: application/json

{
  "topic": "user-events",
  "key": "user456",
  "value": "User registered"
}
```

### Send Batch Messages
```http
POST /api/training/day03/send-batch
Content-Type: application/json

{
  "topic": "user-events",
  "messages": ["msg1", "msg2", "msg3"],
  "count": 3
}
```

### Send Transactional Messages
```http
POST /api/training/day03/send-transactional
Content-Type: application/json

{
  "topic": "orders",
  "messages": ["order1", "order2"]
}
```

Sends messages in a transaction (all or nothing).

## Day 4: Consumers

### Start Auto-Commit Consumer
```http
POST /api/training/day04/consume-auto?topic=user-events&groupId=group1
```

Consumes with automatic offset commit.

### Start Manual-Commit Consumer
```http
POST /api/training/day04/consume-manual?topic=user-events&groupId=group2
```

Consumes with manual offset commit control.

### Demonstrate Seek Operations
```http
POST /api/training/day04/seek-demo?topic=user-events
```

Shows seeking to beginning, end, and specific offsets.

## Day 5: Schema Registry

### Register Avro Schema
```http
POST /api/training/day05/register-schema
Content-Type: application/json

{
  "subject": "user-value",
  "schema": "{...avro schema...}"
}
```

### Produce Avro Message
```http
POST /api/training/day05/produce-avro
Content-Type: application/json

{
  "topic": "users-avro",
  "user": {
    "id": "123",
    "name": "John Doe",
    "email": "john@example.com"
  }
}
```

### Consume Avro Messages
```http
POST /api/training/day05/consume-avro?topic=users-avro
```

### List All Schemas
```http
GET /api/training/day05/schemas
```

Returns all registered Avro schemas.

## Day 6: Streams Processing

### Run Stateless Demo
```http
POST /api/training/day06/stateless-demo
```

Demonstrates filter, map, and flatMap operations.

### Run Stateful Demo
```http
POST /api/training/day06/stateful-demo
```

Demonstrates aggregations with state stores.

### Run Windowing Demo
```http
POST /api/training/day06/windowing-demo
```

Shows tumbling and hopping windows.

## Day 7: Kafka Connect

### List Connectors
```http
GET /api/training/day07/connectors
```

Returns all deployed connectors.

### Create Source Connector
```http
POST /api/training/day07/create-source
Content-Type: application/json

{
  "name": "postgres-source",
  "config": {
    "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector",
    "connection.url": "jdbc:postgresql://postgres:5432/eventmart",
    ...
  }
}
```

### Create Sink Connector
```http
POST /api/training/day07/create-sink
```

Creates JDBC sink connector (Kafka → PostgreSQL).

### Delete Connector
```http
DELETE /api/training/day07/connector/{name}
```

## Day 8: Advanced Topics

### Get System Status
```http
GET /api/training/day08/status
```

Returns security and monitoring configuration.

### Get Cluster Metrics
```http
GET /api/training/day08/metrics/cluster
```

Returns Kafka cluster health metrics.

### Get Consumer Lag Metrics
```http
GET /api/training/day08/metrics/consumer-lag
```

Returns lag for all consumer groups with warnings.

### Get Security Configuration
```http
GET /api/training/day08/config/security
```

Returns SSL/TLS and SASL configurations.

### Get Production Configuration
```http
GET /api/training/day08/config/production
```

Returns production-ready producer/consumer configs.

## cURL Examples

### Create Topic
```bash
curl -X POST "http://localhost:8080/api/training/day01/create-topic?name=orders&partitions=10&replication=1"
```

### Send Message
```bash
curl -X POST http://localhost:8080/api/training/day03/send-sync \
  -H "Content-Type: application/json" \
  -d '{"topic":"orders","key":"order123","value":"New order"}'
```

### Get Consumer Lag
```bash
curl http://localhost:8080/api/training/day02/consumer-lag/my-consumer-group
```

## Response Codes

| Code | Meaning |
|------|---------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request (invalid parameters) |
| 404 | Not Found (topic/resource doesn't exist) |
| 500 | Internal Server Error |

## Next Steps

- [EventMart API](eventmart-api.md) - E-commerce specific endpoints
- [Actuator Endpoints](actuator.md) - Spring Boot management endpoints
- [Error Handling](errors.md) - Error codes and troubleshooting
