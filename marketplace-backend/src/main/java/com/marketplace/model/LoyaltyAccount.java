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

    @Min(value = 0, message = "Points cannot be negative")
    private int totalPoints = 0;

    @Min(value = 0, message = "Available points cannot be negative")
    private int availablePoints = 0;

    @Min(value = 0, message = "Used points cannot be negative")
    private int usedPoints = 0;

    @Pattern(regexp = "BRONZE|SILVER|GOLD|PLATINUM|DIAMOND",
            message = "Invalid tier")
    private String tier = "BRONZE";

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    private LocalDateTime tierUpdatedAt = LocalDateTime.now();

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

    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }

    public int getAvailablePoints() { return availablePoints; }
    public void setAvailablePoints(int availablePoints) { this.availablePoints = availablePoints; }

    public int getUsedPoints() { return usedPoints; }
    public void setUsedPoints(int usedPoints) { this.usedPoints = usedPoints; }

    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getTierUpdatedAt() { return tierUpdatedAt; }
    public void setTierUpdatedAt(LocalDateTime tierUpdatedAt) { this.tierUpdatedAt = tierUpdatedAt; }
}