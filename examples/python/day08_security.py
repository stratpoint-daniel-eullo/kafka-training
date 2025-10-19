#!/usr/bin/env python3
"""
Day 8: Kafka Security - Python Example

This demonstrates security configurations for Kafka:
- SSL/TLS encryption
- SASL authentication (PLAIN, SCRAM-SHA-512)
- Combined SSL + SASL

Perfect for data engineers deploying to production environments.

What you'll learn:
- How to configure SSL/TLS in Python
- Different SASL mechanisms
- Security best practices
- Compare to Java security configurations

NOTE: This is a demonstration script showing configurations.
      To run it, you need a Kafka cluster with security enabled.

Run: python examples/python/day08_security.py
"""

import sys
from kafka import KafkaProducer, KafkaConsumer
from kafka.admin import KafkaAdminClient


def print_security_config_ssl():
    """
    Demonstrate SSL/TLS configuration
    Compare to Java: props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL")
    """
    print("╔═════════════════════════════════════════════════════════╗")
    print("║  Configuration 1: SSL/TLS Encryption                   ║")
    print("╚═════════════════════════════════════════════════════════╝\n")

    ssl_config = {
        'bootstrap_servers': 'localhost:9093',  # SSL port
        'security_protocol': 'SSL',

        # SSL/TLS settings
        'ssl_cafile': '/path/to/ca-cert',  # Certificate Authority cert
        'ssl_certfile': '/path/to/client-cert',  # Client certificate
        'ssl_keyfile': '/path/to/client-key',  # Client key

        # Optional: SSL/TLS settings
        'ssl_check_hostname': True,
        'ssl_password': 'your-key-password',  # If key is encrypted

        # Producer/Consumer specific settings
        'acks': 'all',
        'retries': 3,
    }

    print("SSL/TLS Configuration (kafka-python):")
    print("=" * 60)
    for key, value in ssl_config.items():
        if 'password' not in key.lower():
            print(f"  {key}: {value}")
        else:
            print(f"  {key}: ********")
    print()

    print("Compare to Java:")
    print("-" * 60)
    print("  props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, \"SSL\");")
    print("  props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, \"/path/to/truststore.jks\");")
    print("  props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, \"password\");")
    print("  props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, \"/path/to/keystore.jks\");")
    print("  props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, \"password\");")
    print("  props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, \"password\");")
    print()


def print_security_config_sasl_plain():
    """
    Demonstrate SASL/PLAIN configuration
    Compare to Java: SASL_JAAS_CONFIG with PlainLoginModule
    """
    print("╔═════════════════════════════════════════════════════════╗")
    print("║  Configuration 2: SASL/PLAIN Authentication            ║")
    print("╚═════════════════════════════════════════════════════════╝\n")

    sasl_plain_config = {
        'bootstrap_servers': 'localhost:9094',  # SASL port
        'security_protocol': 'SASL_PLAINTEXT',  # Or SASL_SSL for encryption

        # SASL settings
        'sasl_mechanism': 'PLAIN',
        'sasl_plain_username': 'kafka-user',
        'sasl_plain_password': 'kafka-password',

        # Producer/Consumer specific settings
        'acks': 'all',
        'retries': 3,
    }

    print("SASL/PLAIN Configuration (kafka-python):")
    print("=" * 60)
    for key, value in sasl_plain_config.items():
        if 'password' not in key.lower():
            print(f"  {key}: {value}")
        else:
            print(f"  {key}: ********")
    print()

    print("Compare to Java:")
    print("-" * 60)
    print("  props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, \"SASL_PLAINTEXT\");")
    print("  props.put(SaslConfigs.SASL_MECHANISM, \"PLAIN\");")
    print("  props.put(SaslConfigs.SASL_JAAS_CONFIG,")
    print("    \"org.apache.kafka.common.security.plain.PlainLoginModule required \" +")
    print("    \"username='kafka-user' password='kafka-password';\");")
    print()


def print_security_config_sasl_scram():
    """
    Demonstrate SASL/SCRAM-SHA-512 configuration
    Compare to Java: SASL_JAAS_CONFIG with ScramLoginModule
    """
    print("╔═════════════════════════════════════════════════════════╗")
    print("║  Configuration 3: SASL/SCRAM-SHA-512 Authentication    ║")
    print("╚═════════════════════════════════════════════════════════╝\n")

    sasl_scram_config = {
        'bootstrap_servers': 'localhost:9094',
        'security_protocol': 'SASL_SSL',  # Combined SASL + SSL

        # SASL SCRAM settings
        'sasl_mechanism': 'SCRAM-SHA-512',
        'sasl_plain_username': 'kafka-user',
        'sasl_plain_password': 'strong-password',

        # SSL settings (for SASL_SSL)
        'ssl_cafile': '/path/to/ca-cert',

        # Producer/Consumer specific settings
        'acks': 'all',
        'retries': 3,
    }

    print("SASL/SCRAM-SHA-512 Configuration (kafka-python):")
    print("=" * 60)
    for key, value in sasl_scram_config.items():
        if 'password' not in key.lower():
            print(f"  {key}: {value}")
        else:
            print(f"  {key}: ********")
    print()

    print("Compare to Java:")
    print("-" * 60)
    print("  props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, \"SASL_SSL\");")
    print("  props.put(SaslConfigs.SASL_MECHANISM, \"SCRAM-SHA-512\");")
    print("  props.put(SaslConfigs.SASL_JAAS_CONFIG,")
    print("    \"org.apache.kafka.common.security.scram.ScramLoginModule required \" +")
    print("    \"username='kafka-user' password='strong-password';\");")
    print()


def print_security_config_confluent_cloud():
    """
    Demonstrate configuration for Confluent Cloud (managed Kafka)
    This is a common production scenario
    """
    print("╔═════════════════════════════════════════════════════════╗")
    print("║  Configuration 4: Confluent Cloud (Managed Kafka)      ║")
    print("╚═════════════════════════════════════════════════════════╝\n")

    # Using confluent-kafka library for Confluent Cloud
    confluent_config = {
        'bootstrap.servers': 'pkc-xxxxx.us-west-2.aws.confluent.cloud:9092',
        'security.protocol': 'SASL_SSL',
        'sasl.mechanisms': 'PLAIN',
        'sasl.username': 'API_KEY',
        'sasl.password': 'API_SECRET',

        # Additional settings
        'client.id': 'python-client',
        'acks': 'all',
        'retries': 3,
        'enable.idempotence': True,
    }

    print("Confluent Cloud Configuration (confluent-kafka):")
    print("=" * 60)
    for key, value in confluent_config.items():
        if 'password' not in key.lower() and 'secret' not in key.lower():
            print(f"  {key}: {value}")
        else:
            print(f"  {key}: ********")
    print()

    print("Note: confluent-kafka uses Java-style property names")
    print()


def demonstrate_producer_with_ssl():
    """
    Show how to create a producer with SSL configuration
    """
    print("╔═════════════════════════════════════════════════════════╗")
    print("║  Example: Creating SSL Producer                        ║")
    print("╚═════════════════════════════════════════════════════════╝\n")

    print("Python code example:")
    print("-" * 60)
    print("""
from kafka import KafkaProducer

producer = KafkaProducer(
    bootstrap_servers='localhost:9093',
    security_protocol='SSL',
    ssl_cafile='/path/to/ca-cert',
    ssl_certfile='/path/to/client-cert',
    ssl_keyfile='/path/to/client-key',
    ssl_check_hostname=True,

    # Serialization
    key_serializer=lambda k: k.encode('utf-8'),
    value_serializer=lambda v: v.encode('utf-8'),

    # Reliability
    acks='all',
    retries=3,
    enable_idempotence=True
)

# Send message
producer.send('secure-topic', key='key1', value='secure message')
producer.flush()
producer.close()
""")
    print()


def demonstrate_consumer_with_sasl():
    """
    Show how to create a consumer with SASL configuration
    """
    print("╔═════════════════════════════════════════════════════════╗")
    print("║  Example: Creating SASL Consumer                       ║")
    print("╚═════════════════════════════════════════════════════════╝\n")

    print("Python code example:")
    print("-" * 60)
    print("""
from kafka import KafkaConsumer

consumer = KafkaConsumer(
    'secure-topic',
    bootstrap_servers='localhost:9094',
    security_protocol='SASL_SSL',
    sasl_mechanism='SCRAM-SHA-512',
    sasl_plain_username='kafka-user',
    sasl_plain_password='strong-password',
    ssl_cafile='/path/to/ca-cert',

    # Consumer settings
    group_id='secure-consumer-group',
    auto_offset_reset='earliest',
    enable_auto_commit=False,

    # Deserialization
    key_deserializer=lambda k: k.decode('utf-8'),
    value_deserializer=lambda v: v.decode('utf-8')
)

# Consume messages
for message in consumer:
    print(f"Received: {message.value}")
    consumer.commit()
""")
    print()


def print_security_best_practices():
    """
    Print security best practices
    """
    print("╔═════════════════════════════════════════════════════════╗")
    print("║  Security Best Practices                               ║")
    print("╚═════════════════════════════════════════════════════════╝\n")

    practices = [
        ("Always use encryption in production", "Use SSL/TLS (security.protocol=SSL or SASL_SSL)"),
        ("Use SCRAM over PLAIN when possible", "SCRAM-SHA-512 is more secure than PLAIN"),
        ("Never commit credentials to git", "Use environment variables or secret managers"),
        ("Enable ACLs for authorization", "Control who can read/write to which topics"),
        ("Use separate users per application", "Easier to audit and revoke access"),
        ("Rotate credentials regularly", "Implement credential rotation policy"),
        ("Enable SSL hostname verification", "Prevents man-in-the-middle attacks"),
        ("Use TLS 1.2 or 1.3 only", "Disable older, vulnerable TLS versions"),
        ("Monitor security logs", "Watch for authentication failures"),
        ("Use Confluent Cloud or MSK", "Managed services handle security for you"),
    ]

    for idx, (practice, detail) in enumerate(practices, 1):
        print(f"{idx:2}. {practice}")
        print(f"    → {detail}\n")


def print_environment_variables_example():
    """
    Show how to use environment variables for credentials
    """
    print("╔═════════════════════════════════════════════════════════╗")
    print("║  Environment Variables for Credentials (Best Practice) ║")
    print("╚═════════════════════════════════════════════════════════╝\n")

    print("Python code example:")
    print("-" * 60)
    print("""
import os
from kafka import KafkaProducer

# Get credentials from environment
KAFKA_BOOTSTRAP_SERVERS = os.getenv('KAFKA_BOOTSTRAP_SERVERS', 'localhost:9094')
KAFKA_USERNAME = os.getenv('KAFKA_USERNAME')
KAFKA_PASSWORD = os.getenv('KAFKA_PASSWORD')
KAFKA_CA_CERT = os.getenv('KAFKA_CA_CERT_PATH')

producer = KafkaProducer(
    bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS,
    security_protocol='SASL_SSL',
    sasl_mechanism='SCRAM-SHA-512',
    sasl_plain_username=KAFKA_USERNAME,
    sasl_plain_password=KAFKA_PASSWORD,
    ssl_cafile=KAFKA_CA_CERT,
    acks='all'
)
""")
    print()

    print("Set environment variables:")
    print("-" * 60)
    print("  export KAFKA_BOOTSTRAP_SERVERS='localhost:9094'")
    print("  export KAFKA_USERNAME='kafka-user'")
    print("  export KAFKA_PASSWORD='your-password'")
    print("  export KAFKA_CA_CERT_PATH='/path/to/ca-cert'")
    print()


def main():
    """
    Main demonstration
    """
    print("\n╔═════════════════════════════════════════════════════════╗")
    print("║                                                         ║")
    print("║  Day 8: Kafka Security Configurations (Python)         ║")
    print("║                                                         ║")
    print("║  Production-Ready Security Patterns                    ║")
    print("║  Same concepts across Java, Python, Go, etc.           ║")
    print("║                                                         ║")
    print("╚═════════════════════════════════════════════════════════╝\n")

    print("This demonstration covers:")
    print("  1. SSL/TLS encryption configuration")
    print("  2. SASL/PLAIN authentication")
    print("  3. SASL/SCRAM-SHA-512 authentication")
    print("  4. Confluent Cloud configuration")
    print("  5. Security best practices")
    print()

    print("NOTE: This is a configuration reference.")
    print("      To run with actual security, you need:")
    print("      - Kafka broker with security enabled")
    print("      - SSL certificates (if using SSL/TLS)")
    print("      - User credentials configured in Kafka")
    print()

    input("Press Enter to view security configurations...")
    print()

    try:
        # Show all configurations
        print_security_config_ssl()
        print_security_config_sasl_plain()
        print_security_config_sasl_scram()
        print_security_config_confluent_cloud()
        demonstrate_producer_with_ssl()
        demonstrate_consumer_with_sasl()
        print_environment_variables_example()
        print_security_best_practices()

        print("╔═════════════════════════════════════════════════════════╗")
        print("║      Day 8 Security Configurations Complete (Python)   ║")
        print("╚═════════════════════════════════════════════════════════╝")
        print("\nKey Takeaways:")
        print("  ✓ Learned SSL/TLS encryption configuration")
        print("  ✓ Understood SASL authentication mechanisms")
        print("  ✓ Saw production-ready security patterns")
        print("  ✓ Same concepts as Java (just different syntax)")
        print()
        print("Security Layers:")
        print("  → Encryption: Protects data in transit")
        print("  → Authentication: Verifies client identity")
        print("  → Authorization: Controls access (ACLs)")
        print("  → Audit: Logs who did what")
        print()
        print("Production Deployment:")
        print("  → Use managed Kafka (Confluent Cloud, AWS MSK)")
        print("  → Or set up SSL + SASL + ACLs yourself")
        print("  → Never use plaintext in production!")
        print("  → Store credentials in secret managers")
        print()
        print("Next Steps:")
        print("  → Compare with Java SecurityConfig.java")
        print("  → Set up test Kafka cluster with security")
        print("  → Practice credential rotation")
        print("  → Deploy to production with confidence!")
        print()
        print("Complete Training Finished!")
        print("  You now have platform-agnostic Kafka knowledge")
        print("  that works across Java, Python, Scala, Go, etc.")

    except KeyboardInterrupt:
        print("\n\n⚠ Interrupted by user")
    except Exception as e:
        print(f"\n❌ ERROR: {e}", file=sys.stderr)
        import traceback
        traceback.print_exc()
        sys.exit(1)


if __name__ == '__main__':
    main()
