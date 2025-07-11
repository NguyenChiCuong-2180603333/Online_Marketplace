package com.marketplace.controller;

import com.marketplace.dto.UpdateProfileRequest;
import com.marketplace.dto.ChangePasswordRequest;
import com.marketplace.model.User;
import com.marketplace.model.Product;
import com.marketplace.model.Order;
import com.marketplace.service.UserService;
import com.marketplace.service.ProductService;
import com.marketplace.service.OrderService;
import com.marketplace.service.FileUploadService;
import com.marketplace.security.JwtTokenProvider;
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
import java.util.Objects;

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

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

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
        profile.put("birthday", user.getBirthday());
        profile.put("address", user.getAddress());
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

        // Upload to Cloudinary using FileUploadService
        String avatarUrl = fileUploadService.uploadAvatar(file);
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
        Map<String, Object> result = new HashMap<>();
        result.put("notifications", List.of());
        result.put("unreadCount", 0);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/addresses")
    public ResponseEntity<Map<String, Object>> getAddresses() {
        String userId = getCurrentUserId();
        User user = userService.getUserById(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("addresses", List.of(Map.of(
            "id", "default",
            "fullName", user.getFirstName() + " " + user.getLastName(),
            "phone", user.getPhone(),
            "address", user.getAddress(),
            "isDefault", true
        )));
        result.put("defaultAddressId", "default");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/addresses")
    public ResponseEntity<Map<String, Object>> addAddress(@RequestBody Map<String, Object> addressData) {
        String userId = getCurrentUserId();
        User user = userService.getUserById(userId);
        
        user.setAddress((String) addressData.get("address"));
        userService.updateUser(userId, user);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Địa chỉ đã được thêm thành công");
        return ResponseEntity.ok(result);
    }

    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<Map<String, Object>> updateAddress(@PathVariable String addressId, 
                                                           @RequestBody Map<String, Object> addressData) {
        String userId = getCurrentUserId();
        User user = userService.getUserById(userId);
        
        user.setAddress((String) addressData.get("address"));
        userService.updateUser(userId, user);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Địa chỉ đã được cập nhật thành công");
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<Map<String, Object>> deleteAddress(@PathVariable String addressId) {
        String userId = getCurrentUserId();
        User user = userService.getUserById(userId);
        
        user.setAddress(null);
        userService.updateUser(userId, user);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Địa chỉ đã được xóa thành công");
        return ResponseEntity.ok(result);
    }

    @PutMapping("/addresses/{addressId}/default")
    public ResponseEntity<Map<String, Object>> setDefaultAddress(@PathVariable String addressId) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Đã đặt địa chỉ mặc định");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/payment-methods")
    public ResponseEntity<Map<String, Object>> getPaymentMethods() {
        String userId = getCurrentUserId();
        Map<String, Object> result = new HashMap<>();
        result.put("paymentMethods", List.of());
        result.put("defaultPaymentMethodId", null);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/payment-methods")
    public ResponseEntity<Map<String, Object>> addPaymentMethod(@RequestBody Map<String, Object> paymentData) {
        String userId = getCurrentUserId();
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Phương thức thanh toán đã được thêm thành công");
        return ResponseEntity.ok(result);
    }

    @PutMapping("/payment-methods/{methodId}")
    public ResponseEntity<Map<String, Object>> updatePaymentMethod(@PathVariable String methodId, 
                                                                 @RequestBody Map<String, Object> paymentData) {
        String userId = getCurrentUserId();
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Phương thức thanh toán đã được cập nhật thành công");
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/payment-methods/{methodId}")
    public ResponseEntity<Map<String, Object>> deletePaymentMethod(@PathVariable String methodId) {
        String userId = getCurrentUserId();
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Phương thức thanh toán đã được xóa thành công");
        return ResponseEntity.ok(result);
    }

    @PutMapping("/payment-methods/{methodId}/default")
    public ResponseEntity<Map<String, Object>> setDefaultPaymentMethod(@PathVariable String methodId) {
        String userId = getCurrentUserId();
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Đã đặt phương thức thanh toán mặc định");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/wishlist")
    public ResponseEntity<Map<String, Object>> getWishlist() {
        String userId = getCurrentUserId();
        Map<String, Object> result = new HashMap<>();
        result.put("wishlist", List.of());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/wishlist")
    public ResponseEntity<Map<String, Object>> addToWishlist(@RequestBody Map<String, Object> request) {
        String userId = getCurrentUserId();
        String productId = (String) request.get("productId");
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Sản phẩm đã được thêm vào danh sách yêu thích");
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/wishlist/{productId}")
    public ResponseEntity<Map<String, Object>> removeFromWishlist(@PathVariable String productId) {
        String userId = getCurrentUserId();
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Sản phẩm đã được xóa khỏi danh sách yêu thích");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/saved-items")
    public ResponseEntity<Map<String, Object>> getSavedItems() {
        String userId = getCurrentUserId();
        List<String> savedProductIds = userService.getSavedItems(userId);
        List<Product> savedProducts = savedProductIds.stream()
            .map(id -> {
                try {
                    return productService.getProductById(id);
                } catch (Exception e) {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .toList();
        Map<String, Object> result = new HashMap<>();
        result.put("savedItems", savedProducts);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/saved-items")
    public ResponseEntity<Map<String, Object>> saveForLater(@RequestBody Map<String, Object> request) {
        String userId = getCurrentUserId();
        String productId = (String) request.get("productId");
        userService.addSavedItem(userId, productId);
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Sản phẩm đã được lưu để mua sau");
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/saved-items/{itemId}")
    public ResponseEntity<Map<String, Object>> removeFromSavedItems(@PathVariable String itemId) {
        String userId = getCurrentUserId();
        userService.removeSavedItem(userId, itemId);
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Sản phẩm đã được xóa khỏi danh sách lưu");
        return ResponseEntity.ok(result);
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getPrincipal().equals("anonymousUser")) {
            String token = getJwtFromCurrentRequest();
            if (token != null) {
                try {
                    return jwtTokenProvider.getUserIdFromToken(token);
                } catch (Exception e) {
    
                }
            }
        }
        throw new RuntimeException("Cannot extract userId from JWT");
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
}