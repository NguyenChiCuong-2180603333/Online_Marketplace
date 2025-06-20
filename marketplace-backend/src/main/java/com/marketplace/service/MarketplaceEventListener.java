package com.marketplace.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Event Listener để tự động gửi email và tặng điểm khi có sự kiện
 */
@Component
public class MarketplaceEventListener {

    @Autowired
    private EmailService emailService;

    @Autowired
    private LoyaltyService loyaltyService;

    /**
     * Xử lý khi user đăng ký thành công
     */
    public void handleUserRegistration(String userId, String userEmail, String userName) {
        try {
            // Tạo loyalty account và tặng điểm chào mừng
            loyaltyService.createLoyaltyAccount(userId);

            // Gửi email chào mừng
            emailService.sendWelcomeEmail(userEmail, userName);
        } catch (Exception e) {
            // Log error but don't fail registration
            System.err.println("Failed to process user registration events: " + e.getMessage());
        }
    }

    /**
     * Xử lý khi đơn hàng được tạo
     */
    public void handleOrderCreated(String orderId, String userEmail, String userName, Double totalAmount) {
        try {
            // Gửi email xác nhận đơn hàng
            emailService.sendOrderConfirmationEmail(userEmail, userName, orderId, totalAmount);
        } catch (Exception e) {
            System.err.println("Failed to send order confirmation: " + e.getMessage());
        }
    }

    /**
     * Xử lý khi đơn hàng hoàn thành
     */
    public void handleOrderCompleted(String orderId, String userId) {
        try {
            // Tặng điểm loyalty cho đơn hàng hoàn thành
            // Implementation sẽ được gọi từ OrderService
        } catch (Exception e) {
            System.err.println("Failed to process order completion: " + e.getMessage());
        }
    }

    /**
     * Xử lý khi có review mới
     */
    public void handleNewReview(String userId, String productId, String sellerEmail, String sellerName, String productName, int rating) {
        try {
            // Tặng điểm cho việc review
            loyaltyService.awardPointsForReview(userId, productId);

            // Gửi email thông báo cho seller
            if (sellerEmail != null) {
                emailService.sendNewReviewNotificationEmail(sellerEmail, sellerName, productName, rating);
            }
        } catch (Exception e) {
            System.err.println("Failed to process review events: " + e.getMessage());
        }
    }

    /**
     * Xử lý khi trạng thái đơn hàng thay đổi
     */
    public void handleOrderStatusUpdate(String orderId, String userEmail, String userName, String newStatus) {
        try {
            // Gửi email cập nhật trạng thái
            emailService.sendOrderStatusUpdateEmail(userEmail, userName, orderId, newStatus);
        } catch (Exception e) {
            System.err.println("Failed to send order status update: " + e.getMessage());
        }
    }
}