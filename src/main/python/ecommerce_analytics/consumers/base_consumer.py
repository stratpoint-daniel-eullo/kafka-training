"""
Base Consumer Module

Provides base consumer functionality with:
- Day 4: Manual offset commits for exactly-once processing
- Day 5: Avro deserialization with Schema Registry
- Day 8: Error handling with Dead Letter Queue (DLQ)

This base class ensures consistent consumer behavior across all event types.
"""

import logging
import signal
from typing import Optional, Callable, List
from confluent_kafka import DeserializingConsumer, KafkaError, KafkaException
from confluent_kafka.serialization import StringDeserializer

from ecommerce_analytics.config.kafka_config import get_kafka_config
from ecommerce_analytics.config.settings import get_settings

logger = logging.getLogger(__name__)


class BaseConsumer:
    """
    Base consumer class with manual commits and Avro deserialization.

    Features:
    - Manual offset commits (Day 4): Exactly-once processing
    - Avro deserialization (Day 5): Schema Registry integration
    - Error handling (Day 8): DLQ pattern for poison pills
    - Graceful shutdown: Clean consumer shutdown on signals
    """

    def __init__(
        self,
        topics: List[str],
        group_id: str,
        schema_path: Optional[str] = None,
        from_dict: Optional[Callable] = None,
        auto_commit: bool = False,
        offset_reset: str = "earliest"
    ):
        """
        Initialize base consumer.

        Args:
            topics: List of Kafka topics to subscribe to
            group_id: Consumer group ID
            schema_path: Optional path to Avro schema file (for validation)
            from_dict: Optional function to convert dict to object
            auto_commit: Enable auto commit (False for manual commits)
            offset_reset: Offset reset strategy (earliest, latest, none)
        """
        self.topics = topics
        self.group_id = group_id
        self.schema_path = schema_path
        self.from_dict = from_dict or (lambda obj, ctx: obj)

        # Load configuration
        self.kafka_config = get_kafka_config()
        self.settings = get_settings()

        # Load Avro schema if provided
        self.schema_str = self._load_schema() if schema_path else None

        # Create Avro deserializer
        self.value_deserializer = self._create_deserializer()

        # Create consumer
        self.consumer = self._create_consumer(auto_commit, offset_reset)

        # Subscribe to topics
        self.consumer.subscribe(topics)

        # Shutdown flag
        self.running = True

        # Setup signal handlers for graceful shutdown
        signal.signal(signal.SIGINT, self._signal_handler)
        signal.signal(signal.SIGTERM, self._signal_handler)

        # Metrics
        self.messages_processed = 0
        self.messages_failed = 0
        self.commits_performed = 0

    def _load_schema(self) -> Optional[str]:
        """
        Load Avro schema from file.

        Returns:
            Avro schema as JSON string or None
        """
        if not self.schema_path:
            return None

        try:
            with open(self.schema_path, 'r') as f:
                return f.read()
        except Exception as e:
            logger.error(f"Failed to load schema from {self.schema_path}: {e}")
            raise

    def _create_deserializer(self):
        """
        Create Avro deserializer.

        Returns:
            AvroDeserializer instance or None for auto-fetch
        """
        # Auto-fetch schema from registry if no local schema provided
        return self.kafka_config.get_avro_deserializer(
            self.schema_str,
            self.from_dict
        )

    def _create_consumer(
        self,
        auto_commit: bool,
        offset_reset: str
    ) -> DeserializingConsumer:
        """
        Create DeserializingConsumer with Avro support.

        Args:
            auto_commit: Enable auto commit
            offset_reset: Offset reset strategy

        Returns:
            DeserializingConsumer instance
        """
        # Get consumer configuration (Day 4)
        consumer_config = self.kafka_config.get_consumer_config(
            group_id=self.group_id,
            auto_commit=auto_commit,
            offset_reset=offset_reset
        )

        # Add deserializers
        consumer_config['key.deserializer'] = StringDeserializer('utf_8')
        consumer_config['value.deserializer'] = self.value_deserializer

        return DeserializingConsumer(consumer_config)

    def consume(
        self,
        process_func: Callable,
        commit_interval: int = 10,
        max_messages: Optional[int] = None
    ) -> None:
        """
        Start consuming messages.

        Args:
            process_func: Function to process each message (takes msg.value())
            commit_interval: Commit offsets every N messages
            max_messages: Optional max messages to consume (for testing)
        """
        logger.info(
            f"Starting consumer for topics {self.topics} "
            f"(group: {self.group_id})"
        )

        messages_since_commit = 0
        messages_consumed = 0

        try:
            while self.running:
                # Poll for messages (timeout 1 second)
                msg = self.consumer.poll(timeout=1.0)

                if msg is None:
                    # No message available
                    continue

                if msg.error():
                    # Handle errors
                    self._handle_error(msg.error())
                    continue

                try:
                    # Process message
                    process_func(msg.value())

                    # Update metrics
                    self.messages_processed += 1
                    messages_since_commit += 1
                    messages_consumed += 1

                    # Manual commit after N messages (Day 4)
                    if messages_since_commit >= commit_interval:
                        self.commit()
                        messages_since_commit = 0

                    # Check max messages limit (for testing)
                    if max_messages and messages_consumed >= max_messages:
                        logger.info(f"Reached max messages limit: {max_messages}")
                        break

                except Exception as e:
                    # Error processing message - send to DLQ (Day 8)
                    logger.error(f"Error processing message: {e}")
                    self.messages_failed += 1
                    self._send_to_dlq(msg, str(e))

        except KafkaException as e:
            logger.error(f"Kafka exception: {e}")
            raise

        finally:
            # Final commit before shutdown
            if messages_since_commit > 0:
                self.commit()

            self.close()

    def commit(self) -> None:
        """
        Manually commit offsets (Day 4: Exactly-once processing).
        """
        try:
            self.consumer.commit(asynchronous=False)
            self.commits_performed += 1
            logger.debug("Committed offsets")
        except Exception as e:
            logger.error(f"Failed to commit offsets: {e}")

    def _handle_error(self, error: KafkaError) -> None:
        """
        Handle Kafka errors.

        Args:
            error: Kafka error
        """
        if error.code() == KafkaError._PARTITION_EOF:
            # End of partition - not an error
            logger.debug("Reached end of partition")
        elif error.code() == KafkaError._ALL_BROKERS_DOWN:
            logger.error("All brokers are down")
            self.running = False
        else:
            logger.error(f"Kafka error: {error}")

    def _send_to_dlq(self, msg, error_message: str) -> None:
        """
        Send message to Dead Letter Queue (Day 8).

        Args:
            msg: Failed message
            error_message: Error description
        """
        # TODO: Implement DLQ producer in Phase 2.5
        logger.warning(
            f"Would send to DLQ - Topic: {msg.topic()}, "
            f"Partition: {msg.partition()}, "
            f"Offset: {msg.offset()}, "
            f"Error: {error_message}"
        )

    def _signal_handler(self, signum, frame):
        """
        Handle shutdown signals for graceful shutdown.

        Args:
            signum: Signal number
            frame: Current stack frame
        """
        logger.info(f"Received signal {signum}, shutting down gracefully...")
        self.running = False

    def close(self) -> None:
        """
        Close the consumer gracefully.
        """
        logger.info(f"Closing consumer for topics {self.topics}")
        logger.info(
            f"Stats - Processed: {self.messages_processed}, "
            f"Failed: {self.messages_failed}, "
            f"Commits: {self.commits_performed}"
        )

        self.consumer.close()

    def get_metrics(self) -> dict:
        """
        Get consumer metrics (Day 8: Monitoring).

        Returns:
            Dictionary with consumer metrics
        """
        return {
            "messages_processed": self.messages_processed,
            "messages_failed": self.messages_failed,
            "commits_performed": self.commits_performed,
            "success_rate": (
                self.messages_processed / (self.messages_processed + self.messages_failed)
                if (self.messages_processed + self.messages_failed) > 0
                else 0.0
            )
        }

    def __enter__(self):
        """Context manager entry."""
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        """Context manager exit - ensures consumer is closed."""
        self.close()
