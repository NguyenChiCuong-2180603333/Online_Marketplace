package com.marketplace.repository;

import com.marketplace.model.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {

    // Tìm chat room giữa buyer và seller về sản phẩm cụ thể
    Optional<ChatRoom> findBySellerIdAndBuyerIdAndProductId(String sellerId, String buyerId, String productId);

    // Tìm tất cả chat rooms của user
    @Query("{'participants': ?0, 'active': true}")
    List<ChatRoom> findByParticipantsContainingAndActiveTrue(String userId);

    // Tìm chat rooms của seller
    List<ChatRoom> findBySellerIdAndActiveTrue(String sellerId);

    // Tìm chat rooms của buyer
    List<ChatRoom> findByBuyerIdAndActiveTrue(String buyerId);

    // Tìm chat rooms theo product
    List<ChatRoom> findByProductIdAndActiveTrue(String productId);

    // Tìm chat rooms theo type
    List<ChatRoom> findByRoomTypeAndActiveTrue(String roomType);

    // Đếm số chat rooms active của user
    @Query(value = "{'participants': ?0, 'active': true}", count = true)
    long countActiveRoomsByUser(String userId);
}