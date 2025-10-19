#!/usr/bin/env python3
"""
Day 2: Kafka Data Flow - Python Example

This demonstrates the complete Kafka data flow:
1. Create topic (Admin)
2. Produce messages (Producer)
3. Consume messages (Consumer)

This shows how Kafka works regardless of programming language.

Perfect for data engineers understanding end-to-end data pipelines.

What you'll learn:
- Complete data flow in Kafka
- How producers and consumers interact
- Message delivery guarantees
- Offset management basics

Run: python examples/python/day02_dataflow.py
"""

import json
import sys
import time
from datetime import datetime
from kafka.admin import KafkaAdminClient, NewTopic
from kafka import KafkaProducer, KafkaConsumer
from kafka.errors import TopicAlreadyExistsError, KafkaError

# Configuration
BOOTSTRAP_SERVERS = 'localhost:9092'
TOPIC_NAME = 'python-dataflow-demo'
GROUP_ID = 'python-dataflow-consumer-group'


def step1_create_topic():
    """
    Step 1: Create a topic using AdminClient
    """
    print("╔═════════════════════════════════════════════════════════╗")
    print("║  STEP 1: Create Topic (Admin API)                      ║")
    print("╚═════════════════════════════════════════════════════════╝\n")

    admin_client = KafkaAdminClient(
        bootstrap_servers=BOOTSTRAP_SERVERS,
        client_id='python-admin'
    )

    topic = NewTopic(
        name=TOPIC_NAME,
        num_partitions=3,
        replication_factor=1,
        topic_configs={
            'retention.ms': '3600000',  # 1 hour
            'compression.type': 'snappy'
        }
    )

    try:
        admin_client.create_topics(new_topics=[topic], validate_only=False)
        print(f"✓ Topic created: {TOPIC_NAME}")
        print(f"  Partitions: 3")
        print(f"  Replication Factor: 1")
        print(f"  Retention: 1 hour")
        print(f"  Compression: snappy")
    except TopicAlreadyExistsError:
        print(f"✓ Topic already exists: {TOPIC_NAME} (OK for demo)")
    except Exception as e:
        print(f"❌ Failed to create topic: {e}", file=sys.stderr)
        sys.exit(1)
    finally:
        admin_client.close()

    print()
    time.sleep(1)  # Give Kafka time to propagate


def step2_produce_messages():
    """
    Step 2: Produce messages to the topic
    """
    print("╔═════════════════════════════════════════════════════════╗")
    print("║  STEP 2: Produce Messages (Producer API)               ║")
    print("╚═════════════════════════════════════════════════════════╝\n")

    producer = KafkaProducer(
        bootstrap_servers=BOOTSTRAP_SERVERS,
        client_id='python-producer',
        key_serializer=lambda k: k.encode('utf-8'),
        value_serializer=lambda v: json.dumps(v).encode('utf-8'),
        acks='all',
        retries=3,
        enable_idempotence=True
    )

    # Sample data: user events
    events = [
        {'user_id': 'user-1', 'action': 'login', 'timestamp': datetime.utcnow().isoformat()},
        {'user_id': 'user-2', 'action': 'view_product', 'product_id': 'prod-123', 'timestamp': datetime.utcnow().isoformat()},
        {'user_id': 'user-1', 'action': 'add_to_cart', 'product_id': 'prod-123', 'timestamp': datetime.utcnow().isoformat()},
        {'user_id': 'user-3', 'action': 'login', 'timestamp': datetime.utcnow().isoformat()},
        {'user_id': 'user-2', 'action': 'checkout', 'cart_total': 99.99, 'timestamp': datetime.utcnow().isoformat()},
        {'user_id': 'user-1', 'action': 'checkout', 'cart_total': 149.99, 'timestamp': datetime.utcnow().isoformat()},
        {'user_id': 'user-3', 'action': 'view_product', 'product_id': 'prod-456', 'timestamp': datetime.utcnow().isoformat()},
        {'user_id': 'user-2', 'action': 'logout', 'timestamp': datetime.utcnow().isoformat()},
        {'user_id': 'user-1', 'action': 'logout', 'timestamp': datetime.utcnow().isoformat()},
        {'user_id': 'user-3', 'action': 'logout', 'timestamp': datetime.utcnow().isoformat()},
    ]

    print(f"Producing {len(events)} events to topic: {TOPIC_NAME}\n")

    message_count = 0
    for event in events:
        key = event['user_id']

        try:
            # Send message and wait for confirmation
            future = producer.send(TOPIC_NAME, key=key, value=event)
            record_metadata = future.get(timeout=10)

            message_count += 1
            print(f"  {message_count}. Sent: {event['action']:15} | User: {key} | "
                  f"Partition: {record_metadata.partition} | Offset: {record_metadata.offset}")

        except Exception as e:
            print(f"❌ Failed to send message: {e}", file=sys.stderr)

    producer.flush()
    producer.close()

    print(f"\n✓ Successfully produced {message_count} messages")
    print()
    time.sleep(1)


def step3_consume_messages():
    """
    Step 3: Consume messages from the topic
    """
    print("╔═════════════════════════════════════════════════════════╗")
    print("║  STEP 3: Consume Messages (Consumer API)               ║")
    print("╚═════════════════════════════════════════════════════════╝\n")

    consumer = KafkaConsumer(
        TOPIC_NAME,
        bootstrap_servers=BOOTSTRAP_SERVERS,
        client_id='python-consumer',
        group_id=GROUP_ID,
        key_deserializer=lambda k: k.decode('utf-8') if k else None,
        value_deserializer=lambda v: json.loads(v.decode('utf-8')),
        auto_offset_reset='earliest',  # Read from beginning
        enable_auto_commit=False,  # Manual commit for demo
        consumer_timeout_ms=5000  # Exit after 5 seconds of no messages
    )

    print(f"Consuming messages from topic: {TOPIC_NAME}")
    print(f"Consumer Group: {GROUP_ID}\n")

    message_count = 0
    try:
        for message in consumer:
            message_count += 1

            print(f"  {message_count}. Received: {message.value['action']:15} | "
                  f"User: {message.key} | Partition: {message.partition} | "
                  f"Offset: {message.offset}")

            # Process message (your business logic here)
            # ...

            # Manual commit after processing
            consumer.commit()

    except Exception as e:
        if 'timeout' not in str(e).lower():
            print(f"❌ Consumer error: {e}", file=sys.stderr)

    finally:
        consumer.close()

    print(f"\n✓ Successfully consumed {message_count} messages")
    print()


def step4_demonstrate_partitioning():
    """
    Step 4: Demonstrate how partitioning works
    """
    print("╔═════════════════════════════════════════════════════════╗")
    print("║  STEP 4: Understanding Partitioning                    ║")
    print("╚═════════════════════════════════════════════════════════╝\n")

    print("Key Observations:")
    print("  ✓ Messages with same key go to same partition")
    print("  ✓ This ensures ordering for events from the same user")
    print("  ✓ Different partitions can be consumed in parallel")
    print()

    print("Example from our data:")
    print("  • user-1 events → Partition X (all in order)")
    print("  • user-2 events → Partition Y (all in order)")
    print("  • user-3 events → Partition Z (all in order)")
    print()

    print("Benefits:")
    print("  ✓ Parallelism: Multiple consumers process different partitions")
    print("  ✓ Ordering: Events for same key are ordered within partition")
    print("  ✓ Scalability: Add partitions = add parallel consumers")
    print()


def step5_cleanup():
    """
    Step 5: Cleanup (optional - delete topic)
    """
    print("╔═════════════════════════════════════════════════════════╗")
    print("║  STEP 5: Cleanup (Optional)                            ║")
    print("╚═════════════════════════════════════════════════════════╝\n")

    response = input(f"Delete topic '{TOPIC_NAME}'? (y/N): ").strip().lower()

    if response == 'y':
        admin_client = KafkaAdminClient(
            bootstrap_servers=BOOTSTRAP_SERVERS,
            client_id='python-admin'
        )

        try:
            admin_client.delete_topics(topics=[TOPIC_NAME])
            print(f"✓ Topic deleted: {TOPIC_NAME}")
        except Exception as e:
            print(f"⚠ Failed to delete topic: {e}")
        finally:
            admin_client.close()
    else:
        print(f"✓ Keeping topic '{TOPIC_NAME}' for further exploration")

    print()


def main():
    """
    Main demonstration - Complete Kafka data flow
    """
    print("\n╔═════════════════════════════════════════════════════════╗")
    print("║                                                         ║")
    print("║  Day 2: Complete Kafka Data Flow - Python              ║")
    print("║                                                         ║")
    print("║  Platform-Agnostic Kafka Concepts                      ║")
    print("║  Same whether you use Java, Python, Go, Scala          ║")
    print("║                                                         ║")
    print("╚═════════════════════════════════════════════════════════╝\n")

    print("Configuration:")
    print(f"  Bootstrap Servers: {BOOTSTRAP_SERVERS}")
    print(f"  Topic: {TOPIC_NAME}")
    print(f"  Consumer Group: {GROUP_ID}\n")

    print("This demo will show you:")
    print("  1. How to create topics programmatically")
    print("  2. How to produce messages with keys")
    print("  3. How to consume messages with manual commits")
    print("  4. How partitioning ensures ordering")
    print()

    input("Press Enter to start the demo...")
    print()

    try:
        # Run complete flow
        step1_create_topic()
        step2_produce_messages()
        step3_consume_messages()
        step4_demonstrate_partitioning()
        step5_cleanup()

        print("╔═════════════════════════════════════════════════════════╗")
        print("║     Day 2 Complete Data Flow Demo Complete (Python)    ║")
        print("╚═════════════════════════════════════════════════════════╝")
        print("\nKey Takeaways:")
        print("  ✓ Understood complete Kafka data flow")
        print("  ✓ Admin → Producer → Consumer interaction")
        print("  ✓ Message keys ensure ordering within partitions")
        print("  ✓ Platform-agnostic concepts (work in any language)")
        print()
        print("These concepts apply to:")
        print("  → Java applications with Spring Kafka")
        print("  → Python data pipelines with Airflow")
        print("  → Scala/Spark streaming jobs")
        print("  → Go microservices")
        print("  → Node.js real-time applications")
        print()
        print("Next Steps:")
        print("  → Run Day 3: python day03_producer.py (deep dive on producers)")
        print("  → Run Day 4: python day04_consumer.py (deep dive on consumers)")
        print("  → Apply these concepts in your data engineering projects!")

    except KeyboardInterrupt:
        print("\n\n⚠ Demo interrupted by user")
    except Exception as e:
        print(f"\n❌ ERROR: {e}", file=sys.stderr)
        import traceback
        traceback.print_exc()
        sys.exit(1)


if __name__ == '__main__':
    main()
