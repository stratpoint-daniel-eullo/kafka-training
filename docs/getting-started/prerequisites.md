# Prerequisites

Before starting the Kafka training, ensure you have the required tools and knowledge.

!!! note "Track-Specific Prerequisites"
    This training offers two learning tracks with different prerequisites:

    - **Python Data Engineers**: Focus on Python, CLI tools, and platform-agnostic Kafka
    - **Java Developers**: Focus on Java, Spring Boot, and microservices integration

    Choose your track in [Track Selection Guide](track-selection.md)

## Required Tools (Both Tracks)

### Java Development Kit (JDK) 11+

Apache Kafka requires Java 11 or higher. Both tracks need Java for running Kafka itself.

=== "macOS"

    ```bash
    # Using Homebrew
    brew install openjdk@11

    # Verify installation
    java -version
    # Should output: openjdk version "11.x.x" or higher
    ```

=== "Ubuntu/Debian"

    ```bash
    # Install OpenJDK 11
    sudo apt update
    sudo apt install openjdk-11-jdk

    # Verify installation
    java -version
    ```

=== "Windows"

    1. Download from [Adoptium](https://adoptium.net/)
    2. Run the installer
    3. Add to PATH
    4. Verify: `java -version`

!!! note "Java Version"
    This training uses Java 11, but Java 17 or 21 also work. Ensure `JAVA_HOME` is set correctly.

### Docker Desktop

Docker is required for running Kafka and all services in containers.

=== "macOS"

    ```bash
    # Using Homebrew
    brew install --cask docker

    # Or download from docker.com
    # Start Docker Desktop from Applications

    # Verify installation
    docker --version
    docker-compose --version
    ```

=== "Ubuntu/Debian"

    ```bash
    # Install Docker
    curl -fsSL https://get.docker.com -o get-docker.sh
    sudo sh get-docker.sh

    # Add user to docker group
    sudo usermod -aG docker $USER

    # Install Docker Compose
    sudo apt install docker-compose-plugin

    # Verify
    docker --version
    docker compose version
    ```

=== "Windows"

    1. Download [Docker Desktop for Windows](https://www.docker.com/products/docker-desktop/)
    2. Run installer
    3. Enable WSL 2
    4. Verify: `docker --version`

!!! warning "Docker Resources"
    Allocate at least:

    - **Memory**: 4 GB minimum, 8 GB recommended
    - **CPUs**: 2 cores minimum, 4 cores recommended
    - **Disk**: 20 GB free space

### Maven 3.8+

Maven is required for building the Spring Boot application.

=== "macOS"

    ```bash
    # Using Homebrew
    brew install maven

    # Verify installation
    mvn -version
    ```

=== "Ubuntu/Debian"

    ```bash
    # Install Maven
    sudo apt update
    sudo apt install maven

    # Verify
    mvn -version
    ```

=== "Windows"

    1. Download from [Maven](https://maven.apache.org/download.cgi)
    2. Extract to `C:\Program Files\Apache\maven`
    3. Add to PATH
    4. Verify: `mvn -version`

## Track-Specific Prerequisites

### Python Data Engineers Track {#python-data-engineers}

=== "Python 3.8+"

    ```bash
    # Check Python version
    python3 --version
    # Expected: Python 3.8.0 or higher

    # Install pip (if not included)
    python3 -m ensurepip --upgrade
    ```

=== "Python Libraries"

    ```bash
    # Create virtual environment
    python3 -m venv kafka-env
    source kafka-env/bin/activate  # Unix/macOS
    # or: kafka-env\Scripts\activate  # Windows

    # Install confluent-kafka with Avro support
    pip install confluent-kafka[avro]==2.3.0

    # Install kafka-python (alternative client)
    pip install kafka-python==2.0.2

    # Install Faust for stream processing
    pip install faust-streaming==0.10.0

    # Install Avro library
    pip install avro-python3==1.10.2

    # Install PostgreSQL adapter
    pip install psycopg2-binary==2.9.9

    # Verify installations
    python -c "import confluent_kafka; print(confluent_kafka.version())"
    python -c "import faust; print(faust.__version__)"
    ```

!!! tip "Python Virtual Environment"
    Always use a virtual environment to avoid conflicts with system Python packages.

### Java Developers Track {#java-developers}

=== "Maven Configuration"

    Java developers should verify Maven can build Spring Boot applications:

    ```bash
    # Clone the repository first
    git clone <repo-url>
    cd kafka-training-java

    # Build the Spring Boot application
    mvn clean compile

    # This will download all Spring Boot and Kafka dependencies
    # First run may take 5-10 minutes
    ```

=== "IDE Setup"

    For Java developers, an IDE is highly recommended:

    - **IntelliJ IDEA**: Best Spring Boot support
    - **VS Code**: With Java Extension Pack
    - **Eclipse**: With Spring Tools 4

    Configure your IDE to:

    1. Use Java 21 (or Java 11+)
    2. Import as Maven project
    3. Enable annotation processing
    4. Set up Spring Boot run configurations

### Git

Git is required for cloning the repository (both tracks).

=== "macOS"

    ```bash
    # Git is usually pre-installed
    git --version

    # If not, install with Homebrew
    brew install git
    ```

=== "Ubuntu/Debian"

    ```bash
    sudo apt update
    sudo apt install git

    # Verify
    git --version
    ```

=== "Windows"

    1. Download from [git-scm.com](https://git-scm.com/)
    2. Run installer
    3. Verify: `git --version`

## Optional Tools

### IDE (Recommended)

An IDE makes development easier but is not required.

<div class="card-grid">

<div class="info-box">
<strong>IntelliJ IDEA</strong><br/>
Best for Java/Spring Boot<br/>
<a href="https://www.jetbrains.com/idea/download/">Download</a>
</div>

<div class="info-box">
<strong>Visual Studio Code</strong><br/>
Lightweight with Java extensions<br/>
<a href="https://code.visualstudio.com/">Download</a>
</div>

<div class="info-box">
<strong>Eclipse</strong><br/>
Free Java IDE<br/>
<a href="https://www.eclipse.org/downloads/">Download</a>
</div>

</div>

### Kubernetes (For Production Deployment)

Optional for Day 8 production deployment exercises.

=== "macOS"

    ```bash
    # Install kubectl
    brew install kubectl

    # Verify
    kubectl version --client

    # For local testing, enable Kubernetes in Docker Desktop
    # Docker Desktop -> Preferences -> Kubernetes -> Enable
    ```

=== "Ubuntu/Debian"

    ```bash
    # Install kubectl
    curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
    sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl

    # Verify
    kubectl version --client
    ```

=== "Windows"

    ```bash
    # Using Chocolatey
    choco install kubectl

    # Or download from kubernetes.io
    # Verify: kubectl version --client
    ```

## System Requirements

### Minimum Requirements

| Resource | Minimum | Recommended |
|----------|---------|-------------|
| **RAM** | 8 GB | 16 GB |
| **CPU** | 2 cores | 4 cores |
| **Disk Space** | 10 GB | 20 GB |
| **OS** | macOS 10.15+, Ubuntu 20.04+, Windows 10+ | Latest versions |

### Docker Resource Allocation

Configure Docker Desktop resources:

1. Open Docker Desktop Preferences
2. Go to Resources
3. Set:
    - **CPUs**: 2-4 cores
    - **Memory**: 4-8 GB
    - **Swap**: 1-2 GB
    - **Disk**: 20 GB+

## Knowledge Prerequisites

### Required Knowledge

!!! success "You Should Know"
    - **Java Basics**: Classes, objects, methods, exceptions
    - **Command Line**: Basic terminal/shell commands
    - **REST APIs**: HTTP methods, JSON format
    - **Git Basics**: Clone, commit, push, pull

### Helpful (But Not Required)

- Spring Framework/Spring Boot
- Docker and containerization concepts
- Distributed systems concepts
- Maven or Gradle build tools
- SQL and databases

## Verification Checklist

Before proceeding, verify all tools are installed:

```bash
# Java
java -version
# Expected: openjdk version "11.x.x" or higher

# Docker
docker --version
docker compose version
# Expected: Docker version 20.x.x or higher

# Maven
mvn -version
# Expected: Apache Maven 3.8.x or higher

# Git
git --version
# Expected: git version 2.x.x or higher

# Check Docker is running
docker ps
# Expected: No errors, empty list is OK

# Optional: kubectl
kubectl version --client
# Expected: Client Version v1.x.x
```

## Network Requirements

### Required Ports

Ensure these ports are available:

| Port | Service | Required |
|------|---------|----------|
| 8080 | Spring Boot App | Yes |
| 8081 | Kafka UI | Yes |
| 8082 | Schema Registry | Yes |
| 8083 | Kafka Connect | Yes |
| 9092 | Kafka Broker | Yes |
| 2181 | Zookeeper | Yes |
| 5432 | PostgreSQL | Yes |
| 9090 | Prometheus | Optional |
| 3000 | Grafana | Optional |

### Check Port Availability

```bash
# Check if ports are in use
lsof -i :8080
lsof -i :9092
lsof -i :8082

# If ports are in use, stop the conflicting service
# Or change the port in docker-compose.yml
```

## Firewall Configuration

If you have a firewall enabled:

1. Allow Docker to access the network
2. Allow ports 8080-8083, 9092, 2181, 5432
3. For Kubernetes: Allow port 6443 (API server)

## Internet Connection

### During Setup

Required for:

- Downloading Docker images (first run)
- Downloading Maven dependencies
- Pulling Confluent Kafka images

### After Setup

Most exercises work offline, but required for:

- Pulling updated Docker images
- Downloading additional Maven dependencies
- Accessing online documentation

## Troubleshooting

### Java Issues

```bash
# Check JAVA_HOME
echo $JAVA_HOME

# Set JAVA_HOME (add to ~/.bashrc or ~/.zshrc)
export JAVA_HOME=$(/usr/libexec/java_home -v 11)  # macOS
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64  # Linux

# Verify
java -version
echo $JAVA_HOME
```

### Docker Issues

```bash
# Docker daemon not running
# Solution: Start Docker Desktop

# Permission denied
# Solution: Add user to docker group (Linux)
sudo usermod -aG docker $USER
# Then log out and back in

# Low disk space
docker system prune -a
# WARNING: This removes unused images and containers
```

### Maven Issues

```bash
# Maven not found
# Solution: Ensure Maven is in PATH
export PATH="/usr/local/bin/maven/bin:$PATH"

# Update Maven settings
mvn clean compile

# Use Maven wrapper (alternative)
./mvnw clean compile  # Unix/macOS
mvnw.cmd clean compile  # Windows
```

## Next Steps

Once all prerequisites are installed:

1. Verify all tools with the checklist above
2. Proceed to [Installation Guide](installation.md)
3. Or jump to [Quick Start](quick-start.md) for immediate setup

!!! tip "Ready to Start?"
    All prerequisites installed? Great! Continue to the [Quick Start Guide](quick-start.md) to get up and running in 5 minutes!
