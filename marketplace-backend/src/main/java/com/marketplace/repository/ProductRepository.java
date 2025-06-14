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

    // Tìm kiếm theo giá
    List<Product> findByPriceBetweenAndActiveTrue(Double minPrice, Double maxPrice);

    // Tìm kiếm theo rating
    List<Product> findByAverageRatingGreaterThanEqualAndActiveTrue(Double minRating);

    // Tìm kiếm full-text
    @Query("{'$or': [{'name': {'$regex': ?0, '$options': 'i'}}, {'description': {'$regex': ?0, '$options': 'i'}}, {'tags': {'$regex': ?0, '$options': 'i'}}], 'active': true}")
    List<Product> findBySearchTermAndActiveTrue(String searchTerm);

    // Sản phẩm bán chạy
    List<Product> findTop10ByActiveTrueOrderByReviewCountDesc();

    // Sản phẩm mới nhất
    List<Product> findTop10ByActiveTrueOrderByCreatedAtDesc();

    // Đếm số sản phẩm theo category
    long countByCategoryAndActiveTrue(String category);
}
