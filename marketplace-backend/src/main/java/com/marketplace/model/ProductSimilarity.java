package com.marketplace.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Document(collection = "product_similarities")
public class ProductSimilarity {
    @Id
    private String id;

    @NotBlank(message = "Product ID is required")
    private String productId;

    @NotBlank(message = "Similar product ID is required")
    private String similarProductId;

    @DecimalMin(value = "0.0", message = "Similarity score cannot be negative")
    @DecimalMax(value = "1.0", message = "Similarity score cannot exceed 1.0")
    private Double similarityScore;

    @Pattern(regexp = "CATEGORY|TAGS|PRICE|RATING|COLLABORATIVE|CONTENT",
            message = "Similarity type must be CATEGORY, TAGS, PRICE, RATING, COLLABORATIVE, or CONTENT")
    private String similarityType; 

    private LocalDateTime calculatedAt = LocalDateTime.now();

    public ProductSimilarity() {}

    public ProductSimilarity(String productId, String similarProductId, Double similarityScore, String similarityType) {
        this.productId = productId;
        this.similarProductId = similarProductId;
        this.similarityScore = similarityScore;
        this.similarityType = similarityType;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getSimilarProductId() { return similarProductId; }
    public void setSimilarProductId(String similarProductId) { this.similarProductId = similarProductId; }

    public Double getSimilarityScore() { return similarityScore; }
    public void setSimilarityScore(Double similarityScore) { this.similarityScore = similarityScore; }

    public String getSimilarityType() { return similarityType; }
    public void setSimilarityType(String similarityType) { this.similarityType = similarityType; }

    public LocalDateTime getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; }
}