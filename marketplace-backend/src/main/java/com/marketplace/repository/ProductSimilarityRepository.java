package com.marketplace.repository;

import com.marketplace.model.ProductSimilarity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSimilarityRepository extends MongoRepository<ProductSimilarity, String> {

    // Lấy products tương tự với sản phẩm
    List<ProductSimilarity> findByProductIdOrderBySimilarityScoreDesc(String productId);

    // Lấy products tương tự theo type
    List<ProductSimilarity> findByProductIdAndSimilarityTypeOrderBySimilarityScoreDesc(
            String productId, String similarityType);

    // Lấy top similar products với threshold
    @Query("{'productId': ?0, 'similarityScore': {'$gte': ?1}}")
    List<ProductSimilarity> findSimilarProductsAboveThreshold(String productId, Double threshold);

    // Xóa similarities của product
    void deleteByProductId(String productId);
    void deleteByProductIdOrSimilarProductId(String productId, String similarProductId);

    // Kiểm tra similarity đã tồn tại
    boolean existsByProductIdAndSimilarProductId(String productId, String similarProductId);

    // Lấy mutual similarities (A tương tự B và B tương tự A)
    @Query("{'$or': [" +
            "{'productId': ?0, 'similarProductId': ?1}," +
            "{'productId': ?1, 'similarProductId': ?0}" +
            "]}")
    List<ProductSimilarity> findMutualSimilarities(String productId1, String productId2);
}