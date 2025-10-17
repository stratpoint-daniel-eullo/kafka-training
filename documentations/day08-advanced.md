# Day 8: Advanced Topics - Security, Monitoring & Production

## Learning Objectives
By the end of Day 8, you will:
- Implement comprehensive Kafka security (SSL, SASL, ACLs)
- Set up production monitoring and alerting
- Optimize performance for high-throughput scenarios
- Deploy Kafka in production environments
- Troubleshoot complex operational issues
- Implement disaster recovery strategies

## Morning Session (3 hours): Security Implementation

### 1. Security Architecture Overview

**Kafka Security Layers:**
1. **Network Security**: SSL/TLS encryption
2. **Authentication**: SASL mechanisms
3. **Authorization**: ACL-based access control
4. **Data Security**: Encryption at rest

### 2. SSL/TLS Configuration

#### Generate Certificates
```bash
# Create CA certificate
openssl req -new -x509 -keyout ca-key -out ca-cert -days 365 -passout pass:ca-password

# Create server keystore
keytool -keystore server.keystore.jks -alias server -validity 365 -genkey -keyalg RSA -storepass server-password

# Create certificate signing request
keytool -keystore server.keystore.jks -alias server -certreq -file cert-file -storepass server-password

# Sign certificate with CA
openssl x509 -req -CA ca-cert -CAkey ca-key -in cert-file -out cert-signed -days 365 -CAcreateserial -passin pass:ca-password

# Import CA certificate
keytool -keystore server.keystore.jks -alias CARoot -import -file ca-cert -storepass server-password

# Import signed certificate
keytool -keystore server.keystore.jks -alias server -import -file cert-signed -storepass server-password

# Create client truststore
keytool -keystore client.truststore.jks -alias CARoot -import -file ca-cert -storepass truststore-password
```

#### Server Configuration (server.properties)
```properties
# SSL Configuration
listeners=SSL://localhost:9093
ssl.keystore.location=/path/to/server.keystore.jks
ssl.keystore.password=server-password
ssl.key.password=server-password
ssl.truststore.location=/path/to/server.truststore.jks
ssl.truststore.password=truststore-password
ssl.client.auth=required
ssl.protocol=TLSv1.3
ssl.enabled.protocols=TLSv1.3,TLSv1.2
ssl.cipher.suites=TLS_AES_256_GCM_SHA384,TLS_CHACHA20_POLY1305_SHA256
```

#### Client SSL Configuration
```java
Properties props = new Properties();
props.put("security.protocol", "SSL");
props.put("ssl.truststore.location", "/path/to/client.truststore.jks");
props.put("ssl.truststore.password", "truststore-password");
props.put("ssl.keystore.location", "/path/to/client.keystore.jks");
props.put("ssl.keystore.password", "keystore-password");
props.put("ssl.key.password", "key-password");
```

### 3. SASL Authentication

#### SASL/SCRAM Configuration
```properties
# Server configuration
listeners=SASL_SSL://localhost:9094
security.inter.broker.protocol=SASL_SSL
sasl.mechanism.inter.broker.protocol=SCRAM-SHA-512
sasl.enabled.mechanisms=SCRAM-SHA-512

# JAAS configuration
listener.name.sasl_ssl.scram-sha-512.sasl.jaas.config=org.apache.kafka.common.security.scram.ScramLoginModule required \
  username="admin" \
  password="admin-secret";
```

#### Create SCRAM Users
```bash
# Create users
kafka-configs --zookeeper localhost:2181 --alter --add-config 'SCRAM-SHA-512=[password=alice-secret]' --entity-type users --entity-name alice
kafka-configs --zookeeper localhost:2181 --alter --add-config 'SCRAM-SHA-512=[password=bob-secret]' --entity-type users --entity-name bob

# List users
kafka-configs --zookeeper localhost:2181 --describe --entity-type users
```

#### Client SASL Configuration
```java
Properties props = new Properties();
props.put("security.protocol", "SASL_SSL");
props.put("sasl.mechanism", "SCRAM-SHA-512");
props.put("sasl.jaas.config", 
    "org.apache.kafka.common.security.scram.ScramLoginModule required " +
    "username=\"alice\" password=\"alice-secret\";");
```

### 4. Authorization with ACLs

#### Enable ACLs
```properties
# Server configuration
authorizer.class.name=kafka.security.auth.SimpleAclAuthorizer
super.users=User:admin
allow.everyone.if.no.acl.found=false
```

#### Grant Permissions
```bash
# Producer permissions
kafka-acls --authorizer-properties zookeeper.connect=localhost:2181 \
  --add --allow-principal User:alice \
  --operation Write --topic user-events

# Consumer permissions
kafka-acls --authorizer-properties zookeeper.connect=localhost:2181 \
  --add --allow-principal User:bob \
  --operation Read --topic user-events \
  --group consumer-group-1

# Admin permissions
kafka-acls --authorizer-properties zookeeper.connect=localhost:2181 \
  --add --allow-principal User:admin \
  --operation All --topic '*' --group '*'

# List ACLs
kafka-acls --authorizer-properties zookeeper.connect=localhost:2181 --list
```

## Afternoon Session (3 hours): Monitoring & Production Operations

### Exercise 1: Security Implementation

Run the Security Configuration example:

```bash
# Run Security Config demo
mvn exec:java -Dexec.mainClass="com.training.kafka.Day08Advanced.SecurityConfig"
```

This demonstrates:
- SSL configuration generation
- SASL authentication setup
- OAuth integration patterns
- Production security best practices

### Exercise 2: Comprehensive Monitoring

Run the Monitoring Example:

```bash
# Run Monitoring demo
mvn exec:java -Dexec.mainClass="com.training.kafka.Day08Advanced.MonitoringExample"
```

Key monitoring areas:
- Producer/Consumer metrics
- Cluster health monitoring
- Performance alerting
- Dashboard creation

### 3. JMX Metrics Collection

#### Key Broker Metrics
```java
// Broker metrics to monitor
public class KafkaBrokerMetrics {
    
    public void collectBrokerMetrics() {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        
        // Message rate metrics
        double messagesInPerSec = getMetric(server, "kafka.server:type=BrokerTopicMetrics,name=MessagesInPerSec");
        double bytesInPerSec = getMetric(server, "kafka.server:type=BrokerTopicMetrics,name=BytesInPerSec");
        double bytesOutPerSec = getMetric(server, "kafka.server:type=BrokerTopicMetrics,name=BytesOutPerSec");
        
        // Request metrics
        double requestHandlerAvgIdle = getMetric(server, "kafka.server:type=KafkaRequestHandlerPool,name=RequestHandlerAvgIdlePercent");
        double networkProcessorAvgIdle = getMetric(server, "kafka.network:type=SocketServer,name=NetworkProcessorAvgIdlePercent");
        
        // Log metrics
        long logSize = (Long) getMetric(server, "kafka.log:type=LogManager,name=Size");
        double logFlushRate = getMetric(server, "kafka.log:type=LogFlushStats,name=LogFlushRateAndTimeMs");
        
        // ISR metrics
        long underReplicatedPartitions = (Long) getMetric(server, "kafka.server:type=ReplicaManager,name=UnderReplicatedPartitions");
        
        logger.info("Broker Metrics:");
        logger.info("  Messages/sec: {}", messagesInPerSec);
        logger.info("  Bytes In/sec: {}", bytesInPerSec);
        logger.info("  Bytes Out/sec: {}", bytesOutPerSec);
        logger.info("  Request Handler Idle: {}%", requestHandlerAvgIdle);
        logger.info("  Under-replicated Partitions: {}", underReplicatedPartitions);
        
        // Alert on critical metrics
        if (underReplicatedPartitions > 0) {
            sendAlert("Under-replicated partitions detected: " + underReplicatedPartitions);
        }
        
        if (requestHandlerAvgIdle < 20) {
            sendAlert("High broker load - Request handler idle: " + requestHandlerAvgIdle + "%");
        }
    }
}
```

#### Consumer Lag Monitoring
```java
public class ConsumerLagMonitor {
    
    public void monitorConsumerLag() {
        AdminClient adminClient = AdminClient.create(adminProps);
        
        try {
            // Get all consumer groups
            ListConsumerGroupsResult groupsResult = adminClient.listConsumerGroups();
            Set<String> groupIds = groupsResult.all().get().stream()
                .map(ConsumerGroupListing::groupId)
                .collect(Collectors.toSet());
            
            for (String groupId : groupIds) {
                monitorGroupLag(adminClient, groupId);
            }
            
        } catch (Exception e) {
            logger.error("Error monitoring consumer lag", e);
        }
    }
    
    private void monitorGroupLag(AdminClient adminClient, String groupId) {
        try {
            DescribeConsumerGroupsResult groupResult = adminClient.describeConsumerGroups(
                Collections.singletonList(groupId));
            ConsumerGroupDescription description = groupResult.describedGroups().get(groupId).get();
            
            if (description.state() != ConsumerGroupState.STABLE) {
                logger.warn("Consumer group {} is not stable: {}", groupId, description.state());
            }
            
            ListConsumerGroupOffsetsResult offsetsResult = adminClient.listConsumerGroupOffsets(groupId);
            Map<TopicPartition, OffsetAndMetadata> offsets = offsetsResult.partitionsToOffsetAndMetadata().get();
            
            // Get latest offsets for comparison
            Set<TopicPartition> partitions = offsets.keySet();
            ListOffsetsResult latestOffsetsResult = adminClient.listOffsets(
                partitions.stream().collect(Collectors.toMap(
                    tp -> tp,
                    tp -> OffsetSpec.latest()
                ))
            );
            
            Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> latestOffsets = 
                latestOffsetsResult.all().get();
            
            long totalLag = 0;
            for (TopicPartition partition : partitions) {
                long currentOffset = offsets.get(partition).offset();
                long latestOffset = latestOffsets.get(partition).offset();
                long lag = latestOffset - currentOffset;
                totalLag += lag;
                
                if (lag > 1000) {
                    logger.warn("High lag in group {} partition {}: {} records", 
                        groupId, partition, lag);
                }
            }
            
            logger.info("Consumer group {} total lag: {} records", groupId, totalLag);
            
            if (totalLag > 10000) {
                sendAlert("High consumer lag in group " + groupId + ": " + totalLag + " records");
            }
            
        } catch (Exception e) {
            logger.error("Error monitoring lag for group {}", groupId, e);
        }
    }
}
```

### 4. Performance Optimization

#### Broker Tuning
```properties
# JVM Settings
-Xmx6g -Xms6g
-XX:+UseG1GC
-XX:MaxGCPauseMillis=20
-XX:InitiatingHeapOccupancyPercent=35

# OS Settings
# Increase file descriptor limits
ulimit -n 100000

# Increase TCP buffer sizes
net.core.rmem_default = 262144
net.core.rmem_max = 134217728
net.core.wmem_default = 262144
net.core.wmem_max = 134217728

# Kafka Broker Settings
num.network.threads=8
num.io.threads=16
socket.send.buffer.bytes=102400
socket.receive.buffer.bytes=102400
socket.request.max.bytes=104857600

# Log settings
log.flush.interval.messages=10000
log.flush.interval.ms=1000
log.retention.hours=168
log.segment.bytes=1073741824
log.retention.check.interval.ms=300000

# Replication
replica.fetch.max.bytes=1048576
replica.socket.timeout.ms=30000
replica.socket.receive.buffer.bytes=65536
```

#### Producer Optimization
```java
// High throughput producer configuration
Properties props = new Properties();
props.put(ProducerConfig.ACKS_CONFIG, "1"); // Leader acknowledgment only
props.put(ProducerConfig.BATCH_SIZE_CONFIG, 65536); // 64KB batches
props.put(ProducerConfig.LINGER_MS_CONFIG, 100); // Wait 100ms to batch
props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4"); // Fast compression
props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 134217728); // 128MB buffer
props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);

// Low latency producer configuration
props.put(ProducerConfig.ACKS_CONFIG, "1");
props.put(ProducerConfig.BATCH_SIZE_CONFIG, 1); // No batching
props.put(ProducerConfig.LINGER_MS_CONFIG, 0); // Send immediately
props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "none");
```

#### Consumer Optimization
```java
// High throughput consumer configuration
Properties props = new Properties();
props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 50000); // 50KB minimum
props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500); // Max wait 500ms
props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 2000); // Large batches
props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000);

// Parallel processing pattern
public class ParallelConsumer {
    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    
    public void processRecords(ConsumerRecords<String, String> records) {
        List<Future<?>> futures = new ArrayList<>();
        
        for (ConsumerRecord<String, String> record : records) {
            Future<?> future = executor.submit(() -> processRecord(record));
            futures.add(future);
        }
        
        // Wait for all to complete
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                logger.error("Error processing record", e);
            }
        }
    }
}
```

### 5. Disaster Recovery

#### Cross-Datacenter Replication
```properties
# MirrorMaker 2.0 configuration
clusters = source, target
source.bootstrap.servers = source-broker1:9092,source-broker2:9092
target.bootstrap.servers = target-broker1:9092,target-broker2:9092

# Replication flows
source->target.enabled = true
source->target.topics = user-events, order-events, payment-events

# Replication settings
replication.factor = 3
refresh.topics.enabled = true
refresh.topics.interval.seconds = 30

# Transform topic names for target cluster
source->target.replication.policy.class = org.apache.kafka.connect.mirror.DefaultReplicationPolicy
```

#### Backup and Recovery
```bash
# Export topic configuration
kafka-configs --bootstrap-server localhost:9092 --describe --entity-type topics --entity-name user-events

# Export consumer group offsets
kafka-run-class kafka.tools.ExportZkOffsets \
  --zkconnect localhost:2181 \
  --group consumer-group-1 \
  --output-file offsets.txt

# Import consumer group offsets
kafka-run-class kafka.tools.ImportZkOffsets \
  --zkconnect localhost:2181 \
  --input-file offsets.txt

# Cluster metadata backup
kafka-cluster-backup --bootstrap-server localhost:9092 --backup-dir /backup/kafka-metadata
```

## Troubleshooting Guide

### 1. Common Production Issues

#### Broker Memory Issues
```bash
# Symptoms: OutOfMemoryError, high GC overhead
# Solutions:
# 1. Increase heap size: -Xmx8g
# 2. Tune GC: -XX:+UseG1GC -XX:MaxGCPauseMillis=20
# 3. Reduce log segment size: log.segment.bytes=536870912
# 4. Enable log compression: compression.type=lz4
```

#### Network Saturation
```bash
# Symptoms: High network utilization, timeouts
# Solutions:
# 1. Increase network threads: num.network.threads=16
# 2. Tune socket buffers: socket.send.buffer.bytes=1048576
# 3. Enable compression: compression.type=snappy
# 4. Scale horizontally: add more brokers
```

#### Disk I/O Issues
```bash
# Symptoms: High disk utilization, slow writes
# Solutions:
# 1. Use dedicated disks for logs
# 2. Increase I/O threads: num.io.threads=16
# 3. Tune flush settings: log.flush.interval.ms=10000
# 4. Use faster storage: NVMe SSDs
```

### 2. Monitoring Dashboards

#### Grafana Dashboard Metrics
```json
{
  "dashboard": {
    "title": "Kafka Cluster Monitoring",
    "panels": [
      {
        "title": "Message Rate",
        "targets": [
          {
            "expr": "kafka_server_BrokerTopicMetrics_MessagesInPerSec",
            "legendFormat": "Messages In/sec"
          }
        ]
      },
      {
        "title": "Consumer Lag",
        "targets": [
          {
            "expr": "kafka_consumer_lag_max",
            "legendFormat": "Max Lag"
          }
        ]
      },
      {
        "title": "Under-replicated Partitions",
        "targets": [
          {
            "expr": "kafka_server_ReplicaManager_UnderReplicatedPartitions",
            "legendFormat": "Under-replicated"
          }
        ]
      }
    ]
  }
}
```

#### Alerting Rules
```yaml
# Prometheus alerting rules
groups:
- name: kafka.rules
  rules:
  - alert: KafkaConsumerLag
    expr: kafka_consumer_lag_max > 1000
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "High consumer lag detected"
      
  - alert: KafkaUnderReplicatedPartitions
    expr: kafka_server_ReplicaManager_UnderReplicatedPartitions > 0
    for: 1m
    labels:
      severity: critical
    annotations:
      summary: "Under-replicated partitions detected"
      
  - alert: KafkaBrokerDown
    expr: up{job="kafka"} == 0
    for: 30s
    labels:
      severity: critical
    annotations:
      summary: "Kafka broker is down"
```

## Production Deployment Checklist

### 1. Infrastructure
- [ ] Dedicated hardware/VMs for Kafka brokers
- [ ] Separate disks for logs and OS
- [ ] Network bandwidth sufficient for peak load
- [ ] Monitoring and logging infrastructure in place

### 2. Security
- [ ] SSL/TLS enabled for all communications
- [ ] SASL authentication configured
- [ ] ACLs implemented for authorization
- [ ] Network firewalls configured
- [ ] Certificate rotation procedure established

### 3. High Availability
- [ ] Minimum 3 brokers for fault tolerance
- [ ] Replication factor ≥ 3 for critical topics
- [ ] Cross-AZ deployment
- [ ] Backup and recovery procedures tested

### 4. Monitoring
- [ ] JMX metrics collection enabled
- [ ] Dashboards for cluster health
- [ ] Alerting for critical issues
- [ ] Log aggregation configured
- [ ] Performance baseline established

### 5. Operations
- [ ] Runbooks for common issues
- [ ] Deployment automation
- [ ] Rolling update procedures
- [ ] Capacity planning process
- [ ] Incident response plan

## Key Takeaways

1. **Security** requires multiple layers: encryption, authentication, and authorization
2. **Monitoring** is essential for maintaining healthy Kafka clusters
3. **Performance tuning** involves broker, producer, and consumer optimizations
4. **Disaster recovery** planning prevents data loss and ensures business continuity
5. **Operations** procedures ensure reliable production deployments
6. **Troubleshooting** skills are critical for maintaining uptime

## Advanced Topics Summary

| Topic | Key Concepts | Production Impact |
|-------|--------------|-------------------|
| Security | SSL, SASL, ACLs | Data protection, compliance |
| Monitoring | JMX, metrics, alerting | Operational visibility |
| Performance | Tuning, optimization | Throughput, latency |
| Disaster Recovery | Replication, backup | Business continuity |
| Troubleshooting | Debugging, resolution | System reliability |

## Final Course Summary

**Congratulations!** You've completed the comprehensive Kafka training course covering:

- **Day 1-2**: Fundamentals and data flow patterns
- **Day 3-4**: Producer and consumer development
- **Day 5**: Stream processing with Kafka Streams
- **Day 6**: Schema management with Avro
- **Day 7**: Data integration with Kafka Connect
- **Day 8**: Advanced security, monitoring, and production operations

You're now equipped to build, deploy, and operate production Kafka systems!

---

**🎉 Course Complete!** You've mastered Apache Kafka from fundamentals to production deployment.