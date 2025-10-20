# Kafka Rosetta Stone

## Understanding Kafka Across Languages

This guide shows the same Kafka concepts implemented in both **Python** and **Java**. The underlying Kafka concepts are identical - only the syntax differs. Use this reference to:

- Understand that Kafka knowledge is language-agnostic
- Translate concepts between Python and Java
- See how both tracks teach the same fundamentals
- Choose your preferred learning path

!!! tip "Key Insight"
    Kafka concepts like topics, partitions, consumer groups, and offset management work exactly the same way in Python, Java, Scala, Go, or any other language. Master the concepts, and you can use any client library.

## Environment Setup

### Starting Kafka Infrastructure

Both tracks use the same Kafka infrastructure:

=== "Both Tracks"

    ```bash
    # Start Kafka, Schema Registry, Kafka Connect, PostgreSQL
    docker-compose -f docker-compose-dev.yml up -d

    # Verify all services are running
    docker ps

    # Check Kafka is ready
    docker exec -it dev-kafka kafka-topics --bootstrap-server localhost:9092 --list
    ```

!!! note "Same Infrastructure"
    Both Python and Java tracks use the same `docker-compose-dev.yml` file. The Kafka infrastructure is identical - only the client applications differ.

## Day 1: Topic Management

### Creating Topics

=== "Python (kafka-python)"

    ```python
    # examples/python/day01_admin.py
    from kafka.admin import KafkaAdminClient, NewTopic

    # Create admin client
    admin = KafkaAdminClient(
        bootstrap_servers='localhost:9092',
        client_id='python-admin'
    )

    # Define topic
    topic = NewTopic(
        name='user-events',
        num_partitions=3,
        replication_factor=1
    )

    # Create topic
    admin.create_topics([topic])
    print(f"Created topic: {topic.name}")

    admin.close()
    ```

=== "Java (Pure Kafka API)"

    ```java
    // src/main/java/com/training/kafka/Day01Foundation/AdminClientExample.java
    import org.apache.kafka.clients.admin.*;
    import java.util.*;

    public class AdminClientExample {
        public static void main(String[] args) {
            // Configure admin client
            Properties props = new Properties();
            props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
            props.put(AdminClientConfig.CLIENT_ID_CONFIG, "java-admin");

            // Create admin client
            try (AdminClient admin = AdminClient.create(props)) {
                // Define topic
                NewTopic topic = new NewTopic("user-events", 3, (short) 1);

                // Create topic
                admin.createTopics(Collections.singleton(topic)).all().get();
                System.out.println("Created topic: " + topic.name());
            }
        }
    }
    ```

### Listing Topics

=== "Python"

    ```python
    from kafka.admin import KafkaAdminClient

    admin = KafkaAdminClient(bootstrap_servers='localhost:9092')

    # List all topics
    topics = admin.list_topics()
    print(f"Available topics: {topics}")
    ```

=== "Java"

    ```java
    try (AdminClient admin = AdminClient.create(props)) {
        // List all topics
        ListTopicsResult topics = admin.listTopics();
        Set<String> topicNames = topics.names().get();
        System.out.println("Available topics: " + topicNames);
    }
    ```

=== "CLI (Language-Agnostic)"

    ```bash
    # List topics using Kafka CLI
    kafka-topics --bootstrap-server localhost:9092 --list

    # Describe a topic
    kafka-topics --bootstrap-server localhost:9092 \
      --describe --topic user-events
    ```

## Day 3: Producers

### Basic Producer

=== "Python (confluent-kafka)"

    ```python
    # examples/python/day03_producer.py
    from confluent_kafka import Producer
    import json

    # Producer configuration
    config = {
        'bootstrap.servers': 'localhost:9092',
        'client.id': 'python-producer',
        'acks': 'all',
        'enable.idempotence': True,
        'compression.type': 'snappy'
    }

    # Create producer
    producer = Producer(config)

    # Prepare message
    event = {
        'user_id': 'user-123',
        'action': 'login',
        'timestamp': '2025-01-19T10:00:00Z'
    }

    # Delivery callback
    def delivery_callback(err, msg):
        if err:
            print(f'Delivery failed: {err}')
        else:
            print(f'Message delivered to {msg.topic()} [{msg.partition()}] @ {msg.offset()}')

    # Send message
    producer.produce(
        topic='user-events',
        key='user-123',
        value=json.dumps(event),
        callback=delivery_callback
    )

    # Wait for delivery
    producer.flush()
    ```

=== "Java (Pure Kafka API)"

    ```java
    // src/main/java/com/training/kafka/Day03Producers/BasicProducer.java
    import org.apache.kafka.clients.producer.*;
    import java.util.Properties;

    public class BasicProducer {
        public static void main(String[] args) {
            // Producer configuration
            Properties props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
            props.put(ProducerConfig.CLIENT_ID_CONFIG, "java-producer");
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
            props.put(ProducerConfig.ACKS_CONFIG, "all");
            props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
            props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");

            // Create producer
            try (Producer<String, String> producer = new KafkaProducer<>(props)) {
                // Prepare message
                String event = "{\"user_id\":\"user-123\",\"action\":\"login\",\"timestamp\":\"2025-01-19T10:00:00Z\"}";

                // Create record
                ProducerRecord<String, String> record =
                    new ProducerRecord<>("user-events", "user-123", event);

                // Send message with callback
                producer.send(record, (metadata, exception) -> {
                    if (exception != null) {
                        System.err.println("Delivery failed: " + exception.getMessage());
                    } else {
                        System.out.printf("Message delivered to %s [%d] @ %d%n",
                            metadata.topic(), metadata.partition(), metadata.offset());
                    }
                });

                // Wait for delivery
                producer.flush();
            }
        }
    }
    ```

### Key Configuration Comparison

| Configuration | Python (confluent-kafka) | Java (KafkaProducer) |
|---------------|-------------------------|---------------------|
| **Bootstrap Servers** | `'bootstrap.servers'` | `ProducerConfig.BOOTSTRAP_SERVERS_CONFIG` |
| **Acknowledgments** | `'acks': 'all'` | `ProducerConfig.ACKS_CONFIG, "all"` |
| **Idempotence** | `'enable.idempotence': True` | `ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true` |
| **Compression** | `'compression.type': 'snappy'` | `ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy"` |
| **Key Serializer** | Automatic | `ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG` |
| **Value Serializer** | Automatic | `ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG` |

## Day 4: Consumers

### Basic Consumer with Manual Offset Commit

=== "Python (confluent-kafka)"

    ```python
    # examples/python/day04_consumer.py
    from confluent_kafka import Consumer, KafkaError
    import json

    # Consumer configuration
    config = {
        'bootstrap.servers': 'localhost:9092',
        'group.id': 'analytics-group',
        'auto.offset.reset': 'earliest',
        'enable.auto.commit': False  # Manual commit
    }

    # Create consumer
    consumer = Consumer(config)

    # Subscribe to topic
    consumer.subscribe(['user-events'])

    try:
        while True:
            # Poll for messages
            msg = consumer.poll(timeout=1.0)

            if msg is None:
                continue

            if msg.error():
                if msg.error().code() == KafkaError._PARTITION_EOF:
                    continue
                else:
                    print(f'Error: {msg.error()}')
                    break

            # Process message
            event = json.loads(msg.value().decode('utf-8'))
            print(f'Received: {event}')
            print(f'Partition: {msg.partition()}, Offset: {msg.offset()}')

            # Manual offset commit (after processing)
            consumer.commit(message=msg)

    finally:
        consumer.close()
    ```

=== "Java (Pure Kafka API)"

    ```java
    // src/main/java/com/training/kafka/Day04Consumers/BasicConsumer.java
    import org.apache.kafka.clients.consumer.*;
    import java.time.Duration;
    import java.util.*;

    public class BasicConsumer {
        public static void main(String[] args) {
            // Consumer configuration
            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
            props.put(ConsumerConfig.GROUP_ID_CONFIG, "analytics-group");
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // Manual commit

            // Create consumer
            try (Consumer<String, String> consumer = new KafkaConsumer<>(props)) {
                // Subscribe to topic
                consumer.subscribe(Collections.singletonList("user-events"));

                while (true) {
                    // Poll for messages
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

                    for (ConsumerRecord<String, String> record : records) {
                        // Process message
                        System.out.println("Received: " + record.value());
                        System.out.printf("Partition: %d, Offset: %d%n",
                            record.partition(), record.offset());

                        // Manual offset commit (after processing)
                        consumer.commitSync();
                    }
                }
            }
        }
    }
    ```

### Consumer Group Management

=== "Python"

    ```python
    # Check consumer group
    from kafka.admin import KafkaAdminClient

    admin = KafkaAdminClient(bootstrap_servers='localhost:9092')

    # List consumer groups
    groups = admin.list_consumer_groups()
    print(f"Consumer groups: {groups}")
    ```

=== "Java"

    ```java
    // Check consumer group
    try (AdminClient admin = AdminClient.create(props)) {
        ListConsumerGroupsResult groups = admin.listConsumerGroups();
        System.out.println("Consumer groups: " + groups.all().get());
    }
    ```

=== "CLI (Language-Agnostic)"

    ```bash
    # List consumer groups
    kafka-consumer-groups --bootstrap-server localhost:9092 --list

    # Describe consumer group
    kafka-consumer-groups --bootstrap-server localhost:9092 \
      --describe --group analytics-group
    ```

## Day 5: Schema Registry with Avro

### Avro Producer

=== "Python (confluent-kafka)"

    ```python
    # examples/python/day05_avro_producer.py
    from confluent_kafka import Producer
    from confluent_kafka.schema_registry import SchemaRegistryClient
    from confluent_kafka.schema_registry.avro import AvroSerializer
    from confluent_kafka.serialization import SerializationContext, MessageField

    # Schema Registry client
    schema_registry_client = SchemaRegistryClient({
        'url': 'http://localhost:8082'
    })

    # Avro schema
    value_schema_str = """
    {
      "type": "record",
      "name": "UserEvent",
      "fields": [
        {"name": "user_id", "type": "string"},
        {"name": "action", "type": "string"},
        {"name": "timestamp", "type": "long"}
      ]
    }
    """

    # Avro serializer
    avro_serializer = AvroSerializer(
        schema_registry_client,
        value_schema_str
    )

    # Producer
    producer = Producer({'bootstrap.servers': 'localhost:9092'})

    # Event data
    event = {
        'user_id': 'user-123',
        'action': 'login',
        'timestamp': 1674124800000
    }

    # Serialize and send
    producer.produce(
        topic='user-events-avro',
        key='user-123',
        value=avro_serializer(event, SerializationContext('user-events-avro', MessageField.VALUE))
    )

    producer.flush()
    ```

=== "Java (Pure Kafka API)"

    ```java
    // src/main/java/com/training/kafka/Day05Schemas/AvroProducer.java
    import io.confluent.kafka.serializers.KafkaAvroSerializer;
    import org.apache.avro.Schema;
    import org.apache.avro.generic.GenericData;
    import org.apache.avro.generic.GenericRecord;
    import org.apache.kafka.clients.producer.*;

    public class AvroProducer {
        public static void main(String[] args) {
            // Producer configuration
            Properties props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                KafkaAvroSerializer.class.getName());
            props.put("schema.registry.url", "http://localhost:8082");

            // Avro schema
            String schemaString = """
                {
                  "type": "record",
                  "name": "UserEvent",
                  "fields": [
                    {"name": "user_id", "type": "string"},
                    {"name": "action", "type": "string"},
                    {"name": "timestamp", "type": "long"}
                  ]
                }
                """;

            Schema schema = new Schema.Parser().parse(schemaString);

            // Create producer
            try (Producer<String, GenericRecord> producer = new KafkaProducer<>(props)) {
                // Create Avro record
                GenericRecord event = new GenericData.Record(schema);
                event.put("user_id", "user-123");
                event.put("action", "login");
                event.put("timestamp", 1674124800000L);

                // Send message
                ProducerRecord<String, GenericRecord> record =
                    new ProducerRecord<>("user-events-avro", "user-123", event);

                producer.send(record);
                producer.flush();
            }
        }
    }
    ```

## Day 6: Stream Processing

### Real-Time Aggregation

=== "Python (Faust)"

    ```python
    # examples/python/day06_streams_faust.py
    import faust

    # Faust app
    app = faust.App(
        'sales-aggregator',
        broker='kafka://localhost:9092',
        value_serializer='json'
    )

    # Define event schema
    class OrderEvent(faust.Record, serializer='json'):
        order_id: str
        product_id: str
        amount: float
        timestamp: int

    # Input topic
    orders_topic = app.topic('order-events', value_type=OrderEvent)

    # State table for aggregations
    sales_table = app.Table('sales_by_product', default=float)

    # Stream processing agent
    @app.agent(orders_topic)
    async def aggregate_sales(orders):
        async for order in orders.group_by(OrderEvent.product_id):
            # Update running total
            current = sales_table[order.product_id]
            sales_table[order.product_id] = current + order.amount
            print(f'Product {order.product_id}: ${sales_table[order.product_id]:.2f}')

    if __name__ == '__main__':
        app.main()
    ```

=== "Java (Kafka Streams)"

    ```java
    // src/main/java/com/training/kafka/Day06Streams/SalesAggregator.java
    import org.apache.kafka.streams.*;
    import org.apache.kafka.streams.kstream.*;

    public class SalesAggregator {
        public static void main(String[] args) {
            // Streams configuration
            Properties props = new Properties();
            props.put(StreamsConfig.APPLICATION_ID_CONFIG, "sales-aggregator");
            props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

            // Build topology
            StreamsBuilder builder = new StreamsBuilder();

            // Input stream
            KStream<String, OrderEvent> orders = builder.stream("order-events");

            // Aggregate sales by product
            orders
                .groupBy((key, order) -> order.getProductId())
                .aggregate(
                    () -> 0.0,
                    (productId, order, total) -> total + order.getAmount(),
                    Materialized.as("sales-by-product")
                )
                .toStream()
                .foreach((productId, total) ->
                    System.out.printf("Product %s: $%.2f%n", productId, total)
                );

            // Start streams
            KafkaStreams streams = new KafkaStreams(builder.build(), props);
            streams.start();

            // Shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
        }
    }
    ```

## Concept Equivalence

### Producer Concepts

| Concept | Python Term | Java Term | Description |
|---------|------------|-----------|-------------|
| **Send Message** | `producer.produce()` | `producer.send()` | Asynchronously send message |
| **Wait for Delivery** | `producer.flush()` | `producer.flush()` | Block until all messages sent |
| **Delivery Callback** | `callback=func` | `Callback` interface | Handle delivery confirmation |
| **Idempotence** | `'enable.idempotence': True` | `ENABLE_IDEMPOTENCE_CONFIG, true` | Exactly-once producer |
| **Acknowledgments** | `'acks': 'all'` | `ACKS_CONFIG, "all"` | Wait for all replicas |

### Consumer Concepts

| Concept | Python Term | Java Term | Description |
|---------|------------|-----------|-------------|
| **Poll Messages** | `consumer.poll()` | `consumer.poll(Duration)` | Fetch messages |
| **Subscribe** | `consumer.subscribe()` | `consumer.subscribe()` | Join consumer group |
| **Manual Commit** | `consumer.commit()` | `consumer.commitSync()` | Commit offsets |
| **Auto Commit** | `'enable.auto.commit': False` | `ENABLE_AUTO_COMMIT_CONFIG, false` | Disable auto commit |
| **Offset Reset** | `'auto.offset.reset': 'earliest'` | `AUTO_OFFSET_RESET_CONFIG, "earliest"` | Start from beginning |

### Stream Processing Concepts

| Concept | Python (Faust) | Java (Kafka Streams) | Description |
|---------|---------------|---------------------|-------------|
| **Stream Definition** | `app.topic()` | `builder.stream()` | Define input stream |
| **Stateful Aggregation** | `app.Table()` | `Materialized.as()` | State store |
| **Grouping** | `group_by()` | `groupBy()` | Group by key |
| **Windowing** | `tumbling()` / `hopping()` | `TimeWindows.of()` | Time-based windows |
| **Processing** | `@app.agent` | `KStream` operations | Stream transformations |

## Common Patterns

### Pattern 1: Idempotent Processing

=== "Python"

    ```python
    # Check if already processed
    processed_ids = set()

    for message in consumer:
        order_id = message.value()['order_id']

        # Skip if already processed
        if order_id in processed_ids:
            continue

        # Process order
        process_order(message.value())

        # Mark as processed
        processed_ids.add(order_id)

        # Commit offset
        consumer.commit(message=message)
    ```

=== "Java"

    ```java
    // Check if already processed
    Set<String> processedIds = new HashSet<>();

    while (true) {
        ConsumerRecords<String, OrderEvent> records = consumer.poll(Duration.ofMillis(100));

        for (ConsumerRecord<String, OrderEvent> record : records) {
            String orderId = record.value().getOrderId();

            // Skip if already processed
            if (processedIds.contains(orderId)) {
                continue;
            }

            // Process order
            processOrder(record.value());

            // Mark as processed
            processedIds.add(orderId);

            // Commit offset
            consumer.commitSync();
        }
    }
    ```

### Pattern 2: Dead Letter Queue (DLQ)

=== "Python"

    ```python
    # DLQ producer
    dlq_producer = Producer({'bootstrap.servers': 'localhost:9092'})

    for message in consumer:
        try:
            # Process message
            process_message(message.value())
            consumer.commit(message=message)
        except Exception as e:
            # Send to DLQ
            dlq_producer.produce(
                topic='dlq-topic',
                key=message.key(),
                value=message.value(),
                headers=[('error', str(e))]
            )
            dlq_producer.flush()
            consumer.commit(message=message)  # Commit to move past bad message
    ```

=== "Java"

    ```java
    // DLQ producer
    Producer<String, String> dlqProducer = new KafkaProducer<>(props);

    while (true) {
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

        for (ConsumerRecord<String, String> record : records) {
            try {
                // Process message
                processMessage(record.value());
                consumer.commitSync();
            } catch (Exception e) {
                // Send to DLQ
                ProducerRecord<String, String> dlqRecord =
                    new ProducerRecord<>("dlq-topic", record.key(), record.value());
                dlqRecord.headers().add("error", e.getMessage().getBytes());

                dlqProducer.send(dlqRecord);
                dlqProducer.flush();

                consumer.commitSync(); // Commit to move past bad message
            }
        }
    }
    ```

## CLI Tools (Language-Agnostic)

These Kafka CLI tools work the same regardless of your client language:

```bash
# Topic management
kafka-topics --bootstrap-server localhost:9092 --list
kafka-topics --bootstrap-server localhost:9092 --create --topic test --partitions 3 --replication-factor 1
kafka-topics --bootstrap-server localhost:9092 --describe --topic test

# Console producer
kafka-console-producer --bootstrap-server localhost:9092 --topic test

# Console consumer
kafka-console-consumer --bootstrap-server localhost:9092 --topic test --from-beginning

# Consumer groups
kafka-consumer-groups --bootstrap-server localhost:9092 --list
kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group analytics-group

# ACLs (security)
kafka-acls --bootstrap-server localhost:9092 --list

# Log segments
kafka-log-dirs --bootstrap-server localhost:9092 --describe
```

## Summary

**Key Takeaway:** The Kafka concepts are identical across Python and Java. What changes is:

- Language syntax
- Library names
- Configuration property names
- Error handling patterns

**Universal Concepts:**

- Topics have partitions
- Partitions have offsets
- Consumer groups coordinate consumers
- Producers send, consumers receive
- Schema Registry manages schemas
- Kafka Connect integrates systems
- Security uses SASL/SSL

**Choose Your Track Based On:**

- **Python**: Data pipelines, analytics, platform-agnostic knowledge
- **Java**: Microservices, Spring Boot, enterprise applications

Both tracks teach the same Kafka fundamentals. The language is just a tool to interact with Kafka.

## Next Steps

- **Compare Tracks**: [Track Selection Guide](../getting-started/track-selection.md)
- **Python Track**: [Python Capstone Guide](../exercises/capstone-guide-python.md)
- **Java Track**: [Java Capstone Guide](../exercises/capstone-guide-java.md)
- **Capstone Projects**: [Capstone Landing](../exercises/capstone-landing.md)
