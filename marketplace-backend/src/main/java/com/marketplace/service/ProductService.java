package com.marketplace.service;

import com.marketplace.dto.ProductRequest;
import com.marketplace.model.Product;
import com.marketplace.repository.ProductRepository;
import com.marketplace.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findByActiveTrue();
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
        return productRepository.findBySellerIdAndActiveTrue(sellerId);
    }

    public Product createProduct(ProductRequest productRequest, String sellerId) {
        Product product = new Product();
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setImages(productRequest.getImages());
        product.setCategory(productRequest.getCategory());
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
}