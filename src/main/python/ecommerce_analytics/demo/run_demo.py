"""
Demo Runner

Runs a complete demo of the E-Commerce Analytics Platform.
Generates sample data and demonstrates all Days 1-8 concepts.
"""

import logging
import time
import argparse
from typing import Dict, Any

from ecommerce_analytics.producers.user_event_producer import UserEventProducer
from ecommerce_analytics.producers.order_event_producer import OrderEventProducer
from ecommerce_analytics.producers.product_event_producer import ProductEventProducer
from ecommerce_analytics.producers.payment_event_producer import PaymentEventProducer
from ecommerce_analytics.utils.sample_data_generator import SampleDataGenerator
from ecommerce_analytics.monitoring.metrics import get_metrics

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)

logger = logging.getLogger(__name__)


def run_demo(
    num_events: int = 100,
    delay_seconds: float = 0.5,
    enable_metrics: bool = True
) -> Dict[str, Any]:
    """
    Run E-Commerce Analytics Platform demo.

    Args:
        num_events: Number of events to generate per type
        delay_seconds: Delay between events
        enable_metrics: Enable Prometheus metrics

    Returns:
        Dictionary with demo results
    """
    logger.info("=" * 60)
    logger.info("E-Commerce Analytics Platform Demo")
    logger.info("=" * 60)

    results = {
        "events_produced": 0,
        "success": False,
    }

    try:
        # Step 1: Initialize metrics (Day 8)
        if enable_metrics:
            logger.info("\nInitializing Prometheus metrics...")
            metrics = get_metrics()
            metrics.start_server(port=9090)
            logger.info("Metrics available at: http://localhost:9090/metrics")

        # Step 2: Initialize data generator
        logger.info("\nInitializing sample data generator...")
        generator = SampleDataGenerator(seed=42)

        # Step 3: Initialize producers (Days 3 and 5)
        logger.info("\nInitializing producers...")

        with UserEventProducer() as user_producer, \
             OrderEventProducer() as order_producer, \
             ProductEventProducer() as product_producer, \
             PaymentEventProducer() as payment_producer:

            logger.info("All producers initialized")

            # Step 4: Generate and produce events
            logger.info(f"\nGenerating {num_events} events of each type...")
            logger.info(f"Delay between events: {delay_seconds}s")
            logger.info("")

            for i in range(num_events):
                # User events
                user_data = generator.generate_user_registration()
                user_producer.produce_user_registered(
                    user_id=user_data["user_id"],
                    email=user_data["email"],
                    username=user_data["username"],
                    country=user_data["country"],
                )
                results["events_produced"] += 1

                # Product events
                product_data = generator.generate_product_creation()
                product_producer.produce_product_created(**product_data)
                results["events_produced"] += 1

                # Order events
                order_data = generator.generate_order()
                order_producer.produce_order_placed(**order_data)
                results["events_produced"] += 1

                # Payment events
                payment_data = generator.generate_payment(order_data)
                payment_producer.produce_payment_initiated(**payment_data)
                results["events_produced"] += 1

                # Occasionally complete/fail payments
                if i % 5 == 0:
                    payment_producer.produce_payment_completed(
                        payment_id=payment_data["payment_id"],
                        order_id=payment_data["order_id"],
                        user_id=payment_data["user_id"],
                        amount=payment_data["amount"],
                        payment_method=payment_data["payment_method"],
                    )
                    results["events_produced"] += 1

                elif i % 11 == 0:
                    payment_producer.produce_payment_failed(
                        payment_id=payment_data["payment_id"],
                        order_id=payment_data["order_id"],
                        user_id=payment_data["user_id"],
                        amount=payment_data["amount"],
                        payment_method=payment_data["payment_method"],
                        failure_reason="Insufficient funds",
                    )
                    results["events_produced"] += 1

                # Progress indicator
                if (i + 1) % 10 == 0:
                    logger.info(f"Progress: {i + 1}/{num_events} iterations")

                # Delay between events
                time.sleep(delay_seconds)

            logger.info(f"\nGenerated {results['events_produced']} events total")

            # Step 5: Flush all producers
            logger.info("\nFlushing producers...")
            # Producers auto-flush on context manager exit

        # Step 6: Summary
        logger.info("\n" + "=" * 60)
        logger.info("Demo Summary")
        logger.info("=" * 60)
        logger.info(f"Total events produced: {results['events_produced']}")
        logger.info(f"Event types: User, Product, Order, Payment")
        logger.info("")
        logger.info("Topics populated:")
        logger.info("  - user-events")
        logger.info("  - product-events")
        logger.info("  - order-events")
        logger.info("  - payment-events")
        logger.info("")
        logger.info("Next steps:")
        logger.info("  1. Start consumers to process events")
        logger.info("  2. Launch Faust stream processors")
        logger.info("  3. Check Prometheus metrics at http://localhost:9090/metrics")
        logger.info("  4. Query consumer lag with lag monitor")
        logger.info("=" * 60)

        results["success"] = True
        return results

    except KeyboardInterrupt:
        logger.info("\nDemo stopped by user")
        results["success"] = False
        return results

    except Exception as e:
        logger.error(f"Demo failed: {e}")
        results["success"] = False
        results["error"] = str(e)
        return results


if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description="Run E-Commerce Analytics Platform Demo"
    )
    parser.add_argument(
        "--events",
        type=int,
        default=100,
        help="Number of events to generate per type (default: 100)"
    )
    parser.add_argument(
        "--delay",
        type=float,
        default=0.5,
        help="Delay between events in seconds (default: 0.5)"
    )
    parser.add_argument(
        "--no-metrics",
        action="store_true",
        help="Disable Prometheus metrics"
    )

    args = parser.parse_args()

    run_demo(
        num_events=args.events,
        delay_seconds=args.delay,
        enable_metrics=not args.no_metrics
    )
