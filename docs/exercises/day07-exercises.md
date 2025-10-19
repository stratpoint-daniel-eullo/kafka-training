# Day 7 Exercises: Kafka Connect Integration

> **Learning Tracks:** These exercises focus on Kafka Connect REST API - platform-agnostic integration patterns. Data engineers should complete all exercises.

## Data Engineer Track Exercises (Recommended)

### Exercise 1: File Source and Sink Connectors - REST API

### Objective
Set up basic file-based data integration using Kafka Connect connectors.

### Setup
```bash
# Make sure Kafka Connect is running
# Kafka Connect starts automatically with docker-compose up -d

# Create data directories
mkdir -p /tmp/kafka-connect-data
mkdir -p /tmp/kafka-connect-output

# Create sample source data
echo "2024-01-01,user1,login,192.168.1.100" > /tmp/kafka-connect-data/user-activity.csv
echo "2024-01-01,user2,purchase,192.168.1.101" >> /tmp/kafka-connect-data/user-activity.csv
echo "2024-01-01,user1,logout,192.168.1.100" >> /tmp/kafka-connect-data/user-activity.csv
```

### Part A: File Source Connector

1. **Create File Source Connector**
   ```bash
   curl -X POST \
     -H "Content-Type: application/json" \
     --data '{
       "name": "file-source-connector",
       "config": {
         "connector.class": "org.apache.kafka.connect.file.FileStreamSourceConnector",
         "tasks.max": "1",
         "file": "/tmp/kafka-connect-data/user-activity.csv",
         "topic": "file-user-activity",
         "key.converter": "org.apache.kafka.connect.storage.StringConverter",
         "value.converter": "org.apache.kafka.connect.storage.StringConverter"
       }
     }' \
     http://localhost:8083/connectors
   ```

2. **Verify Connector Status**
   ```bash
   curl http://localhost:8083/connectors/file-source-connector/status
   ```

3. **Check Topic Data**
   ```bash
   docker-compose exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic file-user-activity --from-beginning
   ```

### Expected Results
- Data flows from source file → Kafka topic → sink file
- Changes to source file are automatically detected
- Understanding of connector lifecycle

---

### Exercise 2: Connector Management with REST API - CRUD Operations

### Objective
Learn to manage connectors programmatically using the REST API.

### Part A: Connector CRUD Operations

1. **List All Connectors**
   ```bash
   curl http://localhost:8083/connectors
   ```

2. **Get Connector Configuration**
   ```bash
   curl http://localhost:8083/connectors/file-source-connector/config
   ```

3. **Pause/Resume Connector**
   ```bash
   curl -X PUT http://localhost:8083/connectors/file-source-connector/pause
   curl -X PUT http://localhost:8083/connectors/file-source-connector/resume
   ```

### Part B: Use Java Connector Manager

1. **Run the Connector Manager**
   ```bash
   mvn exec:java -Dexec.mainClass="com.training.kafka.Day07Connect.ConnectorManager"
   ```

### Expected Results
- Understanding of REST API operations
- Ability to manage connector lifecycle
- Programmatic connector management skills

---

### Exercise 3: Data Transformation with SMTs - Single Message Transforms

### Objective
Apply Single Message Transforms (SMTs) to modify data during transit.

### Setup
```bash
# Create JSON test data
cat > /tmp/kafka-connect-data/user-events.json << EOF
{"user_id":"user1","action":"login","timestamp":"2024-01-01T10:00:00Z","ip":"192.168.1.100"}
{"user_id":"user2","action":"purchase","timestamp":"2024-01-01T10:05:00Z","ip":"192.168.1.101","amount":99.99}
EOF
```

### Part A: Extract Field Transform

1. **Create Connector with Field Extraction**
   ```bash
   curl -X POST \
     -H "Content-Type: application/json" \
     --data '{
       "name": "json-extract-connector",
       "config": {
         "connector.class": "org.apache.kafka.connect.file.FileStreamSourceConnector",
         "tasks.max": "1",
         "file": "/tmp/kafka-connect-data/user-events.json",
         "topic": "extracted-user-events",
         "key.converter": "org.apache.kafka.connect.storage.StringConverter",
         "value.converter": "org.apache.kafka.connect.storage.StringConverter",
         "transforms": "addTimestamp",
         "transforms.addTimestamp.type": "org.apache.kafka.connect.transforms.InsertField$Value",
         "transforms.addTimestamp.timestamp.field": "processed_at"
       }
     }' \
     http://localhost:8083/connectors
   ```

### Expected Results
- Understanding of SMT capabilities
- Data enrichment patterns
- Transform chaining

---

### Exercise 4: Error Handling and Dead Letter Queues - Fault Tolerance

### Objective
Implement robust error handling with dead letter queues.

### Setup
```bash
# Create data with intentional errors
cat > /tmp/kafka-connect-data/error-prone-data.txt << EOF
valid,data,row1
invalid,incomplete
valid,data,row2
EOF
```

### Part A: Configure Error Tolerance

1. **Create Connector with Error Handling**
   ```bash
   curl -X POST \
     -H "Content-Type: application/json" \
     --data '{
       "name": "error-tolerant-connector",
       "config": {
         "connector.class": "org.apache.kafka.connect.file.FileStreamSourceConnector",
         "tasks.max": "1",
         "file": "/tmp/kafka-connect-data/error-prone-data.txt",
         "topic": "error-test-topic",
         "key.converter": "org.apache.kafka.connect.storage.StringConverter",
         "value.converter": "org.apache.kafka.connect.storage.StringConverter",
         "errors.tolerance": "all",
         "errors.log.enable": "true",
         "errors.deadletterqueue.topic.name": "connect-dlq",
         "errors.deadletterqueue.topic.replication.factor": "1"
       }
     }' \
     http://localhost:8083/connectors
   ```

2. **Monitor Error Handling**
   ```bash
   # Check main topic
   docker-compose exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic error-test-topic --from-beginning
   
   # Check dead letter queue
   docker-compose exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic connect-dlq --from-beginning
   ```

### Expected Results
- Failed records sent to DLQ
- Connector continues processing
- Error context preserved

---

## Java Developer Track Exercises (Optional)

> **Java Developer Track Only**
>
> Kafka Connect uses REST API (platform-agnostic). Spring Boot can add management features:

### Spring Boot Connect Integration

1. **Connector Manager Service**
    ```bash
    # Review Spring Boot Connect service
    cat src/main/java/com/training/kafka/services/Day07ConnectService.java
    ```

2. **REST API Wrapper**
    ```bash
    # Start Spring Boot application
    mvn spring-boot:run

    # Manage connectors via Spring REST endpoints
    curl http://localhost:8080/api/training/day07/connectors
    ```

3. **Additional Features**
    - REST client abstraction
    - Error handling
    - Health checks
    - Monitoring endpoints

**Note:** Kafka Connect is managed via its own REST API. Spring Boot mainly provides wrapper services. The core Connect REST API skills from Data Engineer track are essential.

---

## Key Learning Outcomes

After completing the Data Engineer track exercises, you should understand:

1. **Connector Basics**: Source vs sink connectors
2. **REST API**: Complete connector management lifecycle
3. **Transformations**: Data modification during transit
4. **Error Handling**: Robust error tolerance strategies
5. **Monitoring**: Health checks and performance tracking

## Cleanup
```bash
# Delete connectors
curl -X DELETE http://localhost:8083/connectors/file-source-connector
curl -X DELETE http://localhost:8083/connectors/error-tolerant-connector

# Clean up files
rm -rf /tmp/kafka-connect-data
rm -rf /tmp/kafka-connect-output
```

---

**Next**: [Day 8 Exercises: Advanced Security and Monitoring](./day08-exercises.md)
