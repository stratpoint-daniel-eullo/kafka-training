"""
Real-Time E-Commerce Analytics Platform - Python Data Engineering Capstone

A production-grade Kafka-based streaming analytics platform demonstrating
all concepts from Days 1-8 of Kafka training.

Author: Kafka Training Team
License: MIT
"""

__version__ = "1.0.0"
__author__ = "Kafka Training Team"

# Package-level imports for convenience
from ecommerce_analytics.config.settings import Settings
from ecommerce_analytics.config.kafka_config import KafkaConfig

__all__ = ["Settings", "KafkaConfig", "__version__"]
