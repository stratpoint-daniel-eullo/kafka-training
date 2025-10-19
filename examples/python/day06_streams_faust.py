#!/usr/bin/env python3
"""
Day 6: Kafka Streams with Faust - Python Example

This demonstrates stream processing using Faust,
the Python equivalent of Kafka Streams.

Perfect for data engineers who want stream processing
without JVM overhead.

What you'll learn:
- How to create stream processing applications in Python
- Filtering, mapping, and aggregating streams
- Windowing and stateful operations
- Compare to Java Kafka Streams

Requirements:
    pip install faust-streaming

Run: faust -A day06_streams_faust worker -l info

Note: This is a Faust app. Run it with the faust command, not python directly.
      Or use: python examples/python/day06_streams_faust.py worker
"""

import faust
from datetime import timedelta

# Configuration
KAFKA_BROKER = 'kafka://localhost:9092'
APP_ID = 'python-faust-stream-processor'

# Create Faust app (similar to StreamsBuilder in Java)
app = faust.App(
    APP_ID,
    broker=KAFKA_BROKER,
    value_serializer='json',
    # Store data locally for aggregations
    store='rocksdb://',
)


# Define data models (similar to Avro schemas)
class UserEvent(faust.Record, serializer='json'):
    """
    User event model
    Compare to Java: Avro or JSON Serde
    """
    user_id: str
    event_type: str
    timestamp: int
    properties: dict


class UserActivity(faust.Record, serializer='json'):
    """
    Aggregated user activity
    Compare to Java: aggregated state in KTable
    """
    user_id: str
    event_count: int
    event_types: list


# Define topics
input_topic = app.topic('user-events', value_type=UserEvent)
filtered_topic = app.topic('high-value-events', value_type=UserEvent)
aggregated_topic = app.topic('user-activity-counts', value_type=UserActivity)


# Stream Processing Agent 1: Filter high-value events
@app.agent(input_topic)
async def filter_high_value_events(stream):
    """
    Filter stream to keep only high-value events
    Compare to Java: stream.filter(predicate)
    """
    high_value_types = {'LOGIN', 'PURCHASE', 'CHECKOUT'}

    async for event in stream:
        if event.event_type in high_value_types:
            print(f"[FILTER] High-value event: {event.event_type} from {event.user_id}")

            # Forward to filtered topic
            await filtered_topic.send(value=event)


# Stream Processing Agent 2: Count events per user
user_event_counts = app.Table('user_event_counts', default=int)


@app.agent(input_topic)
async def count_events_per_user(stream):
    """
    Count events per user (stateful aggregation)
    Compare to Java: stream.groupByKey().count()
    """
    async for event in stream.group_by(UserEvent.user_id):
        # Increment count for this user
        user_event_counts[event.user_id] += 1

        current_count = user_event_counts[event.user_id]
        print(f"[COUNT] User {event.user_id}: {current_count} events")

        # Every 5 events, output aggregated result
        if current_count % 5 == 0:
            activity = UserActivity(
                user_id=event.user_id,
                event_count=current_count,
                event_types=['aggregated']  # In real app, track actual types
            )
            await aggregated_topic.send(value=activity)


# Stream Processing Agent 3: Windowed aggregation
@app.agent(input_topic)
async def windowed_event_counts(stream):
    """
    Count events in tumbling windows
    Compare to Java: stream.windowedBy(TimeWindows.of(...))
    """
    # Create tumbling window of 60 seconds
    async for window, events in stream.tumbling(timedelta(seconds=60)).items():
        event_list = [event async for event in events]
        count = len(event_list)

        if count > 0:
            print(f"[WINDOW] Window {window.start} -> {window.end}: {count} events")


# Stream Processing Agent 4: Enrich events (join-like operation)
@app.agent(input_topic)
async def enrich_events(stream):
    """
    Enrich events with additional data
    Compare to Java: stream.leftJoin() or stream.join()
    """
    # Simulate user lookup table
    user_profiles = {
        'user-123': {'name': 'Alice', 'tier': 'premium'},
        'user-456': {'name': 'Bob', 'tier': 'basic'},
        'user-789': {'name': 'Charlie', 'tier': 'premium'},
    }

    async for event in stream:
        # Enrich with user profile
        profile = user_profiles.get(event.user_id, {'name': 'Unknown', 'tier': 'basic'})

        enriched_event = {
            **event.asdict(),
            'user_name': profile['name'],
            'user_tier': profile['tier']
        }

        print(f"[ENRICH] {event.event_type} by {profile['name']} (tier: {profile['tier']})")


# Web view for monitoring (bonus feature in Faust)
@app.page('/stats/')
async def stats_view(web, request):
    """
    Web endpoint to view current counts
    Access at: http://localhost:6066/stats/
    """
    return web.json({
        'app_id': APP_ID,
        'user_event_counts': dict(user_event_counts.items()),
        'total_users': len(user_event_counts)
    })


# Command to send test data (for demo purposes)
@app.command()
async def produce_test_data():
    """
    Produce test events to demonstrate stream processing
    Run: faust -A day06_streams_faust produce-test-data
    """
    import time

    print("Producing test events to 'user-events' topic...\n")

    test_events = [
        UserEvent(user_id='user-123', event_type='LOGIN', timestamp=int(time.time() * 1000), properties={}),
        UserEvent(user_id='user-456', event_type='PAGE_VIEW', timestamp=int(time.time() * 1000), properties={}),
        UserEvent(user_id='user-123', event_type='PURCHASE', timestamp=int(time.time() * 1000), properties={'amount': '99.99'}),
        UserEvent(user_id='user-789', event_type='LOGIN', timestamp=int(time.time() * 1000), properties={}),
        UserEvent(user_id='user-456', event_type='ADD_TO_CART', timestamp=int(time.time() * 1000), properties={}),
        UserEvent(user_id='user-123', event_type='CHECKOUT', timestamp=int(time.time() * 1000), properties={}),
        UserEvent(user_id='user-789', event_type='PAGE_VIEW', timestamp=int(time.time() * 1000), properties={}),
        UserEvent(user_id='user-456', event_type='PURCHASE', timestamp=int(time.time() * 1000), properties={'amount': '149.99'}),
        UserEvent(user_id='user-123', event_type='LOGOUT', timestamp=int(time.time() * 1000), properties={}),
        UserEvent(user_id='user-789', event_type='PURCHASE', timestamp=int(time.time() * 1000), properties={'amount': '79.99'}),
    ]

    for event in test_events:
        await input_topic.send(value=event)
        print(f"  ✓ Sent: {event.event_type:15} | User: {event.user_id}")
        await app.sleep(0.5)  # Small delay between messages

    print(f"\n✓ Sent {len(test_events)} test events")


if __name__ == '__main__':
    # Print usage instructions
    print("╔═════════════════════════════════════════════════════════╗")
    print("║  Day 6: Kafka Streams with Faust (Python)              ║")
    print("║  Stream processing without JVM overhead                ║")
    print("╚═════════════════════════════════════════════════════════╝\n")

    print("Faust is the Python equivalent of Kafka Streams\n")

    print("To run this stream processor:")
    print("  faust -A day06_streams_faust worker -l info")
    print()
    print("Or with Python:")
    print("  python examples/python/day06_streams_faust.py worker")
    print()
    print("To send test data:")
    print("  faust -A day06_streams_faust produce-test-data")
    print()
    print("To view stats:")
    print("  Open http://localhost:6066/stats/")
    print()
    print("What this demonstrates:")
    print("  ✓ Filtering streams (high-value events)")
    print("  ✓ Stateful aggregations (event counts per user)")
    print("  ✓ Windowed aggregations (60-second windows)")
    print("  ✓ Stream enrichment (join-like operations)")
    print()
    print("Compare to Java Kafka Streams:")
    print("  • StreamsBuilder → faust.App")
    print("  • KStream → faust topic + agent")
    print("  • KTable → faust.Table")
    print("  • filter() → async for with if condition")
    print("  • groupByKey() → stream.group_by()")
    print("  • count() → stateful counter")
    print()
    print("Benefits of Faust over Java Kafka Streams:")
    print("  ✓ No JVM required (lower memory footprint)")
    print("  ✓ Python ecosystem integration")
    print("  ✓ Async/await for better concurrency")
    print("  ✓ Built-in web server for monitoring")
    print()
    print("Next Steps:")
    print("  → Compare with Java StreamProcessor.java")
    print("  → Try more complex aggregations")
    print("  → Run Day 7: python day07_connect_client.py")

    # Run the app if called directly with 'worker' argument
    import sys
    if len(sys.argv) > 1 and sys.argv[1] == 'worker':
        app.main()
