# API Reference

Complete REST API documentation for the Kafka Training Spring Boot application.

!!! note "Java Developer Track Only"
    This API reference is for the **optional Spring Boot track**. Data engineers following the pure Kafka track don't need these REST APIs - use CLI tools and raw Kafka APIs instead.

## Base URL

**Local Development:** `http://localhost:8080`

**Docker Compose:** `http://localhost:8080`

**Kubernetes:** `https://your-domain.com`

## API Overview

The Kafka Training application provides REST APIs for:

- **Training Modules** - Interactive demonstrations for each training day
- **EventMart Platform** - E-commerce event streaming simulation
- **Actuator Endpoints** - Health checks and metrics
- **Management** - Application configuration and monitoring

## Quick Reference

### Training Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/training/modules` | List all training modules |
| `GET` | `/api/training/profile` | Get active profile info |
| `POST` | `/api/training/day01/demo` | Day 1 foundation demo |
| `POST` | `/api/training/day03/demo` | Day 3 producer demo |
| `POST` | `/api/training/day04/demo` | Day 4 consumer demo |

[View Training Endpoints →](training-endpoints.md)

### EventMart API

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/training/eventmart/topics` | Create EventMart topics |
| `GET` | `/api/training/eventmart/status` | Get platform status |
| `POST` | `/api/training/eventmart/simulate/user` | Simulate user registration |
| `POST` | `/api/training/eventmart/simulate/product` | Simulate product creation |
| `POST` | `/api/training/eventmart/simulate/order` | Simulate order placement |

[View EventMart API →](eventmart-api.md)

### Actuator Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/actuator/health` | Health check |
| `GET` | `/actuator/info` | Application information |
| `GET` | `/actuator/metrics` | Available metrics |
| `GET` | `/actuator/prometheus` | Prometheus metrics |

[View Actuator Endpoints →](actuator.md)

## API Standards

### Request Format

All API requests accept JSON:

```http
Content-Type: application/json
Accept: application/json
```

### Response Format

Standard success response:

```json
{
  "status": "success",
  "message": "Operation completed successfully",
  "data": {},
  "timestamp": "2025-10-17T10:30:00Z"
}
```

Standard error response:

```json
{
  "status": "error",
  "message": "Error description",
  "timestamp": "2025-10-17T10:30:00Z"
}
```

### HTTP Status Codes

| Code | Description | Usage |
|------|-------------|-------|
| 200 | OK | Successful GET/POST request |
| 201 | Created | Resource successfully created |
| 400 | Bad Request | Invalid parameters or request |
| 404 | Not Found | Endpoint or resource not found |
| 500 | Internal Server Error | Server error occurred |

## Authentication

!!! note "Training Environment"
    The current training environment does not require authentication for simplicity.

    For production deployment, implement:

    - Spring Security with JWT tokens
    - API key authentication
    - Role-based access control (RBAC)
    - Rate limiting

## Quick Examples

### Get Training Modules

```bash
curl http://localhost:8080/api/training/modules | jq
```

**Response:**

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
  }
}
```

### Run Day 1 Demo

```bash
curl -X POST http://localhost:8080/api/training/day01/demo | jq
```

**Response:**

```json
{
  "status": "success",
  "message": "Day 1 foundation demonstration completed successfully",
  "operations": [
    "Listed existing topics",
    "Retrieved cluster information",
    "Demonstrated basic admin operations"
  ],
  "timestamp": "2025-10-17T10:30:00Z"
}
```

### Create EventMart Topics

```bash
curl -X POST http://localhost:8080/api/training/eventmart/topics | jq
```

**Response:**

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
  "timestamp": "2025-10-17T10:30:00Z"
}
```

### Simulate User Registration

```bash
curl -X POST "http://localhost:8080/api/training/eventmart/simulate/user?userId=user123&email=john@example.com&name=John%20Doe" | jq
```

**Response:**

```json
{
  "status": "success",
  "message": "User registration event published successfully",
  "event": {
    "userId": "user123",
    "email": "john@example.com",
    "name": "John Doe",
    "timestamp": "2025-10-17T10:30:00Z"
  }
}
```

### Check Application Health

```bash
curl http://localhost:8080/actuator/health | jq
```

**Response:**

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

## API Clients

### cURL

```bash
# GET request
curl http://localhost:8080/api/training/modules

# POST request
curl -X POST http://localhost:8080/api/training/day01/demo

# With parameters
curl -X POST "http://localhost:8080/api/training/eventmart/simulate/user?userId=user1&email=user@example.com&name=User%20One"

# With JSON body
curl -X POST http://localhost:8080/api/custom \
  -H "Content-Type: application/json" \
  -d '{"key": "value"}'
```

### JavaScript/Fetch

```javascript
// GET request
fetch('/api/training/modules')
  .then(response => response.json())
  .then(data => console.log(data));

// POST request
fetch('/api/training/day01/demo', {
  method: 'POST'
})
  .then(response => response.json())
  .then(data => console.log(data));

// With query parameters
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

### Python

```python
import requests

# GET request
response = requests.get('http://localhost:8080/api/training/modules')
print(response.json())

# POST request
response = requests.post('http://localhost:8080/api/training/day01/demo')
print(response.json())

# With parameters
params = {
    'userId': 'user123',
    'email': 'john@example.com',
    'name': 'John Doe'
}
response = requests.post(
    'http://localhost:8080/api/training/eventmart/simulate/user',
    params=params
)
print(response.json())
```

### Java

```java
// Using RestTemplate
RestTemplate restTemplate = new RestTemplate();

// GET request
String url = "http://localhost:8080/api/training/modules";
ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

// POST request
String postUrl = "http://localhost:8080/api/training/day01/demo";
ResponseEntity<Map> postResponse = restTemplate.postForEntity(postUrl, null, Map.class);

// With parameters
String paramUrl = "http://localhost:8080/api/training/eventmart/simulate/user" +
    "?userId=user123&email=john@example.com&name=John Doe";
ResponseEntity<Map> paramResponse = restTemplate.postForEntity(paramUrl, null, Map.class);
```

## API Testing

### Postman Collection

Import the Postman collection:

```json
{
  "info": {
    "name": "Kafka Training API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Get Training Modules",
      "request": {
        "method": "GET",
        "url": "http://localhost:8080/api/training/modules"
      }
    },
    {
      "name": "Run Day 1 Demo",
      "request": {
        "method": "POST",
        "url": "http://localhost:8080/api/training/day01/demo"
      }
    }
  ]
}
```

### Automated Testing

```bash
# Run integration tests
mvn test

# Test specific endpoint
mvn test -Dtest=TrainingControllerTest#testDay01Demo
```

## Rate Limiting

!!! warning "Production Consideration"
    For production deployment, implement rate limiting:

    ```yaml
    # application-prod.yml
    spring:
      cloud:
        gateway:
          routes:
            - id: api
              predicates:
                - Path=/api/**
              filters:
                - name: RequestRateLimiter
                  args:
                    redis-rate-limiter.replenishRate: 10
                    redis-rate-limiter.burstCapacity: 20
    ```

## API Documentation

### Interactive API Docs

Access Swagger UI (future enhancement):

- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI Spec:** `http://localhost:8080/v3/api-docs`

### Detailed References

<div class="card-grid">

<div class="info-box">
<a href="training-endpoints/"><strong>Training Endpoints</strong></a><br/>
All training module APIs
</div>

<div class="info-box">
<a href="eventmart-api/"><strong>EventMart API</strong></a><br/>
E-commerce platform APIs
</div>

<div class="info-box">
<a href="actuator/"><strong>Actuator Endpoints</strong></a><br/>
Health checks and metrics
</div>

<div class="info-box">
<a href="errors/"><strong>Error Handling</strong></a><br/>
Error codes and responses
</div>

</div>

## Support

For API issues:

1. Check [Error Handling Guide](errors.md)
2. Review [Development Setup](../contributing/development-setup.md)
3. Verify Kafka is running: `docker-compose -f docker-compose-dev.yml ps`
4. Check application logs: `docker-compose -f docker-compose-dev.yml logs kafka-training-app`

---

Explore the complete API reference starting with [Training Endpoints](training-endpoints.md)
