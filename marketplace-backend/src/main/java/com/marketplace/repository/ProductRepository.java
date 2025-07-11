package com.marketplace.repository;

import com.marketplace.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findByActiveTrue();
    List<Product> findByCategoryAndActiveTrue(String category);
    List<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name);
    List<Product> findBySellerId(String sellerId);
    List<Product> findBySellerIdAndActiveTrue(String sellerId);

    List<Product> findByPriceBetweenAndActiveTrue(Double minPrice, Double maxPrice);

    List<Product> findByAverageRatingGreaterThanEqualAndActiveTrue(Double minRating);

    @Query("{'$or': [{'name': {'$regex': ?0, '$options': 'i'}}, {'description': {'$regex': ?0, '$options': 'i'}}, {'tags': {'$regex': ?0, '$options': 'i'}}], 'active': true}")
    List<Product> findBySearchTermAndActiveTrue(String searchTerm);

    List<Product> findTop10ByActiveTrueOrderByReviewCountDesc();

    List<Product> findTop10ByActiveTrueOrderByCreatedAtDesc();

    long countByCategoryAndActiveTrue(String category);

    long countByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
}
