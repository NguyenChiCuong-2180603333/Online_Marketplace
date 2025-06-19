package com.marketplace.controller;

import com.marketplace.model.Product;
import com.marketplace.model.Category;
import com.marketplace.service.ProductService;
import com.marketplace.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class SearchController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    // Tìm kiếm tổng hợp
    @GetMapping
    public ResponseEntity<Map<String, Object>> globalSearch(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int limit) {

        Map<String, Object> results = new HashMap<>();

        // Tìm sản phẩm
        List<Product> products = productService.searchProducts(q);
        if (products.size() > limit) {
            products = products.subList(0, limit);
        }

        // Tìm danh mục
        List<Category> categories = categoryService.searchCategories(q);
        if (categories.size() > limit) {
            categories = categories.subList(0, limit);
        }

        results.put("products", products);
        results.put("categories", categories);
        results.put("totalProducts", products.size());
        results.put("totalCategories", categories.size());
        results.put("query", q);

        return ResponseEntity.ok(results);
    }

    // Tìm kiếm sản phẩm nâng cao
    @GetMapping("/products")
    public ResponseEntity<Map<String, Object>> searchProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<Product> products = productService.searchProductsAdvanced(
                q, category, minPrice, maxPrice, minRating, sortBy, sortOrder, page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("products", products);
        response.put("page", page);
        response.put("size", size);
        response.put("totalElements", products.size());

        // Thêm thông tin filter
        Map<String, Object> filters = new HashMap<>();
        filters.put("query", q);
        filters.put("category", category);
        filters.put("minPrice", minPrice);
        filters.put("maxPrice", maxPrice);
        filters.put("minRating", minRating);
        filters.put("sortBy", sortBy);
        filters.put("sortOrder", sortOrder);
        response.put("filters", filters);

        return ResponseEntity.ok(response);
    }

    // Tìm kiếm gợi ý (autocomplete)
    @GetMapping("/suggestions")
    public ResponseEntity<Map<String, Object>> getSearchSuggestions(@RequestParam String q) {
        Map<String, Object> suggestions = new HashMap<>();

        if (q.length() >= 2) {
            // Gợi ý sản phẩm
            List<Product> productSuggestions = productService.searchProducts(q);
            List<String> productNames = productSuggestions.stream()
                    .map(Product::getName)
                    .distinct()
                    .limit(5)
                    .toList();

            // Gợi ý danh mục
            List<Category> categorySuggestions = categoryService.searchCategories(q);
            List<String> categoryNames = categorySuggestions.stream()
                    .map(Category::getName)
                    .distinct()
                    .limit(3)
                    .toList();

            suggestions.put("products", productNames);
            suggestions.put("categories", categoryNames);
        } else {
            suggestions.put("products", List.of());
            suggestions.put("categories", List.of());
        }

        return ResponseEntity.ok(suggestions);
    }

    // Lấy bộ lọc có sẵn
    @GetMapping("/filters")
    public ResponseEntity<Map<String, Object>> getAvailableFilters() {
        Map<String, Object> filters = new HashMap<>();

        // Danh mục
        List<Category> categories = categoryService.getAllActiveCategories();
        filters.put("categories", categories);

        // Khoảng giá
        Map<String, Object> priceRanges = new HashMap<>();
        priceRanges.put("ranges", List.of(
                Map.of("label", "Dưới 100,000đ", "min", 0, "max", 100000),
                Map.of("label", "100,000đ - 500,000đ", "min", 100000, "max", 500000),
                Map.of("label", "500,000đ - 1,000,000đ", "min", 500000, "max", 1000000),
                Map.of("label", "1,000,000đ - 5,000,000đ", "min", 1000000, "max", 5000000),
                Map.of("label", "Trên 5,000,000đ", "min", 5000000, "max", null)
        ));
        filters.put("priceRanges", priceRanges);

        // Rating
        filters.put("ratings", List.of(
                Map.of("label", "5 sao", "value", 5),
                Map.of("label", "4 sao trở lên", "value", 4),
                Map.of("label", "3 sao trở lên", "value", 3),
                Map.of("label", "2 sao trở lên", "value", 2),
                Map.of("label", "1 sao trở lên", "value", 1)
        ));

        // Sắp xếp
        filters.put("sortOptions", List.of(
                Map.of("label", "Tên A-Z", "value", "name", "order", "asc"),
                Map.of("label", "Tên Z-A", "value", "name", "order", "desc"),
                Map.of("label", "Giá thấp đến cao", "value", "price", "order", "asc"),
                Map.of("label", "Giá cao đến thấp", "value", "price", "order", "desc"),
                Map.of("label", "Đánh giá cao nhất", "value", "rating", "order", "desc"),
                Map.of("label", "Mới nhất", "value", "createdAt", "order", "desc"),
                Map.of("label", "Bán chạy nhất", "value", "reviewCount", "order", "desc")
        ));

        return ResponseEntity.ok(filters);
    }

    // Tìm kiếm xu hướng
    @GetMapping("/trending")
    public ResponseEntity<Map<String, Object>> getTrendingSearches() {
        Map<String, Object> trending = new HashMap<>();

        // Mock data - trong thực tế sẽ lấy từ analytics
        trending.put("keywords", List.of(
                "smartphone", "laptop", "tai nghe", "đồng hồ", "giày sneaker",
                "túi xách", "máy ảnh", "tablet", "phụ kiện điện thoại", "quần áo"
        ));

        trending.put("categories", List.of(
                "Điện tử", "Thời trang", "Gia dụng", "Sách", "Thể thao"
        ));

        return ResponseEntity.ok(trending);
    }

    // Sản phẩm liên quan
    @GetMapping("/related/{productId}")
    public ResponseEntity<List<Product>> getRelatedProducts(
            @PathVariable String productId,
            @RequestParam(defaultValue = "5") int limit) {

        List<Product> relatedProducts = productService.getRelatedProducts(productId, limit);
        return ResponseEntity.ok(relatedProducts);
    }
}