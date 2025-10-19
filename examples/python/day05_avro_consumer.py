#!/usr/bin/env python3
"""
Day 5: Kafka Avro Consumer with Schema Registry - Python Example

This demonstrates consuming Avro-serialized messages using Schema Registry.

Perfect for data engineers working with structured, evolving data schemas.

What you'll learn:
- How to consume Avro messages with Python
- Schema Registry integration
- Automatic schema deserialization
- Compare to Java KafkaAvroDeserializer

Requirements:
    pip install confluent-kafka[avro]

Run: python examples/python/day05_avro_consumer.py
     (After running day05_avro_producer.py)
"""

import sys
from confluent_kafka import Consumer
from confluent_kafka.schema_registry import SchemaRegistryClient
from confluent_kafka.schema_registry.avro import AvroDeserializer
from confluent_kafka.serialization import StringDeserializer, SerializationContext, MessageField

# Configuration
BOOTSTRAP_SERVERS = 'localhost:9092'
SCHEMA_REGISTRY_URL = 'http://localhost:8081'
TOPIC_NAME = 'python-avro-user-events'
GROUP_ID = 'python-avro-consumer-group'


def user_event_from_dict(obj, ctx):
    """
    Convert dictionary back to Python object
    (In production, you might have a proper class)
    """
    if obj is None:
        return None

    return {
        'user_id': obj['user_id'],
        'event_type': obj['event_type'],
        'timestamp': obj['timestamp'],
        'properties': obj['properties']
    }


def create_avro_consumer():
    """
    Create Kafka consumer with Avro deserialization
    Compare to Java: new KafkaConsumer<>(props) with KafkaAvroDeserializer
    """
    print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    print("Step 1: Create Schema Registry Client & Avro Deserializer")
    print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

    # Schema Registry client
    schema_registry_client = SchemaRegistryClient({
        'url': SCHEMA_REGISTRY_URL
    })

    # Avro Deserializer for the value
    # Note: No need to specify schema - it's fetched from Schema Registry!
    avro_deserializer = AvroDeserializer(
        schema_registry_client,
        schema_str=None,  # Auto-fetch from registry
        from_dict=user_event_from_dict
    )

    # String Deserializer for the key
    string_deserializer = StringDeserializer('utf_8')

    # Consumer configuration
    consumer_config = {
        'bootstrap.servers': BOOTSTRAP_SERVERS,
        'group.id': GROUP_ID,
        'client.id': 'python-avro-consumer',

        # Consumer behavior (same as Java)
        'auto.offset.reset': 'earliest',
        'enable.auto.commit': False,  # Manual commit
        'max.poll.interval.ms': 300000,
        'session.timeout.ms': 30000,
        'heartbeat.interval.ms': 10000
    }

    consumer = Consumer(consumer_config)

    print("✓ Schema Registry Client created")
    print(f"  URL: {SCHEMA_REGISTRY_URL}")
    print("✓ Avro Deserializer configured")
    print("  Schema: Auto-fetched from registry")
    print("✓ Consumer configured")
    print(f"  Bootstrap Servers: {BOOTSTRAP_SERVERS}")
    print(f"  Group ID: {GROUP_ID}")
    print(f"  Auto Offset Reset: earliest\n")

    return consumer, string_deserializer, avro_deserializer


def consume_avro_messages(consumer, string_deserializer, avro_deserializer):
    """
    Consume Avro-serialized messages
    Compare to Java: consumer.poll() with KafkaAvroDeserializer
    """
    print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    print("Step 2: Consume Avro-serialized Messages")
    print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

    # Subscribe to topic
    consumer.subscribe([TOPIC_NAME])

    print(f"Consuming messages from topic: {TOPIC_NAME}")
    print("(Press Ctrl+C to stop)\n")

    message_count = 0
    max_messages = 10  # Consume up to 10 messages for demo

    try:
        while message_count < max_messages:
            # Poll for messages (1 second timeout)
            msg = consumer.poll(timeout=1.0)

            if msg is None:
                continue

            if msg.error():
                print(f"❌ Consumer error: {msg.error()}", file=sys.stderr)
                continue

            message_count += 1

            # Deserialize key (string)
            key = string_deserializer(msg.key())

            # Deserialize value (Avro) - automatically fetches schema from registry!
            value = avro_deserializer(
                msg.value(),
                SerializationContext(msg.topic(), MessageField.VALUE)
            )

            # Display message
            print(f"  Message #{message_count}:")
            print(f"    Topic: {msg.topic()}")
            print(f"    Partition: {msg.partition()}")
            print(f"    Offset: {msg.offset()}")
            print(f"    Key: {key}")
            print(f"    Value:")
            print(f"      User ID: {value['user_id']}")
            print(f"      Event Type: {value['event_type']}")
            print(f"      Timestamp: {value['timestamp']}")
            print(f"      Properties: {value['properties']}")
            print()

            # Commit offset after processing
            consumer.commit(asynchronous=False)

        print(f"✓ Successfully consumed {message_count} Avro-serialized messages\n")

    except KeyboardInterrupt:
        print(f"\n✓ Consumed {message_count} messages before interruption\n")


def demonstrate_schema_evolution():
    """
    Demonstrate how schema evolution works
    """
    print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    print("Step 3: Understanding Schema Evolution")
    print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

    print("Schema Evolution Benefits:")
    print("  ✓ Add new optional fields without breaking consumers")
    print("  ✓ Schema Registry enforces compatibility rules")
    print("  ✓ Old consumers can still read new messages")
    print("  ✓ New consumers can read old messages")
    print()

    print("Example Evolution:")
    print("  Version 1: {user_id, event_type, timestamp, properties}")
    print("  Version 2: {user_id, event_type, timestamp, properties, session_id}")
    print()

    print("Compatibility Modes:")
    print("  • BACKWARD: New schema can read old data")
    print("  • FORWARD: Old schema can read new data")
    print("  • FULL: Both BACKWARD and FORWARD")
    print("  • NONE: No compatibility checks")
    print()

    print("Try it yourself:")
    print("  1. Update schema (add optional field)")
    print("  2. Run producer with new schema")
    print("  3. Run consumer (still works!)")
    print()


def main():
    """
    Main demonstration
    """
    print("╔═════════════════════════════════════════════════════════╗")
    print("║  Day 5: Avro Consumer with Schema Registry (Python)    ║")
    print("║  Same concepts as Java • confluent-kafka library       ║")
    print("╚═════════════════════════════════════════════════════════╝\n")

    print("Prerequisites:")
    print("  ✓ Kafka running on localhost:9092")
    print("  ✓ Schema Registry running on localhost:8081")
    print("  ✓ confluent-kafka[avro] installed")
    print("  ✓ Messages produced by day05_avro_producer.py")
    print()

    print("What this demo shows:")
    print("  1. How to consume Avro-serialized messages")
    print("  2. How Schema Registry auto-fetches schemas")
    print("  3. How schema evolution works")
    print()

    input("Press Enter to start consuming...")
    print()

    try:
        # Create consumer with Avro deserialization
        consumer, string_deserializer, avro_deserializer = create_avro_consumer()

        # Consume Avro messages
        consume_avro_messages(consumer, string_deserializer, avro_deserializer)

        # Demonstrate schema evolution concepts
        demonstrate_schema_evolution()

        consumer.close()

        print("╔═════════════════════════════════════════════════════════╗")
        print("║    Day 5 Avro Consumer Demo Complete (Python)          ║")
        print("╚═════════════════════════════════════════════════════════╝")
        print("\nKey Takeaways:")
        print("  ✓ Consumed Avro messages with automatic schema fetching")
        print("  ✓ Schema Registry handles schema management")
        print("  ✓ No need to hardcode schemas in consumer")
        print("  ✓ Same concepts as Java KafkaAvroDeserializer")
        print()
        print("Benefits:")
        print("  → Type-safe data consumption")
        print("  → Automatic schema compatibility checks")
        print("  → Decoupled producers and consumers")
        print("  → Reduced data size (binary format)")
        print()
        print("Production Use Cases:")
        print("  → Data lakes (Avro files on S3/HDFS)")
        print("  → Real-time analytics pipelines")
        print("  → Event-driven microservices")
        print("  → Change data capture (CDC)")
        print()
        print("Next Steps:")
        print("  → Run Day 6: python day06_streams_faust.py (stream processing)")
        print("  → Compare with Java AvroConsumer")
        print("  → Try schema evolution experiments")
        print("  → Use in production data engineering workflows")

    except KeyboardInterrupt:
        print("\n\n⚠ Interrupted by user")
    except Exception as e:
        print(f"\n❌ ERROR: {e}", file=sys.stderr)
        import traceback
        traceback.print_exc()
        sys.exit(1)


if __name__ == '__main__':
    main()
