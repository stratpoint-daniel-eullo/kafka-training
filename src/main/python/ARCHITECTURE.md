# E-Commerce Analytics Platform - Architecture

This document provides a detailed technical overview of the E-Commerce Analytics Platform architecture, design decisions, and implementation patterns.

## Table of Contents

- [System Overview](#system-overview)
- [Component Architecture](#component-architecture)
- [Data Flow](#data-flow)
- [Event Schema Design](#event-schema-design)
- [Stream Processing](#stream-processing)
- [Data Storage](#data-storage)
- [Monitoring and Observability](#monitoring-and-observability)
- [Scalability](#scalability)
- [Reliability and Fault Tolerance](#reliability-and-fault-tolerance)
- [Security](#security)
- [Design Decisions](#design-decisions)

## System Overview

The E-Commerce Analytics Platform is a distributed, event-driven system built on Apache Kafka. It processes real-time events from an e-commerce application and provides:

- **Real-time analytics**: Sub-second latency for critical metrics
- **Historical analytics**: Long-term storage in PostgreSQL
- **Fraud detection**: ML-based anomaly detection
- **Customer insights**: User behavior segmentation

### Technology Stack

| Component | Technology | Version | Purpose |
|-----------|------------|---------|---------|
| Streaming | Apache Kafka | 3.8.0 | Event backbone |
| Client Library | confluent-kafka | 2.6.1 | Producer/Consumer |
| Stream Processing | Faust | 0.11.3 | Real-time processing |
| Schema Registry | Confluent | 7.8.0 | Schema management |
| Database | PostgreSQL | 16 | Analytics storage |
| Data Integration | Kafka Connect | 7.8.0 | Sink connectors |
| Monitoring | Prometheus | 2.55.1 | Metrics collection |
| Visualization | Grafana | 11.4.0 | Dashboards |
| Orchestration | Airflow | 2.10.4 | Workflow management |
| Stream Processing | Flink | 1.20.0 | Advanced analytics |

## Component Architecture

### High-Level Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                     Event Producers                          │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │   User   │  │  Product │  │  Order   │  │ Payment  │   │
│  │  Events  │  │  Events  │  │  Events  │  │  Events  │   │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘   │
└───────┼─────────────┼─────────────┼─────────────┼──────────┘
        │             │             │             │
        └─────────────┴─────────────┴─────────────┘
                      │
                      ▼
        ┌─────────────────────────────┐
        │     Schema Registry         │◀───── Avro Schemas
        │   (Compatibility: BACKWARD) │
        └─────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                    Kafka Cluster                             │
│  ┌───────────┐  ┌───────────┐  ┌───────────┐  ┌──────────┐│
│  │   user-   │  │  product- │  │  order-   │  │ payment- ││
│  │  events   │  │  events   │  │  events   │  │  events  ││
│  │ (P=3,R=1) │  │ (P=3,R=1) │  │ (P=6,R=1) │  │(P=3,R=1) ││
│  └─────┬─────┘  └─────┬─────┘  └─────┬─────┘  └────┬─────┘│
└────────┼──────────────┼──────────────┼──────────────┼──────┘
         │              │              │              │
    ┌────┴────┬─────────┴───┬──────────┴──┬───────────┴────┐
    │         │             │             │                │
    ▼         ▼             ▼             ▼                ▼
┌──────┐  ┌──────────────────────────┐  ┌────────┐  ┌─────────┐
│Base  │  │   Faust Stream Apps      │  │ Kafka  │  │  Dead   │
│Cons. │  │ ┌──────────────────────┐ │  │Connect │  │ Letter  │
│      │  │ │ Sales Aggregator     │ │  │        │  │ Queue   │
│Users │  │ │ Fraud Detector       │ │  │JDBC    │  │         │
│Orders│  │ │ User Segmentation    │ │  │Sink    │  │Failures │
│Pymts │  │ └──────────────────────┘ │  │        │  │Retries  │
└──┬───┘  └────────────┬─────────────┘  └───┬────┘  └────┬────┘
   │                   │                     │            │
   │              RocksDB State              │            │
   │                   │                     │            │
   └───────────────────┴─────────────────────┴────────────┘
                       │
                       ▼
              ┌─────────────────┐
              │   PostgreSQL    │
              │  (Analytics DB) │
              └────────┬────────┘
                       │
                  ┌────┴────┐
                  │         │
                  ▼         ▼
          ┌───────────┐  ┌──────────┐
          │Prometheus │  │ Grafana  │
          │ Metrics   │  │Dashboard │
          └───────────┘  └──────────┘
```

### Component Responsibilities

#### Producers (Day 3)

**Location**: `ecommerce_analytics/producers/`

- **Base Producer**: Abstract class with idempotence, Avro serialization
- **Event Producers**: Domain-specific producers for user, product, order, payment events
- **Configuration**: Acks=all, retries=3, compression=snappy
- **Monitoring**: Prometheus metrics for throughput, errors, latency

#### Consumers (Day 4)

**Location**: `ecommerce_analytics/consumers/`

- **Base Consumer**: Manual offset commits, error handling, DLQ integration
- **Event Consumers**: Domain-specific processing logic
- **Configuration**: Auto-commit disabled, commit interval=10 messages
- **Monitoring**: Consumer lag, processing time, commit success rate

#### Stream Processors (Day 6)

**Location**: `ecommerce_analytics/streams/`

- **Faust Applications**: Python-native stream processing
- **State Management**: RocksDB for local state storage
- **Windowing**: Tumbling windows for aggregations
- **Processing Guarantee**: Exactly-once semantics

#### Kafka Connect (Day 7)

**Location**: `ecommerce_analytics/connect/`

- **JDBC Sink Connector**: PostgreSQL sink for analytics
- **Connector Configuration**: Batch size=1000, flush interval=30s
- **Schema Evolution**: Automatic table schema updates
- **Error Handling**: DLQ topic for failed records

## Data Flow

### Event Flow (End-to-End)

1. **Event Generation**: Sample data generator creates realistic e-commerce events
2. **Producer**: Serializes event to Avro, validates against schema, sends to Kafka
3. **Kafka**: Stores event in topic partition, replicates (if configured)
4. **Schema Registry**: Validates schema compatibility on write
5. **Consumer/Stream**: Consumes event, processes, commits offset
6. **Storage**: Persists to PostgreSQL via Kafka Connect
7. **Monitoring**: Metrics collected by Prometheus, visualized in Grafana

### Example: Order Placement Flow

```
User Action (Order Placed)
    │
    ▼
┌─────────────────────┐
│ OrderEventProducer  │
│ - validate order    │ ──────────┐
│ - serialize Avro    │           │
│ - send to topic     │           │
└─────────────────────┘           │
                                  │
                        ┌─────────▼──────────┐
                        │ Schema Registry    │
                        │ - validate schema  │
                        └─────────┬──────────┘
                                  │
                        ┌─────────▼──────────┐
                        │   order-events     │
                        │   Partition 2      │
                        └─────────┬──────────┘
                                  │
            ┌─────────────────────┼─────────────────────┐
            │                     │                     │
            ▼                     ▼                     ▼
    ┌───────────────┐   ┌─────────────────┐   ┌───────────────┐
    │OrderConsumer  │   │SalesAggregator  │   │KafkaConnect   │
    │- validate     │   │- update metrics │   │- sink to DB   │
    │- process      │   │- windowing      │   │- batch write  │
    │- commit       │   │- state update   │   │               │
    └───────────────┘   └─────────────────┘   └───┬───────────┘
                                                    │
                                          ┌─────────▼──────────┐
                                          │   PostgreSQL       │
                                          │   orders table     │
                                          └────────────────────┘
```

## Event Schema Design

### Schema Evolution Strategy

- **Compatibility Mode**: BACKWARD
- **Required Fields**: Always include for new messages
- **Optional Fields**: Use for schema evolution
- **Defaults**: Provide sensible defaults for new fields

### User Event Schema

**Location**: `ecommerce_analytics/schemas/user_events.avsc`

```json
{
  "type": "record",
  "name": "UserEvent",
  "fields": [
    {"name": "event_id", "type": "string"},
    {"name": "event_type", "type": {
      "type": "enum",
      "symbols": ["USER_REGISTERED", "USER_UPDATED", "USER_LOGIN"]
    }},
    {"name": "user_id", "type": "string"},
    {"name": "timestamp", "type": "long", "logicalType": "timestamp-millis"},
    {"name": "metadata", "type": ["null", "string"], "default": null}
  ]
}
```

### Topic-Schema Mapping

| Topic | Schema | Partitioning Strategy |
|-------|--------|----------------------|
| user-events | user_events.avsc | Hash(user_id) |
| order-events | order_events.avsc | Hash(order_id) |
| product-events | product_events.avsc | Hash(product_id) |
| payment-events | payment_events.avsc | Hash(payment_id) |

## Stream Processing

### Faust Architecture

Faust provides Python-native stream processing with exactly-once semantics:

```python
# Sales Aggregator Example
app = faust.App(
    id="sales-aggregator",
    broker="kafka://localhost:9092",
    store="rocksdb://data/rocksdb",
    processing_guarantee="exactly_once"
)

sales_table = app.Table("sales_aggregation").tumbling(
    size=timedelta(minutes=5),
    expires=timedelta(hours=1)
)

@app.agent(order_events_topic)
async def aggregate_sales(orders):
    async for order in orders:
        current_window = sales_table["global"].current()
        current_window["total_orders"] += 1
        current_window["total_revenue"] += order.total_amount
        sales_table["global"] = current_window
```

### State Management

- **Local State**: RocksDB embedded database
- **State Backup**: Changelog topics in Kafka
- **State Recovery**: Automatic rebuild from changelog on restart
- **State Size**: Monitored via Prometheus metrics

### Windowing Patterns

- **Tumbling Windows**: Fixed-size, non-overlapping (5-minute sales aggregations)
- **Hopping Windows**: Fixed-size, overlapping (user activity in last hour, every 15 min)
- **Session Windows**: Dynamic size based on inactivity gap (user sessions)

## Data Storage

### PostgreSQL Schema

**Location**: `sql/init.sql`

Key tables:

- **users**: User registration and profile data
- **products**: Product catalog
- **orders**: Order transactions
- **order_items**: Individual items within orders
- **payments**: Payment transactions
- **user_events**: User activity log
- **stream_aggregations**: Real-time metrics from Faust

### Indexes

Strategic indexes for query performance:

```sql
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_date ON orders(order_date);
CREATE INDEX idx_payments_fraud_score ON payments(fraud_score);
CREATE INDEX idx_user_events_metadata ON user_events USING GIN (metadata);
```

### Views

Analytical views for common queries:

- **daily_sales**: Aggregated sales metrics by day
- **product_performance**: Product sales and revenue
- **user_purchase_behavior**: Customer lifetime value
- **payment_method_analysis**: Payment success rates

## Monitoring and Observability

### Metrics Architecture

**Day 8 Implementation**

```
┌──────────────────────┐
│  Application Code    │
│  - Producers         │
│  - Consumers         │ ──▶ prometheus_client
│  - Streams           │     (Python library)
└──────────────────────┘
          │
          │ /metrics HTTP endpoint
          │
          ▼
    ┌──────────┐
    │Prometheus│ ──scrape──▶ Pull metrics every 15s
    └────┬─────┘
         │
         │ PromQL queries
         │
         ▼
    ┌──────────┐
    │ Grafana  │ ──dashboard──▶ Real-time visualization
    └──────────┘
```

### Key Metrics

**Producer Metrics**:
- `ecommerce_messages_produced_total`: Counter of messages produced
- `ecommerce_producer_errors_total`: Counter of producer errors
- `ecommerce_producer_latency_seconds`: Histogram of produce latency

**Consumer Metrics**:
- `ecommerce_messages_consumed_total`: Counter of messages consumed
- `ecommerce_consumer_lag`: Gauge of consumer lag (messages behind)
- `ecommerce_processing_time_seconds`: Histogram of processing time

**Stream Metrics**:
- `ecommerce_stream_events_processed_total`: Counter of events processed
- `ecommerce_stream_state_size_bytes`: Gauge of RocksDB state size

**Error Metrics**:
- `ecommerce_errors_total`: Counter of errors by type
- `ecommerce_dlq_messages_total`: Counter of DLQ messages

### Alerting Rules

Defined in `monitoring/alerts.yml` (optional):

```yaml
- alert: HighConsumerLag
  expr: ecommerce_consumer_lag > 1000
  for: 5m
  annotations:
    summary: "Consumer lag exceeds threshold"

- alert: HighErrorRate
  expr: rate(ecommerce_errors_total[5m]) > 10
  for: 2m
  annotations:
    summary: "Error rate above 10/sec"
```

## Scalability

### Horizontal Scaling

**Producers**: Stateless, scale independently
```bash
docker-compose up -d --scale producer=5
```

**Consumers**: Partition-based scaling (max = num partitions)
```bash
docker-compose up -d --scale consumer=3  # Max 3 for 3-partition topic
```

**Faust Workers**: Distributed state via changelog topics
```bash
docker-compose up -d --scale stream-processor=2
```

### Vertical Scaling

**Kafka Brokers**:
- CPU: Increase for high throughput
- Memory: Increase for page cache
- Disk: SSD for low latency

**PostgreSQL**:
- Connection pooling: 20 max connections
- Query optimization: Indexed columns
- Partitioning: Time-based table partitioning for orders

## Reliability and Fault Tolerance

### Producer Reliability (Day 3)

```python
config = {
    'enable.idempotence': True,
    'acks': 'all',
    'retries': 3,
    'max.in.flight.requests.per.connection': 5
}
```

- **Idempotence**: Prevents duplicate messages
- **Acks=all**: Waits for all replicas
- **Retries**: Automatic retry on transient failures

### Consumer Reliability (Day 4)

```python
config = {
    'enable.auto.commit': False,
    'auto.offset.reset': 'earliest',
    'session.timeout.ms': 30000
}
```

- **Manual Commits**: Commit after processing success
- **Offset Reset**: Read from earliest on new consumer group
- **Session Timeout**: 30s for failure detection

### Error Handling (Day 8)

**Dead Letter Queue Pattern**:

```python
try:
    process_message(message)
    consumer.commit()
except Exception as e:
    dlq_handler.send_to_dlq(message, error=str(e))
    consumer.commit()  # Commit to prevent reprocessing
```

**Retry Strategy**:
- Immediate retry: 3 attempts with exponential backoff
- DLQ: After max retries exceeded
- Manual replay: DLQ messages can be replayed after investigation

## Security

### Authentication (Production)

**SASL/SCRAM-SHA-256**:
```python
config = {
    'security.protocol': 'SASL_SSL',
    'sasl.mechanism': 'SCRAM-SHA-256',
    'sasl.username': 'user',
    'sasl.password': 'password'
}
```

### Encryption

**SSL/TLS**:
```python
config = {
    'security.protocol': 'SSL',
    'ssl.ca.location': '/path/to/ca-cert',
    'ssl.certificate.location': '/path/to/client-cert',
    'ssl.key.location': '/path/to/client-key'
}
```

### Authorization

**ACLs**: Topic-level permissions (configure on Kafka brokers)

## Design Decisions

### Why Faust Instead of Kafka Streams?

- **Python-native**: No JVM dependency
- **Async/await**: Efficient I/O with asyncio
- **Developer Experience**: Pythonic API, easier debugging
- **Trade-off**: Less mature than Kafka Streams, smaller ecosystem

### Why Manual Offset Commits?

- **Reliability**: Ensures messages are processed before commit
- **At-least-once**: Prevents message loss on consumer crashes
- **Trade-off**: Requires duplicate handling logic

### Why PostgreSQL Over NoSQL?

- **Transactions**: ACID guarantees for analytics queries
- **Joins**: Complex analytical queries with multiple tables
- **Tools**: Rich ecosystem for BI and reporting
- **Trade-off**: Lower write throughput than NoSQL

### Why Avro Over JSON?

- **Schema Evolution**: Backward/forward compatibility
- **Compression**: Smaller message size (30-50% reduction)
- **Validation**: Schema enforcement at write time
- **Trade-off**: Requires Schema Registry infrastructure

---

For implementation details, see:
- [README.md](./README.md) - Getting started guide
- [Capstone Guide](../../../docs/exercises/capstone-guide-python.md) - Presentation guidelines
- [Capstone Extensions](../../../docs/exercises/capstone-extensions.md) - Advanced features
