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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @GetMapping("/for-you")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getRecommendationsForUser(
            @RequestParam(defaultValue = "20") int limit) {
        String userId = getCurrentUserId();
        List<Product> recommendations;
        try {
            recommendations = recommendationService.getRecommendationsForUser(userId, limit);
        } catch (Exception e) {
            System.err.println("[ERROR] RecommendationController: " + e.getMessage());
            recommendations = Collections.emptyList();
        }
        Map<String, Object> response = new HashMap<>();
        response.put("recommendations", recommendations);  
        response.put("count", recommendations.size());     
        response.put("userId", userId);                    
        response.put("algorithm", "hybrid");              
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/similar/{productId}")
    public ResponseEntity<Map<String, Object>> getSimilarProducts(
            @PathVariable String productId,
            @RequestParam(defaultValue = "10") int limit) {

        String userId = getCurrentUserId(); 

        List<Product> similarProducts = recommendationService.getProductRecommendations(productId, userId, limit);

        Map<String, Object> response = new HashMap<>();
        response.put("similarProducts", similarProducts);
        response.put("count", similarProducts.size());
        response.put("baseProductId", productId);
        response.put("algorithm", "content_based_similarity");

        return ResponseEntity.ok(response);
    }

   
    @PostMapping("/track")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> trackInteraction(@RequestBody Map<String, Object> request) {
        String userId = getCurrentUserId();
        String productId = (String) request.get("productId");
        String interactionType = (String) request.get("interactionType");
        String sessionId = (String) request.get("sessionId");

        @SuppressWarnings("unchecked")
        Map<String, Object> context = (Map<String, Object>) request.getOrDefault("context", new HashMap<>());

        
        recommendationService.trackInteraction(userId, productId, interactionType, sessionId, context);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Interaction tracked successfully");
        response.put("interactionType", interactionType);
        response.put("status", "processing");

        return ResponseEntity.ok(response);
    }

   
    @GetMapping("/category/{category}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getCategoryRecommendations(
            @PathVariable String category,
            @RequestParam(defaultValue = "10") int limit) {
        String userId = getCurrentUserId();


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

   
    @GetMapping("/trending")
    public ResponseEntity<Map<String, Object>> getTrendingRecommendations(
            @RequestParam(defaultValue = "15") int limit) {
        String userId = getCurrentUserId();

        List<Product> trending = recommendationService.getRecommendationsForUser(userId, limit);

        Map<String, Object> response = new HashMap<>();
        response.put("trending", trending);
        response.put("count", trending.size());
        response.put("timeframe", "last_7_days");
        response.put("algorithm", "trending_weighted");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/recently-viewed")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getRecentlyViewedRecommendations(
            @RequestParam(defaultValue = "10") int limit) {
        String userId = getCurrentUserId();

        List<Product> recommendations = recommendationService.getRecommendationsForUser(userId, limit);

        Map<String, Object> response = new HashMap<>();
        response.put("recommendations", recommendations);
        response.put("count", recommendations.size());
        response.put("basedOn", "recently_viewed");
        response.put("algorithm", "view_based_similarity");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/new-user")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getNewUserRecommendations(
            @RequestParam(defaultValue = "20") int limit) {

        List<Product> recommendations = recommendationService.getRecommendationsForUser("new_user", limit);

        Map<String, Object> response = new HashMap<>();
        response.put("recommendations", recommendations);
        response.put("count", recommendations.size());
        response.put("strategy", "popular_trending");
        response.put("message", "Recommendations for new users based on popular products");
        response.put("algorithm", "cold_start_popular");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/preferences")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserPreferences() {
        String userId = getCurrentUserId();

       
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("userId", userId);
        preferences.put("message", "User preferences retrieved");

        return ResponseEntity.ok(preferences);
    }

    @PutMapping("/preferences")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateUserPreferences(@RequestBody Map<String, Object> preferences) {
        String userId = getCurrentUserId();

        recommendationService.updateUserPreferences(userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User preferences updated successfully");
        response.put("status", "completed");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/calculate-similarities")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> calculateProductSimilarities() {

        recommendationService.calculateProductSimilarities();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Product similarities calculation started");
        response.put("status", "Processing in background");
        response.put("estimatedTime", "10-30 minutes depending on product count");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/price-range")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getRecommendationsByPriceRange(
            @RequestParam Double minPrice,
            @RequestParam Double maxPrice,
            @RequestParam(defaultValue = "15") int limit) {
        String userId = getCurrentUserId();

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

    @GetMapping("/cross-sell/{productId}")
    public ResponseEntity<Map<String, Object>> getCrossSellRecommendations(
            @PathVariable String productId,
            @RequestParam(defaultValue = "8") int limit) {

        List<Product> crossSellProducts = recommendationService.getProductRecommendations(productId, null, limit);

        Map<String, Object> response = new HashMap<>();
        response.put("crossSellProducts", crossSellProducts);
        response.put("baseProductId", productId);
        response.put("count", crossSellProducts.size());
        response.put("message", "Products often bought together");
        response.put("algorithm", "market_basket_analysis");

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
                        com.marketplace.security.JwtTokenProvider jwtProvider =
                                context.getBean(com.marketplace.security.JwtTokenProvider.class);
                        return jwtProvider.getUserIdFromToken(token);
                    }
                } catch (Exception e) {
                    System.err.println("Error extracting user ID from token: " + e.getMessage());
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
                return bearerToken.substring(7); // Remove "Bearer " prefix
            }
        } catch (Exception e) {
        }
        return null;
    }
}