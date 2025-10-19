"""
Revenue Calculator Stream Processor

Faust stream processing app for real-time revenue calculations (Day 6).
Demonstrates stream-table joins and stateful transformations.
"""

import faust
import logging
from typing import Dict, Any
from datetime import timedelta

logger = logging.getLogger(__name__)


# Define Faust Record models
class OrderEvent(faust.Record, serializer='json'):
    """Order event record."""
    event_id: str
    event_type: str
    order_id: str
    user_id: str
    timestamp: int
    total_amount: float
    currency: str
    order_status: str


class PaymentEvent(faust.Record, serializer='json'):
    """Payment event record."""
    event_id: str
    event_type: str
    payment_id: str
    order_id: str
    user_id: str
    timestamp: int
    amount: float
    payment_status: str


class RevenueMetric(faust.Record, serializer='json'):
    """Revenue metric record."""
    timestamp: int
    user_id: str
    total_orders_value: float
    total_payments_collected: float
    pending_payments: float
    refunded_amount: float
    currency: str


def create_revenue_calculator_app(
    broker: str = "kafka://localhost:9092",
    store_uri: str = "rocksdb://",
) -> faust.App:
    """
    Create Faust app for revenue calculations.

    Args:
        broker: Kafka broker URL
        store_uri: State store URI

    Returns:
        Faust application instance

    Stream Processing Flow:
    1. Consume order-events and payment-events topics
    2. Maintain stateful tables per user
    3. Calculate revenue metrics (orders, payments, refunds)
    4. Join order and payment streams
    5. Produce to revenue-metrics topic
    """
    app = faust.App(
        id="revenue-calculator",
        broker=broker,
        store=store_uri,
        processing_guarantee="exactly_once",
        consumer_auto_offset_reset="earliest",
        producer_acks="all",
        producer_compression_type="snappy",
    )

    # Input topics
    order_events_topic = app.topic(
        "order-events",
        value_type=OrderEvent,
    )

    payment_events_topic = app.topic(
        "payment-events",
        value_type=PaymentEvent,
    )

    # Output topic
    revenue_metrics_topic = app.topic(
        "revenue-metrics",
        value_type=RevenueMetric,
    )

    # Stateful tables (Day 6: State stores)
    user_revenue_table = app.Table(
        "user_revenue",
        default=dict,
        partitions=3,
    )

    @app.agent(order_events_topic)
    async def process_orders(orders):
        """
        Process order events and update revenue metrics.

        Tracks:
        - Total orders value per user
        - Pending payments
        """
        async for order in orders:
            if order.event_type not in ["ORDER_PLACED", "ORDER_CANCELLED"]:
                continue

            user_id = order.user_id

            # Get or initialize user revenue state
            revenue = user_revenue_table.get(user_id, {
                "total_orders_value": 0.0,
                "total_payments_collected": 0.0,
                "pending_payments": 0.0,
                "refunded_amount": 0.0,
                "currency": order.currency,
            })

            # Update metrics based on event type
            if order.event_type == "ORDER_PLACED":
                revenue["total_orders_value"] += order.total_amount
                revenue["pending_payments"] += order.total_amount

                logger.info(
                    f"User {user_id} placed order {order.order_id}: "
                    f"${order.total_amount} (total orders: ${revenue['total_orders_value']:.2f})"
                )

            elif order.event_type == "ORDER_CANCELLED":
                # Reduce pending payments for cancelled orders
                revenue["pending_payments"] = max(0, revenue["pending_payments"] - order.total_amount)

                logger.info(f"User {user_id} cancelled order {order.order_id}")

            # Update table
            user_revenue_table[user_id] = revenue

    @app.agent(payment_events_topic)
    async def process_payments(payments):
        """
        Process payment events and update revenue metrics.

        Tracks:
        - Payments collected
        - Refunds issued
        - Pending payments
        """
        async for payment in payments:
            user_id = payment.user_id

            # Get or initialize user revenue state
            revenue = user_revenue_table.get(user_id, {
                "total_orders_value": 0.0,
                "total_payments_collected": 0.0,
                "pending_payments": 0.0,
                "refunded_amount": 0.0,
                "currency": "USD",
            })

            # Update metrics based on event type
            if payment.event_type == "PAYMENT_COMPLETED":
                revenue["total_payments_collected"] += payment.amount
                revenue["pending_payments"] = max(0, revenue["pending_payments"] - payment.amount)

                logger.info(
                    f"User {user_id} payment completed: ${payment.amount} "
                    f"(total collected: ${revenue['total_payments_collected']:.2f})"
                )

            elif payment.event_type == "PAYMENT_REFUNDED":
                revenue["refunded_amount"] += payment.amount
                revenue["total_payments_collected"] = max(
                    0,
                    revenue["total_payments_collected"] - payment.amount
                )

                logger.info(
                    f"User {user_id} refund issued: ${payment.amount} "
                    f"(total refunded: ${revenue['refunded_amount']:.2f})"
                )

            # Update table
            user_revenue_table[user_id] = revenue

            # Publish revenue metric
            await publish_revenue_metric(user_id, revenue, payment.timestamp)

    async def publish_revenue_metric(user_id: str, revenue: dict, timestamp: int):
        """
        Publish revenue metric to output topic.

        Args:
            user_id: User identifier
            revenue: Revenue data
            timestamp: Event timestamp
        """
        metric = RevenueMetric(
            timestamp=timestamp,
            user_id=user_id,
            total_orders_value=revenue["total_orders_value"],
            total_payments_collected=revenue["total_payments_collected"],
            pending_payments=revenue["pending_payments"],
            refunded_amount=revenue["refunded_amount"],
            currency=revenue["currency"],
        )

        await revenue_metrics_topic.send(value=metric)

        logger.debug(
            f"Published revenue metric for user {user_id}: "
            f"Orders: ${revenue['total_orders_value']:.2f}, "
            f"Collected: ${revenue['total_payments_collected']:.2f}, "
            f"Pending: ${revenue['pending_payments']:.2f}"
        )

    @app.page("/revenue/user/{user_id}")
    async def get_user_revenue(web, request, user_id):
        """
        Web endpoint to query user revenue metrics.

        Access: http://localhost:6067/revenue/user/{user_id}
        """
        revenue = user_revenue_table.get(user_id)

        if revenue:
            return web.json({
                "user_id": user_id,
                "total_orders_value": revenue["total_orders_value"],
                "total_payments_collected": revenue["total_payments_collected"],
                "pending_payments": revenue["pending_payments"],
                "refunded_amount": revenue["refunded_amount"],
                "currency": revenue["currency"],
            })
        else:
            return web.json({"error": f"No revenue data for user {user_id}"})

    @app.page("/revenue/summary")
    async def get_revenue_summary(web, request):
        """
        Web endpoint to get overall revenue summary.

        Access: http://localhost:6067/revenue/summary
        """
        total_orders = 0.0
        total_collected = 0.0
        total_pending = 0.0
        total_refunded = 0.0

        # Aggregate across all users
        for user_id, revenue in user_revenue_table.items():
            total_orders += revenue.get("total_orders_value", 0.0)
            total_collected += revenue.get("total_payments_collected", 0.0)
            total_pending += revenue.get("pending_payments", 0.0)
            total_refunded += revenue.get("refunded_amount", 0.0)

        return web.json({
            "total_orders_value": total_orders,
            "total_payments_collected": total_collected,
            "pending_payments": total_pending,
            "total_refunded": total_refunded,
            "net_revenue": total_collected - total_refunded,
            "total_users": len(user_revenue_table),
        })

    logger.info("Revenue Calculator Faust app created")

    return app


# For running standalone
if __name__ == "__main__":
    app = create_revenue_calculator_app()
    app.main()
