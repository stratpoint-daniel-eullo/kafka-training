# E-Commerce Analytics Platform - Dockerfile
# Multi-stage build for Python Kafka application

# ============================================================================
# Stage 1: Builder - Install dependencies and compile extensions
# ============================================================================
FROM python:3.11-slim as builder

# Set build arguments
ARG ROCKSDB_VERSION=8.3.3

# Install build dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    gcc \
    g++ \
    make \
    cmake \
    libsnappy-dev \
    zlib1g-dev \
    libbz2-dev \
    liblz4-dev \
    libzstd-dev \
    librdkafka-dev \
    git \
    && rm -rf /var/lib/apt/lists/*

# Create virtual environment
RUN python -m venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

# Upgrade pip and install build tools
RUN pip install --no-cache-dir --upgrade pip setuptools wheel

# Copy requirements file
COPY requirements.txt /tmp/requirements.txt

# Install Python dependencies
# Note: RocksDB and confluent-kafka require compilation
RUN pip install --no-cache-dir -r /tmp/requirements.txt

# ============================================================================
# Stage 2: Runtime - Minimal production image
# ============================================================================
FROM python:3.11-slim

# Metadata
LABEL maintainer="Kafka Training Team"
LABEL description="E-Commerce Analytics Platform - Python Capstone"
LABEL version="1.0.0"

# Set environment variables
ENV PYTHONUNBUFFERED=1 \
    PYTHONDONTWRITEBYTECODE=1 \
    PYTHONPATH=/app/src/main/python \
    PATH="/opt/venv/bin:$PATH"

# Install runtime dependencies only
RUN apt-get update && apt-get install -y --no-install-recommends \
    libsnappy1v5 \
    zlib1g \
    libbz2-1.0 \
    liblz4-1 \
    libzstd1 \
    librdkafka1 \
    postgresql-client \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Copy virtual environment from builder
COPY --from=builder /opt/venv /opt/venv

# Create non-root user for security
RUN groupadd -r ecommerce && useradd -r -g ecommerce -u 1000 ecommerce

# Create application directories
RUN mkdir -p /app/src/main/python \
    /app/data/rocksdb \
    /app/logs \
    && chown -R ecommerce:ecommerce /app

# Set working directory
WORKDIR /app

# Copy application code
COPY --chown=ecommerce:ecommerce . /app/

# Switch to non-root user
USER ecommerce

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD python -c "import sys; sys.exit(0)" || exit 1

# Default command (can be overridden)
CMD ["python", "-m", "ecommerce_analytics.demo.run_demo", "--events", "100"]

# ============================================================================
# Build Instructions:
# ============================================================================
# docker build -t ecommerce-analytics:latest -f src/main/python/Dockerfile .
#
# Run Producer Demo:
# docker run --rm --network kafka-training \
#   -e KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
#   ecommerce-analytics:latest \
#   python -m ecommerce_analytics.demo.run_demo --events 100
#
# Run Consumer:
# docker run --rm --network kafka-training \
#   -e KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
#   ecommerce-analytics:latest \
#   python -m ecommerce_analytics.consumers.user_consumer
#
# Run Faust Stream Processor:
# docker run --rm --network kafka-training \
#   -e KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
#   -v $(pwd)/data/rocksdb:/app/data/rocksdb \
#   ecommerce-analytics:latest \
#   python -m ecommerce_analytics.streams.sales_aggregator worker -l info
#
# Interactive Shell:
# docker run --rm -it --network kafka-training \
#   -e KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
#   ecommerce-analytics:latest /bin/bash
# ============================================================================
