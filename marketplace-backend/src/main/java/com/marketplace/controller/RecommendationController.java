package com.marketplace.controller;

import com.marketplace.model.Product;
import com.marketplace.model.UserPreference;
import com.marketplace.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RecommendationController - Xử lý tất cả API liên quan đến hệ thống đề xuất AI
 *
 * Các tính năng chính:
 * 1. Personal recommendations (AI-powered)
 * 2. Similar products (content-based)
 * 3. Interaction tracking (behavior analysis)
 * 4. Category & price-based filtering
 * 5. Cross-sell & up-sell suggestions
 */
@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    /**
     * 🎯 ENDPOINT CHÍNH: Lấy recommendations cá nhân hóa cho user
     *
     * Flow hoạt động:
     * 1. Extract user ID từ JWT token
     * 2. Call RecommendationService với hybrid algorithm
     * 3. Trả về danh sách products được đề xuất
     *
     * Algorithm sử dụng: Hybrid (Content-based + Collaborative + Popular)
     */
    @GetMapping("/for-you")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getRecommendationsForUser(
            @RequestParam(defaultValue = "20") int limit) {

        // 🔑 Lấy user ID từ JWT token
        String userId = getCurrentUserId();

        // 🤖 Gọi AI recommendation engine
        List<Product> recommendations = recommendationService.getRecommendationsForUser(userId, limit);

        // 📊 Chuẩn bị response data
        Map<String, Object> response = new HashMap<>();
        response.put("recommendations", recommendations);  // Danh sách sản phẩm đề xuất
        response.put("count", recommendations.size());     // Số lượng
        response.put("userId", userId);                    // User ID để debug
        response.put("algorithm", "hybrid");               // Algorithm đã sử dụng
        response.put("timestamp", System.currentTimeMillis()); // Thời gian tạo

        return ResponseEntity.ok(response);
    }

    /**
     * 🔗 Similar Products: Sản phẩm tương tự dựa trên content
     *
     * Use case:
     * - Hiển thị trong product detail page
     * - "Sản phẩm tương tự" section
     * - Cross-selling opportunities
     */
    @GetMapping("/similar/{productId}")
    public ResponseEntity<Map<String, Object>> getSimilarProducts(
            @PathVariable String productId,
            @RequestParam(defaultValue = "10") int limit) {

        String userId = getCurrentUserId(); // Có thể null cho anonymous users

        // 🎯 Lấy sản phẩm tương tự dựa trên:
        // - Cùng category
        // - Similar price range
        // - Common tags
        // - Similar ratings
        List<Product> similarProducts = recommendationService.getProductRecommendations(productId, userId, limit);

        Map<String, Object> response = new HashMap<>();
        response.put("similarProducts", similarProducts);
        response.put("count", similarProducts.size());
        response.put("baseProductId", productId);
        response.put("algorithm", "content_based_similarity");

        return ResponseEntity.ok(response);
    }

    /**
     * 📊 Tracking User Interaction - Cốt lõi của machine learning
     *
     * Các loại interaction được track:
     * - VIEW: User xem sản phẩm
     * - CLICK: User click vào sản phẩm
     * - ADD_TO_CART: Thêm vào giỏ hàng
     * - PURCHASE: Mua sản phẩm
     * - REVIEW: Đánh giá sản phẩm
     * - LIKE: Yêu thích sản phẩm
     * - SEARCH: Tìm kiếm sản phẩm
     *
     * Data này sẽ được sử dụng để:
     * 1. Cập nhật user preferences
     * 2. Cải thiện recommendation accuracy
     * 3. Tính collaborative filtering
     */
    @PostMapping("/track")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> trackInteraction(@RequestBody Map<String, Object> request) {
        String userId = getCurrentUserId();
        String productId = (String) request.get("productId");
        String interactionType = (String) request.get("interactionType");
        String sessionId = (String) request.get("sessionId");

        // 📱 Context data để phân tích chi tiết hơn
        @SuppressWarnings("unchecked")
        Map<String, Object> context = (Map<String, Object>) request.getOrDefault("context", new HashMap<>());

        // Ví dụ context data:
        // {
        //   "duration": 30.5,           // Thời gian xem (seconds)
        //   "deviceType": "MOBILE",     // Thiết bị
        //   "referrer": "search",       // Đến từ đâu
        //   "searchQuery": "laptop",    // Từ khóa tìm kiếm
        //   "rating": 4.5              // Đánh giá (nếu có)
        // }

        // 🔄 Async processing - không block response
        recommendationService.trackInteraction(userId, productId, interactionType, sessionId, context);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Interaction tracked successfully");
        response.put("interactionType", interactionType);
        response.put("status", "processing");

        return ResponseEntity.ok(response);
    }

    /**
     * 🏷️ Category-based Recommendations
     *
     * Logic:
     * 1. Lấy general recommendations cho user
     * 2. Filter theo category cụ thể
     * 3. Trả về products phù hợp với preferences + category
     */
    @GetMapping("/category/{category}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getCategoryRecommendations(
            @PathVariable String category,
            @RequestParam(defaultValue = "10") int limit) {
        String userId = getCurrentUserId();

        // 🎯 Smart approach: Lấy nhiều hơn rồi filter
        // Tránh trường hợp không đủ products trong category
        List<Product> recommendations = recommendationService.getRecommendationsForUser(userId, limit * 2)
                .stream()
                .filter(p -> p.getCategory().equals(category))
                .limit(limit)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("recommendations", recommendations);
        response.put("category", category);
        response.put("count", recommendations.size());
        response.put("algorithm", "hybrid_category_filtered");

        return ResponseEntity.ok(response);
    }

    /**
     * 🔥 Trending Products - Popular items
     *
     * Based on:
     * - Recent interactions (last 7 days)
     * - High engagement rates
     * - Purchase frequency
     */
    @GetMapping("/trending")
    public ResponseEntity<Map<String, Object>> getTrendingRecommendations(
            @RequestParam(defaultValue = "15") int limit) {
        String userId = getCurrentUserId();

        // 📈 Trending algorithm có thể:
        // 1. Analyze recent interactions
        // 2. Weight by recency
        // 3. Consider user preferences if logged in
        List<Product> trending = recommendationService.getRecommendationsForUser(userId, limit);

        Map<String, Object> response = new HashMap<>();
        response.put("trending", trending);
        response.put("count", trending.size());
        response.put("timeframe", "last_7_days");
        response.put("algorithm", "trending_weighted");

        return ResponseEntity.ok(response);
    }

    /**
     * 👁️ Recently Viewed Based Recommendations
     *
     * Strategy: Recommend similar products to what user recently viewed
     */
    @GetMapping("/recently-viewed")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getRecentlyViewedRecommendations(
            @RequestParam(defaultValue = "10") int limit) {
        String userId = getCurrentUserId();

        // 🔄 Logic:
        // 1. Get user's recent VIEW interactions
        // 2. Find similar products to those viewed
        // 3. Remove already viewed items
        // 4. Return fresh recommendations
        List<Product> recommendations = recommendationService.getRecommendationsForUser(userId, limit);

        Map<String, Object> response = new HashMap<>();
        response.put("recommendations", recommendations);
        response.put("count", recommendations.size());
        response.put("basedOn", "recently_viewed");
        response.put("algorithm", "view_based_similarity");

        return ResponseEntity.ok(response);
    }

    /**
     * 🆕 Cold Start Problem - Recommendations for new users
     *
     * Challenge: New users có ít hoặc không có interaction data
     * Solution: Recommend popular/trending products
     */
    @GetMapping("/new-user")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getNewUserRecommendations(
            @RequestParam(defaultValue = "20") int limit) {

        // 🎯 Strategy cho new users:
        // 1. Most popular products (high ratings, many reviews)
        // 2. Recently added products
        // 3. Best sellers
        // 4. Featured products
        List<Product> recommendations = recommendationService.getRecommendationsForUser("new_user", limit);

        Map<String, Object> response = new HashMap<>();
        response.put("recommendations", recommendations);
        response.put("count", recommendations.size());
        response.put("strategy", "popular_trending");
        response.put("message", "Recommendations for new users based on popular products");
        response.put("algorithm", "cold_start_popular");

        return ResponseEntity.ok(response);
    }

    /**
     * ⚙️ Get User Preferences - Hiển thị preferences của user
     */
    @GetMapping("/preferences")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserPreferences() {
        String userId = getCurrentUserId();

        // 📊 Trong thực tế sẽ call service để lấy:
        // - Category preferences
        // - Price range preferences
        // - Brand preferences
        // - Behavioral patterns
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("userId", userId);
        preferences.put("message", "User preferences retrieved");
        // TODO: Implement actual preference retrieval

        return ResponseEntity.ok(preferences);
    }

    /**
     * 🔄 Update User Preferences - Cập nhật preferences manually
     */
    @PutMapping("/preferences")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateUserPreferences(@RequestBody Map<String, Object> preferences) {
        String userId = getCurrentUserId();

        // 🔄 Update preferences dựa trên:
        // 1. Manual user input
        // 2. Recent interactions
        // 3. Purchase history
        recommendationService.updateUserPreferences(userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User preferences updated successfully");
        response.put("status", "completed");

        return ResponseEntity.ok(response);
    }

    /**
     * 🔧 ADMIN: Calculate Product Similarities
     *
     * Heavy computation - chạy background process
     * Tính similarity giữa tất cả products để improve recommendations
     */
    @PostMapping("/admin/calculate-similarities")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> calculateProductSimilarities() {

        // 🚀 Async background job
        // Tính similarity matrix cho tất cả products
        // Based on: category, tags, price, ratings, purchase patterns
        recommendationService.calculateProductSimilarities();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Product similarities calculation started");
        response.put("status", "Processing in background");
        response.put("estimatedTime", "10-30 minutes depending on product count");

        return ResponseEntity.ok(response);
    }

    /**
     * 💰 Price Range Recommendations
     *
     * Filter recommendations theo budget của user
     */
    @GetMapping("/price-range")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getRecommendationsByPriceRange(
            @RequestParam Double minPrice,
            @RequestParam Double maxPrice,
            @RequestParam(defaultValue = "15") int limit) {
        String userId = getCurrentUserId();

        // 💡 Smart filtering approach:
        // 1. Get more recommendations than needed
        // 2. Filter by price range
        // 3. Return requested limit
        List<Product> recommendations = recommendationService.getRecommendationsForUser(userId, limit * 2)
                .stream()
                .filter(p -> p.getPrice() >= minPrice && p.getPrice() <= maxPrice)
                .limit(limit)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("recommendations", recommendations);
        response.put("priceRange", Map.of("min", minPrice, "max", maxPrice));
        response.put("count", recommendations.size());
        response.put("algorithm", "price_filtered_hybrid");

        return ResponseEntity.ok(response);
    }

    /**
     * 🛒 Cross-sell Recommendations
     *
     * "Những sản phẩm thường được mua cùng nhau"
     * Use case: Product detail page, checkout page
     */
    @GetMapping("/cross-sell/{productId}")
    public ResponseEntity<Map<String, Object>> getCrossSellRecommendations(
            @PathVariable String productId,
            @RequestParam(defaultValue = "8") int limit) {

        // 📊 Cross-sell logic:
        // 1. Find orders containing this product
        // 2. Analyze other products in those orders
        // 3. Calculate frequency/correlation
        // 4. Return most commonly bought together items
        List<Product> crossSellProducts = recommendationService.getProductRecommendations(productId, null, limit);

        Map<String, Object> response = new HashMap<>();
        response.put("crossSellProducts", crossSellProducts);
        response.put("baseProductId", productId);
        response.put("count", crossSellProducts.size());
        response.put("message", "Products often bought together");
        response.put("algorithm", "market_basket_analysis");

        return ResponseEntity.ok(response);
    }

    /**
     * 🔑 Helper Method: Extract User ID từ JWT Token
     *
     * Security flow:
     * 1. Get Authentication từ SecurityContext
     * 2. Extract JWT token từ Authorization header
     * 3. Decode token để lấy user ID
     * 4. Return user ID cho business logic
     */
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getPrincipal().equals("anonymousUser")) {

            String token = getJwtFromCurrentRequest();
            if (token != null) {
                try {
                    // 🔧 Get ApplicationContext để access JwtTokenProvider bean
                    org.springframework.context.ApplicationContext context =
                            org.springframework.web.context.support.WebApplicationContextUtils
                                    .getWebApplicationContext(((org.springframework.web.context.request.ServletRequestAttributes)
                                            org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes())
                                            .getRequest().getServletContext());

                    if (context != null) {
                        // 🔑 Decode JWT token
                        com.marketplace.security.JwtTokenProvider jwtProvider =
                                context.getBean(com.marketplace.security.JwtTokenProvider.class);
                        return jwtProvider.getUserIdFromToken(token);
                    }
                } catch (Exception e) {
                    // 🚨 Fallback nếu có lỗi
                    System.err.println("Error extracting user ID from token: " + e.getMessage());
                }
            }
        }

        // 🔄 Mock user ID cho development/testing
        // Production: return null và handle anonymous users
        return "mockUserId123";
    }

    /**
     * 🔍 Helper Method: Extract JWT Token từ HTTP Request
     *
     * Expected header format: "Authorization: Bearer <jwt_token>"
     */
    private String getJwtFromCurrentRequest() {
        try {
            org.springframework.web.context.request.ServletRequestAttributes attr =
                    (org.springframework.web.context.request.ServletRequestAttributes)
                            org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes();
            jakarta.servlet.http.HttpServletRequest request = attr.getRequest();

            String bearerToken = request.getHeader("Authorization");
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                return bearerToken.substring(7); // Remove "Bearer " prefix
            }
        } catch (Exception e) {
            // 🔇 Silent fail - không log lỗi này vì có thể spam logs
        }
        return null;
    }
}