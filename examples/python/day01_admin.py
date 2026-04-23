#!/usr/bin/env python3
"""
Day 1: Kafka AdminClient - Python Example

This demonstrates the SAME concepts as the Java AdminClient,
but using Python kafka-python library.

Perfect for data engineers who need to manage Kafka clusters programmatically.

What you'll learn:
- How to create an AdminClient in Python
- Create, list, and describe topics
- Get cluster metadata
- Manage topic configurations
- Delete topics

Run: python examples/python/day01_admin.py
"""

import sys
from kafka.admin import KafkaAdminClient, NewTopic, ConfigResource, ConfigResourceType
from kafka.errors import KafkaError, TopicAlreadyExistsError

# Configuration
BOOTSTRAP_SERVERS = 'localhost:9092'


def create_admin_client():
    """
    Create Kafka AdminClient
    Compare to Java: AdminClient.create(properties)
    """
    return KafkaAdminClient(
        bootstrap_servers=BOOTSTRAP_SERVERS,
        client_id='python-admin-client'
    )


def create_topics(admin_client):
    """
    Create topics with specific configurations
    Compare to Java: adminClient.createTopics(newTopics)
    """
    print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    print("Demo 1: Creating Topics")
    print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

    # Define topics to create
    topics = [
        NewTopic(
            name='test-topic-1',
            num_partitions=3,
            replication_factor=1
        ),
        NewTopic(
            name='test-topic-2',
            num_partitions=6,
            replication_factor=1,
            topic_configs={
                'retention.ms': '86400000',  # 1 day
                'compression.type': 'snappy'
            }
        )
    ]

    try:
        result = admin_client.create_topics(new_topics=topics, validate_only=False)
        print("✓ Topics created successfully:")
        for topic_name in result.topic_errors.keys():
            print(f"  - {topic_name}")
        print()
    except TopicAlreadyExistsError as e:
        print(f"⚠ Topics already exist (OK for demo): {e}\n")
    except KafkaError as e:
        print(f"❌ Failed to create topics: {e}\n", file=sys.stderr)


def list_topics(admin_client):
    """
    List all topics in the cluster
    Compare to Java: adminClient.listTopics().names().get()
    """
    print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    print("Demo 2: Listing All Topics")
    print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

    try:
        topics = admin_client.list_topics()
        print(f"✓ Found {len(topics)} topics:")
        for topic in sorted(topics):
            # Filter out internal topics
            if not topic.startswith('_'):
                print(f"  - {topic}")
        print()
    except KafkaError as e:
        print(f"❌ Failed to list topics: {e}\n", file=sys.stderr)


def describe_topics(admin_client):
    """
    Describe specific topics (get partition info)
    Compare to Java: adminClient.describeTopics(topicNames).all().get()
    """
    print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    print("Demo 3: Describing Topics")
    print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

    try:
        # Get metadata for specific topics
        from kafka import KafkaConsumer
        consumer = KafkaConsumer(bootstrap_servers=BOOTSTRAP_SERVERS)

        topics_to_describe = ['python-demo-topic-1', 'python-demo-topic-2']

        for topic_name in topics_to_describe:
            partitions = consumer.partitions_for_topic(topic_name)
            if partitions:
                print(f"✓ Topic: {topic_name}")
                print(f"  Partitions: {len(partitions)}")
                print(f"  Partition IDs: {sorted(partitions)}")
                print()

        consumer.close()
    except KafkaError as e:
        print(f"❌ Failed to describe topics: {e}\n", file=sys.stderr)


def get_cluster_metadata(admin_client):
    """
    Get cluster metadata
    Compare to Java: adminClient.describeCluster()
    """
    print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    print("Demo 4: Cluster Metadata")
    print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

    try:
        # Get cluster metadata through a consumer connection
        from kafka import KafkaConsumer
        consumer = KafkaConsumer(bootstrap_servers=BOOTSTRAP_SERVERS)

        cluster_metadata = consumer._client.cluster

        print("✓ Cluster Information:")
        print(f"  Cluster ID: {cluster_metadata.cluster_id() or 'N/A'}")
        print(f"  Brokers: {len(cluster_metadata.brokers())}")

        for broker in cluster_metadata.brokers():
            print(f"    - Broker {broker.nodeId}: {broker.host}:{broker.port}")

        print()
        consumer.close()
    except KafkaError as e:
        print(f"❌ Failed to get cluster metadata: {e}\n", file=sys.stderr)


def describe_configs(admin_client):
    """
    Describe topic configurations
    Compare to Java: adminClient.describeConfigs()
    """
    print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    print("Demo 5: Topic Configuration")
    print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

    try:
        topic_name = 'python-demo-topic-2'

        # Create ConfigResource for the topic
        resource = ConfigResource(
            resource_type=ConfigResourceType.TOPIC,
            name=topic_name
        )

        # Describe configs
        configs = admin_client.describe_configs(config_resources=[resource])

        print(f"✓ Configuration for topic: {topic_name}")

        for resource_type, config_entries in configs.items():
            for config_entry in config_entries:
                # Only show non-default configurations
                if not config_entry[1].is_default:
                    print(f"  {config_entry[0]}: {config_entry[1].value}")
        print()

    except KafkaError as e:
        print(f"❌ Failed to describe configs: {e}\n", file=sys.stderr)


def delete_topics(admin_client):
    """
    Delete topics
    Compare to Java: adminClient.deleteTopics(topicNames)
    """
    print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    print("Demo 6: Deleting Topics (cleanup)")
    print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

    topics_to_delete = ['python-demo-topic-1', 'python-demo-topic-2']

    try:
        result = admin_client.delete_topics(topics=topics_to_delete)
        print("✓ Topics deleted successfully:")
        for topic_name in topics_to_delete:
            print(f"  - {topic_name}")
        print()
    except KafkaError as e:
        print(f"⚠ Failed to delete topics (they may not exist): {e}\n")


def main():
    """
    Main demonstration
    """
    print("╔═════════════════════════════════════════════════════════╗")
    print("║  Day 1: Kafka AdminClient - Python kafka-python        ║")
    print("║  Same concepts as Java • Different language            ║")
    print("╚═════════════════════════════════════════════════════════╝\n")

    print("✓ AdminClient Configuration:")
    print(f"  Bootstrap Servers: {BOOTSTRAP_SERVERS}")
    print(f"  Client ID: python-admin-client\n")

    # Create admin client
    admin_client = create_admin_client()

    try:
        # Run all demos
        create_topics(admin_client)
        list_topics(admin_client)
        describe_topics(admin_client)
        get_cluster_metadata(admin_client)
        describe_configs(admin_client)
        delete_topics(admin_client)

        print("╔═════════════════════════════════════════════════════════╗")
        print("║      Day 1 AdminClient Demo Complete (Python)          ║")
        print("╚═════════════════════════════════════════════════════════╝")
        print("\nKey Takeaways:")
        print("  ✓ Created Kafka AdminClient in Python")
        print("  ✓ Same concepts as Java (topics, configs, metadata)")
        print("  ✓ Managed topics programmatically")
        print("  ✓ Platform-agnostic Kafka knowledge")
        print("\nNext Steps:")
        print("  → Use these skills in any Python data pipeline")
        print("  → Integrate with Airflow, Luigi, Prefect")
        print("  → Run Day 2: python day02_dataflow.py")
        print("  → Compare: Same Kafka concepts, different languages!")

    except KeyboardInterrupt:
        print("\n\n⚠ Interrupted by user")
    except Exception as e:
        print(f"\n❌ ERROR: {e}", file=sys.stderr)
        import traceback
        traceback.print_exc()
        sys.exit(1)
    finally:
        admin_client.close()
        print("\n✓ AdminClient closed")


if __name__ == '__main__':
    main()
