package com.marketplace.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

@Document(collection = "user_preferences")
public class UserPreference {
    @Id
    private String id;

    @Indexed(unique = true)
    @NotBlank(message = "User ID is required")
    private String userId;

    // Danh mục yêu thích (category -> score)
    private Map<String, Double> categoryPreferences = new HashMap<>();

    // Thương hiệu yêu thích (brand -> score)
    private Map<String, Double> brandPreferences = new HashMap<>();

    // Khoảng giá ưa thích
    private Double minPreferredPrice = 0.0;
    private Double maxPreferredPrice = Double.MAX_VALUE;

    // Tags yêu thích (tag -> score)
    private Map<String, Double> tagPreferences = new HashMap<>();

    // Sellers yêu thích (sellerId -> score)
    private Map<String, Double> sellerPreferences = new HashMap<>();

    // Thời gian mua sắm ưa thích (hour -> score)
    private Map<Integer, Double> timePreferences = new HashMap<>();

    // Behavioral scores
    private double browseBehaviorScore = 0.0; // Điểm dựa trên lịch sử duyệt
    private double purchaseBehaviorScore = 0.0; // Điểm dựa trên lịch sử mua
    private double reviewBehaviorScore = 0.0; // Điểm dựa trên việc đánh giá

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Constructors
    public UserPreference() {}

    public UserPreference(String userId) {
        this.userId = userId;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Map<String, Double> getCategoryPreferences() { return categoryPreferences; }
    public void setCategoryPreferences(Map<String, Double> categoryPreferences) { this.categoryPreferences = categoryPreferences; }

    public Map<String, Double> getBrandPreferences() { return brandPreferences; }
    public void setBrandPreferences(Map<String, Double> brandPreferences) { this.brandPreferences = brandPreferences; }

    public Double getMinPreferredPrice() { return minPreferredPrice; }
    public void setMinPreferredPrice(Double minPreferredPrice) { this.minPreferredPrice = minPreferredPrice; }

    public Double getMaxPreferredPrice() { return maxPreferredPrice; }
    public void setMaxPreferredPrice(Double maxPreferredPrice) { this.maxPreferredPrice = maxPreferredPrice; }

    public Map<String, Double> getTagPreferences() { return tagPreferences; }
    public void setTagPreferences(Map<String, Double> tagPreferences) { this.tagPreferences = tagPreferences; }

    public Map<String, Double> getSellerPreferences() { return sellerPreferences; }
    public void setSellerPreferences(Map<String, Double> sellerPreferences) { this.sellerPreferences = sellerPreferences; }

    public Map<Integer, Double> getTimePreferences() { return timePreferences; }
    public void setTimePreferences(Map<Integer, Double> timePreferences) { this.timePreferences = timePreferences; }

    public double getBrowseBehaviorScore() { return browseBehaviorScore; }
    public void setBrowseBehaviorScore(double browseBehaviorScore) { this.browseBehaviorScore = browseBehaviorScore; }

    public double getPurchaseBehaviorScore() { return purchaseBehaviorScore; }
    public void setPurchaseBehaviorScore(double purchaseBehaviorScore) { this.purchaseBehaviorScore = purchaseBehaviorScore; }

    public double getReviewBehaviorScore() { return reviewBehaviorScore; }
    public void setReviewBehaviorScore(double reviewBehaviorScore) { this.reviewBehaviorScore = reviewBehaviorScore; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
