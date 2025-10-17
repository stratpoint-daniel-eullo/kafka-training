# Security Architecture

## SSL/TLS Encryption

### Configuration
```java
Map<String, Object> sslConfig = new HashMap<>();
sslConfig.put("security.protocol", "SSL");
sslConfig.put(SslConfigs.SSL_PROTOCOL_CONFIG, "TLSv1.3");
sslConfig.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, "/path/to/keystore.jks");
sslConfig.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, keystorePassword);
sslConfig.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "/path/to/truststore.jks");
sslConfig.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, truststorePassword);
```

## SASL Authentication

### SCRAM-SHA-512 (Recommended)
```java
Map<String, Object> saslConfig = new HashMap<>();
saslConfig.put("security.protocol", "SASL_SSL");
saslConfig.put(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-512");
saslConfig.put(SaslConfigs.SASL_JAAS_CONFIG,
    "org.apache.kafka.common.security.scram.ScramLoginModule required " +
    "username=\"admin\" password=\"admin-secret\";");
```

### SASL/PLAIN
```java
saslConfig.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
saslConfig.put(SaslConfigs.SASL_JAAS_CONFIG,
    "org.apache.kafka.common.security.plain.PlainLoginModule required " +
    "username=\"user\" password=\"password\";");
```

## Container Security

### Non-Root User
```yaml
securityContext:
  runAsNonRoot: true
  runAsUser: 1000
  fsGroup: 1000
  readOnlyRootFilesystem: true
```

### Network Policies
```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: kafka-training-netpol
spec:
  podSelector:
    matchLabels:
      app: kafka-training
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - podSelector: {}
    ports:
    - port: 8080
  egress:
  - to:
    - podSelector:
        matchLabels:
          app: kafka
    ports:
    - port: 9092
```

## Secrets Management

### Kubernetes Secrets
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: kafka-credentials
type: Opaque
stringData:
  username: admin
  password: changeme-in-production
```

### External Secrets (AWS Secrets Manager)
```yaml
apiVersion: external-secrets.io/v1beta1
kind: ExternalSecret
metadata:
  name: kafka-external-secret
spec:
  secretStoreRef:
    name: aws-secrets-manager
  target:
    name: kafka-credentials
  data:
  - secretKey: username
    remoteRef:
      key: prod/kafka/credentials
      property: username
```

## Best Practices

1. **Never commit secrets to Git**
2. **Use TLS 1.3** for encryption
3. **Rotate credentials** regularly
4. **Implement ACLs** for topic access control
5. **Monitor security events**
6. **Run containers as non-root**
7. **Use read-only filesystems**
8. **Apply network policies**

## Next Steps

- [Deployment Guide](../deployment/deployment-guide.md) - Secure deployment
- [Production Checklist](../deployment/checklist.md) - Security verification
