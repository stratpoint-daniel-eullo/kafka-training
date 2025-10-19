"""
Sample Data Generator

Generates realistic sample data for e-commerce events.
Used for demos and testing.
"""

import random
import uuid
from typing import Dict, Any, List
from decimal import Decimal
from faker import Faker

# Initialize Faker for realistic data
fake = Faker()


class SampleDataGenerator:
    """
    Generate sample e-commerce data for testing and demos.

    Generates:
    - User events (registrations, logins)
    - Order events (placements, confirmations)
    - Product events (creation, inventory changes)
    - Payment events (transactions, refunds)
    """

    # Sample product catalog
    PRODUCTS = [
        {"name": "Laptop Pro", "category": "Electronics", "price": 1299.99},
        {"name": "Wireless Mouse", "category": "Accessories", "price": 29.99},
        {"name": "USB-C Cable", "category": "Accessories", "price": 12.99},
        {"name": "Mechanical Keyboard", "category": "Accessories", "price": 89.99},
        {"name": "Monitor 27\"", "category": "Electronics", "price": 349.99},
        {"name": "Webcam HD", "category": "Electronics", "price": 79.99},
        {"name": "Desk Lamp", "category": "Office", "price": 39.99},
        {"name": "Office Chair", "category": "Furniture", "price": 249.99},
        {"name": "Notebook Set", "category": "Stationery", "price": 15.99},
        {"name": "Pen Pack", "category": "Stationery", "price": 9.99},
    ]

    COUNTRIES = ["US", "GB", "CA", "AU", "DE", "FR", "JP", "BR"]

    PAYMENT_METHODS = ["CREDIT_CARD", "DEBIT_CARD", "PAYPAL", "BANK_TRANSFER"]

    def __init__(self, seed: int = None):
        """
        Initialize data generator.

        Args:
            seed: Random seed for reproducibility
        """
        if seed:
            random.seed(seed)
            Faker.seed(seed)

        self.user_pool: List[str] = []
        self.product_pool: List[str] = []

    def generate_user_id(self) -> str:
        """Generate a user ID."""
        user_id = f"user_{uuid.uuid4().hex[:8]}"
        self.user_pool.append(user_id)
        return user_id

    def generate_product_id(self) -> str:
        """Generate a product ID."""
        product_id = f"prod_{uuid.uuid4().hex[:8]}"
        self.product_pool.append(product_id)
        return product_id

    def get_random_user_id(self) -> str:
        """Get a random existing user ID or create new one."""
        if self.user_pool and random.random() > 0.3:
            return random.choice(self.user_pool)
        return self.generate_user_id()

    def get_random_product_id(self) -> str:
        """Get a random existing product ID or create new one."""
        if self.product_pool and random.random() > 0.2:
            return random.choice(self.product_pool)
        return self.generate_product_id()

    def generate_user_registration(self) -> Dict[str, Any]:
        """Generate user registration data."""
        return {
            "user_id": self.generate_user_id(),
            "email": fake.email(),
            "username": fake.user_name(),
            "country": random.choice(self.COUNTRIES),
        }

    def generate_user_login(self) -> Dict[str, Any]:
        """Generate user login data."""
        return {
            "user_id": self.get_random_user_id(),
            "metadata": {
                "ip_address": fake.ipv4(),
                "device": random.choice(["mobile", "desktop", "tablet"]),
            },
        }

    def generate_product_creation(self) -> Dict[str, Any]:
        """Generate product creation data."""
        product = random.choice(self.PRODUCTS)

        return {
            "product_id": self.generate_product_id(),
            "product_name": product["name"],
            "category": product["category"],
            "price": Decimal(str(product["price"])),
            "stock_quantity": random.randint(10, 100),
            "currency": "USD",
            "warehouse_location": f"WH-{random.randint(1, 5)}",
            "supplier_id": f"supplier_{random.randint(100, 999)}",
        }

    def generate_inventory_change(self) -> Dict[str, Any]:
        """Generate inventory change data."""
        product_id = self.get_random_product_id()
        change = random.randint(1, 20)

        return {
            "product_id": product_id,
            "quantity_change": change,
            "new_stock_quantity": random.randint(0, 100),
            "warehouse_location": f"WH-{random.randint(1, 5)}",
        }

    def generate_order(self) -> Dict[str, Any]:
        """Generate order data."""
        # Generate order items
        num_items = random.randint(1, 5)
        items = []
        total = Decimal("0.00")

        for _ in range(num_items):
            product = random.choice(self.PRODUCTS)
            quantity = random.randint(1, 3)
            unit_price = Decimal(str(product["price"]))
            item_total = unit_price * quantity

            items.append({
                "product_id": self.get_random_product_id(),
                "product_name": product["name"],
                "quantity": quantity,
                "unit_price": unit_price,
            })

            total += item_total

        return {
            "order_id": f"order_{uuid.uuid4().hex[:12]}",
            "user_id": self.get_random_user_id(),
            "items": items,
            "total_amount": total,
            "currency": "USD",
            "shipping_address": {
                "street": fake.street_address(),
                "city": fake.city(),
                "state": fake.state_abbr(),
                "postal_code": fake.postcode(),
                "country": random.choice(self.COUNTRIES),
            },
            "payment_method": random.choice(["credit_card", "paypal", "bank_transfer"]),
        }

    def generate_payment(self, order_data: Dict[str, Any] = None) -> Dict[str, Any]:
        """
        Generate payment data.

        Args:
            order_data: Optional order data to link payment to

        Returns:
            Payment data
        """
        if order_data is None:
            order_data = self.generate_order()

        payment_method = random.choice(self.PAYMENT_METHODS)

        payment = {
            "payment_id": f"pay_{uuid.uuid4().hex[:12]}",
            "order_id": order_data.get("order_id", f"order_{uuid.uuid4().hex[:12]}"),
            "user_id": order_data.get("user_id", self.get_random_user_id()),
            "amount": order_data.get("total_amount", Decimal("99.99")),
            "payment_method": payment_method,
            "currency": "USD",
            "billing_address": {
                "street": fake.street_address(),
                "city": fake.city(),
                "state": fake.state_abbr(),
                "postal_code": fake.postcode(),
                "country": random.choice(self.COUNTRIES),
            },
            "ip_address": fake.ipv4(),
            "device_fingerprint": uuid.uuid4().hex,
        }

        # Add card details for card payments
        if "CARD" in payment_method:
            payment["card_last_four"] = f"{random.randint(1000, 9999)}"
            payment["card_brand"] = random.choice(["Visa", "Mastercard", "Amex"])

        # Random fraud score (mostly low, occasional high)
        if random.random() < 0.05:  # 5% suspicious
            payment["fraud_score"] = random.uniform(0.7, 0.95)
        else:
            payment["fraud_score"] = random.uniform(0.0, 0.3)

        return payment

    def generate_batch(
        self,
        num_users: int = 10,
        num_products: int = 20,
        num_orders: int = 50,
        num_payments: int = 50
    ) -> Dict[str, List[Dict[str, Any]]]:
        """
        Generate a batch of sample data.

        Args:
            num_users: Number of users to generate
            num_products: Number of products to generate
            num_orders: Number of orders to generate
            num_payments: Number of payments to generate

        Returns:
            Dictionary with lists of generated data
        """
        return {
            "users": [self.generate_user_registration() for _ in range(num_users)],
            "products": [self.generate_product_creation() for _ in range(num_products)],
            "orders": [self.generate_order() for _ in range(num_orders)],
            "payments": [self.generate_payment() for _ in range(num_payments)],
        }
