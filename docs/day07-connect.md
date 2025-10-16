# Day 7: Kafka Connect Integration

## Learning Objectives
By the end of Day 7, you will:
- Understand Kafka Connect architecture and concepts
- Deploy and manage source and sink connectors
- Configure connector transformations and error handling
- Build data integration pipelines
- Monitor connector performance and health
- Troubleshoot common connector issues

## 🚀 Quick Start with Spring Boot

This section shows you how to use the Spring Boot implementation of Day 7 concepts. The exercises can run using our web-based training interface or command-line tools.

### Prerequisites
```bash
# Start Docker Compose (includes Kafka, PostgreSQL, and Kafka Connect)
docker-compose up -d

# Wait for services to be ready
docker-compose ps  # All services should show "healthy"

# Start Spring Boot application
mvn spring-boot:run
```

### Web API Quick Start

#### 1. Run Complete Day 7 Demo
```bash
# Execute full Kafka Connect demonstration
curl -X POST http://localhost:8080/api/training/day07/demo

# Response:
{
  "status": "success",
  "message": "Day 7 Kafka Connect demonstration completed successfully",
  "module": "Day07Connect",
  "info": "JDBC source and sink connectors created for EventMart data integration"
}
```

#### 2. Get Kafka Connect Cluster Information
```bash
# Check Kafka Connect cluster status
curl http://localhost:8080/api/training/day07/connect/info

# Response:
{
  "status": "success",
  "clusterInfo": {
    "version": "7.7.0",
    "commit": "...",
    "kafka_cluster_id": "..."
  }
}
```

#### 3. List Available Connector Plugins
```bash
# See all available connectors
curl http://localhost:8080/api/training/day07/connectors/plugins

# Response shows JDBC, File, and other connector types
{
  "status": "success",
  "count": 8,
  "plugins": [
    {
      "class": "io.confluent.connect.jdbc.JdbcSourceConnector",
      "version": "10.7.4"
    },
    ...
  ]
}
```

#### 4. Create JDBC Source Connector
```bash
# Create User Activity Log source connector (PostgreSQL → Kafka)
curl -X POST http://localhost:8080/api/training/day07/connectors/create/user-activity-source

# Create Product Events source connector
curl -X POST http://localhost:8080/api/training/day07/connectors/create/product-events-source

# Response:
{
  "status": "success",
  "message": "Connector created successfully",
  "connector": {...}
}
```

#### 5. Create JDBC Sink Connector
```bash
# Create Order Events sink connector (Kafka → PostgreSQL)
curl -X POST http://localhost:8080/api/training/day07/connectors/create/order-events-sink

# This connector consumes from "order-events-avro" topic
# and writes to PostgreSQL "orders" table
```

#### 6. List All Connectors
```bash
# See all active connectors
curl http://localhost:8080/api/training/day07/connectors/list

# Response:
{
  "status": "success",
  "count": 3,
  "connectors": [
    "user-activity-source",
    "product-events-source",
    "order-events-sink"
  ]
}
```

#### 7. Check Connector Status
```bash
# Get detailed status of a connector
curl http://localhost:8080/api/training/day07/connectors/user-activity-source/status

# Response shows connector state and task information:
{
  "status": "success",
  "connectorStatus": {
    "name": "user-activity-source",
    "connector": {
      "state": "RUNNING",
      "worker_id": "kafka-connect:8083"
    },
    "tasks": [
      {
        "id": 0,
        "state": "RUNNING",
        "worker_id": "kafka-connect:8083"
      }
    ]
  }
}
```

#### 8. Connector Lifecycle Management
```bash
# Pause a connector
curl -X POST http://localhost:8080/api/training/day07/connectors/user-activity-source/pause

# Resume a connector
curl -X POST http://localhost:8080/api/training/day07/connectors/user-activity-source/resume

# Restart a connector
curl -X POST http://localhost:8080/api/training/day07/connectors/user-activity-source/restart

# Delete a connector
curl -X DELETE http://localhost:8080/api/training/day07/connectors/user-activity-source/delete
```

### Service Layer Usage

Day 7 functionality is also available through `Day07ConnectService`:

```java
@Autowired
private Day07ConnectService connectService;

// Run full demonstration
connectService.demonstrateKafkaConnect();

// Get cluster information
Map<String, Object> clusterInfo = connectService.getConnectClusterInfo();

// List all connectors
Map<String, Object> connectors = connectService.listConnectors();

// Create source connector
Map<String, Object> result = connectService.createUserActivitySourceConnector();

// Check connector status
Map<String, Object> status = connectService.checkConnectorStatus("user-activity-source");

// Lifecycle management
connectService.pauseConnector("user-activity-source");
connectService.resumeConnector("user-activity-source");
connectService.restartConnector("user-activity-source");
connectService.deleteConnector("user-activity-source");
```

### Understanding the Data Flow

#### Source Connector Flow (PostgreSQL → Kafka)
```
PostgreSQL               Kafka Connect          Kafka Topics
┌─────────────┐        ┌──────────────┐       ┌─────────────────┐
│ EventMart   │        │ JDBC Source  │       │ jdbc-user_      │
│ Database    │ ─────> │ Connector    │ ────> │ activity_log    │
│             │        │              │       │                 │
│ - users     │        │ Polls every  │       │ jdbc-product_   │
│ - products  │        │ 5 seconds    │       │ events          │
│ - orders    │        │              │       │                 │
└─────────────┘        └──────────────┘       └─────────────────┘
```

#### Sink Connector Flow (Kafka → PostgreSQL)
```
Kafka Topics            Kafka Connect          PostgreSQL
┌─────────────────┐    ┌──────────────┐       ┌─────────────┐
│ order-events-   │    │ JDBC Sink    │       │ EventMart   │
│ avro            │ ─> │ Connector    │ ────> │ Database    │
│                 │    │              │       │             │
│ (Avro format)   │    │ Consumes and │       │ orders      │
│                 │    │ upserts      │       │ table       │
└─────────────────┘    └──────────────┘       └─────────────┘
```

### EventMart Database Schema

The PostgreSQL database includes 6 tables for comprehensive testing:

1. **users** - User accounts and profiles
2. **products** - Product catalog with pricing and inventory
3. **orders** - Customer orders with items and shipping
4. **payments** - Payment transactions and processing
5. **user_activity_log** - CDC source for user activity events
6. **product_events** - CDC source for product inventory changes

All tables include sample data for immediate testing!

### Verifying Data Flow

#### Check Source Connector Data
```bash
# Consume from JDBC source topics
docker exec kafka-training-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic jdbc-user_activity_log \
  --from-beginning

# You should see user activity records from PostgreSQL
```

#### Check Sink Connector Data
```bash
# Produce test data to order-events-avro topic
# (Use Day06SchemaService to produce Avro-formatted order events)

# Then check PostgreSQL for new records
docker exec kafka-training-postgres psql -U eventmart -d eventmart \
  -c "SELECT * FROM orders ORDER BY created_at DESC LIMIT 5;"
```

### Testing with Kafka UI

The Docker Compose setup includes Kafka UI at http://localhost:8080 where you can:

1. **Monitor Topics**: See jdbc-user_activity_log and jdbc-product_events topics
2. **View Messages**: Inspect records flowing through source connectors
3. **Check Connect**: View all connectors and their status
4. **Monitor Schemas**: See Avro schemas registered for sink connectors

### Monitoring Best Practices

1. **Check Connector Health**
   ```bash
   # Monitor connector status regularly
   curl http://localhost:8080/api/training/day07/connectors/user-activity-source/status
   ```

2. **Watch Kafka Topics**
   - Monitor topic lag for sink connectors
   - Check partition distribution
   - Verify message throughput

3. **Database Monitoring**
   ```bash
   # Check PostgreSQL activity
   docker exec kafka-training-postgres psql -U eventmart -d eventmart \
     -c "SELECT COUNT(*) FROM user_activity_log;"
   ```

4. **Error Handling**
   - Check connector logs for failures
   - Monitor dead letter queue topics
   - Review task status for failures

### Common Patterns

#### Pattern 1: Database CDC with JDBC Source
```java
// Poll PostgreSQL tables every 5 seconds
// Use incrementing ID column for tracking
// Produce to Kafka with "jdbc-" topic prefix
Map<String, Object> result = connectService.createUserActivitySourceConnector();
```

#### Pattern 2: Data Warehouse Sink with JDBC Sink
```java
// Consume Avro-formatted events from Kafka
// Upsert into PostgreSQL based on record key
// Auto-evolve schema with new fields
Map<String, Object> result = connectService.createOrderEventsSinkConnector();
```

#### Pattern 3: Real-time Data Pipeline
```
1. Application writes to PostgreSQL (OLTP)
2. JDBC Source connector streams changes to Kafka
3. Kafka Streams processes and enriches data
4. JDBC Sink connector writes to analytics database (OLAP)
```

### Troubleshooting Tips

#### Issue: Connector Not Starting
```bash
# Check connector status
curl http://localhost:8080/api/training/day07/connectors/my-connector/status

# Common causes:
# - PostgreSQL not ready (check health)
# - Invalid configuration (check logs)
# - Network connectivity (verify docker network)
```

#### Issue: No Data Flowing
```bash
# For source connectors:
# 1. Verify PostgreSQL has data
docker exec kafka-training-postgres psql -U eventmart -d eventmart \
  -c "SELECT COUNT(*) FROM user_activity_log;"

# 2. Check connector is polling
curl http://localhost:8080/api/training/day07/connectors/user-activity-source/status

# 3. Verify Kafka topics exist
docker exec kafka-training-kafka kafka-topics --list --bootstrap-server localhost:9092
```

#### Issue: Sink Connector Failing
```bash
# Check for schema mismatches
# Verify Avro schema compatibility
# Review PostgreSQL table structure
# Check for data type conversions
```

### Next Steps

After completing the Quick Start:
1. Read the full documentation below for architecture details
2. Experiment with custom connector configurations
3. Try the advanced SMT (Single Message Transform) examples
4. Implement error handling and dead letter queues
5. Set up monitoring and alerting

---

## Morning Session (3 hours): Kafka Connect Fundamentals

### 1. What is Kafka Connect?

**Kafka Connect** is a framework for connecting Kafka with external systems:
- **Scalable**: Distributed mode for production workloads
- **Fault Tolerant**: Automatic restart and recovery
- **Declarative**: JSON configuration instead of code
- **Pluggable**: Extensive ecosystem of connectors

### 2. Core Concepts

#### Source Connectors
- Import data **INTO** Kafka from external systems
- Examples: Database → Kafka, File → Kafka, API → Kafka

#### Sink Connectors  
- Export data **FROM** Kafka to external systems
- Examples: Kafka → Database, Kafka → S3, Kafka → Elasticsearch

#### Workers
- **Standalone**: Single process (development/testing)
- **Distributed**: Cluster of workers (production)

#### Tasks
- Units of work that actually move data
- Connectors can create multiple tasks for parallelism

### 3. Connect Architecture

```
External System ←→ Connector ←→ Tasks ←→ Workers ←→ Kafka
```

## Afternoon Session (3 hours): Hands-on Integration

### Exercise 1: Connector Management

Run the Connector Manager example:

```bash
# Start Kafka Connect (if using standalone)
confluent local connect start

# Run Connector Manager
mvn exec:java -Dexec.mainClass="com.training.kafka.Day07Connect.ConnectorManager"
```

The example demonstrates:
- Creating file source/sink connectors
- Managing connector lifecycle
- Monitoring connector status
- REST API operations

### Exercise 2: File Source Connector

#### Setup Data Source
```bash
# Create source data file
mkdir -p /tmp/kafka-connect-data
echo "line1,data1,value1" > /tmp/kafka-connect-data/source.txt
echo "line2,data2,value2" >> /tmp/kafka-connect-data/source.txt
echo "line3,data3,value3" >> /tmp/kafka-connect-data/source.txt
```

#### Create Source Connector
```bash
curl -X POST \
  -H "Content-Type: application/json" \
  --data '{
    "name": "file-source-demo",
    "config": {
      "connector.class": "org.apache.kafka.connect.file.FileStreamSourceConnector",
      "tasks.max": "1",
      "file": "/tmp/kafka-connect-data/source.txt",
      "topic": "file-source-topic",
      "key.converter": "org.apache.kafka.connect.storage.StringConverter",
      "value.converter": "org.apache.kafka.connect.storage.StringConverter"
    }
  }' \
  http://localhost:8083/connectors
```

#### Verify Data Flow
```bash
# Check topic has data
confluent local kafka topic consume file-source-topic --from-beginning
```

### Exercise 3: File Sink Connector

#### Create Sink Connector
```bash
curl -X POST \
  -H "Content-Type: application/json" \
  --data '{
    "name": "file-sink-demo",
    "config": {
      "connector.class": "org.apache.kafka.connect.file.FileStreamSinkConnector",
      "tasks.max": "1",
      "file": "/tmp/kafka-connect-data/sink.txt",
      "topics": "user-events",
      "key.converter": "org.apache.kafka.connect.storage.StringConverter",
      "value.converter": "org.apache.kafka.connect.storage.StringConverter"
    }
  }' \
  http://localhost:8083/connectors
```

#### Generate Test Data
```bash
# Send data to user-events topic
echo "user1,login,2024-01-01" | confluent local kafka topic produce user-events
echo "user2,purchase,2024-01-01" | confluent local kafka topic produce user-events
```

#### Verify Sink Output
```bash
# Check sink file has data
cat /tmp/kafka-connect-data/sink.txt
```

### Exercise 4: JDBC Connectors (Advanced)

#### JDBC Source Configuration
```json
{
  "name": "jdbc-source-users",
  "config": {
    "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector",
    "tasks.max": "1",
    "connection.url": "jdbc:postgresql://localhost:5432/ecommerce",
    "connection.user": "kafka_user",
    "connection.password": "kafka_pass",
    "mode": "incrementing",
    "incrementing.column.name": "id",
    "table.whitelist": "users",
    "topic.prefix": "db-",
    "poll.interval.ms": "10000",
    "key.converter": "org.apache.kafka.connect.json.JsonConverter",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "key.converter.schemas.enable": "false",
    "value.converter.schemas.enable": "false"
  }
}
```

#### JDBC Sink Configuration
```json
{
  "name": "jdbc-sink-analytics",
  "config": {
    "connector.class": "io.confluent.connect.jdbc.JdbcSinkConnector",
    "tasks.max": "1",
    "connection.url": "jdbc:postgresql://localhost:5432/analytics",
    "connection.user": "kafka_user", 
    "connection.password": "kafka_pass",
    "topics": "processed-events",
    "table.name.format": "kafka_${topic}",
    "insert.mode": "upsert",
    "pk.mode": "record_key",
    "pk.fields": "user_id",
    "auto.create": "true",
    "auto.evolve": "true",
    "key.converter": "org.apache.kafka.connect.json.JsonConverter",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "key.converter.schemas.enable": "false",
    "value.converter.schemas.enable": "false"
  }
}
```

## Advanced Connector Features

### 1. Single Message Transforms (SMTs)

#### Extract Field from JSON
```json
{
  "transforms": "extractUserId",
  "transforms.extractUserId.type": "org.apache.kafka.connect.transforms.ExtractField$Value",
  "transforms.extractUserId.field": "user_id"
}
```

#### Add Timestamp
```json
{
  "transforms": "addTimestamp",
  "transforms.addTimestamp.type": "org.apache.kafka.connect.transforms.TimestampConverter$Value",
  "transforms.addTimestamp.field": "created_at",
  "transforms.addTimestamp.format": "yyyy-MM-dd HH:mm:ss",
  "transforms.addTimestamp.target.type": "Timestamp"
}
```

#### Route Messages
```json
{
  "transforms": "route",
  "transforms.route.type": "org.apache.kafka.connect.transforms.RegexRouter",
  "transforms.route.regex": "([^.]+)\\..*",
  "transforms.route.replacement": "$1-processed"
}
```

#### Chain Multiple Transforms
```json
{
  "transforms": "extractField,addTimestamp,route",
  "transforms.extractField.type": "org.apache.kafka.connect.transforms.ExtractField$Value",
  "transforms.extractField.field": "payload",
  "transforms.addTimestamp.type": "org.apache.kafka.connect.transforms.InsertField$Value",
  "transforms.addTimestamp.timestamp.field": "processed_at",
  "transforms.route.type": "org.apache.kafka.connect.transforms.RegexRouter",
  "transforms.route.regex": "raw-(.*)",
  "transforms.route.replacement": "processed-$1"
}
```

### 2. Error Handling and Dead Letter Queues

#### Configure Error Tolerance
```json
{
  "errors.tolerance": "all",
  "errors.log.enable": "true",
  "errors.log.include.messages": "true",
  "errors.deadletterqueue.topic.name": "connect-dlq",
  "errors.deadletterqueue.topic.replication.factor": "1",
  "errors.deadletterqueue.context.headers.enable": "true"
}
```

#### Custom Error Handling
```java
public class CustomErrorHandler implements ErrorReporter {
    
    @Override
    public void report(ProcessingContext context, ConnectRecord<?> record, Throwable error) {
        logger.error("Error processing record: topic={}, partition={}, offset={}", 
            record.topic(), record.kafkaPartition(), record.kafkaOffset(), error);
        
        // Send to monitoring system
        sendToMonitoring(record, error);
        
        // Store in external error tracking system
        storeInErrorTracking(record, error);
    }
}
```

### 3. Custom Converters

#### Avro Converter
```json
{
  "key.converter": "io.confluent.connect.avro.AvroConverter",
  "key.converter.schema.registry.url": "http://localhost:8081",
  "value.converter": "io.confluent.connect.avro.AvroConverter",
  "value.converter.schema.registry.url": "http://localhost:8081"
}
```

#### Custom JSON Converter
```java
public class CustomJsonConverter implements Converter {
    
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // Configure converter
    }
    
    @Override
    public byte[] fromConnectData(String topic, Schema schema, Object value) {
        // Convert Connect data to bytes
        return customSerializer.serialize(value);
    }
    
    @Override
    public SchemaAndValue toConnectData(String topic, byte[] value) {
        // Convert bytes to Connect data
        Object data = customDeserializer.deserialize(value);
        return new SchemaAndValue(null, data);
    }
}
```

## Monitoring and Management

### 1. REST API Operations

```bash
# List all connectors
curl http://localhost:8083/connectors

# Get connector status
curl http://localhost:8083/connectors/my-connector/status

# Get connector configuration
curl http://localhost:8083/connectors/my-connector/config

# Pause connector
curl -X PUT http://localhost:8083/connectors/my-connector/pause

# Resume connector
curl -X PUT http://localhost:8083/connectors/my-connector/resume

# Restart connector
curl -X POST http://localhost:8083/connectors/my-connector/restart

# Restart specific task
curl -X POST http://localhost:8083/connectors/my-connector/tasks/0/restart

# Delete connector
curl -X DELETE http://localhost:8083/connectors/my-connector
```

### 2. Monitoring Metrics

#### JMX Metrics
```java
// Key metrics to monitor:
// kafka.connect:type=connector-metrics,connector="{connector}"
// - connector-startup-attempts-total
// - connector-startup-failure-total
// - connector-startup-success-total

// kafka.connect:type=task-metrics,connector="{connector}",task="{task}"
// - task-startup-attempts-total
// - task-startup-failure-total
// - task-startup-success-total

// kafka.connect:type=source-task-metrics,connector="{connector}",task="{task}"
// - source-record-poll-rate
// - source-record-write-rate

// kafka.connect:type=sink-task-metrics,connector="{connector}",task="{task}"
// - sink-record-read-rate
// - sink-record-send-rate
```

#### Health Check Implementation
```java
public class ConnectorHealthCheck {
    
    public HealthStatus checkConnectorHealth(String connectorName) {
        try {
            ConnectorStatus status = getConnectorStatus(connectorName);
            
            if (!"RUNNING".equals(status.getState())) {
                return HealthStatus.UNHEALTHY("Connector not running: " + status.getState());
            }
            
            // Check task health
            for (TaskStatus task : status.getTasks()) {
                if (!"RUNNING".equals(task.getState())) {
                    return HealthStatus.UNHEALTHY("Task not running: " + task.getState());
                }
            }
            
            // Check lag (for source connectors)
            long lag = calculateSourceLag(connectorName);
            if (lag > 10000) {
                return HealthStatus.DEGRADED("High lag: " + lag + " records");
            }
            
            return HealthStatus.HEALTHY();
            
        } catch (Exception e) {
            return HealthStatus.UNHEALTHY("Health check failed: " + e.getMessage());
        }
    }
}
```

## Production Best Practices

### 1. Distributed Mode Configuration
```properties
# Worker configuration (connect-distributed.properties)
bootstrap.servers=broker1:9092,broker2:9092,broker3:9092
group.id=connect-cluster

# Kafka topics for storing connector and task configs
config.storage.topic=connect-configs
config.storage.replication.factor=3

# Kafka topic for storing offset data
offset.storage.topic=connect-offsets
offset.storage.replication.factor=3
offset.storage.partitions=25

# Kafka topic for storing status data
status.storage.topic=connect-status
status.storage.replication.factor=3
status.storage.partitions=5

# Converter settings
key.converter=org.apache.kafka.connect.json.JsonConverter
value.converter=org.apache.kafka.connect.json.JsonConverter
key.converter.schemas.enable=false
value.converter.schemas.enable=false

# Worker settings
rest.port=8083
rest.advertised.host.name=connect-worker-1
```

### 2. Security Configuration
```properties
# SSL configuration
security.protocol=SSL
ssl.truststore.location=/path/to/truststore.jks
ssl.truststore.password=truststore-password
ssl.keystore.location=/path/to/keystore.jks
ssl.keystore.password=keystore-password

# SASL configuration
security.protocol=SASL_SSL
sasl.mechanism=SCRAM-SHA-512
sasl.jaas.config=org.apache.kafka.common.security.scram.ScramLoginModule required \
  username="connect-user" password="connect-password";

# Producer/Consumer overrides
producer.security.protocol=SASL_SSL
consumer.security.protocol=SASL_SSL
```

### 3. Performance Tuning
```json
{
  "config": {
    "tasks.max": "8",
    "batch.size": "2000",
    "poll.interval.ms": "5000",
    "flush.timeout.ms": "10000",
    "max.retries": "3",
    "retry.backoff.ms": "1000"
  }
}
```

## Troubleshooting Guide

### 1. Common Issues

#### Connector Won't Start
```bash
# Check logs
docker logs kafka-connect

# Check connector status
curl http://localhost:8083/connectors/my-connector/status

# Common causes:
# - Invalid configuration
# - Missing dependencies
# - Network connectivity issues
# - Insufficient permissions
```

#### Tasks Failing
```bash
# Restart failed tasks
curl -X POST http://localhost:8083/connectors/my-connector/tasks/0/restart

# Check task configuration
curl http://localhost:8083/connectors/my-connector/tasks/0/status

# Common causes:
# - Target system unavailable
# - Schema conflicts
# - Data format issues
# - Resource constraints
```

#### Performance Issues
```bash
# Monitor task metrics
# Check batch sizes and poll intervals
# Verify network connectivity
# Review resource usage (CPU, memory, disk)

# Scale horizontally
# Increase tasks.max
# Add more worker nodes
```

### 2. Debugging Tools

#### Log Analysis
```bash
# Enable debug logging
log4j.logger.org.apache.kafka.connect=DEBUG

# Common log patterns to look for:
# - "Starting connector"
# - "Task finished initialization" 
# - "Finished creating/updating"
# - "Error in task"
```

#### Metrics Collection
```java
// Collect key metrics
public class ConnectorMetricsCollector {
    public void collectMetrics() {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        
        // Connector metrics
        ObjectName connectorPattern = new ObjectName("kafka.connect:type=connector-metrics,connector=*");
        Set<ObjectName> connectorMBeans = server.queryNames(connectorPattern, null);
        
        for (ObjectName mbean : connectorMBeans) {
            String connector = mbean.getKeyProperty("connector");
            Double startupRate = (Double) server.getAttribute(mbean, "connector-startup-success-total");
            logger.info("Connector {}: startup rate = {}", connector, startupRate);
        }
    }
}
```

## Key Takeaways

1. **Kafka Connect** simplifies data integration without custom code
2. **Source connectors** import data into Kafka from external systems
3. **Sink connectors** export data from Kafka to external systems
4. **Transforms** enable data modification during transit
5. **Error handling** ensures robust data pipelines
6. **Monitoring** is crucial for production deployments
7. **Distributed mode** provides scalability and fault tolerance

## Popular Connectors

| Connector | Type | Use Case |
|-----------|------|----------|
| JDBC | Source/Sink | Database integration |
| S3 | Sink | Data lake storage |
| Elasticsearch | Sink | Search and analytics |
| Debezium | Source | Change data capture |
| HDFS | Sink | Big data storage |
| MongoDB | Source/Sink | Document database |
| Salesforce | Source | CRM data integration |

## Next Steps

Tomorrow we'll explore:
- Advanced security configurations
- Production monitoring and alerting
- Performance optimization techniques
- Operational best practices

---

**🚀 Ready for Day 8?** Continue with [Day 8: Advanced Topics](./day08-advanced.md)