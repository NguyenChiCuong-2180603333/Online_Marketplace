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

    @Query("{'userId': ?0, 'createdAt': {'$gte': ?1, '$lte': ?2}}")
    List<PointTransaction> findByUserIdAndDateRange(String userId, LocalDateTime start, LocalDateTime end);

    List<PointTransaction> findByUserIdAndTypeAndCreatedAtAfter(String userId, String type, LocalDateTime after);

    List<PointTransaction> findByTypeAndExpiresAtBefore(String type, LocalDateTime date);

    @Query(value = "{'userId': ?0, 'type': 'EARN'}", fields = "{'points': 1}")
    List<PointTransaction> findEarnedPointsByUser(String userId);

    @Query(value = "{'userId': ?0, 'type': 'SPEND'}", fields = "{'points': 1}")
    List<PointTransaction> findSpentPointsByUser(String userId);

    @Query("{'type': ?0, 'createdAt': {'$gte': ?1, '$lt': ?2}}")
    List<PointTransaction> findByTypeAndMonth(String type, LocalDateTime monthStart, LocalDateTime monthEnd);

    @Query(value = "{'type': 'EARN', 'createdAt': {'$gte': ?0, '$lt': ?1}}",
            sort = "{'points': -1}")
    List<PointTransaction> findTopEarnersInPeriod(LocalDateTime start, LocalDateTime end);

    @Query(value = "{'userId': ?0, 'type': ?1}")
    List<PointTransaction> findByUserIdAndTypeForSum(String userId, String type);

    List<PointTransaction> findByRelatedId(String relatedId);

    @Query(value = "{}", sort = "{'createdAt': -1}")
    List<PointTransaction> findRecentTransactions();

    long countByReason(String reason);

    void deleteByCreatedAtBefore(LocalDateTime date);
}