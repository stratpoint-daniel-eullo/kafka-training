"""
Sales Aggregator Stream Processor

Faust stream processing app for real-time sales aggregation (Day 6).
Demonstrates windowing, stateful aggregations, and stream-table joins.
"""

import faust
import logging
from typing import Dict, Any
from datetime import timedelta

logger = logging.getLogger(__name__)


# Define Faust Record models (similar to Avro schemas)
class OrderEvent(faust.Record, serializer='json'):
    """Order event record."""
    event_id: str
    event_type: str
    order_id: str
    user_id: str
    timestamp: int
    total_amount: float  # Simplified from Decimal for demo
    currency: str
    items: list
    order_status: str


class SalesAggregate(faust.Record, serializer='json'):
    """Sales aggregation result."""
    window_start: int
    window_end: int
    total_orders: int
    total_revenue: float
    average_order_value: float
    currency: str


def create_sales_aggregator_app(
    broker: str = "kafka://localhost:9092",
    store_uri: str = "rocksdb://",
) -> faust.App:
    """
    Create Faust app for sales aggregation.

    Args:
        broker: Kafka broker URL
        store_uri: State store URI (rocksdb or memory)

    Returns:
        Faust application instance

    Stream Processing Flow:
    1. Consume order-events topic
    2. Filter for ORDER_PLACED events
    3. Apply tumbling window (5 minutes)
    4. Aggregate: count orders, sum revenue
    5. Produce to sales-analytics topic
    """
    app = faust.App(
        id="sales-aggregator",
        broker=broker,
        store=store_uri,
        # Day 6: Processing guarantees
        processing_guarantee="exactly_once",
        # Consumer configuration
        consumer_auto_offset_reset="earliest",
        # Producer configuration
        producer_acks="all",
        producer_compression_type="snappy",
    )

    # Input topic (order events)
    order_events_topic = app.topic(
        "order-events",
        value_type=OrderEvent,
    )

    # Output topic (sales analytics)
    sales_analytics_topic = app.topic(
        "sales-analytics",
        value_type=SalesAggregate,
    )

    # Tumbling window table (Day 6: Windowing)
    # Window: 5 minutes, hopping every 5 minutes (non-overlapping)
    sales_table = app.Table(
        "sales_aggregation",
        default=dict,
    ).tumbling(
        size=timedelta(minutes=5),
        expires=timedelta(hours=1),
    )

    @app.agent(order_events_topic)
    async def aggregate_sales(orders):
        """
        Process order events and aggregate sales metrics.

        Day 6 Concepts:
        - Stateful stream processing
        - Tumbling windows
        - Aggregations (count, sum, average)
        """
        async for order in orders:
            # Filter for ORDER_PLACED events only
            if order.event_type != "ORDER_PLACED":
                continue

            logger.info(
                f"Processing order {order.order_id}: ${order.total_amount}"
            )

            # Get current window or create new aggregate
            current_window = sales_table["global"].current()

            if current_window is None:
                current_window = {
                    "total_orders": 0,
                    "total_revenue": 0.0,
                    "currency": order.currency,
                }

            # Update aggregates
            current_window["total_orders"] += 1
            current_window["total_revenue"] += order.total_amount

            # Update table
            sales_table["global"] = current_window

            logger.debug(
                f"Window aggregate: {current_window['total_orders']} orders, "
                f"${current_window['total_revenue']:.2f} revenue"
            )

    @app.timer(interval=60.0)  # Run every minute
    async def publish_sales_analytics():
        """
        Periodically publish aggregated sales analytics.

        This timer publishes completed window results to the output topic.
        """
        async for window, aggregate in sales_table["global"].items():
            if aggregate:
                # Calculate average order value
                avg_order_value = (
                    aggregate["total_revenue"] / aggregate["total_orders"]
                    if aggregate["total_orders"] > 0
                    else 0.0
                )

                # Create sales aggregate record
                sales_agg = SalesAggregate(
                    window_start=int(window.start.timestamp() * 1000),
                    window_end=int(window.end.timestamp() * 1000),
                    total_orders=aggregate["total_orders"],
                    total_revenue=aggregate["total_revenue"],
                    average_order_value=avg_order_value,
                    currency=aggregate["currency"],
                )

                # Publish to output topic
                await sales_analytics_topic.send(value=sales_agg)

                logger.info(
                    f"Published sales analytics - "
                    f"Window: {window.start} to {window.end}, "
                    f"Orders: {aggregate['total_orders']}, "
                    f"Revenue: ${aggregate['total_revenue']:.2f}, "
                    f"AOV: ${avg_order_value:.2f}"
                )

    @app.page("/sales/current")
    @app.table_route(table=sales_table, match_info="key")
    async def get_sales(web, request, key):
        """
        Web endpoint to query current sales aggregates.

        Access: http://localhost:6066/sales/current/global
        """
        current = sales_table["global"].current()

        if current:
            return web.json({
                "total_orders": current["total_orders"],
                "total_revenue": current["total_revenue"],
                "average_order_value": (
                    current["total_revenue"] / current["total_orders"]
                    if current["total_orders"] > 0
                    else 0.0
                ),
            })
        else:
            return web.json({"error": "No data available"})

    logger.info("Sales Aggregator Faust app created")

    return app


# For running standalone
if __name__ == "__main__":
    app = create_sales_aggregator_app()
    app.main()
