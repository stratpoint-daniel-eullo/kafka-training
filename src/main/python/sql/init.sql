-- ============================================================================
-- E-Commerce Analytics Platform - Database Initialization
-- PostgreSQL schema for analytics data storage
-- Day 7: Kafka Connect Sink Target
-- ============================================================================

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";

-- ============================================================================
-- Schema: Create dedicated schema for e-commerce analytics
-- ============================================================================

CREATE SCHEMA IF NOT EXISTS ecommerce;

-- Set search path
SET search_path TO ecommerce, public;

-- ============================================================================
-- Table: users - User registration and profile data
-- ============================================================================

CREATE TABLE IF NOT EXISTS ecommerce.users (
    user_id VARCHAR(100) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(100) NOT NULL,
    country VARCHAR(3),
    registration_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON ecommerce.users(email);
CREATE INDEX idx_users_country ON ecommerce.users(country);
CREATE INDEX idx_users_registration_date ON ecommerce.users(registration_date);

-- ============================================================================
-- Table: products - Product catalog
-- ============================================================================

CREATE TABLE IF NOT EXISTS ecommerce.products (
    product_id VARCHAR(100) PRIMARY KEY,
    product_name VARCHAR(500) NOT NULL,
    category VARCHAR(100),
    price NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
    stock_quantity INTEGER NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    currency VARCHAR(3) DEFAULT 'USD',
    warehouse_location VARCHAR(50),
    supplier_id VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_products_category ON ecommerce.products(category);
CREATE INDEX idx_products_price ON ecommerce.products(price);
CREATE INDEX idx_products_stock ON ecommerce.products(stock_quantity);
CREATE INDEX idx_products_warehouse ON ecommerce.products(warehouse_location);

-- ============================================================================
-- Table: orders - Order transactions
-- ============================================================================

CREATE TABLE IF NOT EXISTS ecommerce.orders (
    order_id VARCHAR(100) PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    total_amount NUMERIC(12, 2) NOT NULL CHECK (total_amount >= 0),
    currency VARCHAR(3) DEFAULT 'USD',
    order_status VARCHAR(50) DEFAULT 'PLACED',
    payment_method VARCHAR(50),
    shipping_country VARCHAR(3),
    order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES ecommerce.users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_orders_user_id ON ecommerce.orders(user_id);
CREATE INDEX idx_orders_status ON ecommerce.orders(order_status);
CREATE INDEX idx_orders_date ON ecommerce.orders(order_date);
CREATE INDEX idx_orders_amount ON ecommerce.orders(total_amount);

-- ============================================================================
-- Table: order_items - Individual items within orders
-- ============================================================================

CREATE TABLE IF NOT EXISTS ecommerce.order_items (
    order_item_id SERIAL PRIMARY KEY,
    order_id VARCHAR(100) NOT NULL,
    product_id VARCHAR(100) NOT NULL,
    product_name VARCHAR(500),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(12, 2) NOT NULL CHECK (unit_price >= 0),
    total_price NUMERIC(12, 2) GENERATED ALWAYS AS (quantity * unit_price) STORED,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (order_id) REFERENCES ecommerce.orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES ecommerce.products(product_id) ON DELETE RESTRICT
);

CREATE INDEX idx_order_items_order_id ON ecommerce.order_items(order_id);
CREATE INDEX idx_order_items_product_id ON ecommerce.order_items(product_id);

-- ============================================================================
-- Table: payments - Payment transactions
-- ============================================================================

CREATE TABLE IF NOT EXISTS ecommerce.payments (
    payment_id VARCHAR(100) PRIMARY KEY,
    order_id VARCHAR(100) NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    amount NUMERIC(12, 2) NOT NULL CHECK (amount >= 0),
    payment_method VARCHAR(50) NOT NULL,
    payment_status VARCHAR(50) DEFAULT 'INITIATED',
    currency VARCHAR(3) DEFAULT 'USD',
    card_last_four VARCHAR(4),
    card_brand VARCHAR(50),
    fraud_score NUMERIC(5, 4),
    ip_address VARCHAR(45),
    payment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    failure_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (order_id) REFERENCES ecommerce.orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES ecommerce.users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_payments_order_id ON ecommerce.payments(order_id);
CREATE INDEX idx_payments_user_id ON ecommerce.payments(user_id);
CREATE INDEX idx_payments_status ON ecommerce.payments(payment_status);
CREATE INDEX idx_payments_date ON ecommerce.payments(payment_date);
CREATE INDEX idx_payments_fraud_score ON ecommerce.payments(fraud_score);

-- ============================================================================
-- Table: user_events - User activity log (from Kafka)
-- ============================================================================

CREATE TABLE IF NOT EXISTS ecommerce.user_events (
    event_id VARCHAR(100) PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    event_timestamp TIMESTAMP NOT NULL,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES ecommerce.users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_user_events_user_id ON ecommerce.user_events(user_id);
CREATE INDEX idx_user_events_type ON ecommerce.user_events(event_type);
CREATE INDEX idx_user_events_timestamp ON ecommerce.user_events(event_timestamp);
CREATE INDEX idx_user_events_metadata ON ecommerce.user_events USING GIN (metadata);

-- ============================================================================
-- Table: inventory_changes - Product inventory history
-- ============================================================================

CREATE TABLE IF NOT EXISTS ecommerce.inventory_changes (
    change_id SERIAL PRIMARY KEY,
    product_id VARCHAR(100) NOT NULL,
    quantity_change INTEGER NOT NULL,
    old_quantity INTEGER,
    new_quantity INTEGER,
    warehouse_location VARCHAR(50),
    change_reason VARCHAR(100),
    change_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (product_id) REFERENCES ecommerce.products(product_id) ON DELETE CASCADE
);

CREATE INDEX idx_inventory_product_id ON ecommerce.inventory_changes(product_id);
CREATE INDEX idx_inventory_timestamp ON ecommerce.inventory_changes(change_timestamp);
CREATE INDEX idx_inventory_warehouse ON ecommerce.inventory_changes(warehouse_location);

-- ============================================================================
-- Table: stream_aggregations - Real-time aggregated metrics from Faust
-- ============================================================================

CREATE TABLE IF NOT EXISTS ecommerce.stream_aggregations (
    aggregation_id SERIAL PRIMARY KEY,
    aggregation_type VARCHAR(50) NOT NULL,
    window_start TIMESTAMP NOT NULL,
    window_end TIMESTAMP NOT NULL,
    metric_name VARCHAR(100) NOT NULL,
    metric_value NUMERIC(20, 4),
    dimensions JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(aggregation_type, window_start, window_end, metric_name)
);

CREATE INDEX idx_aggregations_type ON ecommerce.stream_aggregations(aggregation_type);
CREATE INDEX idx_aggregations_window ON ecommerce.stream_aggregations(window_start, window_end);
CREATE INDEX idx_aggregations_metric ON ecommerce.stream_aggregations(metric_name);
CREATE INDEX idx_aggregations_dimensions ON ecommerce.stream_aggregations USING GIN (dimensions);

-- ============================================================================
-- Views: Analytics views for common queries
-- ============================================================================

-- Daily sales summary
CREATE OR REPLACE VIEW ecommerce.daily_sales AS
SELECT
    DATE(order_date) AS sale_date,
    COUNT(DISTINCT order_id) AS total_orders,
    COUNT(DISTINCT user_id) AS unique_customers,
    SUM(total_amount) AS total_revenue,
    AVG(total_amount) AS avg_order_value,
    MAX(total_amount) AS max_order_value,
    MIN(total_amount) AS min_order_value
FROM ecommerce.orders
WHERE order_status NOT IN ('CANCELLED', 'FAILED')
GROUP BY DATE(order_date)
ORDER BY sale_date DESC;

-- Product performance
CREATE OR REPLACE VIEW ecommerce.product_performance AS
SELECT
    p.product_id,
    p.product_name,
    p.category,
    p.price,
    p.stock_quantity,
    COUNT(DISTINCT oi.order_id) AS times_ordered,
    SUM(oi.quantity) AS total_quantity_sold,
    SUM(oi.total_price) AS total_revenue
FROM ecommerce.products p
LEFT JOIN ecommerce.order_items oi ON p.product_id = oi.product_id
GROUP BY p.product_id, p.product_name, p.category, p.price, p.stock_quantity
ORDER BY total_revenue DESC NULLS LAST;

-- User purchase behavior
CREATE OR REPLACE VIEW ecommerce.user_purchase_behavior AS
SELECT
    u.user_id,
    u.email,
    u.country,
    COUNT(DISTINCT o.order_id) AS total_orders,
    SUM(o.total_amount) AS total_spent,
    AVG(o.total_amount) AS avg_order_value,
    MIN(o.order_date) AS first_order_date,
    MAX(o.order_date) AS last_order_date,
    MAX(o.order_date) - MIN(o.order_date) AS customer_lifetime_days
FROM ecommerce.users u
LEFT JOIN ecommerce.orders o ON u.user_id = o.user_id
GROUP BY u.user_id, u.email, u.country
ORDER BY total_spent DESC NULLS LAST;

-- Payment method analysis
CREATE OR REPLACE VIEW ecommerce.payment_method_analysis AS
SELECT
    payment_method,
    payment_status,
    COUNT(*) AS transaction_count,
    SUM(amount) AS total_amount,
    AVG(amount) AS avg_amount,
    AVG(fraud_score) AS avg_fraud_score,
    SUM(CASE WHEN payment_status = 'COMPLETED' THEN 1 ELSE 0 END)::FLOAT /
        NULLIF(COUNT(*), 0) * 100 AS success_rate
FROM ecommerce.payments
GROUP BY payment_method, payment_status
ORDER BY payment_method, payment_status;

-- ============================================================================
-- Functions: Trigger functions for automatic timestamps
-- ============================================================================

CREATE OR REPLACE FUNCTION ecommerce.update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply update triggers to all tables with updated_at
CREATE TRIGGER update_users_modtime
    BEFORE UPDATE ON ecommerce.users
    FOR EACH ROW
    EXECUTE FUNCTION ecommerce.update_modified_column();

CREATE TRIGGER update_products_modtime
    BEFORE UPDATE ON ecommerce.products
    FOR EACH ROW
    EXECUTE FUNCTION ecommerce.update_modified_column();

CREATE TRIGGER update_orders_modtime
    BEFORE UPDATE ON ecommerce.orders
    FOR EACH ROW
    EXECUTE FUNCTION ecommerce.update_modified_column();

CREATE TRIGGER update_payments_modtime
    BEFORE UPDATE ON ecommerce.payments
    FOR EACH ROW
    EXECUTE FUNCTION ecommerce.update_modified_column();

-- ============================================================================
-- Sample Data: Insert seed data for testing
-- ============================================================================

-- Insert sample users
INSERT INTO ecommerce.users (user_id, email, username, country)
VALUES
    ('user_00000001', 'alice@example.com', 'alice', 'US'),
    ('user_00000002', 'bob@example.com', 'bob', 'GB'),
    ('user_00000003', 'charlie@example.com', 'charlie', 'CA')
ON CONFLICT (user_id) DO NOTHING;

-- Insert sample products
INSERT INTO ecommerce.products (product_id, product_name, category, price, stock_quantity, warehouse_location)
VALUES
    ('prod_00000001', 'Laptop Pro', 'Electronics', 1299.99, 50, 'WH-1'),
    ('prod_00000002', 'Wireless Mouse', 'Accessories', 29.99, 200, 'WH-1'),
    ('prod_00000003', 'USB-C Cable', 'Accessories', 12.99, 500, 'WH-2'),
    ('prod_00000004', 'Mechanical Keyboard', 'Accessories', 89.99, 100, 'WH-1'),
    ('prod_00000005', 'Monitor 27"', 'Electronics', 349.99, 75, 'WH-2')
ON CONFLICT (product_id) DO NOTHING;

-- ============================================================================
-- Grants: Set up permissions
-- ============================================================================

-- Grant usage on schema
GRANT USAGE ON SCHEMA ecommerce TO analytics_user;

-- Grant select on all tables
GRANT SELECT ON ALL TABLES IN SCHEMA ecommerce TO analytics_user;

-- Grant select, insert, update on tables (for Kafka Connect)
GRANT SELECT, INSERT, UPDATE ON ALL TABLES IN SCHEMA ecommerce TO analytics_user;

-- Grant usage on sequences
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA ecommerce TO analytics_user;

-- Set default privileges for future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA ecommerce
    GRANT SELECT, INSERT, UPDATE ON TABLES TO analytics_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA ecommerce
    GRANT USAGE, SELECT ON SEQUENCES TO analytics_user;

-- ============================================================================
-- Verification: Display summary
-- ============================================================================

DO $$
DECLARE
    table_count INTEGER;
    view_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO table_count
    FROM information_schema.tables
    WHERE table_schema = 'ecommerce' AND table_type = 'BASE TABLE';

    SELECT COUNT(*) INTO view_count
    FROM information_schema.views
    WHERE table_schema = 'ecommerce';

    RAISE NOTICE '========================================';
    RAISE NOTICE 'E-Commerce Analytics Database Initialized';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Schema: ecommerce';
    RAISE NOTICE 'Tables: %', table_count;
    RAISE NOTICE 'Views: %', view_count;
    RAISE NOTICE 'Extensions: uuid-ossp, pg_stat_statements';
    RAISE NOTICE '========================================';
END $$;

-- ============================================================================
-- End of initialization script
-- ============================================================================
