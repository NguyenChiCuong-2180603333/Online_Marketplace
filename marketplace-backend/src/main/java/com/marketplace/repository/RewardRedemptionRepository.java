package com.marketplace.repository;

import com.marketplace.model.RewardRedemption;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RewardRedemptionRepository extends MongoRepository<RewardRedemption, String> {

    List<RewardRedemption> findByUserIdOrderByRedeemedAtDesc(String userId);
    List<RewardRedemption> findByRewardIdOrderByRedeemedAtDesc(String rewardId);
    List<RewardRedemption> findByStatus(String status);
    Optional<RewardRedemption> findByRedemptionCode(String redemptionCode);

    @Query("{'status': ?0, 'redeemedAt': {'$gte': ?1, '$lte': ?2}}")
    List<RewardRedemption> findByStatusAndDateRange(String status, LocalDateTime start, LocalDateTime end);

    @Query("{'expiryDate': {'$lte': ?0}, 'status': {'$in': ['PENDING', 'APPROVED']}}")
    List<RewardRedemption> findExpiredRedemptions(LocalDateTime date);

    long countByRewardId(String rewardId);
    long countByUserIdAndStatus(String userId, String status);

    @Query("{'userId': ?0, 'rewardId': ?1}")
    List<RewardRedemption> findByUserAndReward(String userId, String rewardId);
}