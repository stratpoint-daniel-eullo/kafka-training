#!/usr/bin/env python3
"""
Day 4: Kafka Consumer - Python Example

This demonstrates the SAME concepts as the Java examples,
but using Python kafka-python library.

Perfect for data engineers who work with Python-based data pipelines.

What you'll learn:
- How to create a Kafka consumer in Python
- Consumer groups and offset management
- Polling and processing messages
- Manual vs auto commit

Run: python examples/python/day04_consumer.py
"""

import json
import sys
from kafka import KafkaConsumer
from kafka.errors import KafkaError

# Configuration (same as Java Properties)
BOOTSTRAP_SERVERS = 'localhost:9092'
TOPIC_NAME = 'training-day01-cli'
GROUP_ID = 'python-consumer-group-cli'


def create_consumer():
    """
    Create Kafka consumer with configuration
    Compare to Java: new KafkaConsumer<>(properties)
    """
    return KafkaConsumer(
        TOPIC_NAME,

        # Connection
        bootstrap_servers=BOOTSTRAP_SERVERS,
        client_id='python-consumer-cli',
        group_id=GROUP_ID,

        # Deserialization (Java: StringDeserializer)
        key_deserializer=lambda k: k.decode('utf-8') if k else None,
        value_deserializer=lambda v: json.loads(v.decode('utf-8')),

        # Consumer behavior (same as Java)
        auto_offset_reset='earliest',  # ConsumerConfig.AUTO_OFFSET_RESET_CONFIG
        enable_auto_commit=False,      # ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG
        max_poll_records=100,          # ConsumerConfig.MAX_POLL_RECORDS_CONFIG

        # Performance tuning (same as Java)
        fetch_min_bytes=1024,          # ConsumerConfig.FETCH_MIN_BYTES_CONFIG
        fetch_max_wait_ms=500          # ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG
    )


def process_message(message):
    """
    Process individual message
    In real applications, this would contain your business logic:
    - Parse and validate data
    - Store in database
    - Trigger workflows
    - Send to other systems
    """
    # Your business logic here
    pass


def main():
    """
    Main demonstration
    """
    print("╔═════════════════════════════════════════════════════════╗")
    print("║  Day 4: Kafka Consumer - Python kafka-python           ║")
    print("║  Same concepts as Java • Different language            ║")
    print("╚═════════════════════════════════════════════════════════╝\n")

    print("✓ Consumer Configuration:")
    print(f"  Bootstrap Servers: {BOOTSTRAP_SERVERS}")
    print(f"  Group ID: {GROUP_ID}")
    print(f"  Topic: {TOPIC_NAME}")
    print(f"  Auto Offset Reset: earliest")
    print(f"  Auto Commit: False (manual commit)\n")

    print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    print("Consuming messages... (Press Ctrl+C to stop)")
    print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

    # Create consumer
    consumer = create_consumer()

    message_count = 0
    poll_count = 0

    try:
        # Poll and process messages
        # Compare to Java: while(true) { records = consumer.poll(...) }
        for message in consumer:
            poll_count += 1
            message_count += 1

            print(f"  Message #{message_count}:")
            print(f"    Topic: {message.topic}")
            print(f"    Partition: {message.partition}")
            print(f"    Offset: {message.offset}")
            print(f"    Timestamp: {message.timestamp}")
            print(f"    Key: {message.key}")
            print(f"    Value: {json.dumps(message.value, indent=6)}")
            print()

            # Process message
            process_message(message)

            # Manual commit (at-least-once semantics)
            # Compare to Java: consumer.commitSync()
            consumer.commit()

            # Show progress every 10 messages
            if message_count % 10 == 0:
                print(f"  ✓ Processed and committed {message_count} messages")
                print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

    except KeyboardInterrupt:
        print("\n\n╔═════════════════════════════════════════════════════════╗")
        print("║           Consumer Shutting Down Gracefully             ║")
        print("╚═════════════════════════════════════════════════════════╝")
        print(f"  Total messages consumed: {message_count}")

    except KafkaError as e:
        print(f"\n❌ Kafka ERROR: {e}", file=sys.stderr)
        sys.exit(1)

    except Exception as e:
        print(f"\n❌ ERROR: {e}", file=sys.stderr)
        sys.exit(1)

    finally:
        consumer.close()
        print("  ✓ Consumer closed\n")

        print("Key Takeaways:")
        print("  ✓ Created Kafka consumer in Python")
        print("  ✓ Same concepts as Java (groups, offsets, polling)")
        print("  ✓ Manual offset commit for at-least-once processing")
        print("  ✓ Platform-agnostic Kafka knowledge")
        print("\nNext Steps:")
        print("  → Run Java version: java -cp target/... AdvancedConsumer")
        print("  → Compare: Same Kafka concepts, different languages!")
        print("  → Use in Spark: Read from Kafka, process with Spark")
        print("  → Use in Flink: Real-time stream processing")


if __name__ == '__main__':
    main()
