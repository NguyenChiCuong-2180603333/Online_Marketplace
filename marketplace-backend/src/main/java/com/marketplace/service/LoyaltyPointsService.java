package com.marketplace.service;

import com.marketplace.model.*;
import com.marketplace.repository.*;
import com.marketplace.exception.ResourceNotFoundException;
import com.marketplace.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class LoyaltyPointsService {

    @Autowired
    private LoyaltyAccountRepository loyaltyAccountRepository;

    @Autowired
    private PointsTransactionRepository pointsTransactionRepository;

    @Autowired
    private PointsRewardRepository pointsRewardRepository;

    @Autowired
    private RewardRedemptionRepository rewardRedemptionRepository;

    @Autowired
    private NotificationService notificationService;

    // Tier thresholds
    private static final Map<String, Integer> TIER_THRESHOLDS = Map.of(
            "BRONZE", 0,
            "SILVER", 1000,
            "GOLD", 5000,
            "PLATINUM", 15000,
            "DIAMOND", 50000
    );

    // Points earning rules
    private static final int POINTS_PER_DOLLAR = 1; // 1 point per $1 spent
    private static final int SIGNUP_BONUS = 100;
    private static final int REVIEW_POINTS = 50;
    private static final int REFERRAL_POINTS = 200;
    private static final int BIRTHDAY_BONUS = 500;

    // Lấy hoặc tạo loyalty account
    public LoyaltyAccount getLoyaltyAccount(String userId) {
        return loyaltyAccountRepository.findByUserId(userId)
                .orElseGet(() -> createLoyaltyAccount(userId));
    }

    // Tạo loyalty account mới
    private LoyaltyAccount createLoyaltyAccount(String userId) {
        LoyaltyAccount account = new LoyaltyAccount(userId);
        LoyaltyAccount savedAccount = loyaltyAccountRepository.save(account);

        // Award signup bonus
        awardPoints(userId, SIGNUP_BONUS, "SIGNUP", "Chào mừng bạn đến với chương trình khách hàng thân thiết!");

        return savedAccount;
    }

    // Thêm điểm
    public PointsTransaction awardPoints(String userId, int points, String reason, String description) {
        LoyaltyAccount account = getLoyaltyAccount(userId);

        // Create transaction
        PointsTransaction transaction = new PointsTransaction(userId, points, "EARNED", reason);
        transaction.setDescription(description);

        // Set expiry date (points expire after 1 year)
        transaction.setExpiryDate(LocalDateTime.now().plusYears(1));

        PointsTransaction savedTransaction = pointsTransactionRepository.save(transaction);

        // Update account balance
        account.setPointsBalance(account.getPointsBalance() + points);
        account.setTotalEarnedPoints(account.getTotalEarnedPoints() + points);
        account.setLastEarnedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());

        // Check tier upgrade
        checkTierUpgrade(account);

        loyaltyAccountRepository.save(account);

        // Send notification
        notificationService.createNotification(new com.marketplace.dto.NotificationRequest(
                userId,
                "Bạn đã nhận được " + points + " điểm!",
                description,
                "SYSTEM_UPDATE"
        ));

        return savedTransaction;
    }

    // Trừ điểm
    public PointsTransaction spendPoints(String userId, int points, String reason, String description) {
        LoyaltyAccount account = getLoyaltyAccount(userId);

        if (account.getPointsBalance() < points) {
            throw new BadRequestException("Số điểm không đủ. Bạn có " + account.getPointsBalance() + " điểm.");
        }

        // Create transaction
        PointsTransaction transaction = new PointsTransaction(userId, -points, "SPENT", reason);
        transaction.setDescription(description);

        PointsTransaction savedTransaction = pointsTransactionRepository.save(transaction);

        // Update account balance
        account.setPointsBalance(account.getPointsBalance() - points);
        account.setTotalSpentPoints(account.getTotalSpentPoints() + points);
        account.setLastSpentAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());

        loyaltyAccountRepository.save(account);

        return savedTransaction;
    }

    // Award points cho purchase
    public void awardPurchasePoints(String userId, String orderId, double orderAmount) {
        int points = (int) Math.floor(orderAmount * POINTS_PER_DOLLAR);

        PointsTransaction transaction = awardPoints(userId, points, "PURCHASE",
                "Tích điểm từ đơn hàng #" + orderId);
        transaction.setRelatedOrderId(orderId);
        pointsTransactionRepository.save(transaction);
    }

    // Award points cho review
    public void awardReviewPoints(String userId, String reviewId, String productId) {
        PointsTransaction transaction = awardPoints(userId, REVIEW_POINTS, "REVIEW",
                "Cảm ơn bạn đã đánh giá sản phẩm!");
        transaction.setRelatedReviewId(reviewId);
        transaction.setRelatedProductId(productId);
        pointsTransactionRepository.save(transaction);
    }

    // Award referral points
    public void awardReferralPoints(String referrerId, String newUserId) {
        awardPoints(referrerId, REFERRAL_POINTS, "REFERRAL",
                "Cảm ơn bạn đã giới thiệu người bạn!");

        // New user also gets bonus
        awardPoints(newUserId, REFERRAL_POINTS / 2, "REFERRAL",
                "Chào mừng! Bạn nhận được điểm từ người giới thiệu.");
    }

    // Birthday bonus
    public void awardBirthdayBonus(String userId) {
        awardPoints(userId, BIRTHDAY_BONUS, "BIRTHDAY",
                "Chúc mừng sinh nhật! Đây là món quà từ chúng tôi 🎉");
    }

    // Lấy danh sách rewards
    public List<PointsReward> getAvailableRewards(String userId) {
        LoyaltyAccount account = getLoyaltyAccount(userId);
        String userTier = account.getTier();

        return pointsRewardRepository.findActiveRewards().stream()
                .filter(reward -> canUserAccessReward(userTier, reward.getRequiredTier()))
                .filter(reward -> reward.getStockQuantity() != 0) // Not out of stock
                .toList();
    }

    // Đổi điểm lấy reward
    public RewardRedemption redeemReward(String userId, String rewardId) {
        LoyaltyAccount account = getLoyaltyAccount(userId);
        PointsReward reward = pointsRewardRepository.findById(rewardId)
                .orElseThrow(() -> new ResourceNotFoundException("Reward not found"));

        // Validate conditions
        if (!reward.isActive()) {
            throw new BadRequestException("Phần thưởng này hiện không khả dụng");
        }

        if (reward.getValidUntil() != null && reward.getValidUntil().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Phần thưởng này đã hết hạn");
        }

        if (!canUserAccessReward(account.getTier(), reward.getRequiredTier())) {
            throw new BadRequestException("Bạn cần đạt tier " + reward.getRequiredTier() + " để đổi phần thưởng này");
        }

        if (account.getPointsBalance() < reward.getPointsCost()) {
            throw new BadRequestException("Số điểm không đủ. Cần " + reward.getPointsCost() + " điểm.");
        }

        if (reward.getStockQuantity() == 0) {
            throw new BadRequestException("Phần thưởng này đã hết");
        }

        // Spend points
        spendPoints(userId, reward.getPointsCost(), "REDEMPTION",
                "Đổi điểm lấy: " + reward.getName());

        // Create redemption
        RewardRedemption redemption = new RewardRedemption(userId, rewardId, reward.getPointsCost());
        redemption.setExpiryDate(LocalDateTime.now().plusDays(30)); // Redemption expires in 30 days

        RewardRedemption savedRedemption = rewardRedemptionRepository.save(redemption);

        // Update reward stock
        if (reward.getStockQuantity() > 0) {
            reward.setStockQuantity(reward.getStockQuantity() - 1);
        }
        reward.setRedeemedCount(reward.getRedeemedCount() + 1);
        pointsRewardRepository.save(reward);

        // Send notification
        notificationService.createNotification(new com.marketplace.dto.NotificationRequest(
                userId,
                "Đổi điểm thành công!",
                "Bạn đã đổi " + reward.getPointsCost() + " điểm lấy " + reward.getName() +
                        ". Mã sử dụng: " + redemption.getRedemptionCode(),
                "SYSTEM_UPDATE"
        ));

        return savedRedemption;
    }

    // Lấy lịch sử giao dịch điểm
    public List<PointsTransaction> getPointsHistory(String userId, int limit) {
        return pointsTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .limit(limit)
                .toList();
    }

    // Lấy lịch sử đổi thưởng
    public List<RewardRedemption> getRedemptionHistory(String userId) {
        return rewardRedemptionRepository.findByUserIdOrderByRedeemedAtDesc(userId);
    }

    // Check tier upgrade
    private void checkTierUpgrade(LoyaltyAccount account) {
        String currentTier = account.getTier();
        String newTier = calculateTier(account.getTotalEarnedPoints());

        if (!currentTier.equals(newTier)) {
            account.setTier(newTier);

            // Send tier upgrade notification
            notificationService.createNotification(new com.marketplace.dto.NotificationRequest(
                    account.getUserId(),
                    "Chúc mừng! Bạn đã lên tier " + newTier + "! 🎉",
                    "Bạn đã đạt tier " + newTier + " với nhiều ưu đãi hấp dẫn.",
                    "SYSTEM_UPDATE"
            ));
        }

        // Update tier progress
        String nextTier = getNextTier(newTier);
        if (nextTier != null) {
            int nextTierThreshold = TIER_THRESHOLDS.get(nextTier);
            account.setTierProgress(nextTierThreshold - account.getTotalEarnedPoints());
        } else {
            account.setTierProgress(0); // Max tier reached
        }
    }

    private String calculateTier(int totalPoints) {
        if (totalPoints >= TIER_THRESHOLDS.get("DIAMOND")) return "DIAMOND";
        if (totalPoints >= TIER_THRESHOLDS.get("PLATINUM")) return "PLATINUM";
        if (totalPoints >= TIER_THRESHOLDS.get("GOLD")) return "GOLD";
        if (totalPoints >= TIER_THRESHOLDS.get("SILVER")) return "SILVER";
        return "BRONZE";
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

    private boolean canUserAccessReward(String userTier, String requiredTier) {
        if ("ALL".equals(requiredTier)) return true;

        int userTierLevel = getTierLevel(userTier);
        int requiredTierLevel = getTierLevel(requiredTier);

        return userTierLevel >= requiredTierLevel;
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

    // Expire old points
    public void expireOldPoints() {
        LocalDateTime now = LocalDateTime.now();
        List<PointsTransaction> expiredTransactions = pointsTransactionRepository
                .findExpiredEarnedPoints(now);

        for (PointsTransaction transaction : expiredTransactions) {
            if (transaction.isActive()) {
                // Create expiry transaction
                PointsTransaction expiryTransaction = new PointsTransaction(
                        transaction.getUserId(),
                        -transaction.getPoints(),
                        "EXPIRED",
                        "EXPIRY"
                );
                expiryTransaction.setDescription("Điểm hết hạn");
                expiryTransaction.setRelatedOrderId(transaction.getRelatedOrderId());

                pointsTransactionRepository.save(expiryTransaction);

                // Update original transaction
                transaction.setActive(false);
                pointsTransactionRepository.save(transaction);

                // Update user balance
                LoyaltyAccount account = getLoyaltyAccount(transaction.getUserId());
                account.setPointsBalance(account.getPointsBalance() - transaction.getPoints());
                loyaltyAccountRepository.save(account);

                // Notify user
                notificationService.createNotification(new com.marketplace.dto.NotificationRequest(
                        transaction.getUserId(),
                        "Điểm đã hết hạn",
                        transaction.getPoints() + " điểm của bạn đã hết hạn sử dụng.",
                        "SYSTEM_UPDATE"
                ));
            }
        }
    }

    // Get loyalty statistics
    public Map<String, Object> getLoyaltyStats(String userId) {
        LoyaltyAccount account = getLoyaltyAccount(userId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("currentPoints", account.getPointsBalance());
        stats.put("totalEarned", account.getTotalEarnedPoints());
        stats.put("totalSpent", account.getTotalSpentPoints());
        stats.put("currentTier", account.getTier());
        stats.put("tierProgress", account.getTierProgress());
        stats.put("nextTier", getNextTier(account.getTier()));

        // Points expiring soon (within 30 days)
        LocalDateTime thirtyDaysFromNow = LocalDateTime.now().plusDays(30);
        List<PointsTransaction> expiringSoon = pointsTransactionRepository
                .findExpiringPoints(userId, thirtyDaysFromNow);
        int pointsExpiringSoon = expiringSoon.stream()
                .mapToInt(PointsTransaction::getPoints)
                .sum();
        stats.put("pointsExpiringSoon", pointsExpiringSoon);

        // Recent transactions
        List<PointsTransaction> recentTransactions = getPointsHistory(userId, 5);
        stats.put("recentTransactions", recentTransactions);

        // Available rewards count
        List<PointsReward> availableRewards = getAvailableRewards(userId);
        stats.put("availableRewardsCount", availableRewards.size());

        // Tier benefits
        stats.put("tierBenefits", getTierBenefits(account.getTier()));

        return stats;
    }

    private Map<String, Object> getTierBenefits(String tier) {
        Map<String, Object> benefits = new HashMap<>();

        switch (tier) {
            case "BRONZE":
                benefits.put("pointsMultiplier", 1.0);
                benefits.put("freeShippingThreshold", 500000.0);
                benefits.put("birthdayBonus", 500);
                break;
            case "SILVER":
                benefits.put("pointsMultiplier", 1.2);
                benefits.put("freeShippingThreshold", 300000.0);
                benefits.put("birthdayBonus", 750);
                benefits.put("exclusiveDeals", true);
                break;
            case "GOLD":
                benefits.put("pointsMultiplier", 1.5);
                benefits.put("freeShippingThreshold", 200000.0);
                benefits.put("birthdayBonus", 1000);
                benefits.put("exclusiveDeals", true);
                benefits.put("prioritySupport", true);
                break;
            case "PLATINUM":
                benefits.put("pointsMultiplier", 2.0);
                benefits.put("freeShippingThreshold", 0.0);
                benefits.put("birthdayBonus", 1500);
                benefits.put("exclusiveDeals", true);
                benefits.put("prioritySupport", true);
                benefits.put("earlyAccess", true);
                break;
            case "DIAMOND":
                benefits.put("pointsMultiplier", 2.5);
                benefits.put("freeShippingThreshold", 0.0);
                benefits.put("birthdayBonus", 2000);
                benefits.put("exclusiveDeals", true);
                benefits.put("prioritySupport", true);
                benefits.put("earlyAccess", true);
                benefits.put("personalShopper", true);
                break;
        }

        return benefits;
    }

    // Admin methods
    public PointsReward createReward(PointsReward reward) {
        return pointsRewardRepository.save(reward);
    }

    public void updateReward(String rewardId, PointsReward rewardDetails) {
        PointsReward reward = pointsRewardRepository.findById(rewardId)
                .orElseThrow(() -> new ResourceNotFoundException("Reward not found"));

        reward.setName(rewardDetails.getName());
        reward.setDescription(rewardDetails.getDescription());
        reward.setPointsCost(rewardDetails.getPointsCost());
        reward.setRewardType(rewardDetails.getRewardType());
        reward.setDiscountPercentage(rewardDetails.getDiscountPercentage());
        reward.setDiscountAmount(rewardDetails.getDiscountAmount());
        reward.setStockQuantity(rewardDetails.getStockQuantity());
        reward.setRequiredTier(rewardDetails.getRequiredTier());
        reward.setValidUntil(rewardDetails.getValidUntil());
        reward.setUpdatedAt(LocalDateTime.now());

        pointsRewardRepository.save(reward);
    }

    public void deleteReward(String rewardId) {
        PointsReward reward = pointsRewardRepository.findById(rewardId)
                .orElseThrow(() -> new ResourceNotFoundException("Reward not found"));

        reward.setActive(false);
        pointsRewardRepository.save(reward);
    }

    // Bulk operations
    public void awardBulkPoints(List<String> userIds, int points, String reason, String description) {
        for (String userId : userIds) {
            awardPoints(userId, points, reason, description);
        }
    }

    // Use redemption code
    public void useRedemptionCode(String redemptionCode, String orderId) {
        RewardRedemption redemption = rewardRedemptionRepository.findByRedemptionCode(redemptionCode)
                .orElseThrow(() -> new ResourceNotFoundException("Mã đổi thưởng không tồn tại"));

        if (!"APPROVED".equals(redemption.getStatus())) {
            throw new BadRequestException("Mã đổi thưởng chưa được phê duyệt");
        }

        if (redemption.getExpiryDate() != null && redemption.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Mã đổi thưởng đã hết hạn");
        }

        redemption.setStatus("USED");
        redemption.setUsedAt(LocalDateTime.now());
        redemption.setOrderId(orderId);

        rewardRedemptionRepository.save(redemption);
    }
}