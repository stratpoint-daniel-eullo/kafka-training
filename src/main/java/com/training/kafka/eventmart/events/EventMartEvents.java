package com.training.kafka.eventmart.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * EventMart Event Definitions
 * 
 * Day 2 Deliverable: Define all event schemas and message structures
 * 
 * This class contains all event types used in the EventMart platform:
 * - User events (registration, updates, deletion)
 * - Product events (creation, updates, inventory changes)
 * - Order events (placement, confirmation, shipping, delivery)
 * - Payment events (initiation, completion, failure)
 * - Notification events (email, SMS, push notifications)
 */
public class EventMartEvents {
    
    private static final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    
    // Base Event Interface
    public interface EventMartEvent {
        String getEventId();
        String getEventType();
        Instant getTimestamp();
        String toJson();
    }
    
    // Abstract Base Event
    public static abstract class BaseEvent implements EventMartEvent {
        @JsonProperty("event_id")
        protected String eventId;
        
        @JsonProperty("event_type")
        protected String eventType;
        
        @JsonProperty("timestamp")
        protected Instant timestamp;
        
        @JsonProperty("version")
        protected String version = "1.0";
        
        public BaseEvent(String eventType) {
            this.eventId = UUID.randomUUID().toString();
            this.eventType = eventType;
            this.timestamp = Instant.now();
        }
        
        @Override
        public String getEventId() { return eventId; }
        
        @Override
        public String getEventType() { return eventType; }
        
        @Override
        public Instant getTimestamp() { return timestamp; }
        
        @Override
        public String toJson() {
            try {
                return objectMapper.writeValueAsString(this);
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize event", e);
            }
        }
    }
    
    // User Events
    public static class UserRegistered extends BaseEvent {
        @JsonProperty("user_id")
        public String userId;
        
        @JsonProperty("email")
        public String email;
        
        @JsonProperty("first_name")
        public String firstName;
        
        @JsonProperty("last_name")
        public String lastName;
        
        @JsonProperty("registration_source")
        public String registrationSource;
        
        public UserRegistered(String userId, String email, String firstName, String lastName, String source) {
            super("user-registered");
            this.userId = userId;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.registrationSource = source;
        }
    }
    
    public static class UserUpdated extends BaseEvent {
        @JsonProperty("user_id")
        public String userId;
        
        @JsonProperty("updated_fields")
        public Map<String, Object> updatedFields;
        
        public UserUpdated(String userId, Map<String, Object> updatedFields) {
            super("user-updated");
            this.userId = userId;
            this.updatedFields = updatedFields;
        }
    }
    
    // Product Events
    public static class ProductCreated extends BaseEvent {
        @JsonProperty("product_id")
        public String productId;
        
        @JsonProperty("name")
        public String name;
        
        @JsonProperty("category")
        public String category;
        
        @JsonProperty("price")
        public double price;
        
        @JsonProperty("initial_inventory")
        public int initialInventory;
        
        public ProductCreated(String productId, String name, String category, double price, int inventory) {
            super("product-created");
            this.productId = productId;
            this.name = name;
            this.category = category;
            this.price = price;
            this.initialInventory = inventory;
        }
    }
    
    public static class InventoryChanged extends BaseEvent {
        @JsonProperty("product_id")
        public String productId;
        
        @JsonProperty("previous_quantity")
        public int previousQuantity;
        
        @JsonProperty("new_quantity")
        public int newQuantity;
        
        @JsonProperty("change_reason")
        public String changeReason;
        
        public InventoryChanged(String productId, int previousQty, int newQty, String reason) {
            super("inventory-changed");
            this.productId = productId;
            this.previousQuantity = previousQty;
            this.newQuantity = newQty;
            this.changeReason = reason;
        }
    }
    
    // Order Events
    public static class OrderPlaced extends BaseEvent {
        @JsonProperty("order_id")
        public String orderId;
        
        @JsonProperty("user_id")
        public String userId;
        
        @JsonProperty("items")
        public OrderItem[] items;
        
        @JsonProperty("total_amount")
        public double totalAmount;
        
        @JsonProperty("shipping_address")
        public Address shippingAddress;
        
        public OrderPlaced(String orderId, String userId, OrderItem[] items, double total, Address address) {
            super("order-placed");
            this.orderId = orderId;
            this.userId = userId;
            this.items = items;
            this.totalAmount = total;
            this.shippingAddress = address;
        }
    }
    
    public static class OrderConfirmed extends BaseEvent {
        @JsonProperty("order_id")
        public String orderId;
        
        @JsonProperty("confirmation_number")
        public String confirmationNumber;
        
        @JsonProperty("estimated_delivery")
        public Instant estimatedDelivery;
        
        public OrderConfirmed(String orderId, String confirmationNumber, Instant estimatedDelivery) {
            super("order-confirmed");
            this.orderId = orderId;
            this.confirmationNumber = confirmationNumber;
            this.estimatedDelivery = estimatedDelivery;
        }
    }
    
    // Payment Events
    public static class PaymentInitiated extends BaseEvent {
        @JsonProperty("payment_id")
        public String paymentId;
        
        @JsonProperty("order_id")
        public String orderId;
        
        @JsonProperty("amount")
        public double amount;
        
        @JsonProperty("payment_method")
        public String paymentMethod;
        
        public PaymentInitiated(String paymentId, String orderId, double amount, String method) {
            super("payment-initiated");
            this.paymentId = paymentId;
            this.orderId = orderId;
            this.amount = amount;
            this.paymentMethod = method;
        }
    }
    
    public static class PaymentCompleted extends BaseEvent {
        @JsonProperty("payment_id")
        public String paymentId;
        
        @JsonProperty("order_id")
        public String orderId;
        
        @JsonProperty("transaction_id")
        public String transactionId;
        
        @JsonProperty("amount")
        public double amount;
        
        public PaymentCompleted(String paymentId, String orderId, String transactionId, double amount) {
            super("payment-completed");
            this.paymentId = paymentId;
            this.orderId = orderId;
            this.transactionId = transactionId;
            this.amount = amount;
        }
    }
    
    // Notification Events
    public static class NotificationSent extends BaseEvent {
        @JsonProperty("notification_id")
        public String notificationId;
        
        @JsonProperty("user_id")
        public String userId;
        
        @JsonProperty("type")
        public String type; // email, sms, push
        
        @JsonProperty("subject")
        public String subject;
        
        @JsonProperty("content")
        public String content;
        
        public NotificationSent(String notificationId, String userId, String type, String subject, String content) {
            super("notification-sent");
            this.notificationId = notificationId;
            this.userId = userId;
            this.type = type;
            this.subject = subject;
            this.content = content;
        }
    }
    
    // Supporting Classes
    public static class OrderItem {
        @JsonProperty("product_id")
        public String productId;
        
        @JsonProperty("quantity")
        public int quantity;
        
        @JsonProperty("unit_price")
        public double unitPrice;
        
        public OrderItem(String productId, int quantity, double unitPrice) {
            this.productId = productId;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
    }
    
    public static class Address {
        @JsonProperty("street")
        public String street;
        
        @JsonProperty("city")
        public String city;
        
        @JsonProperty("state")
        public String state;
        
        @JsonProperty("zip_code")
        public String zipCode;
        
        @JsonProperty("country")
        public String country;
        
        public Address(String street, String city, String state, String zipCode, String country) {
            this.street = street;
            this.city = city;
            this.state = state;
            this.zipCode = zipCode;
            this.country = country;
        }
    }
}
