package com.marketplace.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Document(collection = "orders")
public class Order {
    @Id
    private String id;

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "User email is required")
    @Email(message = "Invalid email format")
    private String userEmail;

    @NotNull(message = "Items list cannot be null")
    @NotEmpty(message = "Order must contain at least one item")
    private List<OrderItem> items = new ArrayList<>();

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Total amount must be greater than 0")
    private Double totalAmount;

    @Pattern(regexp = "PENDING|PROCESSING|SHIPPED|DELIVERED|CANCELLED",
            message = "Status must be PENDING, PROCESSING, SHIPPED, DELIVERED, or CANCELLED")
    private String status = "PENDING";

    private String paymentId; // Stripe payment intent ID

    @Pattern(regexp = "PENDING|COMPLETED|FAILED",
            message = "Payment status must be PENDING, COMPLETED, or FAILED")
    private String paymentStatus = "PENDING";

    private String shippingAddress;
    private String billingAddress;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    private LocalDateTime deliveredAt;

    // Inner class for order items
    public static class OrderItem {
        @NotBlank(message = "Product ID is required")
        private String productId;

        @NotBlank(message = "Product name is required")
        private String productName;

        @NotNull(message = "Product price is required")
        @DecimalMin(value = "0.01", message = "Product price must be greater than 0")
        private Double productPrice;

        private String productImage;

        @Min(value = 1, message = "Quantity must be at least 1")
        private int quantity;

        @DecimalMin(value = "0.01", message = "Subtotal must be greater than 0")
        private Double subtotal;

        @NotBlank(message = "Seller ID is required")
        private String sellerId;

        // Constructors
        public OrderItem() {}

        public OrderItem(String productId, String productName, Double productPrice, String productImage, int quantity, String sellerId) {
            this.productId = productId;
            this.productName = productName;
            this.productPrice = productPrice;
            this.productImage = productImage;
            this.quantity = quantity;
            this.sellerId = sellerId;
            this.subtotal = productPrice * quantity;
        }

        // Getters and Setters
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public Double getProductPrice() { return productPrice; }
        public void setProductPrice(Double productPrice) { this.productPrice = productPrice; }

        public String getProductImage() { return productImage; }
        public void setProductImage(String productImage) { this.productImage = productImage; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public Double getSubtotal() { return subtotal; }
        public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }

        public String getSellerId() { return sellerId; }
        public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    }

    // Constructors
    public Order() {}

    public Order(String userId, String userEmail, List<OrderItem> items, Double totalAmount) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.items = items;
        this.totalAmount = totalAmount;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getBillingAddress() { return billingAddress; }
    public void setBillingAddress(String billingAddress) { this.billingAddress = billingAddress; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
}