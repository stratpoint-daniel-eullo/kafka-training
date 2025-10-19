"""
Analytics Consumer

Consumes order and user events to generate real-time analytics.
Demonstrates Day 4 (manual commits) and business logic integration.
"""

import logging
from typing import Dict, Any
from collections import defaultdict

from ecommerce_analytics.consumers.base_consumer import BaseConsumer
from ecommerce_analytics.config.settings import get_settings

logger = logging.getLogger(__name__)


class AnalyticsConsumer(BaseConsumer):
    """
    Consumer for real-time analytics on orders and users.

    Tracks:
    - Total orders placed
    - Total revenue
    - Orders by status
    - Active users
    - User registrations

    This consumer demonstrates aggregating data from multiple event types
    for business intelligence purposes.
    """

    def __init__(self):
        """Initialize AnalyticsConsumer."""
        # Subscribe to both order and user events
        topics = ["order-events", "user-events"]

        super().__init__(
            topics=topics,
            group_id="analytics-consumer-group"
        )

        # In-memory analytics state (would be external DB in production)
        self.analytics = {
            "total_orders": 0,
            "total_revenue": 0.0,
            "orders_by_status": defaultdict(int),
            "active_users": set(),
            "total_user_registrations": 0,
        }

        logger.info("AnalyticsConsumer initialized")

    def start(self) -> None:
        """
        Start consuming and processing analytics events.
        """
        logger.info("Starting analytics processing...")

        # Start consuming with process function
        self.consume(
            process_func=self._process_event,
            commit_interval=50  # Commit every 50 messages
        )

    def _process_event(self, event: Dict[str, Any]) -> None:
        """
        Process incoming event and update analytics.

        Args:
            event: Deserialized event dictionary
        """
        event_type = event.get("event_type")

        if event_type == "ORDER_PLACED":
            self._process_order_placed(event)
        elif event_type == "ORDER_CONFIRMED":
            self._process_order_confirmed(event)
        elif event_type == "ORDER_CANCELLED":
            self._process_order_cancelled(event)
        elif event_type == "USER_REGISTERED":
            self._process_user_registered(event)
        elif event_type == "USER_LOGIN":
            self._process_user_login(event)
        else:
            logger.debug(f"Ignoring event type: {event_type}")

    def _process_order_placed(self, event: Dict[str, Any]) -> None:
        """
        Process ORDER_PLACED event.

        Args:
            event: Order event
        """
        self.analytics["total_orders"] += 1
        self.analytics["orders_by_status"]["PLACED"] += 1

        # Extract revenue (Avro decimal comes as bytes)
        total_amount = event.get("total_amount", 0)
        if isinstance(total_amount, bytes):
            # Convert bytes to float (Avro decimal)
            # In production, use proper decimal handling
            total_amount = float(int.from_bytes(total_amount, byteorder='big')) / 100

        self.analytics["total_revenue"] += total_amount

        logger.info(
            f"Processed ORDER_PLACED: {event.get('order_id')} - "
            f"Revenue: ${total_amount:.2f}"
        )

        # Log analytics every 10 orders
        if self.analytics["total_orders"] % 10 == 0:
            self._log_analytics()

    def _process_order_confirmed(self, event: Dict[str, Any]) -> None:
        """
        Process ORDER_CONFIRMED event.

        Args:
            event: Order event
        """
        self.analytics["orders_by_status"]["CONFIRMED"] += 1

        logger.debug(f"Processed ORDER_CONFIRMED: {event.get('order_id')}")

    def _process_order_cancelled(self, event: Dict[str, Any]) -> None:
        """
        Process ORDER_CANCELLED event.

        Args:
            event: Order event
        """
        self.analytics["orders_by_status"]["CANCELLED"] += 1

        logger.info(f"Processed ORDER_CANCELLED: {event.get('order_id')}")

    def _process_user_registered(self, event: Dict[str, Any]) -> None:
        """
        Process USER_REGISTERED event.

        Args:
            event: User event
        """
        self.analytics["total_user_registrations"] += 1

        user_id = event.get("user_id")
        self.analytics["active_users"].add(user_id)

        logger.info(
            f"Processed USER_REGISTERED: {user_id} - "
            f"Total users: {len(self.analytics['active_users'])}"
        )

    def _process_user_login(self, event: Dict[str, Any]) -> None:
        """
        Process USER_LOGIN event.

        Args:
            event: User event
        """
        user_id = event.get("user_id")
        self.analytics["active_users"].add(user_id)

        logger.debug(f"Processed USER_LOGIN: {user_id}")

    def _log_analytics(self) -> None:
        """
        Log current analytics state.
        """
        logger.info("=" * 60)
        logger.info("ANALYTICS SUMMARY")
        logger.info("=" * 60)
        logger.info(f"Total Orders: {self.analytics['total_orders']}")
        logger.info(f"Total Revenue: ${self.analytics['total_revenue']:.2f}")
        logger.info(f"Active Users: {len(self.analytics['active_users'])}")
        logger.info(f"User Registrations: {self.analytics['total_user_registrations']}")
        logger.info("Orders by Status:")
        for status, count in self.analytics["orders_by_status"].items():
            logger.info(f"  {status}: {count}")
        logger.info("=" * 60)

    def get_analytics(self) -> Dict[str, Any]:
        """
        Get current analytics snapshot.

        Returns:
            Dictionary with analytics data
        """
        return {
            "total_orders": self.analytics["total_orders"],
            "total_revenue": self.analytics["total_revenue"],
            "orders_by_status": dict(self.analytics["orders_by_status"]),
            "active_users_count": len(self.analytics["active_users"]),
            "total_user_registrations": self.analytics["total_user_registrations"],
        }

    def close(self) -> None:
        """
        Close consumer and log final analytics.
        """
        logger.info("Closing AnalyticsConsumer...")
        self._log_analytics()
        super().close()
