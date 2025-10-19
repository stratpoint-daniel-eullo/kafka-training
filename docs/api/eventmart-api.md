# EventMart API Reference

> **⚠️ Java Developer Track Only**
> EventMart is a Spring Boot project for Java developers. Data engineers focus on pure Kafka patterns in the core training.

EventMart is the e-commerce platform demonstrating real-world Kafka integration.

## Domain Events

### Order Events
- `order-created` - New order placed
- `order-confirmed` - Payment confirmed
- `order-shipped` - Order dispatched
- `order-delivered` - Customer received order
- `order-cancelled` - Order cancelled

### Customer Events
- `customer-registered` - New customer account
- `customer-updated` - Profile updated
- `customer-deleted` - Account deleted

### Product Events
- `product-created` - New product added
- `product-updated` - Product details changed
- `product-deleted` - Product removed

## Event Schemas

### Order Event Schema
```json
{
  "orderId": "string",
  "customerId": "string",
  "items": [
    {
      "productId": "string",
      "quantity": "number",
      "price": "number"
    }
  ],
  "totalAmount": "number",
  "status": "string",
  "timestamp": "string (ISO 8601)"
}
```

## Next Steps

See [Training Endpoints](training-endpoints.md) for Kafka operation APIs.
