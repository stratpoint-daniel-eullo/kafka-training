"""Consumer module for E-Commerce Analytics Platform."""

from ecommerce_analytics.consumers.base_consumer import BaseConsumer
from ecommerce_analytics.consumers.analytics_consumer import AnalyticsConsumer
from ecommerce_analytics.consumers.inventory_consumer import InventoryConsumer
from ecommerce_analytics.consumers.fraud_detection_consumer import FraudDetectionConsumer

__all__ = [
    "BaseConsumer",
    "AnalyticsConsumer",
    "InventoryConsumer",
    "FraudDetectionConsumer",
]
