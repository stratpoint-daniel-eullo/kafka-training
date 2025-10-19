# Error Handling

> **⚠️ Java Developer Track Only**
> REST API error handling. Data engineers handle errors in pure Kafka client code.

## HTTP Status Codes

| Code | Description | Common Causes |
|------|-------------|---------------|
| 200 | Success | Request completed successfully |
| 201 | Created | Resource created (e.g., topic) |
| 400 | Bad Request | Invalid parameters or payload |
| 404 | Not Found | Topic or resource doesn't exist |
| 409 | Conflict | Resource already exists |
| 500 | Internal Server Error | Kafka connection failed, unexpected error |
| 503 | Service Unavailable | Kafka cluster unreachable |

## Error Response Format

```json
{
  "status": "error",
  "message": "Topic 'invalid-topic' does not exist",
  "code": "TOPIC_NOT_FOUND",
  "timestamp": "2025-01-15T10:30:00Z"
}
```

## Common Errors

### Topic Not Found
**Status:** 404  
**Message:** "Topic 'xxx' does not exist"  
**Solution:** Create topic first using `/day01/create-topic`

### Connection Error
**Status:** 500  
**Message:** "Failed to connect to Kafka broker"  
**Solution:** Check Kafka is running: `docker ps | grep kafka`

### Serialization Error
**Status:** 400  
**Message:** "Failed to serialize message"  
**Solution:** Check message format matches schema

## Troubleshooting

### Check Kafka Connection
```bash
# Inside container
docker exec -it kafka-training-kafka kafka-broker-api-versions --bootstrap-server localhost:9092
```

### Check Logs
```bash
# Application logs
docker logs -f kafka-training-application

# Kafka logs
docker logs -f kafka-training-kafka
```

### Verify Topic Exists
```bash
curl http://localhost:8080/api/training/day01/list-topics
```

## Next Steps

- [Training Endpoints](training-endpoints.md) - Full API reference
- [Deployment Guide](../deployment/deployment-guide.md) - Production deployment
