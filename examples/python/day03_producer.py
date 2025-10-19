#!/usr/bin/env python3
"""
Day 3: Kafka Producer - Python Example

This demonstrates the SAME concepts as the Java examples,
but using Python kafka-python library.

Perfect for data engineers who work with Python-based data pipelines.

What you'll learn:
- How to create a Kafka producer in Python
- Producer configuration (same concepts as Java)
- Sending messages with keys
- Error handling and callbacks

Run: python examples/python/day03_producer.py
"""

import json
import sys
from datetime import datetime
from kafka import KafkaProducer
from kafka.errors import KafkaError

# Configuration (same as Java Properties)
BOOTSTRAP_SERVERS = 'localhost:9092'
TOPIC_NAME = 'training-day01-cli'


def create_producer():
    """
    Create Kafka producer with configuration
    Compare to Java: new KafkaProducer<>(properties)
    """
    return KafkaProducer(
        # Connection
        bootstrap_servers=BOOTSTRAP_SERVERS,
        client_id='python-producer-cli',

        # Serialization (Java: StringSerializer)
        key_serializer=lambda k: k.encode('utf-8') if k else None,
        value_serializer=lambda v: json.dumps(v).encode('utf-8'),

        # Reliability (same as Java)
        acks='all',  # ProducerConfig.ACKS_CONFIG = "all"
        retries=3,   # ProducerConfig.RETRIES_CONFIG = 3
        enable_idempotence=True,  # ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG

        # Performance (same as Java)
        batch_size=16384,  # ProducerConfig.BATCH_SIZE_CONFIG = 16KB
        linger_ms=10,      # ProducerConfig.LINGER_MS_CONFIG = 10ms
        compression_type='snappy'  # ProducerConfig.COMPRESSION_TYPE_CONFIG
    )


def send_message_sync(producer, key, value):
    """
    Send message synchronously (blocks until complete)
    Compare to Java: producer.send(record).get()
    """
    try:
        # Create message
        message_data = {
            'user': key,
            'action': value,
            'timestamp': datetime.utcnow().isoformat(),
            'source': 'python-cli'
        }

        # Send and wait for response
        future = producer.send(TOPIC_NAME, key=key, value=message_data)
        record_metadata = future.get(timeout=10)  # Synchronous - waits for result

        print(f"✓ Message sent successfully:")
        print(f"  Topic: {record_metadata.topic}")
        print(f"  Partition: {record_metadata.partition}")
        print(f"  Offset: {record_metadata.offset}")
        print(f"  Key: {key}")
        print(f"  Value: {json.dumps(message_data, indent=2)}")

        return True

    except KafkaError as e:
        print(f"❌ Failed to send message: {e}", file=sys.stderr)
        return False


def send_message_async(producer, key, value):
    """
    Send message asynchronously (with callback)
    Compare to Java: producer.send(record, callback)
    """
    message_data = {
        'user': key,
        'action': value,
        'timestamp': datetime.utcnow().isoformat(),
        'source': 'python-cli'
    }

    def on_send_success(record_metadata):
        print(f"✓ Async message sent:")
        print(f"  Partition: {record_metadata.partition}")
        print(f"  Offset: {record_metadata.offset}")

    def on_send_error(exception):
        print(f"❌ Async send failed: {exception}", file=sys.stderr)

    # Send with callback
    producer.send(TOPIC_NAME, key=key, value=message_data).add_callback(
        on_send_success
    ).add_errback(on_send_error)


def main():
    """
    Main demonstration
    """
    print("╔═════════════════════════════════════════════════════════╗")
    print("║  Day 3: Kafka Producer - Python kafka-python           ║")
    print("║  Same concepts as Java • Different language            ║")
    print("╚═════════════════════════════════════════════════════════╝\n")

    print("✓ Producer Configuration:")
    print(f"  Bootstrap Servers: {BOOTSTRAP_SERVERS}")
    print(f"  Topic: {TOPIC_NAME}")
    print(f"  Acks: all")
    print(f"  Idempotence: True")
    print(f"  Compression: snappy\n")

    # Create producer
    producer = create_producer()

    try:
        # Demo 1: Synchronous send
        print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        print("Demo 1: Synchronous Send (blocks until complete)")
        print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        send_message_sync(producer, 'user-123', 'login')

        # Demo 2: Asynchronous send
        print("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        print("Demo 2: Asynchronous Send (non-blocking with callback)")
        print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        send_message_async(producer, 'user-456', 'logout')

        # Demo 3: Batch send
        print("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        print("Demo 3: Batch Send (multiple messages)")
        print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

        actions = ['view_product', 'add_to_cart', 'checkout', 'payment', 'confirm']
        for i, action in enumerate(actions, 1):
            send_message_async(producer, f'user-{i}', action)

        # Flush to ensure all async messages are sent
        producer.flush()

        print("\n╔═════════════════════════════════════════════════════════╗")
        print("║         Day 3 Producer Demo Complete (Python)          ║")
        print("╚═════════════════════════════════════════════════════════╝")
        print("\nKey Takeaways:")
        print("  ✓ Created Kafka producer in Python")
        print("  ✓ Same concepts as Java (acks, idempotence, batching)")
        print("  ✓ Sent messages synchronously and asynchronously")
        print("  ✓ Platform-agnostic Kafka knowledge")
        print("\nNext Steps:")
        print("  → Run Java version: java -cp target/... AdvancedProducer")
        print("  → Run Day 4 Python consumer: python day04_consumer.py")
        print("  → Compare: Same Kafka concepts, different languages!")

    except KeyboardInterrupt:
        print("\n\n⚠ Interrupted by user")
    except Exception as e:
        print(f"\n❌ ERROR: {e}", file=sys.stderr)
        sys.exit(1)
    finally:
        producer.close()
        print("\n✓ Producer closed")


if __name__ == '__main__':
    main()
