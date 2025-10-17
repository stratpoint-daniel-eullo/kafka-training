# Container Architecture

## Docker Compose Architecture

```mermaid
graph TB
    subgraph "kafka-training-network"
        ZK[Zookeeper:2181]
        K[Kafka:9092]
        SR[Schema Registry:8082]
        KC[Kafka Connect:8083]
        PG[(PostgreSQL:5432)]
        UI[Kafka UI:8081]
        APP[Training App:8080]
    end

    ZK --> K
    K --> SR
    K --> KC
    SR --> KC
    PG --> KC
    K --> APP
    SR --> APP
    PG --> APP
    K --> UI
    SR --> UI
    KC --> UI

    style K fill:#fff3e0
    style APP fill:#e3f2fd
```

## Kubernetes Pod Architecture

```mermaid
graph TB
    subgraph "Kubernetes Cluster"
        subgraph "kafka-training-app Pod"
            Init[Init Container:<br/>wait-for-kafka]
            Main[Main Container:<br/>Spring Boot App]
        end
        
        subgraph "External Services"
            KAFKA[Kafka Cluster]
            PG[(PostgreSQL)]
            SR[Schema Registry]
        end
    end

    Init -.->|checks| KAFKA
    Init -->|complete| Main
    Main --> KAFKA
    Main --> PG
    Main --> SR

    style KAFKA fill:#fff3e0
    style Main fill:#e3f2fd
```

## Volume Management

Development mode (ephemeral):
```yaml
# No volumes - clean state on restart
services:
  kafka:
    image: confluentinc/cp-kafka:7.7.0
    # No volumes defined
```

Production mode (persistent):
```yaml
services:
  kafka:
    volumes:
      - kafka-data:/var/lib/kafka/data
volumes:
  kafka-data:
```

## Health Checks

```yaml
kafka:
  healthcheck:
    test: ["CMD-SHELL", "kafka-broker-api-versions --bootstrap-server localhost:9092"]
    interval: 10s
    timeout: 5s
    retries: 5
```

## Next Steps

- [System Design](system-design.md) - Overall architecture
- [Security](security.md) - Container security practices
