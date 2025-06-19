package com.marketplace.repository;

import com.marketplace.model.PointsTransaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PointsTransactionRepository extends MongoRepository<PointsTransaction, String> {

    List<PointsTransaction> findByUserIdOrderByCreatedAtDesc(String userId);
    List<PointsTransaction> findByUserIdAndTransactionTypeOrderByCreatedAtDesc(String userId, String transactionType);
    List<PointsTransaction> findByRelatedOrderId(String orderId);
    List<PointsTransaction> findByRelatedReviewId(String reviewId);

    @Query("{'userId': ?0, 'transactionType': 'EARNED', 'isActive': true, 'expiryDate': {'$lte': ?1}}")
    List<PointsTransaction> findExpiredEarnedPoints(LocalDateTime expiryDate);

    @Query("{'userId': ?0, 'transactionType': 'EARNED', 'isActive': true, 'expiryDate': {'$lte': ?1}}")
    List<PointsTransaction> findExpiringPoints(String userId, LocalDateTime expiryDate);

    @Query("{'createdAt': {'$gte': ?0, '$lte': ?1}}")
    List<PointsTransaction> findTransactionsBetweenDates(LocalDateTime startDate, LocalDateTime endDate);

    @Query("{'transactionType': ?0, 'createdAt': {'$gte': ?1}}")
    List<PointsTransaction> findByTypeAndCreatedAfter(String transactionType, LocalDateTime date);

    long countByUserIdAndTransactionType(String userId, String transactionType);
    long countByTransactionTypeAndCreatedAtBetween(String transactionType, LocalDateTime start, LocalDateTime end);
}
