package com.marketplace.controller;

import com.marketplace.dto.ReviewRequest;
import com.marketplace.model.Review;
import com.marketplace.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    // Lấy tất cả reviews của một sản phẩm
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Review>> getProductReviews(@PathVariable String productId) {
        List<Review> reviews = reviewService.getReviewsByProductId(productId);
        return ResponseEntity.ok(reviews);
    }

    // Tạo review mới
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Review> createReview(@Valid @RequestBody ReviewRequest reviewRequest) {
        String userId = getCurrentUserId();
        Review review = reviewService.createReview(reviewRequest, userId);
        return ResponseEntity.ok(review);
    }

    // Cập nhật review
    @PutMapping("/{reviewId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Review> updateReview(
            @PathVariable String reviewId,
            @Valid @RequestBody ReviewRequest reviewRequest) {
        String userId = getCurrentUserId();
        Review review = reviewService.updateReview(reviewId, reviewRequest, userId);
        return ResponseEntity.ok(review);
    }

    // Xóa review
    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteReview(@PathVariable String reviewId) {
        String userId = getCurrentUserId();
        reviewService.deleteReview(reviewId, userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Đánh giá đã được xóa thành công");
        return ResponseEntity.ok(response);
    }

    // Lấy reviews của user
    @GetMapping("/my-reviews")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<Review>> getMyReviews() {
        String userId = getCurrentUserId();
        List<Review> reviews = reviewService.getReviewsByUserId(userId);
        return ResponseEntity.ok(reviews);
    }

    // Kiểm tra user đã review sản phẩm chưa
    @GetMapping("/check/{productId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> checkUserReview(@PathVariable String productId) {
        String userId = getCurrentUserId();
        boolean hasReviewed = reviewService.hasUserReviewedProduct(userId, productId);

        Map<String, Object> response = new HashMap<>();
        response.put("hasReviewed", hasReviewed);

        if (hasReviewed) {
            Review existingReview = reviewService.getUserReviewForProduct(userId, productId);
            response.put("review", existingReview);
        }

        return ResponseEntity.ok(response);
    }

    // Lấy thống kê reviews của sản phẩm
    @GetMapping("/stats/{productId}")
    public ResponseEntity<Map<String, Object>> getReviewStats(@PathVariable String productId) {
        Map<String, Object> stats = reviewService.getReviewStats(productId);
        return ResponseEntity.ok(stats);
    }

    // Admin: Lấy tất cả reviews
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Review>> getAllReviews() {
        List<Review> reviews = reviewService.getAllReviews();
        return ResponseEntity.ok(reviews);
    }

    // Admin: Xóa review
    @DeleteMapping("/admin/{reviewId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> adminDeleteReview(@PathVariable String reviewId) {
        reviewService.adminDeleteReview(reviewId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Đánh giá đã được xóa bởi admin");
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
                        com.marketplace.security.JwtTokenProvider jwtProvider = context.getBean(com.marketplace.security.JwtTokenProvider.class);
                        return jwtProvider.getUserIdFromToken(token);
                    }
                } catch (Exception e) {
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
                return bearerToken.substring(7);
            }
        } catch (Exception e) {
        }
        return null;
    }
}