package com.training.kafka.services;

import com.training.kafka.eventmart.EventMartTopicManager;
import com.training.kafka.eventmart.demo.EventMartDemoOrchestrator;
import com.training.kafka.config.TrainingKafkaProperties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Spring Service for EventMart Progressive Project
 *
 * This service manages the complete EventMart e-commerce platform demonstration,
 * including topic management, event generation, and orchestrated demos.
 *
 * @author Kafka Training Course
 * @version 1.0.0
 */
@Service
public class EventMartService {

    private static final Logger logger = LoggerFactory.getLogger(EventMartService.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaProducer<String, String> rawProducer;
    private final TrainingKafkaProperties kafkaProperties;
    private final ScheduledExecutorService scheduler;

    private EventMartTopicManager topicManager;
    private EventMartDemoOrchestrator demoOrchestrator;
    private boolean demoRunning = false;

    /**
     * Constructor with dependency injection
     */
    public EventMartService(
            @Qualifier("trainingKafkaTemplate") KafkaTemplate<String, String> kafkaTemplate,
            @Qualifier("trainingRawProducer") KafkaProducer<String, String> rawProducer,
            TrainingKafkaProperties kafkaProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.rawProducer = rawProducer;
        this.kafkaProperties = kafkaProperties;
        this.scheduler = Executors.newScheduledThreadPool(4);
        logger.info("🎭 EventMartService initialized with Spring-managed Kafka clients");
    }

    /**
     * Initialize EventMart components after construction
     */
    @PostConstruct
    public void initialize() {
        try {
            logger.info("🚀 Initializing EventMart Progressive Project");

            // Initialize topic manager
            this.topicManager = new EventMartTopicManager(kafkaProperties.getKafka().getBootstrapServers());

            // Initialize demo orchestrator
            this.demoOrchestrator = new EventMartDemoOrchestrator(rawProducer);

            logger.info("✅ EventMart components initialized successfully");

        } catch (Exception e) {
            logger.error("❌ Failed to initialize EventMart components", e);
        }
    }

    /**
     * Create all EventMart topics
     */
    public boolean createEventMartTopics() {
        try {
            logger.info("🏗️  Creating EventMart topic architecture");
            topicManager.createEventMartTopics();
            logger.info("✅ EventMart topics created successfully");
            return true;

        } catch (Exception e) {
            logger.error("❌ Failed to create EventMart topics", e);
            return false;
        }
    }

    /**
     * Start the complete EventMart demonstration
     */
    public boolean startEventMartDemo() {
        if (demoRunning) {
            logger.warn("⚠️  EventMart demo is already running");
            return false;
        }

        try {
            logger.info("🎭 Starting EventMart Final Demo Showcase");

            // Ensure topics exist
            createEventMartTopics();

            // Start the demo in a separate thread
            scheduler.execute(() -> {
                try {
                    demoRunning = true;
                    demoOrchestrator.startDemo();
                } catch (Exception e) {
                    logger.error("❌ EventMart demo failed", e);
                } finally {
                    demoRunning = false;
                }
            });

            logger.info("✅ EventMart demo started successfully");
            return true;

        } catch (Exception e) {
            logger.error("❌ Failed to start EventMart demo", e);
            demoRunning = false;
            return false;
        }
    }

    /**
     * Stop the EventMart demonstration
     */
    public boolean stopEventMartDemo() {
        if (!demoRunning) {
            logger.warn("⚠️  EventMart demo is not currently running");
            return false;
        }

        try {
            logger.info("🛑 Stopping EventMart demo");

            if (demoOrchestrator != null) {
                demoOrchestrator.stopDemo();
            }

            demoRunning = false;
            logger.info("✅ EventMart demo stopped successfully");
            return true;

        } catch (Exception e) {
            logger.error("❌ Failed to stop EventMart demo", e);
            return false;
        }
    }

    /**
     * Get EventMart demo status
     */
    public EventMartStatus getEventMartStatus() {
        EventMartStatus status = new EventMartStatus();
        status.demoRunning = demoRunning;
        status.topicsCreated = topicManager != null;

        if (demoOrchestrator != null && demoRunning) {
            // Get metrics from demo orchestrator
            status.usersRegistered = demoOrchestrator.getMetrics().usersRegistered;
            status.productsCreated = demoOrchestrator.getMetrics().productsCreated;
            status.ordersPlaced = demoOrchestrator.getMetrics().ordersPlaced;
            status.paymentsCompleted = demoOrchestrator.getMetrics().paymentsCompleted;
            status.totalRevenue = demoOrchestrator.getMetrics().totalRevenue;
        }

        return status;
    }

    /**
     * Send a custom event to EventMart
     */
    public boolean sendEventMartEvent(String topic, String key, String eventJson) {
        try {
            kafkaTemplate.send("eventmart-" + topic, key, eventJson);
            logger.info("📤 Sent EventMart event to topic: eventmart-{}, key: {}", topic, key);
            return true;

        } catch (Exception e) {
            logger.error("❌ Failed to send EventMart event", e);
            return false;
        }
    }

    /**
     * Simulate user registration
     */
    public boolean simulateUserRegistration(String userId, String email, String name) {
        String userEvent = String.format(
            "{\"event_type\":\"user-registered\",\"user_id\":\"%s\",\"email\":\"%s\",\"name\":\"%s\",\"timestamp\":\"%s\"}",
            userId, email, name, java.time.Instant.now());

        return sendEventMartEvent("users", userId, userEvent);
    }

    /**
     * Simulate product creation
     */
    public boolean simulateProductCreation(String productId, String name, String category, double price) {
        String productEvent = String.format(
            "{\"event_type\":\"product-created\",\"product_id\":\"%s\",\"name\":\"%s\",\"category\":\"%s\",\"price\":%.2f,\"timestamp\":\"%s\"}",
            productId, name, category, price, java.time.Instant.now());

        return sendEventMartEvent("products", productId, productEvent);
    }

    /**
     * Simulate order placement
     */
    public boolean simulateOrderPlacement(String orderId, String userId, double amount) {
        String orderEvent = String.format(
            "{\"event_type\":\"order-placed\",\"order_id\":\"%s\",\"user_id\":\"%s\",\"amount\":%.2f,\"timestamp\":\"%s\"}",
            orderId, userId, amount, java.time.Instant.now());

        return sendEventMartEvent("orders", orderId, orderEvent);
    }

    /**
     * Run EventMart topic architecture demo
     */
    public void runTopicArchitectureDemo() {
        logger.info("🏗️  Running EventMart Topic Architecture Demo");

        try {
            createEventMartTopics();

            // Describe the architecture
            logger.info("📊 EventMart Topic Architecture:");
            logger.info("  🔹 eventmart-users: User lifecycle events (3 partitions, compacted)");
            logger.info("  🔹 eventmart-products: Product catalog events (5 partitions, 7-day retention)");
            logger.info("  🔹 eventmart-orders: Order lifecycle events (10 partitions, 30-day retention)");
            logger.info("  🔹 eventmart-payments: Payment transactions (3 partitions, 90-day retention)");
            logger.info("  🔹 eventmart-notifications: User notifications (2 partitions, 1-day retention)");
            logger.info("  🔹 eventmart-analytics: Real-time metrics (1 partition, 1-hour retention)");
            logger.info("  🔹 eventmart-audit: Audit trail (1 partition, 1-year retention)");

            logger.info("✅ EventMart Topic Architecture Demo completed");

        } catch (Exception e) {
            logger.error("❌ EventMart Topic Architecture Demo failed", e);
        }
    }

    /**
     * Check if demo is currently running
     */
    public boolean isDemoRunning() {
        return demoRunning;
    }

    /**
     * Cleanup resources
     */
    @PreDestroy
    public void cleanup() {
        logger.info("🧹 Cleaning up EventMart service");

        if (demoRunning) {
            stopEventMartDemo();
        }

        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                // Wait for existing tasks to terminate
                if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                    logger.warn("⚠️  Scheduler did not terminate in time, forcing shutdown");
                    scheduler.shutdownNow();

                    // Wait again for tasks to respond to cancellation
                    if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                        logger.error("❌ Scheduler did not terminate after forced shutdown");
                    }
                }
                logger.info("✅ Scheduler shut down successfully");
            } catch (InterruptedException e) {
                logger.warn("⚠️  Cleanup interrupted, forcing scheduler shutdown");
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        logger.info("✅ EventMart service cleanup completed");
    }

    /**
     * EventMart status information
     */
    public static class EventMartStatus {
        public boolean demoRunning = false;
        public boolean topicsCreated = false;
        public int usersRegistered = 0;
        public int productsCreated = 0;
        public int ordersPlaced = 0;
        public int paymentsCompleted = 0;
        public double totalRevenue = 0.0;
        public String timestamp = java.time.Instant.now().toString();
    }
}
