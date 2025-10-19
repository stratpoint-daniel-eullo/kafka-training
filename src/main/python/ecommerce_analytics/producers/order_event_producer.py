"""
Order Event Producer

Produces order lifecycle events to Kafka.
Demonstrates Day 3 (idempotent producer) and Day 5 (Avro with complex types).
"""

import uuid
import time
import logging
from typing import Dict, Any, List, Optional
from decimal import Decimal

from ecommerce_analytics.producers.base_producer import BaseProducer
from ecommerce_analytics.config.settings import get_settings

logger = logging.getLogger(__name__)


class OrderEventProducer(BaseProducer):
    """
    Producer for order lifecycle events.

    Event Types:
    - ORDER_PLACED: New order created
    - ORDER_CONFIRMED: Order confirmed by system
    - ORDER_SHIPPED: Order shipped
    - ORDER_DELIVERED: Order delivered
    - ORDER_CANCELLED: Order cancelled
    - ORDER_RETURNED: Order returned
    """

    def __init__(self):
        """Initialize OrderEventProducer with order-events topic."""
        settings = get_settings()
        schema_path = settings.get_schema_path("order_events.avsc")

        super().__init__(
            topic="order-events",
            schema_path=schema_path,
            to_dict=self._order_event_to_dict
        )

    @staticmethod
    def _order_event_to_dict(event: Dict[str, Any], ctx) -> Dict[str, Any]:
        """
        Convert order event to dict for Avro serialization.

        Args:
            event: Order event dictionary
            ctx: Serialization context

        Returns:
            Dictionary ready for Avro serialization
        """
        # Convert Decimal to bytes for Avro decimal logical type
        if "total_amount" in event and isinstance(event["total_amount"], Decimal):
            # Avro decimal serializer handles Decimal objects
            pass

        # Convert item prices
        if "items" in event:
            for item in event["items"]:
                if isinstance(item.get("unit_price"), Decimal):
                    # Avro handles Decimal objects
                    pass

        return event

    def produce_order_placed(
        self,
        order_id: str,
        user_id: str,
        items: List[Dict[str, Any]],
        total_amount: Decimal,
        currency: str = "USD",
        shipping_address: Optional[Dict[str, str]] = None,
        payment_method: Optional[str] = None
    ) -> None:
        """
        Produce ORDER_PLACED event.

        Args:
            order_id: Unique order identifier
            user_id: User who placed the order
            items: List of order items
            total_amount: Total order amount
            currency: Currency code (ISO 4217)
            shipping_address: Shipping address dict
            payment_method: Payment method used
        """
        event = {
            "event_id": str(uuid.uuid4()),
            "event_type": "ORDER_PLACED",
            "order_id": order_id,
            "user_id": user_id,
            "timestamp": int(time.time() * 1000),
            "total_amount": total_amount,
            "currency": currency,
            "items": items,
            "shipping_address": shipping_address,
            "payment_method": payment_method,
            "order_status": "PENDING",
            "source_system": "ecommerce-platform",
            "schema_version": "1.0.0"
        }

        # Use order_id as key for ordering guarantees
        self.produce(key=order_id, value=event)

        logger.info(f"Produced ORDER_PLACED event for order {order_id}")

    def produce_order_confirmed(self, order_id: str, user_id: str) -> None:
        """
        Produce ORDER_CONFIRMED event.

        Args:
            order_id: Unique order identifier
            user_id: User who owns the order
        """
        event = {
            "event_id": str(uuid.uuid4()),
            "event_type": "ORDER_CONFIRMED",
            "order_id": order_id,
            "user_id": user_id,
            "timestamp": int(time.time() * 1000),
            "total_amount": Decimal("0.00"),  # Not relevant for confirmation
            "currency": "USD",
            "items": [],
            "shipping_address": None,
            "payment_method": None,
            "order_status": "CONFIRMED",
            "source_system": "ecommerce-platform",
            "schema_version": "1.0.0"
        }

        self.produce(key=order_id, value=event)

        logger.info(f"Produced ORDER_CONFIRMED event for order {order_id}")

    def produce_order_shipped(self, order_id: str, user_id: str) -> None:
        """
        Produce ORDER_SHIPPED event.

        Args:
            order_id: Unique order identifier
            user_id: User who owns the order
        """
        event = {
            "event_id": str(uuid.uuid4()),
            "event_type": "ORDER_SHIPPED",
            "order_id": order_id,
            "user_id": user_id,
            "timestamp": int(time.time() * 1000),
            "total_amount": Decimal("0.00"),
            "currency": "USD",
            "items": [],
            "shipping_address": None,
            "payment_method": None,
            "order_status": "SHIPPED",
            "source_system": "ecommerce-platform",
            "schema_version": "1.0.0"
        }

        self.produce(key=order_id, value=event)

        logger.info(f"Produced ORDER_SHIPPED event for order {order_id}")

    def produce_order_delivered(self, order_id: str, user_id: str) -> None:
        """
        Produce ORDER_DELIVERED event.

        Args:
            order_id: Unique order identifier
            user_id: User who owns the order
        """
        event = {
            "event_id": str(uuid.uuid4()),
            "event_type": "ORDER_DELIVERED",
            "order_id": order_id,
            "user_id": user_id,
            "timestamp": int(time.time() * 1000),
            "total_amount": Decimal("0.00"),
            "currency": "USD",
            "items": [],
            "shipping_address": None,
            "payment_method": None,
            "order_status": "DELIVERED",
            "source_system": "ecommerce-platform",
            "schema_version": "1.0.0"
        }

        self.produce(key=order_id, value=event)

        logger.info(f"Produced ORDER_DELIVERED event for order {order_id}")

    def produce_order_cancelled(self, order_id: str, user_id: str) -> None:
        """
        Produce ORDER_CANCELLED event.

        Args:
            order_id: Unique order identifier
            user_id: User who owns the order
        """
        event = {
            "event_id": str(uuid.uuid4()),
            "event_type": "ORDER_CANCELLED",
            "order_id": order_id,
            "user_id": user_id,
            "timestamp": int(time.time() * 1000),
            "total_amount": Decimal("0.00"),
            "currency": "USD",
            "items": [],
            "shipping_address": None,
            "payment_method": None,
            "order_status": "CANCELLED",
            "source_system": "ecommerce-platform",
            "schema_version": "1.0.0"
        }

        self.produce(key=order_id, value=event)

        logger.info(f"Produced ORDER_CANCELLED event for order {order_id}")

    def produce_order_returned(self, order_id: str, user_id: str) -> None:
        """
        Produce ORDER_RETURNED event.

        Args:
            order_id: Unique order identifier
            user_id: User who owns the order
        """
        event = {
            "event_id": str(uuid.uuid4()),
            "event_type": "ORDER_RETURNED",
            "order_id": order_id,
            "user_id": user_id,
            "timestamp": int(time.time() * 1000),
            "total_amount": Decimal("0.00"),
            "currency": "USD",
            "items": [],
            "shipping_address": None,
            "payment_method": None,
            "order_status": "RETURNED",
            "source_system": "ecommerce-platform",
            "schema_version": "1.0.0"
        }

        self.produce(key=order_id, value=event)

        logger.info(f"Produced ORDER_RETURNED event for order {order_id}")
