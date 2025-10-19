"""Configuration module for E-Commerce Analytics Platform."""

from ecommerce_analytics.config.kafka_config import KafkaConfig
from ecommerce_analytics.config.settings import Settings

__all__ = ["KafkaConfig", "Settings"]
