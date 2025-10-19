"""
Base Producer Module

Provides base producer functionality with:
- Day 3: Idempotent producer with exactly-once semantics
- Day 5: Avro serialization with Schema Registry
- Day 8: Error handling and monitoring

This base class ensures consistent producer behavior across all event types.
"""

import json
import logging
from typing import Optional, Dict, Any, Callable
from confluent_kafka import SerializingProducer
from confluent_kafka.serialization import StringSerializer

from ecommerce_analytics.config.kafka_config import get_kafka_config
from ecommerce_analytics.config.settings import get_settings

logger = logging.getLogger(__name__)


class BaseProducer:
    """
    Base producer class with idempotence and Avro serialization.

    Features:
    - Idempotent producer (Day 3): Exactly-once semantics
    - Avro serialization (Day 5): Schema Registry integration
    - Error handling (Day 8): Callbacks and retry logic
    - Monitoring (Day 8): Metrics and logging
    """

    def __init__(
        self,
        topic: str,
        schema_path: str,
        to_dict: Optional[Callable] = None,
        idempotence: bool = True,
        compression: str = "snappy"
    ):
        """
        Initialize base producer.

        Args:
            topic: Kafka topic to produce to
            schema_path: Path to Avro schema file
            to_dict: Optional function to convert object to dict for serialization
            idempotence: Enable idempotent producer (exactly-once)
            compression: Compression type (snappy, gzip, lz4, zstd)
        """
        self.topic = topic
        self.schema_path = schema_path
        self.to_dict = to_dict or (lambda obj, ctx: obj)

        # Load configuration
        self.kafka_config = get_kafka_config()
        self.settings = get_settings()

        # Load Avro schema
        self.schema_str = self._load_schema()

        # Create Avro serializer
        self.value_serializer = self.kafka_config.get_avro_serializer(
            self.schema_str,
            self.to_dict
        )

        # Create producer
        self.producer = self._create_producer(idempotence, compression)

        # Metrics
        self.messages_sent = 0
        self.messages_failed = 0

    def _load_schema(self) -> str:
        """
        Load Avro schema from file.

        Returns:
            Avro schema as JSON string
        """
        try:
            with open(self.schema_path, 'r') as f:
                return f.read()
        except Exception as e:
            logger.error(f"Failed to load schema from {self.schema_path}: {e}")
            raise

    def _create_producer(self, idempotence: bool, compression: str) -> SerializingProducer:
        """
        Create SerializingProducer with Avro support.

        Args:
            idempotence: Enable idempotent producer
            compression: Compression type

        Returns:
            SerializingProducer instance
        """
        # Get producer configuration (Day 3)
        producer_config = self.kafka_config.get_producer_config(
            idempotence=idempotence,
            compression=compression
        )

        # Add serializers
        producer_config['key.serializer'] = StringSerializer('utf_8')
        producer_config['value.serializer'] = self.value_serializer

        return SerializingProducer(producer_config)

    def produce(
        self,
        key: str,
        value: Dict[str, Any],
        headers: Optional[Dict[str, str]] = None,
        on_delivery: Optional[Callable] = None
    ) -> None:
        """
        Produce a message to Kafka (async).

        Args:
            key: Message key (used for partitioning)
            value: Message value (will be serialized with Avro)
            headers: Optional message headers
            on_delivery: Optional callback for delivery reports
        """
        try:
            # Convert headers to required format
            kafka_headers = None
            if headers:
                kafka_headers = [(k, v.encode('utf-8')) for k, v in headers.items()]

            # Use default callback if none provided
            callback = on_delivery or self._default_delivery_callback

            # Produce message (async)
            self.producer.produce(
                topic=self.topic,
                key=key,
                value=value,
                headers=kafka_headers,
                on_delivery=callback
            )

            # Trigger callbacks (non-blocking)
            self.producer.poll(0)

        except Exception as e:
            logger.error(f"Failed to produce message: {e}")
            self.messages_failed += 1
            raise

    def produce_sync(
        self,
        key: str,
        value: Dict[str, Any],
        headers: Optional[Dict[str, str]] = None,
        timeout: float = 10.0
    ) -> None:
        """
        Produce a message to Kafka (synchronous).

        Args:
            key: Message key
            value: Message value
            headers: Optional message headers
            timeout: Timeout in seconds

        Raises:
            Exception if message delivery fails
        """
        self.produce(key, value, headers)

        # Wait for delivery (blocking)
        self.flush(timeout)

    def flush(self, timeout: float = 10.0) -> int:
        """
        Wait for all messages to be delivered.

        Args:
            timeout: Timeout in seconds

        Returns:
            Number of messages still in queue
        """
        remaining = self.producer.flush(timeout)

        if remaining > 0:
            logger.warning(f"{remaining} messages not delivered within {timeout}s")

        return remaining

    def close(self) -> None:
        """
        Close the producer and flush pending messages.
        """
        logger.info(f"Closing producer for topic '{self.topic}'")
        logger.info(f"Stats - Sent: {self.messages_sent}, Failed: {self.messages_failed}")

        # Flush pending messages
        self.flush(timeout=30.0)

    def _default_delivery_callback(self, err, msg):
        """
        Default delivery report callback (Day 3: Monitoring).

        Args:
            err: Error if delivery failed
            msg: Message that was delivered
        """
        if err is not None:
            logger.error(f"Message delivery failed: {err}")
            self.messages_failed += 1
        else:
            logger.debug(
                f"Message delivered to {msg.topic()} "
                f"[partition {msg.partition()}] at offset {msg.offset()}"
            )
            self.messages_sent += 1

    def get_metrics(self) -> Dict[str, int]:
        """
        Get producer metrics (Day 8: Monitoring).

        Returns:
            Dictionary with producer metrics
        """
        return {
            "messages_sent": self.messages_sent,
            "messages_failed": self.messages_failed,
            "success_rate": (
                self.messages_sent / (self.messages_sent + self.messages_failed)
                if (self.messages_sent + self.messages_failed) > 0
                else 0.0
            )
        }

    def __enter__(self):
        """Context manager entry."""
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        """Context manager exit - ensures producer is closed."""
        self.close()
