package com.marketplace.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Document(collection = "points_rewards")
public class PointsReward {
    @Id
    private String id;

    @NotBlank(message = "Reward name is required")
    @Size(max = 100, message = "Reward name must not exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Min(value = 1, message = "Points cost must be at least 1")
    private int pointsCost;

    @Pattern(regexp = "DISCOUNT|FREE_SHIPPING|GIFT_CARD|PRODUCT|CASHBACK",
            message = "Reward type must be DISCOUNT, FREE_SHIPPING, GIFT_CARD, PRODUCT, or CASHBACK")
    private String rewardType;

    private Double discountPercentage; // For DISCOUNT type
    private Double discountAmount; // For DISCOUNT/CASHBACK type
    private String productId; // For PRODUCT type
    private String giftCardCode; // For GIFT_CARD type

    private String imageUrl;
    private int stockQuantity = -1; // -1 means unlimited
    private int redeemedCount = 0;

    @Pattern(regexp = "BRONZE|SILVER|GOLD|PLATINUM|DIAMOND|ALL",
            message = "Required tier must be BRONZE, SILVER, GOLD, PLATINUM, DIAMOND, or ALL")
    private String requiredTier = "ALL";

    private boolean active = true;
    private LocalDateTime validFrom = LocalDateTime.now();
    private LocalDateTime validUntil;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Constructors
    public PointsReward() {}

    public PointsReward(String name, int pointsCost, String rewardType) {
        this.name = name;
        this.pointsCost = pointsCost;
        this.rewardType = rewardType;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getPointsCost() { return pointsCost; }
    public void setPointsCost(int pointsCost) { this.pointsCost = pointsCost; }

    public String getRewardType() { return rewardType; }
    public void setRewardType(String rewardType) { this.rewardType = rewardType; }

    public Double getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(Double discountPercentage) { this.discountPercentage = discountPercentage; }

    public Double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(Double discountAmount) { this.discountAmount = discountAmount; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getGiftCardCode() { return giftCardCode; }
    public void setGiftCardCode(String giftCardCode) { this.giftCardCode = giftCardCode; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    public int getRedeemedCount() { return redeemedCount; }
    public void setRedeemedCount(int redeemedCount) { this.redeemedCount = redeemedCount; }

    public String getRequiredTier() { return requiredTier; }
    public void setRequiredTier(String requiredTier) { this.requiredTier = requiredTier; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDateTime validFrom) { this.validFrom = validFrom; }

    public LocalDateTime getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDateTime validUntil) { this.validUntil = validUntil; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
