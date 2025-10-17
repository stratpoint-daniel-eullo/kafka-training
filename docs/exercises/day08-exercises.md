# Day 8 Exercises: Advanced Security and Monitoring

## Exercise 1: SSL/TLS Security Configuration

### Objective
Implement SSL/TLS encryption for secure Kafka communication.

### Setup
```bash
# Create certificates directory
mkdir -p /tmp/kafka-security/certs
cd /tmp/kafka-security/certs
```

### Part A: Generate Certificates

1. **Create CA Certificate**
   ```bash
   # Generate CA key and certificate
   openssl req -new -x509 -keyout ca-key -out ca-cert -days 365 \
     -passout pass:ca-password \
     -subj "/C=US/ST=CA/L=San Francisco/O=Training/OU=Kafka/CN=ca"
   ```

2. **Create Server Keystore**
   ```bash
   # Generate server keystore
   keytool -keystore server.keystore.jks -alias server -validity 365 \
     -genkey -keyalg RSA -storepass server-password \
     -dname "CN=localhost, OU=Training, O=Kafka, L=San Francisco, S=CA, C=US"
   ```

3. **Create Certificate Signing Request**
   ```bash
   # Create CSR
   keytool -keystore server.keystore.jks -alias server -certreq \
     -file cert-file -storepass server-password
   
   # Sign with CA
   openssl x509 -req -CA ca-cert -CAkey ca-key -in cert-file \
     -out cert-signed -days 365 -CAcreateserial -passin pass:ca-password
   ```

4. **Complete Keystore Setup**
   ```bash
   # Import CA certificate
   keytool -keystore server.keystore.jks -alias CARoot -import \
     -file ca-cert -storepass server-password -noprompt
   
   # Import signed certificate
   keytool -keystore server.keystore.jks -alias server -import \
     -file cert-signed -storepass server-password -noprompt
   
   # Create client truststore
   keytool -keystore client.truststore.jks -alias CARoot -import \
     -file ca-cert -storepass truststore-password -noprompt
   ```

### Part B: Configure SSL Producer

1. **Create SSL Producer Configuration**
   ```java
   Properties props = new Properties();
   props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9093");
   props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
   props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
   
   // SSL Configuration
   props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
   props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "/tmp/kafka-security/certs/client.truststore.jks");
   props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "truststore-password");
   props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, "/tmp/kafka-security/certs/client.keystore.jks");
   props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "keystore-password");
   props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, "key-password");
   ```

2. **Test SSL Connection**
   ```bash
   # Run Security Config demo
   mvn exec:java -Dexec.mainClass="com.training.kafka.Day08Advanced.SecurityConfig"
   ```

### Expected Results
- SSL certificates generated successfully
- Secure producer connection established
- Understanding of certificate-based security

### Questions to Answer
1. Why is SSL/TLS important for Kafka?
2. How does certificate validation work?
3. What are the performance implications of SSL?

---

## Exercise 2: SASL Authentication

### Objective
Implement SASL authentication for user-based security.

### Part A: SASL/SCRAM Configuration

1. **Create SCRAM Users** (if Kafka server supports it)
   ```bash
   # Add users to Kafka
   kafka-configs --zookeeper localhost:2181 --alter \
     --add-config 'SCRAM-SHA-512=[password=alice-secret]' \
     --entity-type users --entity-name alice
   
   kafka-configs --zookeeper localhost:2181 --alter \
     --add-config 'SCRAM-SHA-512=[password=bob-secret]' \
     --entity-type users --entity-name bob
   ```

2. **Configure SASL Producer**
   ```java
   Properties props = new Properties();
   props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9094");
   props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
   props.put(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-512");
   props.put(SaslConfigs.SASL_JAAS_CONFIG, 
       "org.apache.kafka.common.security.scram.ScramLoginModule required " +
       "username=\"alice\" password=\"alice-secret\";");
   ```

### Part B: OAuth Configuration (Conceptual)

1. **OAuth Configuration Pattern**
   ```java
   Properties props = new Properties();
   props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
   props.put(SaslConfigs.SASL_MECHANISM, "OAUTHBEARER");
   props.put(SaslConfigs.SASL_JAAS_CONFIG, 
       "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required;");
   
   // OAuth-specific configurations
   props.put("sasl.oauthbearer.token.endpoint.url", "https://auth.example.com/token");
   props.put("sasl.oauthbearer.client.id", "kafka-client");
   props.put("sasl.oauthbearer.client.secret", "client-secret");
   ```

### Expected Results
- Understanding of SASL mechanisms
- User-based authentication patterns
- Modern OAuth integration concepts

---

## Exercise 3: Access Control Lists (ACLs)

### Objective
Implement fine-grained authorization using ACLs.

### Part A: Topic-Level ACLs

1. **Grant Producer Permissions**
   ```bash
   # Allow alice to write to user-events topic
   kafka-acls --authorizer-properties zookeeper.connect=localhost:2181 \
     --add --allow-principal User:alice \
     --operation Write --topic user-events
   
   # Allow alice to describe topics
   kafka-acls --authorizer-properties zookeeper.connect=localhost:2181 \
     --add --allow-principal User:alice \
     --operation Describe --topic user-events
   ```

2. **Grant Consumer Permissions**
   ```bash
   # Allow bob to read from user-events topic
   kafka-acls --authorizer-properties zookeeper.connect=localhost:2181 \
     --add --allow-principal User:bob \
     --operation Read --topic user-events
   
   # Allow bob to join consumer group
   kafka-acls --authorizer-properties zookeeper.connect=localhost:2181 \
     --add --allow-principal User:bob \
     --operation Read --group consumer-group-1
   ```

### Part B: Administrative ACLs

1. **Grant Admin Permissions**
   ```bash
   # Allow admin user full access
   kafka-acls --authorizer-properties zookeeper.connect=localhost:2181 \
     --add --allow-principal User:admin \
     --operation All --topic '*' --group '*'
   
   # Allow cluster operations
   kafka-acls --authorizer-properties zookeeper.connect=localhost:2181 \
     --add --allow-principal User:admin \
     --operation ClusterAction --cluster
   ```

2. **List and Verify ACLs**
   ```bash
   # List all ACLs
   kafka-acls --authorizer-properties zookeeper.connect=localhost:2181 --list
   
   # List ACLs for specific topic
   kafka-acls --authorizer-properties zookeeper.connect=localhost:2181 \
     --list --topic user-events
   ```

### Expected Results
- Granular permission control
- Understanding of principal-based security
- Role-based access patterns

---

## Exercise 4: Comprehensive Monitoring

### Objective
Set up production-grade monitoring for Kafka clusters.

### Part A: JMX Metrics Collection

1. **Run Monitoring Example**
   ```bash
   mvn exec:java -Dexec.mainClass="com.training.kafka.Day08Advanced.MonitoringExample"
   ```

2. **Create Custom Metrics Dashboard**
   ```java
   public class KafkaMetricsDashboard {
       public void displayMetrics(KafkaProducer<String, String> producer,
                                 KafkaConsumer<String, String> consumer) {
           
           // Producer metrics
           Map<MetricName, ? extends Metric> producerMetrics = producer.metrics();
           double recordSendRate = getMetricValue(producerMetrics, "record-send-rate");
           double requestLatency = getMetricValue(producerMetrics, "request-latency-avg");
           
           // Consumer metrics  
           Map<MetricName, ? extends Metric> consumerMetrics = consumer.metrics();
           double recordsLag = getMetricValue(consumerMetrics, "records-lag-max");
           double fetchRate = getMetricValue(consumerMetrics, "fetch-rate");
           
           // Display dashboard
           System.out.println("=== Kafka Metrics Dashboard ===");
           System.out.printf("Producer Send Rate: %.2f records/sec%n", recordSendRate);
           System.out.printf("Producer Latency: %.2f ms%n", requestLatency);
           System.out.printf("Consumer Lag: %.0f records%n", recordsLag);
           System.out.printf("Consumer Fetch Rate: %.2f fetches/sec%n", fetchRate);
       }
   }
   ```

### Part B: Alerting Implementation

1. **Create Alert Manager**
   ```java
   public class KafkaAlertManager {
       private static final double MAX_LATENCY_THRESHOLD = 100.0;
       private static final double MAX_LAG_THRESHOLD = 1000.0;
       
       public void checkAlerts(Map<MetricName, ? extends Metric> metrics) {
           double latency = getMetricValue(metrics, "request-latency-avg");
           double lag = getMetricValue(metrics, "records-lag-max");
           
           if (latency > MAX_LATENCY_THRESHOLD) {
               sendAlert("HIGH_LATENCY", "Request latency: " + latency + "ms");
           }
           
           if (lag > MAX_LAG_THRESHOLD) {
               sendAlert("HIGH_LAG", "Consumer lag: " + lag + " records");
           }
       }
       
       private void sendAlert(String type, String message) {
           // In production: send to Slack, PagerDuty, email, etc.
           System.err.println("ALERT [" + type + "]: " + message);
       }
   }
   ```

### Part C: Health Checks

1. **Implement Health Check Endpoints**
   ```java
   public class KafkaHealthCheck {
       public HealthStatus checkProducerHealth(KafkaProducer<String, String> producer) {
           try {
               // Send test message with timeout
               ProducerRecord<String, String> testRecord = 
                   new ProducerRecord<>("health-check", "ping");
               
               RecordMetadata metadata = producer.send(testRecord)
                   .get(5, TimeUnit.SECONDS);
               
               return HealthStatus.healthy("Producer responsive");
               
           } catch (Exception e) {
               return HealthStatus.unhealthy("Producer failed: " + e.getMessage());
           }
       }
       
       public HealthStatus checkConsumerHealth(KafkaConsumer<String, String> consumer) {
           try {
               Set<TopicPartition> assignment = consumer.assignment();
               if (assignment.isEmpty()) {
                   return HealthStatus.unhealthy("No partitions assigned");
               }
               
               // Check lag
               Map<MetricName, ? extends Metric> metrics = consumer.metrics();
               double lag = getMetricValue(metrics, "records-lag-max");
               
               if (lag > 10000) {
                   return HealthStatus.degraded("High lag: " + lag + " records");
               }
               
               return HealthStatus.healthy("Consumer healthy");
               
           } catch (Exception e) {
               return HealthStatus.unhealthy("Consumer check failed: " + e.getMessage());
           }
       }
   }
   ```

### Expected Results
- Real-time metrics monitoring
- Automated alerting system
- Health check implementation
- Production monitoring patterns

---

## Exercise 5: Performance Optimization

### Objective
Optimize Kafka clients for production workloads.

### Part A: Producer Optimization

1. **High Throughput Configuration**
   ```java
   Properties highThroughputProps = new Properties();
   
   // Batch optimization
   highThroughputProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 65536); // 64KB
   highThroughputProps.put(ProducerConfig.LINGER_MS_CONFIG, 100); // Wait 100ms
   
   // Compression
   highThroughputProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
   
   // Memory
   highThroughputProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 134217728); // 128MB
   
   // Reliability vs Performance
   highThroughputProps.put(ProducerConfig.ACKS_CONFIG, "1"); // Leader only
   ```

2. **Low Latency Configuration**
   ```java
   Properties lowLatencyProps = new Properties();
   
   // No batching
   lowLatencyProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 1);
   lowLatencyProps.put(ProducerConfig.LINGER_MS_CONFIG, 0);
   
   // No compression
   lowLatencyProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "none");
   
   // Fast acknowledgment
   lowLatencyProps.put(ProducerConfig.ACKS_CONFIG, "0");
   ```

### Part B: Consumer Optimization

1. **High Throughput Consumer**
   ```java
   Properties highThroughputConsumerProps = new Properties();
   
   // Large fetch sizes
   highThroughputConsumerProps.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 50000);
   highThroughputConsumerProps.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);
   highThroughputConsumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 2000);
   ```

2. **Performance Testing**
   ```java
   public void performanceTest() {
       long startTime = System.currentTimeMillis();
       int messageCount = 10000;
       
       // Send messages
       for (int i = 0; i < messageCount; i++) {
           producer.send(new ProducerRecord<>("perf-test", "message-" + i));
       }
       producer.flush();
       
       long duration = System.currentTimeMillis() - startTime;
       double throughput = (double) messageCount / (duration / 1000.0);
       
       System.out.printf("Sent %d messages in %d ms%n", messageCount, duration);
       System.out.printf("Throughput: %.2f messages/sec%n", throughput);
   }
   ```

### Expected Results
- Understanding of performance trade-offs
- Optimized configurations for different use cases
- Performance measurement techniques

---

## Exercise 6: Disaster Recovery and Backup

### Objective
Implement backup and disaster recovery strategies.

### Part A: Topic Configuration Backup

1. **Export Topic Configurations**
   ```bash
   # Export all topic configurations
   kafka-configs --bootstrap-server localhost:9092 \
     --describe --entity-type topics > topic-configs-backup.txt
   
   # Export specific topic
   kafka-configs --bootstrap-server localhost:9092 \
     --describe --entity-type topics --entity-name user-events
   ```

2. **Export Consumer Group Offsets**
   ```bash
   # List consumer groups
   kafka-consumer-groups --bootstrap-server localhost:9092 --list
   
   # Export offsets for specific group
   kafka-consumer-groups --bootstrap-server localhost:9092 \
     --group my-consumer-group --describe > offsets-backup.txt
   ```

### Part B: Cross-Datacenter Replication (Conceptual)

1. **MirrorMaker 2.0 Configuration**
   ```properties
   # mm2.properties
   clusters = source, target
   source.bootstrap.servers = source-cluster:9092
   target.bootstrap.servers = target-cluster:9092
   
   # Replication flow
   source->target.enabled = true
   source->target.topics = user-events, order-events
   
   # Replication settings
   replication.factor = 3
   refresh.topics.enabled = true
   refresh.topics.interval.seconds = 30
   ```

### Expected Results
- Backup strategies for topic metadata
- Understanding of replication patterns
- Disaster recovery planning

---

## Key Learning Outcomes

After completing these exercises, you should understand:

1. **SSL/TLS Security**: Certificate-based encryption
2. **SASL Authentication**: User-based security mechanisms
3. **ACL Authorization**: Fine-grained access control
4. **Monitoring**: Production metrics and alerting
5. **Performance**: Optimization strategies and trade-offs
6. **Disaster Recovery**: Backup and replication patterns

## Common Security Issues and Solutions

### Issue 1: Certificate Validation Failures
**Problem**: SSL handshake failures
**Solutions**:
```bash
# Verify certificate chain
openssl verify -CAfile ca-cert server-cert

# Check certificate expiration
openssl x509 -in cert-file -noout -dates

# Verify keystore contents
keytool -list -keystore server.keystore.jks
```

### Issue 2: Authentication Failures
**Problem**: SASL authentication errors
**Solutions**:
```bash
# Verify user exists
kafka-configs --zookeeper localhost:2181 --describe --entity-type users

# Check JAAS configuration
# Verify username/password combination
```

### Issue 3: Authorization Denied
**Problem**: ACL permission errors
**Solutions**:
```bash
# List user permissions
kafka-acls --authorizer-properties zookeeper.connect=localhost:2181 \
  --list --principal User:alice

# Add missing permissions
kafka-acls --authorizer-properties zookeeper.connect=localhost:2181 \
  --add --allow-principal User:alice \
  --operation Read --topic user-events
```

## Production Security Checklist

- [ ] SSL/TLS enabled for all communications
- [ ] Strong authentication mechanisms (SASL/SCRAM or OAuth)
- [ ] ACLs configured with least privilege principle
- [ ] Certificate rotation procedures established
- [ ] Security monitoring and alerting in place
- [ ] Regular security audits scheduled
- [ ] Backup and disaster recovery tested
- [ ] Network segmentation implemented
- [ ] Security patches up to date

## Cleanup
```bash
# Remove test certificates
rm -rf /tmp/kafka-security

# Remove test ACLs (if created)
kafka-acls --authorizer-properties zookeeper.connect=localhost:2181 \
  --remove --principal User:alice

# Remove test users (if created)
kafka-configs --zookeeper localhost:2181 --alter \
  --delete-config 'SCRAM-SHA-512' \
  --entity-type users --entity-name alice
```

---

**🎉 Congratulations!** You've completed the comprehensive Kafka security and monitoring exercises. You now have production-ready skills for securing and operating Kafka clusters.
