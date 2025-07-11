package com.marketplace.service;

import com.marketplace.model.*;
import com.marketplace.repository.*;
import com.marketplace.dto.RecommendationRequest;
import com.marketplace.dto.RecommendationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    @Autowired
    private UserPreferenceRepository userPreferenceRepository;

    @Autowired
    private UserInteractionRepository userInteractionRepository;

    @Autowired
    private ProductSimilarityRepository productSimilarityRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ReviewService reviewService;

    // Lấy recommendations chính cho user
    public List<Product> getRecommendationsForUser(String userId, int limit) {
        try {
            Set<String> recommendedProductIds = new LinkedHashSet<>();

            List<String> contentBasedIds = getContentBasedRecommendations(userId, limit / 3);
            recommendedProductIds.addAll(contentBasedIds);

            List<String> collaborativeIds = getCollaborativeRecommendations(userId, limit / 3);
            recommendedProductIds.addAll(collaborativeIds);

            List<String> popularIds = getPopularRecommendations(userId, limit / 3);
            recommendedProductIds.addAll(popularIds);

            if (recommendedProductIds.size() < limit) {
                List<String> fallbackIds = getFallbackRecommendations(userId,
                        limit - recommendedProductIds.size(), recommendedProductIds);
                recommendedProductIds.addAll(fallbackIds);
            }

            return recommendedProductIds.stream()
                    .limit(limit)
                    .map(id -> {
                        try {
                            return productService.getProductById(id);
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Recommendation error for user " + userId + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<String> getContentBasedRecommendations(String userId, int limit) {
        UserPreference preference = getUserPreference(userId);
        List<Product> allProducts = productService.getAllProducts();

        List<ProductScore> scoredProducts = allProducts.stream()
                .map(product -> new ProductScore(product.getId(),
                        calculateContentScore(product, preference)))
                .sorted((a, b) -> Double.compare(b.score, a.score))
                .limit(limit)
                .collect(Collectors.toList());

        return scoredProducts.stream()
                .map(ps -> ps.productId)
                .collect(Collectors.toList());
    }

    private List<String> getCollaborativeRecommendations(String userId, int limit) {
        List<String> similarUsers = findSimilarUsers(userId, 10);

        Set<String> recommendedIds = new LinkedHashSet<>();

        for (String similarUserId : similarUsers) {
            List<UserInteraction> interactions = userInteractionRepository
                    .findByUserIdAndInteractionTypeOrderByCreatedAtDesc(similarUserId, "PURCHASE");

            interactions.stream()
                    .map(UserInteraction::getProductId)
                    .filter(productId -> !hasUserInteractedWith(userId, productId))
                    .forEach(recommendedIds::add);

            if (recommendedIds.size() >= limit) break;
        }

        return recommendedIds.stream().limit(limit).collect(Collectors.toList());
    }

    private List<String> getPopularRecommendations(String userId, int limit) {
        LocalDateTime lastWeek = LocalDateTime.now().minusDays(7);

        List<UserInteraction> recentInteractions = userInteractionRepository
                .findTrendingInteractions(lastWeek);

        Map<String, Long> productCounts = recentInteractions.stream()
                .filter(i -> i.getProductId() != null)
                .collect(Collectors.groupingBy(
                        UserInteraction::getProductId,
                        Collectors.counting()
                ));

        return productCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .filter(productId -> !hasUserInteractedWith(userId, productId))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private List<String> getFallbackRecommendations(String userId, int limit, Set<String> excludeIds) {
        List<Product> products = productService.getTopRatedProducts(limit * 2);

        return products.stream()
                .map(Product::getId)
                .filter(id -> !excludeIds.contains(id))
                .filter(id -> !hasUserInteractedWith(userId, id))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<Product> getProductRecommendations(String productId, String userId, int limit) {
        Set<String> recommendedIds = new LinkedHashSet<>();

        List<ProductSimilarity> similarities = productSimilarityRepository
                .findByProductIdOrderBySimilarityScoreDesc(productId);

        similarities.stream()
                .limit(limit / 2)
                .map(ProductSimilarity::getSimilarProductId)
                .forEach(recommendedIds::add);

        try {
            Product product = productService.getProductById(productId);
            List<Product> categoryProducts = productService.getProductsByCategory(product.getCategory());

            categoryProducts.stream()
                    .filter(p -> !p.getId().equals(productId))
                    .sorted((a, b) -> Double.compare(b.getAverageRating(), a.getAverageRating()))
                    .limit(limit / 2)
                    .map(Product::getId)
                    .forEach(recommendedIds::add);

        } catch (Exception e) {
        }

        return recommendedIds.stream()
                .limit(limit)
                .map(id -> {
                    try {
                        return productService.getProductById(id);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public void trackInteraction(String userId, String productId, String interactionType,
                                 String sessionId, Map<String, Object> context) {
        UserInteraction interaction = new UserInteraction(userId, productId, interactionType);
        interaction.setSessionId(sessionId);

        if (context != null) {
            if (context.containsKey("duration")) {
                interaction.setDuration((Double) context.get("duration"));
            }
            if (context.containsKey("deviceType")) {
                interaction.setDeviceType((String) context.get("deviceType"));
            }
            if (context.containsKey("searchQuery")) {
                interaction.setSearchQuery((String) context.get("searchQuery"));
            }
            if (context.containsKey("rating")) {
                interaction.setRating((Double) context.get("rating"));
            }
        }

        userInteractionRepository.save(interaction);

        java.util.concurrent.CompletableFuture.runAsync(() -> updateUserPreferences(userId));
    }

    public void updateUserPreferences(String userId) {
        UserPreference preference = getUserPreference(userId);

        LocalDateTime lastMonth = LocalDateTime.now().minusDays(30);
        List<UserInteraction> recentInteractions = userInteractionRepository
                .findByUserIdAndCreatedAtBetween(userId, lastMonth, LocalDateTime.now());

        updateCategoryPreferences(preference, recentInteractions);

        updatePricePreferences(preference, recentInteractions);

        updateTimePreferences(preference, recentInteractions);

        preference.setUpdatedAt(LocalDateTime.now());
        userPreferenceRepository.save(preference);
    }

    public void calculateProductSimilarities() {
        List<Product> products = productService.getAllProducts();

        for (Product product1 : products) {
            for (Product product2 : products) {
                if (!product1.getId().equals(product2.getId())) {
                    double similarity = calculateProductSimilarity(product1, product2);

                    if (similarity > 0.3) { 
                        ProductSimilarity ps = new ProductSimilarity(
                                product1.getId(),
                                product2.getId(),
                                similarity,
                                "CONTENT"
                        );

                        if (!productSimilarityRepository.existsByProductIdAndSimilarProductId(
                                product1.getId(), product2.getId())) {
                            productSimilarityRepository.save(ps);
                        }
                    }
                }
            }
        }
    }

    private UserPreference getUserPreference(String userId) {
        return userPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserPreference newPref = new UserPreference(userId);
                    return userPreferenceRepository.save(newPref);
                });
    }

    private double calculateContentScore(Product product, UserPreference preference) {
        double score = 0.0;

        Double categoryScore = preference.getCategoryPreferences().get(product.getCategory());
        if (categoryScore != null) {
            score += categoryScore * 0.3;
        }

        if (product.getPrice() >= preference.getMinPreferredPrice() &&
                product.getPrice() <= preference.getMaxPreferredPrice()) {
            score += 0.2;
        }

        score += (product.getAverageRating() / 5.0) * 0.3;

        for (String tag : product.getTags()) {
            Double tagScore = preference.getTagPreferences().get(tag);
            if (tagScore != null) {
                score += tagScore * 0.1;
            }
        }

        Double sellerScore = preference.getSellerPreferences().get(product.getSellerId());
        if (sellerScore != null) {
            score += sellerScore * 0.1;
        }

        return Math.min(score, 1.0);
    }

    private List<String> findSimilarUsers(String userId, int limit) {
        List<String> userProducts = userInteractionRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(UserInteraction::getProductId)
                .distinct()
                .collect(Collectors.toList());

        if (userProducts.isEmpty()) return List.of();

        List<UserInteraction> similarInteractions = userInteractionRepository
                .findSimilarUsersByProducts(userProducts, userId);

        Map<String, Long> userCounts = similarInteractions.stream()
                .collect(Collectors.groupingBy(
                        UserInteraction::getUserId,
                        Collectors.counting()
                ));

        return userCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(limit)
                .collect(Collectors.toList());
    }

    private boolean hasUserInteractedWith(String userId, String productId) {
        return userInteractionRepository.countByUserIdAndProductIdAndInteractionType(userId, productId, "VIEW") > 0 ||
               userInteractionRepository.countByUserIdAndProductIdAndInteractionType(userId, productId, "PURCHASE") > 0;
    }

    private double calculateProductSimilarity(Product p1, Product p2) {
        double similarity = 0.0;

        if (p1.getCategory().equals(p2.getCategory())) {
            similarity += 0.4;
        }

        double priceDiff = Math.abs(p1.getPrice() - p2.getPrice());
        double maxPrice = Math.max(p1.getPrice(), p2.getPrice());
        if (maxPrice > 0) {
            similarity += (1.0 - (priceDiff / maxPrice)) * 0.2;
        }

        Set<String> tags1 = new HashSet<>(p1.getTags());
        Set<String> tags2 = new HashSet<>(p2.getTags());
        Set<String> intersection = new HashSet<>(tags1);
        intersection.retainAll(tags2);
        Set<String> union = new HashSet<>(tags1);
        union.addAll(tags2);

        if (!union.isEmpty()) {
            similarity += ((double) intersection.size() / union.size()) * 0.3;
        }

        double ratingDiff = Math.abs(p1.getAverageRating() - p2.getAverageRating());
        similarity += (1.0 - (ratingDiff / 5.0)) * 0.1;

        return Math.min(similarity, 1.0);
    }

    private void updateCategoryPreferences(UserPreference preference, List<UserInteraction> interactions) {
        Map<String, Double> categoryScores = new HashMap<>();

        for (UserInteraction interaction : interactions) {
            try {
                Product product = productService.getProductById(interaction.getProductId());
                String category = product.getCategory();

                double weight = getInteractionWeight(interaction.getInteractionType());
                categoryScores.merge(category, weight, Double::sum);

            } catch (Exception e) {
            }
        }

        double maxScore = categoryScores.values().stream().mapToDouble(d -> d).max().orElse(1.0);
        categoryScores.replaceAll((k, v) -> v / maxScore);

        preference.setCategoryPreferences(categoryScores);
    }

    private void updatePricePreferences(UserPreference preference, List<UserInteraction> interactions) {
        List<Double> prices = new ArrayList<>();

        for (UserInteraction interaction : interactions) {
            if ("PURCHASE".equals(interaction.getInteractionType())) {
                try {
                    Product product = productService.getProductById(interaction.getProductId());
                    prices.add(product.getPrice());
                } catch (Exception e) {
                }
            }
        }

        if (!prices.isEmpty()) {
            prices.sort(Double::compareTo);
            int size = prices.size();

            preference.setMinPreferredPrice(prices.get(size / 4)); 
            preference.setMaxPreferredPrice(prices.get(3 * size / 4)); 
        }
    }

    private void updateTimePreferences(UserPreference preference, List<UserInteraction> interactions) {
        Map<Integer, Double> timeScores = new HashMap<>();

        for (UserInteraction interaction : interactions) {
            int hour = interaction.getCreatedAt().getHour();
            double weight = getInteractionWeight(interaction.getInteractionType());
            timeScores.merge(hour, weight, Double::sum);
        }

        preference.setTimePreferences(timeScores);
    }

    private double getInteractionWeight(String interactionType) {
        return switch (interactionType) {
            case "PURCHASE" -> 1.0;
            case "ADD_TO_CART" -> 0.8;
            case "REVIEW" -> 0.7;
            case "LIKE" -> 0.5;
            case "CLICK" -> 0.3;
            case "VIEW" -> 0.1;
            default -> 0.05;
        };
    }

    private static class ProductScore {
        String productId;
        double score;

        ProductScore(String productId, double score) {
            this.productId = productId;
            this.score = score;
        }
    }
}