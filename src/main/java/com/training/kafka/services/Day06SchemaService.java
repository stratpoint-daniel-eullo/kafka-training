package com.training.kafka.services;

import com.training.kafka.avro.*;
import com.training.kafka.config.TrainingKafkaProperties;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Day 6: Schema Management with Avro and Schema Registry
 *
 * This service demonstrates:
 * - Avro schema usage with Kafka
 * - Schema Registry integration
 * - Schema evolution and compatibility
 * - Avro producer and consumer operations
 * - EventMart events with Avro serialization
 *
 * @author Kafka Training Course
 * @version 1.0.0
 */
@Service
public class Day06SchemaService {

    private static final Logger logger = LoggerFactory.getLogger(Day06SchemaService.class);

    @Autowired
    private TrainingKafkaProperties kafkaProperties;

    private SchemaRegistryClient schemaRegistryClient;
    private KafkaProducer<String, Object> avroProducer;
    private KafkaConsumer<String, Object> avroConsumer;

    @PostConstruct
    public void init() {
        String schemaRegistryUrl = kafkaProperties.getKafka().getSchemaRegistryUrl();
        schemaRegistryClient = new CachedSchemaRegistryClient(schemaRegistryUrl, 100);
        logger.info("Schema Registry client initialized with URL: {}", schemaRegistryUrl);
    }

    @PreDestroy
    public void cleanup() {
        if (avroProducer != null) {
            avroProducer.close();
        }
        if (avroConsumer != null) {
            avroConsumer.close();
        }
        logger.info("Day06SchemaService cleanup complete");
    }

    /**
     * Demonstrate Day 6 Schema Management concepts
     */
    public void demonstrateSchemaManagement() {
        logger.info("=".repeat(80));
        logger.info("Day 6: Schema Management with Avro and Schema Registry");
        logger.info("=".repeat(80));

        try {
            // 1. Create topics for Avro events
            logger.info("\n1. Creating Avro event topics...");
            createAvroTopics();

            // 2. Demonstrate Avro producer
            logger.info("\n2. Producing Avro events...");
            produceAvroEvents();

            // 3. Demonstrate Avro consumer
            logger.info("\n3. Consuming Avro events...");
            consumeAvroEvents();

            // 4. List registered schemas
            logger.info("\n4. Listing registered schemas...");
            listRegisteredSchemas();

            // 5. Demonstrate schema compatibility
            logger.info("\n5. Checking schema compatibility...");
            checkSchemaCompatibility();

            logger.info("\nDay 6 demonstration complete!");

        } catch (Exception e) {
            logger.error("Error in Day 6 demonstration", e);
        }
    }

    /**
     * Create topics for Avro events
     */
    private void createAvroTopics() {
        logger.info("Topics for Avro events:");
        logger.info("  - user-events-avro: User activity events");
        logger.info("  - product-events-avro: Product management events");
        logger.info("  - order-events-avro: Order lifecycle events");
        logger.info("  - payment-events-avro: Payment processing events");
    }

    /**
     * Produce Avro events to Kafka topics
     */
    public void produceAvroEvents() {
        KafkaProducer<String, Object> producer = createAvroProducer();

        try {
            // 1. Produce User Event
            UserEvent userEvent = createSampleUserEvent();
            ProducerRecord<String, Object> userRecord =
                new ProducerRecord<>("user-events-avro", userEvent.getUserId().toString(), userEvent);
            RecordMetadata userMeta = producer.send(userRecord).get();
            logger.info("Produced UserEvent: topic={}, partition={}, offset={}",
                userMeta.topic(), userMeta.partition(), userMeta.offset());

            // 2. Produce Product Event
            ProductEvent productEvent = createSampleProductEvent();
            ProducerRecord<String, Object> productRecord =
                new ProducerRecord<>("product-events-avro", productEvent.getProductId().toString(), productEvent);
            RecordMetadata productMeta = producer.send(productRecord).get();
            logger.info("Produced ProductEvent: topic={}, partition={}, offset={}",
                productMeta.topic(), productMeta.partition(), productMeta.offset());

            // 3. Produce Order Event
            OrderEvent orderEvent = createSampleOrderEvent();
            ProducerRecord<String, Object> orderRecord =
                new ProducerRecord<>("order-events-avro", orderEvent.getOrderId().toString(), orderEvent);
            RecordMetadata orderMeta = producer.send(orderRecord).get();
            logger.info("Produced OrderEvent: topic={}, partition={}, offset={}",
                orderMeta.topic(), orderMeta.partition(), orderMeta.offset());

            // 4. Produce Payment Event
            PaymentEvent paymentEvent = createSamplePaymentEvent();
            ProducerRecord<String, Object> paymentRecord =
                new ProducerRecord<>("payment-events-avro", paymentEvent.getPaymentId().toString(), paymentEvent);
            RecordMetadata paymentMeta = producer.send(paymentRecord).get();
            logger.info("Produced PaymentEvent: topic={}, partition={}, offset={}",
                paymentMeta.topic(), paymentMeta.partition(), paymentMeta.offset());

            logger.info("All Avro events produced successfully!");

        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error producing Avro events", e);
        } finally {
            producer.close();
        }
    }

    /**
     * Consume Avro events from Kafka topics
     */
    public void consumeAvroEvents() {
        KafkaConsumer<String, Object> consumer = createAvroConsumer();

        try {
            consumer.subscribe(Arrays.asList(
                "user-events-avro",
                "product-events-avro",
                "order-events-avro",
                "payment-events-avro"
            ));

            ConsumerRecords<String, Object> records = consumer.poll(Duration.ofSeconds(5));
            logger.info("Consumed {} Avro records", records.count());

            for (ConsumerRecord<String, Object> record : records) {
                processAvroRecord(record);
            }

        } catch (Exception e) {
            logger.error("Error consuming Avro events", e);
        } finally {
            consumer.close();
        }
    }

    /**
     * Process individual Avro record based on type
     */
    private void processAvroRecord(ConsumerRecord<String, Object> record) {
        Object value = record.value();

        if (value instanceof UserEvent) {
            UserEvent event = (UserEvent) value;
            logger.info("Consumed UserEvent: userId={}, action={}, timestamp={}",
                event.getUserId(), event.getAction(), event.getTimestamp());
        } else if (value instanceof ProductEvent) {
            ProductEvent event = (ProductEvent) value;
            logger.info("Consumed ProductEvent: productId={}, eventType={}, price=${}",
                event.getProductId(), event.getEventType(), event.getPrice());
        } else if (value instanceof OrderEvent) {
            OrderEvent event = (OrderEvent) value;
            logger.info("Consumed OrderEvent: orderId={}, eventType={}, totalAmount=${}",
                event.getOrderId(), event.getEventType(), event.getTotalAmount());
        } else if (value instanceof PaymentEvent) {
            PaymentEvent event = (PaymentEvent) value;
            logger.info("Consumed PaymentEvent: paymentId={}, eventType={}, amount=${}",
                event.getPaymentId(), event.getEventType(), event.getAmount());
        }
    }

    /**
     * List all registered schemas in Schema Registry
     */
    public Map<String, Object> listRegisteredSchemas() {
        Map<String, Object> result = new HashMap<>();

        try {
            Collection<String> subjects = schemaRegistryClient.getAllSubjects();
            List<Map<String, Object>> schemaList = new ArrayList<>();

            logger.info("Found {} registered schemas:", subjects.size());

            for (String subject : subjects) {
                int version = schemaRegistryClient.getLatestSchemaMetadata(subject).getVersion();
                int id = schemaRegistryClient.getLatestSchemaMetadata(subject).getId();

                Map<String, Object> schemaInfo = new HashMap<>();
                schemaInfo.put("subject", subject);
                schemaInfo.put("version", version);
                schemaInfo.put("id", id);
                schemaList.add(schemaInfo);

                logger.info("  - {}: version={}, id={}", subject, version, id);
            }

            result.put("status", "success");
            result.put("count", subjects.size());
            result.put("schemas", schemaList);

        } catch (IOException | RestClientException e) {
            logger.error("Error listing schemas", e);
            result.put("status", "error");
            result.put("message", e.getMessage());
        }

        return result;
    }

    /**
     * Check schema compatibility for a given subject
     */
    public Map<String, Object> checkSchemaCompatibility(String subject, String schemaString) {
        Map<String, Object> result = new HashMap<>();

        try {
            org.apache.avro.Schema parsedSchema = new org.apache.avro.Schema.Parser().parse(schemaString);
            AvroSchema avroSchema = new AvroSchema(parsedSchema);
            boolean compatible = schemaRegistryClient.testCompatibility(subject, avroSchema);

            result.put("status", "success");
            result.put("subject", subject);
            result.put("compatible", compatible);

            logger.info("Schema compatibility check for {}: {}", subject, compatible);

        } catch (IOException | RestClientException e) {
            logger.error("Error checking compatibility", e);
            result.put("status", "error");
            result.put("message", e.getMessage());
        }

        return result;
    }

    /**
     * Check compatibility for all EventMart schemas
     */
    private void checkSchemaCompatibility() {
        logger.info("Schema compatibility mode: BACKWARD (default)");
        logger.info("  - New schema can read data written by old schema");
        logger.info("  - Safe to add optional fields with defaults");
        logger.info("  - Cannot remove required fields");
    }

    /**
     * Create Avro producer with Schema Registry support
     */
    private KafkaProducer<String, Object> createAvroProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getKafka().getBootstrapServers());
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "avro-producer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        props.put("schema.registry.url", kafkaProperties.getKafka().getSchemaRegistryUrl());
        props.put(ProducerConfig.ACKS_CONFIG, "all");

        return new KafkaProducer<>(props);
    }

    /**
     * Create Avro consumer with Schema Registry support
     */
    private KafkaConsumer<String, Object> createAvroConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getKafka().getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "avro-consumer-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        props.put("schema.registry.url", kafkaProperties.getKafka().getSchemaRegistryUrl());
        props.put("specific.avro.reader", "true");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new KafkaConsumer<>(props);
    }

    // ===== Sample Data Creation =====

    private UserEvent createSampleUserEvent() {
        return UserEvent.newBuilder()
            .setUserId("user-001")
            .setAction(ActionType.LOGIN)
            .setTimestamp(System.currentTimeMillis())
            .setSessionId("session-" + UUID.randomUUID().toString())
            .setProperties(new HashMap<>())
            .setDeviceInfo(DeviceInfo.newBuilder()
                .setDeviceType("Desktop")
                .setUserAgent("Mozilla/5.0")
                .setIpAddress("192.168.1.100")
                .setLocation("San Francisco, CA")
                .build())
            .setVersion(1)
            .build();
    }

    private ProductEvent createSampleProductEvent() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("color", "Black");
        attributes.put("size", "Large");

        return ProductEvent.newBuilder()
            .setProductId("prod-001")
            .setEventType(ProductEventType.CREATED)
            .setTimestamp(System.currentTimeMillis())
            .setName("Wireless Headphones")
            .setCategory("Electronics")
            .setPrice(99.99)
            .setStockQuantity(50)
            .setDescription("Premium wireless headphones with noise cancellation")
            .setAttributes(attributes)
            .setTags(Arrays.asList("electronics", "audio", "wireless"))
            .setVendor(VendorInfo.newBuilder()
                .setVendorId("vendor-001")
                .setVendorName("TechVendor Inc")
                .setRating(4.5)
                .build())
            .setVersion(1)
            .build();
    }

    private OrderEvent createSampleOrderEvent() {
        List<OrderItem> items = new ArrayList<>();
        items.add(OrderItem.newBuilder()
            .setProductId("prod-001")
            .setProductName("Wireless Headphones")
            .setQuantity(2)
            .setPricePerUnit(99.99)
            .setTotalPrice(199.98)
            .build());

        return OrderEvent.newBuilder()
            .setOrderId("order-001")
            .setEventType(OrderEventType.CREATED)
            .setTimestamp(System.currentTimeMillis())
            .setUserId("user-001")
            .setOrderItems(items)
            .setTotalAmount(199.98)
            .setStatus("PENDING")
            .setShippingAddress(Address.newBuilder()
                .setStreet("123 Main St")
                .setCity("San Francisco")
                .setState("CA")
                .setZipCode("94102")
                .setCountry("USA")
                .build())
            .setPaymentInfo(PaymentInfo.newBuilder()
                .setPaymentMethod("CREDIT_CARD")
                .setPaymentId("pay-001")
                .setPaymentStatus("PENDING")
                .build())
            .setMetadata(new HashMap<>())
            .setVersion(1)
            .build();
    }

    private PaymentEvent createSamplePaymentEvent() {
        return PaymentEvent.newBuilder()
            .setPaymentId("pay-001")
            .setEventType(PaymentEventType.AUTHORIZED)
            .setTimestamp(System.currentTimeMillis())
            .setOrderId("order-001")
            .setUserId("user-001")
            .setAmount(199.98)
            .setCurrency("USD")
            .setPaymentMethod(PaymentMethod.CREDIT_CARD)
            .setStatus("AUTHORIZED")
            .setProcessorTransactionId("txn-" + UUID.randomUUID().toString())
            .setCardInfo(CardInfo.newBuilder()
                .setCardType("VISA")
                .setLastFourDigits("1234")
                .setExpiryMonth(12)
                .setExpiryYear(2025)
                .build())
            .setErrorDetails(null)
            .setRiskScore(0.15)
            .setMetadata(new HashMap<>())
            .setVersion(1)
            .build();
    }
}
