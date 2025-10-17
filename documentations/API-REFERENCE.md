# API Reference Guide

## 🌐 Overview

The Kafka Training Spring Boot application provides comprehensive REST API endpoints for all training modules and the EventMart progressive project. All endpoints return JSON responses and follow RESTful conventions.

## 🔗 Base URL

- **Local Development**: `http://localhost:8080`
- **Docker Compose**: `http://localhost:8080`
- **Production**: `https://your-domain.com`

## 📋 API Endpoints

### Training Modules

#### Get All Training Modules
```http
GET /api/training/modules
```

**Description**: Returns information about all available training modules.

**Response**:
```json
{
  "Day01Foundation": {
    "name": "Kafka Foundation",
    "description": "Basic Kafka concepts and operations",
    "endpoints": ["/api/training/day01/demo"]
  },
  "Day03Producers": {
    "name": "Kafka Producers",
    "description": "Producer patterns and configurations",
    "endpoints": ["/api/training/day03/demo"]
  },
  "Day04Consumers": {
    "name": "Kafka Consumers",
    "description": "Consumer groups and message processing",
    "endpoints": ["/api/training/day04/demo"]
  },
  "EventMart": {
    "name": "EventMart Progressive Project",
    "description": "E-commerce event streaming platform",
    "endpoints": [
      "/api/training/eventmart/topics",
      "/api/training/eventmart/status",
      "/api/training/eventmart/simulate/*"
    ]
  }
}
```

#### Get Profile Information
```http
GET /api/training/profile
```

**Description**: Returns current Spring Boot profile and configuration information.

**Response**:
```json
{
  "status": "success",
  "applicationName": "kafka-training-dev",
  "activeProfiles": ["dev"],
  "defaultProfiles": ["default"],
  "features": {
    "debugMode": true,
    "autoTopicCreation": true,
    "webInterfaceEnabled": true
  },
  "timestamp": "2025-07-14T10:30:00Z"
}
```

### Day 01 Foundation

#### Run Foundation Demonstration
```http
POST /api/training/day01/demo
```

**Description**: Executes Day 1 foundation demonstration including topic operations and cluster information.

**Response**:
```json
{
  "status": "success",
  "message": "Day 1 foundation demonstration completed successfully",
  "operations": [
    "Listed existing topics",
    "Retrieved cluster information",
    "Demonstrated basic admin operations"
  ],
  "timestamp": "2025-07-14T10:30:00Z"
}
```

### Day 03 Producers

#### Run Producer Demonstration
```http
POST /api/training/day03/demo
```

**Description**: Executes Day 3 producer demonstration with various producer patterns.

**Response**:
```json
{
  "status": "success",
  "message": "Day 3 producer demonstration completed successfully",
  "operations": [
    "Simple producer example",
    "Batch producer example",
    "Producer with callbacks"
  ],
  "messagesProduced": 100,
  "timestamp": "2025-07-14T10:30:00Z"
}
```

### Day 04 Consumers

#### Run Consumer Demonstration
```http
POST /api/training/day04/demo
```

**Description**: Executes Day 4 consumer demonstration with consumer group examples.

**Response**:
```json
{
  "status": "success",
  "message": "Day 4 consumer demonstration completed successfully",
  "operations": [
    "Simple consumer example",
    "Consumer group demonstration",
    "Offset management example"
  ],
  "messagesConsumed": 50,
  "timestamp": "2025-07-14T10:30:00Z"
}
```

### EventMart Progressive Project

#### Create EventMart Topics
```http
POST /api/training/eventmart/topics
```

**Description**: Creates all required topics for the EventMart e-commerce platform.

**Response**:
```json
{
  "status": "success",
  "message": "EventMart topics created successfully",
  "topicsCreated": [
    "eventmart-users",
    "eventmart-products", 
    "eventmart-orders",
    "eventmart-payments",
    "eventmart-inventory",
    "eventmart-analytics"
  ],
  "timestamp": "2025-07-14T10:30:00Z"
}
```

#### Get EventMart Status
```http
GET /api/training/eventmart/status
```

**Description**: Returns current status of the EventMart platform.

**Response**:
```json
{
  "status": "success",
  "topicsCreated": true,
  "activeSimulations": 0,
  "totalEvents": {
    "users": 150,
    "products": 75,
    "orders": 230
  },
  "timestamp": "2025-07-14T10:30:00Z"
}
```

#### Simulate User Registration
```http
POST /api/training/eventmart/simulate/user?userId={userId}&email={email}&name={name}
```

**Parameters**:
- `userId` (required): Unique user identifier
- `email` (required): User email address
- `name` (required): User full name

**Example**:
```http
POST /api/training/eventmart/simulate/user?userId=user123&email=john@example.com&name=John Doe
```

**Response**:
```json
{
  "status": "success",
  "message": "User registration event published successfully",
  "event": {
    "userId": "user123",
    "email": "john@example.com",
    "name": "John Doe",
    "timestamp": "2025-07-14T10:30:00Z"
  }
}
```

#### Simulate Product Creation
```http
POST /api/training/eventmart/simulate/product?productId={productId}&name={name}&category={category}&price={price}
```

**Parameters**:
- `productId` (required): Unique product identifier
- `name` (required): Product name
- `category` (required): Product category
- `price` (required): Product price (decimal)

**Example**:
```http
POST /api/training/eventmart/simulate/product?productId=prod123&name=Laptop&category=Electronics&price=999.99
```

**Response**:
```json
{
  "status": "success",
  "message": "Product creation event published successfully",
  "event": {
    "productId": "prod123",
    "name": "Laptop",
    "category": "Electronics",
    "price": 999.99,
    "timestamp": "2025-07-14T10:30:00Z"
  }
}
```

#### Simulate Order Placement
```http
POST /api/training/eventmart/simulate/order?orderId={orderId}&userId={userId}&amount={amount}
```

**Parameters**:
- `orderId` (required): Unique order identifier
- `userId` (required): User placing the order
- `amount` (required): Order total amount (decimal)

**Example**:
```http
POST /api/training/eventmart/simulate/order?orderId=order123&userId=user123&amount=999.99
```

**Response**:
```json
{
  "status": "success",
  "message": "Order placement event published successfully",
  "event": {
    "orderId": "order123",
    "userId": "user123",
    "amount": 999.99,
    "timestamp": "2025-07-14T10:30:00Z"
  }
}
```

## 🔧 Spring Boot Actuator Endpoints

### Health Check
```http
GET /actuator/health
```

**Response**:
```json
{
  "status": "UP",
  "components": {
    "diskSpace": {"status": "UP"},
    "ping": {"status": "UP"},
    "kafka": {"status": "UP"}
  }
}
```

### Application Information
```http
GET /actuator/info
```

**Response**:
```json
{
  "app": {
    "name": "kafka-training-java",
    "version": "1.0.0",
    "description": "Apache Kafka Training Course with Spring Boot"
  },
  "java": {
    "version": "11.0.26",
    "vendor": "Eclipse Adoptium"
  }
}
```

### Metrics
```http
GET /actuator/metrics
```

**Response**: List of available metrics

```http
GET /actuator/metrics/{metric-name}
```

**Example**: `GET /actuator/metrics/jvm.memory.used`

### Environment Properties
```http
GET /actuator/env
```

**Response**: All environment properties and configuration

## 🚨 Error Responses

### Standard Error Format
```json
{
  "status": "error",
  "message": "Error description",
  "timestamp": "2025-07-14T10:30:00Z"
}
```

### Common HTTP Status Codes
- `200 OK`: Successful operation
- `400 Bad Request`: Invalid parameters
- `404 Not Found`: Endpoint not found
- `500 Internal Server Error`: Server error

### Example Error Responses

#### Missing Required Parameter
```json
{
  "status": "error",
  "message": "Required parameter 'userId' is missing",
  "timestamp": "2025-07-14T10:30:00Z"
}
```

#### Kafka Connection Error
```json
{
  "status": "error",
  "message": "Failed to connect to Kafka broker: Connection refused",
  "timestamp": "2025-07-14T10:30:00Z"
}
```

## 📝 Usage Examples

### cURL Examples

#### Get Training Modules
```bash
curl -X GET http://localhost:8080/api/training/modules
```

#### Run Day 1 Demo
```bash
curl -X POST http://localhost:8080/api/training/day01/demo
```

#### Create EventMart Topics
```bash
curl -X POST http://localhost:8080/api/training/eventmart/topics
```

#### Simulate Complete EventMart Flow
```bash
# 1. Create topics
curl -X POST http://localhost:8080/api/training/eventmart/topics

# 2. Register user
curl -X POST "http://localhost:8080/api/training/eventmart/simulate/user?userId=user123&email=john@example.com&name=John Doe"

# 3. Create product
curl -X POST "http://localhost:8080/api/training/eventmart/simulate/product?productId=prod123&name=Laptop&category=Electronics&price=999.99"

# 4. Place order
curl -X POST "http://localhost:8080/api/training/eventmart/simulate/order?orderId=order123&userId=user123&amount=999.99"

# 5. Check status
curl -X GET http://localhost:8080/api/training/eventmart/status
```

### JavaScript/Fetch Examples

#### Get Profile Information
```javascript
fetch('/api/training/profile')
  .then(response => response.json())
  .then(data => console.log(data));
```

#### Simulate User Registration
```javascript
const params = new URLSearchParams({
  userId: 'user123',
  email: 'john@example.com',
  name: 'John Doe'
});

fetch(`/api/training/eventmart/simulate/user?${params}`, {
  method: 'POST'
})
.then(response => response.json())
.then(data => console.log(data));
```

## 🔒 Authentication & Security

### Current Implementation
- **No Authentication Required**: All endpoints are publicly accessible for training purposes
- **CORS Enabled**: Cross-origin requests allowed for web interface
- **Input Validation**: Basic parameter validation implemented

### Production Considerations
For production deployment, consider adding:
- **Spring Security**: Authentication and authorization
- **API Keys**: Rate limiting and access control
- **HTTPS**: Encrypted communication
- **Input Sanitization**: Enhanced security validation

This API reference provides comprehensive documentation for all available endpoints in the Kafka Training Spring Boot application.
