package com.marketplace.repository;

import com.marketplace.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByProductId(String productId);
    List<Review> findByProductIdOrderByCreatedAtDesc(String productId);
    List<Review> findByUserId(String userId);
    Optional<Review> findByProductIdAndUserId(String productId, String userId);
    List<Review> findByRating(int rating);
    List<Review> findByVerifiedTrue();
    long countByProductId(String productId);
    long countByProductIdAndRating(String productId, int rating);
}