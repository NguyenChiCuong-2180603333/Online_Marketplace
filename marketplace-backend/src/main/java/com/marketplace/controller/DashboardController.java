package com.marketplace.controller;

import com.marketplace.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.marketplace.security.UserPrincipal;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class DashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private CategoryService categoryService;

    // Admin Dashboard - Tổng quan hệ thống
    @GetMapping("/admin/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAdminOverview() {
        Map<String, Object> overview = new HashMap<>();

        // User statistics
        Map<String, Object> userStats = userService.getUserStats();
        overview.put("users", userStats);

        // Product statistics
        Map<String, Object> productStats = productService.getProductStatistics();
        overview.put("products", productStats);

        // Order statistics
        Map<String, Object> orderStats = getOrderStatistics();
        overview.put("orders", orderStats);

        // Revenue statistics
        Map<String, Object> revenueStats = getRevenueStatistics();
        overview.put("revenue", revenueStats);

        // Recent activities
        overview.put("recentOrders", orderService.getRecentOrders(10));
        overview.put("lowStockProducts", productService.getLowStockProducts(10));

        overview.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return ResponseEntity.ok(overview);
    }

    // Seller Dashboard - Tổng quan người bán
    @GetMapping("/seller/overview")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSellerOverview() {
        String sellerId = getCurrentUserId();
        Map<String, Object> overview = new HashMap<>();

        // Seller's products
        List<com.marketplace.model.Product> sellerProducts = productService.getProductsBySeller(sellerId);
        overview.put("totalProducts", sellerProducts.size());
        overview.put("activeProducts", sellerProducts.stream().mapToInt(p -> p.isActive() ? 1 : 0).sum());

        // Seller's orders
        List<com.marketplace.model.Order> sellerOrders = orderService.getOrdersBySeller(sellerId);
        overview.put("totalOrders", sellerOrders.size());
        overview.put("pendingOrders", sellerOrders.stream().mapToInt(o -> "PENDING".equals(o.getStatus()) ? 1 : 0).sum());
        overview.put("completedOrders", sellerOrders.stream().mapToInt(o -> "DELIVERED".equals(o.getStatus()) ? 1 : 0).sum());

        // Revenue
        double totalRevenue = sellerOrders.stream()
                .filter(o -> "DELIVERED".equals(o.getStatus()))
                .mapToDouble(com.marketplace.model.Order::getTotalAmount)
                .sum();
        overview.put("totalRevenue", totalRevenue);

        // Top products by reviews
        List<com.marketplace.model.Product> topProducts = sellerProducts.stream()
                .filter(p -> p.getReviewCount() > 0)
                .sorted((p1, p2) -> Double.compare(p2.getAverageRating(), p1.getAverageRating()))
                .limit(5)
                .toList();
        overview.put("topRatedProducts", topProducts);

        // Low stock alerts
        List<com.marketplace.model.Product> lowStockProducts = sellerProducts.stream()
                .filter(p -> p.getStockQuantity() <= 5)
                .toList();
        overview.put("lowStockProducts", lowStockProducts);

        return ResponseEntity.ok(overview);
    }

    // User Dashboard - Tổng quan người mua
    @GetMapping("/user/overview")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserOverview() {
        String userId = getCurrentUserId();
        Map<String, Object> overview = new HashMap<>();

        // User's orders
        List<com.marketplace.model.Order> userOrders = orderService.getOrdersByUserId(userId);
        overview.put("totalOrders", userOrders.size());
        overview.put("pendingOrders", userOrders.stream().mapToInt(o -> "PENDING".equals(o.getStatus()) ? 1 : 0).sum());
        overview.put("deliveredOrders", userOrders.stream().mapToInt(o -> "DELIVERED".equals(o.getStatus()) ? 1 : 0).sum());

        // Spending
        double totalSpent = userOrders.stream()
                .filter(o -> "DELIVERED".equals(o.getStatus()))
                .mapToDouble(com.marketplace.model.Order::getTotalAmount)
                .sum();
        overview.put("totalSpent", totalSpent);

        // Recent orders
        overview.put("recentOrders", userOrders.stream().limit(5).toList());

        // Reviews
        List<com.marketplace.model.Review> userReviews = reviewService.getReviewsByUserId(userId);
        overview.put("totalReviews", userReviews.size());

        // Favorite categories (based on purchase history)
        Map<String, Integer> categoryPurchases = new HashMap<>();
        userOrders.stream()
                .filter(o -> "DELIVERED".equals(o.getStatus()))
                .flatMap(o -> o.getItems().stream())
                .forEach(item -> {
                    try {
                        com.marketplace.model.Product product = productService.getProductById(item.getProductId());
                        categoryPurchases.merge(product.getCategory(), 1, Integer::sum);
                    } catch (Exception e) {
                        // Product might be deleted
                    }
                });
        overview.put("favoriteCategories", categoryPurchases);

        return ResponseEntity.ok(overview);
    }

    // Analytics endpoints
    @GetMapping("/analytics/sales")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSalesAnalytics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        Map<String, Object> analytics = new HashMap<>();

        // Mock daily sales data - in production, implement proper date filtering
        analytics.put("dailySales", List.of(
                Map.of("date", "2024-01-01", "sales", 1500000, "orders", 25),
                Map.of("date", "2024-01-02", "sales", 2200000, "orders", 35),
                Map.of("date", "2024-01-03", "sales", 1800000, "orders", 30)
        ));

        analytics.put("totalRevenue", orderService.getTotalRevenue());
        analytics.put("averageOrderValue", orderService.getTotalRevenue() / Math.max(1, orderService.getTotalOrdersCount()));

        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/analytics/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getProductAnalytics() {
        Map<String, Object> analytics = productService.getProductStatistics();

        // Add top selling products
        analytics.put("topSellingProducts", productService.getFeaturedProducts());
        analytics.put("topRatedProducts", productService.getTopRatedProducts(10));

        return ResponseEntity.ok(analytics);
    }

    private Map<String, Object> getOrderStatistics() {
        Map<String, Object> orderStats = new HashMap<>();

        orderStats.put("total", orderService.getTotalOrdersCount());
        orderStats.put("pending", orderService.getOrdersCountByStatus("PENDING"));
        orderStats.put("processing", orderService.getOrdersCountByStatus("PROCESSING"));
        orderStats.put("shipped", orderService.getOrdersCountByStatus("SHIPPED"));
        orderStats.put("delivered", orderService.getOrdersCountByStatus("DELIVERED"));
        orderStats.put("cancelled", orderService.getOrdersCountByStatus("CANCELLED"));

        return orderStats;
    }

    private Map<String, Object> getRevenueStatistics() {
        Map<String, Object> revenueStats = new HashMap<>();

        double totalRevenue = orderService.getTotalRevenue();
        long completedOrders = orderService.getOrdersCountByStatus("DELIVERED");

        revenueStats.put("total", totalRevenue);
        revenueStats.put("averageOrderValue", completedOrders > 0 ? totalRevenue / completedOrders : 0);
        revenueStats.put("completedOrders", completedOrders);

        return revenueStats;
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserPrincipal) {
                return ((UserPrincipal) principal).getId();
            }
        }
        throw new RuntimeException("Cannot get current user id");
    }
}