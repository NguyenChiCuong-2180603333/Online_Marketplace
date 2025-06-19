package com.marketplace.repository;

import com.marketplace.model.UserInteraction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserInteractionRepository extends MongoRepository<UserInteraction, String> {

    // Lấy interactions của user
    List<UserInteraction> findByUserIdOrderByCreatedAtDesc(String userId);

    // Lấy interactions theo product
    List<UserInteraction> findByProductIdOrderByCreatedAtDesc(String productId);

    // Lấy interactions theo type
    List<UserInteraction> findByUserIdAndInteractionTypeOrderByCreatedAtDesc(String userId, String interactionType);

    // Lấy interactions trong khoảng thời gian
    List<UserInteraction> findByUserIdAndCreatedAtBetween(String userId, LocalDateTime start, LocalDateTime end);

    // Lấy top products được interact nhiều nhất
    @Query(value = "{'interactionType': ?0}",
            fields = "{'productId': 1}",
            sort = "{'createdAt': -1}")
    List<UserInteraction> findTopInteractedProducts(String interactionType);

    // Đếm interactions theo product
    long countByProductIdAndInteractionType(String productId, String interactionType);

    // Đếm interactions của user
    long countByUserIdAndInteractionType(String userId, String interactionType);

    // Lấy users tương tác với product
    @Query(value = "{'productId': ?0}", fields = "{'userId': 1}")
    List<UserInteraction> findUsersByProduct(String productId);

    // Lấy products của user trong session
    List<UserInteraction> findByUserIdAndSessionId(String userId, String sessionId);

    // Tìm similar users (users có interact với products tương tự)
    @Query("{'productId': {'$in': ?0}, 'userId': {'$ne': ?1}}")
    List<UserInteraction> findSimilarUsersByProducts(List<String> productIds, String excludeUserId);

    // Lấy trending products (nhiều interactions gần đây)
    @Query(value = "{'createdAt': {'$gte': ?0}}",
            sort = "{'createdAt': -1}")
    List<UserInteraction> findTrendingInteractions(LocalDateTime since);
}