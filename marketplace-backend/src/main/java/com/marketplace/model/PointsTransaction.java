package com.marketplace.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Document(collection = "points_transactions")
public class PointsTransaction {
    @Id
    private String id;

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotNull(message = "Points amount is required")
    private int points;

    @Pattern(regexp = "EARNED|SPENT|EXPIRED|REFUNDED|BONUS",
            message = "Transaction type must be EARNED, SPENT, EXPIRED, REFUNDED, or BONUS")
    private String transactionType;

    @Pattern(regexp = "PURCHASE|REVIEW|REFERRAL|SIGNUP|BIRTHDAY|PROMOTION|REDEMPTION|EXPIRY",
            message = "Invalid reason")
    private String reason;

    @Size(max = 200, message = "Description must not exceed 200 characters")
    private String description;

    private String relatedOrderId;
    private String relatedProductId;
    private String relatedReviewId;
    private String promotionId;

    private LocalDateTime expiryDate; // Điểm có thể có hạn sử dụng
    private boolean isActive = true;
    private LocalDateTime createdAt = LocalDateTime.now();

    // Constructors
    public PointsTransaction() {}

    public PointsTransaction(String userId, int points, String transactionType, String reason) {
        this.userId = userId;
        this.points = points;
        this.transactionType = transactionType;
        this.reason = reason;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRelatedOrderId() { return relatedOrderId; }
    public void setRelatedOrderId(String relatedOrderId) { this.relatedOrderId = relatedOrderId; }

    public String getRelatedProductId() { return relatedProductId; }
    public void setRelatedProductId(String relatedProductId) { this.relatedProductId = relatedProductId; }

    public String getRelatedReviewId() { return relatedReviewId; }
    public void setRelatedReviewId(String relatedReviewId) { this.relatedReviewId = relatedReviewId; }

    public String getPromotionId() { return promotionId; }
    public void setPromotionId(String promotionId) { this.promotionId = promotionId; }

    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
