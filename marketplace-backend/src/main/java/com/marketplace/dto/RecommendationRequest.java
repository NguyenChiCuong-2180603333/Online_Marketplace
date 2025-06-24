package com.marketplace.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.util.List;
import java.util.Map;


public class RecommendationRequest {
    
    @NotNull(message = "User ID không được null")
    private String userId;
    
    @Min(value = 1, message = "Limit phải >= 1")
    @Max(value = 100, message = "Limit phải <= 100")
    private Integer limit = 20;
    
    private String category;
    private String productId; 
    private Double minPrice;
    private Double maxPrice;
    private List<String> tags;
    private String sortBy; 
    private Map<String, Object> filters;
    
    public RecommendationRequest() {}
    
    public RecommendationRequest(String userId, Integer limit) {
        this.userId = userId;
        this.limit = limit;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public Integer getLimit() {
        return limit;
    }
    
    public void setLimit(Integer limit) {
        this.limit = limit;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getProductId() {
        return productId;
    }
    
    public void setProductId(String productId) {
        this.productId = productId;
    }
    
    public Double getMinPrice() {
        return minPrice;
    }
    
    public void setMinPrice(Double minPrice) {
        this.minPrice = minPrice;
    }
    
    public Double getMaxPrice() {
        return maxPrice;
    }
    
    public void setMaxPrice(Double maxPrice) {
        this.maxPrice = maxPrice;
    }
    
    public List<String> getTags() {
        return tags;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    
    public String getSortBy() {
        return sortBy;
    }
    
    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }
    
    public Map<String, Object> getFilters() {
        return filters;
    }
    
    public void setFilters(Map<String, Object> filters) {
        this.filters = filters;
    }
    
    @Override
    public String toString() {
        return "RecommendationRequest{" +
                "userId='" + userId + '\'' +
                ", limit=" + limit +
                ", category='" + category + '\'' +
                ", productId='" + productId + '\'' +
                ", minPrice=" + minPrice +
                ", maxPrice=" + maxPrice +
                ", tags=" + tags +
                ", sortBy='" + sortBy + '\'' +
                ", filters=" + filters +
                '}';
    }
}