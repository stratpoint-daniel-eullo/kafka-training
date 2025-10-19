"""
User Event Producer

Produces user lifecycle events (registration, login, updates) to Kafka.
Demonstrates Day 3 (idempotent producer) and Day 5 (Avro serialization).
"""

import uuid
import time
import logging
from typing import Dict, Any, Optional

from ecommerce_analytics.producers.base_producer import BaseProducer
from ecommerce_analytics.config.settings import get_settings

logger = logging.getLogger(__name__)


class UserEventProducer(BaseProducer):
    """
    Producer for user lifecycle events.

    Event Types:
    - USER_REGISTERED: New user registration
    - USER_UPDATED: User profile update
    - USER_LOGIN: User login event
    - USER_LOGOUT: User logout event
    - USER_DELETED: User account deletion
    """

    def __init__(self):
        """Initialize UserEventProducer with user-events topic."""
        settings = get_settings()
        schema_path = settings.get_schema_path("user_events.avsc")

        super().__init__(
            topic="user-events",
            schema_path=schema_path,
            to_dict=self._user_event_to_dict
        )

    @staticmethod
    def _user_event_to_dict(event: Dict[str, Any], ctx) -> Dict[str, Any]:
        """
        Convert user event to dict for Avro serialization.

        Args:
            event: User event dictionary
            ctx: Serialization context

        Returns:
            Dictionary ready for Avro serialization
        """
        # Avro serializer expects dict - already in correct format
        return event

    def produce_user_registered(
        self,
        user_id: str,
        email: str,
        username: str,
        country: Optional[str] = None,
        metadata: Optional[Dict[str, str]] = None
    ) -> None:
        """
        Produce USER_REGISTERED event.

        Args:
            user_id: Unique user identifier
            email: User email address
            username: User display name
            country: User country code (ISO 3166-1 alpha-2)
            metadata: Additional user attributes
        """
        event = {
            "event_id": str(uuid.uuid4()),
            "event_type": "USER_REGISTERED",
            "user_id": user_id,
            "timestamp": int(time.time() * 1000),
            "email": email,
            "username": username,
            "country": country,
            "user_metadata": metadata,
            "source_system": "ecommerce-platform",
            "schema_version": "1.0.0"
        }

        # Use user_id as key for consistent partitioning
        self.produce(key=user_id, value=event)

        logger.info(f"Produced USER_REGISTERED event for user {user_id}")

    def produce_user_updated(
        self,
        user_id: str,
        email: Optional[str] = None,
        username: Optional[str] = None,
        country: Optional[str] = None,
        metadata: Optional[Dict[str, str]] = None
    ) -> None:
        """
        Produce USER_UPDATED event.

        Args:
            user_id: Unique user identifier
            email: Updated email (optional)
            username: Updated username (optional)
            country: Updated country (optional)
            metadata: Updated metadata (optional)
        """
        event = {
            "event_id": str(uuid.uuid4()),
            "event_type": "USER_UPDATED",
            "user_id": user_id,
            "timestamp": int(time.time() * 1000),
            "email": email,
            "username": username,
            "country": country,
            "user_metadata": metadata,
            "source_system": "ecommerce-platform",
            "schema_version": "1.0.0"
        }

        self.produce(key=user_id, value=event)

        logger.info(f"Produced USER_UPDATED event for user {user_id}")

    def produce_user_login(
        self,
        user_id: str,
        metadata: Optional[Dict[str, str]] = None
    ) -> None:
        """
        Produce USER_LOGIN event.

        Args:
            user_id: Unique user identifier
            metadata: Login metadata (IP, device, etc.)
        """
        event = {
            "event_id": str(uuid.uuid4()),
            "event_type": "USER_LOGIN",
            "user_id": user_id,
            "timestamp": int(time.time() * 1000),
            "email": None,
            "username": None,
            "country": None,
            "user_metadata": metadata,
            "source_system": "ecommerce-platform",
            "schema_version": "1.0.0"
        }

        self.produce(key=user_id, value=event)

        logger.debug(f"Produced USER_LOGIN event for user {user_id}")

    def produce_user_logout(
        self,
        user_id: str,
        metadata: Optional[Dict[str, str]] = None
    ) -> None:
        """
        Produce USER_LOGOUT event.

        Args:
            user_id: Unique user identifier
            metadata: Logout metadata
        """
        event = {
            "event_id": str(uuid.uuid4()),
            "event_type": "USER_LOGOUT",
            "user_id": user_id,
            "timestamp": int(time.time() * 1000),
            "email": None,
            "username": None,
            "country": None,
            "user_metadata": metadata,
            "source_system": "ecommerce-platform",
            "schema_version": "1.0.0"
        }

        self.produce(key=user_id, value=event)

        logger.debug(f"Produced USER_LOGOUT event for user {user_id}")

    def produce_user_deleted(self, user_id: str) -> None:
        """
        Produce USER_DELETED event.

        Args:
            user_id: Unique user identifier
        """
        event = {
            "event_id": str(uuid.uuid4()),
            "event_type": "USER_DELETED",
            "user_id": user_id,
            "timestamp": int(time.time() * 1000),
            "email": None,
            "username": None,
            "country": None,
            "user_metadata": None,
            "source_system": "ecommerce-platform",
            "schema_version": "1.0.0"
        }

        self.produce(key=user_id, value=event)

        logger.info(f"Produced USER_DELETED event for user {user_id}")
