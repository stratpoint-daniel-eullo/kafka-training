#!/usr/bin/env python3
"""
Day 5: Kafka Avro Producer with Schema Registry - Python Example

This demonstrates schema management and Avro serialization,
using confluent-kafka with Schema Registry integration.

Perfect for data engineers working with structured, evolving data schemas.

What you'll learn:
- How to use Schema Registry with Python
- Avro serialization and deserialization
- Schema evolution compatibility
- Compare to Java KafkaAvroSerializer

Requirements:
    pip install confluent-kafka[avro]

Run: python examples/python/day05_avro_producer.py
"""

import sys
from confluent_kafka import Producer
from confluent_kafka.schema_registry import SchemaRegistryClient
from confluent_kafka.schema_registry.avro import AvroSerializer
from confluent_kafka.serialization import StringSerializer, SerializationContext, MessageField

# Configuration
BOOTSTRAP_SERVERS = 'localhost:9092'
SCHEMA_REGISTRY_URL = 'http://localhost:8081'
TOPIC_NAME = 'python-avro-user-events'

# Avro Schema Definition (same as Java)
USER_EVENT_SCHEMA = """
{
  "type": "record",
  "name": "UserEvent",
  "namespace": "com.training.kafka.avro",
  "fields": [
    {"name": "user_id", "type": "string"},
    {"name": "event_type", "type": "string"},
    {"name": "timestamp", "type": "long"},
    {"name": "properties", "type": {"type": "map", "values": "string"}}
  ]
}
"""


class UserEvent:
    """
    UserEvent class - represents the Avro record
    """
    def __init__(self, user_id, event_type, timestamp, properties):
        self.user_id = user_id
        self.event_type = event_type
        self.timestamp = timestamp
        self.properties = properties


def user_event_to_dict(user_event, ctx):
    """
    Convert UserEvent object to dictionary for Avro serialization
    """
    return {
        'user_id': user_event.user_id,
        'event_type': user_event.event_type,
        'timestamp': user_event.timestamp,
        'properties': user_event.properties
    }


def delivery_report(err, msg):
    """
    Callback for message delivery reports
    Compare to Java: Callback interface in producer.send()
    """
    if err is not None:
        print(f'❌ Message delivery failed: {err}', file=sys.stderr)
    else:
        print(f'✓ Message delivered to {msg.topic()} '
              f'[partition {msg.partition()}] at offset {msg.offset()}')


def create_avro_producer():
    """
    Create Kafka producer with Avro serialization
    Compare to Java: new KafkaProducer<>(props) with KafkaAvroSerializer
    """
    print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    print("Step 1: Create Schema Registry Client & Avro Serializer")
    print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

    # Schema Registry client
    schema_registry_client = SchemaRegistryClient({
        'url': SCHEMA_REGISTRY_URL
    })

    # Avro Serializer for the value
    avro_serializer = AvroSerializer(
        schema_registry_client,
        USER_EVENT_SCHEMA,
        user_event_to_dict
    )

    # String Serializer for the key
    string_serializer = StringSerializer('utf_8')

    # Producer configuration
    producer_config = {
        'bootstrap.servers': BOOTSTRAP_SERVERS,
        'client.id': 'python-avro-producer',

        # Reliability settings (same as Java)
        'acks': 'all',
        'retries': 3,
        'enable.idempotence': True,

        # Performance settings
        'linger.ms': 10,
        'batch.size': 16384,
        'compression.type': 'snappy'
    }

    producer = Producer(producer_config)

    print("✓ Schema Registry Client created")
    print(f"  URL: {SCHEMA_REGISTRY_URL}")
    print("✓ Avro Serializer configured")
    print("  Schema: UserEvent")
    print("✓ Producer configured")
    print(f"  Bootstrap Servers: {BOOTSTRAP_SERVERS}")
    print(f"  Acks: all")
    print(f"  Idempotence: True\n")

    return producer, string_serializer, avro_serializer


def send_avro_messages(producer, string_serializer, avro_serializer):
    """
    Send messages with Avro serialization
    Compare to Java: producer.send(new ProducerRecord<>(...))
    """
    print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    print("Step 2: Send Avro-serialized Messages")
    print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

    import time

    # Sample user events
    events = [
        UserEvent(
            user_id='user-123',
            event_type='LOGIN',
            timestamp=int(time.time() * 1000),
            properties={'device': 'mobile', 'location': 'US'}
        ),
        UserEvent(
            user_id='user-456',
            event_type='PAGE_VIEW',
            timestamp=int(time.time() * 1000),
            properties={'page': '/products', 'referrer': 'google'}
        ),
        UserEvent(
            user_id='user-123',
            event_type='PURCHASE',
            timestamp=int(time.time() * 1000),
            properties={'product_id': 'prod-789', 'amount': '99.99'}
        ),
        UserEvent(
            user_id='user-789',
            event_type='LOGIN',
            timestamp=int(time.time() * 1000),
            properties={'device': 'desktop', 'location': 'UK'}
        ),
        UserEvent(
            user_id='user-456',
            event_type='ADD_TO_CART',
            timestamp=int(time.time() * 1000),
            properties={'product_id': 'prod-123', 'quantity': '2'}
        ),
    ]

    print(f"Sending {len(events)} Avro-serialized events to topic: {TOPIC_NAME}\n")

    for idx, event in enumerate(events, 1):
        try:
            # Serialize key (string)
            key = string_serializer(event.user_id)

            # Serialize value (Avro)
            value = avro_serializer(
                event,
                SerializationContext(TOPIC_NAME, MessageField.VALUE)
            )

            # Send message
            producer.produce(
                topic=TOPIC_NAME,
                key=key,
                value=value,
                on_delivery=delivery_report
            )

            print(f"  {idx}. Sent: {event.event_type:15} | User: {event.user_id}")

            # Poll to trigger delivery report callbacks
            producer.poll(0)

        except Exception as e:
            print(f"❌ Failed to send message: {e}", file=sys.stderr)

    # Wait for all messages to be delivered
    print("\nFlushing producer (waiting for all messages to be sent)...")
    producer.flush()

    print(f"\n✓ Successfully sent {len(events)} Avro-serialized messages\n")


def verify_schema_registry():
    """
    Verify schema was registered in Schema Registry
    """
    print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    print("Step 3: Verify Schema in Schema Registry")
    print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

    schema_registry_client = SchemaRegistryClient({
        'url': SCHEMA_REGISTRY_URL
    })

    try:
        # Get the latest schema for the topic's value
        subject_name = f"{TOPIC_NAME}-value"
        schema = schema_registry_client.get_latest_version(subject_name)

        print(f"✓ Schema registered successfully:")
        print(f"  Subject: {subject_name}")
        print(f"  Schema ID: {schema.schema_id}")
        print(f"  Version: {schema.version}")
        print(f"  Type: {schema.schema.schema_type}")
        print()

    except Exception as e:
        print(f"⚠ Could not verify schema: {e}\n")


def main():
    """
    Main demonstration
    """
    print("╔═════════════════════════════════════════════════════════╗")
    print("║  Day 5: Avro Producer with Schema Registry (Python)    ║")
    print("║  Same concepts as Java • confluent-kafka library       ║")
    print("╚═════════════════════════════════════════════════════════╝\n")

    print("Prerequisites:")
    print("  ✓ Kafka running on localhost:9092")
    print("  ✓ Schema Registry running on localhost:8081")
    print("  ✓ confluent-kafka[avro] installed")
    print()

    print("What this demo shows:")
    print("  1. How to create Avro serializer with Schema Registry")
    print("  2. How to send Avro-serialized messages")
    print("  3. How Schema Registry automatically registers schemas")
    print()

    input("Press Enter to start the demo...")
    print()

    try:
        # Create producer with Avro serialization
        producer, string_serializer, avro_serializer = create_avro_producer()

        # Send Avro messages
        send_avro_messages(producer, string_serializer, avro_serializer)

        # Verify schema was registered
        verify_schema_registry()

        print("╔═════════════════════════════════════════════════════════╗")
        print("║    Day 5 Avro Producer Demo Complete (Python)          ║")
        print("╚═════════════════════════════════════════════════════════╝")
        print("\nKey Takeaways:")
        print("  ✓ Used Schema Registry for schema management")
        print("  ✓ Avro serialization ensures type safety")
        print("  ✓ Schemas are versioned and evolve independently")
        print("  ✓ Same concepts as Java KafkaAvroSerializer")
        print()
        print("Benefits of Avro + Schema Registry:")
        print("  → Strong typing and data validation")
        print("  → Schema evolution with compatibility checks")
        print("  → Compact binary format (smaller than JSON)")
        print("  → Self-documenting schemas")
        print()
        print("Next Steps:")
        print("  → Run Day 5 consumer: python day05_avro_consumer.py")
        print("  → Try schema evolution (add optional field)")
        print("  → Compare with Java AvroProducer.java")
        print("  → Use in production data pipelines")

    except KeyboardInterrupt:
        print("\n\n⚠ Interrupted by user")
    except Exception as e:
        print(f"\n❌ ERROR: {e}", file=sys.stderr)
        import traceback
        traceback.print_exc()
        sys.exit(1)


if __name__ == '__main__':
    main()
