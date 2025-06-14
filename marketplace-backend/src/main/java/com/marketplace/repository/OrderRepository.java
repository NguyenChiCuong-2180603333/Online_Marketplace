package com.marketplace.repository;

import com.marketplace.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByUserId(String userId);
    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);
    List<Order> findByStatus(String status);
    Optional<Order> findByPaymentId(String paymentId);

    // Đơn hàng theo seller
    @Query("{'items.sellerId': ?0}")
    List<Order> findBySellerIdInItems(String sellerId);

    // Thống kê doanh thu
    @Query("{'createdAt': {'$gte': ?0, '$lte': ?1}, 'paymentStatus': 'COMPLETED'}")
    List<Order> findCompletedOrdersBetweenDates(LocalDateTime startDate, LocalDateTime endDate);

    // Đếm đơn hàng theo trạng thái
    long countByStatus(String status);
    long countByPaymentStatus(String paymentStatus);

    // Tổng doanh thu
    @Query(value = "{'paymentStatus': 'COMPLETED'}", fields = "{'totalAmount': 1}")
    List<Order> findAllCompletedOrders();
}