package com.marketplace.controller;

import com.marketplace.model.Order;
import com.marketplace.model.Product;
import com.marketplace.model.User;
import com.marketplace.service.OrderService;
import com.marketplace.service.ProductService;
import com.marketplace.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    // Dashboard Statistics
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // User statistics
        List<User> allUsers = userService.getAllUsers();
        stats.put("totalUsers", allUsers.size());
        stats.put("activeUsers", allUsers.stream().filter(User::isEnabled).count());

        // Product statistics
        List<Product> allProducts = productService.getAllProducts();
        stats.put("totalProducts", allProducts.size());
        stats.put("activeProducts", allProducts.stream().filter(Product::isActive).count());

        // Order statistics
        stats.put("totalOrders", orderService.getTotalOrdersCount());
        stats.put("pendingOrders", orderService.getOrdersCountByStatus("PENDING"));
        stats.put("processingOrders", orderService.getOrdersCountByStatus("PROCESSING"));
        stats.put("completedOrders", orderService.getOrdersCountByStatus("DELIVERED"));

        // Revenue
        stats.put("totalRevenue", orderService.getTotalRevenue());

        // Recent orders
        stats.put("recentOrders", orderService.getRecentOrders(10));

        return ResponseEntity.ok(stats);
    }

    // User Management
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable String userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/users/{userId}/toggle-status")
    public ResponseEntity<?> toggleUserStatus(@PathVariable String userId) {
        userService.toggleUserStatus(userId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Trạng thái người dùng đã được cập nhật");
        return ResponseEntity.ok(response);
    }

    // Product Management
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @PutMapping("/products/{productId}/toggle-status")
    public ResponseEntity<?> toggleProductStatus(@PathVariable String productId) {
        productService.toggleProductStatus(productId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Trạng thái sản phẩm đã được cập nhật");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable String productId) {
        productService.adminDeleteProduct(productId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Sản phẩm đã được xóa");
        return ResponseEntity.ok(response);
    }

    // Order Management
    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/orders/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable String status) {
        List<Order> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable String orderId,
            @RequestBody Map<String, String> request) {
        String status = request.get("status");
        Order order = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(order);
    }

    // Analytics
    @GetMapping("/analytics/revenue")
    public ResponseEntity<Map<String, Object>> getRevenueAnalytics() {
        Map<String, Object> analytics = new HashMap<>();

        analytics.put("totalRevenue", orderService.getTotalRevenue());
        analytics.put("completedOrders", orderService.getOrdersCountByStatus("DELIVERED"));
        analytics.put("averageOrderValue", orderService.getTotalRevenue() / Math.max(1, orderService.getOrdersCountByStatus("DELIVERED")));

        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/analytics/orders")
    public ResponseEntity<Map<String, Object>> getOrderAnalytics() {
        Map<String, Object> analytics = new HashMap<>();

        analytics.put("totalOrders", orderService.getTotalOrdersCount());
        analytics.put("pendingOrders", orderService.getOrdersCountByStatus("PENDING"));
        analytics.put("processingOrders", orderService.getOrdersCountByStatus("PROCESSING"));
        analytics.put("shippedOrders", orderService.getOrdersCountByStatus("SHIPPED"));
        analytics.put("deliveredOrders", orderService.getOrdersCountByStatus("DELIVERED"));
        analytics.put("cancelledOrders", orderService.getOrdersCountByStatus("CANCELLED"));

        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/analytics/products")
    public ResponseEntity<Map<String, Object>> getProductAnalytics() {
        Map<String, Object> analytics = new HashMap<>();

        List<Product> allProducts = productService.getAllProducts();
        analytics.put("totalProducts", allProducts.size());
        analytics.put("activeProducts", allProducts.stream().filter(Product::isActive).count());
        analytics.put("featuredProducts", productService.getFeaturedProducts().size());

        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/analytics/users")
    public ResponseEntity<Map<String, Object>> getUserAnalytics() {
        Map<String, Object> analytics = new HashMap<>();

        List<User> allUsers = userService.getAllUsers();
        analytics.put("totalUsers", allUsers.size());
        analytics.put("activeUsers", allUsers.stream().filter(User::isEnabled).count());
        analytics.put("adminUsers", allUsers.stream().filter(u -> "ADMIN".equals(u.getRole())).count());
        analytics.put("regularUsers", allUsers.stream().filter(u -> "USER".equals(u.getRole())).count());

        return ResponseEntity.ok(analytics);
    }
}