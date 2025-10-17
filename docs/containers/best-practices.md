# Container Best Practices

## Overview

Production-ready container practices for Kafka applications.

## Health Checks

Health checks ensure containers are running correctly.

### Docker Health Check

```dockerfile
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY target/kafka-training-*.jar app.jar

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Docker Compose Health Check

```yaml
services:
  app:
    image: kafka-training:latest
    healthcheck:
      test: ["CMD", "wget", "-q", "--spider", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 3s
      retries: 3
      start_period: 40s
    depends_on:
      kafka:
        condition: service_healthy

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    healthcheck:
      test: ["CMD", "kafka-broker-api-versions", "--bootstrap-server", "localhost:9092"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 30s
```

### Spring Boot Actuator Health

```java
@Component
public class KafkaHealthIndicator implements HealthIndicator {

    @Autowired
    private AdminClient adminClient;

    @Override
    public Health health() {
        try {
            // Check if Kafka is reachable
            adminClient.describeCluster()
                .nodes()
                .get(5, TimeUnit.SECONDS);

            return Health.up()
                .withDetail("kafka", "Available")
                .build();

        } catch (Exception e) {
            return Health.down()
                .withDetail("kafka", "Unavailable")
                .withException(e)
                .build();
        }
    }
}
```

## Resource Limits

### Memory Limits

```yaml
services:
  app:
    image: kafka-training:latest
    deploy:
      resources:
        limits:
          memory: 2G
          cpus: '2.0'
        reservations:
          memory: 1G
          cpus: '1.0'
```

### Java Heap Settings

```dockerfile
ENV JAVA_OPTS="-Xms1g -Xmx2g \
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75.0 \
  -XX:InitialRAMPercentage=50.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### Kafka Broker Limits

```yaml
services:
  kafka:
    image: confluentinc/cp-kafka:7.5.0
    environment:
      KAFKA_HEAP_OPTS: "-Xms2g -Xmx4g"
    deploy:
      resources:
        limits:
          memory: 6G
          cpus: '4.0'
```

## Volume Mounts

### Named Volumes

```yaml
volumes:
  kafka-data:
    driver: local
    driver_opts:
      type: none
      o: bind
      device: /data/kafka

services:
  kafka:
    volumes:
      - kafka-data:/var/lib/kafka/data
```

### Bind Mounts for Configuration

```yaml
services:
  app:
    volumes:
      - ./config/application.yml:/app/config/application.yml:ro
      - ./logs:/app/logs
      - /etc/localtime:/etc/localtime:ro  # Sync timezone
```

### tmpfs for Temporary Data

```yaml
services:
  app:
    tmpfs:
      - /tmp
      - /app/temp
```

## Security

### Non-Root User

```dockerfile
FROM eclipse-temurin:17-jre-alpine

# Create non-root user
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

WORKDIR /app
COPY --chown=appuser:appgroup target/kafka-training-*.jar app.jar

# Switch to non-root user
USER appuser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Secrets Management

```yaml
services:
  app:
    environment:
      SPRING_DATASOURCE_PASSWORD_FILE: /run/secrets/db_password
      KAFKA_SASL_JAAS_CONFIG_FILE: /run/secrets/kafka_jaas
    secrets:
      - db_password
      - kafka_jaas

secrets:
  db_password:
    file: ./secrets/db_password.txt
  kafka_jaas:
    file: ./secrets/kafka_jaas.conf
```

### Read-Only Root Filesystem

```yaml
services:
  app:
    security_opt:
      - no-new-privileges:true
    read_only: true
    tmpfs:
      - /tmp
      - /app/temp
```

### Drop Capabilities

```yaml
services:
  app:
    cap_drop:
      - ALL
    cap_add:
      - NET_BIND_SERVICE
```

## Image Optimization

### Multi-Stage Build

```dockerfile
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy only JAR from build stage
COPY --from=build /app/target/kafka-training-*.jar app.jar

# Add non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

EXPOSE 8080
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
```

### Layer Caching

```dockerfile
# Dependencies (changes infrequently)
COPY pom.xml .
RUN mvn dependency:go-offline

# Source code (changes frequently)
COPY src ./src
RUN mvn package
```

### Minimize Image Size

```dockerfile
# Use Alpine base
FROM eclipse-temurin:17-jre-alpine

# Remove unnecessary files
RUN rm -rf /var/cache/apk/*

# Use .dockerignore
# .git
# target/test-classes
# *.md
```

## Docker Compose Best Practices

### Environment Variables

```yaml
services:
  app:
    env_file:
      - .env.production
    environment:
      SPRING_PROFILES_ACTIVE: ${ENV:-production}
      LOG_LEVEL: ${LOG_LEVEL:-INFO}
```

### Dependency Management

```yaml
services:
  app:
    depends_on:
      kafka:
        condition: service_healthy
      postgres:
        condition: service_healthy
```

### Logging

```yaml
services:
  app:
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
        labels: "app=kafka-training"
```

### Restart Policies

```yaml
services:
  app:
    restart: unless-stopped  # or always, on-failure

  postgres:
    restart: always

  kafka:
    restart: on-failure:5
```

## Networking

### Custom Networks

```yaml
networks:
  frontend:
    driver: bridge
  backend:
    driver: bridge
    internal: true  # No external access

services:
  app:
    networks:
      - frontend
      - backend

  kafka:
    networks:
      - backend

  nginx:
    networks:
      - frontend
```

### Network Aliases

```yaml
services:
  kafka:
    networks:
      kafka-network:
        aliases:
          - kafka-broker
          - kafka.local
```

## Monitoring

### Prometheus Metrics

```yaml
services:
  app:
    labels:
      - "prometheus.io/scrape=true"
      - "prometheus.io/port=8080"
      - "prometheus.io/path=/actuator/prometheus"

  prometheus:
    image: prom/prometheus:latest
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
    ports:
      - "9090:9090"
```

### Logging to External System

```yaml
services:
  app:
    logging:
      driver: "fluentd"
      options:
        fluentd-address: localhost:24224
        tag: kafka-training
```

## Production Configuration

### Complete Production Example

```yaml
version: '3.8'

services:
  app:
    image: kafka-training:${VERSION:-latest}
    build:
      context: .
      dockerfile: Dockerfile
      target: production
    container_name: kafka-training-app
    restart: unless-stopped
    user: "1001:1001"
    read_only: true
    security_opt:
      - no-new-privileges:true
    cap_drop:
      - ALL
    deploy:
      resources:
        limits:
          memory: 2G
          cpus: '2.0'
        reservations:
          memory: 1G
          cpus: '1.0'
    healthcheck:
      test: ["CMD", "wget", "-q", "--spider", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 3s
      retries: 3
      start_period: 40s
    environment:
      SPRING_PROFILES_ACTIVE: production
      JAVA_OPTS: >-
        -Xms1g -Xmx2g
        -XX:+UseContainerSupport
        -XX:MaxRAMPercentage=75.0
        -XX:+UseG1GC
        -XX:+UseStringDeduplication
        -Djava.security.egd=file:/dev/./urandom
    env_file:
      - .env.production
    volumes:
      - app-logs:/app/logs
      - app-config:/app/config:ro
    tmpfs:
      - /tmp
      - /app/temp
    networks:
      - kafka-network
    depends_on:
      kafka:
        condition: service_healthy
      postgres:
        condition: service_healthy
    ports:
      - "8080:8080"
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
        labels: "service=kafka-training"

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    hostname: kafka
    container_name: kafka-training-kafka
    restart: always
    healthcheck:
      test: ["CMD", "kafka-broker-api-versions", "--bootstrap-server", "localhost:9092"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 30s
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
      KAFKA_HEAP_OPTS: "-Xms2g -Xmx4g"
      KAFKA_JMX_PORT: 9999
    deploy:
      resources:
        limits:
          memory: 6G
          cpus: '4.0'
    volumes:
      - kafka-data:/var/lib/kafka/data
    networks:
      - kafka-network
    ports:
      - "9092:9092"
      - "9999:9999"
    logging:
      driver: "json-file"
      options:
        max-size: "50m"
        max-file: "5"

  postgres:
    image: postgres:15-alpine
    container_name: kafka-training-postgres
    restart: always
    shm_size: 256mb
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U eventmart_user -d eventmart"]
      interval: 10s
      timeout: 5s
      retries: 5
    environment:
      POSTGRES_DB: eventmart
      POSTGRES_USER: eventmart_user
      POSTGRES_PASSWORD_FILE: /run/secrets/postgres_password
    secrets:
      - postgres_password
    deploy:
      resources:
        limits:
          memory: 2G
    volumes:
      - postgres-data:/var/lib/postgresql/data
      - ./scripts/init-db.sql:/docker-entrypoint-initdb.d/init.sql:ro
    networks:
      - kafka-network
    ports:
      - "5432:5432"

volumes:
  app-logs:
  app-config:
  kafka-data:
  postgres-data:

networks:
  kafka-network:
    driver: bridge
    ipam:
      config:
        - subnet: 172.25.0.0/16

secrets:
  postgres_password:
    file: ./secrets/postgres_password.txt
```

## CI/CD Integration

### Build Pipeline

```yaml
# .github/workflows/build.yml
name: Build and Test

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v3

      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v2

      - name: Cache Docker layers
        uses: actions/cache@v3
        with:
          path: /tmp/.buildx-cache
          key: ${{ runner.os }}-buildx-${{ github.sha }}
          restore-keys: |
            ${{ runner.os }}-buildx-

      - name: Build image
        uses: docker/build-push-action@v4
        with:
          context: .
          file: ./Dockerfile
          push: false
          tags: kafka-training:test
          cache-from: type=local,src=/tmp/.buildx-cache
          cache-to: type=local,dest=/tmp/.buildx-cache-new

      - name: Run tests
        run: docker run --rm kafka-training:test mvn test

      - name: Move cache
        run: |
          rm -rf /tmp/.buildx-cache
          mv /tmp/.buildx-cache-new /tmp/.buildx-cache
```

## Troubleshooting

### Debug Running Container

```bash
# View logs
docker logs -f container-name

# Shell into container
docker exec -it container-name /bin/sh

# Inspect container
docker inspect container-name | jq

# Check resource usage
docker stats container-name

# View processes
docker top container-name
```

### Container Won't Start

```bash
# Check logs
docker logs container-name

# Check health
docker inspect --format='{{.State.Health.Status}}' container-name

# Test connectivity
docker exec container-name ping kafka
docker exec container-name telnet kafka 9092
```

### Performance Issues

```bash
# Check resource limits
docker stats

# Increase limits in docker-compose.yml
deploy:
  resources:
    limits:
      memory: 4G
      cpus: '4.0'

# Check JVM settings
docker exec app java -XX:+PrintFlagsFinal | grep -i heap
```

## Key Takeaways

!!! success "Container Best Practices"
    1. **Always use health checks** for production
    2. **Set resource limits** to prevent resource exhaustion
    3. **Run as non-root user** for security
    4. **Use multi-stage builds** to minimize image size
    5. **Implement proper logging** for troubleshooting
    6. **Use secrets management** for sensitive data
    7. **Configure restart policies** for resilience
    8. **Monitor containers** with Prometheus/Grafana
    9. **Test containers** in CI/CD pipeline
    10. **Document configurations** for team members

## Next Steps

- Deploy to [Kubernetes](../deployment/kubernetes-overview.md)
- Set up [Monitoring](../deployment/monitoring.md)
- Review [Production Checklist](../deployment/checklist.md)

## Resources

- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)
- [Docker Security](https://docs.docker.com/engine/security/)
- [Docker Compose Best Practices](https://docs.docker.com/compose/production/)
