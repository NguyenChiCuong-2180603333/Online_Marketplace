package com.marketplace.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Document(collection = "loyalty_accounts")
public class LoyaltyAccount {
    @Id
    private String id;

    @NotBlank(message = "User ID is required")
    private String userId;

    @Min(value = 0, message = "Points balance cannot be negative")
    private int pointsBalance = 0;

    @Min(value = 0, message = "Total earned points cannot be negative")
    private int totalEarnedPoints = 0;

    @Min(value = 0, message = "Total spent points cannot be negative")
    private int totalSpentPoints = 0;

    @Pattern(regexp = "BRONZE|SILVER|GOLD|PLATINUM|DIAMOND",
            message = "Tier must be BRONZE, SILVER, GOLD, PLATINUM, or DIAMOND")
    private String tier = "BRONZE";

    private int tierProgress = 0; // Points needed for next tier
    private LocalDateTime lastEarnedAt;
    private LocalDateTime lastSpentAt;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Constructors
    public LoyaltyAccount() {}

    public LoyaltyAccount(String userId) {
        this.userId = userId;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getPointsBalance() { return pointsBalance; }
    public void setPointsBalance(int pointsBalance) { this.pointsBalance = pointsBalance; }

    public int getTotalEarnedPoints() { return totalEarnedPoints; }
    public void setTotalEarnedPoints(int totalEarnedPoints) { this.totalEarnedPoints = totalEarnedPoints; }

    public int getTotalSpentPoints() { return totalSpentPoints; }
    public void setTotalSpentPoints(int totalSpentPoints) { this.totalSpentPoints = totalSpentPoints; }

    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }

    public int getTierProgress() { return tierProgress; }
    public void setTierProgress(int tierProgress) { this.tierProgress = tierProgress; }

    public LocalDateTime getLastEarnedAt() { return lastEarnedAt; }
    public void setLastEarnedAt(LocalDateTime lastEarnedAt) { this.lastEarnedAt = lastEarnedAt; }

    public LocalDateTime getLastSpentAt() { return lastSpentAt; }
    public void setLastSpentAt(LocalDateTime lastSpentAt) { this.lastSpentAt = lastSpentAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
