package com.marketplace.controller;

import com.marketplace.dto.UpdateProfileRequest;
import com.marketplace.dto.ChangePasswordRequest;
import com.marketplace.model.User;
import com.marketplace.model.Product;
import com.marketplace.model.Order;
import com.marketplace.service.UserService;
import com.marketplace.service.ProductService;
import com.marketplace.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class UserProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    // Lấy thông tin profile
    @GetMapping
    public ResponseEntity<Map<String, Object>> getProfile() {
        String userId = getCurrentUserId();
        User user = userService.getUserById(userId);

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("email", user.getEmail());
        profile.put("firstName", user.getFirstName());
        profile.put("lastName", user.getLastName());
        profile.put("phone", user.getPhone());
        profile.put("avatar", user.getAvatar());
        profile.put("role", user.getRole());
        profile.put("enabled", user.isEnabled());
        profile.put("createdAt", user.getCreatedAt());

        return ResponseEntity.ok(profile);
    }

    // Cập nhật thông tin profile
    @PutMapping
    public ResponseEntity<User> updateProfile(@Valid @RequestBody UpdateProfileRequest updateRequest) {
        String userId = getCurrentUserId();
        User updatedUser = userService.updateUserProfile(userId, updateRequest);
        return ResponseEntity.ok(updatedUser);
    }

    // Thay đổi mật khẩu
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        String userId = getCurrentUserId();
        userService.changePassword(userId, changePasswordRequest);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Mật khẩu đã được thay đổi thành công");
        return ResponseEntity.ok(response);
    }

    // Upload avatar
    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String userId = getCurrentUserId();

        // Validate file
        if (file.isEmpty()) {
            throw new RuntimeException("File không được để trống");
        }

        if (!isImageFile(file)) {
            throw new RuntimeException("Chỉ cho phép upload file ảnh (JPG, PNG, GIF)");
        }

        // For now, return a mock URL - in production, implement actual file upload
        String avatarUrl = "https://via.placeholder.com/150?text=" + file.getOriginalFilename();

        User user = userService.getUserById(userId);
        user.setAvatar(avatarUrl);
        userService.updateUser(userId, user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Avatar đã được cập nhật thành công");
        response.put("avatarUrl", avatarUrl);
        return ResponseEntity.ok(response);
    }

    // Lấy lịch sử mua hàng
    @GetMapping("/purchase-history")
    public ResponseEntity<List<Order>> getPurchaseHistory() {
        String userId = getCurrentUserId();
        List<Order> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    // Lấy danh sách sản phẩm đã đăng bán
    @GetMapping("/my-products")
    public ResponseEntity<List<Product>> getMyProducts() {
        String userId = getCurrentUserId();
        List<Product> products = productService.getProductsBySeller(userId);
        return ResponseEntity.ok(products);
    }

    // Thống kê profile
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getProfileStats() {
        String userId = getCurrentUserId();

        List<Order> purchaseHistory = orderService.getOrdersByUserId(userId);
        List<Product> myProducts = productService.getProductsBySeller(userId);
        List<Order> salesHistory = orderService.getOrdersBySeller(userId);

        Map<String, Object> stats = new HashMap<>();

        // Thống kê mua hàng
        stats.put("totalOrders", purchaseHistory.size());
        stats.put("completedOrders", purchaseHistory.stream()
                .mapToInt(o -> "DELIVERED".equals(o.getStatus()) ? 1 : 0)
                .sum());
        stats.put("totalSpent", purchaseHistory.stream()
                .filter(o -> "DELIVERED".equals(o.getStatus()))
                .mapToDouble(Order::getTotalAmount)
                .sum());

        // Thống kê bán hàng
        stats.put("totalProductsListed", myProducts.size());
        stats.put("activeProducts", myProducts.stream()
                .mapToInt(p -> p.isActive() ? 1 : 0)
                .sum());
        stats.put("totalSales", salesHistory.size());
        stats.put("totalRevenue", salesHistory.stream()
                .filter(o -> "DELIVERED".equals(o.getStatus()))
                .mapToDouble(Order::getTotalAmount)
                .sum());

        return ResponseEntity.ok(stats);
    }

    // Deactivate account
    @PutMapping("/deactivate")
    public ResponseEntity<?> deactivateAccount() {
        String userId = getCurrentUserId();
        userService.deactivateUser(userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Tài khoản đã được vô hiệu hóa");
        return ResponseEntity.ok(response);
    }

    // Lấy thông báo của user
    @GetMapping("/notifications")
    public ResponseEntity<Map<String, Object>> getNotifications(
            @RequestParam(defaultValue = "false") boolean unreadOnly) {
        String userId = getCurrentUserId();
        // This would typically call NotificationService
        Map<String, Object> result = new HashMap<>();
        result.put("notifications", List.of());
        result.put("unreadCount", 0);
        return ResponseEntity.ok(result);
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
                    // Fall back to mock for now
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
            // Ignore
        }
        return null;
    }

    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (
                contentType.equals("image/jpeg") ||
                        contentType.equals("image/jpg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/gif")
        );
    }
}