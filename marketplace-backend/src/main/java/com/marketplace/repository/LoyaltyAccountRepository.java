package com.marketplace.repository;

import com.marketplace.model.LoyaltyAccount;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoyaltyAccountRepository extends MongoRepository<LoyaltyAccount, String> {

    Optional<LoyaltyAccount> findByUserId(String userId);

    List<LoyaltyAccount> findByTier(String tier);

    List<LoyaltyAccount> findByTierOrderByTotalPointsDesc(String tier);

    // Top users by points
    @Query(value = "{}", sort = "{'totalPoints': -1}")
    List<LoyaltyAccount> findTopUsersByPoints();

    // Users with points above threshold
    @Query("{'totalPoints': {'$gte': ?0}}")
    List<LoyaltyAccount> findUsersWithPointsAbove(int threshold);

    // Users by tier and minimum points
    @Query("{'tier': ?0, 'totalPoints': {'$gte': ?1}}")
    List<LoyaltyAccount> findByTierAndMinPoints(String tier, int minPoints);

    // Statistics
    @Query(value = "{}", count = true)
    long countAllAccounts();

    long countByTier(String tier);

    // Average points by tier
    @Query(value = "{'tier': ?0}", fields = "{'totalPoints': 1}")
    List<LoyaltyAccount> findPointsByTier(String tier);
}