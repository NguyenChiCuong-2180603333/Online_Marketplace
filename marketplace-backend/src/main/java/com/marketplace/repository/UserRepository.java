package com.marketplace.repository;

import com.marketplace.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(String role);
    List<User> findByEnabledTrue();
    List<User> findByEnabledFalse();
    long countByRole(String role);
    
    // VIP users
    List<User> findByIsVipTrue();
    long countByIsVipTrue();
    
    // Verified users
    List<User> findByIsVerifiedTrue();
    long countByIsVerifiedTrue();
    
    // Users created in date range
    List<User> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Users by multiple criteria
    List<User> findByRoleAndIsVipTrue(String role);
    List<User> findByRoleAndIsVerifiedTrue(String role);
    List<User> findByEnabledAndIsVipTrue(boolean enabled);
    
    // Search by name or email
    @Query("{'$or': [{'firstName': {'$regex': ?0, '$options': 'i'}}, {'lastName': {'$regex': ?0, '$options': 'i'}}, {'email': {'$regex': ?0, '$options': 'i'}}]}")
    List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String searchTerm);
    
    // Count by enabled status
    long countByEnabledTrue();
    long countByEnabledFalse();
}
