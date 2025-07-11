package com.marketplace.controller;

import com.marketplace.dto.NotificationRequest;
import com.marketplace.model.Notification;
import com.marketplace.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // Lấy notifications của user hiện tại
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<Notification>> getUserNotifications(
            @RequestParam(defaultValue = "false") boolean unreadOnly) {
        String userId = getCurrentUserId();
        List<Notification> notifications;

        if (unreadOnly) {
            notifications = notificationService.getUnreadNotifications(userId);
        } else {
            notifications = notificationService.getUserNotifications(userId);
        }

        return ResponseEntity.ok(notifications);
    }

    // Đếm notifications chưa đọc
    @GetMapping("/unread-count")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUnreadCount() {
        String userId = getCurrentUserId();
        long unreadCount = notificationService.getUnreadCount(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("unreadCount", unreadCount);
        return ResponseEntity.ok(response);
    }

    // Đánh dấu notification đã đọc
    @PutMapping("/{notificationId}/read")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Notification> markAsRead(@PathVariable String notificationId) {
        String userId = getCurrentUserId();
        Notification notification = notificationService.markAsRead(notificationId, userId);
        return ResponseEntity.ok(notification);
    }

    // Đánh dấu tất cả notifications đã đọc
    @PutMapping("/mark-all-read")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> markAllAsRead() {
        String userId = getCurrentUserId();
        notificationService.markAllAsRead(userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Đã đánh dấu tất cả thông báo là đã đọc");
        return ResponseEntity.ok(response);
    }

    // Xóa notification
    @DeleteMapping("/{notificationId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteNotification(@PathVariable String notificationId) {
        String userId = getCurrentUserId();
        notificationService.deleteNotification(notificationId, userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Thông báo đã được xóa");
        return ResponseEntity.ok(response);
    }

    // Admin: Tạo thông báo toàn hệ thống
    @PostMapping("/broadcast")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Notification> createBroadcastNotification(
            @Valid @RequestBody NotificationRequest notificationRequest) {
        Notification notification = notificationService.createBroadcastNotification(notificationRequest);
        return ResponseEntity.ok(notification);
    }

    // Admin: Tạo thông báo cho user cụ thể
    @PostMapping("/send")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Notification> sendNotificationToUser(
            @Valid @RequestBody NotificationRequest notificationRequest) {
        Notification notification = notificationService.createNotification(notificationRequest);
        return ResponseEntity.ok(notification);
    }

    // Admin: Lấy tất cả notifications
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Notification>> getAllNotifications() {
        List<Notification> notifications = notificationService.getAllNotifications();
        return ResponseEntity.ok(notifications);
    }

    // Admin: Xóa notification (admin)
    @DeleteMapping("/admin/{notificationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> adminDeleteNotification(@PathVariable String notificationId) {
        notificationService.adminDeleteNotification(notificationId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Thông báo đã được xóa bởi admin");
        return ResponseEntity.ok(response);
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getPrincipal().equals("anonymousUser")) {

            String token = getJwtFromCurrentRequest();
            if (token != null) {
                try {
                    org.springframework.context.ApplicationContext context =
                            org.springframework.web.context.support.WebApplicationContextUtils
                                    .getWebApplicationContext(((org.springframework.web.context.request.ServletRequestAttributes)
                                            org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes())
                                            .getRequest().getServletContext());

                    if (context != null) {
                        com.marketplace.security.JwtTokenProvider jwtProvider = context.getBean(com.marketplace.security.JwtTokenProvider.class);
                        return jwtProvider.getUserIdFromToken(token);
                    }
                } catch (Exception e) {
                }
            }
        }
        return "mockUserId123";
    }

    private String getJwtFromCurrentRequest() {
        try {
            org.springframework.web.context.request.ServletRequestAttributes attr =
                    (org.springframework.web.context.request.ServletRequestAttributes)
                            org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes();
            jakarta.servlet.http.HttpServletRequest request = attr.getRequest();
            String bearerToken = request.getHeader("Authorization");
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                return bearerToken.substring(7);
            }
        } catch (Exception e) {
        }
        return null;
    }
    @PutMapping("/admin/{notificationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Notification> updateNotification(
            @PathVariable String notificationId,
            @Valid @RequestBody NotificationRequest notificationRequest) {
        try {
            Notification updated = notificationService.updateNotification(notificationId, notificationRequest);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            e.printStackTrace(); 
            throw e;
        }
    }
}