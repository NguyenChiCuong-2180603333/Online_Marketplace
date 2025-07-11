package com.marketplace.dto;

import jakarta.validation.constraints.*;

public class ReviewRequest {
    @NotBlank(message = "Product ID is required")
    private String productId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must not exceed 5")
    private Integer rating;

    @Size(max = 500, message = "Comment must not exceed 500 characters")
    private String comment;

    private String orderId;

   
    public ReviewRequest() {}

    public ReviewRequest(String productId, Integer rating, String comment) {
        this.productId = productId;
        this.rating = rating;
        this.comment = comment;
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
}