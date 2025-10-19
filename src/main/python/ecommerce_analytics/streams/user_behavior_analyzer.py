"""
User Behavior Analyzer Stream Processor

Faust stream processing app for real-time user behavior analysis (Day 6).
Demonstrates complex event processing and pattern detection.
"""

import faust
import logging
from typing import Dict, Any, List
from datetime import timedelta

logger = logging.getLogger(__name__)


# Define Faust Record models
class UserEvent(faust.Record, serializer='json'):
    """User event record."""
    event_id: str
    event_type: str
    user_id: str
    timestamp: int
    email: str = None
    username: str = None
    country: str = None


class OrderEvent(faust.Record, serializer='json'):
    """Order event record."""
    event_id: str
    event_type: str
    order_id: str
    user_id: str
    timestamp: int
    total_amount: float
    currency: str


class UserBehaviorProfile(faust.Record, serializer='json'):
    """User behavior profile."""
    user_id: str
    username: str
    country: str
    total_logins: int
    total_orders: int
    total_spent: float
    last_login_timestamp: int
    last_order_timestamp: int
    engagement_score: float
    user_segment: str  # new, active, loyal, dormant


def create_user_behavior_app(
    broker: str = "kafka://localhost:9092",
    store_uri: str = "rocksdb://",
) -> faust.App:
    """
    Create Faust app for user behavior analysis.

    Args:
        broker: Kafka broker URL
        store_uri: State store URI

    Returns:
        Faust application instance

    Stream Processing Flow:
    1. Consume user-events and order-events topics
    2. Build user behavior profiles
    3. Calculate engagement scores
    4. Segment users (new, active, loyal, dormant)
    5. Produce to user-behavior topic
    """
    app = faust.App(
        id="user-behavior-analyzer",
        broker=broker,
        store=store_uri,
        processing_guarantee="at_least_once",  # Slightly relaxed for analytics
        consumer_auto_offset_reset="earliest",
        producer_acks="all",
        producer_compression_type="snappy",
    )

    # Input topics
    user_events_topic = app.topic(
        "user-events",
        value_type=UserEvent,
    )

    order_events_topic = app.topic(
        "order-events",
        value_type=OrderEvent,
    )

    # Output topic
    user_behavior_topic = app.topic(
        "user-behavior",
        value_type=UserBehaviorProfile,
    )

    # Stateful table for user profiles (Day 6: State management)
    user_profiles_table = app.Table(
        "user_profiles",
        default=dict,
        partitions=3,
    )

    @app.agent(user_events_topic)
    async def process_user_events(users):
        """
        Process user events and update behavior profiles.

        Tracks:
        - User registrations
        - Login activity
        - Profile updates
        """
        async for event in users:
            user_id = event.user_id

            # Get or initialize profile
            profile = user_profiles_table.get(user_id, {
                "user_id": user_id,
                "username": "",
                "country": "",
                "total_logins": 0,
                "total_orders": 0,
                "total_spent": 0.0,
                "last_login_timestamp": 0,
                "last_order_timestamp": 0,
                "engagement_score": 0.0,
                "user_segment": "new",
            })

            # Update profile based on event type
            if event.event_type == "USER_REGISTERED":
                profile["username"] = event.username or ""
                profile["country"] = event.country or ""
                profile["user_segment"] = "new"

                logger.info(f"New user registered: {user_id} ({event.username})")

            elif event.event_type == "USER_LOGIN":
                profile["total_logins"] += 1
                profile["last_login_timestamp"] = event.timestamp

                logger.debug(f"User login: {user_id} (total logins: {profile['total_logins']})")

            elif event.event_type == "USER_UPDATED":
                # Update user metadata
                if event.username:
                    profile["username"] = event.username
                if event.country:
                    profile["country"] = event.country

            # Recalculate engagement and segment
            profile = calculate_user_segment(profile)

            # Update table
            user_profiles_table[user_id] = profile

            # Publish updated profile
            await publish_behavior_profile(profile)

    @app.agent(order_events_topic)
    async def process_order_events(orders):
        """
        Process order events and update user spending behavior.

        Tracks:
        - Total orders per user
        - Total spending per user
        - Purchase patterns
        """
        async for event in orders:
            if event.event_type != "ORDER_PLACED":
                continue

            user_id = event.user_id

            # Get or initialize profile
            profile = user_profiles_table.get(user_id, {
                "user_id": user_id,
                "username": "",
                "country": "",
                "total_logins": 0,
                "total_orders": 0,
                "total_spent": 0.0,
                "last_login_timestamp": 0,
                "last_order_timestamp": 0,
                "engagement_score": 0.0,
                "user_segment": "new",
            })

            # Update order metrics
            profile["total_orders"] += 1
            profile["total_spent"] += event.total_amount
            profile["last_order_timestamp"] = event.timestamp

            logger.info(
                f"User {user_id} placed order: ${event.total_amount} "
                f"(total: {profile['total_orders']} orders, ${profile['total_spent']:.2f})"
            )

            # Recalculate engagement and segment
            profile = calculate_user_segment(profile)

            # Update table
            user_profiles_table[user_id] = profile

            # Publish updated profile
            await publish_behavior_profile(profile)

    def calculate_user_segment(profile: dict) -> dict:
        """
        Calculate user engagement score and segment.

        Scoring:
        - Logins: 1 point each (max 50)
        - Orders: 10 points each (max 50)
        - Total spent: 1 point per $10 (no max)

        Segments:
        - new: 0-10 points
        - active: 11-50 points
        - loyal: 51-100 points
        - power: 100+ points

        Args:
            profile: User profile dict

        Returns:
            Updated profile with engagement_score and user_segment
        """
        # Calculate engagement score
        login_score = min(profile["total_logins"], 50)
        order_score = min(profile["total_orders"] * 10, 50)
        spending_score = profile["total_spent"] / 10.0

        engagement_score = login_score + order_score + spending_score

        # Determine segment
        if engagement_score <= 10:
            segment = "new"
        elif engagement_score <= 50:
            segment = "active"
        elif engagement_score <= 100:
            segment = "loyal"
        else:
            segment = "power"

        profile["engagement_score"] = engagement_score
        profile["user_segment"] = segment

        return profile

    async def publish_behavior_profile(profile: dict):
        """
        Publish user behavior profile to output topic.

        Args:
            profile: User profile dict
        """
        behavior_profile = UserBehaviorProfile(
            user_id=profile["user_id"],
            username=profile["username"],
            country=profile["country"],
            total_logins=profile["total_logins"],
            total_orders=profile["total_orders"],
            total_spent=profile["total_spent"],
            last_login_timestamp=profile["last_login_timestamp"],
            last_order_timestamp=profile["last_order_timestamp"],
            engagement_score=profile["engagement_score"],
            user_segment=profile["user_segment"],
        )

        await user_behavior_topic.send(value=behavior_profile)

        logger.debug(
            f"Published behavior profile for user {profile['user_id']}: "
            f"Segment={profile['user_segment']}, "
            f"Score={profile['engagement_score']:.1f}"
        )

    @app.page("/behavior/user/{user_id}")
    async def get_user_behavior(web, request, user_id):
        """
        Web endpoint to query user behavior profile.

        Access: http://localhost:6068/behavior/user/{user_id}
        """
        profile = user_profiles_table.get(user_id)

        if profile:
            return web.json(profile)
        else:
            return web.json({"error": f"No profile for user {user_id}"})

    @app.page("/behavior/segments")
    async def get_segment_distribution(web, request):
        """
        Web endpoint to get user segment distribution.

        Access: http://localhost:6068/behavior/segments
        """
        segments = {"new": 0, "active": 0, "loyal": 0, "power": 0}

        # Count users in each segment
        for user_id, profile in user_profiles_table.items():
            segment = profile.get("user_segment", "new")
            segments[segment] = segments.get(segment, 0) + 1

        return web.json({
            "total_users": len(user_profiles_table),
            "segments": segments,
        })

    @app.page("/behavior/top-users")
    async def get_top_users(web, request):
        """
        Web endpoint to get top users by engagement.

        Access: http://localhost:6068/behavior/top-users
        """
        # Get all profiles and sort by engagement score
        profiles = [
            {
                "user_id": profile["user_id"],
                "username": profile["username"],
                "engagement_score": profile["engagement_score"],
                "user_segment": profile["user_segment"],
                "total_orders": profile["total_orders"],
                "total_spent": profile["total_spent"],
            }
            for profile in user_profiles_table.values()
        ]

        # Sort by engagement score (descending)
        top_users = sorted(
            profiles,
            key=lambda x: x["engagement_score"],
            reverse=True
        )[:10]

        return web.json({"top_users": top_users})

    logger.info("User Behavior Analyzer Faust app created")

    return app


# For running standalone
if __name__ == "__main__":
    app = create_user_behavior_app()
    app.main()
