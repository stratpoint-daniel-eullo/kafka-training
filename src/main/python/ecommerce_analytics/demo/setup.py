"""
Platform Setup Script

Sets up the E-Commerce Analytics Platform for demo.
Creates topics, initializes schemas, and validates configuration.
"""

import logging
from typing import Dict, Any

from ecommerce_analytics.admin.topic_manager import TopicManager
from ecommerce_analytics.config.settings import get_settings

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)

logger = logging.getLogger(__name__)


def setup_platform(
    recreate_topics: bool = False,
    validate_schemas: bool = True
) -> Dict[str, Any]:
    """
    Setup the E-Commerce Analytics Platform.

    Args:
        recreate_topics: If True, delete and recreate all topics
        validate_schemas: If True, validate Avro schemas

    Returns:
        Dictionary with setup results
    """
    logger.info("=" * 60)
    logger.info("E-Commerce Analytics Platform Setup")
    logger.info("=" * 60)

    results = {
        "topics_created": {},
        "topics_deleted": {},
        "schemas_validated": {},
        "success": False,
    }

    settings = get_settings()

    try:
        # Step 1: Create or recreate topics
        with TopicManager() as topic_manager:
            logger.info("\nStep 1: Managing Kafka topics...")

            if recreate_topics:
                logger.info("Deleting existing topics...")
                deleted = topic_manager.delete_all_topics()
                results["topics_deleted"] = deleted

                logger.info("Waiting for topic deletion to propagate...")
                import time
                time.sleep(5)

            # Create topics
            logger.info("Creating topics...")
            created = topic_manager.create_all_topics()
            results["topics_created"] = created

            # List all topics
            all_topics = topic_manager.list_topics()
            logger.info(f"\nAll topics in cluster: {len(all_topics)}")
            for topic in sorted(all_topics):
                if not topic.startswith("_"):  # Skip internal topics
                    logger.info(f"  - {topic}")

        # Step 2: Validate Avro schemas
        if validate_schemas:
            logger.info("\nStep 2: Validating Avro schemas...")

            schema_files = [
                "user_events.avsc",
                "order_events.avsc",
                "product_events.avsc",
                "payment_events.avsc",
            ]

            for schema_file in schema_files:
                try:
                    schema_path = settings.get_schema_path(schema_file)

                    with open(schema_path, 'r') as f:
                        import json
                        schema_json = json.load(f)

                    logger.info(f"  ✓ {schema_file}: {schema_json.get('name')}")
                    results["schemas_validated"][schema_file] = True

                except Exception as e:
                    logger.error(f"  ✗ {schema_file}: {e}")
                    results["schemas_validated"][schema_file] = False

        # Step 3: Summary
        logger.info("\n" + "=" * 60)
        logger.info("Setup Summary")
        logger.info("=" * 60)

        topics_ok = sum(1 for v in results["topics_created"].values() if v)
        logger.info(f"Topics created: {topics_ok}/{len(results['topics_created'])}")

        if validate_schemas:
            schemas_ok = sum(1 for v in results["schemas_validated"].values() if v)
            logger.info(f"Schemas validated: {schemas_ok}/{len(results['schemas_validated'])}")

        # Check overall success
        results["success"] = (
            topics_ok == len(results["topics_created"]) and
            (not validate_schemas or schemas_ok == len(results["schemas_validated"]))
        )

        if results["success"]:
            logger.info("\n✓ Platform setup completed successfully!")
        else:
            logger.warning("\n⚠ Platform setup completed with warnings")

        logger.info("\nNext steps:")
        logger.info("  1. Run producers to generate events")
        logger.info("  2. Start consumers to process events")
        logger.info("  3. Launch Faust stream processors")
        logger.info("  4. Monitor with Prometheus metrics")

        logger.info("=" * 60)

        return results

    except Exception as e:
        logger.error(f"Setup failed: {e}")
        results["success"] = False
        results["error"] = str(e)
        return results


if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser(
        description="Setup E-Commerce Analytics Platform"
    )
    parser.add_argument(
        "--recreate-topics",
        action="store_true",
        help="Delete and recreate all topics"
    )
    parser.add_argument(
        "--skip-schema-validation",
        action="store_true",
        help="Skip Avro schema validation"
    )

    args = parser.parse_args()

    setup_platform(
        recreate_topics=args.recreate_topics,
        validate_schemas=not args.skip_schema_validation
    )
