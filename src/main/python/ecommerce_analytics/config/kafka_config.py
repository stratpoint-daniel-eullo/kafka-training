"""
Kafka Configuration Module

Provides configuration for:
- Day 3: Producer configs (idempotence, compression, batching)
- Day 4: Consumer configs (manual commits, offset management)
- Day 5: Avro serialization/deserialization
- Day 8: Security (SSL/TLS, SASL/SCRAM)

This module centralizes all Kafka client configurations to ensure
consistency across producers and consumers.
"""

import os
from typing import Dict, Any, Optional
from confluent_kafka import SerializingProducer, DeserializingConsumer
from confluent_kafka.schema_registry import SchemaRegistryClient
from confluent_kafka.schema_registry.avro import AvroSerializer, AvroDeserializer


class KafkaConfig:
    """
    Centralized Kafka configuration management.

    Supports multiple environments (dev, test, prod) and security modes.
    """

    def __init__(self, environment: str = "dev"):
        """
        Initialize Kafka configuration.

        Args:
            environment: Environment name (dev, test, prod)
        """
        self.environment = environment
        self.bootstrap_servers = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092")
        self.schema_registry_url = os.getenv("SCHEMA_REGISTRY_URL", "http://localhost:8081")

        # Security configuration (Day 8)
        self.security_protocol = os.getenv("KAFKA_SECURITY_PROTOCOL", "PLAINTEXT")
        self.sasl_mechanism = os.getenv("KAFKA_SASL_MECHANISM", "SCRAM-SHA-512")
        self.sasl_username = os.getenv("KAFKA_SASL_USERNAME", "")
        self.sasl_password = os.getenv("KAFKA_SASL_PASSWORD", "")

        # SSL configuration
        self.ssl_ca_location = os.getenv("KAFKA_SSL_CA_LOCATION", "")
        self.ssl_cert_location = os.getenv("KAFKA_SSL_CERT_LOCATION", "")
        self.ssl_key_location = os.getenv("KAFKA_SSL_KEY_LOCATION", "")

    def get_base_config(self) -> Dict[str, Any]:
        """
        Get base Kafka configuration shared by producers and consumers.

        Returns:
            Base configuration dictionary
        """
        config = {
            'bootstrap.servers': self.bootstrap_servers,
            'client.id': f'ecommerce-analytics-{self.environment}',
        }

        # Add security config if not PLAINTEXT (Day 8)
        if self.security_protocol != "PLAINTEXT":
            config.update(self._get_security_config())

        return config

    def _get_security_config(self) -> Dict[str, Any]:
        """
        Get security configuration for SSL/SASL (Day 8).

        Returns:
            Security configuration dictionary
        """
        config = {
            'security.protocol': self.security_protocol,
        }

        # SASL configuration
        if 'SASL' in self.security_protocol:
            config.update({
                'sasl.mechanism': self.sasl_mechanism,
                'sasl.username': self.sasl_username,
                'sasl.password': self.sasl_password,
            })

        # SSL configuration
        if 'SSL' in self.security_protocol:
            config.update({
                'ssl.ca.location': self.ssl_ca_location,
                'ssl.certificate.location': self.ssl_cert_location,
                'ssl.key.location': self.ssl_key_location,
            })

        return config

    def get_producer_config(
        self,
        idempotence: bool = True,
        compression: str = "snappy",
        linger_ms: int = 10,
        batch_size: int = 16384
    ) -> Dict[str, Any]:
        """
        Get producer configuration (Day 3).

        Args:
            idempotence: Enable idempotent producer (exactly-once)
            compression: Compression type (none, gzip, snappy, lz4, zstd)
            linger_ms: Delay in ms to allow batching
            batch_size: Batch size in bytes

        Returns:
            Producer configuration dictionary
        """
        config = self.get_base_config()

        # Day 3: Producer configurations
        config.update({
            # Idempotence for exactly-once semantics
            'enable.idempotence': idempotence,

            # Durability - wait for all replicas
            'acks': 'all',

            # Retry configuration
            'retries': 3,
            'retry.backoff.ms': 100,

            # Compression for network efficiency
            'compression.type': compression,

            # Batching for throughput
            'linger.ms': linger_ms,
            'batch.size': batch_size,

            # Max in-flight requests (must be <=5 for idempotence)
            'max.in.flight.requests.per.connection': 5,

            # Timeout configurations
            'request.timeout.ms': 30000,
            'delivery.timeout.ms': 120000,
        })

        return config

    def get_consumer_config(
        self,
        group_id: str,
        auto_commit: bool = False,
        offset_reset: str = "earliest"
    ) -> Dict[str, Any]:
        """
        Get consumer configuration (Day 4).

        Args:
            group_id: Consumer group ID
            auto_commit: Enable auto commit (False for manual commits)
            offset_reset: Offset reset strategy (earliest, latest, none)

        Returns:
            Consumer configuration dictionary
        """
        config = self.get_base_config()

        # Day 4: Consumer configurations
        config.update({
            # Consumer group
            'group.id': group_id,

            # Manual offset management for exactly-once processing
            'enable.auto.commit': auto_commit,

            # Offset reset strategy
            'auto.offset.reset': offset_reset,

            # Session and heartbeat timeouts
            'session.timeout.ms': 30000,
            'heartbeat.interval.ms': 10000,

            # Max poll records and interval
            'max.poll.records': 100,
            'max.poll.interval.ms': 300000,

            # Fetch configuration
            'fetch.min.bytes': 1,
            'fetch.max.wait.ms': 500,
        })

        return config

    def get_schema_registry_client(self) -> SchemaRegistryClient:
        """
        Get Schema Registry client (Day 5).

        Returns:
            SchemaRegistryClient instance
        """
        config = {
            'url': self.schema_registry_url,
        }

        # Add basic auth if configured
        sr_username = os.getenv("SCHEMA_REGISTRY_USERNAME", "")
        sr_password = os.getenv("SCHEMA_REGISTRY_PASSWORD", "")

        if sr_username and sr_password:
            config.update({
                'basic.auth.credentials.source': 'USER_INFO',
                'basic.auth.user.info': f"{sr_username}:{sr_password}",
            })

        return SchemaRegistryClient(config)

    def get_avro_serializer(
        self,
        schema_str: str,
        to_dict: Optional[callable] = None
    ) -> AvroSerializer:
        """
        Get Avro serializer for producers (Day 5).

        Args:
            schema_str: Avro schema as JSON string
            to_dict: Optional function to convert object to dict

        Returns:
            AvroSerializer instance
        """
        schema_registry_client = self.get_schema_registry_client()

        return AvroSerializer(
            schema_registry_client,
            schema_str,
            to_dict
        )

    def get_avro_deserializer(
        self,
        schema_str: Optional[str] = None,
        from_dict: Optional[callable] = None
    ) -> AvroDeserializer:
        """
        Get Avro deserializer for consumers (Day 5).

        Args:
            schema_str: Optional Avro schema (None to auto-fetch from registry)
            from_dict: Optional function to convert dict to object

        Returns:
            AvroDeserializer instance
        """
        schema_registry_client = self.get_schema_registry_client()

        return AvroDeserializer(
            schema_registry_client,
            schema_str,
            from_dict
        )


# Singleton instance
_kafka_config: Optional[KafkaConfig] = None


def get_kafka_config(environment: str = "dev") -> KafkaConfig:
    """
    Get singleton KafkaConfig instance.

    Args:
        environment: Environment name

    Returns:
        KafkaConfig instance
    """
    global _kafka_config
    if _kafka_config is None:
        _kafka_config = KafkaConfig(environment)
    return _kafka_config
