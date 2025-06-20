package com.marketplace.service;

import com.marketplace.model.LoyaltyAccount;
import com.marketplace.model.PointTransaction;
import com.marketplace.model.Order;
import com.marketplace.repository.LoyaltyAccountRepository;
import com.marketplace.repository.PointTransactionRepository;
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
public class LoyaltyService {

    @Autowired
    private LoyaltyAccountRepository loyaltyAccountRepository;

    @Autowired
    private PointTransactionRepository pointTransactionRepository;

    // Points earning rates
    private static final int POINTS_PER_VND = 1; // 1 point per 1000 VND
    private static final int REVIEW_POINTS = 50;
    private static final int SIGNUP_BONUS = 100;
    private static final int REFERRAL_BONUS = 200;

    // Tier thresholds
    private static final Map<String, Integer> TIER_THRESHOLDS = Map.of(
            "BRONZE", 0,
            "SILVER", 1000,
            "GOLD", 5000,
            "PLATINUM", 15000,
            "DIAMOND", 50000
    );

    /**
     * Tạo loyalty account cho user mới
     */
    public LoyaltyAccount createLoyaltyAccount(String userId) {
        Optional<LoyaltyAccount> existing = loyaltyAccountRepository.findByUserId(userId);
        if (existing.isPresent()) {
            return existing.get();
        }

        LoyaltyAccount account = new LoyaltyAccount(userId);
        LoyaltyAccount savedAccount = loyaltyAccountRepository.save(account);

        // Signup bonus
        awardPoints(userId, SIGNUP_BONUS, "EARN", "SIGNUP_BONUS",
                "Chào mừng bạn đến với Cosmic Marketplace!", null);

        return savedAccount;
    }

    /**
     * Lấy loyalty account của user
     */
    public LoyaltyAccount getLoyaltyAccount(String userId) {
        return loyaltyAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Loyalty account not found"));
    }

    /**
     * Tính điểm từ đơn hàng
     */
    public void awardPointsFromOrder(Order order) {
        if (!"DELIVERED".equals(order.getStatus())) {
            return; // Only award points for completed orders
        }

        int pointsToAward = (int) (order.getTotalAmount() / 1000) * POINTS_PER_VND;

        awardPoints(
                order.getUserId(),
                pointsToAward,
                "EARN",
                "PURCHASE",
                "Điểm từ đơn hàng #" + order.getId(),
                order.getId()
        );
    }

    /**
     * Tặng điểm cho review
     */
    public void awardPointsForReview(String userId, String productId) {
        // Check if user already got points for this product review
        List<PointTransaction> existingReviewPoints = pointTransactionRepository
                .findByUserIdAndReasonAndRelatedId(userId, "REVIEW", productId);

        if (existingReviewPoints.isEmpty()) {
            awardPoints(
                    userId,
                    REVIEW_POINTS,
                    "EARN",
                    "REVIEW",
                    "Điểm cho đánh giá sản phẩm",
                    productId
            );
        }
    }

    /**
     * Tặng điểm referral
     */
    public void awardReferralPoints(String referrerId, String newUserId) {
        awardPoints(
                referrerId,
                REFERRAL_BONUS,
                "EARN",
                "REFERRAL",
                "Điểm giới thiệu bạn bè",
                newUserId
        );
    }

    /**
     * Core method để tặng điểm
     */
    public void awardPoints(String userId, int points, String type, String reason,
                            String description, String relatedId) {
        LoyaltyAccount account = loyaltyAccountRepository.findByUserId(userId)
                .orElseGet(() -> createLoyaltyAccount(userId));

        // Create transaction
        PointTransaction transaction = new PointTransaction(userId, points, type, reason);
        transaction.setDescription(description);
        transaction.setRelatedId(relatedId);

        if ("EARN".equals(type)) {
            // Set expiration (1 year from now)
            transaction.setExpiresAt(LocalDateTime.now().plusYears(1));
        }

        pointTransactionRepository.save(transaction);

        // Update account
        if ("EARN".equals(type)) {
            account.setTotalPoints(account.getTotalPoints() + points);
            account.setAvailablePoints(account.getAvailablePoints() + points);
        } else if ("SPEND".equals(type)) {
            if (account.getAvailablePoints() < points) {
                throw new BadRequestException("Không đủ điểm để sử dụng");
            }
            account.setAvailablePoints(account.getAvailablePoints() - points);
            account.setUsedPoints(account.getUsedPoints() + points);
        }

        account.setUpdatedAt(LocalDateTime.now());

        // Update tier
        updateUserTier(account);

        loyaltyAccountRepository.save(account);
    }

    /**
     * Sử dụng điểm
     */
    public void spendPoints(String userId, int points, String reason, String description, String relatedId) {
        awardPoints(userId, points, "SPEND", reason, description, relatedId);
    }

    /**
     * Đổi điểm lấy voucher
     */
    public Map<String, Object> redeemVoucher(String userId, String voucherType, int pointsCost) {
        LoyaltyAccount account = getLoyaltyAccount(userId);

        if (account.getAvailablePoints() < pointsCost) {
            throw new BadRequestException("Không đủ điểm để đổi voucher");
        }

        // Spend points
        spendPoints(userId, pointsCost, "REDEEM_VOUCHER",
                "Đổi voucher " + voucherType, null);

        // Generate voucher code
        String voucherCode = generateVoucherCode();

        Map<String, Object> voucher = new HashMap<>();
        voucher.put("code", voucherCode);
        voucher.put("type", voucherType);
        voucher.put("pointsCost", pointsCost);
        voucher.put("expiresAt", LocalDateTime.now().plusDays(30));

        return voucher;
    }

    /**
     * Cập nhật tier của user
     */
    private void updateUserTier(LoyaltyAccount account) {
        String currentTier = account.getTier();
        String newTier = calculateTier(account.getTotalPoints());

        if (!currentTier.equals(newTier)) {
            account.setTier(newTier);
            account.setTierUpdatedAt(LocalDateTime.now());

            // Award tier upgrade bonus
            if (getTierLevel(newTier) > getTierLevel(currentTier)) {
                int bonusPoints = getTierUpgradeBonus(newTier);
                PointTransaction bonus = new PointTransaction(
                        account.getUserId(), bonusPoints, "EARN", "TIER_UPGRADE");
                bonus.setDescription("Thưởng nâng hạng " + newTier);
                pointTransactionRepository.save(bonus);

                account.setTotalPoints(account.getTotalPoints() + bonusPoints);
                account.setAvailablePoints(account.getAvailablePoints() + bonusPoints);
            }
        }
    }

    /**
     * Tính tier dựa trên total points
     */
    private String calculateTier(int totalPoints) {
        if (totalPoints >= TIER_THRESHOLDS.get("DIAMOND")) return "DIAMOND";
        if (totalPoints >= TIER_THRESHOLDS.get("PLATINUM")) return "PLATINUM";
        if (totalPoints >= TIER_THRESHOLDS.get("GOLD")) return "GOLD";
        if (totalPoints >= TIER_THRESHOLDS.get("SILVER")) return "SILVER";
        return "BRONZE";
    }

    private int getTierLevel(String tier) {
        return switch (tier) {
            case "BRONZE" -> 1;
            case "SILVER" -> 2;
            case "GOLD" -> 3;
            case "PLATINUM" -> 4;
            case "DIAMOND" -> 5;
            default -> 0;
        };
    }

    private int getTierUpgradeBonus(String tier) {
        return switch (tier) {
            case "SILVER" -> 200;
            case "GOLD" -> 500;
            case "PLATINUM" -> 1000;
            case "DIAMOND" -> 2000;
            default -> 0;
        };
    }

    /**
     * Lấy lịch sử giao dịch điểm
     */
    public List<PointTransaction> getPointHistory(String userId) {
        return pointTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Thống kê loyalty
     */
    public Map<String, Object> getLoyaltyStats(String userId) {
        LoyaltyAccount account = getLoyaltyAccount(userId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("account", account);
        stats.put("tierProgress", getTierProgress(account));
        stats.put("availableRewards", getAvailableRewards());
        stats.put("pointsThisMonth", getPointsThisMonth(userId));

        return stats;
    }

    private Map<String, Object> getTierProgress(LoyaltyAccount account) {
        String currentTier = account.getTier();
        String nextTier = getNextTier(currentTier);

        Map<String, Object> progress = new HashMap<>();
        progress.put("currentTier", currentTier);
        progress.put("nextTier", nextTier);
        progress.put("currentPoints", account.getTotalPoints());

        if (nextTier != null) {
            int nextTierThreshold = TIER_THRESHOLDS.get(nextTier);
            int currentTierThreshold = TIER_THRESHOLDS.get(currentTier);
            int pointsNeeded = nextTierThreshold - account.getTotalPoints();
            int progressPercentage = (int) (((double) (account.getTotalPoints() - currentTierThreshold) /
                    (nextTierThreshold - currentTierThreshold)) * 100);

            progress.put("pointsNeeded", pointsNeeded);
            progress.put("progressPercentage", Math.max(0, Math.min(100, progressPercentage)));
        }

        return progress;
    }

    private String getNextTier(String currentTier) {
        return switch (currentTier) {
            case "BRONZE" -> "SILVER";
            case "SILVER" -> "GOLD";
            case "GOLD" -> "PLATINUM";
            case "PLATINUM" -> "DIAMOND";
            default -> null;
        };
    }

    private List<Map<String, Object>> getAvailableRewards() {
        return List.of(
                Map.of("name", "Voucher giảm 50K", "points", 500, "type", "DISCOUNT_50K"),
                Map.of("name", "Voucher giảm 100K", "points", 1000, "type", "DISCOUNT_100K"),
                Map.of("name", "Freeship toàn quốc", "points", 300, "type", "FREE_SHIPPING"),
                Map.of("name", "Voucher giảm 10%", "points", 800, "type", "DISCOUNT_10_PERCENT")
        );
    }

    private int getPointsThisMonth(String userId) {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        return pointTransactionRepository.findByUserIdAndTypeAndCreatedAtAfter(userId, "EARN", startOfMonth)
                .stream()
                .mapToInt(PointTransaction::getPoints)
                .sum();
    }

    private String generateVoucherCode() {
        return "COSMIC" + System.currentTimeMillis() % 1000000;
    }

    /**
     * Admin: Điều chỉnh điểm
     */
    public void adminAdjustPoints(String userId, int points, String reason) {
        String type = points > 0 ? "EARN" : "SPEND";
        awardPoints(userId, Math.abs(points), type, "ADMIN_ADJUSTMENT", reason, null);
    }

    /**
     * Xử lý điểm hết hạn
     */
    public void processExpiredPoints() {
        LocalDateTime now = LocalDateTime.now();
        List<PointTransaction> expiredTransactions = pointTransactionRepository
                .findByTypeAndExpiresAtBefore("EARN", now);

        for (PointTransaction transaction : expiredTransactions) {
            // Create expiration transaction
            PointTransaction expiration = new PointTransaction(
                    transaction.getUserId(),
                    transaction.getPoints(),
                    "EXPIRE",
                    "POINT_EXPIRATION"
            );
            expiration.setDescription("Điểm hết hạn");
            expiration.setRelatedId(transaction.getId());
            pointTransactionRepository.save(expiration);

            // Update user's available points
            LoyaltyAccount account = getLoyaltyAccount(transaction.getUserId());
            account.setAvailablePoints(Math.max(0, account.getAvailablePoints() - transaction.getPoints()));
            loyaltyAccountRepository.save(account);

            // Mark original transaction as processed
            transaction.setExpiresAt(null);
            pointTransactionRepository.save(transaction);
        }
    }
}