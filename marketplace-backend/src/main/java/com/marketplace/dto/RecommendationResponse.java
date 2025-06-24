package com.marketplace.dto;

import com.marketplace.model.Product;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class RecommendationResponse {
    
    private List<Product> recommendations;
    private Integer count;
    private String userId;
    private String algorithm; 
    private String type; 
    private Long timestamp;
    private Map<String, Object> metadata;
    private String baseProductId;
    private String category; 
    private Double confidence; 
    
    public RecommendationResponse() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public RecommendationResponse(List<Product> recommendations, String userId, String algorithm) {
        this();
        this.recommendations = recommendations;
        this.count = recommendations != null ? recommendations.size() : 0;
        this.userId = userId;
        this.algorithm = algorithm;
    }
    
    public static RecommendationResponse createPersonalRecommendations(
            List<Product> products, String userId, String algorithm) {
        RecommendationResponse response = new RecommendationResponse(products, userId, algorithm);
        response.setType("personal");
        return response;
    }
    
    public static RecommendationResponse createSimilarProducts(
            List<Product> products, String baseProductId, String algorithm) {
        RecommendationResponse response = new RecommendationResponse(products, null, algorithm);
        response.setType("similar");
        response.setBaseProductId(baseProductId);
        return response;
    }
    
    public static RecommendationResponse createTrendingProducts(
            List<Product> products, String algorithm) {
        RecommendationResponse response = new RecommendationResponse(products, null, algorithm);
        response.setType("trending");
        return response;
    }
    
    public static RecommendationResponse createCategoryRecommendations(
            List<Product> products, String category, String algorithm) {
        RecommendationResponse response = new RecommendationResponse(products, null, algorithm);
        response.setType("category");
        response.setCategory(category);
        return response;
    }
    
    public List<Product> getRecommendations() {
        return recommendations;
    }
    
    public void setRecommendations(List<Product> recommendations) {
        this.recommendations = recommendations;
        this.count = recommendations != null ? recommendations.size() : 0;
    }
    
    public Integer getCount() {
        return count;
    }
    
    public void setCount(Integer count) {
        this.count = count;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getAlgorithm() {
        return algorithm;
    }
    
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public String getBaseProductId() {
        return baseProductId;
    }
    
    public void setBaseProductId(String baseProductId) {
        this.baseProductId = baseProductId;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public Double getConfidence() {
        return confidence;
    }
    
    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
    
    @Override
    public String toString() {
        return "RecommendationResponse{" +
                "count=" + count +
                ", userId='" + userId + '\'' +
                ", algorithm='" + algorithm + '\'' +
                ", type='" + type + '\'' +
                ", timestamp=" + timestamp +
                ", baseProductId='" + baseProductId + '\'' +
                ", category='" + category + '\'' +
                ", confidence=" + confidence +
                '}';
    }
}