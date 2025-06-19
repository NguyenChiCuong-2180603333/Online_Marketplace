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

    @Query(value = "{}", sort = "{'totalEarnedPoints': -1}")
    List<LoyaltyAccount> findTopAccountsByPoints();

    @Query(value = "{'pointsBalance': {'$gte': ?0}}")
    List<LoyaltyAccount> findAccountsWithMinPoints(int minPoints);

    long countByTier(String tier);
}
