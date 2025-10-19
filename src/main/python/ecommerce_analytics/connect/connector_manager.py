"""
Connector Manager

Manages Kafka Connect connectors via REST API (Day 7).
Demonstrates sink connectors (JDBC) for data integration.
"""

import requests
import logging
import json
from typing import Dict, Any, List, Optional

from ecommerce_analytics.config.settings import get_settings

logger = logging.getLogger(__name__)


class ConnectorManager:
    """
    Manages Kafka Connect connectors via REST API.

    Features (Day 7):
    - Create JDBC Sink connectors
    - List connectors
    - Get connector status
    - Pause/resume connectors
    - Delete connectors
    """

    def __init__(self):
        """Initialize ConnectorManager."""
        self.settings = get_settings()
        self.connect_url = self.settings.connect_url

        logger.info(f"ConnectorManager initialized (Connect URL: {self.connect_url})")

    def create_jdbc_sink_connector(
        self,
        name: str,
        topics: List[str],
        table_name: str,
        insert_mode: str = "upsert",
        pk_fields: Optional[List[str]] = None,
        auto_create: bool = True,
        auto_evolve: bool = True,
    ) -> Dict[str, Any]:
        """
        Create a JDBC Sink Connector (Day 7).

        Args:
            name: Connector name
            topics: List of topics to consume from
            table_name: Target database table name
            insert_mode: Insert mode (insert, upsert, update)
            pk_fields: Primary key fields for upsert mode
            auto_create: Auto-create table if not exists
            auto_evolve: Auto-evolve table schema

        Returns:
            Connector configuration response
        """
        # Build connector configuration
        config = {
            "connector.class": "io.confluent.connect.jdbc.JdbcSinkConnector",
            "tasks.max": "1",
            "topics": ",".join(topics),

            # JDBC connection
            "connection.url": self.settings.get_postgres_jdbc_url(),
            "connection.user": self.settings.postgres_user,
            "connection.password": self.settings.postgres_password,

            # Table configuration
            "table.name.format": table_name,
            "insert.mode": insert_mode,
            "auto.create": str(auto_create).lower(),
            "auto.evolve": str(auto_evolve).lower(),

            # Kafka configuration
            "key.converter": "org.apache.kafka.connect.storage.StringConverter",
            "value.converter": "io.confluent.connect.avro.AvroConverter",
            "value.converter.schema.registry.url": self.settings.schema_registry_url,
        }

        # Add primary key fields for upsert mode
        if insert_mode == "upsert" and pk_fields:
            config["pk.fields"] = ",".join(pk_fields)
            config["pk.mode"] = "record_key" if len(pk_fields) == 1 else "record_value"

        return self.create_connector(name, config)

    def create_connector(
        self,
        name: str,
        config: Dict[str, str]
    ) -> Dict[str, Any]:
        """
        Create a generic connector.

        Args:
            name: Connector name
            config: Connector configuration

        Returns:
            Connector configuration response
        """
        try:
            url = f"{self.connect_url}/connectors"

            payload = {
                "name": name,
                "config": config,
            }

            response = requests.post(
                url,
                headers={"Content-Type": "application/json"},
                json=payload,
                timeout=10,
            )

            response.raise_for_status()

            logger.info(f"Created connector '{name}'")
            return response.json()

        except requests.exceptions.HTTPError as e:
            if e.response.status_code == 409:
                logger.warning(f"Connector '{name}' already exists")
                # Return existing connector config
                return self.get_connector(name)
            else:
                logger.error(f"Failed to create connector '{name}': {e}")
                raise

        except Exception as e:
            logger.error(f"Failed to create connector '{name}': {e}")
            raise

    def list_connectors(self) -> List[str]:
        """
        List all connectors.

        Returns:
            List of connector names
        """
        try:
            url = f"{self.connect_url}/connectors"

            response = requests.get(url, timeout=10)
            response.raise_for_status()

            connectors = response.json()
            logger.info(f"Found {len(connectors)} connectors")

            return connectors

        except Exception as e:
            logger.error(f"Failed to list connectors: {e}")
            raise

    def get_connector(self, name: str) -> Dict[str, Any]:
        """
        Get connector configuration.

        Args:
            name: Connector name

        Returns:
            Connector configuration
        """
        try:
            url = f"{self.connect_url}/connectors/{name}"

            response = requests.get(url, timeout=10)
            response.raise_for_status()

            return response.json()

        except Exception as e:
            logger.error(f"Failed to get connector '{name}': {e}")
            raise

    def get_connector_status(self, name: str) -> Dict[str, Any]:
        """
        Get connector status.

        Args:
            name: Connector name

        Returns:
            Connector status information
        """
        try:
            url = f"{self.connect_url}/connectors/{name}/status"

            response = requests.get(url, timeout=10)
            response.raise_for_status()

            status = response.json()

            logger.info(
                f"Connector '{name}' status: {status.get('connector', {}).get('state')}"
            )

            return status

        except Exception as e:
            logger.error(f"Failed to get status for connector '{name}': {e}")
            raise

    def pause_connector(self, name: str) -> bool:
        """
        Pause a connector.

        Args:
            name: Connector name

        Returns:
            True if successful
        """
        try:
            url = f"{self.connect_url}/connectors/{name}/pause"

            response = requests.put(url, timeout=10)
            response.raise_for_status()

            logger.info(f"Paused connector '{name}'")
            return True

        except Exception as e:
            logger.error(f"Failed to pause connector '{name}': {e}")
            raise

    def resume_connector(self, name: str) -> bool:
        """
        Resume a paused connector.

        Args:
            name: Connector name

        Returns:
            True if successful
        """
        try:
            url = f"{self.connect_url}/connectors/{name}/resume"

            response = requests.put(url, timeout=10)
            response.raise_for_status()

            logger.info(f"Resumed connector '{name}'")
            return True

        except Exception as e:
            logger.error(f"Failed to resume connector '{name}': {e}")
            raise

    def restart_connector(self, name: str) -> bool:
        """
        Restart a connector.

        Args:
            name: Connector name

        Returns:
            True if successful
        """
        try:
            url = f"{self.connect_url}/connectors/{name}/restart"

            response = requests.post(url, timeout=10)
            response.raise_for_status()

            logger.info(f"Restarted connector '{name}'")
            return True

        except Exception as e:
            logger.error(f"Failed to restart connector '{name}': {e}")
            raise

    def delete_connector(self, name: str) -> bool:
        """
        Delete a connector.

        Args:
            name: Connector name

        Returns:
            True if successful
        """
        try:
            url = f"{self.connect_url}/connectors/{name}"

            response = requests.delete(url, timeout=10)
            response.raise_for_status()

            logger.info(f"Deleted connector '{name}'")
            return True

        except Exception as e:
            logger.error(f"Failed to delete connector '{name}': {e}")
            raise

    def update_connector_config(
        self,
        name: str,
        config: Dict[str, str]
    ) -> Dict[str, Any]:
        """
        Update connector configuration.

        Args:
            name: Connector name
            config: New connector configuration

        Returns:
            Updated connector configuration
        """
        try:
            url = f"{self.connect_url}/connectors/{name}/config"

            response = requests.put(
                url,
                headers={"Content-Type": "application/json"},
                json=config,
                timeout=10,
            )

            response.raise_for_status()

            logger.info(f"Updated connector '{name}' configuration")
            return response.json()

        except Exception as e:
            logger.error(f"Failed to update connector '{name}': {e}")
            raise

    def get_connector_plugins(self) -> List[Dict[str, Any]]:
        """
        List available connector plugins.

        Returns:
            List of available connector plugins
        """
        try:
            url = f"{self.connect_url}/connector-plugins"

            response = requests.get(url, timeout=10)
            response.raise_for_status()

            plugins = response.json()
            logger.info(f"Found {len(plugins)} connector plugins")

            return plugins

        except Exception as e:
            logger.error(f"Failed to list connector plugins: {e}")
            raise

    def validate_connector_config(
        self,
        connector_class: str,
        config: Dict[str, str]
    ) -> Dict[str, Any]:
        """
        Validate connector configuration before creating.

        Args:
            connector_class: Connector class name
            config: Connector configuration

        Returns:
            Validation result
        """
        try:
            url = f"{self.connect_url}/connector-plugins/{connector_class}/config/validate"

            payload = config.copy()
            payload["connector.class"] = connector_class

            response = requests.put(
                url,
                headers={"Content-Type": "application/json"},
                json=payload,
                timeout=10,
            )

            response.raise_for_status()

            return response.json()

        except Exception as e:
            logger.error(f"Failed to validate connector config: {e}")
            raise
