package com.marketplace.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Document(collection = "user_interactions")
public class UserInteraction {
    @Id
    private String id;

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Product ID is required")
    private String productId;

    @Pattern(regexp = "VIEW|CLICK|ADD_TO_CART|PURCHASE|REVIEW|LIKE|SHARE|SEARCH",
            message = "Interaction type must be VIEW, CLICK, ADD_TO_CART, PURCHASE, REVIEW, LIKE, SHARE, or SEARCH")
    private String interactionType;

    private Double duration;
    private String sessionId;
    private String deviceType; 
    private String referrer; 

    private String searchQuery; 
    private String categoryBrowsed; 
    private Double rating; 

    private LocalDateTime createdAt = LocalDateTime.now();
    public UserInteraction() {}

    public UserInteraction(String userId, String productId, String interactionType) {
        this.userId = userId;
        this.productId = productId;
        this.interactionType = interactionType;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getInteractionType() { return interactionType; }
    public void setInteractionType(String interactionType) { this.interactionType = interactionType; }

    public Double getDuration() { return duration; }
    public void setDuration(Double duration) { this.duration = duration; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public String getReferrer() { return referrer; }
    public void setReferrer(String referrer) { this.referrer = referrer; }

    public String getSearchQuery() { return searchQuery; }
    public void setSearchQuery(String searchQuery) { this.searchQuery = searchQuery; }

    public String getCategoryBrowsed() { return categoryBrowsed; }
    public void setCategoryBrowsed(String categoryBrowsed) { this.categoryBrowsed = categoryBrowsed; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}