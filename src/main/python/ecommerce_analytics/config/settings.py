"""
Application Settings Module

Centralized settings for the E-Commerce Analytics Platform.
Covers topic names, partition counts, retention policies, and database settings.
"""

import os
from dataclasses import dataclass, field
from typing import Dict


@dataclass
class TopicConfig:
    """Configuration for a single Kafka topic."""
    name: str
    partitions: int
    replication_factor: int
    retention_ms: int  # Retention time in milliseconds
    cleanup_policy: str = "delete"
    compression_type: str = "snappy"


@dataclass
class Settings:
    """
    Application settings for E-Commerce Analytics Platform.

    Designed to be environment-aware and support:
    - Days 1-2: Topic configurations
    - Days 3-4: Producer/Consumer settings
    - Day 7: Kafka Connect configurations
    - Day 8: Database and monitoring settings
    """

    # Environment
    environment: str = field(default_factory=lambda: os.getenv("ENV", "dev"))

    # Kafka Broker Configuration
    kafka_brokers: str = field(
        default_factory=lambda: os.getenv("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092")
    )

    # Schema Registry
    schema_registry_url: str = field(
        default_factory=lambda: os.getenv("SCHEMA_REGISTRY_URL", "http://localhost:8081")
    )

    # Kafka Connect
    connect_url: str = field(
        default_factory=lambda: os.getenv("KAFKA_CONNECT_URL", "http://localhost:8083")
    )

    # PostgreSQL Configuration (for Kafka Connect JDBC Sink - Day 7)
    postgres_host: str = field(
        default_factory=lambda: os.getenv("POSTGRES_HOST", "localhost")
    )
    postgres_port: int = field(
        default_factory=lambda: int(os.getenv("POSTGRES_PORT", "5432"))
    )
    postgres_database: str = field(
        default_factory=lambda: os.getenv("POSTGRES_DATABASE", "analytics")
    )
    postgres_user: str = field(
        default_factory=lambda: os.getenv("POSTGRES_USER", "kafka")
    )
    postgres_password: str = field(
        default_factory=lambda: os.getenv("POSTGRES_PASSWORD", "kafka-password")
    )

    # Prometheus Metrics (Day 8)
    prometheus_port: int = field(
        default_factory=lambda: int(os.getenv("PROMETHEUS_PORT", "9090"))
    )
    metrics_enabled: bool = field(
        default_factory=lambda: os.getenv("METRICS_ENABLED", "true").lower() == "true"
    )

    def get_postgres_jdbc_url(self) -> str:
        """Get PostgreSQL JDBC URL for Kafka Connect."""
        return (
            f"jdbc:postgresql://{self.postgres_host}:{self.postgres_port}/"
            f"{self.postgres_database}"
        )

    def get_postgres_connection_string(self) -> str:
        """Get PostgreSQL connection string for Python."""
        return (
            f"postgresql://{self.postgres_user}:{self.postgres_password}@"
            f"{self.postgres_host}:{self.postgres_port}/{self.postgres_database}"
        )

    @property
    def topics(self) -> Dict[str, TopicConfig]:
        """
        Get all topic configurations (Days 1-2).

        Retention periods are based on business requirements:
        - User events: 7 days (GDPR compliance)
        - Order events: 30 days (order lifecycle)
        - Product events: 7 days (inventory updates)
        - Payment events: 90 days (financial compliance)
        - Analytics: 7-30 days (operational metrics)
        - DLQ: 14 days (error debugging)
        """
        return {
            # Core event topics
            "user-events": TopicConfig(
                name="user-events",
                partitions=3,
                replication_factor=1,
                retention_ms=7 * 24 * 60 * 60 * 1000,  # 7 days
                compression_type="snappy"
            ),
            "order-events": TopicConfig(
                name="order-events",
                partitions=6,
                replication_factor=1,
                retention_ms=30 * 24 * 60 * 60 * 1000,  # 30 days
                compression_type="snappy"
            ),
            "product-events": TopicConfig(
                name="product-events",
                partitions=3,
                replication_factor=1,
                retention_ms=7 * 24 * 60 * 60 * 1000,  # 7 days
                compression_type="snappy"
            ),
            "payment-events": TopicConfig(
                name="payment-events",
                partitions=6,
                replication_factor=1,
                retention_ms=90 * 24 * 60 * 60 * 1000,  # 90 days (compliance)
                compression_type="snappy"
            ),

            # Analytics/aggregation topics (Day 6: Streams)
            "sales-analytics": TopicConfig(
                name="sales-analytics",
                partitions=3,
                replication_factor=1,
                retention_ms=7 * 24 * 60 * 60 * 1000,  # 7 days
                compression_type="snappy"
            ),
            "user-behavior": TopicConfig(
                name="user-behavior",
                partitions=3,
                replication_factor=1,
                retention_ms=7 * 24 * 60 * 60 * 1000,  # 7 days
                compression_type="snappy"
            ),
            "revenue-metrics": TopicConfig(
                name="revenue-metrics",
                partitions=3,
                replication_factor=1,
                retention_ms=30 * 24 * 60 * 60 * 1000,  # 30 days
                compression_type="snappy"
            ),

            # Dead Letter Queue (Day 8: Error Handling)
            "dlq-topic": TopicConfig(
                name="dlq-topic",
                partitions=1,
                replication_factor=1,
                retention_ms=14 * 24 * 60 * 60 * 1000,  # 14 days
                compression_type="gzip",  # More compression for errors
                cleanup_policy="delete"
            ),
        }

    def get_topic_names(self) -> list:
        """Get list of all topic names."""
        return list(self.topics.keys())

    def get_schema_path(self, schema_name: str) -> str:
        """
        Get path to Avro schema file.

        Args:
            schema_name: Schema filename (e.g., 'user_events.avsc')

        Returns:
            Full path to schema file
        """
        import os
        base_dir = os.path.dirname(os.path.dirname(__file__))
        return os.path.join(base_dir, "schemas", schema_name)


# Singleton instance
_settings: Settings = None


def get_settings() -> Settings:
    """
    Get singleton Settings instance.

    Returns:
        Settings instance
    """
    global _settings
    if _settings is None:
        _settings = Settings()
    return _settings
