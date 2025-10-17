# 🐳 Container-First Kafka Training for Data Engineers

## Why Containers Matter for Data Engineers

Modern data engineering is **container-first** because:

1. **Production Parity**: Dev environment matches production
2. **Portability**: Run anywhere (laptop, cloud, on-prem)
3. **Isolation**: No dependency conflicts or "works on my machine"
4. **Scalability**: Easy to scale horizontally with Kubernetes
5. **Infrastructure as Code**: Declarative configuration, version controlled
6. **Fast Onboarding**: New team members productive in minutes

**This training teaches you to think and work like a production data engineer.**

---

## 🎯 Container-First Learning Path

### Phase 1: Docker Basics (Day 1-2)
**Goal**: Understand containers and Docker fundamentals

```bash
# Start Kafka cluster in containers
docker-compose up -d kafka zookeeper

# View running containers
docker ps

# View Kafka logs
docker logs -f kafka-training-kafka

# Access Kafka container shell
docker exec -it kafka-training-kafka bash

# Inside container: List topics
kafka-topics --bootstrap-server localhost:9092 --list
```

**What You Learn**:
- Containers vs VMs
- Docker images vs containers
- Port mapping and networking
- Volume persistence

---

### Phase 2: Complete Data Platform (Day 3-4)
**Goal**: Run full Kafka ecosystem in containers

```bash
# Start complete data platform
docker-compose up -d

# Verify all services are healthy
docker-compose ps

# Access Kafka UI (Data Engineer's best friend!)
open http://localhost:8081
```

**Services Running**:
- ✅ Kafka Broker (localhost:9092)
- ✅ Zookeeper (localhost:2181)
- ✅ Schema Registry (localhost:8082)
- ✅ Kafka Connect (localhost:8083)
- ✅ PostgreSQL (localhost:5432)
- ✅ Kafka UI (localhost:8081)
- ✅ Training App (localhost:8080)

**What You Learn**:
- Multi-container orchestration
- Service dependencies
- Health checks
- Container networking

---

### Phase 3: Development Workflow (Day 5-6)
**Goal**: Develop and test like a pro

```bash
# Development workflow
docker-compose up -d kafka postgres schema-registry  # Start dependencies only
mvn spring-boot:run  # Run app locally (hot reload)

# Test changes
curl http://localhost:8080/api/training/modules

# Rebuild app container
docker-compose build kafka-training-app
docker-compose up -d kafka-training-app
```

**What You Learn**:
- Local development with containerized dependencies
- Hot reload during development
- Building custom images
- Debugging containerized apps

---

### Phase 4: Production Patterns (Day 7-8)
**Goal**: Production-ready containerization

```bash
# Monitor with Prometheus + Grafana
docker-compose --profile monitoring up -d

# View metrics
open http://localhost:3000  # Grafana
open http://localhost:9090  # Prometheus

# Scale Kafka Connect workers
docker-compose up -d --scale kafka-connect=3

# Backup volumes
docker run --rm -v kafka-training_kafka-data:/data \
  -v $(pwd):/backup alpine tar czf /backup/kafka-data.tar.gz /data
```

**What You Learn**:
- Container monitoring
- Scaling strategies
- Data persistence
- Backup and recovery

---

## 🛠️ Container Commands Cheat Sheet for Data Engineers

### Daily Development
```bash
# Start everything
docker-compose up -d

# Stop everything
docker-compose down

# Restart specific service
docker-compose restart kafka

# View logs (follow)
docker-compose logs -f kafka

# View logs (last 100 lines)
docker-compose logs --tail=100 kafka-training-app

# Check service health
docker-compose ps
```

### Debugging
```bash
# Shell into Kafka container
docker exec -it kafka-training-kafka bash

# Shell into app container
docker exec -it kafka-training-application bash

# View container resource usage
docker stats

# Inspect container configuration
docker inspect kafka-training-kafka

# View container networks
docker network ls
docker network inspect kafka-training-network
```

### Data Management
```bash
# List volumes
docker volume ls

# Inspect volume
docker volume inspect kafka-training_kafka-data

# Remove unused volumes (⚠️ CAUTION: Deletes data!)
docker volume prune

# Backup volume
docker run --rm -v kafka-training_kafka-data:/data \
  -v $(pwd):/backup alpine tar czf /backup/backup.tar.gz /data

# Restore volume
docker run --rm -v kafka-training_kafka-data:/data \
  -v $(pwd):/backup alpine tar xzf /backup/backup.tar.gz -C /
```

### Cleanup
```bash
# Stop and remove containers (keeps volumes)
docker-compose down

# Stop and remove everything including volumes (⚠️ NUCLEAR OPTION)
docker-compose down -v

# Remove dangling images
docker image prune

# Remove all stopped containers
docker container prune
```

---

## 🎓 Container Architecture for This Training

```
┌─────────────────────────────────────────────────────────────────┐
│                    kafka-training-network                        │
│                                                                   │
│  ┌──────────────┐      ┌──────────────┐      ┌──────────────┐  │
│  │  Zookeeper   │◄────►│    Kafka     │◄────►│Schema Registry│  │
│  │   :2181      │      │   :9092      │      │    :8082      │  │
│  └──────────────┘      └──────────────┘      └──────────────┘  │
│         ▲                      ▲                      ▲          │
│         │                      │                      │          │
│  ┌──────┴──────────────────────┴──────────────────────┴──────┐  │
│  │              Kafka Connect (:8083)                         │  │
│  └────────────────────────────────────────────────────────────┘  │
│         ▲                      ▲                                 │
│         │                      │                                 │
│  ┌──────┴──────┐      ┌───────┴────────┐      ┌─────────────┐  │
│  │  PostgreSQL │      │  Training App  │      │  Kafka UI   │  │
│  │   :5432     │      │     :8080      │      │   :8081     │  │
│  └─────────────┘      └────────────────┘      └─────────────┘  │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
         ▲                           ▲
         │                           │
    Host Network               Host Network
    (Your Machine)            (Web Browser)
```

**Key Concepts**:
- All services run in `kafka-training-network` (internal communication)
- Ports exposed to host machine (external access)
- Volumes persist data across restarts
- Health checks ensure service readiness
- Dependencies managed with `depends_on`

---

## 🏗️ Building Custom Images

### Dockerfile Explained
```dockerfile
# Multi-stage build for smaller images
FROM maven:3.9-eclipse-temurin-11 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline  # Cache dependencies
COPY src ./src
RUN mvn clean package -DskipTests  # Build app

# Runtime stage
FROM eclipse-temurin:11-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Data Engineer Concepts**:
- **Multi-stage builds**: Smaller production images
- **Layer caching**: Faster builds (dependencies cached)
- **Alpine Linux**: Minimal base image (smaller, faster)
- **No build tools in production**: Only runtime needed

### Build and Test
```bash
# Build image
docker build -t kafka-training:latest .

# Run locally
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  kafka-training:latest

# Tag for registry
docker tag kafka-training:latest myregistry.com/kafka-training:v1.0.0

# Push to registry
docker push myregistry.com/kafka-training:v1.0.0
```

---

## 🎯 TestContainers: Testing Like Production

**Why TestContainers?**
- Tests run against **real Kafka**, not mocks
- Each test gets isolated environment
- Tests work on any machine (no local Kafka required)
- CI/CD friendly

### Example Test
```java
@SpringBootTest
@Testcontainers
class Day02DataFlowTest {

    @Container
    static final KafkaContainer kafka =
        new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.7.0"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("training.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Test
    void shouldProduceAndConsumeMessages() {
        // Test with REAL Kafka running in Docker!
    }
}
```

**What Happens**:
1. Test starts → TestContainers spins up Kafka container
2. Test runs → Uses real Kafka (not mock)
3. Test ends → Container automatically destroyed
4. Next test → Fresh Kafka container

**Benefits for Data Engineers**:
- ✅ Test against real Kafka behavior
- ✅ Catch serialization issues
- ✅ Test consumer rebalancing
- ✅ Verify schema evolution
- ✅ Test Kafka Connect pipelines

---

## 🚀 Kubernetes Deployment (Production)

### Why Kubernetes for Data Engineers?

**Kubernetes is the standard for production data platforms** because:
- Horizontal scaling (add more consumers/producers)
- Self-healing (auto-restart failed pods)
- Rolling updates (zero-downtime deployments)
- Resource management (CPU, memory limits)
- Service discovery (no hardcoded IPs)

### Simple Kubernetes Deployment
```yaml
# kafka-app-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: kafka-training-app
spec:
  replicas: 3  # Scale to 3 instances
  selector:
    matchLabels:
      app: kafka-training
  template:
    metadata:
      labels:
        app: kafka-training
    spec:
      containers:
      - name: app
        image: kafka-training:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "kubernetes"
        - name: KAFKA_BOOTSTRAP_SERVERS
          value: "kafka-cluster:9092"
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
```

### Deploy to Kubernetes
```bash
# Apply deployment
kubectl apply -f kafka-app-deployment.yaml

# Check pods
kubectl get pods

# View logs
kubectl logs -f kafka-training-app-xxxxx

# Scale up/down
kubectl scale deployment kafka-training-app --replicas=5

# Rolling update
kubectl set image deployment/kafka-training-app app=kafka-training:v2.0.0
```

**Data Engineer Takeaway**:
- Containers = Building blocks
- Docker Compose = Local development
- Kubernetes = Production orchestration

---

## 📊 Container Monitoring for Data Engineers

### Metrics to Watch
```bash
# Container resource usage
docker stats

# Kafka container metrics
docker exec kafka-training-kafka kafka-run-class kafka.tools.JmxTool \
  --object-name kafka.server:type=BrokerTopicMetrics,name=MessagesInPerSec \
  --attributes Count

# View Prometheus metrics
curl http://localhost:9090/api/v1/query?query=kafka_server_brokertopicmetrics_messagesinpersec_count
```

### Grafana Dashboards
```bash
# Start monitoring stack
docker-compose --profile monitoring up -d

# Access Grafana
open http://localhost:3000
# Login: admin / admin

# Pre-configured dashboards for:
- Kafka broker metrics
- Consumer lag
- Topic throughput
- JVM metrics
- Container resource usage
```

---

## 🔍 Troubleshooting Guide

### Issue: Container won't start
```bash
# Check logs
docker logs kafka-training-kafka

# Common causes:
# 1. Port already in use
lsof -i :9092
# Solution: Stop conflicting process or change port

# 2. Out of disk space
df -h
docker system prune  # Clean up

# 3. Memory limit
docker stats
# Solution: Increase Docker memory in settings
```

### Issue: Can't connect to Kafka
```bash
# Verify Kafka is running
docker ps | grep kafka

# Test connectivity
docker exec -it kafka-training-kafka \
  kafka-broker-api-versions --bootstrap-server localhost:9092

# Check network
docker network inspect kafka-training-network
```

### Issue: Data not persisting
```bash
# Check volumes
docker volume ls | grep kafka

# Verify volume mounted
docker inspect kafka-training-kafka | grep -A 10 Mounts

# Solution: Ensure volumes defined in docker-compose.yml
```

---

## 🎓 Container Best Practices for Data Engineers

### 1. **Always Use Volume Mounts for Data**
```yaml
# ✅ Good: Data persists
volumes:
  - kafka-data:/var/lib/kafka/data

# ❌ Bad: Data lost on restart
# (no volume mount)
```

### 2. **Use Health Checks**
```yaml
healthcheck:
  test: ["CMD-SHELL", "kafka-broker-api-versions --bootstrap-server localhost:9092"]
  interval: 30s
  timeout: 10s
  retries: 3
```

### 3. **Set Resource Limits**
```yaml
deploy:
  resources:
    limits:
      memory: 4G
      cpus: '2.0'
```

### 4. **Use .dockerignore**
```
# .dockerignore
target/
*.log
.git
.idea
*.md
```

### 5. **Tag Images Properly**
```bash
# ✅ Good: Semantic versioning
kafka-training:1.2.3
kafka-training:latest

# ❌ Bad: No version
kafka-training
```

### 6. **Never Store Secrets in Images**
```yaml
# ✅ Good: Environment variables
environment:
  DB_PASSWORD: ${DB_PASSWORD}

# ❌ Bad: Hardcoded
environment:
  DB_PASSWORD: "hardcoded123"
```

---

## 🎯 Container Workflow Summary

### Local Development
```bash
# 1. Start dependencies
docker-compose up -d kafka postgres

# 2. Run app locally (hot reload)
mvn spring-boot:run

# 3. Make changes, app reloads automatically

# 4. Test
curl http://localhost:8080/api/training/modules
```

### Integration Testing
```bash
# 1. TestContainers automatically starts Kafka
mvn test

# 2. Each test gets isolated environment

# 3. Containers cleaned up after tests
```

### Production Deployment
```bash
# 1. Build production image
docker build -t kafka-training:1.0.0 .

# 2. Push to registry
docker push myregistry.com/kafka-training:1.0.0

# 3. Deploy to Kubernetes
kubectl apply -f k8s/deployment.yaml

# 4. Monitor
kubectl logs -f deployment/kafka-training-app
```

---

## 📚 Learning Resources

### Docker Fundamentals
- [Docker Official Docs](https://docs.docker.com/)
- [Docker for Data Engineers (Tutorial)](https://docs.docker.com/language/python/)
- [Docker Compose Reference](https://docs.docker.com/compose/)

### Kubernetes Basics
- [Kubernetes Official Tutorial](https://kubernetes.io/docs/tutorials/)
- [Kubernetes for Data Engineers](https://kubernetes.io/docs/tutorials/stateless-application/)
- [Helm Charts](https://helm.sh/docs/)

### Kafka on Containers
- [Confluent Docker Images](https://hub.docker.com/u/confluentinc)
- [Strimzi Kafka Operator](https://strimzi.io/) (Kubernetes)
- [Kafka Connect Docker](https://docs.confluent.io/platform/current/installation/docker/config-reference.html)

---

## 🎉 You're Now Container-Ready!

**Key Takeaways**:
1. ✅ Containers are the standard for modern data engineering
2. ✅ Docker Compose for local development
3. ✅ TestContainers for integration testing
4. ✅ Kubernetes for production orchestration
5. ✅ Always think: "How will this run in containers?"

**Next Steps**:
1. Complete all 8 days of training using containers
2. Build your own containerized data pipeline
3. Deploy to Kubernetes cluster
4. Add monitoring with Prometheus/Grafana

**You now have production-ready skills for containerized data engineering!** 🚀
