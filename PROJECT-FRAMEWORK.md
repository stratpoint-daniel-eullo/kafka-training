# EventMart: Progressive Kafka Training Project

> **Java Developer Track - Spring Boot Project**
>
> This document describes the **EventMart Spring Boot project** for the Java Developer track. Data engineers should use pure Kafka examples in the Data Engineer track.
>
> **Choose your track**: [START-HERE.md](./START-HERE.md)

## 🎯 Project Overview

**EventMart** is a real-world e-commerce event streaming platform that trainees build progressively throughout the 8-day Kafka training course. Each day adds new functionality, culminating in a complete, demonstrable system.

## 🏗 Project Architecture

```
EventMart E-commerce Event Streaming Platform
├── User Service (Events: user-registered, user-updated, user-deleted)
├── Product Service (Events: product-created, product-updated, inventory-changed)
├── Order Service (Events: order-placed, order-confirmed, order-shipped, order-delivered)
├── Payment Service (Events: payment-initiated, payment-completed, payment-failed)
├── Notification Service (Events: email-sent, sms-sent, push-notification-sent)
├── Analytics Service (Real-time dashboards and metrics)
├── Audit Service (Compliance and security logging)
└── External Integrations (CRM, ERP, Shipping providers)
```

## 📅 Daily Progressive Build

### Day 1: Foundation & Infrastructure
**Deliverable**: EventMart Topic Architecture
- **What to Build**: Design and create the complete topic structure
- **Demo**: Show topic creation, configuration, and management
- **Assessment**: 
  - ✅ All EventMart topics created with proper partitioning
  - ✅ Topic configurations optimized for use cases
  - ✅ AdminClient operations working

**Topics to Create**:
```
eventmart-users (3 partitions, compacted)
eventmart-products (5 partitions, 7-day retention)
eventmart-orders (10 partitions, 30-day retention)
eventmart-payments (3 partitions, 90-day retention)
eventmart-notifications (2 partitions, 1-day retention)
eventmart-analytics (1 partition, 1-hour retention)
eventmart-audit (1 partition, 1-year retention)
```

### Day 2: Data Flow Design
**Deliverable**: EventMart Message Flow Documentation
- **What to Build**: Design message schemas and flow patterns
- **Demo**: Demonstrate message routing and partitioning strategies
- **Assessment**:
  - ✅ Message flow diagrams completed
  - ✅ Partitioning strategy documented
  - ✅ Consumer group design finalized

### Day 3: Event Producers
**Deliverable**: EventMart Producer Services
- **What to Build**: Implement all producer services (User, Product, Order, Payment)
- **Demo**: Generate realistic event streams for all services
- **Assessment**:
  - ✅ All producer services implemented
  - ✅ Error handling and retries working
  - ✅ Message ordering preserved where needed

### Day 4: Event Consumers
**Deliverable**: EventMart Consumer Services
- **What to Build**: Implement consumer services (Notification, Analytics, Audit)
- **Demo**: Show real-time event processing and consumer group coordination
- **Assessment**:
  - ✅ All consumer services implemented
  - ✅ Consumer groups working correctly
  - ✅ Offset management implemented

### Day 5: Stream Processing
**Deliverable**: EventMart Real-time Analytics
- **What to Build**: Kafka Streams applications for real-time metrics
- **Demo**: Live dashboard showing order metrics, user activity, revenue streams
- **Assessment**:
  - ✅ Stream processing applications working
  - ✅ Real-time aggregations accurate
  - ✅ Windowing and joins implemented

### Day 6: Schema Management
**Deliverable**: EventMart Schema Registry Integration
- **What to Build**: Implement Avro schemas for all events with evolution
- **Demo**: Show schema evolution without breaking consumers
- **Assessment**:
  - ✅ All events use Avro schemas
  - ✅ Schema evolution demonstrated
  - ✅ Backward/forward compatibility maintained

### Day 7: External Integrations
**Deliverable**: EventMart Connect Ecosystem
- **What to Build**: Kafka Connect pipelines to external systems
- **Demo**: Show data flowing to/from databases, APIs, and file systems
- **Assessment**:
  - ✅ Source connectors implemented
  - ✅ Sink connectors working
  - ✅ Data transformation pipelines active

### Day 8: Production Readiness
**Deliverable**: EventMart Production Deployment
- **What to Build**: Security, monitoring, and operational readiness
- **Demo**: Complete production-ready EventMart platform
- **Assessment**:
  - ✅ Security implemented (SSL, SASL, ACLs)
  - ✅ Monitoring and alerting configured
  - ✅ Performance optimized
  - ✅ Disaster recovery plan documented

## 🎭 Final Demo Requirements

### Demo Duration: 30 minutes
### Demo Structure:

1. **System Overview** (5 minutes)
   - Architecture explanation
   - Technology stack overview
   - Business use case presentation

2. **Live Event Generation** (10 minutes)
   - Generate user registrations
   - Create product catalog updates
   - Process order lifecycle (place → pay → ship → deliver)
   - Show real-time notifications

3. **Real-time Analytics** (5 minutes)
   - Live dashboard with metrics
   - Stream processing results
   - Business intelligence insights

4. **Operational Excellence** (5 minutes)
   - Monitoring and alerting
   - Security features
   - Performance characteristics

5. **Technical Deep Dive** (5 minutes)
   - Schema evolution demonstration
   - Error handling and recovery
   - Scaling and partitioning strategy

## 📊 Assessment Criteria for Trainers

### Technical Implementation (40 points)
- **Event Architecture** (10 pts): Proper topic design and configuration
- **Producer Implementation** (10 pts): Reliable event generation with error handling
- **Consumer Implementation** (10 pts): Correct event processing and offset management
- **Stream Processing** (10 pts): Real-time analytics and aggregations

### Advanced Features (30 points)
- **Schema Management** (10 pts): Avro implementation with evolution
- **External Integration** (10 pts): Kafka Connect pipelines
- **Production Readiness** (10 pts): Security, monitoring, performance

### Demo Presentation (20 points)
- **System Understanding** (10 pts): Clear explanation of architecture and design decisions
- **Live Demonstration** (10 pts): Smooth execution of end-to-end scenarios

### Code Quality (10 points)
- **Best Practices** (5 pts): Following Kafka development patterns
- **Documentation** (5 pts): Clear code comments and README files

### Scoring:
- **90-100**: Exceptional - Production-ready implementation
- **80-89**: Proficient - Strong understanding with minor gaps
- **70-79**: Developing - Basic functionality with some issues
- **Below 70**: Needs Improvement - Significant gaps in understanding

## 🚀 Getting Started

1. **Clone the project template**:
   ```bash
   git checkout -b eventmart-project
   mkdir -p src/main/java/com/training/kafka/eventmart
   ```

2. **Follow daily build instructions** in each day's documentation

3. **Use provided utilities**:
   - `SampleDataGenerator` for realistic test data
   - `PerformanceTester` for load testing
   - `KafkaConfig` for standardized configurations

4. **Track progress** using the daily deliverable checklists

---

This progressive project ensures trainees build real skills while creating a portfolio-worthy demonstration of Kafka expertise!
