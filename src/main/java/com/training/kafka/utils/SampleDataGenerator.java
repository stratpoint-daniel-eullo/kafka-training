package com.training.kafka.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Sample Data Generator for Kafka Training
 *
 * Generates realistic sample data for various training scenarios:
 * - User events (login, logout, page views, purchases)
 * - E-commerce orders
 * - IoT sensor data
 * - Log events
 */
public class SampleDataGenerator {
    private static final Logger logger = LoggerFactory.getLogger(SampleDataGenerator.class);
    private static final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private static final Random random = new Random();

    // Sample data arrays
    private static final String[] USER_IDS = {
        "user001", "user002", "user003", "user004", "user005",
        "alice", "bob", "charlie", "diana", "eve",
        "customer_123", "customer_456", "customer_789"
    };

    private static final String[] PRODUCTS = {
        "laptop", "smartphone", "tablet", "headphones", "smartwatch",
        "keyboard", "mouse", "monitor", "webcam", "speaker"
    };

    private static final String[] ACTIONS = {
        "login", "logout", "page_view", "search", "add_to_cart",
        "remove_from_cart", "purchase", "review", "share", "bookmark"
    };

    private static final String[] PAGES = {
        "/home", "/products", "/cart", "/checkout", "/profile",
        "/search", "/category/electronics", "/category/books", "/help", "/contact"
    };

    private static final String[] DEVICES = {
        "desktop", "mobile", "tablet", "smart_tv", "gaming_console"
    };

    private static final String[] LOCATIONS = {
        "New York", "Los Angeles", "Chicago", "Houston", "Phoenix",
        "Philadelphia", "San Antonio", "San Diego", "Dallas", "San Jose"
    };

    /**
     * Generate a user event in JSON format
     */
    public static String generateUserEvent() {
        ObjectNode event = objectMapper.createObjectNode();

        event.put("user_id", randomChoice(USER_IDS));
        event.put("action", randomChoice(ACTIONS));
        event.put("timestamp", Instant.now().toString());
        event.put("session_id", "session_" + random.nextInt(10000));
        event.put("page", randomChoice(PAGES));
        event.put("device", randomChoice(DEVICES));
        event.put("location", randomChoice(LOCATIONS));

        // Add some optional properties
        if (random.nextBoolean()) {
            event.put("user_agent", "Mozilla/5.0 (Training Browser)");
        }

        if (random.nextBoolean()) {
            event.put("referrer", "https://example.com" + randomChoice(PAGES));
        }

        return event.toString();
    }

    /**
     * Generate an e-commerce order in JSON format
     */
    public static String generateOrder() {
        ObjectNode order = objectMapper.createObjectNode();

        String orderId = "order_" + System.currentTimeMillis() + "_" + random.nextInt(1000);
        String customerId = randomChoice(USER_IDS);
        String product = randomChoice(PRODUCTS);
        double amount = 19.99 + (random.nextDouble() * 500); // $19.99 to $519.99

        order.put("order_id", orderId);
        order.put("customer_id", customerId);
        order.put("product", product);
        order.put("amount", Math.round(amount * 100.0) / 100.0);
        order.put("currency", "USD");
        order.put("status", randomChoice(new String[]{"pending", "confirmed", "shipped", "delivered"}));
        order.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        order.put("shipping_address", randomChoice(LOCATIONS));

        return order.toString();
    }

    /**
     * Generate IoT sensor data in JSON format
     */
    public static String generateSensorData() {
        ObjectNode sensor = objectMapper.createObjectNode();

        String sensorId = "sensor_" + (random.nextInt(100) + 1);
        double temperature = 15.0 + (random.nextDouble() * 25); // 15-40°C
        double humidity = 30.0 + (random.nextDouble() * 40); // 30-70%

        sensor.put("sensor_id", sensorId);
        sensor.put("temperature", Math.round(temperature * 10.0) / 10.0);
        sensor.put("humidity", Math.round(humidity * 10.0) / 10.0);
        sensor.put("timestamp", Instant.now().toEpochMilli());
        sensor.put("location", randomChoice(LOCATIONS));
        sensor.put("battery_level", random.nextInt(101)); // 0-100%

        // Simulate some sensor alerts
        if (temperature > 35 || humidity > 65) {
            sensor.put("alert", true);
            sensor.put("alert_type", temperature > 35 ? "high_temperature" : "high_humidity");
        }

        return sensor.toString();
    }

    /**
     * Generate application log event in JSON format
     */
    public static String generateLogEvent() {
        ObjectNode log = objectMapper.createObjectNode();

        String[] levels = {"DEBUG", "INFO", "WARN", "ERROR"};
        String[] services = {"user-service", "order-service", "payment-service", "notification-service"};
        String[] messages = {
            "User authentication successful",
            "Order processed successfully",
            "Payment completed",
            "Database connection established",
            "Cache miss for key",
            "API request processed",
            "Background job completed"
        };

        log.put("timestamp", Instant.now().toString());
        log.put("level", randomChoice(levels));
        log.put("service", randomChoice(services));
        log.put("message", randomChoice(messages));
        log.put("thread", "thread-" + random.nextInt(10));
        log.put("request_id", UUID.randomUUID().toString());

        return log.toString();
    }

    /**
     * Generate a batch of user events
     */
    public static List<String> generateUserEventBatch(int count) {
        List<String> events = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            events.add(generateUserEvent());
        }
        return events;
    }

    /**
     * Generate a batch of orders
     */
    public static List<String> generateOrderBatch(int count) {
        List<String> orders = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            orders.add(generateOrder());
        }
        return orders;
    }

    /**
     * Get a random user ID for consistent key usage
     */
    public static String getRandomUserId() {
        return randomChoice(USER_IDS);
    }

    /**
     * Get a random product for consistent data
     */
    public static String getRandomProduct() {
        return randomChoice(PRODUCTS);
    }

    /**
     * Utility method to choose random element from array
     */
    private static String randomChoice(String[] array) {
        return array[random.nextInt(array.length)];
    }

    /**
     * Demo method to show sample data generation
     */
    public static void main(String[] args) {
        logger.info("=== Sample Data Generator Demo ===");

        logger.info("\n--- User Events ---");
        for (int i = 0; i < 3; i++) {
            logger.info(generateUserEvent());
        }

        logger.info("\n--- E-commerce Orders ---");
        for (int i = 0; i < 3; i++) {
            logger.info(generateOrder());
        }

        logger.info("\n--- IoT Sensor Data ---");
        for (int i = 0; i < 3; i++) {
            logger.info(generateSensorData());
        }

        logger.info("\n--- Log Events ---");
        for (int i = 0; i < 3; i++) {
            logger.info(generateLogEvent());
        }
    }
}
