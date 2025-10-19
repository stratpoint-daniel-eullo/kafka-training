"""
Inventory Consumer

Tracks product inventory changes in real-time.
Demonstrates Day 4 (manual commits) and stateful processing.
"""

import logging
from typing import Dict, Any

from ecommerce_analytics.consumers.base_consumer import BaseConsumer
from ecommerce_analytics.config.settings import get_settings

logger = logging.getLogger(__name__)


class InventoryConsumer(BaseConsumer):
    """
    Consumer for tracking product inventory changes.

    Tracks:
    - Current stock levels for each product
    - Low stock alerts (< 10 items)
    - Out of stock alerts
    - Inventory change history

    This consumer demonstrates maintaining state based on event stream.
    In production, this would update an external inventory database.
    """

    def __init__(self, low_stock_threshold: int = 10):
        """
        Initialize InventoryConsumer.

        Args:
            low_stock_threshold: Threshold for low stock alerts
        """
        super().__init__(
            topics=["product-events"],
            group_id="inventory-consumer-group"
        )

        self.low_stock_threshold = low_stock_threshold

        # In-memory inventory state (would be external DB in production)
        self.inventory = {}  # product_id -> stock_quantity
        self.product_info = {}  # product_id -> {name, category, price}

        # Metrics
        self.low_stock_alerts = 0
        self.out_of_stock_alerts = 0

        logger.info(
            f"InventoryConsumer initialized (low stock threshold: {low_stock_threshold})"
        )

    def start(self) -> None:
        """
        Start consuming and processing inventory events.
        """
        logger.info("Starting inventory tracking...")

        # Start consuming with process function
        self.consume(
            process_func=self._process_event,
            commit_interval=20  # Commit every 20 messages
        )

    def _process_event(self, event: Dict[str, Any]) -> None:
        """
        Process incoming product event and update inventory.

        Args:
            event: Deserialized product event
        """
        event_type = event.get("event_type")

        if event_type == "PRODUCT_CREATED":
            self._process_product_created(event)
        elif event_type == "INVENTORY_INCREASED":
            self._process_inventory_increased(event)
        elif event_type == "INVENTORY_DECREASED":
            self._process_inventory_decreased(event)
        elif event_type == "PRODUCT_DELETED":
            self._process_product_deleted(event)
        else:
            logger.debug(f"Ignoring event type: {event_type}")

    def _process_product_created(self, event: Dict[str, Any]) -> None:
        """
        Process PRODUCT_CREATED event.

        Args:
            event: Product event
        """
        product_id = event.get("product_id")
        stock_quantity = event.get("stock_quantity", 0)

        # Initialize inventory
        self.inventory[product_id] = stock_quantity

        # Store product info
        self.product_info[product_id] = {
            "name": event.get("product_name"),
            "category": event.get("category"),
            "price": event.get("price"),
        }

        logger.info(
            f"New product added: {product_id} - "
            f"{event.get('product_name')} (stock: {stock_quantity})"
        )

        # Check for low stock on creation
        self._check_stock_levels(product_id, stock_quantity)

    def _process_inventory_increased(self, event: Dict[str, Any]) -> None:
        """
        Process INVENTORY_INCREASED event.

        Args:
            event: Product event
        """
        product_id = event.get("product_id")
        new_stock = event.get("stock_quantity")
        change = event.get("quantity_change", 0)

        old_stock = self.inventory.get(product_id, 0)
        self.inventory[product_id] = new_stock

        logger.info(
            f"Inventory increased: {product_id} - "
            f"{old_stock} -> {new_stock} (+{change})"
        )

        # Check if we're back in stock
        if old_stock == 0 and new_stock > 0:
            logger.info(f"BACK IN STOCK: {product_id}")

    def _process_inventory_decreased(self, event: Dict[str, Any]) -> None:
        """
        Process INVENTORY_DECREASED event.

        Args:
            event: Product event
        """
        product_id = event.get("product_id")
        new_stock = event.get("stock_quantity")
        change = event.get("quantity_change", 0)

        old_stock = self.inventory.get(product_id, 0)
        self.inventory[product_id] = new_stock

        logger.info(
            f"Inventory decreased: {product_id} - "
            f"{old_stock} -> {new_stock} ({change})"
        )

        # Check stock levels and alert if needed
        self._check_stock_levels(product_id, new_stock)

    def _process_product_deleted(self, event: Dict[str, Any]) -> None:
        """
        Process PRODUCT_DELETED event.

        Args:
            event: Product event
        """
        product_id = event.get("product_id")

        # Remove from inventory tracking
        if product_id in self.inventory:
            del self.inventory[product_id]

        if product_id in self.product_info:
            del self.product_info[product_id]

        logger.info(f"Product deleted: {product_id}")

    def _check_stock_levels(self, product_id: str, stock_quantity: int) -> None:
        """
        Check stock levels and generate alerts.

        Args:
            product_id: Product identifier
            stock_quantity: Current stock quantity
        """
        if stock_quantity == 0:
            self.out_of_stock_alerts += 1
            logger.warning(f"OUT OF STOCK ALERT: {product_id}")

        elif stock_quantity <= self.low_stock_threshold:
            self.low_stock_alerts += 1
            logger.warning(
                f"LOW STOCK ALERT: {product_id} - "
                f"Only {stock_quantity} items remaining"
            )

    def get_inventory_status(self) -> Dict[str, Any]:
        """
        Get current inventory status.

        Returns:
            Dictionary with inventory data
        """
        total_products = len(self.inventory)
        out_of_stock = sum(1 for qty in self.inventory.values() if qty == 0)
        low_stock = sum(
            1 for qty in self.inventory.values()
            if 0 < qty <= self.low_stock_threshold
        )
        total_items = sum(self.inventory.values())

        return {
            "total_products": total_products,
            "total_items_in_stock": total_items,
            "out_of_stock_count": out_of_stock,
            "low_stock_count": low_stock,
            "low_stock_alerts_triggered": self.low_stock_alerts,
            "out_of_stock_alerts_triggered": self.out_of_stock_alerts,
        }

    def get_product_stock(self, product_id: str) -> int:
        """
        Get stock level for a specific product.

        Args:
            product_id: Product identifier

        Returns:
            Stock quantity (0 if not found)
        """
        return self.inventory.get(product_id, 0)

    def close(self) -> None:
        """
        Close consumer and log final inventory status.
        """
        logger.info("Closing InventoryConsumer...")

        status = self.get_inventory_status()
        logger.info("=" * 60)
        logger.info("INVENTORY STATUS")
        logger.info("=" * 60)
        logger.info(f"Total Products: {status['total_products']}")
        logger.info(f"Total Items in Stock: {status['total_items_in_stock']}")
        logger.info(f"Out of Stock: {status['out_of_stock_count']}")
        logger.info(f"Low Stock: {status['low_stock_count']}")
        logger.info(f"Low Stock Alerts: {status['low_stock_alerts_triggered']}")
        logger.info(f"Out of Stock Alerts: {status['out_of_stock_alerts_triggered']}")
        logger.info("=" * 60)

        super().close()
