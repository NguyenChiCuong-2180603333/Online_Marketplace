package com.marketplace.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Document(collection = "carts")
public class Cart {
    @Id
    private String id;

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotNull(message = "Items list cannot be null")
    private List<CartItem> items = new ArrayList<>();

    @DecimalMin(value = "0.0", message = "Total amount cannot be negative")
    private Double totalAmount = 0.0;

    @Min(value = 0, message = "Total items cannot be negative")
    private int totalItems = 0;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Inner class for cart items
    public static class CartItem {
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

        // Constructors
        public CartItem() {}

        public CartItem(String productId, String productName, Double productPrice, String productImage, int quantity) {
            this.productId = productId;
            this.productName = productName;
            this.productPrice = productPrice;
            this.productImage = productImage;
            this.quantity = quantity;
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
        public void setQuantity(int quantity) {
            this.quantity = quantity;
            this.subtotal = this.productPrice * quantity;
        }

        public Double getSubtotal() { return subtotal; }
        public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
    }

    // Constructors
    public Cart() {}

    public Cart(String userId) {
        this.userId = userId;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public int getTotalItems() { return totalItems; }
    public void setTotalItems(int totalItems) { this.totalItems = totalItems; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}