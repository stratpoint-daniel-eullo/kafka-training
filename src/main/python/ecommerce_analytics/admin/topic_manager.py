"""
Topic Manager

Manages Kafka topics using AdminClient (Days 1-2).
Demonstrates topic creation, configuration, and management.
"""

import logging
from typing import List, Dict, Any, Optional
from kafka import KafkaAdminClient
from kafka.admin import NewTopic, ConfigResource, ConfigResourceType
from kafka.errors import TopicAlreadyExistsError, UnknownTopicOrPartitionError

from ecommerce_analytics.config.settings import get_settings

logger = logging.getLogger(__name__)


class TopicManager:
    """
    Manages Kafka topics using AdminClient.

    Features (Days 1-2):
    - Create topics with custom configurations
    - Delete topics
    - List topics
    - Describe topic configurations
    - Alter topic configurations
    """

    def __init__(self):
        """Initialize TopicManager with AdminClient."""
        self.settings = get_settings()

        # Create AdminClient
        self.admin_client = KafkaAdminClient(
            bootstrap_servers=self.settings.kafka_brokers,
            client_id="topic-manager",
        )

        logger.info(f"TopicManager initialized (brokers: {self.settings.kafka_brokers})")

    def create_all_topics(self) -> Dict[str, bool]:
        """
        Create all topics defined in settings.

        Returns:
            Dictionary mapping topic names to success status
        """
        logger.info("Creating all topics from configuration...")

        results = {}
        topics_config = self.settings.topics

        for topic_name, topic_config in topics_config.items():
            try:
                success = self.create_topic(
                    name=topic_config.name,
                    partitions=topic_config.partitions,
                    replication_factor=topic_config.replication_factor,
                    retention_ms=topic_config.retention_ms,
                    cleanup_policy=topic_config.cleanup_policy,
                    compression_type=topic_config.compression_type,
                )
                results[topic_name] = success
            except Exception as e:
                logger.error(f"Failed to create topic {topic_name}: {e}")
                results[topic_name] = False

        # Summary
        successful = sum(1 for v in results.values() if v)
        logger.info(f"Topic creation complete: {successful}/{len(results)} successful")

        return results

    def create_topic(
        self,
        name: str,
        partitions: int = 3,
        replication_factor: int = 1,
        retention_ms: Optional[int] = None,
        cleanup_policy: str = "delete",
        compression_type: str = "snappy",
        **extra_config
    ) -> bool:
        """
        Create a single Kafka topic with configuration.

        Args:
            name: Topic name
            partitions: Number of partitions
            replication_factor: Replication factor
            retention_ms: Retention time in milliseconds
            cleanup_policy: Cleanup policy (delete, compact)
            compression_type: Compression type (snappy, gzip, lz4, zstd)
            **extra_config: Additional topic configurations

        Returns:
            True if created, False if already exists
        """
        # Build topic configuration
        topic_config = {
            "cleanup.policy": cleanup_policy,
            "compression.type": compression_type,
        }

        if retention_ms is not None:
            topic_config["retention.ms"] = str(retention_ms)

        # Add extra config
        topic_config.update(extra_config)

        # Create NewTopic
        new_topic = NewTopic(
            name=name,
            num_partitions=partitions,
            replication_factor=replication_factor,
            topic_configs=topic_config,
        )

        try:
            # Create topic
            self.admin_client.create_topics(
                new_topics=[new_topic],
                validate_only=False,
            )

            logger.info(
                f"Created topic '{name}': {partitions} partitions, "
                f"replication={replication_factor}, "
                f"retention={retention_ms}ms, "
                f"compression={compression_type}"
            )
            return True

        except TopicAlreadyExistsError:
            logger.warning(f"Topic '{name}' already exists")
            return False

        except Exception as e:
            logger.error(f"Failed to create topic '{name}': {e}")
            raise

    def delete_topic(self, name: str) -> bool:
        """
        Delete a Kafka topic.

        Args:
            name: Topic name

        Returns:
            True if deleted, False if not found
        """
        try:
            self.admin_client.delete_topics(topics=[name])
            logger.info(f"Deleted topic '{name}'")
            return True

        except UnknownTopicOrPartitionError:
            logger.warning(f"Topic '{name}' does not exist")
            return False

        except Exception as e:
            logger.error(f"Failed to delete topic '{name}': {e}")
            raise

    def delete_all_topics(self) -> Dict[str, bool]:
        """
        Delete all topics defined in settings.

        Returns:
            Dictionary mapping topic names to success status
        """
        logger.info("Deleting all configured topics...")

        results = {}
        topics_config = self.settings.topics

        for topic_name in topics_config.keys():
            try:
                success = self.delete_topic(topic_name)
                results[topic_name] = success
            except Exception as e:
                logger.error(f"Failed to delete topic {topic_name}: {e}")
                results[topic_name] = False

        return results

    def list_topics(self) -> List[str]:
        """
        List all topics in the Kafka cluster.

        Returns:
            List of topic names
        """
        try:
            topics = self.admin_client.list_topics()
            logger.info(f"Found {len(topics)} topics in cluster")
            return topics

        except Exception as e:
            logger.error(f"Failed to list topics: {e}")
            raise

    def describe_topic(self, name: str) -> Dict[str, Any]:
        """
        Describe a topic's configuration and partitions.

        Args:
            name: Topic name

        Returns:
            Dictionary with topic details
        """
        try:
            # Get topic metadata
            metadata = self.admin_client.describe_topics([name])

            if not metadata or name not in metadata:
                raise Exception(f"Topic '{name}' not found")

            topic_metadata = metadata[name]

            # Get topic configurations
            config_resource = ConfigResource(
                ConfigResourceType.TOPIC,
                name,
            )

            configs = self.admin_client.describe_configs([config_resource])

            topic_config = {}
            if configs and config_resource in configs:
                for config_entry in configs[config_resource]:
                    topic_config[config_entry.name] = config_entry.value

            return {
                "name": name,
                "partitions": len(topic_metadata.get("partitions", [])),
                "replication_factor": (
                    len(topic_metadata["partitions"][0]["replicas"])
                    if topic_metadata.get("partitions")
                    else 0
                ),
                "config": topic_config,
            }

        except Exception as e:
            logger.error(f"Failed to describe topic '{name}': {e}")
            raise

    def alter_topic_config(
        self,
        name: str,
        config: Dict[str, str]
    ) -> bool:
        """
        Alter topic configuration.

        Args:
            name: Topic name
            config: Configuration key-value pairs

        Returns:
            True if successful
        """
        try:
            config_resource = ConfigResource(
                ConfigResourceType.TOPIC,
                name,
                configs=config,
            )

            self.admin_client.alter_configs([config_resource])

            logger.info(f"Altered config for topic '{name}': {config}")
            return True

        except Exception as e:
            logger.error(f"Failed to alter config for topic '{name}': {e}")
            raise

    def topic_exists(self, name: str) -> bool:
        """
        Check if a topic exists.

        Args:
            name: Topic name

        Returns:
            True if topic exists
        """
        try:
            topics = self.list_topics()
            return name in topics
        except Exception:
            return False

    def close(self) -> None:
        """Close AdminClient connection."""
        logger.info("Closing TopicManager")
        self.admin_client.close()

    def __enter__(self):
        """Context manager entry."""
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        """Context manager exit."""
        self.close()
