package com.marketplace.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Document(collection = "point_transactions")
public class PointTransaction {
    @Id
    private String id;

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotNull(message = "Points amount is required")
    private int points;

    @Pattern(regexp = "EARN|SPEND|EXPIRE|REFUND|BONUS",
            message = "Invalid transaction type")
    private String type;

    @Pattern(regexp = "PURCHASE|REVIEW|REFERRAL|SIGNUP_BONUS|REDEEM_VOUCHER|ORDER_REFUND|ADMIN_ADJUSTMENT",
            message = "Invalid reason")
    private String reason;

    private String description;
    private String relatedId; // Order ID, Product ID, etc.
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime expiresAt;

    // Constructors
    public PointTransaction() {}

    public PointTransaction(String userId, int points, String type, String reason) {
        this.userId = userId;
        this.points = points;
        this.type = type;
        this.reason = reason;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRelatedId() { return relatedId; }
    public void setRelatedId(String relatedId) { this.relatedId = relatedId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
