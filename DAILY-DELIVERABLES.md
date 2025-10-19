# EventMart Daily Deliverables & Demo Requirements

> **⚠️ Java Developer Track Only**
> This guide is for the EventMart progressive project (Spring Boot). Data engineers follow the pure Kafka curriculum in [README-DATA-ENGINEERS.md](./README-DATA-ENGINEERS.md) and focus on CLI tools and platform-agnostic patterns.

## 📋 Overview

Each day of the Kafka training builds upon the previous day's work, creating a complete EventMart e-commerce platform using Spring Boot. This is the Java Developer track project.

---

## Day 1: Foundation & Infrastructure

### 🎯 **Deliverable**: EventMart Topic Architecture
**Time Allocation**: 3 hours | **Demo Time**: 10 minutes

#### What to Build:
1. **Complete Topic Structure** using `EventMartTopicManager.java`
2. **Topic Configuration Documentation** explaining design decisions
3. **AdminClient Operations** demonstrating topic management

#### Demo Requirements:
```bash
# 1. Show topic creation (2 minutes)
mvn exec:java -Dexec.mainClass="com.training.kafka.eventmart.EventMartTopicManager"

# 2. Demonstrate topic listing and description (3 minutes)
confluent local kafka topic list
confluent local kafka topic describe eventmart-orders

# 3. Explain partitioning strategy (5 minutes)
# - Why 10 partitions for orders?
# - Why compaction for users?
# - Why different retention periods?
```

#### Assessment Checklist:
- [ ] All 7 EventMart topics created successfully
- [ ] Proper partitioning strategy implemented
- [ ] Appropriate retention and cleanup policies configured
- [ ] AdminClient operations working correctly
- [ ] Can explain design decisions clearly

#### Expected Output:
```
=== EventMart Topic Architecture ===
Successfully created 7 EventMart topics
Topic: eventmart-orders
  Partitions: 10
  Replication Factor: 1
  retention.ms: 2592000000
```

---

## Day 2: Data Flow Design

### 🎯 **Deliverable**: EventMart Message Flow Documentation
**Time Allocation**: 3 hours | **Demo Time**: 10 minutes

#### What to Build:
1. **Event Schema Definitions** using `EventMartEvents.java`
2. **Message Flow Diagrams** showing event relationships
3. **Partitioning Strategy Document** explaining key selection

#### Demo Requirements:
```bash
# 1. Show event schema generation (3 minutes)
# Generate sample events and show JSON structure

# 2. Explain message flow (4 minutes)
# User Registration → Order Placement → Payment → Notification

# 3. Demonstrate partitioning strategy (3 minutes)
# Show how user_id, order_id ensure proper routing
```

#### Assessment Checklist:
- [ ] All event schemas defined with proper JSON structure
- [ ] Message flow documented with clear relationships
- [ ] Partitioning strategy explained and justified
- [ ] Event versioning strategy documented
- [ ] Can trace complete user journey through events

#### Expected Output:
- Complete event schema documentation
- Visual message flow diagram
- Partitioning strategy document

---

## Day 3: Event Producers

### 🎯 **Deliverable**: EventMart Producer Services
**Time Allocation**: 3 hours | **Demo Time**: 15 minutes

#### What to Build:
1. **UserService Producer** - User registration and updates
2. **ProductService Producer** - Product catalog management
3. **OrderService Producer** - Order lifecycle events
4. **PaymentService Producer** - Payment processing events

#### Demo Requirements:
```bash
# 1. Generate user events (3 minutes)
mvn exec:java -Dexec.mainClass="com.training.kafka.eventmart.producers.UserService"

# 2. Create product catalog (3 minutes)
mvn exec:java -Dexec.mainClass="com.training.kafka.eventmart.producers.ProductService"

# 3. Process complete order flow (6 minutes)
# Show: Order Placed → Payment Initiated → Payment Completed

# 4. Demonstrate error handling (3 minutes)
# Show retry logic and error scenarios
```

#### Assessment Checklist:
- [ ] All producer services implemented and working
- [ ] Proper error handling and retry logic
- [ ] Message ordering preserved where required
- [ ] Idempotent producers configured
- [ ] Can generate realistic event streams

#### Expected Output:
```
=== EventMart Producer Demo ===
User registered: user-123
Product created: product-456
Order placed: order-789
Payment completed: payment-101
All events sent successfully with proper partitioning
```

---

## Day 4: Event Consumers

### 🎯 **Deliverable**: EventMart Consumer Services
**Time Allocation**: 3 hours | **Demo Time**: 15 minutes

#### What to Build:
1. **NotificationService Consumer** - Process notification events
2. **AnalyticsService Consumer** - Real-time metrics collection
3. **AuditService Consumer** - Compliance and security logging
4. **InventoryService Consumer** - Stock level management

#### Demo Requirements:
```bash
# 1. Start all consumer services (2 minutes)
# Show consumer group coordination

# 2. Generate events and show consumption (8 minutes)
# Demonstrate real-time processing of user/order events

# 3. Show consumer group rebalancing (3 minutes)
# Stop/start consumers to show partition reassignment

# 4. Demonstrate offset management (2 minutes)
# Show manual offset commits and recovery
```

#### Assessment Checklist:
- [ ] All consumer services implemented and processing events
- [ ] Consumer groups working with proper coordination
- [ ] Manual offset management implemented
- [ ] Error handling and dead letter queue logic
- [ ] Can demonstrate real-time event processing

#### Expected Output:
```
=== EventMart Consumer Demo ===
NotificationService: Processing user-registered event
AnalyticsService: Order metrics updated
AuditService: Security event logged
Consumer group rebalancing completed
```

---

## Day 5: Stream Processing

### 🎯 **Deliverable**: EventMart Real-time Analytics
**Time Allocation**: 3 hours | **Demo Time**: 15 minutes

#### What to Build:
1. **Order Metrics Stream** - Real-time order aggregations
2. **User Activity Stream** - User behavior analytics
3. **Revenue Stream** - Real-time revenue calculations
4. **Inventory Alerts Stream** - Low stock notifications

#### Demo Requirements:
```bash
# 1. Start stream processing applications (2 minutes)

# 2. Generate order events and show real-time metrics (8 minutes)
# Show live dashboard with:
# - Orders per minute
# - Revenue per hour
# - Top products
# - User activity patterns

# 3. Demonstrate windowing and joins (3 minutes)
# Show time-based aggregations

# 4. Show stream topology (2 minutes)
# Explain processing graph and state stores
```

#### Assessment Checklist:
- [ ] Stream processing applications working correctly
- [ ] Real-time aggregations accurate and timely
- [ ] Windowing operations implemented properly
- [ ] Stream-stream and stream-table joins working
- [ ] Can explain stream processing topology

#### Expected Output:
```
=== EventMart Stream Processing Demo ===
Real-time Metrics Dashboard:
- Orders/minute: 45
- Revenue/hour: $12,450
- Top product: laptop-pro-15
- Active users: 234
Stream topology: 4 processors, 2 state stores
```

---

## Day 6: Schema Management

### 🎯 **Deliverable**: EventMart Schema Registry Integration
**Time Allocation**: 3 hours | **Demo Time**: 15 minutes

#### What to Build:
1. **Avro Schema Definitions** for all EventMart events
2. **Schema Evolution Examples** showing backward/forward compatibility
3. **Schema Registry Integration** in producers and consumers
4. **Schema Validation** and error handling

#### Demo Requirements:
```bash
# 1. Show schema registration (3 minutes)
# Register all EventMart schemas

# 2. Demonstrate schema evolution (8 minutes)
# Add optional field to UserRegistered event
# Show backward compatibility with existing consumers

# 3. Show schema validation (2 minutes)
# Attempt to send invalid data

# 4. Demonstrate schema compatibility checks (2 minutes)
# Show compatibility matrix
```

#### Assessment Checklist:
- [ ] All events converted to Avro with proper schemas
- [ ] Schema evolution demonstrated successfully
- [ ] Backward and forward compatibility maintained
- [ ] Schema Registry integration working
- [ ] Can explain schema evolution strategies

#### Expected Output:
```
=== EventMart Schema Management Demo ===
Schemas registered: 8 event types
Schema evolution: UserRegistered v1 → v2
Compatibility: BACKWARD maintained
All producers/consumers using Avro serialization
```

---

## Day 7: External Integrations

### 🎯 **Deliverable**: EventMart Connect Ecosystem
**Time Allocation**: 3 hours | **Demo Time**: 15 minutes

#### What to Build:
1. **Database Sink Connector** - Store events in PostgreSQL
2. **File Source Connector** - Import product catalogs
3. **REST API Sink** - Send notifications to external service
4. **Custom Transformation** - Data enrichment pipeline

#### Demo Requirements:
```bash
# 1. Show connector deployment (3 minutes)
# Deploy and configure all connectors

# 2. Demonstrate data flow (8 minutes)
# Show events flowing from Kafka to database
# Import product data from CSV file

# 3. Show connector management (2 minutes)
# Pause/resume connectors, check status

# 4. Demonstrate transformations (2 minutes)
# Show data enrichment and filtering
```

#### Assessment Checklist:
- [ ] Source and sink connectors deployed and working
- [ ] Data transformations implemented correctly
- [ ] Connector management operations working
- [ ] Error handling and dead letter queues configured
- [ ] Can explain Connect architecture and use cases

#### Expected Output:
```
=== EventMart Connect Demo ===
Connectors deployed: 4 active
Database sink: 1,234 events stored
File source: 500 products imported
REST sink: 89 notifications sent
All connectors healthy and processing
```

---

## Day 8: Production Readiness

### 🎯 **Deliverable**: EventMart Production Deployment
**Time Allocation**: 3 hours | **Demo Time**: 20 minutes

#### What to Build:
1. **Security Configuration** - SSL, SASL, ACLs
2. **Monitoring Setup** - Metrics, alerts, dashboards
3. **Performance Optimization** - Tuning and benchmarks
4. **Disaster Recovery Plan** - Backup and restore procedures

#### Demo Requirements:
```bash
# 1. Show security features (5 minutes)
# SSL encryption, user authentication, ACL authorization

# 2. Demonstrate monitoring (8 minutes)
# Live metrics dashboard
# Alert simulation and response

# 3. Show performance optimization (4 minutes)
# Throughput and latency measurements
# Configuration tuning results

# 4. Disaster recovery simulation (3 minutes)
# Backup and restore demonstration
```

#### Assessment Checklist:
- [ ] Security implemented (SSL, SASL, ACLs)
- [ ] Monitoring and alerting configured
- [ ] Performance optimized and documented
- [ ] Disaster recovery plan documented and tested
- [ ] Production readiness checklist completed

#### Expected Output:
```
=== EventMart Production Demo ===
Security: SSL + SASL-SCRAM enabled
Monitoring: 25 metrics tracked, 5 alerts configured
Performance: 50K msgs/sec, 10ms p99 latency
DR Plan: Tested backup/restore procedures
Production readiness: ✅ CERTIFIED
```

---

## 🎭 Final Demo (30 minutes)

### Complete EventMart Platform Demonstration

1. **System Overview** (5 min) - Architecture and business value
2. **Live Event Generation** (10 min) - End-to-end user journey
3. **Real-time Analytics** (5 min) - Live dashboard and insights
4. **Operational Excellence** (5 min) - Monitoring, security, performance
5. **Technical Deep Dive** (5 min) - Schema evolution, error handling, scaling

### Success Criteria:
- Complete working EventMart platform
- All 8 daily deliverables integrated
- Professional presentation of technical solution
- Ability to answer questions about design decisions
- Demonstration of production-ready system
