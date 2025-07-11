package com.marketplace.repository;

import com.marketplace.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByUserId(String userId);
    List<Notification> findByUserIdAndIsReadFalse(String userId);
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);
    List<Notification> findByIsGlobalTrue();
    List<Notification> findByType(String type);

    @Query("{'$or': [{'userId': ?0}, {'isGlobal': true}]}")
    List<Notification> findByUserIdOrGlobal(String userId);

    long countByUserIdAndIsReadFalse(String userId);

    @Query("{'$or': [{'userId': ?0, 'isRead': false}, {'isGlobal': true, 'isRead': false}]}")
    long countUnreadByUserIdOrGlobal(String userId);
}