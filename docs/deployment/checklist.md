# Production Readiness Checklist

This comprehensive checklist ensures your Kafka Training application is production-ready. Review and complete all items before deploying to production environments.

## Overview

Production deployments require careful planning and verification. This checklist covers all critical aspects of running Apache Kafka applications in production on Kubernetes.

!!! warning "Critical Importance"
    Skipping items on this checklist can result in data loss, security vulnerabilities, downtime, or poor performance. Review each section carefully.

## Infrastructure Checklist

### Kubernetes Cluster

- [ ] **Cluster Version**: Running supported Kubernetes version (1.24+)
- [ ] **Node Count**: Minimum 3 nodes for high availability
- [ ] **Node Resources**: Each node has sufficient CPU and memory
- [ ] **Storage Class**: Persistent storage configured (if needed)
- [ ] **Network Policies**: Network segmentation implemented
- [ ] **Cluster Autoscaling**: Cluster autoscaler configured (cloud environments)
- [ ] **Backup Strategy**: Cluster backup and recovery plan in place

```bash
# Verify cluster version
kubectl version

# Check node count and resources
kubectl get nodes
kubectl describe nodes | grep -A 5 "Allocated resources"

# Verify storage classes
kubectl get storageclasses
```

### Kafka Infrastructure

- [ ] **Kafka Version**: Compatible version (3.x+)
- [ ] **Replication Factor**: Topics have replication factor ≥ 3
- [ ] **Partition Count**: Topics appropriately partitioned
- [ ] **Retention Policy**: Data retention configured appropriately
- [ ] **Min ISR**: `min.insync.replicas` configured (typically 2)
- [ ] **Rack Awareness**: Broker rack awareness configured
- [ ] **Monitoring**: Kafka JMX metrics exposed and collected

```bash
# Check Kafka version
kubectl exec -it kafka-0 -n data-platform -- kafka-broker-api-versions --bootstrap-server localhost:9092

# List topics and describe critical ones
kubectl exec -it kafka-0 -n data-platform -- kafka-topics --bootstrap-server localhost:9092 --list
kubectl exec -it kafka-0 -n data-platform -- kafka-topics --bootstrap-server localhost:9092 --describe --topic eventmart-orders
```

### Database (PostgreSQL)

- [ ] **PostgreSQL Version**: Running supported version (12+)
- [ ] **Replication**: Primary-replica setup configured
- [ ] **Backups**: Automated backups enabled
- [ ] **Connection Pooling**: PgBouncer or similar configured
- [ ] **Resource Limits**: CPU and memory limits set
- [ ] **Monitoring**: Database metrics collected
- [ ] **Backup Testing**: Backup restoration tested

```bash
# Verify database connectivity
kubectl exec -it postgres-0 -n data-platform -- psql -U eventmart -c "SELECT version();"

# Check database size
kubectl exec -it postgres-0 -n data-platform -- psql -U eventmart -c "SELECT pg_size_pretty(pg_database_size('eventmart'));"
```

## Application Configuration

### Docker Image

- [ ] **Image Tag**: Specific version tag (not `latest`)
- [ ] **Image Registry**: Stored in private registry
- [ ] **Image Scanning**: Security scanning completed
- [ ] **Multi-Stage Build**: Using multi-stage Dockerfile
- [ ] **Non-Root User**: Container runs as non-root
- [ ] **Image Size**: Optimized image size (<500 MB)
- [ ] **Vulnerability Scan**: No critical vulnerabilities

```bash
# Check image details
docker images | grep kafka-training
docker inspect kafka-training:1.0.0

# Scan for vulnerabilities (using Trivy)
trivy image kafka-training:1.0.0
```

### Deployment Configuration

- [ ] **Namespace**: Deployed to dedicated namespace
- [ ] **Replicas**: Minimum 3 replicas for HA
- [ ] **Resource Requests**: CPU and memory requests set
- [ ] **Resource Limits**: CPU and memory limits set
- [ ] **Environment**: Correct Spring profile activated
- [ ] **Labels**: Proper labels for monitoring and selection
- [ ] **Annotations**: Prometheus scrape annotations present

```bash
# Verify deployment
kubectl get deployment kafka-training-app -n data-engineering -o yaml

# Check replica count
kubectl get deployment kafka-training-app -n data-engineering

# Verify resource settings
kubectl describe deployment kafka-training-app -n data-engineering | grep -A 10 "Limits\|Requests"
```

### ConfigMaps and Secrets

- [ ] **Secrets**: Database passwords stored in Secrets
- [ ] **Secret Management**: Using external secret manager (Vault, AWS Secrets Manager)
- [ ] **ConfigMap**: Non-sensitive config in ConfigMap
- [ ] **Environment Variables**: Sensitive data not in plain environment variables
- [ ] **Secret Rotation**: Secret rotation strategy defined
- [ ] **Access Control**: RBAC limiting secret access

```bash
# Check secrets (verify they exist but don't expose values)
kubectl get secrets -n data-engineering

# Verify ConfigMap
kubectl get configmap kafka-training-config -n data-engineering -o yaml
```

### Health Checks

- [ ] **Startup Probe**: Configured with appropriate timing
- [ ] **Liveness Probe**: Configured to detect deadlocks
- [ ] **Readiness Probe**: Configured to detect unready state
- [ ] **Health Endpoint**: `/actuator/health` responds correctly
- [ ] **Probe Thresholds**: Failure thresholds set appropriately
- [ ] **Probe Timeouts**: Timeout values tested under load

```bash
# Test health endpoints
kubectl port-forward -n data-engineering svc/kafka-training-service 8080:8080
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/health/liveness
curl http://localhost:8080/actuator/health/readiness

# Verify probe configuration
kubectl describe deployment kafka-training-app -n data-engineering | grep -A 15 "Liveness\|Readiness\|Startup"
```

## Security Hardening

### Container Security

- [ ] **Non-Root**: Container runs as non-root user (UID 1001)
- [ ] **Read-Only Filesystem**: Root filesystem is read-only
- [ ] **Capabilities**: Linux capabilities dropped
- [ ] **Security Context**: Pod security context configured
- [ ] **No Privileged**: Container not running privileged
- [ ] **AppArmor/SELinux**: Security profiles applied

```yaml
# Verify security context in deployment
securityContext:
  runAsNonRoot: true
  runAsUser: 1001
  fsGroup: 1001
  readOnlyRootFilesystem: true  # Add if possible
  capabilities:
    drop:
    - ALL
```

```bash
# Check running processes in container
kubectl exec -it kafka-training-app-xyz -n data-engineering -- ps aux
```

### Network Security

- [ ] **Network Policies**: Ingress/egress policies defined
- [ ] **Service Mesh**: Consider Istio/Linkerd for mTLS
- [ ] **TLS**: HTTPS enabled for external endpoints
- [ ] **Certificate Management**: Cert-manager or equivalent configured
- [ ] **Firewall Rules**: Cloud firewall rules restrict access
- [ ] **Private Endpoints**: Services not publicly exposed unnecessarily

```yaml
# Example network policy
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: kafka-training-netpol
  namespace: data-engineering
spec:
  podSelector:
    matchLabels:
      app: kafka-training
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: monitoring
    ports:
    - protocol: TCP
      port: 8080
  egress:
  - to:
    - namespaceSelector:
        matchLabels:
          name: data-platform
    ports:
    - protocol: TCP
      port: 9092  # Kafka
    - protocol: TCP
      port: 5432  # PostgreSQL
```

### Kafka Security

- [ ] **SSL/TLS**: Kafka communication encrypted
- [ ] **SASL Authentication**: Client authentication enabled
- [ ] **ACLs**: Authorization configured for topics
- [ ] **Schema Registry Security**: Authentication enabled
- [ ] **Connect Security**: Connector credentials secured

```yaml
# Example Kafka security configuration
env:
- name: SPRING_KAFKA_SECURITY_PROTOCOL
  value: "SASL_SSL"
- name: SPRING_KAFKA_PROPERTIES_SASL_MECHANISM
  value: "SCRAM-SHA-512"
- name: SPRING_KAFKA_PROPERTIES_SASL_JAAS_CONFIG
  valueFrom:
    secretKeyRef:
      name: kafka-credentials
      key: jaas-config
```

### Authentication and Authorization

- [ ] **RBAC**: Kubernetes RBAC roles defined
- [ ] **Service Accounts**: Dedicated service account created
- [ ] **Pod Security**: Pod Security Standards enforced
- [ ] **Least Privilege**: Minimal required permissions granted
- [ ] **Audit Logging**: Kubernetes audit logs enabled

```bash
# Create service account
kubectl create serviceaccount kafka-training-sa -n data-engineering

# Create role
kubectl create role kafka-training-role \
  --verb=get,list \
  --resource=configmaps,secrets \
  -n data-engineering

# Bind role
kubectl create rolebinding kafka-training-binding \
  --role=kafka-training-role \
  --serviceaccount=data-engineering:kafka-training-sa \
  -n data-engineering
```

## High Availability

### Replication and Redundancy

- [ ] **Multiple Replicas**: At least 3 pod replicas running
- [ ] **Pod Anti-Affinity**: Pods spread across nodes/zones
- [ ] **PodDisruptionBudget**: PDB configured (minAvailable: 2)
- [ ] **Rolling Updates**: RollingUpdate strategy configured
- [ ] **MaxUnavailable**: Set to 0 for zero-downtime deployments
- [ ] **MaxSurge**: Set to 1 or more for faster updates

```yaml
# Verify deployment strategy
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0

# Verify PDB
kubectl get pdb kafka-training-pdb -n data-engineering
```

### Graceful Shutdown

- [ ] **Termination Grace Period**: Set to 30+ seconds
- [ ] **PreStop Hook**: Configured if needed
- [ ] **Offset Commit**: Consumers commit offsets before shutdown
- [ ] **Connection Draining**: Active requests completed before termination

```yaml
spec:
  terminationGracePeriodSeconds: 30
  containers:
  - name: app
    lifecycle:
      preStop:
        exec:
          command: ["/bin/sh", "-c", "sleep 10"]  # Allow time for graceful shutdown
```

### Failure Recovery

- [ ] **Restart Policy**: Set to `Always`
- [ ] **Liveness Restart**: Pods restart on health check failure
- [ ] **Offset Strategy**: Consumer offset strategy appropriate
- [ ] **Dead Letter Queue**: DLQ configured for failed messages
- [ ] **Circuit Breaker**: Circuit breakers for external dependencies

## Monitoring and Observability

### Metrics Collection

- [ ] **Prometheus**: Metrics scraped by Prometheus
- [ ] **Actuator Endpoints**: `/actuator/prometheus` exposed
- [ ] **JVM Metrics**: JVM metrics collected
- [ ] **Kafka Metrics**: Consumer/producer metrics exposed
- [ ] **Custom Metrics**: Application-specific metrics defined
- [ ] **Metrics Retention**: Adequate retention period (15+ days)

```bash
# Verify Prometheus is scraping
kubectl port-forward -n monitoring svc/prometheus 9090:9090
# Open http://localhost:9090/targets

# Test metrics endpoint
curl http://localhost:8080/actuator/prometheus | grep kafka
```

### Dashboards

- [ ] **Grafana**: Dashboards created
- [ ] **Application Dashboard**: Spring Boot dashboard imported
- [ ] **Kafka Dashboard**: Consumer lag dashboard configured
- [ ] **Infrastructure Dashboard**: Node/pod resource usage
- [ ] **Business Metrics**: Business-specific dashboards
- [ ] **Dashboard Testing**: All panels display data correctly

### Alerting

- [ ] **AlertManager**: Configured and tested
- [ ] **Critical Alerts**: High lag, pod down, errors
- [ ] **Warning Alerts**: High CPU/memory, slow requests
- [ ] **Notification Channels**: Slack/PagerDuty configured
- [ ] **Alert Runbooks**: Documentation for each alert
- [ ] **On-Call Rotation**: On-call schedule defined

```yaml
# Example critical alerts
- alert: PodDown
  expr: up{job="kafka-training"} == 0
  for: 5m
  labels:
    severity: critical

- alert: HighConsumerLag
  expr: kafka_consumer_fetch_manager_records_lag_max > 10000
  for: 10m
  labels:
    severity: warning
```

### Logging

- [ ] **Structured Logging**: JSON format enabled
- [ ] **Log Aggregation**: Logs sent to ELK/Loki
- [ ] **Log Retention**: Appropriate retention period
- [ ] **Log Levels**: Production log levels set (INFO)
- [ ] **Sensitive Data**: No passwords/secrets in logs
- [ ] **Correlation IDs**: Trace IDs for distributed tracing

```yaml
# Configure structured logging
env:
- name: LOGGING_PATTERN_CONSOLE
  value: '{"timestamp":"%d{yyyy-MM-dd HH:mm:ss.SSS}","level":"%p","thread":"%t","logger":"%c","message":"%m"}%n'
```

## Scaling and Performance

### Auto-Scaling

- [ ] **HPA Configured**: HorizontalPodAutoscaler created
- [ ] **Metrics Server**: Metrics server installed
- [ ] **CPU Threshold**: Appropriate CPU target (60-80%)
- [ ] **Memory Threshold**: Appropriate memory target
- [ ] **Custom Metrics**: Consumer lag-based scaling (if applicable)
- [ ] **Scale Limits**: Min/max replicas set appropriately
- [ ] **Scaling Behavior**: Scale-up/down policies configured

```bash
# Verify HPA
kubectl get hpa -n data-engineering
kubectl describe hpa kafka-training-hpa -n data-engineering

# Test scaling
kubectl top pods -n data-engineering
```

### Resource Optimization

- [ ] **Resource Requests**: Match actual usage patterns
- [ ] **Resource Limits**: Set with 20-30% headroom
- [ ] **JVM Tuning**: JVM flags optimized for containers
- [ ] **Garbage Collection**: G1GC or ZGC configured
- [ ] **Connection Pools**: Database connection pooling optimized
- [ ] **Batch Sizes**: Kafka batch sizes tuned
- [ ] **Concurrency**: Consumer concurrency optimized

### Performance Testing

- [ ] **Load Testing**: Application load tested
- [ ] **Stress Testing**: Tested under extreme load
- [ ] **Latency Testing**: P50, P95, P99 latencies measured
- [ ] **Throughput Testing**: Max throughput determined
- [ ] **Soak Testing**: 24+ hour stability test completed
- [ ] **Chaos Testing**: Chaos engineering tests passed

```bash
# Example load test with Apache Bench
ab -n 10000 -c 100 http://kafka-training-service:8080/actuator/health

# Kafka load test
kafka-producer-perf-test \
  --topic test-topic \
  --num-records 1000000 \
  --record-size 1024 \
  --throughput 10000 \
  --producer-props bootstrap.servers=kafka:9092
```

## Backup and Disaster Recovery

### Backup Strategy

- [ ] **Database Backups**: Automated PostgreSQL backups
- [ ] **Kafka Topics**: Critical topics backed up (Kafka MirrorMaker)
- [ ] **Configuration Backups**: K8s manifests in version control
- [ ] **State Backups**: Kafka Streams state stores backed up
- [ ] **Backup Testing**: Restore process tested monthly
- [ ] **RTO/RPO Defined**: Recovery objectives documented
- [ ] **Backup Monitoring**: Backup success/failure alerts

### Disaster Recovery

- [ ] **DR Plan**: Documented disaster recovery plan
- [ ] **Multi-Region**: Multi-region deployment (if required)
- [ ] **Failover Testing**: Failover procedures tested
- [ ] **Data Replication**: Cross-region replication configured
- [ ] **DNS Failover**: Automatic DNS failover configured
- [ ] **DR Drills**: Quarterly DR drills scheduled
- [ ] **Runbooks**: Step-by-step recovery procedures

## Compliance and Documentation

### Documentation

- [ ] **Architecture Diagram**: Current architecture documented
- [ ] **Runbooks**: Operational runbooks created
- [ ] **API Documentation**: API endpoints documented
- [ ] **Configuration Guide**: Environment setup documented
- [ ] **Troubleshooting Guide**: Common issues and solutions
- [ ] **Change Log**: Version history maintained
- [ ] **Contact Information**: On-call contacts documented

### Compliance

- [ ] **Data Retention**: Complies with regulations (GDPR, etc.)
- [ ] **Data Encryption**: At-rest and in-transit encryption
- [ ] **Audit Logging**: Compliance audit logs enabled
- [ ] **Access Controls**: Role-based access enforced
- [ ] **Data Privacy**: PII handling compliant
- [ ] **Vulnerability Scanning**: Regular security scans
- [ ] **Penetration Testing**: Annual pen tests completed

## Deployment Process

### Pre-Deployment

- [ ] **Code Review**: All changes peer reviewed
- [ ] **Unit Tests**: All tests passing
- [ ] **Integration Tests**: Integration tests passing
- [ ] **Security Scan**: Code security scan completed
- [ ] **Staging Deployment**: Tested in staging environment
- [ ] **Rollback Plan**: Rollback procedure documented
- [ ] **Change Approval**: Change request approved

### Deployment Execution

- [ ] **Maintenance Window**: Scheduled during low-traffic period
- [ ] **Communication**: Stakeholders notified
- [ ] **Monitoring**: Extra monitoring during deployment
- [ ] **Smoke Tests**: Post-deployment smoke tests
- [ ] **Health Checks**: All health checks passing
- [ ] **Metrics Validation**: Metrics look normal
- [ ] **Log Review**: No errors in logs

### Post-Deployment

- [ ] **Performance Check**: Performance meets SLAs
- [ ] **Error Rate**: Error rate within acceptable limits
- [ ] **Consumer Lag**: No abnormal lag increases
- [ ] **Resource Usage**: CPU/memory within expected ranges
- [ ] **User Acceptance**: User acceptance testing completed
- [ ] **Documentation Updated**: Deployment docs updated
- [ ] **Lessons Learned**: Post-mortem if issues occurred

## Final Verification

Before declaring production-ready, verify:

```bash
# 1. All pods running and ready
kubectl get pods -n data-engineering
# Expected: All pods 1/1 READY, STATUS Running

# 2. Health checks passing
curl http://your-domain/actuator/health
# Expected: {"status":"UP"}

# 3. Metrics being collected
kubectl port-forward -n monitoring svc/prometheus 9090:9090
# Verify targets are up

# 4. HPA active
kubectl get hpa -n data-engineering
# Expected: HPA showing current metrics

# 5. PDB in place
kubectl get pdb -n data-engineering
# Expected: PDB exists with correct minAvailable

# 6. Secrets configured
kubectl get secrets -n data-engineering
# Expected: postgres-credentials exists

# 7. No critical vulnerabilities
trivy image kafka-training:1.0.0 --severity CRITICAL
# Expected: 0 CRITICAL vulnerabilities

# 8. Resource limits set
kubectl describe deployment kafka-training-app -n data-engineering | grep -A 5 "Limits"
# Expected: Limits defined for CPU and memory

# 9. Logging working
kubectl logs -n data-engineering -l app=kafka-training --tail=10
# Expected: Recent structured logs

# 10. Consumer lag reasonable
curl http://your-domain/api/training/day02/consumer-lag/training-consumer-group
# Expected: Lag < acceptable threshold
```

## Sign-Off

Production deployment requires sign-off from:

- [ ] **Development Team Lead**: Code quality and functionality verified
- [ ] **DevOps/SRE**: Infrastructure and deployment verified
- [ ] **Security Team**: Security requirements met
- [ ] **QA Team**: Testing completed successfully
- [ ] **Product Owner**: Business requirements met
- [ ] **On-Call Engineer**: Runbooks and monitoring verified

## Next Steps

After completing this checklist:

1. Schedule production deployment
2. Execute deployment following the plan
3. Monitor closely for first 24-48 hours
4. Conduct post-deployment review
5. Update documentation with lessons learned

!!! success "Production Ready"
    Congratulations! Once all items are checked, your Kafka Training application is ready for production deployment.

## Related Documentation

- [Deployment Guide](deployment-guide.md) - Step-by-step deployment instructions
- [Monitoring](monitoring.md) - Monitoring and alerting setup
- [Scaling](scaling.md) - Auto-scaling configuration
- [Security](../architecture/security.md) - Security best practices
