package com.training.kafka.Day07Connect;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Day 7: Kafka Connect Manager
 *
 * Demonstrates how to programmatically manage Kafka Connect connectors.
 * Shows creation, monitoring, and management of source and sink connectors.
 */
public class ConnectorManager {
    private static final Logger logger = LoggerFactory.getLogger(ConnectorManager.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String connectUrl;

    public ConnectorManager(String connectUrl) {
        this.connectUrl = connectUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
        logger.info("Kafka Connect Manager initialized for URL: {}", connectUrl);
    }

    /**
     * List all connectors
     */
    public void listConnectors() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(connectUrl + "/connectors"))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode connectors = objectMapper.readTree(response.body());
                logger.info("Found {} connectors:", connectors.size());

                for (JsonNode connector : connectors) {
                    logger.info("  - {}", connector.asText());
                }
            } else {
                logger.error("Failed to list connectors: HTTP {}", response.statusCode());
            }

        } catch (Exception e) {
            logger.error("Error listing connectors", e);
        }
    }

    /**
     * Create a file source connector
     */
    public void createFileSourceConnector(String connectorName, String filePath, String topicName) {
        Map<String, Object> config = new HashMap<>();
        config.put("name", connectorName);

        Map<String, Object> connectorConfig = new HashMap<>();
        connectorConfig.put("connector.class", "org.apache.kafka.connect.file.FileStreamSourceConnector");
        connectorConfig.put("tasks.max", "1");
        connectorConfig.put("file", filePath);
        connectorConfig.put("topic", topicName);
        connectorConfig.put("key.converter", "org.apache.kafka.connect.storage.StringConverter");
        connectorConfig.put("value.converter", "org.apache.kafka.connect.storage.StringConverter");

        config.put("config", connectorConfig);

        createConnector(config);
    }

    /**
     * Create a file sink connector
     */
    public void createFileSinkConnector(String connectorName, String filePath, String topicName) {
        Map<String, Object> config = new HashMap<>();
        config.put("name", connectorName);

        Map<String, Object> connectorConfig = new HashMap<>();
        connectorConfig.put("connector.class", "org.apache.kafka.connect.file.FileStreamSinkConnector");
        connectorConfig.put("tasks.max", "1");
        connectorConfig.put("file", filePath);
        connectorConfig.put("topics", topicName);
        connectorConfig.put("key.converter", "org.apache.kafka.connect.storage.StringConverter");
        connectorConfig.put("value.converter", "org.apache.kafka.connect.storage.StringConverter");

        config.put("config", connectorConfig);

        createConnector(config);
    }

    /**
     * Generic method to create a connector
     */
    private void createConnector(Map<String, Object> config) {
        try {
            String jsonConfig = objectMapper.writeValueAsString(config);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(connectUrl + "/connectors"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonConfig))
                .build();

            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201 || response.statusCode() == 200) {
                logger.info("Connector '{}' created successfully", config.get("name"));
                logger.debug("Response: {}", response.body());
            } else {
                logger.error("Failed to create connector '{}': HTTP {} - {}",
                    config.get("name"), response.statusCode(), response.body());
            }

        } catch (Exception e) {
            logger.error("Error creating connector '{}'", config.get("name"), e);
        }
    }

    /**
     * Get connector status
     */
    public void getConnectorStatus(String connectorName) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(connectUrl + "/connectors/" + connectorName + "/status"))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode status = objectMapper.readTree(response.body());
                logger.info("Connector '{}' status:", connectorName);
                logger.info("  Name: {}", status.path("name").asText());
                logger.info("  State: {}", status.path("connector").path("state").asText());
                logger.info("  Worker ID: {}", status.path("connector").path("worker_id").asText());

                JsonNode tasks = status.path("tasks");
                logger.info("  Tasks: {}", tasks.size());

                for (int i = 0; i < tasks.size(); i++) {
                    JsonNode task = tasks.get(i);
                    logger.info("    Task {}: {} on {}",
                        task.path("id").asText(),
                        task.path("state").asText(),
                        task.path("worker_id").asText());
                }
            } else {
                logger.error("Failed to get connector status: HTTP {}", response.statusCode());
            }

        } catch (Exception e) {
            logger.error("Error getting connector status", e);
        }
    }

    /**
     * Pause a connector
     */
    public void pauseConnector(String connectorName) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(connectUrl + "/connectors/" + connectorName + "/pause"))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 202) {
                logger.info("Connector '{}' paused successfully", connectorName);
            } else {
                logger.error("Failed to pause connector '{}': HTTP {}", connectorName, response.statusCode());
            }

        } catch (Exception e) {
            logger.error("Error pausing connector '{}'", connectorName, e);
        }
    }

    /**
     * Resume a connector
     */
    public void resumeConnector(String connectorName) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(connectUrl + "/connectors/" + connectorName + "/resume"))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 202) {
                logger.info("Connector '{}' resumed successfully", connectorName);
            } else {
                logger.error("Failed to resume connector '{}': HTTP {}", connectorName, response.statusCode());
            }

        } catch (Exception e) {
            logger.error("Error resuming connector '{}'", connectorName, e);
        }
    }

    /**
     * Delete a connector
     */
    public void deleteConnector(String connectorName) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(connectUrl + "/connectors/" + connectorName))
                .DELETE()
                .build();

            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 204) {
                logger.info("Connector '{}' deleted successfully", connectorName);
            } else {
                logger.error("Failed to delete connector '{}': HTTP {}", connectorName, response.statusCode());
            }

        } catch (Exception e) {
            logger.error("Error deleting connector '{}'", connectorName, e);
        }
    }

    /**
     * Restart a connector
     */
    public void restartConnector(String connectorName) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(connectUrl + "/connectors/" + connectorName + "/restart"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 204) {
                logger.info("Connector '{}' restarted successfully", connectorName);
            } else {
                logger.error("Failed to restart connector '{}': HTTP {}", connectorName, response.statusCode());
            }

        } catch (Exception e) {
            logger.error("Error restarting connector '{}'", connectorName, e);
        }
    }

    /**
     * Main method for demonstration
     */
    public static void main(String[] args) {
        String connectUrl = "http://localhost:8083";

        ConnectorManager manager = new ConnectorManager(connectUrl);

        try {
            // List existing connectors
            manager.listConnectors();

            // Create demo file source connector
            manager.createFileSourceConnector(
                "demo-file-source",
                "/tmp/test-source.txt",
                "file-source-topic"
            );

            // Create demo file sink connector
            manager.createFileSinkConnector(
                "demo-file-sink",
                "/tmp/test-sink.txt",
                "user-events"
            );

            // Wait a bit for connectors to initialize
            Thread.sleep(3000);

            // Check status
            manager.getConnectorStatus("demo-file-source");
            manager.getConnectorStatus("demo-file-sink");

            // Demonstrate lifecycle management
            manager.pauseConnector("demo-file-source");
            Thread.sleep(2000);

            manager.resumeConnector("demo-file-source");
            Thread.sleep(2000);

            // Cleanup (uncomment if you want to delete the demo connectors)
            // manager.deleteConnector("demo-file-source");
            // manager.deleteConnector("demo-file-sink");

        } catch (Exception e) {
            logger.error("Error in main", e);
        }
    }
}
