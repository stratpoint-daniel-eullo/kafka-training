# Kubernetes Deployment for Kafka Training

This directory contains production-ready Kubernetes manifests for deploying the Kafka Training application.

## 📋 Prerequisites

Before deploying, ensure you have:

1. **Kubernetes Cluster** (v1.24+)
   ```bash
   kubectl version --short
   ```

2. **Kafka Cluster** running in Kubernetes
   - Use [Strimzi Operator](https://strimzi.io/) (recommended)
   - Or managed service (Confluent Cloud, AWS MSK, etc.)

3. **PostgreSQL Database**
   - Use [CloudNativePG Operator](https://cloudnative-pg.io/)
   - Or managed service (AWS RDS, Google Cloud SQL, etc.)

4. **Docker Image** built and pushed to registry
   ```bash
   docker build -t your-registry.com/kafka-training:1.0.0 .
   docker push your-registry.com/kafka-training:1.0.0
   ```

---

## 🚀 Quick Start

### 1. Create Namespace
```bash
kubectl create namespace data-engineering
```

### 2. Update Image Reference
Edit `deployment.yaml` and update the image:
```yaml
image: your-registry.com/kafka-training:1.0.0
```

### 3. Deploy
```bash
kubectl apply -f k8s/
```

### 4. Verify Deployment
```bash
# Check pods
kubectl get pods -n data-engineering

# Check service
kubectl get svc -n data-engineering

# View logs
kubectl logs -f deployment/kafka-training-app -n data-engineering
```

### 5. Access Application
```bash
# Port forward for local access
kubectl port-forward -n data-engineering svc/kafka-training-service 8080:8080

# Open in browser
open http://localhost:8080/api/training/modules
```

---

## 📦 What's Deployed

| Resource | Purpose |
|----------|---------|
| **Deployment** | Runs 3 replicas of the app with rolling updates |
| **Service** | Exposes app on port 8080 (ClusterIP) |
| **HorizontalPodAutoscaler** | Auto-scales 3-10 pods based on CPU/memory |
| **ConfigMap** | Application configuration |
| **Secret** | Database credentials |
| **PodDisruptionBudget** | Ensures 2 pods always available |

---

## 🔧 Configuration

### Environment Variables

Key environment variables defined in `deployment.yaml`:

```yaml
# Kafka Configuration
TRAINING_KAFKA_BOOTSTRAP_SERVERS: kafka-cluster.data-platform.svc.cluster.local:9092
TRAINING_KAFKA_SCHEMA_REGISTRY_URL: http://schema-registry.data-platform.svc.cluster.local:8082

# Database Configuration
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres.data-platform.svc.cluster.local:5432/eventmart
```

**Update these based on your cluster setup!**

### Resource Limits

```yaml
resources:
  requests:
    memory: "512Mi"
    cpu: "500m"
  limits:
    memory: "1Gi"
    cpu: "1000m"
```

Adjust based on your workload requirements.

---

## 📊 Monitoring

### Health Checks

Three types of probes configured:

1. **Startup Probe**: Initial startup check (30 attempts × 5s = 150s max)
2. **Liveness Probe**: Restarts pod if unhealthy
3. **Readiness Probe**: Removes pod from service if not ready

Check probe status:
```bash
kubectl describe pod <pod-name> -n data-engineering
```

### Metrics

Prometheus annotations enable metrics scraping:
```yaml
annotations:
  prometheus.io/scrape: "true"
  prometheus.io/port: "8080"
  prometheus.io/path: "/actuator/prometheus"
```

Access metrics:
```bash
kubectl port-forward -n data-engineering svc/kafka-training-service 8080:8080
curl http://localhost:8080/actuator/prometheus
```

---

## 📈 Scaling

### Manual Scaling
```bash
# Scale to 5 replicas
kubectl scale deployment kafka-training-app -n data-engineering --replicas=5

# Verify
kubectl get pods -n data-engineering
```

### Auto-Scaling

HPA automatically scales based on CPU/memory:
- **Min replicas**: 3
- **Max replicas**: 10
- **Target CPU**: 70%
- **Target Memory**: 80%

Check HPA status:
```bash
kubectl get hpa -n data-engineering
kubectl describe hpa kafka-training-hpa -n data-engineering
```

---

## 🔄 Updates

### Rolling Update (Zero Downtime)
```bash
# Update image
kubectl set image deployment/kafka-training-app \
  app=your-registry.com/kafka-training:2.0.0 \
  -n data-engineering

# Watch rollout
kubectl rollout status deployment/kafka-training-app -n data-engineering

# View rollout history
kubectl rollout history deployment/kafka-training-app -n data-engineering
```

### Rollback
```bash
# Undo last rollout
kubectl rollout undo deployment/kafka-training-app -n data-engineering

# Rollback to specific revision
kubectl rollout undo deployment/kafka-training-app \
  --to-revision=2 \
  -n data-engineering
```

---

## 🐛 Troubleshooting

### Pod Not Starting

```bash
# Check pod events
kubectl describe pod <pod-name> -n data-engineering

# Check logs
kubectl logs <pod-name> -n data-engineering

# Check previous container logs (if pod restarted)
kubectl logs <pod-name> -n data-engineering --previous
```

### Common Issues

**Issue**: `ImagePullBackOff`
```bash
# Solution: Check image name and registry credentials
kubectl describe pod <pod-name> -n data-engineering | grep -A 5 Events
```

**Issue**: `CrashLoopBackOff`
```bash
# Solution: Check application logs
kubectl logs <pod-name> -n data-engineering --tail=100
```

**Issue**: Pod running but not receiving traffic
```bash
# Check readiness probe
kubectl describe pod <pod-name> -n data-engineering | grep -A 10 Readiness

# Test endpoint manually
kubectl exec -it <pod-name> -n data-engineering -- \
  curl http://localhost:8080/actuator/health/readiness
```

### Debug Pod

Get shell access to debug:
```bash
# Interactive shell
kubectl exec -it <pod-name> -n data-engineering -- /bin/sh

# Run commands
kubectl exec <pod-name> -n data-engineering -- env
kubectl exec <pod-name> -n data-engineering -- ps aux
```

---

## 🔒 Security Best Practices

### 1. Use Secrets for Credentials
```bash
# Create secret from file
kubectl create secret generic postgres-credentials \
  --from-literal=username=eventmart \
  --from-literal=password='your-secure-password' \
  -n data-engineering

# Or use external secret management (AWS Secrets Manager, HashiCorp Vault)
```

### 2. Run as Non-Root User
Already configured in `deployment.yaml`:
```yaml
securityContext:
  runAsNonRoot: true
  runAsUser: 1000
  fsGroup: 1000
```

### 3. Network Policies
```bash
# Create network policy to restrict traffic
kubectl apply -f network-policy.yaml
```

### 4. RBAC
```bash
# Create service account with minimal permissions
kubectl create serviceaccount kafka-training-sa -n data-engineering
```

---

## 💾 Data Persistence

If your app requires persistent storage:

```yaml
# Add to deployment.yaml
volumes:
- name: data
  persistentVolumeClaim:
    claimName: kafka-training-pvc
```

Create PVC:
```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: kafka-training-pvc
spec:
  accessModes:
  - ReadWriteOnce
  resources:
    requests:
      storage: 10Gi
  storageClassName: standard
```

---

## 🌐 Ingress (External Access)

To expose app externally using Ingress:

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: kafka-training-ingress
  namespace: data-engineering
  annotations:
    kubernetes.io/ingress.class: nginx
    cert-manager.io/cluster-issuer: letsencrypt-prod
spec:
  tls:
  - hosts:
    - kafka-training.yourdomain.com
    secretName: kafka-training-tls
  rules:
  - host: kafka-training.yourdomain.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: kafka-training-service
            port:
              number: 8080
```

Deploy:
```bash
kubectl apply -f ingress.yaml
```

---

## 📚 Production Checklist

Before going to production, ensure:

- [ ] Image tagged with version (not `:latest`)
- [ ] Resource limits set appropriately
- [ ] Secrets managed securely (not in manifests)
- [ ] Health checks configured
- [ ] Monitoring and alerting set up
- [ ] Backup strategy for data
- [ ] Network policies defined
- [ ] RBAC permissions minimal
- [ ] Pod disruption budget configured
- [ ] Horizontal pod autoscaler tested
- [ ] Rollback procedure documented
- [ ] Load testing performed

---

## 🎓 Learning Resources

- [Kubernetes Docs](https://kubernetes.io/docs/)
- [Kubernetes Best Practices](https://kubernetes.io/docs/concepts/configuration/overview/)
- [12-Factor App](https://12factor.net/)
- [Strimzi Kafka Operator](https://strimzi.io/documentation/)

---

## 🆘 Support

For issues:
1. Check pod logs: `kubectl logs -f <pod-name> -n data-engineering`
2. Check events: `kubectl get events -n data-engineering --sort-by='.lastTimestamp'`
3. Review configuration: `kubectl describe deployment kafka-training-app -n data-engineering`

**You're now ready to run Kafka data pipelines in production on Kubernetes!** 🚀
