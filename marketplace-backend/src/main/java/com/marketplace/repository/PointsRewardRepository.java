package com.marketplace.repository;

import com.marketplace.model.PointsReward;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PointsRewardRepository extends MongoRepository<PointsReward, String> {

    @Query("{'active': true, '$or': [{'validUntil': null}, {'validUntil': {'$gte': ?0}}]}")
    List<PointsReward> findActiveRewards();

    List<PointsReward> findByRewardTypeAndActiveTrue(String rewardType);
    List<PointsReward> findByRequiredTierAndActiveTrue(String requiredTier);

    @Query("{'pointsCost': {'$lte': ?0}, 'active': true}")
    List<PointsReward> findAffordableRewards(int maxPoints);

    @Query("{'active': true, 'stockQuantity': {'$gt': 0}}")
    List<PointsReward> findAvailableRewards();

    @Query(value = "{'active': true}", sort = "{'redeemedCount': -1}")
    List<PointsReward> findPopularRewards();

    @Query(value = "{'active': true}", sort = "{'createdAt': -1}")
    List<PointsReward> findLatestRewards();
}
