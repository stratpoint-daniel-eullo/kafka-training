package com.training.kafka.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;

/**
 * Profile-specific configuration for different environments
 * 
 * This configuration class provides environment-specific settings and
 * initialization logic for development, testing, staging, and production profiles.
 * 
 * @author Kafka Training Course
 * @version 1.0.0
 */
@Configuration
public class ProfileConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(ProfileConfiguration.class);
    
    private final Environment environment;
    
    @Value("${spring.application.name:kafka-training}")
    private String applicationName;
    
    @Value("${training.features.debug-mode:false}")
    private boolean debugMode;
    
    @Value("${training.features.auto-topic-creation:true}")
    private boolean autoTopicCreation;
    
    @Value("${training.features.web-interface-enabled:true}")
    private boolean webInterfaceEnabled;
    
    public ProfileConfiguration(Environment environment) {
        this.environment = environment;
    }
    
    @PostConstruct
    public void logActiveProfiles() {
        String[] activeProfiles = environment.getActiveProfiles();
        String[] defaultProfiles = environment.getDefaultProfiles();
        
        logger.info("🚀 Starting {} with profiles:", applicationName);
        
        if (activeProfiles.length > 0) {
            logger.info("📋 Active profiles: {}", String.join(", ", activeProfiles));
        } else {
            logger.info("📋 Default profiles: {}", String.join(", ", defaultProfiles));
        }
        
        logger.info("🔧 Configuration summary:");
        logger.info("  - Debug mode: {}", debugMode);
        logger.info("  - Auto topic creation: {}", autoTopicCreation);
        logger.info("  - Web interface: {}", webInterfaceEnabled);
    }
    
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        String[] activeProfiles = environment.getActiveProfiles();
        String profileName = activeProfiles.length > 0 ? activeProfiles[0] : "default";
        
        logger.info("✅ {} is ready and running with '{}' profile", applicationName, profileName);
        
        if (webInterfaceEnabled) {
            String port = environment.getProperty("server.port", "8080");
            String contextPath = environment.getProperty("server.servlet.context-path", "/");
            logger.info("🌐 Web interface available at: http://localhost:{}{}", port, contextPath);
            logger.info("📡 API endpoints available at: http://localhost:{}{}/api/training", port, contextPath);
        }
    }
    
    /**
     * Development profile configuration
     */
    @Configuration
    @Profile("dev")
    static class DevelopmentConfiguration {
        
        private static final Logger logger = LoggerFactory.getLogger(DevelopmentConfiguration.class);
        
        @PostConstruct
        public void init() {
            logger.info("🛠️  Development profile activated");
            logger.info("  - Enhanced logging enabled");
            logger.info("  - Auto-restart enabled");
            logger.info("  - Debug features available");
            logger.info("  - Demo data generation enabled");
        }
    }
    
    /**
     * Test profile configuration
     */
    @Configuration
    @Profile("test")
    static class TestConfiguration {
        
        private static final Logger logger = LoggerFactory.getLogger(TestConfiguration.class);
        
        @PostConstruct
        public void init() {
            logger.info("🧪 Test profile activated");
            logger.info("  - TestContainers integration enabled");
            logger.info("  - Reduced logging for cleaner test output");
            logger.info("  - Fast startup optimizations");
            logger.info("  - In-memory database configured");
        }
    }
    
    /**
     * Staging profile configuration
     */
    @Configuration
    @Profile("staging")
    static class StagingConfiguration {
        
        private static final Logger logger = LoggerFactory.getLogger(StagingConfiguration.class);
        
        @PostConstruct
        public void init() {
            logger.info("🎭 Staging profile activated");
            logger.info("  - Production-like settings");
            logger.info("  - Enhanced monitoring enabled");
            logger.info("  - Debug features available for testing");
            logger.info("  - External Kafka cluster connection");
        }
    }
    
    /**
     * Production profile configuration
     */
    @Configuration
    @Profile("prod")
    static class ProductionConfiguration {
        
        private static final Logger logger = LoggerFactory.getLogger(ProductionConfiguration.class);
        
        @PostConstruct
        public void init() {
            logger.info("🚀 Production profile activated");
            logger.info("  - Optimized for performance and security");
            logger.info("  - Comprehensive monitoring enabled");
            logger.info("  - Debug features disabled");
            logger.info("  - External configuration required");
        }
    }
    
    /**
     * Docker profile configuration
     */
    @Configuration
    @Profile("docker")
    static class DockerConfiguration {
        
        private static final Logger logger = LoggerFactory.getLogger(DockerConfiguration.class);
        
        @PostConstruct
        public void init() {
            logger.info("🐳 Docker profile activated");
            logger.info("  - Container-optimized settings");
            logger.info("  - Docker Compose integration");
            logger.info("  - Service discovery via container names");
            logger.info("  - Health checks enabled");
        }
    }
    
    /**
     * Get current profile information
     */
    public ProfileInfo getProfileInfo() {
        String[] activeProfiles = environment.getActiveProfiles();
        String[] defaultProfiles = environment.getDefaultProfiles();
        
        ProfileInfo info = new ProfileInfo();
        info.activeProfiles = activeProfiles;
        info.defaultProfiles = defaultProfiles;
        info.applicationName = applicationName;
        info.debugMode = debugMode;
        info.autoTopicCreation = autoTopicCreation;
        info.webInterfaceEnabled = webInterfaceEnabled;
        
        return info;
    }
    
    /**
     * Profile information data class
     */
    public static class ProfileInfo {
        public String[] activeProfiles;
        public String[] defaultProfiles;
        public String applicationName;
        public boolean debugMode;
        public boolean autoTopicCreation;
        public boolean webInterfaceEnabled;
    }
}
