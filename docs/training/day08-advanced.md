# Day 8: Advanced Topics and Production

> **Primary Audience:** Data Engineers
> **Learning Track:** Platform-agnostic Kafka security, monitoring, and production configuration. Spring Boot integration for Actuator metrics is optional.

## Learning Objectives

By the end of Day 8, you will:

- [ ] Configure Kafka security (SSL/TLS, SASL)
- [ ] Implement monitoring and metrics collection
- [ ] Monitor consumer lag and cluster health
- [ ] Tune Kafka for production performance
- [ ] Configure ACLs and authorization
- [ ] Implement production best practices
- [ ] Troubleshoot common production issues

## Production Example: SecurityConfig.java

> **See Working Example**: `src/main/java/com/training/kafka/Day08Advanced/SecurityConfig.java`

This project includes production-ready security configurations demonstrating:
- SSL/TLS encryption setup
- SASL authentication (PLAIN, SCRAM, OAuth)
- Combined SSL + SASL for production
- ACL configuration examples
- Security best practices

### SSL/TLS Configuration from Actual Code

From `SecurityConfig.java:28-51`:

```java
public static Properties getSslConfig(String keystorePath, String keystorePassword,
                                     String truststorePath, String truststorePassword) {
    Properties props = new Properties();

    // SSL Configuration
    props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
    props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keystorePath);
    props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, keystorePassword);
    props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, keystorePassword);
    props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, truststorePath);
    props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, truststorePassword);

    // SSL Protocol and cipher suites
    props.put(SslConfigs.SSL_PROTOCOL_CONFIG, "TLSv1.3");
    props.put(SslConfigs.SSL_ENABLED_PROTOCOLS_CONFIG, "TLSv1.3,TLSv1.2");
    props.put(SslConfigs.SSL_CIPHER_SUITES_CONFIG,
        "TLS_AES_256_GCM_SHA384,TLS_CHACHA20_POLY1305_SHA256,TLS_AES_128_GCM_SHA256");

    // SSL endpoint identification
    props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "https");

    return props;
}
```

### SASL/SCRAM Configuration from Actual Code

From `SecurityConfig.java:72-83`:

```java
public static Properties getSaslScramConfig(String username, String password) {
    Properties props = new Properties();

    props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
    props.put(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-512");
    props.put(SaslConfigs.SASL_JAAS_CONFIG,
        String.format("org.apache.kafka.common.security.scram.ScramLoginModule required " +
                     "username=\"%s\" password=\"%s\";", username, password));

    return props;
}
```

### Production Security Configuration (SSL + SASL)

From `SecurityConfig.java:110-135`:

```java
public static Properties getProductionSecurityConfig(String keystorePath, String keystorePassword,
                                                    String truststorePath, String truststorePassword,
                                                    String username, String password) {
    Properties props = new Properties();

    // Combine SSL and SASL
    props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");

    // SSL Configuration
    props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keystorePath);
    props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, keystorePassword);
    props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, keystorePassword);
    props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, truststorePath);
    props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, truststorePassword);
    props.put(SslConfigs.SSL_PROTOCOL_CONFIG, "TLSv1.3");
    props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "https");

    // SASL Configuration
    props.put(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-512");
    props.put(SaslConfigs.SASL_JAAS_CONFIG,
        String.format("org.apache.kafka.common.security.scram.ScramLoginModule required " +
                     "username=\"%s\" password=\"%s\";", username, password));

    return props;
}
```

**Run the Example:**

```bash
# From project root
mvn exec:java -Dexec.mainClass="com.training.kafka.Day08Advanced.SecurityConfig"
```

## Python Kafka Security Configuration

For data engineers using Python, here's how to configure secure Kafka connections:

### Python SSL/TLS Configuration

**Complete Example**: `examples/python/day08_security.py`

```bash
# Install required library
pip install kafka-python

# Run the security demo
python examples/python/day08_security.py
```

**Key code from day08_security.py:**

```python
# SSL/TLS Configuration (kafka-python)
# Compare to Java: Similar to setting ssl.truststore.location, ssl.keystore.location in Java
ssl_config = {
    'bootstrap_servers': 'localhost:9093',
    'security_protocol': 'SSL',
    'ssl_cafile': '/path/to/ca-cert',        # Java: ssl.truststore.location
    'ssl_certfile': '/path/to/client-cert',  # Java: ssl.keystore.location
    'ssl_keyfile': '/path/to/client-key',
    'ssl_check_hostname': True,
    'acks': 'all',
    'retries': 3
}

producer = KafkaProducer(**ssl_config)
```

**Note**: For production use with Avro/Schema Registry, use `confluent-kafka` library (also shown in day08_security.py).

### Python SASL/PLAIN Authentication

**From day08_security.py:**

```python
# SASL/PLAIN Configuration (kafka-python)
# Compare to Java: Similar to setting sasl.mechanism, sasl.jaas.config in Java
sasl_plain_config = {
    'bootstrap_servers': 'localhost:9094',
    'security_protocol': 'SASL_PLAINTEXT',  # Use SASL_SSL in production
    'sasl_mechanism': 'PLAIN',               # Java: sasl.mechanism
    'sasl_plain_username': 'kafka-user',     # Java: username in jaas.config
    'sasl_plain_password': 'kafka-password', # Java: password in jaas.config
    'acks': 'all'
}

producer = KafkaProducer(**sasl_plain_config)
```

**⚠️ Security Warning**: SASL/PLAIN with `SASL_PLAINTEXT` transmits credentials in plaintext. Always use `SASL_SSL` in production environments.

### Python SASL/SCRAM-SHA-512 Authentication (Recommended)

**From day08_security.py:**

```python
# SASL/SCRAM-SHA-512 Configuration (kafka-python) - RECOMMENDED for production
# Compare to Java: Similar to setting sasl.mechanism=SCRAM-SHA-512 in Java
sasl_scram_config = {
    'bootstrap_servers': 'localhost:9094',
    'security_protocol': 'SASL_SSL',           # Combines SASL authentication + SSL encryption
    'sasl_mechanism': 'SCRAM-SHA-512',         # Java: sasl.mechanism
    'sasl_plain_username': 'kafka-user',       # Java: username in jaas.config
    'sasl_plain_password': 'strong-password',  # Java: password in jaas.config
    'ssl_cafile': '/path/to/ca-cert',          # Java: ssl.truststore.location
    'ssl_check_hostname': True,
    'acks': 'all',
    'retries': 3
}

producer = KafkaProducer(**sasl_scram_config)
consumer = KafkaConsumer('secure-topic', group_id='scram-group', **sasl_scram_config)
```

**✅ Best Practice**: SCRAM-SHA-512 is recommended over PLAIN because:
- Credentials are never transmitted in plaintext
- Server doesn't store plaintext passwords
- Protection against replay attacks

### Complete Python Security Configuration

**From day08_security.py** - Using `confluent-kafka` for production:

```python
from confluent_kafka import Producer, Consumer
import os

# Production-grade configuration using environment variables
# Compare to Java: Similar to creating Properties object with security configs
confluent_producer_config = {
    'bootstrap.servers': os.getenv('KAFKA_BOOTSTRAP_SERVERS', 'localhost:9094'),
    'security.protocol': 'SASL_SSL',           # Java: security.protocol
    'sasl.mechanism': 'SCRAM-SHA-512',         # Java: sasl.mechanism
    'sasl.username': os.getenv('KAFKA_USERNAME', 'kafka-user'),
    'sasl.password': os.getenv('KAFKA_PASSWORD', 'strong-password'),
    'ssl.ca.location': '/path/to/ca-cert',     # Java: ssl.truststore.location
    'acks': 'all',                              # Java: acks
    'retries': 3,                               # Java: retries
    'enable.idempotence': True                  # Java: enable.idempotence
}

producer = Producer(confluent_producer_config)
producer.produce('secure-topic', value='Secure message')
producer.flush()

# Consumer configuration
confluent_consumer_config = {
    **confluent_producer_config,
    'group.id': 'secure-consumer-group',        # Java: group.id
    'auto.offset.reset': 'earliest'             # Java: auto.offset.reset
}

consumer = Consumer(confluent_consumer_config)
consumer.subscribe(['secure-topic'])
```

**Environment Variables** (store in `.env` file):
```bash
export KAFKA_BOOTSTRAP_SERVERS=kafka-broker1:9094,kafka-broker2:9094
export KAFKA_USERNAME=data-engineer
export KAFKA_PASSWORD=secure-password
```

**✅ Best Practice**: Use `confluent-kafka` for production because:
- C-based implementation (faster than pure Python)
- Better error handling and connection management
- Supports Avro with Schema Registry
- Industry-standard for production Python Kafka applications

### Python ACL Management

**Note**: ACL management in Python is primarily done through the Kafka broker's CLI tools (`kafka-acls.sh`) or Java admin APIs. The `kafka-python` library has limited ACL support. For production ACL management, use:

1. **Kafka CLI Tools** (recommended):
```bash
# Grant WRITE permission to producer
kafka-acls.sh --bootstrap-server localhost:9094 \
  --command-config admin.properties \
  --add --allow-principal User:producer-user \
  --operation WRITE --topic production-events

# Grant READ permissions to consumer
kafka-acls.sh --bootstrap-server localhost:9094 \
  --command-config admin.properties \
  --add --allow-principal User:consumer-user \
  --operation READ --topic production-events \
  --group consumer-group-1
```

2. **Python REST API Client** (for programmatic management):
```python
# Use Kafka REST Proxy or custom admin REST API
# See examples/python/day07_connect_client.py for REST API patterns
import requests

def create_acl(principal, topic, operation):
    """Create ACL via REST API or Kafka Admin protocol"""
    # Implementation depends on your Kafka admin infrastructure
    pass
```

**Python Security Installation:**

```bash
# For kafka-python (SSL/SASL support)
pip install kafka-python

# For confluent-kafka (production-grade with Avro)
pip install confluent-kafka[avro]
```

**Python Security Benefits:**
- **Same Security Model**: Uses same SSL/TLS and SASL mechanisms as Java
- **Platform-Agnostic**: Python apps can securely connect to any Kafka cluster
- **Production-Ready**: Full support for SCRAM-SHA-512, SSL encryption, and authentication
- **Data Engineering Integration**: Secure Kafka connections work seamlessly with:
  - Apache Airflow for workflow orchestration
  - PySpark for distributed data processing
  - Pandas for data analysis
  - Jupyter notebooks for data exploration

## Security (Platform-Agnostic Concepts)

### SSL/TLS Encryption

Encrypt data in transit between clients and brokers.

#### Generate SSL Certificates

```bash
# 1. Create Certificate Authority (CA)
openssl req -new -x509 -keyout ca-key -out ca-cert -days 365 \
  -subj "/CN=KafkaCA" -passout pass:kafka-password

# 2. Create Kafka broker keystore
keytool -keystore kafka.server.keystore.jks -alias localhost \
  -genkey -keyalg RSA -validity 365 \
  -storepass kafka-password -keypass kafka-password \
  -dname "CN=kafka-broker" -ext SAN=DNS:localhost,IP:127.0.0.1

# 3. Create certificate signing request
keytool -keystore kafka.server.keystore.jks -alias localhost \
  -certreq -file cert-file -storepass kafka-password

# 4. Sign the certificate
openssl x509 -req -CA ca-cert -CAkey ca-key -in cert-file \
  -out cert-signed -days 365 -CAcreateserial \
  -passin pass:kafka-password

# 5. Import CA certificate into keystore
keytool -keystore kafka.server.keystore.jks -alias CARoot \
  -import -file ca-cert -storepass kafka-password -noprompt

# 6. Import signed certificate into keystore
keytool -keystore kafka.server.keystore.jks -alias localhost \
  -import -file cert-signed -storepass kafka-password -noprompt

# 7. Create truststore for clients
keytool -keystore kafka.client.truststore.jks -alias CARoot \
  -import -file ca-cert -storepass kafka-password -noprompt
```

#### Broker SSL Configuration

```properties
# server.properties

# SSL/TLS Configuration
listeners=PLAINTEXT://localhost:9092,SSL://localhost:9093
advertised.listeners=PLAINTEXT://localhost:9092,SSL://localhost:9093
listener.security.protocol.map=PLAINTEXT:PLAINTEXT,SSL:SSL

# SSL Keystore
ssl.keystore.location=/etc/kafka/secrets/kafka.server.keystore.jks
ssl.keystore.password=kafka-password
ssl.key.password=kafka-password

# SSL Truststore
ssl.truststore.location=/etc/kafka/secrets/kafka.server.truststore.jks
ssl.truststore.password=kafka-password

# Client Authentication (optional)
ssl.client.auth=required
```

#### Producer SSL Configuration

```java
@Configuration
public class SecureProducerConfig {

    @Bean
    public ProducerFactory<String, String> secureProducerFactory() {
        Map<String, Object> config = new HashMap<>();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
            "localhost:9093");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            StringSerializer.class);

        // SSL Configuration
        config.put("security.protocol", "SSL");
        config.put("ssl.truststore.location",
            "/etc/kafka/secrets/kafka.client.truststore.jks");
        config.put("ssl.truststore.password", "kafka-password");

        // Client keystore (for mutual TLS)
        config.put("ssl.keystore.location",
            "/etc/kafka/secrets/kafka.client.keystore.jks");
        config.put("ssl.keystore.password", "kafka-password");
        config.put("ssl.key.password", "kafka-password");

        return new DefaultKafkaProducerFactory<>(config);
    }
}
```

### SASL Authentication

Authenticate clients using username/password or Kerberos.

#### SASL/PLAIN Configuration

**Broker Configuration:**

```properties
# server.properties

# SASL Configuration
listeners=SASL_SSL://localhost:9094
advertised.listeners=SASL_SSL://localhost:9094
security.inter.broker.protocol=SASL_SSL
sasl.mechanism.inter.broker.protocol=PLAIN
sasl.enabled.mechanisms=PLAIN

# JAAS Configuration
listener.name.sasl_ssl.plain.sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required \
  username="admin" \
  password="admin-secret" \
  user_admin="admin-secret" \
  user_producer="producer-secret" \
  user_consumer="consumer-secret";
```

**Producer Configuration:**

```java
@Configuration
public class SaslProducerConfig {

    @Bean
    public ProducerFactory<String, String> saslProducerFactory() {
        Map<String, Object> config = new HashMap<>();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
            "localhost:9094");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            StringSerializer.class);

        // SASL Configuration
        config.put("security.protocol", "SASL_SSL");
        config.put("sasl.mechanism", "PLAIN");
        config.put("sasl.jaas.config",
            "org.apache.kafka.common.security.plain.PlainLoginModule required " +
            "username=\"producer\" " +
            "password=\"producer-secret\";");

        // SSL Configuration
        config.put("ssl.truststore.location",
            "/etc/kafka/secrets/kafka.client.truststore.jks");
        config.put("ssl.truststore.password", "kafka-password");

        return new DefaultKafkaProducerFactory<>(config);
    }
}
```

### Access Control Lists (ACLs)

Control who can access topics and perform operations.

```bash
# Grant producer permission
kafka-acls --bootstrap-server localhost:9092 \
  --add --allow-principal User:producer \
  --operation Write --operation Describe \
  --topic orders

# Grant consumer permission
kafka-acls --bootstrap-server localhost:9092 \
  --add --allow-principal User:consumer \
  --operation Read --operation Describe \
  --topic orders \
  --group order-processor

# Grant admin permissions
kafka-acls --bootstrap-server localhost:9092 \
  --add --allow-principal User:admin \
  --operation All \
  --topic '*' \
  --cluster

# List ACLs
kafka-acls --bootstrap-server localhost:9092 \
  --list --topic orders

# Remove ACL
kafka-acls --bootstrap-server localhost:9092 \
  --remove --allow-principal User:producer \
  --operation Write \
  --topic orders
```

## Monitoring and Metrics

### JMX Metrics

Kafka exposes metrics via JMX (Java Management Extensions).

#### Enable JMX

```bash
# Set JMX port
export KAFKA_JMX_OPTS="-Dcom.sun.management.jmxremote \
  -Dcom.sun.management.jmxremote.port=9999 \
  -Dcom.sun.management.jmxremote.authenticate=false \
  -Dcom.sun.management.jmxremote.ssl=false"

# Start Kafka
kafka-server-start /etc/kafka/server.properties
```

#### Key Metrics to Monitor

**Broker Metrics:**
```
kafka.server:type=BrokerTopicMetrics,name=MessagesInPerSec
kafka.server:type=BrokerTopicMetrics,name=BytesInPerSec
kafka.server:type=BrokerTopicMetrics,name=BytesOutPerSec
kafka.network:type=RequestMetrics,name=RequestsPerSec,request=Produce
kafka.network:type=RequestMetrics,name=RequestsPerSec,request=FetchConsumer
kafka.server:type=ReplicaManager,name=UnderReplicatedPartitions
kafka.controller:type=KafkaController,name=ActiveControllerCount
```

**Producer Metrics:**
```
kafka.producer:type=producer-metrics,client-id=my-producer,attribute=record-send-rate
kafka.producer:type=producer-metrics,client-id=my-producer,attribute=record-error-rate
kafka.producer:type=producer-metrics,client-id=my-producer,attribute=request-latency-avg
```

**Consumer Metrics:**
```
kafka.consumer:type=consumer-fetch-manager-metrics,client-id=my-consumer,attribute=records-lag-max
kafka.consumer:type=consumer-fetch-manager-metrics,client-id=my-consumer,attribute=fetch-rate
kafka.consumer:type=consumer-coordinator-metrics,client-id=my-consumer,attribute=commit-latency-avg
```

### Spring Boot Actuator

```java
@Configuration
public class ActuatorConfig {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags(
            "application", "kafka-training",
            "environment", "production"
        );
    }
}
```

**application.properties:**

```properties
# Actuator Endpoints
management.endpoints.web.exposure.include=health,metrics,prometheus,info
management.endpoint.health.show-details=always
management.metrics.export.prometheus.enabled=true

# Kafka Metrics
management.metrics.enable.kafka=true
```

**Available Endpoints:**

```bash
# Health check
curl http://localhost:8080/actuator/health

# Metrics
curl http://localhost:8080/actuator/metrics

# Prometheus metrics
curl http://localhost:8080/actuator/prometheus

# Kafka consumer metrics
curl http://localhost:8080/actuator/metrics/kafka.consumer.fetch.manager.records.lag.max
```

### Consumer Lag Monitoring

```java
@Service
public class ConsumerLagMonitor {

    @Autowired
    private AdminClient adminClient;

    public Map<TopicPartition, Long> getConsumerLag(String groupId) {
        Map<TopicPartition, Long> lagMap = new HashMap<>();

        try {
            // Get consumer group offsets
            Map<TopicPartition, OffsetAndMetadata> offsets =
                adminClient.listConsumerGroupOffsets(groupId)
                    .partitionsToOffsetAndMetadata()
                    .get();

            // Get latest offsets
            Map<TopicPartition, Long> endOffsets = new HashMap<>();
            for (TopicPartition partition : offsets.keySet()) {
                ListOffsetsResult.ListOffsetsResultInfo info =
                    adminClient.listOffsets(Map.of(
                        partition,
                        OffsetSpec.latest()
                    )).partitionResult(partition).get();

                endOffsets.put(partition, info.offset());
            }

            // Calculate lag
            for (Map.Entry<TopicPartition, OffsetAndMetadata> entry :
                    offsets.entrySet()) {
                TopicPartition partition = entry.getKey();
                long currentOffset = entry.getValue().offset();
                long endOffset = endOffsets.get(partition);
                long lag = endOffset - currentOffset;

                lagMap.put(partition, lag);
            }

        } catch (Exception e) {
            log.error("Failed to get consumer lag", e);
        }

        return lagMap;
    }

    public long getTotalLag(String groupId) {
        Map<TopicPartition, Long> lagMap = getConsumerLag(groupId);
        return lagMap.values().stream().mapToLong(Long::longValue).sum();
    }

    @Scheduled(fixedRate = 60000)  // Every minute
    public void monitorLag() {
        List<String> consumerGroups = getConsumerGroups();

        for (String groupId : consumerGroups) {
            long totalLag = getTotalLag(groupId);

            if (totalLag > 10000) {
                log.warn("High consumer lag detected: group={}, lag={}",
                    groupId, totalLag);
                alertService.sendAlert("High consumer lag", groupId, totalLag);
            }
        }
    }
}
```

### Prometheus and Grafana

**docker-compose.yml:**

```yaml
services:
  prometheus:
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'

  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - grafana-storage:/var/lib/grafana

volumes:
  grafana-storage:
```

**prometheus.yml:**

```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'kafka-training'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['app:8080']

  - job_name: 'kafka-broker'
    static_configs:
      - targets: ['kafka:9999']
```

## Performance Tuning

### Producer Tuning

```properties
# Throughput Optimization
batch.size=32768                     # Larger batches
linger.ms=10                         # Wait for batch to fill
compression.type=snappy              # Compress messages
buffer.memory=67108864               # 64MB buffer
max.in.flight.requests.per.connection=5

# Latency Optimization
batch.size=16384                     # Smaller batches
linger.ms=0                          # Send immediately
compression.type=none                # No compression overhead
acks=1                               # Don't wait for all replicas
```

### Consumer Tuning

```properties
# Throughput Optimization
fetch.min.bytes=1048576              # 1MB minimum fetch
fetch.max.wait.ms=500                # Wait 500ms for data
max.poll.records=1000                # Process more records
max.partition.fetch.bytes=2097152    # 2MB per partition

# Latency Optimization
fetch.min.bytes=1                    # Don't wait for data
fetch.max.wait.ms=100                # Short wait time
max.poll.records=100                 # Smaller batches
```

### Broker Tuning

```properties
# Network Threads
num.network.threads=8                # More network threads
num.io.threads=16                    # More I/O threads

# Replication
num.replica.fetchers=4               # Parallel replication
replica.fetch.max.bytes=2097152      # 2MB per fetch

# Log Segment
log.segment.bytes=1073741824         # 1GB segments
log.retention.hours=168              # 7 days retention
log.retention.bytes=-1               # Unlimited size

# Compression
compression.type=producer            # Use producer compression
min.insync.replicas=2                # Minimum in-sync replicas

# Memory
socket.send.buffer.bytes=1048576     # 1MB send buffer
socket.receive.buffer.bytes=1048576  # 1MB receive buffer
```

## Production Configuration

### Producer Configuration

```properties
# Reliability
acks=all
enable.idempotence=true
max.in.flight.requests.per.connection=5
retries=2147483647
delivery.timeout.ms=120000

# Performance
batch.size=32768
linger.ms=10
compression.type=snappy
buffer.memory=67108864

# Monitoring
client.id=eventmart-producer-1
```

### Consumer Configuration

```properties
# Reliability
enable.auto.commit=false
isolation.level=read_committed
max.poll.interval.ms=300000
session.timeout.ms=30000
heartbeat.interval.ms=3000

# Performance
fetch.min.bytes=1024
fetch.max.wait.ms=500
max.poll.records=500

# Assignment
partition.assignment.strategy=org.apache.kafka.clients.consumer.CooperativeStickyAssignor

# Monitoring
client.id=eventmart-consumer-1
group.id=eventmart-processor
```

### Broker Configuration

```properties
# Cluster
broker.id=1
zookeeper.connect=zookeeper:2181
advertised.listeners=PLAINTEXT://kafka:9092

# Replication
default.replication.factor=3
min.insync.replicas=2
unclean.leader.election.enable=false
auto.create.topics.enable=false

# Retention
log.retention.hours=168
log.retention.bytes=-1
log.segment.bytes=1073741824

# Performance
num.network.threads=8
num.io.threads=16
socket.send.buffer.bytes=1048576
socket.receive.buffer.bytes=1048576
num.replica.fetchers=4

# Monitoring
jmx.port=9999
```

## REST API Endpoints

### Run Day 8 Demo

```bash
curl -X POST http://localhost:8080/api/training/day08/demo
```

### Get System Status

```bash
curl http://localhost:8080/api/training/day08/status
```

**Response:**

```json
{
  "kafka": {
    "status": "UP",
    "brokers": 3,
    "topics": 25,
    "activeControllers": 1
  },
  "consumers": {
    "totalGroups": 5,
    "totalLag": 234
  },
  "health": "HEALTHY"
}
```

### Get Cluster Metrics

```bash
curl http://localhost:8080/api/training/day08/metrics/cluster
```

### Get Consumer Lag

```bash
curl http://localhost:8080/api/training/day08/metrics/consumer-lag
```

### Get Security Configuration

```bash
curl http://localhost:8080/api/training/day08/config/security
```

### Get Production Configuration

```bash
curl http://localhost:8080/api/training/day08/config/production
```

## Production Checklist

!!! success "Pre-Production Checklist"
    **Security:**
    - [ ] Enable SSL/TLS encryption
    - [ ] Configure SASL authentication
    - [ ] Set up ACLs for authorization
    - [ ] Secure ZooKeeper
    - [ ] Use secrets management (Vault, AWS Secrets Manager)
    - [ ] Rotate credentials regularly

    **High Availability:**
    - [ ] Deploy at least 3 brokers
    - [ ] Set replication factor = 3
    - [ ] Set min.insync.replicas = 2
    - [ ] Disable unclean leader election
    - [ ] Use rack awareness for multi-AZ

    **Monitoring:**
    - [ ] Set up Prometheus and Grafana
    - [ ] Monitor consumer lag
    - [ ] Monitor broker health
    - [ ] Monitor disk usage
    - [ ] Set up alerting (PagerDuty, OpsGenie)
    - [ ] Configure health checks

    **Performance:**
    - [ ] Tune producer configurations
    - [ ] Tune consumer configurations
    - [ ] Tune broker configurations
    - [ ] Enable compression
    - [ ] Optimize partition count

    **Data Management:**
    - [ ] Configure retention policies
    - [ ] Set up compaction for changelog topics
    - [ ] Plan partition count based on throughput
    - [ ] Document topic naming conventions
    - [ ] Implement schema registry

    **Disaster Recovery:**
    - [ ] Configure MirrorMaker for cross-DC replication
    - [ ] Set up backups (Cruise Control, Kafka backups)
    - [ ] Document runbooks
    - [ ] Test failover procedures
    - [ ] Plan for disaster recovery

    **Operations:**
    - [ ] Automate deployments (CI/CD)
    - [ ] Version control configurations
    - [ ] Document procedures
    - [ ] Train team members
    - [ ] Establish on-call rotation

## Troubleshooting

### High Consumer Lag

**Symptoms:**
- Consumers can't keep up with producers
- Lag increasing over time

**Solutions:**
```bash
# 1. Add more consumers (up to partition count)
# 2. Increase consumer throughput
fetch.min.bytes=1048576
max.poll.records=1000

# 3. Optimize consumer processing
# 4. Add more partitions (requires rebalancing)
# 5. Use batching
```

### Out of Memory Errors

**Symptoms:**
- `OutOfMemoryError` in logs
- Broker crashes

**Solutions:**
```properties
# Increase heap size
KAFKA_HEAP_OPTS="-Xms6g -Xmx6g"

# Reduce buffer sizes
socket.send.buffer.bytes=524288
socket.receive.buffer.bytes=524288

# Reduce segment size
log.segment.bytes=536870912
```

### Under-Replicated Partitions

**Symptoms:**
- URPs > 0
- Data loss risk

**Solutions:**
```bash
# Check broker health
kafka-broker-api-versions --bootstrap-server localhost:9092

# Check replica status
kafka-topics --bootstrap-server localhost:9092 \
  --describe --under-replicated-partitions

# Increase replica fetcher threads
num.replica.fetchers=8

# Check network/disk I/O
```

### Rebalancing Issues

**Symptoms:**
- Frequent rebalancing
- Processing delays

**Solutions:**
```properties
# Increase timeouts
session.timeout.ms=45000
heartbeat.interval.ms=3000
max.poll.interval.ms=600000

# Use CooperativeStickyAssignor
partition.assignment.strategy=org.apache.kafka.clients.consumer.CooperativeStickyAssignor

# Ensure consumers process quickly
```

## Key Takeaways

!!! success "What You Learned"
    1. **Security** is critical for production Kafka deployments
    2. **Monitoring** enables proactive issue detection
    3. **Consumer lag** is the most important metric to track
    4. **Performance tuning** balances latency and throughput
    5. **ACLs** control access to topics and operations
    6. **Production configuration** requires careful planning
    7. **Troubleshooting** skills are essential for operations

## Congratulations!

You have completed the 8-day Kafka training program. You now have the skills to:

- Build production-ready Kafka applications
- Design scalable streaming architectures
- Implement security and monitoring
- Troubleshoot production issues
- Optimize performance

## Practice Exercises

Ready to practice what you learned? Complete the **[Day 08 Exercises](../exercises/day08-exercises.md)** to apply today's concepts.

These hands-on exercises will help you master the material before moving forward.

## Next Steps

**Continue Learning:**
- [Container Development](../containers/docker-basics.md)
- [Kubernetes Deployment](../deployment/deployment-guide.md)
- [API Reference](../api/training-endpoints.md)
- [Architecture Deep Dive](../architecture/system-design.md)

**Practice Projects:**
- Build a real-time analytics pipeline
- Implement event sourcing with EventMart
- Create a data lake ingestion system
- Build a fraud detection system

**Certifications:**
- Confluent Certified Developer for Apache Kafka
- Confluent Certified Administrator for Apache Kafka

---

**Keep practicing and exploring Kafka!** The best way to master Kafka is through hands-on experience with real-world projects.
