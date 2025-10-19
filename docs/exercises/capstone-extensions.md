# Capstone Extensions - Airflow & Flink

!!! note "Platform-Agnostic Extensions"
    This guide works for BOTH Java and Python capstone projects.

    - Java Developers: Extend EventMart with orchestration and advanced stream processing
    - Python Developers: Add enterprise features to Analytics Platform

    **Prerequisites:** Complete core capstone first ([Java](capstone-guide-java.md) | [Python](capstone-guide-python.md))

---

## Overview

These optional extensions demonstrate enterprise-grade data engineering skills:

| Tier | Technology | What You'll Learn | Bonus Points |
|------|-----------|-------------------|--------------|
| **Tier 2** | Apache Airflow | Workflow orchestration, DAG dependencies, scheduling | +10% |
| **Tier 3** | Apache Flink | Complex event processing, advanced windowing, Flink SQL | +10% |
| **Bonus** | Spark, dbt, Great Expectations | Batch+stream hybrid, data quality | +5% |

**Note:** Extensions are **optional**. Core capstone alone is worth 100%. Extensions allow you to exceed 100% and demonstrate advanced skills.

---

## Tier 2: Apache Airflow Integration

### What is Airflow?

Apache Airflow is a platform for **orchestrating complex workflows**. It's the industry standard for:
- Scheduling batch jobs
- Managing dependencies between tasks
- Monitoring pipeline execution
- Coordinating multiple systems

### Why Add Airflow to Your Capstone?

**For Java Developers (EventMart):**
- Schedule batch data loads
- Manage Kafka Connect connectors
- Deploy schema updates
- Coordinate microservice deployments

**For Python Developers (Analytics Platform):**
- Orchestrate legacy database ingestion
- Manage Faust worker lifecycle
- Run data quality checks
- Coordinate batch+stream hybrid pipelines

---

### Setup Airflow

### 1. Add Airflow to Docker Compose

**Create `docker-compose-airflow.yml`:**

```yaml
version: '3.8'

services:
  # Airflow requires PostgreSQL
  airflow-postgres:
    image: postgres:15
    environment:
      POSTGRES_USER: airflow
      POSTGRES_PASSWORD: airflow
      POSTGRES_DB: airflow
    volumes:
      - airflow-postgres-data:/var/lib/postgresql/data
    networks:
      - kafka-network

  # Airflow webserver
  airflow-webserver:
    image: apache/airflow:2.7.0-python3.10
    command: webserver
    environment:
      - AIRFLOW__CORE__EXECUTOR=LocalExecutor
      - AIRFLOW__DATABASE__SQL_ALCHEMY_CONN=postgresql+psycopg2://airflow:airflow@airflow-postgres/airflow
      - AIRFLOW__CORE__LOAD_EXAMPLES=False
      - AIRFLOW__WEBSERVER__EXPOSE_CONFIG=True
    volumes:
      - ./airflow/dags:/opt/airflow/dags
      - ./airflow/plugins:/opt/airflow/plugins
      - ./airflow/logs:/opt/airflow/logs
    ports:
      - "8081:8080"
    depends_on:
      - airflow-postgres
    networks:
      - kafka-network

  # Airflow scheduler
  airflow-scheduler:
    image: apache/airflow:2.7.0-python3.10
    command: scheduler
    environment:
      - AIRFLOW__CORE__EXECUTOR=LocalExecutor
      - AIRFLOW__DATABASE__SQL_ALCHEMY_CONN=postgresql+psycopg2://airflow:airflow@airflow-postgres/airflow
    volumes:
      - ./airflow/dags:/opt/airflow/dags
      - ./airflow/plugins:/opt/airflow/plugins
      - ./airflow/logs:/opt/airflow/logs
    depends_on:
      - airflow-postgres
    networks:
      - kafka-network

volumes:
  airflow-postgres-data:

networks:
  kafka-network:
    external: true  # Use existing Kafka network
```

### 2. Start Airflow

```bash
# Initialize Airflow database
docker-compose -f docker-compose-airflow.yml run airflow-webserver airflow db init

# Create admin user
docker-compose -f docker-compose-airflow.yml run airflow-webserver airflow users create \
    --username admin \
    --password admin \
    --firstname Admin \
    --lastname User \
    --role Admin \
    --email admin@example.com

# Start Airflow services
docker-compose -f docker-compose-airflow.yml up -d

# Access Airflow UI
open http://localhost:8081
# Login: admin / admin
```

---

### Airflow DAGs for Java Developers (EventMart)

### DAG 1: Daily Batch Order Processing

**File:** `airflow/dags/eventmart_daily_batch.py`

```python
from airflow import DAG
from airflow.operators.python import PythonOperator
from airflow.providers.http.operators.http import SimpleHttpOperator
from airflow.providers.http.sensors.http import HttpSensor
from datetime import datetime, timedelta

default_args = {
    'owner': 'data-engineer',
    'depends_on_past': False,
    'start_date': datetime(2024, 1, 1),
    'email_on_failure': False,
    'email_on_retry': False,
    'retries': 2,
    'retry_delay': timedelta(minutes=5),
}

with DAG(
    'eventmart_daily_batch',
    default_args=default_args,
    description='Daily batch processing for EventMart',
    schedule_interval='0 2 * * *',  # 2 AM daily
    catchup=False,
    tags=['eventmart', 'batch', 'orders']
) as dag:

    # Task 1: Health check EventMart API
    health_check = HttpSensor(
        task_id='health_check_eventmart',
        http_conn_id='eventmart_api',
        endpoint='/actuator/health',
        timeout=60,
        poke_interval=10,
    )

    # Task 2: Trigger batch order ingestion
    trigger_batch = SimpleHttpOperator(
        task_id='trigger_batch_ingestion',
        http_conn_id='eventmart_api',
        endpoint='/api/training/eventmart/batch/ingest',
        method='POST',
        data='{"date": "{{ ds }}"}',  # Airflow execution date
        headers={'Content-Type': 'application/json'},
        response_check=lambda response: response.status_code == 200,
    )

    # Task 3: Verify data quality
    def check_data_quality(**context):
        """Query database to verify batch loaded correctly"""
        import psycopg2

        execution_date = context['ds']
        conn = psycopg2.connect(
            host='postgres',
            database='eventmart',
            user='kafka',
            password='kafka-password'
        )

        cursor = conn.cursor()
        cursor.execute("""
            SELECT COUNT(*) FROM orders
            WHERE DATE(created_at) = %s
        """, (execution_date,))

        count = cursor.fetchone()[0]
        print(f"Loaded {count} orders for {execution_date}")

        if count == 0:
            raise Exception(f"No orders loaded for {execution_date}")

        conn.close()

    verify_data = PythonOperator(
        task_id='verify_data_quality',
        python_callable=check_data_quality,
    )

    # Task 4: Send success notification
    def send_success_notification(**context):
        """Send notification that batch completed"""
        execution_date = context['ds']
        print(f" EventMart batch for {execution_date} completed successfully!")
        # In production: Send email, Slack, PagerDuty, etc.

    notify = PythonOperator(
        task_id='send_notification',
        python_callable=send_success_notification,
    )

    # DAG flow
    health_check >> trigger_batch >> verify_data >> notify
```

**What This Demonstrates:**
-  DAG definition with scheduling (`@daily`)
-  Task dependencies (`>>` operator)
-  HTTP operators to call Spring Boot REST API
-  Python operators for custom logic
-  Error handling and retries
-  Data quality validation

---

### DAG 2: Kafka Connect Connector Management

**File:** `airflow/dags/eventmart_connector_management.py`

```python
from airflow import DAG
from airflow.operators.python import PythonOperator
from datetime import datetime, timedelta
import requests
import time

default_args = {
    'owner': 'data-engineer',
    'start_date': datetime(2024, 1, 1),
    'retries': 1,
}

with DAG(
    'eventmart_connector_management',
    default_args=default_args,
    description='Manage Kafka Connect connectors for EventMart',
    schedule_interval='@hourly',
    catchup=False,
    tags=['eventmart', 'kafka-connect']
) as dag:

    def check_connector_health():
        """Check if PostgreSQL sink connector is healthy"""
        response = requests.get('http://kafka-connect:8083/connectors/postgres-sink/status')
        status = response.json()

        if status['connector']['state'] != 'RUNNING':
            raise Exception(f"Connector unhealthy: {status}")

        for task in status['tasks']:
            if task['state'] != 'RUNNING':
                raise Exception(f"Task {task['id']} unhealthy: {task}")

        print(" All connectors healthy")

    def restart_connector_if_needed():
        """Restart connector if it's been running for >24 hours"""
        # Get connector status
        response = requests.get('http://kafka-connect:8083/connectors/postgres-sink/status')
        status = response.json()

        # Check uptime (simplified - in production, track in database)
        # For demo, restart weekly
        from datetime import datetime
        if datetime.now().weekday() == 0:  # Monday
            print("Weekly connector restart...")

            # Pause connector
            requests.put('http://kafka-connect:8083/connectors/postgres-sink/pause')
            time.sleep(30)

            # Resume connector
            requests.put('http://kafka-connect:8083/connectors/postgres-sink/resume')
            print(" Connector restarted")

    def monitor_connector_lag():
        """Monitor connector lag and alert if too high"""
        # In production: Query connector metrics
        # Alert if lag > threshold
        print("Monitoring connector lag...")
        # Implementation depends on your monitoring setup

    health_task = PythonOperator(
        task_id='check_connector_health',
        python_callable=check_connector_health,
    )

    restart_task = PythonOperator(
        task_id='restart_connector_if_needed',
        python_callable=restart_connector_if_needed,
    )

    lag_task = PythonOperator(
        task_id='monitor_connector_lag',
        python_callable=monitor_connector_lag,
    )

    # Run health check first, then conditional restart
    health_task >> restart_task >> lag_task
```

---

### Airflow DAGs for Python Developers (Analytics Platform)

### DAG 1: Legacy Database Ingestion

**File:** `airflow/dags/analytics_legacy_ingestion.py`

```python
from airflow import DAG
from airflow.operators.python import PythonOperator
from datetime import datetime, timedelta

default_args = {
    'owner': 'data-engineer',
    'start_date': datetime(2024, 1, 1),
    'retries': 2,
    'retry_delay': timedelta(minutes=5),
}

with DAG(
    'analytics_legacy_ingestion',
    default_args=default_args,
    description='Ingest data from legacy PostgreSQL to Kafka',
    schedule_interval='0 1 * * *',  # 1 AM daily
    catchup=False,
    tags=['analytics', 'batch', 'ingestion']
) as dag:

    def ingest_legacy_orders(**context):
        """Read from legacy database and produce to Kafka"""
        import psycopg2
        from confluent_kafka import Producer
        from confluent_kafka.schema_registry import SchemaRegistryClient
        from confluent_kafka.schema_registry.avro import AvroSerializer

        execution_date = context['ds']

        # Connect to legacy database
        legacy_conn = psycopg2.connect(
            host='legacy-db',
            database='ecommerce_old',
            user='readonly',
            password='readonly'
        )

        # Query yesterday's orders
        cursor = legacy_conn.cursor()
        cursor.execute("""
            SELECT order_id, user_id, product_id, amount, created_at
            FROM orders
            WHERE DATE(created_at) = %s
        """, (execution_date,))

        # Setup Kafka producer with Avro
        schema_registry = SchemaRegistryClient({'url': 'http://schema-registry:8081'})

        with open('/opt/airflow/schemas/order_events.avsc') as f:
            schema_str = f.read()

        avro_serializer = AvroSerializer(schema_registry, schema_str)

        producer = Producer({
            'bootstrap.servers': 'kafka:9092',
            'enable.idempotence': True,
            'acks': 'all'
        })

        # Produce each order to Kafka
        count = 0
        for row in cursor:
            order_event = {
                'order_id': row[0],
                'user_id': row[1],
                'product_id': row[2],
                'amount': float(row[3]),
                'timestamp': int(row[4].timestamp() * 1000)
            }

            producer.produce(
                topic='order-events',
                key=row[1].encode('utf-8'),
                value=avro_serializer(order_event, None)
            )
            count += 1

        producer.flush()
        legacy_conn.close()

        print(f" Ingested {count} orders from legacy database")

    def verify_kafka_ingestion(**context):
        """Verify messages landed in Kafka topic"""
        from kafka import KafkaConsumer

        consumer = KafkaConsumer(
            'order-events',
            bootstrap_servers='kafka:9092',
            auto_offset_reset='latest',
            consumer_timeout_ms=5000
        )

        # Count messages (simplified check)
        message_count = sum(1 for _ in consumer)
        print(f"Verified {message_count} messages in Kafka")

        consumer.close()

    ingest_task = PythonOperator(
        task_id='ingest_legacy_orders',
        python_callable=ingest_legacy_orders,
    )

    verify_task = PythonOperator(
        task_id='verify_kafka_ingestion',
        python_callable=verify_kafka_ingestion,
    )

    ingest_task >> verify_task
```

---

### DAG 2: Faust Worker Management

**File:** `airflow/dags/analytics_faust_management.py`

```python
from airflow import DAG
from airflow.operators.python import PythonOperator
from airflow.operators.bash import BashOperator
from datetime import datetime, timedelta

default_args = {
    'owner': 'data-engineer',
    'start_date': datetime(2024, 1, 1),
    'retries': 1,
}

with DAG(
    'analytics_faust_management',
    default_args=default_args,
    description='Manage Faust stream processing workers',
    schedule_interval='0 */6 * * *',  # Every 6 hours
    catchup=False,
    tags=['analytics', 'faust', 'streams']
) as dag:

    def check_faust_health():
        """Check if Faust workers are healthy"""
        import requests

        # Check Faust web UI (if enabled)
        try:
            response = requests.get('http://sales-aggregator:6066/', timeout=10)
            if response.status_code == 200:
                print(" Sales aggregator healthy")
        except Exception as e:
            raise Exception(f"Sales aggregator unhealthy: {e}")

    def check_processing_lag():
        """Check if Faust is keeping up with Kafka"""
        from kafka import KafkaConsumer, TopicPartition

        consumer = KafkaConsumer(bootstrap_servers='kafka:9092')

        # Check lag for Faust consumer group
        partitions = [TopicPartition('order-events', p) for p in range(6)]
        consumer.assign(partitions)

        end_offsets = consumer.end_offsets(partitions)
        current_offsets = consumer.position(partitions[0])  # Simplified

        lag = sum(end_offsets.values()) - current_offsets
        print(f"Current lag: {lag} messages")

        if lag > 50000:
            raise Exception(f"Faust lag too high: {lag}")

        consumer.close()

    # Restart Faust worker (use with caution in production!)
    restart_faust = BashOperator(
        task_id='restart_faust_worker',
        bash_command="""
            docker-compose restart sales-aggregator && \
            sleep 30 && \
            docker-compose logs --tail=50 sales-aggregator
        """,
    )

    health_task = PythonOperator(
        task_id='check_faust_health',
        python_callable=check_faust_health,
    )

    lag_task = PythonOperator(
        task_id='check_processing_lag',
        python_callable=check_processing_lag,
    )

    # Only restart if health check fails
    health_task >> lag_task
```

---

### Airflow Learning Outcomes

After implementing Airflow DAGs, you'll demonstrate:

 **Workflow Orchestration**
- DAG definition and scheduling
- Task dependencies and branching
- Error handling and retries

 **Hybrid Batch + Stream**
- Batch ingestion to Kafka topics
- Coordinating multiple systems
- Data quality validation

 **Operations & Monitoring**
- Health checks and monitoring
- Automated remediation
- Notification and alerting

 **Production Engineering**
- Infrastructure as code
- Reproducible workflows
- Audit trails and lineage

---

## Tier 3: Apache Flink Integration

### What is Flink?

Apache Flink is a **stream processing framework** with:
- Advanced windowing (tumbling, sliding, session, custom)
- Complex Event Processing (CEP) for pattern matching
- Multi-stream joins with time constraints
- Exactly-once stateful processing
- Flink SQL for streaming analytics

### Why Add Flink to Your Capstone?

**For Java Developers:**
- Flink is Java-native (best performance)
- Show mastery of enterprise stream processing
- Implement complex CEP patterns (fraud detection)
- Use Flink SQL for streaming analytics

**For Python Developers:**
- PyFlink provides Python API
- More powerful than Faust for complex operations
- Industry-standard for production streaming
- SQL interface for analytics

---

### Setup Flink

### Add Flink to Docker Compose

**Create `docker-compose-flink.yml`:**

```yaml
version: '3.8'

services:
  # Flink JobManager
  flink-jobmanager:
    image: flink:1.18-java11
    command: jobmanager
    environment:
      - JOB_MANAGER_RPC_ADDRESS=flink-jobmanager
    ports:
      - "8082:8081"  # Flink Web UI
    networks:
      - kafka-network

  # Flink TaskManager
  flink-taskmanager:
    image: flink:1.18-java11
    command: taskmanager
    environment:
      - JOB_MANAGER_RPC_ADDRESS=flink-jobmanager
    depends_on:
      - flink-jobmanager
    networks:
      - kafka-network

networks:
  kafka-network:
    external: true
```

**Start Flink:**

```bash
docker-compose -f docker-compose-flink.yml up -d

# Access Flink Web UI
open http://localhost:8082
```

---

### Flink Java Examples (for Java Developers)

### Example 1: Fraud Detection with CEP

**File:** `flink-jobs/src/main/java/com/eventmart/FraudDetection.java`

```java
package com.eventmart.analytics;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;

/**
 * Detect fraudulent orders using Complex Event Processing (CEP).
 *
 * Pattern: Multiple high-value orders from same user within 5 minutes
 */
public class FraudDetection {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);

        // Kafka source - EventMart order events
        Properties kafkaProps = new Properties();
        kafkaProps.setProperty("bootstrap.servers", "localhost:9092");
        kafkaProps.setProperty("group.id", "fraud-detection");

        FlinkKafkaConsumer<OrderEvent> consumer = new FlinkKafkaConsumer<>(
            "eventmart.order.events",
            new AvroDeserializationSchema<>(OrderEvent.class),
            kafkaProps
        );

        DataStream<OrderEvent> orders = env
            .addSource(consumer)
            .assignTimestampsAndWatermarks(
                WatermarkStrategy
                    .<OrderEvent>forBoundedOutOfOrderness(Duration.ofSeconds(5))
                    .withTimestampAssigner((event, timestamp) -> event.getTimestamp())
            );

        // Define fraud pattern: 3+ orders over $1000 within 5 minutes
        Pattern<OrderEvent, ?> fraudPattern = Pattern
            .<OrderEvent>begin("firstOrder")
            .where(new SimpleCondition<OrderEvent>() {
                @Override
                public boolean filter(OrderEvent event) {
                    return event.getAmount() > 1000.0;
                }
            })
            .next("secondOrder")
            .where(new SimpleCondition<OrderEvent>() {
                @Override
                public boolean filter(OrderEvent event) {
                    return event.getAmount() > 1000.0;
                }
            })
            .next("thirdOrder")
            .where(new SimpleCondition<OrderEvent>() {
                @Override
                public boolean filter(OrderEvent event) {
                    return event.getAmount() > 1000.0;
                }
            })
            .within(Time.minutes(5));

        // Apply pattern to stream (keyed by userId)
        PatternStream<OrderEvent> patternStream = CEP.pattern(
            orders.keyBy(OrderEvent::getUserId),
            fraudPattern
        );

        // Extract fraud alerts
        DataStream<FraudAlert> alerts = patternStream.select(
            (Map<String, List<OrderEvent>> pattern) -> {
                List<OrderEvent> matched = new ArrayList<>();
                matched.addAll(pattern.get("firstOrder"));
                matched.addAll(pattern.get("secondOrder"));
                matched.addAll(pattern.get("thirdOrder"));

                double totalAmount = matched.stream()
                    .mapToDouble(OrderEvent::getAmount)
                    .sum();

                return new FraudAlert(
                    matched.get(0).getUserId(),
                    matched.stream().map(OrderEvent::getOrderId).collect(Collectors.toList()),
                    totalAmount,
                    "RAPID_HIGH_VALUE_ORDERS",
                    System.currentTimeMillis()
                );
            }
        );

        // Sink to fraud-alerts topic
        FlinkKafkaProducer<FraudAlert> producer = new FlinkKafkaProducer<>(
            "eventmart.fraud.alerts",
            new AvroSerializationSchema<>(FraudAlert.class),
            kafkaProps
        );

        alerts.addSink(producer);

        env.execute("EventMart Fraud Detection");
    }
}
```

**What This Demonstrates:**
-  Complex Event Processing (CEP)
-  Pattern matching across events
-  Time-based constraints
-  Keyed streams
-  Watermarks for event time

---

### Example 2: Flink SQL for Analytics

**File:** `flink-jobs/src/main/java/com/eventmart/FlinkSQLAnalytics.java`

```java
package com.eventmart.analytics;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

/**
 * Use Flink SQL to query EventMart streams
 */
public class FlinkSQLAnalytics {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        // Create Kafka table for orders
        tableEnv.executeSql("""
            CREATE TABLE order_events (
                order_id STRING,
                user_id STRING,
                product_id STRING,
                amount DECIMAL(10, 2),
                event_time TIMESTAMP(3),
                WATERMARK FOR event_time AS event_time - INTERVAL '5' SECOND
            ) WITH (
                'connector' = 'kafka',
                'topic' = 'eventmart.order.events',
                'properties.bootstrap.servers' = 'localhost:9092',
                'properties.group.id' = 'flink-sql',
                'format' = 'avro-confluent',
                'avro-confluent.url' = 'http://localhost:8081'
            )
        """);

        // Tumbling window aggregation - hourly sales by product
        Table result = tableEnv.sqlQuery("""
            SELECT
                product_id,
                TUMBLE_START(event_time, INTERVAL '1' HOUR) as window_start,
                TUMBLE_END(event_time, INTERVAL '1' HOUR) as window_end,
                COUNT(*) as order_count,
                SUM(amount) as total_revenue,
                AVG(amount) as avg_order_value,
                MAX(amount) as max_order
            FROM order_events
            GROUP BY
                product_id,
                TUMBLE(event_time, INTERVAL '1' HOUR)
            HAVING COUNT(*) > 10
        """);

        // Create output table
        tableEnv.executeSql("""
            CREATE TABLE product_sales_hourly (
                product_id STRING,
                window_start TIMESTAMP(3),
                window_end TIMESTAMP(3),
                order_count BIGINT,
                total_revenue DECIMAL(10, 2),
                avg_order_value DECIMAL(10, 2),
                max_order DECIMAL(10, 2)
            ) WITH (
                'connector' = 'kafka',
                'topic' = 'eventmart.sales.hourly',
                'properties.bootstrap.servers' = 'localhost:9092',
                'format' = 'avro-confluent',
                'avro-confluent.url' = 'http://localhost:8081'
            )
        """);

        // Execute query and write to output
        result.executeInsert("product_sales_hourly");
    }
}
```

---

### Flink Python Examples (for Python Developers)

### Example 1: PyFlink Windowing

**File:** `flink-jobs/revenue_analytics.py`

```python
from pyflink.datastream import StreamExecutionEnvironment
from pyflink.datastream.connectors.kafka import FlinkKafkaConsumer, FlinkKafkaProducer
from pyflink.datastream.window import TumblingEventTimeWindows
from pyflink.common import Time, WatermarkStrategy
from pyflink.common.serialization import SimpleStringSchema

def main():
    env = StreamExecutionEnvironment.get_execution_environment()
    env.set_parallelism(2)

    # Kafka source
    kafka_consumer = FlinkKafkaConsumer(
        topics='order-events',
        deserialization_schema=SimpleStringSchema(),
        properties={'bootstrap.servers': 'localhost:9092', 'group.id': 'pyflink-revenue'}
    )

    # Define watermark strategy
    watermark_strategy = WatermarkStrategy \
        .for_bounded_out_of_orderness(Time.seconds(5)) \
        .with_timestamp_assigner(lambda event, ts: event['timestamp'])

    # Read stream
    orders = env.add_source(kafka_consumer) \
        .map(lambda x: json.loads(x)) \
        .assign_timestamps_and_watermarks(watermark_strategy)

    # Tumbling window aggregation (1 hour)
    revenue_per_hour = orders \
        .key_by(lambda order: order['product_id']) \
        .window(TumblingEventTimeWindows.of(Time.hours(1))) \
        .reduce(lambda a, b: {
            'product_id': a['product_id'],
            'total_revenue': a.get('total_revenue', a['amount']) + b['amount'],
            'order_count': a.get('order_count', 1) + 1
        })

    # Sink to Kafka
    kafka_producer = FlinkKafkaProducer(
        topic='revenue-hourly',
        serialization_schema=SimpleStringSchema(),
        producer_config={'bootstrap.servers': 'localhost:9092'}
    )

    revenue_per_hour.map(lambda x: json.dumps(x)).add_sink(kafka_producer)

    env.execute("PyFlink Revenue Analytics")

if __name__ == '__main__':
    main()
```

---

### Example 2: PyFlink SQL

**File:** `flink-jobs/sql_analytics.py`

```python
from pyflink.datastream import StreamExecutionEnvironment
from pyflink.table import StreamTableEnvironment

def main():
    env = StreamExecutionEnvironment.get_execution_environment()
    table_env = StreamTableEnvironment.create(env)

    # Create Kafka source table
    table_env.execute_sql("""
        CREATE TABLE order_events (
            order_id STRING,
            product_id STRING,
            amount DECIMAL(10, 2),
            event_time TIMESTAMP(3),
            WATERMARK FOR event_time AS event_time - INTERVAL '5' SECOND
        ) WITH (
            'connector' = 'kafka',
            'topic' = 'order-events',
            'properties.bootstrap.servers' = 'localhost:9092',
            'format' = 'json'
        )
    """)

    # SQL query with windowing
    result = table_env.sql_query("""
        SELECT
            product_id,
            TUMBLE_START(event_time, INTERVAL '1' HOUR) as hour,
            COUNT(*) as order_count,
            SUM(amount) as revenue
        FROM order_events
        GROUP BY
            product_id,
            TUMBLE(event_time, INTERVAL '1' HOUR)
    """)

    # Create sink table
    table_env.execute_sql("""
        CREATE TABLE product_revenue (
            product_id STRING,
            hour TIMESTAMP(3),
            order_count BIGINT,
            revenue DECIMAL(10, 2)
        ) WITH (
            'connector' = 'kafka',
            'topic' = 'product-revenue',
            'properties.bootstrap.servers' = 'localhost:9092',
            'format' = 'json'
        )
    """)

    # Execute and write results
    result.execute_insert('product_revenue').wait()

if __name__ == '__main__':
    main()
```

---

### Flink Learning Outcomes

After implementing Flink jobs, you'll demonstrate:

 **Advanced Stream Processing**
- Complex Event Processing (CEP)
- Multi-stream joins
- Advanced windowing strategies

 **Exactly-Once Processing**
- Stateful processing with checkpoints
- Savepoints for versioning
- Fault tolerance

 **SQL for Streams**
- Flink SQL for streaming analytics
- Temporal joins
- Windowed aggregations

 **Enterprise Scale**
- Production-grade stream processing
- Better performance than Faust for complex ops
- Industry-standard tool

---

## Comparison: When to Use What?

| Use Case | Faust | Flink | Kafka Streams | Airflow |
|----------|-------|-------|---------------|---------|
| **Simple aggregations** |  Best |  Overkill |  Good |  No |
| **Complex CEP** |  No |  Best |  Limited |  No |
| **Multi-stream joins** |  Basic |  Advanced |  Good |  No |
| **SQL interface** |  No |  Yes |  ksqlDB |  No |
| **Batch orchestration** |  No |  No |  No |  Best |
| **Scheduling** |  No |  No |  No |  Best |
| **Python-native** |  Yes |  PyFlink |  No |  Yes |
| **Java-native** |  No |  Yes |  Yes |  Via API |

---

## Bonus Extensions

### Apache Spark Structured Streaming

Combine batch and streaming in one API:

```python
from pyspark.sql import SparkSession
from pyspark.sql.functions import *

spark = SparkSession.builder \
    .appName("KafkaSparkStreaming") \
    .getOrCreate()

# Read from Kafka
orders = spark.readStream \
    .format("kafka") \
    .option("kafka.bootstrap.servers", "localhost:9092") \
    .option("subscribe", "order-events") \
    .load()

# Process stream
aggregated = orders \
    .selectExpr("CAST(value AS STRING)") \
    .select(from_json(col("value"), schema).alias("data")) \
    .groupBy(
        window(col("data.timestamp"), "1 hour"),
        col("data.product_id")
    ) \
    .agg(sum("data.amount").alias("revenue"))

# Write to sink
query = aggregated.writeStream \
    .outputMode("update") \
    .format("kafka") \
    .option("kafka.bootstrap.servers", "localhost:9092") \
    .option("topic", "revenue-hourly") \
    .start()
```

---

### dbt (Data Build Tool)

Transform data in your warehouse with SQL:

```sql
-- models/sales_summary.sql
{{
  config(
    materialized='incremental',
    unique_key='date'
  )
}}

SELECT
    DATE(created_at) as date,
    product_id,
    COUNT(*) as order_count,
    SUM(amount) as total_revenue
FROM {{ ref('raw_orders') }}
{% if is_incremental() %}
    WHERE created_at > (SELECT MAX(date) FROM {{ this }})
{% endif %}
GROUP BY DATE(created_at), product_id
```

---

### Great Expectations (Data Quality)

Validate data quality automatically:

```python
import great_expectations as ge

# Load data from PostgreSQL
context = ge.data_context.DataContext()

batch = context.get_batch(
    datasource_name="postgres",
    data_asset_name="orders"
)

# Define expectations
batch.expect_column_values_to_not_be_null("order_id")
batch.expect_column_values_to_be_between("amount", min_value=0, max_value=10000)
batch.expect_column_values_to_match_regex("email", r"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$")

# Validate
results = batch.validate()

if not results.success:
    # Alert on data quality issues
    send_alert(results)
```

---

## Presentation Tips for Extensions

### How to Present Extensions

**Don't:** Spend entire presentation on extensions
**Do:** Show extensions as "bonus features"

**Suggested Flow:**
1. Present core capstone (15 minutes)
2. Briefly mention extensions (3 minutes):
   - "I also added Airflow for batch orchestration..."
   - "And Flink for advanced CEP fraud detection..."
3. Show one impressive extension feature (2 minutes)
4. Q&A about extensions

**Example:**
> "Beyond the core real-time analytics platform, I added two enterprise features. First, Airflow orchestrates daily batch loads from our legacy database - here's the DAG [show UI]. Second, I implemented fraud detection with Flink CEP that catches patterns Faust couldn't - like three high-value orders within 5 minutes [show code]. These tools are standard in production data platforms."

---

## Evaluation Rubric for Extensions

### Airflow Extension (+10%)

**Excellent (9-10%):**
- Multiple useful DAGs implemented
- Proper dependency management
- Error handling and monitoring
- Integration with core capstone
- Clear demonstration

**Good (7-8%):**
- Basic DAGs working
- Some error handling
- Shows understanding

**Partial (4-6%):**
- DAGs exist but minimal
- Limited integration

### Flink Extension (+10%)

**Excellent (9-10%):**
- Advanced features (CEP, multi-stream joins, SQL)
- Proper watermark strategy
- Fault tolerance configured
- Clear use case demonstrated

**Good (7-8%):**
- Basic Flink job working
- Windowing implemented
- Shows understanding

**Partial (4-6%):**
- Simple example only
- Limited features

---

## Resources

**Apache Airflow:**
- [Official Docs](https://airflow.apache.org/docs/)
- [Airflow Tutorial](https://airflow.apache.org/docs/apache-airflow/stable/tutorial/index.html)

**Apache Flink:**
- [Flink Docs](https://nightlies.apache.org/flink/flink-docs-stable/)
- [PyFlink Docs](https://nightlies.apache.org/flink/flink-docs-stable/docs/dev/python/overview/)
- [Flink SQL](https://nightlies.apache.org/flink/flink-docs-stable/docs/dev/table/sql/overview/)

**Spark Structured Streaming:**
- [Spark Streaming Guide](https://spark.apache.org/docs/latest/structured-streaming-programming-guide.html)

---

**Good luck with your extended capstone!** 

These tools will set you apart as an advanced data engineer who understands enterprise-grade stream processing and workflow orchestration.
