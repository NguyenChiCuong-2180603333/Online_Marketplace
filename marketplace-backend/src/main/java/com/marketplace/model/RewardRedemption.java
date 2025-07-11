package com.marketplace.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Document(collection = "reward_redemptions")
public class RewardRedemption {
    @Id
    private String id;

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Reward ID is required")
    private String rewardId;

    @Min(value = 1, message = "Points spent must be at least 1")
    private int pointsSpent;

    @Pattern(regexp = "PENDING|APPROVED|DELIVERED|USED|EXPIRED|CANCELLED",
            message = "Status must be PENDING, APPROVED, DELIVERED, USED, EXPIRED, or CANCELLED")
    private String status = "PENDING";

    private String redemptionCode; 
    private String deliveryAddress; 
    private String orderId; 

    private LocalDateTime redeemedAt = LocalDateTime.now();
    private LocalDateTime usedAt;
    private LocalDateTime expiryDate;

    public RewardRedemption() {}

    public RewardRedemption(String userId, String rewardId, int pointsSpent) {
        this.userId = userId;
        this.rewardId = rewardId;
        this.pointsSpent = pointsSpent;
        this.redemptionCode = generateRedemptionCode();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getRewardId() { return rewardId; }
    public void setRewardId(String rewardId) { this.rewardId = rewardId; }

    public int getPointsSpent() { return pointsSpent; }
    public void setPointsSpent(int pointsSpent) { this.pointsSpent = pointsSpent; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRedemptionCode() { return redemptionCode; }
    public void setRedemptionCode(String redemptionCode) { this.redemptionCode = redemptionCode; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public LocalDateTime getRedeemedAt() { return redeemedAt; }
    public void setRedeemedAt(LocalDateTime redeemedAt) { this.redeemedAt = redeemedAt; }

    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }

    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }

    private String generateRedemptionCode() {
        return "RWD" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }
}