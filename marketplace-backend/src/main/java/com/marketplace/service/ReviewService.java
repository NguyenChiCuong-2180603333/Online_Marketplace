package com.marketplace.service;

import com.marketplace.dto.ReviewRequest;
import com.marketplace.model.Review;
import com.marketplace.model.Product;
import com.marketplace.model.User;
import com.marketplace.model.Order;
import com.marketplace.repository.ReviewRepository;
import com.marketplace.exception.ResourceNotFoundException;
import com.marketplace.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private MarketplaceEventListener eventListener;

    public List<Review> getReviewsByProductId(String productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    public List<Review> getReviewsByUserId(String userId) {
        return reviewRepository.findByUserId(userId);
    }

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    public Review createReview(ReviewRequest reviewRequest, String userId) {
        // Kiểm tra sản phẩm tồn tại
        Product product = productService.getProductById(reviewRequest.getProductId());
        User user = userService.getUserById(userId);

        // Kiểm tra user đã review sản phẩm này chưa
        Optional<Review> existingReview = reviewRepository.findByProductIdAndUserId(
                reviewRequest.getProductId(), userId);
        if (existingReview.isPresent()) {
            throw new BadRequestException("Bạn đã đánh giá sản phẩm này rồi");
        }

        // Kiểm tra user đã mua sản phẩm chưa (optional verification)
        boolean hasOrderedProduct = hasUserOrderedProduct(userId, reviewRequest.getProductId());

        Review review = new Review();
        review.setProductId(reviewRequest.getProductId());
        review.setUserId(userId);
        review.setUserName(user.getFirstName() + " " + user.getLastName());
        review.setUserAvatar(user.getAvatar());
        review.setRating(reviewRequest.getRating());
        review.setComment(reviewRequest.getComment());
        review.setVerified(hasOrderedProduct);
        review.setOrderId(reviewRequest.getOrderId());
        review.setCreatedAt(LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);

        // Cập nhật rating trung bình của sản phẩm
        updateProductRating(reviewRequest.getProductId());

        // 🆕 NEW: Trigger review events (loyalty points + seller notification)
        try {
            // Get seller info for notification
            User seller = userService.getUserById(product.getSellerId());

            eventListener.handleNewReview(
                    userId,
                    reviewRequest.getProductId(),
                    seller.getEmail(),
                    seller.getFirstName() + " " + seller.getLastName(),
                    product.getName(),
                    reviewRequest.getRating()
            );
        } catch (Exception e) {
            // Log but don't fail review creation
            System.err.println("Failed to process review events: " + e.getMessage());
        }

        return savedReview;
    }

    public Review updateReview(String reviewId, ReviewRequest reviewRequest, String userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        if (!review.getUserId().equals(userId)) {
            throw new BadRequestException("Bạn chỉ có thể sửa đánh giá của mình");
        }

        review.setRating(reviewRequest.getRating());
        review.setComment(reviewRequest.getComment());
        review.setUpdatedAt(LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);

        // Cập nhật lại rating trung bình
        updateProductRating(review.getProductId());

        return savedReview;
    }

    public void deleteReview(String reviewId, String userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        if (!review.getUserId().equals(userId)) {
            throw new BadRequestException("Bạn chỉ có thể xóa đánh giá của mình");
        }

        reviewRepository.delete(review);
        updateProductRating(review.getProductId());
    }

    public void adminDeleteReview(String reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        reviewRepository.delete(review);
        updateProductRating(review.getProductId());
    }

    public boolean hasUserReviewedProduct(String userId, String productId) {
        return reviewRepository.findByProductIdAndUserId(productId, userId).isPresent();
    }

    public Review getUserReviewForProduct(String userId, String productId) {
        return reviewRepository.findByProductIdAndUserId(productId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
    }

    public Map<String, Object> getReviewStats(String productId) {
        List<Review> reviews = reviewRepository.findByProductId(productId);
        Map<String, Object> stats = new HashMap<>();

        if (reviews.isEmpty()) {
            stats.put("totalReviews", 0);
            stats.put("averageRating", 0.0);
            stats.put("ratingDistribution", new int[]{0, 0, 0, 0, 0});
            return stats;
        }

        // Tính rating trung bình
        double averageRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        // Phân bố rating (1-5 sao)
        int[] ratingDistribution = new int[5];
        for (Review review : reviews) {
            ratingDistribution[review.getRating() - 1]++;
        }

        stats.put("totalReviews", reviews.size());
        stats.put("averageRating", Math.round(averageRating * 10.0) / 10.0);
        stats.put("ratingDistribution", ratingDistribution);
        stats.put("verifiedReviews", reviews.stream().mapToInt(r -> r.isVerified() ? 1 : 0).sum());

        return stats;
    }

    private void updateProductRating(String productId) {
        List<Review> reviews = reviewRepository.findByProductId(productId);

        if (reviews.isEmpty()) {
            productService.updateProductRating(productId, 0.0, 0);
            return;
        }

        double averageRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        productService.updateProductRating(productId, averageRating, reviews.size());
    }

    private boolean hasUserOrderedProduct(String userId, String productId) {
        try {
            List<Order> userOrders = orderService.getOrdersByUserId(userId);
            return userOrders.stream()
                    .filter(order -> "DELIVERED".equals(order.getStatus()))
                    .flatMap(order -> order.getItems().stream())
                    .anyMatch(item -> item.getProductId().equals(productId));
        } catch (Exception e) {
            // Nếu không thể verify, trả về false
            return false;
        }
    }
}