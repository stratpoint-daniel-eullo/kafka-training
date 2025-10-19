"""Stream processing module using Faust (Day 6: Kafka Streams alternative)."""

from ecommerce_analytics.streams.sales_aggregator import create_sales_aggregator_app
from ecommerce_analytics.streams.revenue_calculator import create_revenue_calculator_app
from ecommerce_analytics.streams.user_behavior_analyzer import create_user_behavior_app

__all__ = [
    "create_sales_aggregator_app",
    "create_revenue_calculator_app",
    "create_user_behavior_app",
]
