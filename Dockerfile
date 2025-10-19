# Multi-stage Dockerfile for Kafka Training Application
# This Dockerfile builds the Spring Boot application and creates an optimized runtime image

# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS builder

# Set working directory
WORKDIR /app

# Copy Maven configuration files
COPY pom.xml .

# Download dependencies (this layer will be cached if pom.xml doesn't change)
# Use BuildKit cache mount for Maven local repository and parallel downloads.
# Requires building with DOCKER_BUILDKIT=1. This caches ~/.m2 between builds.
RUN --mount=type=cache,target=/root/.m2 \
    mvn -T1C dependency:go-offline -B || \
    mvn -T1C dependency:go-offline -B || \
    mvn -T1C dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Install curl for health checks
RUN apk add --no-cache curl

# Create application user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Set working directory
WORKDIR /app

# Create logs directory
RUN mkdir -p /var/log/kafka-training && \
    chown -R appuser:appgroup /var/log/kafka-training

# Copy the built JAR from builder stage
COPY --from=builder /app/target/kafka-training-java-*.jar app.jar

# Change ownership of the application files
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose the application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Set JVM options for containerized environment
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -XX:+UseStringDeduplication \
               -XX:+OptimizeStringConcat \
               -Djava.security.egd=file:/dev/./urandom"

# Set Spring Boot profile for Docker
ENV SPRING_PROFILES_ACTIVE=docker

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# Labels for metadata
LABEL maintainer="Kafka Training Course" \
      version="1.0.0" \
      description="Apache Kafka Training Course with Spring Boot" \
      org.opencontainers.image.title="Kafka Training Java" \
      org.opencontainers.image.description="Comprehensive Apache Kafka training application built with Spring Boot" \
      org.opencontainers.image.version="1.0.0" \
      org.opencontainers.image.vendor="Kafka Training Course" \
      org.opencontainers.image.licenses="MIT"
