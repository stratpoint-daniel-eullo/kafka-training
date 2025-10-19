# Spring Boot Actuator Endpoints

> **⚠️ Java Developer Track Only**
> Spring Boot Actuator endpoints. Data engineers use JMX, Prometheus, and Kafka's native monitoring tools instead.

Actuator provides production-ready endpoints for monitoring and management.

## Health Checks

### Overall Health
```http
GET /actuator/health
```

**Response:**
```json
{
  "status": "UP",
  "components": {
    "kafka": {"status": "UP"},
    "db": {"status": "UP"},
    "diskSpace": {"status": "UP"}
  }
}
```

### Liveness Probe
```http
GET /actuator/health/liveness
```

Used by Kubernetes liveness probe. Returns 200 if app is alive.

### Readiness Probe
```http
GET /actuator/health/readiness
```

Used by Kubernetes readiness probe. Returns 200 if app is ready for traffic.

## Metrics

### Prometheus Metrics
```http
GET /actuator/prometheus
```

Returns metrics in Prometheus format for scraping.

### Application Info
```http
GET /actuator/info
```

Returns application version and build information.

## Kubernetes Integration

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080

readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
```

## Next Steps

- [Monitoring](../deployment/monitoring.md) - Prometheus and Grafana setup
- [Deployment Guide](../deployment/deployment-guide.md) - Kubernetes deployment
