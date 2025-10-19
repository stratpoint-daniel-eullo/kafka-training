package com.training.kafka.services;

import com.training.kafka.config.TrainingKafkaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.util.*;

/**
 * Day 7: Kafka Connect Integration
 *
 * This service demonstrates:
 * - JDBC Source Connector (PostgreSQL → Kafka)
 * - JDBC Sink Connector (Kafka → PostgreSQL)
 * - Connector lifecycle management
 * - Change Data Capture (CDC) patterns
 * - Data pipeline monitoring
 *
 * @author Kafka Training Course
 * @version 1.0.0
 */
@Service
public class Day07ConnectService {

    private static final Logger logger = LoggerFactory.getLogger(Day07ConnectService.class);

    @Autowired
    private TrainingKafkaProperties kafkaProperties;

    private final RestTemplate restTemplate = new RestTemplate();

    private String connectUrl;

    @PostConstruct
    public void init() {
        try {
            connectUrl = kafkaProperties.getKafka().getConnectUrl();
            if (connectUrl == null || connectUrl.trim().isEmpty()) {
                connectUrl = "http://localhost:8083"; // Default value
                logger.warn("Kafka Connect URL not configured, using default: {}", connectUrl);
            }
            logger.info("Kafka Connect REST API initialized at: {}", connectUrl);
        } catch (Exception e) {
            connectUrl = "http://localhost:8083"; // Fallback
            logger.error("Failed to get Kafka Connect URL from properties, using default: {}", connectUrl);
        }
    }

    /**
     * Demonstrate Day 7 Kafka Connect concepts
     */
    public void demonstrateKafkaConnect() {
        logger.info("=".repeat(80));
        logger.info("Day 7: Kafka Connect - Data Integration");
        logger.info("=".repeat(80));

        try {
            // 1. Check Kafka Connect cluster info
            logger.info("\n1. Checking Kafka Connect cluster status...");
            getConnectClusterInfo();

            // 2. List available connector plugins
            logger.info("\n2. Listing available connector plugins...");
            listConnectorPlugins();

            // 3. Create JDBC Source Connector
            logger.info("\n3. Creating JDBC Source Connector (PostgreSQL → Kafka)...");
            createUserActivitySourceConnector();
            createProductEventsSourceConnector();

            // 4. Create JDBC Sink Connector
            logger.info("\n4. Creating JDBC Sink Connector (Kafka → PostgreSQL)...");
            createOrderEventsSinkConnector();

            // 5. List all connectors
            logger.info("\n5. Listing all connectors...");
            listConnectors();

            // 6. Get connector status
            logger.info("\n6. Checking connector status...");
            checkConnectorStatus("user-activity-source");

            logger.info("\nDay 7 demonstration complete!");

        } catch (Exception e) {
            logger.error("Error in Day 7 demonstration", e);
        }
    }

    /**
     * Get Kafka Connect cluster information
     */
    public Map<String, Object> getConnectClusterInfo() {
        Map<String, Object> result = new HashMap<>();

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                connectUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {});

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> clusterInfo = response.getBody();
                logger.info("Kafka Connect Version: {}", clusterInfo.get("version"));
                logger.info("Kafka Connect Commit: {}", clusterInfo.get("commit"));
                logger.info("Kafka Cluster ID: {}", clusterInfo.get("kafka_cluster_id"));

                result.put("status", "success");
                result.put("clusterInfo", clusterInfo);
            }
        } catch (RestClientException e) {
            logger.error("Error getting Connect cluster info: {}", e.getMessage());
            result.put("status", "error");
            result.put("message", "Failed to get Connect cluster info: " + e.getMessage());
            result.put("hint", "Ensure Kafka Connect is running at " + connectUrl);
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage());
            result.put("status", "error");
            result.put("message", "Failed to get Connect cluster info: " + e.getMessage());
            result.put("hint", "Ensure Kafka Connect is running at " + connectUrl);
        }

        return result;
    }

    /**
     * List all available connector plugins
     */
    public Map<String, Object> listConnectorPlugins() {
        Map<String, Object> result = new HashMap<>();

        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                connectUrl + "/connector-plugins",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {});

            if (response.getStatusCode() == HttpStatus.OK) {
                List<Map<String, Object>> plugins = response.getBody();
                logger.info("Found {} connector plugins:", plugins.size());

                for (Map<String, Object> plugin : plugins) {
                    logger.info("  - {} (v{})", plugin.get("class"), plugin.get("version"));
                }

                result.put("status", "success");
                result.put("count", plugins.size());
                result.put("plugins", plugins);
            }
        } catch (RestClientException e) {
            logger.error("Error listing connector plugins: {}", e.getMessage());
            result.put("status", "error");
            result.put("message", e.getMessage());
        }

        return result;
    }

    /**
     * List all connectors
     */
    public Map<String, Object> listConnectors() {
        Map<String, Object> result = new HashMap<>();

        try {
            ResponseEntity<List<String>> response = restTemplate.exchange(
                connectUrl + "/connectors",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<String>>() {});

            if (response.getStatusCode() == HttpStatus.OK) {
                List<String> connectors = response.getBody();
                logger.info("Found {} active connectors:", connectors.size());

                for (String connector : connectors) {
                    logger.info("  - {}", connector);
                }

                result.put("status", "success");
                result.put("count", connectors.size());
                result.put("connectors", connectors);
            }
        } catch (RestClientException e) {
            logger.error("Error listing connectors: {}", e.getMessage());
            result.put("status", "error");
            result.put("message", e.getMessage());
        }

        return result;
    }

    /**
     * Create JDBC Source Connector for User Activity Log
     * Reads from PostgreSQL user_activity_log table and produces to Kafka
     */
    public Map<String, Object> createUserActivitySourceConnector() {
        Map<String, Object> connectorConfig = new HashMap<>();
        connectorConfig.put("name", "user-activity-source");

        Map<String, Object> config = new HashMap<>();
        config.put("connector.class", "io.confluent.connect.jdbc.JdbcSourceConnector");
        config.put("tasks.max", "1");
        config.put("connection.url", "jdbc:postgresql://postgres:5432/eventmart");
        config.put("connection.user", "eventmart");
        config.put("connection.password", "eventmart123");
        config.put("mode", "incrementing");
        config.put("incrementing.column.name", "id");
        config.put("table.whitelist", "user_activity_log");
        config.put("topic.prefix", "jdbc-");
        config.put("poll.interval.ms", "5000");
        config.put("batch.max.rows", "100");

        connectorConfig.put("config", config);

        return createConnector(connectorConfig);
    }

    /**
     * Create JDBC Source Connector for Product Events
     * Reads from PostgreSQL product_events table and produces to Kafka
     */
    public Map<String, Object> createProductEventsSourceConnector() {
        Map<String, Object> connectorConfig = new HashMap<>();
        connectorConfig.put("name", "product-events-source");

        Map<String, Object> config = new HashMap<>();
        config.put("connector.class", "io.confluent.connect.jdbc.JdbcSourceConnector");
        config.put("tasks.max", "1");
        config.put("connection.url", "jdbc:postgresql://postgres:5432/eventmart");
        config.put("connection.user", "eventmart");
        config.put("connection.password", "eventmart123");
        config.put("mode", "incrementing");
        config.put("incrementing.column.name", "id");
        config.put("table.whitelist", "product_events");
        config.put("topic.prefix", "jdbc-");
        config.put("poll.interval.ms", "5000");
        config.put("batch.max.rows", "100");

        connectorConfig.put("config", config);

        return createConnector(connectorConfig);
    }

    /**
     * Create JDBC Sink Connector for Order Events
     * Consumes from Kafka and writes to PostgreSQL orders table
     */
    public Map<String, Object> createOrderEventsSinkConnector() {
        Map<String, Object> connectorConfig = new HashMap<>();
        connectorConfig.put("name", "order-events-sink");

        Map<String, Object> config = new HashMap<>();
        config.put("connector.class", "io.confluent.connect.jdbc.JdbcSinkConnector");
        config.put("tasks.max", "1");
        config.put("connection.url", "jdbc:postgresql://postgres:5432/eventmart");
        config.put("connection.user", "eventmart");
        config.put("connection.password", "eventmart123");
        config.put("topics", "order-events-avro");
        config.put("auto.create", "false");
        config.put("auto.evolve", "false");
        config.put("insert.mode", "upsert");
        config.put("pk.mode", "record_key");
        config.put("pk.fields", "order_id");
        config.put("table.name.format", "orders");
        config.put("key.converter", "org.apache.kafka.connect.storage.StringConverter");
        config.put("value.converter", "io.confluent.connect.avro.AvroConverter");
        config.put("value.converter.schema.registry.url", kafkaProperties.getKafka().getSchemaRegistryUrl());

        connectorConfig.put("config", config);

        return createConnector(connectorConfig);
    }

    /**
     * Generic method to create a connector
     */
    private Map<String, Object> createConnector(Map<String, Object> connectorConfig) {
        Map<String, Object> result = new HashMap<>();
        String connectorName = (String) connectorConfig.get("name");

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(connectorConfig, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                connectUrl + "/connectors",
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<Map<String, Object>>() {});

            if (response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) {
                logger.info("Connector '{}' created successfully", connectorName);
                result.put("status", "success");
                result.put("message", "Connector created successfully");
                result.put("connector", response.getBody());
            }
        } catch (RestClientException e) {
            String errorMessage = e.getMessage();
            if (errorMessage.contains("already exists")) {
                logger.warn("Connector '{}' already exists", connectorName);
                result.put("status", "exists");
                result.put("message", "Connector already exists");
            } else {
                logger.error("Error creating connector '{}': {}", connectorName, errorMessage);
                result.put("status", "error");
                result.put("message", errorMessage);
            }
        }

        return result;
    }

    /**
     * Get connector status
     */
    public Map<String, Object> checkConnectorStatus(String connectorName) {
        Map<String, Object> result = new HashMap<>();

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                connectUrl + "/connectors/" + connectorName + "/status",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {});

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> status = response.getBody();
                logger.info("Connector '{}' status:", connectorName);
                logger.info("  State: {}", ((Map<?, ?>) status.get("connector")).get("state"));
                logger.info("  Worker ID: {}", ((Map<?, ?>) status.get("connector")).get("worker_id"));

                Object tasksObj = status.get("tasks");
                if (tasksObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> tasks = (List<Map<String, Object>>) tasksObj;
                    if (!tasks.isEmpty()) {
                        logger.info("  Tasks:");
                        for (Map<String, Object> task : tasks) {
                            logger.info("    - Task {} state: {} on worker {}",
                                task.get("id"), task.get("state"), task.get("worker_id"));
                        }
                    }
                }

                result.put("status", "success");
                result.put("connectorStatus", status);
            }
        } catch (RestClientException e) {
            logger.error("Error getting connector status: {}", e.getMessage());
            result.put("status", "error");
            result.put("message", e.getMessage());
        }

        return result;
    }

    /**
     * Get connector configuration
     */
    public Map<String, Object> getConnectorConfig(String connectorName) {
        Map<String, Object> result = new HashMap<>();

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                connectUrl + "/connectors/" + connectorName + "/config",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {});

            if (response.getStatusCode() == HttpStatus.OK) {
                result.put("status", "success");
                result.put("config", response.getBody());
            }
        } catch (RestClientException e) {
            logger.error("Error getting connector config: {}", e.getMessage());
            result.put("status", "error");
            result.put("message", e.getMessage());
        }

        return result;
    }

    /**
     * Pause a connector
     */
    public Map<String, Object> pauseConnector(String connectorName) {
        Map<String, Object> result = new HashMap<>();

        try {
            restTemplate.put(connectUrl + "/connectors/" + connectorName + "/pause", null);
            logger.info("Connector '{}' paused successfully", connectorName);
            result.put("status", "success");
            result.put("message", "Connector paused");
        } catch (RestClientException e) {
            logger.error("Error pausing connector: {}", e.getMessage());
            result.put("status", "error");
            result.put("message", e.getMessage());
        }

        return result;
    }

    /**
     * Resume a connector
     */
    public Map<String, Object> resumeConnector(String connectorName) {
        Map<String, Object> result = new HashMap<>();

        try {
            restTemplate.put(connectUrl + "/connectors/" + connectorName + "/resume", null);
            logger.info("Connector '{}' resumed successfully", connectorName);
            result.put("status", "success");
            result.put("message", "Connector resumed");
        } catch (RestClientException e) {
            logger.error("Error resuming connector: {}", e.getMessage());
            result.put("status", "error");
            result.put("message", e.getMessage());
        }

        return result;
    }

    /**
     * Restart a connector
     */
    public Map<String, Object> restartConnector(String connectorName) {
        Map<String, Object> result = new HashMap<>();

        try {
            restTemplate.exchange(
                connectUrl + "/connectors/" + connectorName + "/restart",
                HttpMethod.POST,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {});
            logger.info("Connector '{}' restarted successfully", connectorName);
            result.put("status", "success");
            result.put("message", "Connector restarted");
        } catch (RestClientException e) {
            logger.error("Error restarting connector: {}", e.getMessage());
            result.put("status", "error");
            result.put("message", e.getMessage());
        }

        return result;
    }

    /**
     * Delete a connector
     */
    public Map<String, Object> deleteConnector(String connectorName) {
        Map<String, Object> result = new HashMap<>();

        try {
            restTemplate.delete(connectUrl + "/connectors/" + connectorName);
            logger.info("Connector '{}' deleted successfully", connectorName);
            result.put("status", "success");
            result.put("message", "Connector deleted");
        } catch (RestClientException e) {
            logger.error("Error deleting connector: {}", e.getMessage());
            result.put("status", "error");
            result.put("message", e.getMessage());
        }

        return result;
    }

    /**
     * Get connector tasks
     */
    public Map<String, Object> getConnectorTasks(String connectorName) {
        Map<String, Object> result = new HashMap<>();

        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                connectUrl + "/connectors/" + connectorName + "/tasks",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {});

            if (response.getStatusCode() == HttpStatus.OK) {
                result.put("status", "success");
                result.put("tasks", response.getBody());
            }
        } catch (RestClientException e) {
            logger.error("Error getting connector tasks: {}", e.getMessage());
            result.put("status", "error");
            result.put("message", e.getMessage());
        }

        return result;
    }

    /**
     * Validate connector configuration
     */
    public Map<String, Object> validateConnectorConfig(String connectorClass, Map<String, Object> config) {
        Map<String, Object> result = new HashMap<>();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("connector.class", connectorClass);
            requestBody.putAll(config);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                connectUrl + "/connector-plugins/" + connectorClass + "/config/validate",
                HttpMethod.PUT,
                request,
                new ParameterizedTypeReference<Map<String, Object>>() {});

            if (response.getStatusCode() == HttpStatus.OK) {
                result.put("status", "success");
                result.put("validation", response.getBody());
            }
        } catch (RestClientException e) {
            logger.error("Error validating connector config: {}", e.getMessage());
            result.put("status", "error");
            result.put("message", e.getMessage());
        }

        return result;
    }
}
