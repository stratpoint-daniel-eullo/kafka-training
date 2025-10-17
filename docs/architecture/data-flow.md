# Data Flow

## Producer Flow

```mermaid
sequenceDiagram
    participant App as Application
    participant Prod as KafkaProducer
    participant Part as Partitioner
    participant Buf as Record Buffer
    participant Kafka as Kafka Broker

    App->>Prod: send(ProducerRecord)
    Prod->>Part: partition(record)
    Part-->>Prod: partition number
    Prod->>Buf: append to batch
    Note over Buf: Batching for efficiency
    Buf->>Kafka: Batch send
    Kafka-->>Buf: Acknowledgment (acks)
    Buf-->>Prod: RecordMetadata
    Prod-->>App: Future<RecordMetadata>
```

## Consumer Flow

```mermaid
sequenceDiagram
    participant App as Application
    participant Cons as KafkaConsumer
    participant CG as Consumer Group Coordinator
    participant Kafka as Kafka Broker

    App->>Cons: subscribe(topics)
    Cons->>CG: Join group
    CG-->>Cons: Partition assignment
    loop Polling
        App->>Cons: poll()
        Cons->>Kafka: Fetch request
        Kafka-->>Cons: Records batch
        Cons-->>App: ConsumerRecords
        App->>App: Process records
        App->>Cons: commitSync()
        Cons->>Kafka: Commit offsets
    end
```

## Schema Registry Flow

```mermaid
sequenceDiagram
    participant Prod as Producer
    participant SR as Schema Registry
    participant Kafka as Kafka
    participant Cons as Consumer

    Prod->>SR: Register schema (if new)
    SR-->>Prod: Schema ID
    Prod->>Prod: Serialize with schema ID
    Prod->>Kafka: Send Avro message
    Kafka->>Cons: Deliver message
    Cons->>SR: Fetch schema by ID
    SR-->>Cons: Schema definition
    Cons->>Cons: Deserialize
```

## Kafka Connect Flow

```mermaid
graph LR
    PG[(PostgreSQL)] -->|JDBC Source| Kafka[Kafka Topics]
    Kafka -->|JDBC Sink| PG2[(PostgreSQL)]
    
    style PG fill:#f3e5f5
    style Kafka fill:#fff3e0
    style PG2 fill:#f3e5f5
```

## Kafka Streams Flow

```mermaid
graph LR
    Input[Input Topic] --> KS[Kafka Streams App]
    KS --> Filter[Filter]
    Filter --> Map[Map]
    Map --> Aggregate[Aggregate]
    Aggregate --> Output[Output Topic]
    Aggregate <--> State[(State Store)]
    
    style Input fill:#e3f2fd
    style Output fill:#e8f5e9
    style State fill:#fff3e0
```

## Next Steps

- [System Design](system-design.md) - Overall architecture
- [Container Architecture](container-architecture.md) - Docker/Kubernetes
