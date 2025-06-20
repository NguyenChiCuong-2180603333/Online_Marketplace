package com.marketplace.controller;

import com.marketplace.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/email")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class EmailController {

    @Autowired
    private EmailService emailService;

    /**
     * Test endpoint để gửi email đơn giản
     */
    @PostMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> testEmail(@RequestBody Map<String, String> request) {
        String to = request.get("to");
        String subject = request.get("subject");
        String text = request.get("text");

        try {
            emailService.sendSimpleEmail(to, subject, text);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Email sent successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to send email: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Admin: Gửi email thông báo sản phẩm mới
     */
    @PostMapping("/admin/new-product")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> sendNewProductNotification(@RequestBody Map<String, String> request) {
        String userEmail = request.get("userEmail");
        String userName = request.get("userName");
        String productName = request.get("productName");
        String productUrl = request.get("productUrl");

        try {
            emailService.sendNewProductNotificationEmail(userEmail, userName, productName, productUrl);

            Map<String, String> response = new HashMap<>();
            response.put("message", "New product notification sent successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to send notification: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Admin: Gửi email khuyến mãi
     */
    @PostMapping("/admin/promotion")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> sendPromotionEmail(@RequestBody Map<String, String> request) {
        String userEmail = request.get("userEmail");
        String userName = request.get("userName");
        String promotionTitle = request.get("promotionTitle");
        String promotionDetails = request.get("promotionDetails");

        try {
            emailService.sendPromotionEmail(userEmail, userName, promotionTitle, promotionDetails);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Promotion email sent successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to send promotion: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Gửi email reset password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> sendPasswordResetEmail(@RequestBody Map<String, String> request) {
        String userEmail = request.get("email");
        String userName = request.get("userName");
        String resetToken = request.get("resetToken");

        try {
            emailService.sendPasswordResetEmail(userEmail, userName, resetToken);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Password reset email sent successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to send reset email: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Admin: Gửi email thông báo hàng loạt
     */
    @PostMapping("/admin/bulk-notification")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> sendBulkNotification(@RequestBody Map<String, Object> request) {
        // Implementation for bulk email notifications
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Bulk notification feature - implement as needed");
        response.put("recipientCount", 0);
        response.put("successCount", 0);
        response.put("failureCount", 0);

        return ResponseEntity.ok(response);
    }
}