package com.marketplace.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Document(collection = "notifications")
public class Notification {
    @Id
    private String id;

    private String userId; // null nếu là thông báo global

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;

    @NotBlank(message = "Message is required")
    @Size(max = 500, message = "Message must not exceed 500 characters")
    private String message;

    @Pattern(regexp = "NEW_PRODUCT|PROMOTION|ORDER_UPDATE|SYSTEM_UPDATE",
            message = "Type must be NEW_PRODUCT, PROMOTION, ORDER_UPDATE, or SYSTEM_UPDATE")
    private String type; // NEW_PRODUCT, PROMOTION, ORDER_UPDATE, SYSTEM_UPDATE

    private String relatedId; // ID của product, order, etc.
    private boolean isRead = false;
    private boolean isGlobal = false; // Thông báo cho tất cả user
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime readAt;

    // Constructors
    public Notification() {}

    public Notification(String userId, String title, String message, String type) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
    }

    public Notification(String title, String message, String type, boolean isGlobal) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.isGlobal = isGlobal;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getRelatedId() { return relatedId; }
    public void setRelatedId(String relatedId) { this.relatedId = relatedId; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public boolean isGlobal() { return isGlobal; }
    public void setGlobal(boolean global) { isGlobal = global; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
}