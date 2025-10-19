#!/usr/bin/env python3
"""
Day 7: Kafka Connect REST API Client - Python Example

This demonstrates managing Kafka Connect connectors using the REST API.
This is completely platform-agnostic - same API whether you use Java, Python, or curl!

Perfect for data engineers who need to manage data pipelines programmatically.

What you'll learn:
- How to manage Kafka Connect via REST API
- Create, configure, and monitor connectors
- Handle connector lifecycle
- Platform-agnostic integration patterns

Requirements:
    pip install requests

Run: python examples/python/day07_connect_client.py
"""

import sys
import json
import requests
from typing import Dict, List, Optional

# Configuration
CONNECT_URL = 'http://localhost:8083'


class KafkaConnectClient:
    """
    Kafka Connect REST API Client
    This is platform-agnostic - same REST API from any language!
    """

    def __init__(self, connect_url: str = CONNECT_URL):
        self.connect_url = connect_url
        self.headers = {'Content-Type': 'application/json'}

    def _request(self, method: str, endpoint: str, data: Optional[Dict] = None):
        """Helper method for making HTTP requests"""
        url = f"{self.connect_url}{endpoint}"

        try:
            if method == 'GET':
                response = requests.get(url, headers=self.headers)
            elif method == 'POST':
                response = requests.post(url, json=data, headers=self.headers)
            elif method == 'PUT':
                response = requests.put(url, json=data, headers=self.headers)
            elif method == 'DELETE':
                response = requests.delete(url, headers=self.headers)
            else:
                raise ValueError(f"Unsupported method: {method}")

            response.raise_for_status()
            return response.json() if response.text else None

        except requests.exceptions.RequestException as e:
            print(f"❌ Request failed: {e}", file=sys.stderr)
            if hasattr(e.response, 'text'):
                print(f"   Response: {e.response.text}", file=sys.stderr)
            return None

    # Connector Management
    def list_connectors(self) -> Optional[List[str]]:
        """List all connectors"""
        return self._request('GET', '/connectors')

    def get_connector(self, name: str) -> Optional[Dict]:
        """Get connector configuration"""
        return self._request('GET', f'/connectors/{name}')

    def create_connector(self, config: Dict) -> Optional[Dict]:
        """Create a new connector"""
        return self._request('POST', '/connectors', config)

    def update_connector(self, name: str, config: Dict) -> Optional[Dict]:
        """Update connector configuration"""
        return self._request('PUT', f'/connectors/{name}/config', config)

    def delete_connector(self, name: str) -> bool:
        """Delete a connector"""
        result = self._request('DELETE', f'/connectors/{name}')
        return result is not None

    # Connector Status
    def get_connector_status(self, name: str) -> Optional[Dict]:
        """Get connector status"""
        return self._request('GET', f'/connectors/{name}/status')

    def restart_connector(self, name: str) -> bool:
        """Restart a connector"""
        result = self._request('POST', f'/connectors/{name}/restart')
        return result is not None

    def pause_connector(self, name: str) -> bool:
        """Pause a connector"""
        result = self._request('PUT', f'/connectors/{name}/pause')
        return result is not None

    def resume_connector(self, name: str) -> bool:
        """Resume a paused connector"""
        result = self._request('PUT', f'/connectors/{name}/resume')
        return result is not None

    # Task Management
    def get_connector_tasks(self, name: str) -> Optional[List[Dict]]:
        """Get connector tasks"""
        return self._request('GET', f'/connectors/{name}/tasks')

    def restart_task(self, connector_name: str, task_id: int) -> bool:
        """Restart a specific task"""
        result = self._request('POST', f'/connectors/{connector_name}/tasks/{task_id}/restart')
        return result is not None

    # Plugins
    def list_connector_plugins(self) -> Optional[List[Dict]]:
        """List available connector plugins"""
        return self._request('GET', '/connector-plugins')

    def validate_connector_config(self, plugin_name: str, config: Dict) -> Optional[Dict]:
        """Validate connector configuration"""
        return self._request('PUT', f'/connector-plugins/{plugin_name}/config/validate', config)

    # Cluster Info
    def get_cluster_info(self) -> Optional[Dict]:
        """Get Connect cluster information"""
        return self._request('GET', '/')


def demo_step1_check_connect():
    """Step 1: Check if Kafka Connect is running"""
    print("╔═════════════════════════════════════════════════════════╗")
    print("║  STEP 1: Check Kafka Connect Cluster                   ║")
    print("╚═════════════════════════════════════════════════════════╝\n")

    client = KafkaConnectClient()
    info = client.get_cluster_info()

    if info:
        print("✓ Kafka Connect is running:")
        print(f"  Version: {info.get('version', 'N/A')}")
        print(f"  Commit: {info.get('commit', 'N/A')}")
        print(f"  Kafka Cluster ID: {info.get('kafka_cluster_id', 'N/A')}")
    else:
        print("❌ Kafka Connect is not running!")
        print(f"   Make sure Connect is available at {CONNECT_URL}")
        sys.exit(1)

    print()


def demo_step2_list_plugins(client: KafkaConnectClient):
    """Step 2: List available connector plugins"""
    print("╔═════════════════════════════════════════════════════════╗")
    print("║  STEP 2: List Available Connector Plugins              ║")
    print("╚═════════════════════════════════════════════════════════╝\n")

    plugins = client.list_connector_plugins()

    if plugins:
        print(f"✓ Found {len(plugins)} connector plugins:")
        for plugin in plugins[:5]:  # Show first 5
            print(f"  • {plugin['class']}")
            print(f"    Type: {plugin['type']}")
            print(f"    Version: {plugin.get('version', 'N/A')}")
            print()

        if len(plugins) > 5:
            print(f"  ... and {len(plugins) - 5} more")
    else:
        print("❌ Could not retrieve connector plugins")

    print()


def demo_step3_create_file_source_connector(client: KafkaConnectClient):
    """Step 3: Create a FileStreamSource connector (example)"""
    print("╔═════════════════════════════════════════════════════════╗")
    print("║  STEP 3: Create File Source Connector (Example)        ║")
    print("╚═════════════════════════════════════════════════════════╝\n")

    connector_config = {
        "name": "python-file-source-demo",
        "config": {
            "connector.class": "org.apache.kafka.connect.file.FileStreamSourceConnector",
            "tasks.max": "1",
            "file": "/tmp/kafka-connect-test-input.txt",
            "topic": "connect-test-topic",
            "key.converter": "org.apache.kafka.connect.storage.StringConverter",
            "value.converter": "org.apache.kafka.connect.storage.StringConverter"
        }
    }

    print("Creating connector with configuration:")
    print(json.dumps(connector_config, indent=2))
    print()

    result = client.create_connector(connector_config)

    if result:
        print("✓ Connector created successfully:")
        print(f"  Name: {result.get('name')}")
        print(f"  Tasks: {result.get('config', {}).get('tasks.max')}")
    else:
        print("⚠ Connector already exists or creation failed (OK for demo)")

    print()


def demo_step4_list_connectors(client: KafkaConnectClient):
    """Step 4: List all connectors"""
    print("╔═════════════════════════════════════════════════════════╗")
    print("║  STEP 4: List All Connectors                           ║")
    print("╚═════════════════════════════════════════════════════════╝\n")

    connectors = client.list_connectors()

    if connectors:
        print(f"✓ Found {len(connectors)} connectors:")
        for connector_name in connectors:
            print(f"  • {connector_name}")
    else:
        print("  No connectors found")

    print()


def demo_step5_get_connector_status(client: KafkaConnectClient, connector_name: str):
    """Step 5: Get connector status"""
    print("╔═════════════════════════════════════════════════════════╗")
    print("║  STEP 5: Get Connector Status                          ║")
    print("╚═════════════════════════════════════════════════════════╝\n")

    status = client.get_connector_status(connector_name)

    if status:
        print(f"✓ Status for connector '{connector_name}':")
        print(f"  Name: {status.get('name')}")
        print(f"  State: {status.get('connector', {}).get('state')}")
        print(f"  Worker: {status.get('connector', {}).get('worker_id')}")

        tasks = status.get('tasks', [])
        print(f"  Tasks: {len(tasks)}")
        for task in tasks:
            print(f"    - Task {task.get('id')}: {task.get('state')}")
    else:
        print(f"⚠ Could not get status for '{connector_name}'")

    print()


def demo_step6_connector_lifecycle(client: KafkaConnectClient, connector_name: str):
    """Step 6: Demonstrate connector lifecycle operations"""
    print("╔═════════════════════════════════════════════════════════╗")
    print("║  STEP 6: Connector Lifecycle Operations                ║")
    print("╚═════════════════════════════════════════════════════════╝\n")

    import time

    # Pause connector
    print("Pausing connector...")
    if client.pause_connector(connector_name):
        print("  ✓ Connector paused")
        time.sleep(2)

    # Check status
    status = client.get_connector_status(connector_name)
    if status:
        print(f"  State: {status.get('connector', {}).get('state')}")

    # Resume connector
    print("\nResuming connector...")
    if client.resume_connector(connector_name):
        print("  ✓ Connector resumed")
        time.sleep(2)

    # Check status again
    status = client.get_connector_status(connector_name)
    if status:
        print(f"  State: {status.get('connector', {}).get('state')}")

    print()


def demo_step7_cleanup(client: KafkaConnectClient, connector_name: str):
    """Step 7: Cleanup - delete connector"""
    print("╔═════════════════════════════════════════════════════════╗")
    print("║  STEP 7: Cleanup (Delete Connector)                    ║")
    print("╚═════════════════════════════════════════════════════════╝\n")

    response = input(f"Delete connector '{connector_name}'? (y/N): ").strip().lower()

    if response == 'y':
        if client.delete_connector(connector_name):
            print(f"✓ Connector '{connector_name}' deleted")
        else:
            print(f"⚠ Failed to delete connector '{connector_name}'")
    else:
        print(f"✓ Keeping connector '{connector_name}'")

    print()


def main():
    """Main demonstration"""
    print("\n╔═════════════════════════════════════════════════════════╗")
    print("║                                                         ║")
    print("║  Day 7: Kafka Connect REST API Client (Python)         ║")
    print("║                                                         ║")
    print("║  Platform-Agnostic Connector Management                ║")
    print("║  Same REST API in Java, Python, Go, curl, etc.         ║")
    print("║                                                         ║")
    print("╚═════════════════════════════════════════════════════════╝\n")

    print("Prerequisites:")
    print(f"  ✓ Kafka Connect running on {CONNECT_URL}")
    print("  ✓ requests library installed")
    print()

    print("This demo shows:")
    print("  1. Checking Connect cluster info")
    print("  2. Listing available connector plugins")
    print("  3. Creating connectors")
    print("  4. Monitoring connector status")
    print("  5. Managing connector lifecycle (pause/resume)")
    print("  6. Deleting connectors")
    print()

    input("Press Enter to start the demo...")
    print()

    # Create client
    client = KafkaConnectClient()
    connector_name = "python-file-source-demo"

    try:
        # Run all demo steps
        demo_step1_check_connect()
        demo_step2_list_plugins(client)
        demo_step3_create_file_source_connector(client)
        demo_step4_list_connectors(client)
        demo_step5_get_connector_status(client, connector_name)
        demo_step6_connector_lifecycle(client, connector_name)
        demo_step7_cleanup(client, connector_name)

        print("╔═════════════════════════════════════════════════════════╗")
        print("║   Day 7 Kafka Connect REST API Demo Complete (Python)  ║")
        print("╚═════════════════════════════════════════════════════════╝")
        print("\nKey Takeaways:")
        print("  ✓ Managed Kafka Connect via REST API")
        print("  ✓ Platform-agnostic approach (works from any language)")
        print("  ✓ Created, monitored, and controlled connectors")
        print("  ✓ Same API whether using Java, Python, Go, curl")
        print()
        print("Benefits of Kafka Connect:")
        print("  → No code needed for common integrations")
        print("  → 100+ pre-built connectors available")
        print("  → Automatic offset management")
        print("  → Fault tolerance and scalability")
        print()
        print("Production Use Cases:")
        print("  → Stream database changes (CDC with Debezium)")
        print("  → Load data from S3, HDFS, cloud storage")
        print("  → Sync to Elasticsearch, MongoDB, PostgreSQL")
        print("  → Integrate SaaS applications (Salesforce, etc.)")
        print()
        print("Next Steps:")
        print("  → Explore Connect Hub: https://www.confluent.io/hub/")
        print("  → Run Day 8: python day08_security.py")
        print("  → Compare with Java ConnectorManager.java")
        print("  → Build custom connectors if needed")

    except KeyboardInterrupt:
        print("\n\n⚠ Demo interrupted by user")
    except Exception as e:
        print(f"\n❌ ERROR: {e}", file=sys.stderr)
        import traceback
        traceback.print_exc()
        sys.exit(1)


if __name__ == '__main__':
    main()
