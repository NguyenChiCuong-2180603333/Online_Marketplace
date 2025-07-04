package com.marketplace.controller;

import com.marketplace.model.Order;
import com.marketplace.model.Product;
import com.marketplace.model.Category;
import com.marketplace.model.User;
import com.marketplace.service.CategoryService;
import com.marketplace.service.OrderService;
import com.marketplace.service.ProductService;
import com.marketplace.service.UserService;
import com.marketplace.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

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

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats(
            @RequestParam(defaultValue = "30") int timeRange) {
        try {
            Map<String, Object> response = new HashMap<>();

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = now.minusDays(timeRange);
            LocalDateTime prevStart = start.minusDays(timeRange);
            LocalDateTime prevEnd = start;

            // User statistics
            long totalUsers = userService.getAllUsers().size();
            long usersNow = userService.getUserRepository().countByCreatedAtBetween(start, now);
            long usersPrev = userService.getUserRepository().countByCreatedAtBetween(prevStart, prevEnd);
            double userChange = usersPrev == 0 ? (usersNow > 0 ? 100.0 : 0.0) : ((usersNow - usersPrev) * 100.0 / usersPrev);
            response.put("totalUsers", totalUsers);
            response.put("activeUsers", userService.getAllUsers().stream().filter(User::isEnabled).count());

            // Product statistics
            long totalProducts = productService.getAllProductsForAdmin().size();
            long productsNow = productService.getProductRepository().countByCreatedAtBetween(start, now);
            long productsPrev = productService.getProductRepository().countByCreatedAtBetween(prevStart, prevEnd);
            double productChange = productsPrev == 0 ? (productsNow > 0 ? 100.0 : 0.0) : ((productsNow - productsPrev) * 100.0 / productsPrev);
            response.put("totalProducts", totalProducts);
            response.put("activeProducts", productService.getAllProductsForAdmin().stream().filter(Product::isActive).count());

            // Order statistics
            long totalOrders = orderService.getTotalOrdersCount();
            long ordersNow = orderService.getOrderRepository().countByCreatedAtBetween(start, now);
            long ordersPrev = orderService.getOrderRepository().countByCreatedAtBetween(prevStart, prevEnd);
            double orderChange = ordersPrev == 0 ? (ordersNow > 0 ? 100.0 : 0.0) : ((ordersNow - ordersPrev) * 100.0 / ordersPrev);
            response.put("totalOrders", totalOrders);
            response.put("pendingOrders", orderService.getOrdersCountByStatus("PENDING"));
            response.put("processingOrders", orderService.getOrdersCountByStatus("PROCESSING"));
            response.put("completedOrders", orderService.getOrdersCountByStatus("DELIVERED"));

            // Revenue
            double revenueNow = orderService.getOrderRepository().findDeliveredOrdersBetweenDates(start, now).stream().mapToDouble(Order::getTotalAmount).sum();
            double revenuePrev = orderService.getOrderRepository().findDeliveredOrdersBetweenDates(prevStart, prevEnd).stream().mapToDouble(Order::getTotalAmount).sum();
            double revenueChange = revenuePrev == 0 ? (revenueNow > 0 ? 100.0 : 0.0) : ((revenueNow - revenuePrev) * 100.0 / revenuePrev);
            response.put("totalRevenue", orderService.getTotalRevenue());

            // Recent orders (limit 10) with customer names
            List<Order> recentOrders = orderService.getRecentOrders(10);
            List<Map<String, Object>> recentOrdersWithCustomerNames = new ArrayList<>();
            for (Order order : recentOrders) {
                Map<String, Object> orderWithCustomer = new HashMap<>();
                orderWithCustomer.put("id", order.getId());
                orderWithCustomer.put("totalAmount", order.getTotalAmount());
                orderWithCustomer.put("status", order.getStatus());
                orderWithCustomer.put("createdAt", order.getCreatedAt());
                try {
                    User customer = userService.getUserById(order.getUserId());
                    orderWithCustomer.put("customerName", customer.getFirstName() + " " + customer.getLastName());
                } catch (Exception e) {
                    orderWithCustomer.put("customerName", "Khách hàng");
                }
                recentOrdersWithCustomerNames.add(orderWithCustomer);
            }
            response.put("recentOrders", recentOrdersWithCustomerNames);

            // Tỷ lệ thay đổi
            response.put("userChange", userChange);
            response.put("productChange", productChange);
            response.put("orderChange", orderChange);
            response.put("revenueChange", revenueChange);

            System.out.println("📊 Dashboard stats response: " + response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Error in getDashboardStats: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> defaultResponse = new HashMap<>();
            defaultResponse.put("totalUsers", 0);
            defaultResponse.put("activeUsers", 0);
            defaultResponse.put("totalProducts", 0);
            defaultResponse.put("activeProducts", 0);
            defaultResponse.put("totalOrders", 0);
            defaultResponse.put("pendingOrders", 0);
            defaultResponse.put("processingOrders", 0);
            defaultResponse.put("completedOrders", 0);
            defaultResponse.put("totalRevenue", 0);
            defaultResponse.put("recentOrders", new ArrayList<>());
            defaultResponse.put("userChange", 0.0);
            defaultResponse.put("productChange", 0.0);
            defaultResponse.put("orderChange", 0.0);
            defaultResponse.put("revenueChange", 0.0);
            return ResponseEntity.ok(defaultResponse);
        }
    }

    // User Management
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        // Remove passwords from response
        users.forEach(user -> user.setPassword(null));
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable String userId) {
        User user = userService.getUserById(userId);
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/users/stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        Map<String, Object> stats = userService.getUserStats();
        return ResponseEntity.ok(stats);
    }

    @PutMapping("/users/{userId}/toggle-status")
    public ResponseEntity<?> toggleUserStatus(@PathVariable String userId) {
        userService.toggleUserStatus(userId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Trạng thái người dùng đã được cập nhật");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable String userId, @RequestBody Map<String, String> request) {
        String role = request.get("role");
        userService.updateUserRole(userId, role);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Vai trò người dùng đã được cập nhật");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{userId}/vip")
    public ResponseEntity<?> toggleVipStatus(@PathVariable String userId) {
        userService.toggleVipStatus(userId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Trạng thái VIP đã được cập nhật");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Người dùng đã được xóa");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        // Set default values if not provided
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new BadRequestException("Password is required");
        }
        if (user.getRole() == null) {
            user.setRole("USER");
        }
        if (user.getFirstName() == null || user.getFirstName().isEmpty()) {
            throw new BadRequestException("First name is required");
        }
        if (user.getLastName() == null || user.getLastName().isEmpty()) {
            throw new BadRequestException("Last name is required");
        }
        
        User createdUser = userService.createUser(user);
        // Don't return password in response
        createdUser.setPassword(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<User> updateUser(@PathVariable String userId, @RequestBody User userDetails) {
        User updatedUser = userService.updateUser(userId, userDetails);
        updatedUser.setPassword(null);
        return ResponseEntity.ok(updatedUser);
    }

    // Product Management
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProductsForAdmin();
        return ResponseEntity.ok(products);
    }

    @PutMapping("/products/{productId}/toggle-status")
    public ResponseEntity<Product> toggleProductStatus(@PathVariable String productId) {
        Product updatedProduct = productService.toggleProductStatus(productId);
        return ResponseEntity.ok(updatedProduct);
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

        List<Product> allProducts = productService.getAllProductsForAdmin();
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

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategoriesForAdmin() {
        try {
            System.out.println(" Admin categories endpoint called");
            List<Category> categories = categoryService.getAllCategories();
            System.out.println("📂 Found " + categories.size() + " categories");
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            System.err.println(" Error in getAllCategoriesForAdmin: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/categories/{id}/toggle-status")
    public ResponseEntity<Map<String, String>> toggleCategoryStatus(@PathVariable String id) {
        try {
            categoryService.toggleCategoryStatus(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Trạng thái danh mục đã được cập nhật");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Không thể cập nhật trạng thái: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}