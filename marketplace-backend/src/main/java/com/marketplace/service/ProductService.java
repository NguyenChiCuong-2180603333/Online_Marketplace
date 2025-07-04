package com.marketplace.service;

import com.marketplace.dto.ProductRequest;
import com.marketplace.model.Product;
import com.marketplace.model.User;
import com.marketplace.repository.ProductRepository;
import com.marketplace.repository.UserRepository;
import com.marketplace.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    public Product saveProduct(Product product) {
        product.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }

    public List<Product> getAllProducts() {
        return productRepository.findByActiveTrue();
    }

    public List<Product> getAllProductsForAdmin() {
        return productRepository.findAll();
    }

    public List<Product> getProductsByCategory(String category) {
        if (category == null || category.isEmpty()) {
            return getAllProducts();
        }
        return productRepository.findByCategoryAndActiveTrue(category);
    }

    public List<Product> searchProducts(String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            return getAllProducts();
        }
        return productRepository.findBySearchTermAndActiveTrue(searchTerm);
    }

    public List<Product> getProductsByPriceRange(Double minPrice, Double maxPrice) {
        return productRepository.findByPriceBetweenAndActiveTrue(minPrice, maxPrice);
    }

    public Product getProductById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    public List<Product> getProductsBySeller(String sellerId) {
        return productRepository.findBySellerId(sellerId);
    }

    public Product createProduct(ProductRequest productRequest, String sellerId) {
        Product product = new Product();
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setImages(productRequest.getImages());
        product.setCategory(productRequest.getCategory());
        if (sellerId != null) {
            sellerId = sellerId.trim();
            if (sellerId.endsWith(",")) sellerId = sellerId.substring(0, sellerId.length() - 1);
        }
        product.setSellerId(sellerId);
        product.setStockQuantity(productRequest.getStockQuantity());
        product.setTags(productRequest.getTags());
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        return productRepository.save(product);
    }

    public Product updateProduct(String id, ProductRequest productRequest, String sellerId) {
        Product product = getProductById(id);

        // Chỉ seller hoặc admin mới được update
        if (!product.getSellerId().equals(sellerId)) {
            throw new RuntimeException("You can only update your own products");
        }

        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setImages(productRequest.getImages());
        product.setCategory(productRequest.getCategory());
        product.setStockQuantity(productRequest.getStockQuantity());
        product.setTags(productRequest.getTags());
        product.setUpdatedAt(LocalDateTime.now());

        return productRepository.save(product);
    }

    public void deleteProduct(String id, String sellerId) {
        Product product = getProductById(id);

        // Chỉ seller hoặc admin mới được delete
        if (!product.getSellerId().equals(sellerId)) {
            throw new RuntimeException("You can only delete your own products");
        }

        product.setActive(false);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
    }

    public List<Product> getFeaturedProducts() {
        return productRepository.findTop10ByActiveTrueOrderByReviewCountDesc();
    }

    public List<Product> getLatestProducts() {
        return productRepository.findTop10ByActiveTrueOrderByCreatedAtDesc();
    }

    public void updateProductRating(String productId, double newAverageRating, int newReviewCount) {
        Product product = getProductById(productId);
        product.setAverageRating(newAverageRating);
        product.setReviewCount(newReviewCount);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
    }

    // Admin methods
    public Product toggleProductStatus(String productId) {
        Product product = getProductById(productId);
        product.setActive(!product.isActive());
        product.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }

    public Product adminUpdateProduct(String id, ProductRequest productRequest) {
        Product product = getProductById(id);

        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setImages(productRequest.getImages());
        product.setCategory(productRequest.getCategory());
        product.setStockQuantity(productRequest.getStockQuantity());
        product.setTags(productRequest.getTags());
        product.setUpdatedAt(LocalDateTime.now());

        return productRepository.save(product);
    }

    public void adminDeleteProduct(String id) {
        Product product = getProductById(id);
        product.setActive(false);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
    }

    public List<Product> searchProductsAdvanced(String query, String category, Double minPrice,
                                                Double maxPrice, Double minRating, String sortBy,
                                                String sortOrder, int page, int size) {
        List<Product> products = productRepository.findByActiveTrue();

        // Apply filters
        if (query != null && !query.isEmpty()) {
            products = products.stream()
                    .filter(p -> p.getName().toLowerCase().contains(query.toLowerCase()) ||
                            (p.getDescription() != null && p.getDescription().toLowerCase().contains(query.toLowerCase())) ||
                            p.getTags().stream().anyMatch(tag -> tag.toLowerCase().contains(query.toLowerCase())))
                    .toList();
        }

        if (category != null && !category.isEmpty()) {
            products = products.stream()
                    .filter(p -> p.getCategory().equals(category))
                    .toList();
        }

        if (minPrice != null) {
            products = products.stream()
                    .filter(p -> p.getPrice() >= minPrice)
                    .toList();
        }

        if (maxPrice != null) {
            products = products.stream()
                    .filter(p -> p.getPrice() <= maxPrice)
                    .toList();
        }

        if (minRating != null) {
            products = products.stream()
                    .filter(p -> p.getAverageRating() >= minRating)
                    .toList();
        }

        // Apply sorting
        products = products.stream()
                .sorted(getComparator(sortBy, sortOrder))
                .toList();

        // Apply pagination
        int start = page * size;
        int end = Math.min(start + size, products.size());

        if (start >= products.size()) {
            return List.of();
        }

        return products.subList(start, end);
    }

    public List<Product> getRelatedProducts(String productId, int limit) {
        Product product = getProductById(productId);

        // Find products in same category
        List<Product> relatedProducts = productRepository.findByCategoryAndActiveTrue(product.getCategory())
                .stream()
                .filter(p -> !p.getId().equals(productId))
                .limit(limit)
                .toList();

        return relatedProducts;
    }

    public Map<String, Object> getProductStatistics() {
        List<Product> allProducts = productRepository.findAll();
        List<Product> activeProducts = productRepository.findByActiveTrue();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProducts", allProducts.size());
        stats.put("activeProducts", activeProducts.size());
        stats.put("inactiveProducts", allProducts.size() - activeProducts.size());

        // Category breakdown
        Map<String, Long> categoryStats = activeProducts.stream()
                .collect(Collectors.groupingBy(Product::getCategory, Collectors.counting()));
        stats.put("categoryBreakdown", categoryStats);

        // Price statistics
        DoubleSummaryStatistics priceStats = activeProducts.stream()
                .mapToDouble(Product::getPrice)
                .summaryStatistics();
        stats.put("averagePrice", priceStats.getAverage());
        stats.put("minPrice", priceStats.getMin());
        stats.put("maxPrice", priceStats.getMax());

        // Rating statistics
        DoubleSummaryStatistics ratingStats = activeProducts.stream()
                .filter(p -> p.getReviewCount() > 0)
                .mapToDouble(Product::getAverageRating)
                .summaryStatistics();
        stats.put("averageRating", ratingStats.getAverage());
        stats.put("productsWithReviews", ratingStats.getCount());

        return stats;
    }

    private Comparator<Product> getComparator(String sortBy, String sortOrder) {
        Comparator<Product> comparator;

        switch (sortBy.toLowerCase()) {
            case "price":
                comparator = Comparator.comparing(Product::getPrice);
                break;
            case "rating":
                comparator = Comparator.comparing(Product::getAverageRating);
                break;
            case "createdat":
                comparator = Comparator.comparing(Product::getCreatedAt);
                break;
            case "reviewcount":
                comparator = Comparator.comparing(Product::getReviewCount);
                break;
            case "name":
            default:
                comparator = Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER);
                break;
        }

        if ("desc".equalsIgnoreCase(sortOrder)) {
            comparator = comparator.reversed();
        }

        return comparator;
    }

    public void updateProductStock(String productId, int newStock) {
        Product product = getProductById(productId);
        product.setStockQuantity(newStock);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
    }

    public List<Product> getLowStockProducts(int threshold) {
        return productRepository.findByActiveTrue()
                .stream()
                .filter(p -> p.getStockQuantity() <= threshold)
                .toList();
    }

    public List<Product> getTopRatedProducts(int limit) {
        return productRepository.findByActiveTrue()
                .stream()
                .filter(p -> p.getReviewCount() > 0)
                .sorted(Comparator.comparing(Product::getAverageRating).reversed())
                .limit(limit)
                .toList();
    }

    public Map<String, Object> getProductWithSellerById(String id) {
        Product product = getProductById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("product", product);
        User seller = null;
        if (product.getSellerId() != null) {
            seller = userRepository.findById(product.getSellerId()).orElse(null);
        }
        if (seller != null) {
            Map<String, Object> sellerInfo = new HashMap<>();
            sellerInfo.put("id", seller.getId());
            sellerInfo.put("name", seller.getFirstName() + " " + seller.getLastName());
            sellerInfo.put("avatar", seller.getAvatar());
            sellerInfo.put("rating", 0); // TODO: Tính rating thực tế nếu có
            sellerInfo.put("totalProducts", seller.getProductsSold() != null ? seller.getProductsSold().size() : 0);
            sellerInfo.put("completedOrders", 0); // TODO: Tính số đơn hoàn thành nếu có
            result.put("seller", sellerInfo);
        } else {
            result.put("seller", null);
        }
        return result;
    }

    public ProductRepository getProductRepository() {
        return productRepository;
    }
}