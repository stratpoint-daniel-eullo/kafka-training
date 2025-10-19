# E-Commerce Analytics Platform

A production-ready Apache Kafka streaming application demonstrating all Days 1-8 concepts from the Kafka Training course. This Python implementation provides feature parity with the Java EventMart project, showcasing real-time event processing for an e-commerce system.

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Project Structure](#project-structure)
- [Configuration](#configuration)
- [Running the Platform](#running-the-platform)
- [Development](#development)
- [Testing](#testing)
- [Monitoring](#monitoring)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

## Overview

The E-Commerce Analytics Platform is a comprehensive streaming data pipeline that processes events from an e-commerce system in real-time. It demonstrates Apache Kafka best practices and covers all training Days:

- **Day 1-2**: Kafka fundamentals, topics, partitions, and cluster management
- **Day 3**: Advanced producer patterns with idempotence and transactions
- **Day 4**: Consumer groups, offset management, and reliability
- **Day 5**: Stream processing with Faust (Python Kafka Streams alternative)
- **Day 6**: Schema management with Avro and Schema Registry
- **Day 7**: Data integration with Kafka Connect and JDBC sink
- **Day 8**: Monitoring, security, and production best practices

### Key Technologies

- **Apache Kafka 3.8.0**: Distributed streaming platform
- **Python 3.11+**: Primary programming language
- **confluent-kafka[avro]**: Production-grade Kafka client with Avro support
- **Faust**: Stream processing framework
- **PostgreSQL 16**: Analytics data storage
- **Prometheus + Grafana**: Monitoring and observability
- **Docker Compose**: Container orchestration

## Architecture

The platform follows a microservices architecture with event-driven communication:

```
┌─────────────┐
│  Producers  │──┐
└─────────────┘  │
                 ├──▶ ┌──────────────┐     ┌─────────────────┐
┌─────────────┐  │    │ Kafka Broker │────▶│ Schema Registry │
│   Faust     │──┤    └──────────────┘     └─────────────────┘
│  Streams    │  │           │
└─────────────┘  │           │
                 │           ▼
┌─────────────┐  │    ┌──────────────┐     ┌──────────────┐
│  Consumers  │──┘    │ Kafka Connect│────▶│  PostgreSQL  │
└─────────────┘       └──────────────┘     └──────────────┘
                                                   │
                      ┌──────────────┐             │
                      │  Prometheus  │◀────────────┘
                      └──────────────┘
                             │
                             ▼
                      ┌──────────────┐
                      │   Grafana    │
                      └──────────────┘
```

See [ARCHITECTURE.md](./ARCHITECTURE.md) for detailed architecture documentation.

## Features

### Event Processing

- **User Events**: Registration, login, profile updates
- **Product Events**: Catalog management, inventory tracking
- **Order Events**: Order placement, status updates
- **Payment Events**: Transaction processing, fraud detection

### Stream Processing

- **Real-time Aggregations**: Sales metrics, user behavior analytics
- **Fraud Detection**: Payment anomaly detection
- **User Segmentation**: Customer categorization

### Data Integration

- **Kafka Connect Sinks**: PostgreSQL JDBC connector
- **Schema Evolution**: Backward-compatible Avro schemas
- **Dead Letter Queue**: Error handling and replay

### Monitoring

- **Prometheus Metrics**: Producer/consumer lag, throughput, errors
- **Grafana Dashboards**: Real-time visualization
- **Alerting**: Consumer lag and error rate alerts

## Prerequisites

### Required

- **Docker** 20.10+ and **Docker Compose** 2.0+
- **Python** 3.9+ (for local development)
- **Git** 2.0+

### Optional

- **Make** (for convenience commands)
- **PostgreSQL Client** (for database inspection)
- **Kafka CLI Tools** (for cluster management)

### System Requirements

- **CPU**: 4+ cores recommended
- **RAM**: 8GB minimum, 16GB recommended
- **Disk**: 20GB free space

## Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd kafka-training-java/src/main/python
```

### 2. Configure Environment

```bash
# Copy environment template
cp .env.example .env

# Edit .env with your settings (optional for local development)
# Default values work out of the box
```

### 3. Start the Platform

```bash
# Start all services
docker-compose up -d

# Wait for services to be healthy (~60 seconds)
docker-compose ps

# Check logs
docker-compose logs -f
```

### 4. Run the Demo

```bash
# Setup: Create topics and validate schemas
docker-compose run --rm ecommerce-app \
  python -m ecommerce_analytics.demo.setup --recreate-topics

# Run demo: Generate 100 events
docker-compose run --rm ecommerce-app \
  python -m ecommerce_analytics.demo.run_demo --events 100 --delay 0.5
```

### 5. Access Services

- **Grafana**: http://localhost:3000 (admin/admin)
- **Prometheus**: http://localhost:9090
- **Kafka Connect**: http://localhost:8083
- **Schema Registry**: http://localhost:8081
- **PostgreSQL**: localhost:5432 (analytics_user/analytics_pass)

## Project Structure

```
src/main/python/
├── ecommerce_analytics/          # Main application package
│   ├── __init__.py
│   ├── config/                   # Configuration (Day 1-2)
│   │   ├── kafka_config.py       # Kafka client configs
│   │   └── settings.py           # Application settings
│   ├── schemas/                  # Avro schemas (Day 6)
│   │   ├── user_events.avsc
│   │   ├── order_events.avsc
│   │   ├── product_events.avsc
│   │   └── payment_events.avsc
│   ├── producers/                # Event producers (Day 3)
│   │   ├── base_producer.py     # Base producer class
│   │   ├── user_event_producer.py
│   │   ├── order_event_producer.py
│   │   ├── product_event_producer.py
│   │   └── payment_event_producer.py
│   ├── consumers/                # Event consumers (Day 4)
│   │   ├── base_consumer.py     # Base consumer class
│   │   ├── user_consumer.py
│   │   ├── order_consumer.py
│   │   └── payment_consumer.py
│   ├── streams/                  # Stream processors (Day 6)
│   │   ├── sales_aggregator.py  # Sales metrics
│   │   ├── fraud_detector.py    # Fraud detection
│   │   └── user_segmentation.py # User categorization
│   ├── admin/                    # Kafka administration (Day 2)
│   │   └── topic_manager.py     # Topic CRUD operations
│   ├── connect/                  # Kafka Connect (Day 7)
│   │   └── connector_manager.py # Connector management
│   ├── monitoring/               # Observability (Day 8)
│   │   ├── metrics.py           # Prometheus metrics
│   │   └── lag_monitor.py       # Consumer lag tracking
│   ├── error_handling/           # Error handling (Day 8)
│   │   └── dlq_handler.py       # Dead Letter Queue
│   ├── utils/                    # Utilities
│   │   └── sample_data_generator.py
│   └── demo/                     # Demo scripts
│       ├── setup.py             # Platform setup
│       └── run_demo.py          # Demo runner
├── tests/                        # Test suite
├── sql/                          # Database schemas
│   └── init.sql                 # PostgreSQL initialization
├── monitoring/                   # Monitoring configs
│   ├── prometheus.yml
│   ├── grafana-datasource.yml
│   └── grafana-dashboard.json
├── airflow/                      # Airflow DAGs (extensions)
│   ├── dags/
│   └── plugins/
├── flink/                        # Flink jobs (extensions)
│   ├── jobs/
│   └── sql/
├── Dockerfile                    # Application container
├── docker-compose.yml            # Service orchestration
├── docker-compose-extensions.yml # Airflow + Flink
├── requirements.txt              # Python dependencies
├── .env.example                  # Environment template
├── .gitignore
├── README.md                     # This file
└── ARCHITECTURE.md               # Architecture docs
```

## Configuration

### Environment Variables

Key environment variables (see `.env.example` for complete list):

```bash
# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_SCHEMA_REGISTRY_URL=http://localhost:8081

# PostgreSQL
POSTGRES_HOST=localhost
POSTGRES_DB=ecommerce_analytics
POSTGRES_USER=analytics_user
POSTGRES_PASSWORD=analytics_pass

# Application
LOG_LEVEL=INFO
APP_ENV=development
```

### Topic Configuration

Topics are configured in `ecommerce_analytics/config/settings.py`:

```python
topics = {
    "user-events": TopicConfig(
        partitions=3,
        replication_factor=1,
        retention_ms=7_days
    ),
    # ... more topics
}
```

## Running the Platform

### Individual Components

```bash
# Run producer
docker-compose run --rm ecommerce-app \
  python -m ecommerce_analytics.producers.user_event_producer

# Run consumer
docker-compose run --rm ecommerce-app \
  python -m ecommerce_analytics.consumers.user_consumer

# Run stream processor
docker-compose run --rm ecommerce-app \
  python -m ecommerce_analytics.streams.sales_aggregator worker -l info
```

### With Extensions (Airflow + Flink)

```bash
# Start base + extensions
docker-compose -f docker-compose.yml \
  -f docker-compose-extensions.yml up -d

# Access Airflow UI
open http://localhost:8080

# Access Flink Dashboard
open http://localhost:8081
```

## Development

### Local Setup

```bash
# Create virtual environment
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt

# Install development dependencies
pip install pytest pytest-cov black flake8 mypy
```

### Running Tests

```bash
# Run all tests
pytest

# Run with coverage
pytest --cov=ecommerce_analytics --cov-report=html

# Run specific test file
pytest tests/test_producers.py
```

### Code Quality

```bash
# Format code
black ecommerce_analytics/

# Lint
flake8 ecommerce_analytics/

# Type check
mypy ecommerce_analytics/

# Sort imports
isort ecommerce_analytics/
```

## Monitoring

### Prometheus Metrics

Access Prometheus UI at http://localhost:9090

Example queries:

```promql
# Producer throughput
rate(ecommerce_messages_produced_total[1m])

# Consumer lag
ecommerce_consumer_lag

# Error rate
rate(ecommerce_errors_total[5m])
```

### Grafana Dashboards

Access Grafana at http://localhost:3000 (admin/admin)

Pre-configured dashboards include:

- Producer/Consumer metrics
- Consumer lag monitoring
- Stream processing stats
- Error and DLQ tracking

### Consumer Lag Monitoring

```bash
# Check consumer lag
docker-compose run --rm ecommerce-app \
  python -m ecommerce_analytics.monitoring.lag_monitor

# Output:
# Consumer Group: user-consumer-group
#   Topic: user-events, Partition: 0, Lag: 0
#   Topic: user-events, Partition: 1, Lag: 5
```

## Troubleshooting

### Common Issues

**Services not starting**

```bash
# Check service health
docker-compose ps

# View logs
docker-compose logs <service-name>

# Restart specific service
docker-compose restart <service-name>
```

**Consumer lag increasing**

```bash
# Scale consumers
docker-compose up -d --scale consumer=3

# Check partition assignment
docker-compose exec kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --group <consumer-group> \
  --describe
```

**Schema compatibility errors**

```bash
# List schemas
curl http://localhost:8081/subjects

# Check compatibility
curl http://localhost:8081/compatibility/subjects/<subject>/versions/latest \
  -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  -d @schemas/new-schema.avsc
```

### Debugging

```bash
# Interactive Python shell in container
docker-compose run --rm -it ecommerce-app python

# View Kafka topics
docker-compose exec kafka kafka-topics \
  --bootstrap-server localhost:9092 --list

# Consume from topic
docker-compose exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic user-events \
  --from-beginning
```

## Contributing

This is a training project. For capstone extensions:

1. Fork the repository
2. Create a feature branch
3. Follow existing code patterns
4. Add tests for new features
5. Update documentation
6. Submit a pull request

## License

This project is part of the Apache Kafka Training course materials.

---

**Next Steps:**

1. Complete the [Capstone Guide](../../../docs/exercises/capstone-guide-python.md)
2. Implement advanced features from [Capstone Extensions](../../../docs/exercises/capstone-extensions.md)
3. Explore architecture details in [ARCHITECTURE.md](./ARCHITECTURE.md)
4. Review training materials for Days 1-8
