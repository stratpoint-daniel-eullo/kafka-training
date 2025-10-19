"""
Dead Letter Queue (DLQ) Handler

Handles failed messages using DLQ pattern (Day 8).
Demonstrates error handling and poison pill management.
"""

import logging
import json
import time
from typing import Any, Dict, Optional
from confluent_kafka import Producer
from confluent_kafka.serialization import StringSerializer

from ecommerce_analytics.config.kafka_config import get_kafka_config
from ecommerce_analytics.monitoring.metrics import get_metrics

logger = logging.getLogger(__name__)


class DLQHandler:
    """
    Dead Letter Queue handler for failed messages.

    Features (Day 8):
    - Send failed messages to DLQ topic
    - Attach error metadata (timestamp, error message, source)
    - Support retry logic
    - Prometheus metrics for DLQ
    """

    DLQ_TOPIC = "dlq-topic"

    def __init__(self):
        """Initialize DLQHandler with producer."""
        self.kafka_config = get_kafka_config()
        self.metrics = get_metrics()

        # Create simple producer for DLQ (no Avro)
        producer_config = self.kafka_config.get_producer_config(
            idempotence=True,
            compression="gzip",  # More compression for error messages
        )

        # Use string serializers for DLQ
        producer_config['key.serializer'] = StringSerializer('utf_8')
        producer_config['value.serializer'] = StringSerializer('utf_8')

        self.producer = Producer(producer_config)

        logger.info("DLQHandler initialized")

    def send_to_dlq(
        self,
        original_message: Any,
        source_topic: str,
        error_message: str,
        error_type: str = "processing_error",
        partition: Optional[int] = None,
        offset: Optional[int] = None,
        additional_metadata: Optional[Dict[str, Any]] = None
    ) -> None:
        """
        Send failed message to DLQ.

        Args:
            original_message: Original message that failed
            source_topic: Topic where message originated
            error_message: Error description
            error_type: Error type (deserialization, processing, etc.)
            partition: Source partition (optional)
            offset: Source offset (optional)
            additional_metadata: Additional metadata to attach
        """
        try:
            # Build DLQ message with metadata
            dlq_message = {
                "timestamp": int(time.time() * 1000),
                "source_topic": source_topic,
                "source_partition": partition,
                "source_offset": offset,
                "error_type": error_type,
                "error_message": error_message,
                "original_message": self._serialize_message(original_message),
                "metadata": additional_metadata or {},
            }

            # Convert to JSON
            dlq_json = json.dumps(dlq_message, indent=2)

            # Produce to DLQ topic
            self.producer.produce(
                topic=self.DLQ_TOPIC,
                key=f"{source_topic}:{partition}:{offset}",
                value=dlq_json,
                on_delivery=self._delivery_callback,
            )

            # Trigger send
            self.producer.poll(0)

            # Update metrics
            self.metrics.record_dlq_message(
                source_topic=source_topic,
                error_type=error_type,
            )

            logger.warning(
                f"Sent message to DLQ - Topic: {source_topic}, "
                f"Partition: {partition}, Offset: {offset}, "
                f"Error: {error_message}"
            )

        except Exception as e:
            logger.error(f"Failed to send message to DLQ: {e}")
            # Critical failure - log and continue

    def _serialize_message(self, message: Any) -> str:
        """
        Serialize message for DLQ storage.

        Args:
            message: Message to serialize

        Returns:
            JSON string representation
        """
        try:
            # Try to serialize as JSON
            if isinstance(message, (dict, list)):
                return json.dumps(message)
            elif isinstance(message, str):
                return message
            else:
                return str(message)

        except Exception as e:
            logger.error(f"Failed to serialize message for DLQ: {e}")
            return str(message)

    def _delivery_callback(self, err, msg):
        """
        DLQ producer delivery callback.

        Args:
            err: Error if delivery failed
            msg: Message that was delivered
        """
        if err is not None:
            logger.error(f"DLQ message delivery failed: {err}")
        else:
            logger.debug(
                f"DLQ message delivered to partition {msg.partition()} "
                f"at offset {msg.offset()}"
            )

    def flush(self, timeout: float = 10.0) -> int:
        """
        Flush pending DLQ messages.

        Args:
            timeout: Timeout in seconds

        Returns:
            Number of messages still in queue
        """
        remaining = self.producer.flush(timeout)

        if remaining > 0:
            logger.warning(
                f"{remaining} DLQ messages not delivered within {timeout}s"
            )

        return remaining

    def close(self) -> None:
        """Close DLQ handler and flush pending messages."""
        logger.info("Closing DLQHandler")
        self.flush(timeout=30.0)


# Singleton instance
_dlq_handler: Optional[DLQHandler] = None


def get_dlq_handler() -> DLQHandler:
    """
    Get singleton DLQHandler instance.

    Returns:
        DLQHandler instance
    """
    global _dlq_handler
    if _dlq_handler is None:
        _dlq_handler = DLQHandler()
    return _dlq_handler
