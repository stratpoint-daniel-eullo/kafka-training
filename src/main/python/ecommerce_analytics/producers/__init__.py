"""Producer module for E-Commerce Analytics Platform."""

from ecommerce_analytics.producers.base_producer import BaseProducer
from ecommerce_analytics.producers.user_event_producer import UserEventProducer
from ecommerce_analytics.producers.order_event_producer import OrderEventProducer
from ecommerce_analytics.producers.product_event_producer import ProductEventProducer
from ecommerce_analytics.producers.payment_event_producer import PaymentEventProducer

__all__ = [
    "BaseProducer",
    "UserEventProducer",
    "OrderEventProducer",
    "ProductEventProducer",
    "PaymentEventProducer",
]
