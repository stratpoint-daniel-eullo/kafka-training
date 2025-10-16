package com.training.kafka.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Spring Boot Auto-Configuration for Kafka Training Course
 * 
 * This configuration class provides Spring beans for Kafka clients that can be used
 * throughout the training course. It creates both Spring Kafka templates and raw
 * Kafka clients for educational purposes.
 * 
 * @author Kafka Training Course
 * @version 1.0.0
 */
@Configuration
@EnableConfigurationProperties(TrainingKafkaProperties.class)
public class TrainingKafkaAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(TrainingKafkaAutoConfiguration.class);

    @Autowired
    private TrainingKafkaProperties trainingProperties;

    /**
     * Training-specific Kafka Producer Factory
     */
    @Bean("trainingProducerFactory")
    @ConditionalOnMissingBean(name = "trainingProducerFactory")
    public ProducerFactory<String, String> trainingProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        
        // Basic configuration
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, 
            trainingProperties.getKafka().getBootstrapServers());
        configProps.put(ProducerConfig.CLIENT_ID_CONFIG, 
            trainingProperties.getKafka().getClientId());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        
        // Training-specific producer settings
        configProps.put(ProducerConfig.ACKS_CONFIG, trainingProperties.getProducer().getAcks());
        configProps.put(ProducerConfig.RETRIES_CONFIG, trainingProperties.getProducer().getRetries());
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, trainingProperties.getProducer().getBatchSize());
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, trainingProperties.getProducer().getLingerMs());
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, trainingProperties.getProducer().getBufferMemory());
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, trainingProperties.getProducer().getCompressionType());
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        
        logger.info("🔧 Configured training Kafka producer with bootstrap servers: {}", 
            trainingProperties.getKafka().getBootstrapServers());
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Training-specific Kafka Template
     */
    @Bean("trainingKafkaTemplate")
    @ConditionalOnMissingBean(name = "trainingKafkaTemplate")
    public KafkaTemplate<String, String> trainingKafkaTemplate() {
        return new KafkaTemplate<>(trainingProducerFactory());
    }

    /**
     * Training-specific Consumer Factory
     */
    @Bean("trainingConsumerFactory")
    @ConditionalOnMissingBean(name = "trainingConsumerFactory")
    public ConsumerFactory<String, String> trainingConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        
        // Basic configuration
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, 
            trainingProperties.getKafka().getBootstrapServers());
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, 
            trainingProperties.getConsumer().getGroupId());
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        
        // Training-specific consumer settings
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, 
            trainingProperties.getConsumer().getAutoOffsetReset());
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, 
            trainingProperties.getConsumer().isEnableAutoCommit());
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 
            trainingProperties.getConsumer().getMaxPollRecords());
        configProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 
            trainingProperties.getConsumer().getSessionTimeoutMs());
        
        logger.info("🔧 Configured training Kafka consumer with group ID: {}", 
            trainingProperties.getConsumer().getGroupId());
        
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    /**
     * Training-specific Kafka Listener Container Factory
     */
    @Bean("trainingKafkaListenerContainerFactory")
    @ConditionalOnMissingBean(name = "trainingKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, String> trainingKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(trainingConsumerFactory());
        
        // Training-friendly settings
        factory.setConcurrency(1); // Single thread for training clarity
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        
        return factory;
    }

    /**
     * Raw Kafka Producer for training examples (backward compatibility)
     */
    @Bean("trainingRawProducer")
    @ConditionalOnMissingBean(name = "trainingRawProducer")
    public KafkaProducer<String, String> trainingRawProducer() {
        Properties props = createTrainingProducerProperties();
        logger.info("🔧 Created raw Kafka producer for training examples");
        return new KafkaProducer<>(props);
    }

    /**
     * Kafka Admin Client for training
     */
    @Bean("trainingAdminClient")
    @ConditionalOnMissingBean(name = "trainingAdminClient")
    public AdminClient trainingAdminClient() {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, 
            trainingProperties.getKafka().getBootstrapServers());
        props.put(AdminClientConfig.CLIENT_ID_CONFIG, "kafka-training-admin");
        
        logger.info("🔧 Created Kafka admin client for training");
        return AdminClient.create(props);
    }

    /**
     * Kafka Streams configuration properties
     */
    @Bean("trainingStreamsConfig")
    @ConditionalOnMissingBean(name = "trainingStreamsConfig")
    public Properties trainingStreamsConfig() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, 
            trainingProperties.getStreams().getApplicationId());
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, 
            trainingProperties.getKafka().getBootstrapServers());
        props.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 
            trainingProperties.getStreams().getReplicationFactor());
        props.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG,
            trainingProperties.getStreams().getDefaultDeserializationExceptionHandler());
        
        // Default serializers
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, 
            org.apache.kafka.common.serialization.Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, 
            org.apache.kafka.common.serialization.Serdes.String().getClass());
        
        logger.info("🔧 Created Kafka Streams configuration for training");
        return props;
    }

    /**
     * Create training producer properties (for backward compatibility)
     */
    public Properties createTrainingProducerProperties() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, 
            trainingProperties.getKafka().getBootstrapServers());
        props.put(ProducerConfig.CLIENT_ID_CONFIG, 
            trainingProperties.getKafka().getClientId());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        
        // Training-friendly defaults
        props.put(ProducerConfig.ACKS_CONFIG, trainingProperties.getProducer().getAcks());
        props.put(ProducerConfig.RETRIES_CONFIG, trainingProperties.getProducer().getRetries());
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, trainingProperties.getProducer().getBatchSize());
        props.put(ProducerConfig.LINGER_MS_CONFIG, trainingProperties.getProducer().getLingerMs());
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, trainingProperties.getProducer().getBufferMemory());
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, trainingProperties.getProducer().getCompressionType());
        
        return props;
    }

    /**
     * Create training consumer properties (for backward compatibility)
     */
    public Properties createTrainingConsumerProperties(String groupId) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, 
            trainingProperties.getKafka().getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId != null ? groupId : 
            trainingProperties.getConsumer().getGroupId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        
        // Training-friendly defaults
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, 
            trainingProperties.getConsumer().getAutoOffsetReset());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, 
            trainingProperties.getConsumer().isEnableAutoCommit());
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 
            trainingProperties.getConsumer().getMaxPollRecords());
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 
            trainingProperties.getConsumer().getSessionTimeoutMs());
        
        return props;
    }

    /**
     * Create admin client properties (for backward compatibility)
     */
    public Properties createTrainingAdminProperties() {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, 
            trainingProperties.getKafka().getBootstrapServers());
        props.put(AdminClientConfig.CLIENT_ID_CONFIG, "kafka-training-admin");
        
        return props;
    }

    /**
     * Get bootstrap servers (for backward compatibility)
     */
    public String getBootstrapServers() {
        return trainingProperties.getKafka().getBootstrapServers();
    }

    /**
     * Get schema registry URL (for backward compatibility)
     */
    public String getSchemaRegistryUrl() {
        return trainingProperties.getSchemaRegistry().getUrl();
    }
}
