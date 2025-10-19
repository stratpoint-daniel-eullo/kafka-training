"""
Payment Event Producer

Produces payment transaction events to Kafka.
Demonstrates Day 3 (idempotent producer) and Day 8 (fraud detection integration).
"""

import uuid
import time
import logging
from typing import Dict, Any, Optional
from decimal import Decimal

from ecommerce_analytics.producers.base_producer import BaseProducer
from ecommerce_analytics.config.settings import get_settings

logger = logging.getLogger(__name__)


class PaymentEventProducer(BaseProducer):
    """
    Producer for payment transaction events.

    Event Types:
    - PAYMENT_INITIATED: Payment started
    - PAYMENT_AUTHORIZED: Payment authorized by gateway
    - PAYMENT_COMPLETED: Payment successfully completed
    - PAYMENT_FAILED: Payment failed
    - PAYMENT_REFUNDED: Payment refunded
    - PAYMENT_DECLINED: Payment declined
    """

    def __init__(self):
        """Initialize PaymentEventProducer with payment-events topic."""
        settings = get_settings()
        schema_path = settings.get_schema_path("payment_events.avsc")

        super().__init__(
            topic="payment-events",
            schema_path=schema_path,
            to_dict=self._payment_event_to_dict
        )

    @staticmethod
    def _payment_event_to_dict(event: Dict[str, Any], ctx) -> Dict[str, Any]:
        """
        Convert payment event to dict for Avro serialization.

        Args:
            event: Payment event dictionary
            ctx: Serialization context

        Returns:
            Dictionary ready for Avro serialization
        """
        # Avro serializer handles Decimal objects
        return event

    def produce_payment_initiated(
        self,
        payment_id: str,
        order_id: str,
        user_id: str,
        amount: Decimal,
        payment_method: str,
        currency: str = "USD",
        billing_address: Optional[Dict[str, str]] = None,
        ip_address: Optional[str] = None,
        device_fingerprint: Optional[str] = None
    ) -> None:
        """
        Produce PAYMENT_INITIATED event.

        Args:
            payment_id: Unique payment identifier
            order_id: Associated order ID
            user_id: User making the payment
            amount: Payment amount
            payment_method: Payment method (CREDIT_CARD, PAYPAL, etc.)
            currency: Currency code
            billing_address: Billing address dict
            ip_address: IP address for fraud detection
            device_fingerprint: Device fingerprint for fraud detection
        """
        event = {
            "event_id": str(uuid.uuid4()),
            "event_type": "PAYMENT_INITIATED",
            "payment_id": payment_id,
            "order_id": order_id,
            "user_id": user_id,
            "timestamp": int(time.time() * 1000),
            "amount": amount,
            "currency": currency,
            "payment_method": payment_method,
            "card_last_four": None,
            "card_brand": None,
            "payment_gateway": None,
            "transaction_id": None,
            "payment_status": "PENDING",
            "failure_reason": None,
            "fraud_score": None,
            "billing_address": billing_address,
            "ip_address": ip_address,
            "device_fingerprint": device_fingerprint,
            "source_system": "ecommerce-platform",
            "schema_version": "1.0.0"
        }

        # Use payment_id as key for ordering guarantees
        self.produce(key=payment_id, value=event)

        logger.info(f"Produced PAYMENT_INITIATED event for payment {payment_id}")

    def produce_payment_authorized(
        self,
        payment_id: str,
        order_id: str,
        user_id: str,
        amount: Decimal,
        payment_method: str,
        card_last_four: Optional[str] = None,
        card_brand: Optional[str] = None,
        payment_gateway: Optional[str] = None,
        transaction_id: Optional[str] = None,
        fraud_score: Optional[float] = None
    ) -> None:
        """
        Produce PAYMENT_AUTHORIZED event.

        Args:
            payment_id: Unique payment identifier
            order_id: Associated order ID
            user_id: User making the payment
            amount: Payment amount
            payment_method: Payment method
            card_last_four: Last 4 digits of card
            card_brand: Card brand
            payment_gateway: Payment gateway used
            transaction_id: External transaction ID
            fraud_score: Fraud detection score (0.0-1.0)
        """
        event = {
            "event_id": str(uuid.uuid4()),
            "event_type": "PAYMENT_AUTHORIZED",
            "payment_id": payment_id,
            "order_id": order_id,
            "user_id": user_id,
            "timestamp": int(time.time() * 1000),
            "amount": amount,
            "currency": "USD",
            "payment_method": payment_method,
            "card_last_four": card_last_four,
            "card_brand": card_brand,
            "payment_gateway": payment_gateway,
            "transaction_id": transaction_id,
            "payment_status": "AUTHORIZED",
            "failure_reason": None,
            "fraud_score": fraud_score,
            "billing_address": None,
            "ip_address": None,
            "device_fingerprint": None,
            "source_system": "ecommerce-platform",
            "schema_version": "1.0.0"
        }

        self.produce(key=payment_id, value=event)

        logger.info(f"Produced PAYMENT_AUTHORIZED event for payment {payment_id}")

    def produce_payment_completed(
        self,
        payment_id: str,
        order_id: str,
        user_id: str,
        amount: Decimal,
        payment_method: str,
        transaction_id: Optional[str] = None
    ) -> None:
        """
        Produce PAYMENT_COMPLETED event.

        Args:
            payment_id: Unique payment identifier
            order_id: Associated order ID
            user_id: User making the payment
            amount: Payment amount
            payment_method: Payment method
            transaction_id: External transaction ID
        """
        event = {
            "event_id": str(uuid.uuid4()),
            "event_type": "PAYMENT_COMPLETED",
            "payment_id": payment_id,
            "order_id": order_id,
            "user_id": user_id,
            "timestamp": int(time.time() * 1000),
            "amount": amount,
            "currency": "USD",
            "payment_method": payment_method,
            "card_last_four": None,
            "card_brand": None,
            "payment_gateway": None,
            "transaction_id": transaction_id,
            "payment_status": "COMPLETED",
            "failure_reason": None,
            "fraud_score": None,
            "billing_address": None,
            "ip_address": None,
            "device_fingerprint": None,
            "source_system": "ecommerce-platform",
            "schema_version": "1.0.0"
        }

        self.produce(key=payment_id, value=event)

        logger.info(f"Produced PAYMENT_COMPLETED event for payment {payment_id}")

    def produce_payment_failed(
        self,
        payment_id: str,
        order_id: str,
        user_id: str,
        amount: Decimal,
        payment_method: str,
        failure_reason: str
    ) -> None:
        """
        Produce PAYMENT_FAILED event.

        Args:
            payment_id: Unique payment identifier
            order_id: Associated order ID
            user_id: User making the payment
            amount: Payment amount
            payment_method: Payment method
            failure_reason: Reason for payment failure
        """
        event = {
            "event_id": str(uuid.uuid4()),
            "event_type": "PAYMENT_FAILED",
            "payment_id": payment_id,
            "order_id": order_id,
            "user_id": user_id,
            "timestamp": int(time.time() * 1000),
            "amount": amount,
            "currency": "USD",
            "payment_method": payment_method,
            "card_last_four": None,
            "card_brand": None,
            "payment_gateway": None,
            "transaction_id": None,
            "payment_status": "FAILED",
            "failure_reason": failure_reason,
            "fraud_score": None,
            "billing_address": None,
            "ip_address": None,
            "device_fingerprint": None,
            "source_system": "ecommerce-platform",
            "schema_version": "1.0.0"
        }

        self.produce(key=payment_id, value=event)

        logger.warning(
            f"Produced PAYMENT_FAILED event for payment {payment_id}: {failure_reason}"
        )

    def produce_payment_refunded(
        self,
        payment_id: str,
        order_id: str,
        user_id: str,
        amount: Decimal,
        payment_method: str,
        transaction_id: Optional[str] = None
    ) -> None:
        """
        Produce PAYMENT_REFUNDED event.

        Args:
            payment_id: Unique payment identifier (original payment)
            order_id: Associated order ID
            user_id: User who made the payment
            amount: Refund amount
            payment_method: Payment method
            transaction_id: Original transaction ID
        """
        event = {
            "event_id": str(uuid.uuid4()),
            "event_type": "PAYMENT_REFUNDED",
            "payment_id": payment_id,
            "order_id": order_id,
            "user_id": user_id,
            "timestamp": int(time.time() * 1000),
            "amount": amount,
            "currency": "USD",
            "payment_method": payment_method,
            "card_last_four": None,
            "card_brand": None,
            "payment_gateway": None,
            "transaction_id": transaction_id,
            "payment_status": "REFUNDED",
            "failure_reason": None,
            "fraud_score": None,
            "billing_address": None,
            "ip_address": None,
            "device_fingerprint": None,
            "source_system": "ecommerce-platform",
            "schema_version": "1.0.0"
        }

        self.produce(key=payment_id, value=event)

        logger.info(f"Produced PAYMENT_REFUNDED event for payment {payment_id}")

    def produce_payment_declined(
        self,
        payment_id: str,
        order_id: str,
        user_id: str,
        amount: Decimal,
        payment_method: str,
        failure_reason: str,
        fraud_score: Optional[float] = None
    ) -> None:
        """
        Produce PAYMENT_DECLINED event.

        Args:
            payment_id: Unique payment identifier
            order_id: Associated order ID
            user_id: User making the payment
            amount: Payment amount
            payment_method: Payment method
            failure_reason: Reason for decline
            fraud_score: Fraud detection score if declined for fraud
        """
        event = {
            "event_id": str(uuid.uuid4()),
            "event_type": "PAYMENT_DECLINED",
            "payment_id": payment_id,
            "order_id": order_id,
            "user_id": user_id,
            "timestamp": int(time.time() * 1000),
            "amount": amount,
            "currency": "USD",
            "payment_method": payment_method,
            "card_last_four": None,
            "card_brand": None,
            "payment_gateway": None,
            "transaction_id": None,
            "payment_status": "DECLINED",
            "failure_reason": failure_reason,
            "fraud_score": fraud_score,
            "billing_address": None,
            "ip_address": None,
            "device_fingerprint": None,
            "source_system": "ecommerce-platform",
            "schema_version": "1.0.0"
        }

        self.produce(key=payment_id, value=event)

        logger.warning(
            f"Produced PAYMENT_DECLINED event for payment {payment_id}: {failure_reason}"
        )
