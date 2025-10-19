"""
Fraud Detection Consumer

Analyzes payment events for fraud detection (Day 8 demo).
Demonstrates real-time pattern detection and alerting.
"""

import logging
from typing import Dict, Any
from collections import defaultdict
from datetime import datetime, timedelta

from ecommerce_analytics.consumers.base_consumer import BaseConsumer
from ecommerce_analytics.config.settings import get_settings

logger = logging.getLogger(__name__)


class FraudDetectionConsumer(BaseConsumer):
    """
    Consumer for real-time fraud detection on payment events.

    Detects:
    - Multiple failed payments from same user
    - High-value transactions
    - Rapid transaction patterns
    - Suspicious fraud scores

    This consumer demonstrates Day 8 concepts (monitoring and security).
    In production, this would integrate with fraud prevention services.
    """

    def __init__(
        self,
        high_value_threshold: float = 1000.0,
        fraud_score_threshold: float = 0.7
    ):
        """
        Initialize FraudDetectionConsumer.

        Args:
            high_value_threshold: Threshold for high-value alerts ($)
            fraud_score_threshold: Threshold for fraud score alerts (0.0-1.0)
        """
        super().__init__(
            topics=["payment-events"],
            group_id="fraud-detection-consumer-group"
        )

        self.high_value_threshold = high_value_threshold
        self.fraud_score_threshold = fraud_score_threshold

        # Fraud detection state
        self.user_failed_payments = defaultdict(int)  # user_id -> failed count
        self.user_payment_times = defaultdict(list)  # user_id -> [timestamps]

        # Metrics
        self.fraud_alerts = 0
        self.high_value_alerts = 0
        self.rapid_transaction_alerts = 0

        logger.info(
            f"FraudDetectionConsumer initialized "
            f"(high value: ${high_value_threshold}, fraud score: {fraud_score_threshold})"
        )

    def start(self) -> None:
        """
        Start consuming and analyzing payment events.
        """
        logger.info("Starting fraud detection...")

        # Start consuming with process function
        self.consume(
            process_func=self._process_event,
            commit_interval=10  # Commit every 10 messages (important for fraud alerts)
        )

    def _process_event(self, event: Dict[str, Any]) -> None:
        """
        Process payment event and check for fraud patterns.

        Args:
            event: Deserialized payment event
        """
        event_type = event.get("event_type")

        if event_type == "PAYMENT_INITIATED":
            self._check_high_value_transaction(event)
            self._check_rapid_transactions(event)

        elif event_type == "PAYMENT_AUTHORIZED":
            self._check_fraud_score(event)

        elif event_type == "PAYMENT_FAILED":
            self._track_failed_payment(event)

        elif event_type == "PAYMENT_DECLINED":
            self._track_failed_payment(event)
            self._check_fraud_score(event)

    def _check_high_value_transaction(self, event: Dict[str, Any]) -> None:
        """
        Check for high-value transactions.

        Args:
            event: Payment event
        """
        amount = event.get("amount")

        # Convert Avro decimal (bytes) to float
        if isinstance(amount, bytes):
            amount = float(int.from_bytes(amount, byteorder='big')) / 100

        if amount >= self.high_value_threshold:
            self.high_value_alerts += 1

            logger.warning(
                f"HIGH VALUE TRANSACTION ALERT: "
                f"Payment: {event.get('payment_id')}, "
                f"User: {event.get('user_id')}, "
                f"Amount: ${amount:.2f}"
            )

            # In production: trigger additional verification

    def _check_fraud_score(self, event: Dict[str, Any]) -> None:
        """
        Check fraud score from payment gateway.

        Args:
            event: Payment event
        """
        fraud_score = event.get("fraud_score")

        if fraud_score and fraud_score >= self.fraud_score_threshold:
            self.fraud_alerts += 1

            logger.error(
                f"FRAUD SCORE ALERT: "
                f"Payment: {event.get('payment_id')}, "
                f"User: {event.get('user_id')}, "
                f"Score: {fraud_score:.2f}, "
                f"Reason: {event.get('failure_reason', 'N/A')}"
            )

            # In production: block transaction and notify security team

    def _track_failed_payment(self, event: Dict[str, Any]) -> None:
        """
        Track failed payments per user.

        Args:
            event: Payment event
        """
        user_id = event.get("user_id")
        self.user_failed_payments[user_id] += 1

        failed_count = self.user_failed_payments[user_id]

        logger.info(
            f"Payment failed for user {user_id}: "
            f"{event.get('payment_id')} - "
            f"Total failures: {failed_count}"
        )

        # Alert if too many failures
        if failed_count >= 3:
            self.fraud_alerts += 1

            logger.error(
                f"MULTIPLE FAILED PAYMENTS ALERT: "
                f"User {user_id} has {failed_count} failed payments - "
                f"Possible fraud or stolen card"
            )

            # In production: temporarily block user or require verification

    def _check_rapid_transactions(self, event: Dict[str, Any]) -> None:
        """
        Check for rapid transaction patterns (velocity check).

        Args:
            event: Payment event
        """
        user_id = event.get("user_id")
        timestamp = event.get("timestamp")

        # Convert milliseconds to datetime
        event_time = datetime.fromtimestamp(timestamp / 1000.0)

        # Track payment times for this user
        self.user_payment_times[user_id].append(event_time)

        # Check for multiple transactions in short time window (5 minutes)
        recent_payments = [
            t for t in self.user_payment_times[user_id]
            if event_time - t < timedelta(minutes=5)
        ]

        if len(recent_payments) >= 3:
            self.rapid_transaction_alerts += 1

            logger.warning(
                f"RAPID TRANSACTION ALERT: "
                f"User {user_id} has {len(recent_payments)} transactions "
                f"in 5 minutes - Possible card testing"
            )

            # In production: implement velocity limits

        # Clean up old entries (keep last 24 hours)
        cutoff_time = event_time - timedelta(hours=24)
        self.user_payment_times[user_id] = [
            t for t in self.user_payment_times[user_id]
            if t > cutoff_time
        ]

    def get_fraud_metrics(self) -> Dict[str, Any]:
        """
        Get fraud detection metrics.

        Returns:
            Dictionary with fraud metrics
        """
        total_users_monitored = len(self.user_failed_payments)
        users_with_failures = sum(
            1 for count in self.user_failed_payments.values() if count > 0
        )

        return {
            "total_fraud_alerts": self.fraud_alerts,
            "high_value_alerts": self.high_value_alerts,
            "rapid_transaction_alerts": self.rapid_transaction_alerts,
            "users_monitored": total_users_monitored,
            "users_with_failed_payments": users_with_failures,
        }

    def close(self) -> None:
        """
        Close consumer and log final fraud metrics.
        """
        logger.info("Closing FraudDetectionConsumer...")

        metrics = self.get_fraud_metrics()
        logger.info("=" * 60)
        logger.info("FRAUD DETECTION SUMMARY")
        logger.info("=" * 60)
        logger.info(f"Total Fraud Alerts: {metrics['total_fraud_alerts']}")
        logger.info(f"High Value Alerts: {metrics['high_value_alerts']}")
        logger.info(f"Rapid Transaction Alerts: {metrics['rapid_transaction_alerts']}")
        logger.info(f"Users Monitored: {metrics['users_monitored']}")
        logger.info(f"Users with Failures: {metrics['users_with_failed_payments']}")
        logger.info("=" * 60)

        super().close()
