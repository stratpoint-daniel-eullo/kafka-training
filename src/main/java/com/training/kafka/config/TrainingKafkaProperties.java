package com.training.kafka.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Kafka training course
 *
 * This class provides type-safe configuration properties for the Kafka training course,
 * allowing easy customization of Kafka settings for different training scenarios.
 *
 * @author Kafka Training Course
 * @version 1.0.0
 */
@ConfigurationProperties(prefix = "training")
public class TrainingKafkaProperties {

    private Kafka kafka = new Kafka();
    private SchemaRegistry schemaRegistry = new SchemaRegistry();
    private Producer producer = new Producer();
    private Consumer consumer = new Consumer();
    private Streams streams = new Streams();

    // Getters and setters
    public Kafka getKafka() { return kafka; }
    public void setKafka(Kafka kafka) { this.kafka = kafka; }

    public SchemaRegistry getSchemaRegistry() { return schemaRegistry; }
    public void setSchemaRegistry(SchemaRegistry schemaRegistry) { this.schemaRegistry = schemaRegistry; }

    public Producer getProducer() { return producer; }
    public void setProducer(Producer producer) { this.producer = producer; }

    public Consumer getConsumer() { return consumer; }
    public void setConsumer(Consumer consumer) { this.consumer = consumer; }

    public Streams getStreams() { return streams; }
    public void setStreams(Streams streams) { this.streams = streams; }

    /**
     * Kafka connection properties
     */
    public static class Kafka {
        private String bootstrapServers = "localhost:9092";
        private String clientId = "kafka-training-client";
        private String schemaRegistryUrl = "http://localhost:8082";
        private String connectUrl = "http://localhost:8083";

        public String getBootstrapServers() { return bootstrapServers; }
        public void setBootstrapServers(String bootstrapServers) { this.bootstrapServers = bootstrapServers; }

        public String getClientId() { return clientId; }
        public void setClientId(String clientId) { this.clientId = clientId; }

        public String getSchemaRegistryUrl() { return schemaRegistryUrl; }
        public void setSchemaRegistryUrl(String schemaRegistryUrl) { this.schemaRegistryUrl = schemaRegistryUrl; }

        public String getConnectUrl() { return connectUrl; }
        public void setConnectUrl(String connectUrl) { this.connectUrl = connectUrl; }
    }

    /**
     * Schema Registry properties
     */
    public static class SchemaRegistry {
        private String url = "http://localhost:8082";

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }

    /**
     * Producer configuration properties
     */
    public static class Producer {
        private String acks = "all";
        private int retries = 3;
        private int batchSize = 16384;
        private int lingerMs = 10;
        private long bufferMemory = 33554432L;
        private String compressionType = "snappy";

        public String getAcks() { return acks; }
        public void setAcks(String acks) { this.acks = acks; }

        public int getRetries() { return retries; }
        public void setRetries(int retries) { this.retries = retries; }

        public int getBatchSize() { return batchSize; }
        public void setBatchSize(int batchSize) { this.batchSize = batchSize; }

        public int getLingerMs() { return lingerMs; }
        public void setLingerMs(int lingerMs) { this.lingerMs = lingerMs; }

        public long getBufferMemory() { return bufferMemory; }
        public void setBufferMemory(long bufferMemory) { this.bufferMemory = bufferMemory; }

        public String getCompressionType() { return compressionType; }
        public void setCompressionType(String compressionType) { this.compressionType = compressionType; }
    }

    /**
     * Consumer configuration properties
     */
    public static class Consumer {
        private String groupId = "training-consumer-group";
        private String autoOffsetReset = "earliest";
        private boolean enableAutoCommit = false;
        private int maxPollRecords = 500;
        private int sessionTimeoutMs = 30000;

        public String getGroupId() { return groupId; }
        public void setGroupId(String groupId) { this.groupId = groupId; }

        public String getAutoOffsetReset() { return autoOffsetReset; }
        public void setAutoOffsetReset(String autoOffsetReset) { this.autoOffsetReset = autoOffsetReset; }

        public boolean isEnableAutoCommit() { return enableAutoCommit; }
        public void setEnableAutoCommit(boolean enableAutoCommit) { this.enableAutoCommit = enableAutoCommit; }

        public int getMaxPollRecords() { return maxPollRecords; }
        public void setMaxPollRecords(int maxPollRecords) { this.maxPollRecords = maxPollRecords; }

        public int getSessionTimeoutMs() { return sessionTimeoutMs; }
        public void setSessionTimeoutMs(int sessionTimeoutMs) { this.sessionTimeoutMs = sessionTimeoutMs; }
    }

    /**
     * Streams configuration properties
     */
    public static class Streams {
        private String applicationId = "kafka-training-streams";
        private int replicationFactor = 1;
        private String defaultDeserializationExceptionHandler =
            "org.apache.kafka.streams.errors.LogAndContinueExceptionHandler";

        public String getApplicationId() { return applicationId; }
        public void setApplicationId(String applicationId) { this.applicationId = applicationId; }

        public int getReplicationFactor() { return replicationFactor; }
        public void setReplicationFactor(int replicationFactor) { this.replicationFactor = replicationFactor; }

        public String getDefaultDeserializationExceptionHandler() { return defaultDeserializationExceptionHandler; }
        public void setDefaultDeserializationExceptionHandler(String defaultDeserializationExceptionHandler) {
            this.defaultDeserializationExceptionHandler = defaultDeserializationExceptionHandler;
        }
    }
}
