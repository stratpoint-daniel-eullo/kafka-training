"""
Consumer Lag Monitor

Monitors consumer lag and alerts on threshold breaches (Day 8).
Demonstrates operational monitoring for Kafka consumers.
"""

import logging
import time
from typing import Dict, List
from kafka import KafkaConsumer, TopicPartition
from kafka.structs import OffsetAndMetadata

from ecommerce_analytics.config.settings import get_settings
from ecommerce_analytics.monitoring.metrics import get_metrics

logger = logging.getLogger(__name__)


class LagMonitor:
    """
    Monitor consumer lag for consumer groups.

    Features (Day 8):
    - Real-time lag monitoring per partition
    - Threshold-based alerting
    - Prometheus metrics export
    - Lag trend analysis
    """

    def __init__(
        self,
        consumer_group: str,
        lag_threshold: int = 1000,
        check_interval: int = 60
    ):
        """
        Initialize LagMonitor.

        Args:
            consumer_group: Consumer group to monitor
            lag_threshold: Lag threshold for alerts (messages)
            check_interval: Check interval in seconds
        """
        self.consumer_group = consumer_group
        self.lag_threshold = lag_threshold
        self.check_interval = check_interval

        self.settings = get_settings()
        self.metrics = get_metrics()

        # Create consumer for offset management
        self.consumer = KafkaConsumer(
            bootstrap_servers=self.settings.kafka_brokers,
            group_id=f"{consumer_group}-lag-monitor",
            enable_auto_commit=False,
        )

        # Lag history for trend analysis
        self.lag_history: Dict[str, List[int]] = {}

        logger.info(
            f"LagMonitor initialized (group: {consumer_group}, "
            f"threshold: {lag_threshold})"
        )

    def check_lag(self, topics: List[str]) -> Dict[str, Dict[int, int]]:
        """
        Check consumer lag for given topics.

        Args:
            topics: List of topics to check

        Returns:
            Dictionary mapping topic -> partition -> lag
        """
        lag_info = {}

        for topic in topics:
            # Get topic partitions
            partitions = self.consumer.partitions_for_topic(topic)

            if not partitions:
                logger.warning(f"No partitions found for topic {topic}")
                continue

            topic_lag = {}

            for partition in partitions:
                try:
                    tp = TopicPartition(topic, partition)

                    # Get end offset (latest offset)
                    end_offsets = self.consumer.end_offsets([tp])
                    end_offset = end_offsets.get(tp, 0)

                    # Get committed offset for consumer group
                    committed = self.consumer.committed(tp)
                    committed_offset = committed.offset if committed else 0

                    # Calculate lag
                    lag = end_offset - committed_offset

                    topic_lag[partition] = lag

                    # Update Prometheus metrics
                    self.metrics.set_consumer_lag(
                        topic=topic,
                        partition=partition,
                        consumer_group=self.consumer_group,
                        lag=lag,
                    )

                    # Check threshold and alert
                    if lag > self.lag_threshold:
                        self._alert_high_lag(topic, partition, lag)

                    # Track lag history
                    history_key = f"{topic}-{partition}"
                    if history_key not in self.lag_history:
                        self.lag_history[history_key] = []

                    self.lag_history[history_key].append(lag)

                    # Keep last 100 data points
                    if len(self.lag_history[history_key]) > 100:
                        self.lag_history[history_key].pop(0)

                except Exception as e:
                    logger.error(
                        f"Failed to check lag for {topic}[{partition}]: {e}"
                    )

            lag_info[topic] = topic_lag

        return lag_info

    def monitor_continuously(self, topics: List[str]) -> None:
        """
        Continuously monitor consumer lag.

        Args:
            topics: List of topics to monitor
        """
        logger.info(
            f"Starting continuous lag monitoring for topics: {topics}"
        )

        try:
            while True:
                lag_info = self.check_lag(topics)

                # Log summary
                total_lag = sum(
                    sum(partitions.values())
                    for partitions in lag_info.values()
                )

                logger.info(
                    f"Consumer group '{self.consumer_group}' total lag: {total_lag}"
                )

                # Sleep until next check
                time.sleep(self.check_interval)

        except KeyboardInterrupt:
            logger.info("Lag monitoring stopped")

        finally:
            self.close()

    def _alert_high_lag(self, topic: str, partition: int, lag: int) -> None:
        """
        Alert on high consumer lag.

        Args:
            topic: Topic name
            partition: Partition number
            lag: Current lag
        """
        logger.warning(
            f"HIGH LAG ALERT: Consumer group '{self.consumer_group}' "
            f"for topic {topic}[{partition}] has lag of {lag} messages "
            f"(threshold: {self.lag_threshold})"
        )

        # In production: send alert via email, Slack, PagerDuty, etc.

    def get_lag_trend(self, topic: str, partition: int) -> List[int]:
        """
        Get lag trend for a topic partition.

        Args:
            topic: Topic name
            partition: Partition number

        Returns:
            List of lag values (most recent last)
        """
        history_key = f"{topic}-{partition}"
        return self.lag_history.get(history_key, [])

    def is_lag_increasing(
        self,
        topic: str,
        partition: int,
        window: int = 5
    ) -> bool:
        """
        Check if lag is increasing over time.

        Args:
            topic: Topic name
            partition: Partition number
            window: Number of data points to analyze

        Returns:
            True if lag is increasing
        """
        trend = self.get_lag_trend(topic, partition)

        if len(trend) < window:
            return False

        recent = trend[-window:]

        # Check if lag is generally increasing
        increases = sum(
            1 for i in range(len(recent) - 1)
            if recent[i + 1] > recent[i]
        )

        return increases > (window / 2)

    def close(self) -> None:
        """Close lag monitor and consumer."""
        logger.info("Closing LagMonitor")
        self.consumer.close()
