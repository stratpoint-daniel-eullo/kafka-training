"""
Prometheus Metrics Exporter

Exposes Kafka producer/consumer metrics to Prometheus (Day 8).
Demonstrates monitoring and observability best practices.
"""

import logging
from typing import Dict, Any
from prometheus_client import Counter, Histogram, Gauge, start_http_server

from ecommerce_analytics.config.settings import get_settings

logger = logging.getLogger(__name__)


class PrometheusMetrics:
    """
    Prometheus metrics for Kafka producers and consumers.

    Metrics (Day 8):
    - Messages produced/consumed
    - Producer/consumer lag
    - Processing latency
    - Error rates
    - Consumer group lag
    """

    def __init__(self, namespace: str = "ecommerce"):
        """
        Initialize Prometheus metrics.

        Args:
            namespace: Metrics namespace
        """
        self.namespace = namespace
        self.settings = get_settings()

        # Producer metrics
        self.messages_produced = Counter(
            f"{namespace}_messages_produced_total",
            "Total messages produced",
            ["topic", "producer_type"],
        )

        self.produce_latency = Histogram(
            f"{namespace}_produce_latency_seconds",
            "Message production latency",
            ["topic", "producer_type"],
        )

        self.produce_errors = Counter(
            f"{namespace}_produce_errors_total",
            "Total produce errors",
            ["topic", "producer_type", "error_type"],
        )

        # Consumer metrics
        self.messages_consumed = Counter(
            f"{namespace}_messages_consumed_total",
            "Total messages consumed",
            ["topic", "consumer_group"],
        )

        self.consume_latency = Histogram(
            f"{namespace}_consume_latency_seconds",
            "Message consumption latency",
            ["topic", "consumer_group"],
        )

        self.consume_errors = Counter(
            f"{namespace}_consume_errors_total",
            "Total consume errors",
            ["topic", "consumer_group", "error_type"],
        )

        # Consumer lag metrics
        self.consumer_lag = Gauge(
            f"{namespace}_consumer_lag",
            "Consumer lag (messages behind)",
            ["topic", "partition", "consumer_group"],
        )

        # Stream processing metrics
        self.stream_records_processed = Counter(
            f"{namespace}_stream_records_processed_total",
            "Total stream records processed",
            ["stream_app", "topic"],
        )

        self.stream_processing_errors = Counter(
            f"{namespace}_stream_processing_errors_total",
            "Total stream processing errors",
            ["stream_app", "error_type"],
        )

        # DLQ metrics (Day 8)
        self.dlq_messages = Counter(
            f"{namespace}_dlq_messages_total",
            "Total messages sent to DLQ",
            ["source_topic", "error_type"],
        )

        logger.info(f"Prometheus metrics initialized (namespace: {namespace})")

    def record_message_produced(
        self,
        topic: str,
        producer_type: str,
        latency_seconds: float = None
    ):
        """
        Record a produced message.

        Args:
            topic: Topic name
            producer_type: Producer type (user, order, product, payment)
            latency_seconds: Production latency in seconds
        """
        self.messages_produced.labels(
            topic=topic,
            producer_type=producer_type,
        ).inc()

        if latency_seconds is not None:
            self.produce_latency.labels(
                topic=topic,
                producer_type=producer_type,
            ).observe(latency_seconds)

    def record_produce_error(
        self,
        topic: str,
        producer_type: str,
        error_type: str
    ):
        """
        Record a produce error.

        Args:
            topic: Topic name
            producer_type: Producer type
            error_type: Error type (timeout, serialization, network, etc.)
        """
        self.produce_errors.labels(
            topic=topic,
            producer_type=producer_type,
            error_type=error_type,
        ).inc()

    def record_message_consumed(
        self,
        topic: str,
        consumer_group: str,
        latency_seconds: float = None
    ):
        """
        Record a consumed message.

        Args:
            topic: Topic name
            consumer_group: Consumer group ID
            latency_seconds: Consumption latency in seconds
        """
        self.messages_consumed.labels(
            topic=topic,
            consumer_group=consumer_group,
        ).inc()

        if latency_seconds is not None:
            self.consume_latency.labels(
                topic=topic,
                consumer_group=consumer_group,
            ).observe(latency_seconds)

    def record_consume_error(
        self,
        topic: str,
        consumer_group: str,
        error_type: str
    ):
        """
        Record a consume error.

        Args:
            topic: Topic name
            consumer_group: Consumer group ID
            error_type: Error type (deserialization, processing, etc.)
        """
        self.consume_errors.labels(
            topic=topic,
            consumer_group=consumer_group,
            error_type=error_type,
        ).inc()

    def set_consumer_lag(
        self,
        topic: str,
        partition: int,
        consumer_group: str,
        lag: int
    ):
        """
        Set consumer lag gauge.

        Args:
            topic: Topic name
            partition: Partition number
            consumer_group: Consumer group ID
            lag: Lag in messages
        """
        self.consumer_lag.labels(
            topic=topic,
            partition=str(partition),
            consumer_group=consumer_group,
        ).set(lag)

    def record_dlq_message(
        self,
        source_topic: str,
        error_type: str
    ):
        """
        Record a message sent to DLQ.

        Args:
            source_topic: Original topic
            error_type: Error type that caused DLQ
        """
        self.dlq_messages.labels(
            source_topic=source_topic,
            error_type=error_type,
        ).inc()

    def start_server(self, port: int = None):
        """
        Start Prometheus HTTP server.

        Args:
            port: HTTP server port (default from settings)
        """
        if port is None:
            port = self.settings.prometheus_port

        start_http_server(port)
        logger.info(f"Prometheus metrics server started on port {port}")
        logger.info(f"Metrics available at: http://localhost:{port}/metrics")


# Singleton instance
_metrics: PrometheusMetrics = None


def get_metrics(namespace: str = "ecommerce") -> PrometheusMetrics:
    """
    Get singleton PrometheusMetrics instance.

    Args:
        namespace: Metrics namespace

    Returns:
        PrometheusMetrics instance
    """
    global _metrics
    if _metrics is None:
        _metrics = PrometheusMetrics(namespace)
    return _metrics
