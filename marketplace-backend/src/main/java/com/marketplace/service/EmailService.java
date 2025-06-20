package com.marketplace.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.io.UnsupportedEncodingException;
import java.util.Map;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name:Cosmic Marketplace}")
    private String appName;

    /**
     * Gửi email đơn giản
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }

    /**
     * Gửi email HTML với template
     */
    public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // Set basic info
            helper.setFrom(fromEmail, appName);
            helper.setTo(to);
            helper.setSubject(subject);

            // Process template
            Context context = new Context();
            context.setVariables(variables);
            String htmlContent = templateEngine.process(templateName, context);

            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Email chào mừng user mới
     */
    public void sendWelcomeEmail(String userEmail, String userName) {
        Map<String, Object> variables = Map.of(
                "userName", userName,
                "appName", appName,
                "supportEmail", fromEmail
        );

        sendHtmlEmail(userEmail, "Chào mừng đến với " + appName, "welcome", variables);
    }

    /**
     * Email xác nhận đơn hàng
     */
    public void sendOrderConfirmationEmail(String userEmail, String userName, String orderId, Double totalAmount) {
        Map<String, Object> variables = Map.of(
                "userName", userName,
                "orderId", orderId,
                "totalAmount", String.format("%.0f VND", totalAmount),
                "appName", appName
        );

        sendHtmlEmail(userEmail, "Xác nhận đơn hàng #" + orderId, "order-confirmation", variables);
    }

    /**
     * Email cập nhật trạng thái đơn hàng
     */
    public void sendOrderStatusUpdateEmail(String userEmail, String userName, String orderId, String status) {
        String statusText = getStatusText(status);

        Map<String, Object> variables = Map.of(
                "userName", userName,
                "orderId", orderId,
                "status", statusText,
                "appName", appName
        );

        sendHtmlEmail(userEmail, "Cập nhật đơn hàng #" + orderId, "order-status-update", variables);
    }

    /**
     * Email thông báo sản phẩm mới
     */
    public void sendNewProductNotificationEmail(String userEmail, String userName, String productName, String productUrl) {
        Map<String, Object> variables = Map.of(
                "userName", userName,
                "productName", productName,
                "productUrl", productUrl,
                "appName", appName
        );

        sendHtmlEmail(userEmail, "Sản phẩm mới: " + productName, "new-product", variables);
    }

    /**
     * Email khuyến mãi
     */
    public void sendPromotionEmail(String userEmail, String userName, String promotionTitle, String promotionDetails) {
        Map<String, Object> variables = Map.of(
                "userName", userName,
                "promotionTitle", promotionTitle,
                "promotionDetails", promotionDetails,
                "appName", appName
        );

        sendHtmlEmail(userEmail, "🎉 Khuyến mãi đặc biệt: " + promotionTitle, "promotion", variables);
    }

    /**
     * Email reset password
     */
    public void sendPasswordResetEmail(String userEmail, String userName, String resetToken) {
        String resetUrl = "http://localhost:5173/reset-password?token=" + resetToken;

        Map<String, Object> variables = Map.of(
                "userName", userName,
                "resetUrl", resetUrl,
                "appName", appName
        );

        sendHtmlEmail(userEmail, "Đặt lại mật khẩu - " + appName, "password-reset", variables);
    }

    /**
     * Email thông báo review mới cho seller
     */
    public void sendNewReviewNotificationEmail(String sellerEmail, String sellerName, String productName, int rating) {
        Map<String, Object> variables = Map.of(
                "sellerName", sellerName,
                "productName", productName,
                "rating", rating,
                "appName", appName
        );

        sendHtmlEmail(sellerEmail, "Đánh giá mới cho sản phẩm: " + productName, "new-review", variables);
    }

    private String getStatusText(String status) {
        return switch (status) {
            case "PENDING" -> "Đang chờ xử lý";
            case "PROCESSING" -> "Đang xử lý";
            case "SHIPPED" -> "Đã giao cho đơn vị vận chuyển";
            case "DELIVERED" -> "Đã giao thành công";
            case "CANCELLED" -> "Đã hủy";
            default -> status;
        };
    }
}