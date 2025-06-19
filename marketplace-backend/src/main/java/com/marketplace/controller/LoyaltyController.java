package com.marketplace.controller;

import com.marketplace.model.*;
import com.marketplace.service.LoyaltyPointsService;
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
@RequestMapping("/api/loyalty")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class LoyaltyController {

    @Autowired
    private LoyaltyPointsService loyaltyPointsService;

    // Lấy thông tin loyalty account
    @GetMapping("/account")
    public ResponseEntity<LoyaltyAccount> getLoyaltyAccount() {
        String userId = getCurrentUserId();
        LoyaltyAccount account = loyaltyPointsService.getLoyaltyAccount(userId);
        return ResponseEntity.ok(account);
    }

    // Lấy thống kê loyalty
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getLoyaltyStats() {
        String userId = getCurrentUserId();
        Map<String, Object> stats = loyaltyPointsService.getLoyaltyStats(userId);
        return ResponseEntity.ok(stats);
    }

    // Lấy lịch sử giao dịch điểm
    @GetMapping("/transactions")
    public ResponseEntity<List<PointsTransaction>> getPointsHistory(
            @RequestParam(defaultValue = "20") int limit) {
        String userId = getCurrentUserId();
        List<PointsTransaction> transactions = loyaltyPointsService.getPointsHistory(userId, limit);
        return ResponseEntity.ok(transactions);
    }

    // Lấy danh sách rewards
    @GetMapping("/rewards")
    public ResponseEntity<List<PointsReward>> getAvailableRewards() {
        String userId = getCurrentUserId();
        List<PointsReward> rewards = loyaltyPointsService.getAvailableRewards(userId);
        return ResponseEntity.ok(rewards);
    }

    // Đổi điểm lấy reward
    @PostMapping("/redeem/{rewardId}")
    public ResponseEntity<RewardRedemption> redeemReward(@PathVariable String rewardId) {
        String userId = getCurrentUserId();
        RewardRedemption redemption = loyaltyPointsService.redeemReward(userId, rewardId);
        return ResponseEntity.ok(redemption);
    }

    // Lấy lịch sử đổi thưởng
    @GetMapping("/redemptions")
    public ResponseEntity<List<RewardRedemption>> getRedemptionHistory() {
        String userId = getCurrentUserId();
        List<RewardRedemption> redemptions = loyaltyPointsService.getRedemptionHistory(userId);
        return ResponseEntity.ok(redemptions);
    }

    // Sử dụng mã đổi thưởng (trong quá trình thanh toán)
    @PostMapping("/use-code")
    public ResponseEntity<?> useRedemptionCode(@RequestBody Map<String, String> request) {
        String redemptionCode = request.get("redemptionCode");
        String orderId = request.get("orderId");

        loyaltyPointsService.useRedemptionCode(redemptionCode, orderId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Mã đổi thưởng đã được áp dụng thành công");
        return ResponseEntity.ok(response);
    }

    // Thêm điểm thủ công (admin only hoặc special cases)
    @PostMapping("/add-points")
    public ResponseEntity<PointsTransaction> addPoints(@RequestBody Map<String, Object> request) {
        String userId = getCurrentUserId();
        int points = (Integer) request.get("points");
        String reason = (String) request.get("reason");
        String description = (String) request.get("description");

        PointsTransaction transaction = loyaltyPointsService.awardPoints(userId, points, reason, description);
        return ResponseEntity.ok(transaction);
    }

    // Referral - tặng điểm cho người giới thiệu
    @PostMapping("/referral")
    public ResponseEntity<?> processReferral(@RequestBody Map<String, String> request) {
        String userId = getCurrentUserId();
        String newUserId = request.get("newUserId");

        loyaltyPointsService.awardReferralPoints(userId, newUserId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Điểm giới thiệu đã được tặng thành công");
        return ResponseEntity.ok(response);
    }

    // Tier information
    @GetMapping("/tiers")
    public ResponseEntity<Map<String, Object>> getTierInformation() {
        Map<String, Object> tierInfo = new HashMap<>();

        // Tier requirements
        Map<String, Integer> requirements = new HashMap<>();
        requirements.put("BRONZE", 0);
        requirements.put("SILVER", 1000);
        requirements.put("GOLD", 5000);
        requirements.put("PLATINUM", 15000);
        requirements.put("DIAMOND", 50000);

        tierInfo.put("requirements", requirements);

        // Tier benefits
        Map<String, Map<String, Object>> benefits = new HashMap<>();

        Map<String, Object> bronzeBenefits = new HashMap<>();
        bronzeBenefits.put("pointsMultiplier", 1.0);
        bronzeBenefits.put("freeShippingThreshold", 500000.0);
        bronzeBenefits.put("birthdayBonus", 500);
        benefits.put("BRONZE", bronzeBenefits);

        Map<String, Object> silverBenefits = new HashMap<>();
        silverBenefits.put("pointsMultiplier", 1.2);
        silverBenefits.put("freeShippingThreshold", 300000.0);
        silverBenefits.put("birthdayBonus", 750);
        silverBenefits.put("exclusiveDeals", true);
        benefits.put("SILVER", silverBenefits);

        Map<String, Object> goldBenefits = new HashMap<>();
        goldBenefits.put("pointsMultiplier", 1.5);
        goldBenefits.put("freeShippingThreshold", 200000.0);
        goldBenefits.put("birthdayBonus", 1000);
        goldBenefits.put("exclusiveDeals", true);
        goldBenefits.put("prioritySupport", true);
        benefits.put("GOLD", goldBenefits);

        Map<String, Object> platinumBenefits = new HashMap<>();
        platinumBenefits.put("pointsMultiplier", 2.0);
        platinumBenefits.put("freeShippingThreshold", 0.0);
        platinumBenefits.put("birthdayBonus", 1500);
        platinumBenefits.put("exclusiveDeals", true);
        platinumBenefits.put("prioritySupport", true);
        platinumBenefits.put("earlyAccess", true);
        benefits.put("PLATINUM", platinumBenefits);

        Map<String, Object> diamondBenefits = new HashMap<>();
        diamondBenefits.put("pointsMultiplier", 2.5);
        diamondBenefits.put("freeShippingThreshold", 0.0);
        diamondBenefits.put("birthdayBonus", 2000);
        diamondBenefits.put("exclusiveDeals", true);
        diamondBenefits.put("prioritySupport", true);
        diamondBenefits.put("earlyAccess", true);
        diamondBenefits.put("personalShopper", true);
        benefits.put("DIAMOND", diamondBenefits);

        tierInfo.put("benefits", benefits);

        return ResponseEntity.ok(tierInfo);
    }

    // Leaderboard
    @GetMapping("/leaderboard")
    public ResponseEntity<Map<String, Object>> getLeaderboard(
            @RequestParam(defaultValue = "10") int limit) {

        // This would typically call a service method
        Map<String, Object> leaderboard = new HashMap<>();
        leaderboard.put("message", "Leaderboard feature coming soon");
        leaderboard.put("limit", limit);

        return ResponseEntity.ok(leaderboard);
    }

    // =========================
    // ADMIN ENDPOINTS
    // =========================

    // Tạo reward mới
    @PostMapping("/admin/rewards")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PointsReward> createReward(@RequestBody PointsReward reward) {
        PointsReward createdReward = loyaltyPointsService.createReward(reward);
        return ResponseEntity.ok(createdReward);
    }

    // Cập nhật reward
    @PutMapping("/admin/rewards/{rewardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateReward(@PathVariable String rewardId, @RequestBody PointsReward reward) {
        loyaltyPointsService.updateReward(rewardId, reward);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Reward updated successfully");
        return ResponseEntity.ok(response);
    }

    // Xóa reward
    @DeleteMapping("/admin/rewards/{rewardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteReward(@PathVariable String rewardId) {
        loyaltyPointsService.deleteReward(rewardId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Reward deleted successfully");
        return ResponseEntity.ok(response);
    }

    // Tặng điểm hàng loạt
    @PostMapping("/admin/bulk-award")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> bulkAwardPoints(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<String> userIds = (List<String>) request.get("userIds");
        int points = (Integer) request.get("points");
        String reason = (String) request.get("reason");
        String description = (String) request.get("description");

        loyaltyPointsService.awardBulkPoints(userIds, points, reason, description);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Points awarded to " + userIds.size() + " users successfully");
        return ResponseEntity.ok(response);
    }

    // Xử lý hết hạn điểm
    @PostMapping("/admin/expire-points")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> expireOldPoints() {
        loyaltyPointsService.expireOldPoints();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Expired points processed successfully");
        return ResponseEntity.ok(response);
    }

    // Thống kê admin
    @GetMapping("/admin/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getLoyaltyAnalytics() {
        Map<String, Object> analytics = new HashMap<>();

        // Mock data - trong thực tế sẽ tính từ database
        analytics.put("totalActiveUsers", 1250);
        analytics.put("totalPointsIssued", 125000);
        analytics.put("totalPointsRedeemed", 45000);
        analytics.put("averagePointsPerUser", 100);

        Map<String, Integer> tierDistribution = new HashMap<>();
        tierDistribution.put("BRONZE", 800);
        tierDistribution.put("SILVER", 300);
        tierDistribution.put("GOLD", 100);
        tierDistribution.put("PLATINUM", 40);
        tierDistribution.put("DIAMOND", 10);
        analytics.put("tierDistribution", tierDistribution);

        Map<String, Integer> popularRewards = new HashMap<>();
        popularRewards.put("10% Discount", 150);
        popularRewards.put("Free Shipping", 200);
        popularRewards.put("Gift Card 100k", 75);
        analytics.put("popularRewards", popularRewards);

        return ResponseEntity.ok(analytics);
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
                    // Fall back to mock for now
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
            // Ignore
        }
        return null;
    }
}