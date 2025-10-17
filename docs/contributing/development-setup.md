# Development Setup

Get your local development environment ready for contributing.

## Prerequisites

Ensure you have these installed:

- **Java 11+** - Run `java -version`
- **Maven 3.6+** - Run `mvn -version`
- **Docker Desktop** - Run `docker --version`
- **Git** - Run `git --version`
- **IDE** - IntelliJ IDEA or VS Code recommended

## Clone the Repository

```bash
git clone https://github.com/yourusername/kafka-training-java.git
cd kafka-training-java
```

## Start Dependencies

Start Kafka and related services using Docker Compose:

```bash
# Start Kafka, PostgreSQL, Schema Registry, etc.
docker-compose -f docker-compose-dev.yml up -d

# Verify all services are running
docker-compose -f docker-compose-dev.yml ps
```

Services will be available at:
- **Kafka**: localhost:9092
- **Zookeeper**: localhost:2181
- **Schema Registry**: localhost:8082
- **Kafka Connect**: localhost:8083
- **PostgreSQL**: localhost:5432
- **Kafka UI**: http://localhost:8081

## Build the Project

```bash
# Compile and package
mvn clean package

# Skip tests for faster build
mvn clean package -DskipTests
```

## Run the Application

### Option 1: Maven (Hot Reload)
```bash
mvn spring-boot:run
```

The app will start at http://localhost:8080 with hot reload enabled.

### Option 2: Java JAR
```bash
java -jar target/kafka-training-0.0.1-SNAPSHOT.jar
```

### Option 3: Docker
```bash
docker-compose up -d kafka-training-app
```

## Verify Setup

### Test API Endpoint
```bash
curl http://localhost:8080/api/training/modules
```

Should return all 8 training modules.

### Test Kafka Connection
```bash
curl -X POST "http://localhost:8080/api/training/day01/create-topic?name=test&partitions=1&replication=1"
```

Should create a test topic.

## IDE Setup

### IntelliJ IDEA

1. **Import Project**: File → Open → Select `pom.xml`
2. **Enable Annotation Processing**: Settings → Build, Execution, Deployment → Compiler → Annotation Processors → Enable
3. **Code Style**: Import `.editorconfig` (included in repo)
4. **Run Configuration**: 
   - Main class: `com.training.kafka.KafkaTrainingApplication`
   - Working directory: Project root
   - Environment variables: None needed for dev

### VS Code

1. **Install Extensions**:
   - Extension Pack for Java
   - Spring Boot Extension Pack
   - Docker

2. **Open Folder**: Open the project root folder

3. **Build**: Terminal → Run Task → `mvn clean package`

4. **Debug**: F5 → Select "Spring Boot App"

## Debugging

### Debug with IDE

1. Start dependencies: `docker-compose -f docker-compose-dev.yml up -d`
2. Set breakpoints in IDE
3. Run application in debug mode
4. Make API calls to trigger breakpoints

### Debug with Logs

```bash
# Application logs
docker logs -f kafka-training-application

# Kafka logs
docker logs -f kafka-training-kafka

# View all logs
docker-compose -f docker-compose-dev.yml logs -f
```

## Stopping Services

```bash
# Stop application only
docker-compose stop kafka-training-app

# Stop all services
docker-compose -f docker-compose-dev.yml down

# Stop and remove volumes (clean slate)
docker-compose -f docker-compose-dev.yml down -v
```

## Troubleshooting

### Port Already in Use
```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>
```

### Kafka Not Starting
```bash
# Check Kafka logs
docker logs kafka-training-kafka

# Restart Kafka
docker-compose -f docker-compose-dev.yml restart kafka
```

### Out of Disk Space
```bash
# Clean up Docker
docker system prune -a

# Remove unused volumes
docker volume prune
```

## Next Steps

- [Testing Guidelines](testing.md) - Learn how to write tests
- [Code Style](code-style.md) - Follow our coding standards
