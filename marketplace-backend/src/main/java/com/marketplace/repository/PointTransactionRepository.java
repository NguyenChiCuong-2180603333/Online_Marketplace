package com.marketplace.repository;

import com.marketplace.model.PointTransaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PointTransactionRepository extends MongoRepository<PointTransaction, String> {

    List<PointTransaction> findByUserIdOrderByCreatedAtDesc(String userId);

    List<PointTransaction> findByUserIdAndType(String userId, String type);

    List<PointTransaction> findByUserIdAndReason(String userId, String reason);

    List<PointTransaction> findByUserIdAndReasonAndRelatedId(String userId, String reason, String relatedId);

    // Find transactions by date range
    @Query("{'userId': ?0, 'createdAt': {'$gte': ?1, '$lte': ?2}}")
    List<PointTransaction> findByUserIdAndDateRange(String userId, LocalDateTime start, LocalDateTime end);

    // Find transactions of specific type after date
    List<PointTransaction> findByUserIdAndTypeAndCreatedAtAfter(String userId, String type, LocalDateTime after);

    // Find expired transactions
    List<PointTransaction> findByTypeAndExpiresAtBefore(String type, LocalDateTime date);

    // Statistics queries
    @Query(value = "{'userId': ?0, 'type': 'EARN'}", fields = "{'points': 1}")
    List<PointTransaction> findEarnedPointsByUser(String userId);

    @Query(value = "{'userId': ?0, 'type': 'SPEND'}", fields = "{'points': 1}")
    List<PointTransaction> findSpentPointsByUser(String userId);

    // Monthly statistics
    @Query("{'type': ?0, 'createdAt': {'$gte': ?1, '$lt': ?2}}")
    List<PointTransaction> findByTypeAndMonth(String type, LocalDateTime monthStart, LocalDateTime monthEnd);

    // Top earners in period
    @Query(value = "{'type': 'EARN', 'createdAt': {'$gte': ?0, '$lt': ?1}}",
            sort = "{'points': -1}")
    List<PointTransaction> findTopEarnersInPeriod(LocalDateTime start, LocalDateTime end);

    // Sum points by user and type
    @Query(value = "{'userId': ?0, 'type': ?1}")
    List<PointTransaction> findByUserIdAndTypeForSum(String userId, String type);

    // Transactions by related entity
    List<PointTransaction> findByRelatedId(String relatedId);

    // Recent transactions
    @Query(value = "{}", sort = "{'createdAt': -1}")
    List<PointTransaction> findRecentTransactions();

    // Count transactions by reason
    long countByReason(String reason);

    // Delete old transactions (cleanup)
    void deleteByCreatedAtBefore(LocalDateTime date);
}