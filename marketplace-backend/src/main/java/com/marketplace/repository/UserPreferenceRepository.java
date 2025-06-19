package com.marketplace.repository;

import com.marketplace.model.UserPreference;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPreferenceRepository extends MongoRepository<UserPreference, String> {
    Optional<UserPreference> findByUserId(String userId);
    void deleteByUserId(String userId);
}