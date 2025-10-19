"""
Product Event Producer

Produces product and inventory events to Kafka.
Demonstrates Day 3 (idempotent producer) and Day 5 (Avro serialization).
"""

import uuid
import time
import logging
from typing import Dict, Any, Optional
from decimal import Decimal

from ecommerce_analytics.producers.base_producer import BaseProducer
from ecommerce_analytics.config.settings import get_settings

logger = logging.getLogger(__name__)


class ProductEventProducer(BaseProducer):
    """
    Producer for product and inventory events.

    Event Types:
    - PRODUCT_CREATED: New product added
    - PRODUCT_UPDATED: Product details updated
    - PRODUCT_DELETED: Product removed
    - INVENTORY_INCREASED: Stock increased
    - INVENTORY_DECREASED: Stock decreased
    - PRICE_CHANGED: Product price changed
    """

    def __init__(self):
        """Initialize ProductEventProducer with product-events topic."""
        settings = get_settings()
        schema_path = settings.get_schema_path("product_events.avsc")

        super().__init__(
            topic="product-events",
            schema_path=schema_path,
            to_dict=self._product_event_to_dict
        )

    @staticmethod
    def _product_event_to_dict(event: Dict[str, Any], ctx) -> Dict[str, Any]:
        """
        Convert product event to dict for Avro serialization.

        Args:
            event: Product event dictionary
            ctx: Serialization context

        Returns:
            Dictionary ready for Avro serialization
        """
        # Avro serializer handles Decimal objects for price fields
        return event

    def produce_product_created(
        self,
        product_id: str,
        product_name: str,
        category: str,
        price: Decimal,
        stock_quantity: int,
        currency: str = "USD",
        warehouse_location: Optional[str] = None,
        attributes: Optional[Dict[str, str]] = None,
        supplier_id: Optional[str] = None
    ) -> None:
        """
        Produce PRODUCT_CREATED event.

        Args:
            product_id: Unique product identifier
            product_name: Product name
            category: Product category
            price: Product price
            stock_quantity: Initial stock quantity
            currency: Currency code
            warehouse_location: Warehouse location code
            attributes: Product attributes (color, size, etc.)
            supplier_id: Supplier identifier
        """
        event = {
            "event_id": str(uuid.uuid4()),
            "event_type": "PRODUCT_CREATED",
            "product_id": product_id,
            "timestamp": int(time.time() * 1000),
            "product_name": product_name,
            "category": category,
            "price": price,
            "previous_price": None,
            "currency": currency,
            "stock_quantity": stock_quantity,
            "quantity_change": None,
            "warehouse_location": warehouse_location,
            "product_attributes": attributes,
            "supplier_id": supplier_id,
            "is_active": True,
            "source_system": "ecommerce-platform",
            "schema_version": "1.0.0"
        }

        # Use product_id as key for consistent partitioning
        self.produce(key=product_id, value=event)

        logger.info(f"Produced PRODUCT_CREATED event for product {product_id}")

    def produce_product_updated(
        self,
        product_id: str,
        product_name: Optional[str] = None,
        category: Optional[str] = None,
        attributes: Optional[Dict[str, str]] = None,
        is_active: Optional[bool] = None
    ) -> None:
        """
        Produce PRODUCT_UPDATED event.

        Args:
            product_id: Unique product identifier
            product_name: Updated product name
            category: Updated category
            attributes: Updated attributes
            is_active: Updated active status
        """
        event = {
            "event_id": str(uuid.uuid4()),
            "event_type": "PRODUCT_UPDATED",
            "product_id": product_id,
            "timestamp": int(time.time() * 1000),
            "product_name": product_name,
            "category": category,
            "price": None,
            "previous_price": None,
            "currency": "USD",
            "stock_quantity": None,
            "quantity_change": None,
            "warehouse_location": None,
            "product_attributes": attributes,
            "supplier_id": None,
            "is_active": is_active if is_active is not None else True,
            "source_system": "ecommerce-platform",
            "schema_version": "1.0.0"
        }

        self.produce(key=product_id, value=event)

        logger.info(f"Produced PRODUCT_UPDATED event for product {product_id}")

    def produce_product_deleted(self, product_id: str) -> None:
        """
        Produce PRODUCT_DELETED event.

        Args:
            product_id: Unique product identifier
        """
        event = {
            "event_id": str(uuid.uuid4()),
            "event_type": "PRODUCT_DELETED",
            "product_id": product_id,
            "timestamp": int(time.time() * 1000),
            "product_name": None,
            "category": None,
            "price": None,
            "previous_price": None,
            "currency": "USD",
            "stock_quantity": None,
            "quantity_change": None,
            "warehouse_location": None,
            "product_attributes": None,
            "supplier_id": None,
            "is_active": False,
            "source_system": "ecommerce-platform",
            "schema_version": "1.0.0"
        }

        self.produce(key=product_id, value=event)

        logger.info(f"Produced PRODUCT_DELETED event for product {product_id}")

    def produce_inventory_increased(
        self,
        product_id: str,
        quantity_change: int,
        new_stock_quantity: int,
        warehouse_location: Optional[str] = None
    ) -> None:
        """
        Produce INVENTORY_INCREASED event.

        Args:
            product_id: Unique product identifier
            quantity_change: Quantity added to stock
            new_stock_quantity: New total stock quantity
            warehouse_location: Warehouse location code
        """
        event = {
            "event_id": str(uuid.uuid4()),
            "event_type": "INVENTORY_INCREASED",
            "product_id": product_id,
            "timestamp": int(time.time() * 1000),
            "product_name": None,
            "category": None,
            "price": None,
            "previous_price": None,
            "currency": "USD",
            "stock_quantity": new_stock_quantity,
            "quantity_change": quantity_change,
            "warehouse_location": warehouse_location,
            "product_attributes": None,
            "supplier_id": None,
            "is_active": True,
            "source_system": "ecommerce-platform",
            "schema_version": "1.0.0"
        }

        self.produce(key=product_id, value=event)

        logger.info(
            f"Produced INVENTORY_INCREASED event for product {product_id} "
            f"(+{quantity_change}, new total: {new_stock_quantity})"
        )

    def produce_inventory_decreased(
        self,
        product_id: str,
        quantity_change: int,
        new_stock_quantity: int,
        warehouse_location: Optional[str] = None
    ) -> None:
        """
        Produce INVENTORY_DECREASED event.

        Args:
            product_id: Unique product identifier
            quantity_change: Quantity removed from stock (positive number)
            new_stock_quantity: New total stock quantity
            warehouse_location: Warehouse location code
        """
        event = {
            "event_id": str(uuid.uuid4()),
            "event_type": "INVENTORY_DECREASED",
            "product_id": product_id,
            "timestamp": int(time.time() * 1000),
            "product_name": None,
            "category": None,
            "price": None,
            "previous_price": None,
            "currency": "USD",
            "stock_quantity": new_stock_quantity,
            "quantity_change": -quantity_change,  # Negative for decrease
            "warehouse_location": warehouse_location,
            "product_attributes": None,
            "supplier_id": None,
            "is_active": True,
            "source_system": "ecommerce-platform",
            "schema_version": "1.0.0"
        }

        self.produce(key=product_id, value=event)

        logger.info(
            f"Produced INVENTORY_DECREASED event for product {product_id} "
            f"(-{quantity_change}, new total: {new_stock_quantity})"
        )

    def produce_price_changed(
        self,
        product_id: str,
        new_price: Decimal,
        previous_price: Decimal,
        currency: str = "USD"
    ) -> None:
        """
        Produce PRICE_CHANGED event.

        Args:
            product_id: Unique product identifier
            new_price: New product price
            previous_price: Previous product price
            currency: Currency code
        """
        event = {
            "event_id": str(uuid.uuid4()),
            "event_type": "PRICE_CHANGED",
            "product_id": product_id,
            "timestamp": int(time.time() * 1000),
            "product_name": None,
            "category": None,
            "price": new_price,
            "previous_price": previous_price,
            "currency": currency,
            "stock_quantity": None,
            "quantity_change": None,
            "warehouse_location": None,
            "product_attributes": None,
            "supplier_id": None,
            "is_active": True,
            "source_system": "ecommerce-platform",
            "schema_version": "1.0.0"
        }

        self.produce(key=product_id, value=event)

        logger.info(
            f"Produced PRICE_CHANGED event for product {product_id} "
            f"({previous_price} -> {new_price} {currency})"
        )
