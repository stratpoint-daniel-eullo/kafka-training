-- EventMart Database Initialization Script
-- Day 7: Kafka Connect - PostgreSQL Schema Setup
-- This script creates tables for JDBC Source and Sink connectors

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ====================
-- Users Table
-- ====================
CREATE TABLE IF NOT EXISTS users (
    user_id VARCHAR(50) PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(200),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    account_type VARCHAR(50) DEFAULT 'STANDARD',
    preferences JSONB,
    version INTEGER DEFAULT 1
);

-- Index for user queries
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_created_at ON users(created_at);

-- ====================
-- Products Table
-- ====================
CREATE TABLE IF NOT EXISTS products (
    product_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    stock_quantity INTEGER NOT NULL DEFAULT 0,
    vendor_id VARCHAR(50),
    vendor_name VARCHAR(200),
    attributes JSONB,
    tags TEXT[],
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 1
);

-- Indexes for product queries
CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_products_price ON products(price);
CREATE INDEX idx_products_stock ON products(stock_quantity);
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_products_vendor_id ON products(vendor_id);

-- ====================
-- Orders Table
-- ====================
CREATE TABLE IF NOT EXISTS orders (
    order_id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL REFERENCES users(user_id),
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    order_items JSONB NOT NULL,
    shipping_address JSONB,
    payment_id VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    shipped_at TIMESTAMP,
    delivered_at TIMESTAMP,
    metadata JSONB,
    version INTEGER DEFAULT 1
);

-- Indexes for order queries
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_orders_payment_id ON orders(payment_id);

-- ====================
-- Payments Table
-- ====================
CREATE TABLE IF NOT EXISTS payments (
    payment_id VARCHAR(50) PRIMARY KEY,
    order_id VARCHAR(50) NOT NULL REFERENCES orders(order_id),
    user_id VARCHAR(50) NOT NULL REFERENCES users(user_id),
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'USD',
    payment_method VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    processor_transaction_id VARCHAR(100),
    card_type VARCHAR(50),
    last_four_digits VARCHAR(4),
    risk_score DECIMAL(3, 2),
    error_code VARCHAR(50),
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    metadata JSONB,
    version INTEGER DEFAULT 1
);

-- Indexes for payment queries
CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_user_id ON payments(user_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_created_at ON payments(created_at);
CREATE INDEX idx_payments_processor_txn_id ON payments(processor_transaction_id);

-- ====================
-- User Activity Log Table (for CDC - Change Data Capture)
-- ====================
CREATE TABLE IF NOT EXISTS user_activity_log (
    id SERIAL PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    session_id VARCHAR(100),
    device_type VARCHAR(50),
    ip_address VARCHAR(45),
    location VARCHAR(200),
    properties JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for activity log queries
CREATE INDEX idx_activity_log_user_id ON user_activity_log(user_id);
CREATE INDEX idx_activity_log_action ON user_activity_log(action);
CREATE INDEX idx_activity_log_created_at ON user_activity_log(created_at);

-- ====================
-- Product Events Table (for event tracking)
-- ====================
CREATE TABLE IF NOT EXISTS product_events (
    id SERIAL PRIMARY KEY,
    product_id VARCHAR(50) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    previous_price DECIMAL(10, 2),
    new_price DECIMAL(10, 2),
    previous_stock INTEGER,
    new_stock INTEGER,
    changed_by VARCHAR(50),
    change_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for product events
CREATE INDEX idx_product_events_product_id ON product_events(product_id);
CREATE INDEX idx_product_events_type ON product_events(event_type);
CREATE INDEX idx_product_events_created_at ON product_events(created_at);

-- ====================
-- Sample Data for Testing
-- ====================

-- Insert sample users
INSERT INTO users (user_id, username, email, full_name, account_type) VALUES
    ('user-001', 'alice.smith', 'alice.smith@eventmart.com', 'Alice Smith', 'PREMIUM'),
    ('user-002', 'bob.jones', 'bob.jones@eventmart.com', 'Bob Jones', 'STANDARD'),
    ('user-003', 'carol.white', 'carol.white@eventmart.com', 'Carol White', 'PREMIUM')
ON CONFLICT (user_id) DO NOTHING;

-- Insert sample products
INSERT INTO products (product_id, name, category, description, price, stock_quantity, vendor_id, vendor_name, tags) VALUES
    ('prod-001', 'Wireless Headphones', 'Electronics', 'Premium wireless headphones with noise cancellation', 99.99, 50, 'vendor-001', 'TechVendor Inc', ARRAY['electronics', 'audio', 'wireless']),
    ('prod-002', 'Smart Watch', 'Electronics', 'Fitness tracking smart watch with heart rate monitor', 199.99, 30, 'vendor-001', 'TechVendor Inc', ARRAY['electronics', 'wearable', 'fitness']),
    ('prod-003', 'Running Shoes', 'Sports', 'Professional running shoes with advanced cushioning', 89.99, 100, 'vendor-002', 'SportGear Co', ARRAY['sports', 'footwear', 'running']),
    ('prod-004', 'Yoga Mat', 'Sports', 'Non-slip yoga mat with carrying strap', 29.99, 200, 'vendor-002', 'SportGear Co', ARRAY['sports', 'yoga', 'fitness']),
    ('prod-005', 'Coffee Maker', 'Home', 'Programmable coffee maker with thermal carafe', 79.99, 45, 'vendor-003', 'HomeGoods LLC', ARRAY['home', 'kitchen', 'appliance'])
ON CONFLICT (product_id) DO NOTHING;

-- Insert sample orders (note: orders reference users, so they must exist first)
INSERT INTO orders (order_id, user_id, total_amount, status, order_items, shipping_address, payment_id) VALUES
    ('order-001', 'user-001', 199.98, 'DELIVERED',
     '[{"productId": "prod-001", "productName": "Wireless Headphones", "quantity": 2, "pricePerUnit": 99.99, "totalPrice": 199.98}]'::jsonb,
     '{"street": "123 Main St", "city": "San Francisco", "state": "CA", "zipCode": "94102", "country": "USA"}'::jsonb,
     'pay-001'),
    ('order-002', 'user-002', 89.99, 'SHIPPED',
     '[{"productId": "prod-003", "productName": "Running Shoes", "quantity": 1, "pricePerUnit": 89.99, "totalPrice": 89.99}]'::jsonb,
     '{"street": "456 Oak Ave", "city": "Portland", "state": "OR", "zipCode": "97201", "country": "USA"}'::jsonb,
     'pay-002'),
    ('order-003', 'user-003', 109.98, 'PENDING',
     '[{"productId": "prod-004", "productName": "Yoga Mat", "quantity": 2, "pricePerUnit": 29.99, "totalPrice": 59.98}, {"productId": "prod-001", "productName": "Wireless Headphones", "quantity": 1, "pricePerUnit": 99.99, "totalPrice": 99.99}]'::jsonb,
     '{"street": "789 Pine Rd", "city": "Seattle", "state": "WA", "zipCode": "98101", "country": "USA"}'::jsonb,
     'pay-003')
ON CONFLICT (order_id) DO NOTHING;

-- Insert sample payments (note: payments reference both orders and users)
INSERT INTO payments (payment_id, order_id, user_id, amount, payment_method, status, card_type, last_four_digits, risk_score, processed_at) VALUES
    ('pay-001', 'order-001', 'user-001', 199.98, 'CREDIT_CARD', 'CAPTURED', 'VISA', '1234', 0.15, CURRENT_TIMESTAMP - INTERVAL '5 days'),
    ('pay-002', 'order-002', 'user-002', 89.99, 'CREDIT_CARD', 'CAPTURED', 'MASTERCARD', '5678', 0.12, CURRENT_TIMESTAMP - INTERVAL '2 days'),
    ('pay-003', 'order-003', 'user-003', 109.98, 'PAYPAL', 'AUTHORIZED', NULL, NULL, 0.08, NULL)
ON CONFLICT (payment_id) DO NOTHING;

-- Insert sample user activity
INSERT INTO user_activity_log (user_id, action, session_id, device_type, ip_address, location) VALUES
    ('user-001', 'LOGIN', 'session-' || uuid_generate_v4(), 'Desktop', '192.168.1.100', 'San Francisco, CA'),
    ('user-001', 'PURCHASE', 'session-' || uuid_generate_v4(), 'Desktop', '192.168.1.100', 'San Francisco, CA'),
    ('user-002', 'LOGIN', 'session-' || uuid_generate_v4(), 'Mobile', '192.168.1.101', 'Portland, OR'),
    ('user-002', 'SEARCH', 'session-' || uuid_generate_v4(), 'Mobile', '192.168.1.101', 'Portland, OR'),
    ('user-003', 'PAGE_VIEW', 'session-' || uuid_generate_v4(), 'Tablet', '192.168.1.102', 'Seattle, WA');

-- Insert sample product events
INSERT INTO product_events (product_id, event_type, previous_price, new_price, previous_stock, new_stock, changed_by, change_reason) VALUES
    ('prod-001', 'PRICE_CHANGED', 109.99, 99.99, NULL, NULL, 'admin', 'Seasonal discount'),
    ('prod-001', 'STOCK_CHANGED', NULL, NULL, 60, 50, 'system', 'Sales'),
    ('prod-003', 'STOCK_CHANGED', NULL, NULL, 120, 100, 'system', 'Sales'),
    ('prod-005', 'CREATED', NULL, 79.99, NULL, 45, 'admin', 'New product launch');

-- ====================
-- Functions for timestamp updates
-- ====================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers for updated_at
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_products_updated_at BEFORE UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_orders_updated_at BEFORE UPDATE ON orders
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_payments_updated_at BEFORE UPDATE ON payments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ====================
-- Views for Analytics
-- ====================

-- View: Order summary with user and payment information
CREATE OR REPLACE VIEW order_summary AS
SELECT
    o.order_id,
    o.user_id,
    u.username,
    u.email,
    o.total_amount,
    o.status AS order_status,
    p.payment_id,
    p.payment_method,
    p.status AS payment_status,
    o.created_at AS order_date,
    o.shipped_at,
    o.delivered_at
FROM orders o
LEFT JOIN users u ON o.user_id = u.user_id
LEFT JOIN payments p ON o.payment_id = p.payment_id;

-- View: Product inventory with event count
CREATE OR REPLACE VIEW product_inventory AS
SELECT
    p.product_id,
    p.name,
    p.category,
    p.price,
    p.stock_quantity,
    p.vendor_name,
    COUNT(pe.id) AS event_count,
    MAX(pe.created_at) AS last_event_at
FROM products p
LEFT JOIN product_events pe ON p.product_id = pe.product_id
GROUP BY p.product_id, p.name, p.category, p.price, p.stock_quantity, p.vendor_name;

-- ====================
-- Grant permissions
-- ====================

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO eventmart;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO eventmart;
GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public TO eventmart;

-- ====================
-- Database initialization complete
-- ====================

-- Display initialization summary
DO $$
BEGIN
    RAISE NOTICE 'EventMart database initialized successfully!';
    RAISE NOTICE 'Tables created: users, products, orders, payments, user_activity_log, product_events';
    RAISE NOTICE 'Sample data inserted for testing';
    RAISE NOTICE 'Ready for Kafka Connect JDBC source and sink connectors';
END $$;
