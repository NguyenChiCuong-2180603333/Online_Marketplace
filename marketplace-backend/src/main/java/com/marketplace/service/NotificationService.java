package com.marketplace.service;

import com.marketplace.dto.NotificationRequest;
import com.marketplace.model.Notification;
import com.marketplace.repository.NotificationRepository;
import com.marketplace.exception.ResourceNotFoundException;
import com.marketplace.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    // Lấy notifications của user (bao gồm global)
    public List<Notification> getUserNotifications(String userId) {
        return notificationRepository.findByUserIdOrGlobal(userId);
    }

    // Lấy notifications chưa đọc
    public List<Notification> getUnreadNotifications(String userId) {
        return notificationRepository.findByUserIdAndIsReadFalse(userId);
    }

    // Đếm notifications chưa đọc
    public long getUnreadCount(String userId) {
        return notificationRepository.countUnreadByUserIdOrGlobal(userId);
    }

    // Tạo notification cho user cụ thể
    public Notification createNotification(NotificationRequest request) {
        Notification notification = new Notification();
        notification.setUserId(request.getUserId());
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setType(request.getType());
        notification.setRelatedId(request.getRelatedId());
        notification.setGlobal(false);
        notification.setCreatedAt(LocalDateTime.now());

        return notificationRepository.save(notification);
    }

    // Tạo notification toàn hệ thống
    public Notification createBroadcastNotification(NotificationRequest request) {
        Notification notification = new Notification();
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setType(request.getType());
        notification.setRelatedId(request.getRelatedId());
        notification.setGlobal(true);
        notification.setCreatedAt(LocalDateTime.now());

        return notificationRepository.save(notification);
    }

    // Đánh dấu đã đọc
    public Notification markAsRead(String notificationId, String userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        // Kiểm tra quyền truy cập
        if (!notification.isGlobal() && !notification.getUserId().equals(userId)) {
            throw new BadRequestException("Bạn không có quyền truy cập thông báo này");
        }

        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());

        return notificationRepository.save(notification);
    }

    // Đánh dấu tất cả đã đọc
    public void markAllAsRead(String userId) {
        List<Notification> unreadNotifications = getUnreadNotifications(userId);

        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
        }

        notificationRepository.saveAll(unreadNotifications);
    }

    // Xóa notification
    public void deleteNotification(String notificationId, String userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        // Chỉ cho phép xóa notification của chính mình (không bao gồm global)
        if (notification.isGlobal() || !notification.getUserId().equals(userId)) {
            throw new BadRequestException("Bạn không thể xóa thông báo này");
        }

        notificationRepository.delete(notification);
    }

    // Admin xóa notification
    public void adminDeleteNotification(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        notificationRepository.delete(notification);
    }

    // Lấy tất cả notifications (admin)
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    // Tạo notification cho đơn hàng mới
    public void createOrderNotification(String userId, String orderId, String orderStatus) {
        String title = "Cập nhật đơn hàng";
        String message = "Đơn hàng #" + orderId + " đã được " + getStatusMessage(orderStatus);

        NotificationRequest request = new NotificationRequest();
        request.setUserId(userId);
        request.setTitle(title);
        request.setMessage(message);
        request.setType("ORDER_UPDATE");
        request.setRelatedId(orderId);

        createNotification(request);
    }

    // Tạo notification cho sản phẩm mới
    public void createProductNotification(String productId, String productName, String sellerId) {
        String title = "Sản phẩm mới";
        String message = "Sản phẩm '" + productName + "' đã được thêm vào cửa hàng";

        NotificationRequest request = new NotificationRequest();
        request.setTitle(title);
        request.setMessage(message);
        request.setType("NEW_PRODUCT");
        request.setRelatedId(productId);

        createBroadcastNotification(request);
    }

    // Tạo notification cho khuyến mãi
    public void createPromotionNotification(String title, String message) {
        NotificationRequest request = new NotificationRequest();
        request.setTitle(title);
        request.setMessage(message);
        request.setType("PROMOTION");

        createBroadcastNotification(request);
    }

    private String getStatusMessage(String status) {
        return switch (status) {
            case "PENDING" -> "đặt thành công";
            case "PROCESSING" -> "xác nhận và đang xử lý";
            case "SHIPPED" -> "giao cho đơn vị vận chuyển";
            case "DELIVERED" -> "giao thành công";
            case "CANCELLED" -> "hủy";
            default -> "cập nhật trạng thái";
        };
    }
}