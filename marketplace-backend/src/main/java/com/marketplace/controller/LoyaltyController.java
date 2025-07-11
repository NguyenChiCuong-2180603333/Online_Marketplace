package com.marketplace.controller;

import com.marketplace.model.LoyaltyAccount;
import com.marketplace.model.PointTransaction;
import com.marketplace.security.JwtTokenProvider;
import com.marketplace.service.LoyaltyService;
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
public class LoyaltyController {

    @Autowired
    private LoyaltyService loyaltyService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * Lấy thông tin loyalty account của user
     */
    @GetMapping("/account")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<LoyaltyAccount> getLoyaltyAccount() {
        String userId = getCurrentUserId();
        LoyaltyAccount account = loyaltyService.getLoyaltyAccount(userId);
        return ResponseEntity.ok(account);
    }

    /**
     * Lấy điểm hiện tại của user
     */
    @GetMapping("/points")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPoints() {
        String userId = getCurrentUserId();
        LoyaltyAccount account = loyaltyService.getLoyaltyAccount(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("points", account.getAvailablePoints());
        response.put("tier", account.getTier());
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy thông tin các tier
     */
    @GetMapping("/tiers")
    public ResponseEntity<Map<String, Object>> getTiers() {
        Map<String, Object> tiers = new HashMap<>();
        tiers.put("BRONZE", Map.of("minPoints", 0, "discount", 0.05));
        tiers.put("SILVER", Map.of("minPoints", 1000, "discount", 0.10));
        tiers.put("GOLD", Map.of("minPoints", 5000, "discount", 0.15));
        tiers.put("PLATINUM", Map.of("minPoints", 10000, "discount", 0.20));
        return ResponseEntity.ok(tiers);
    }

    /**
     * Lấy thống kê loyalty đầy đủ
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getLoyaltyStats() {
        String userId = getCurrentUserId();
        Map<String, Object> stats = loyaltyService.getLoyaltyStats(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Lấy lịch sử giao dịch điểm
     */
    @GetMapping("/history")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<PointTransaction>> getPointHistory() {
        String userId = getCurrentUserId();
        List<PointTransaction> history = loyaltyService.getPointHistory(userId);
        return ResponseEntity.ok(history);
    }

    /**
     * Đổi điểm lấy voucher
     */
    @PostMapping("/redeem")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> redeemVoucher(@RequestBody Map<String, Object> request) {
        String userId = getCurrentUserId();
        String voucherType = (String) request.get("voucherType");
        Integer pointsCost = (Integer) request.get("pointsCost");

        Map<String, Object> voucher = loyaltyService.redeemVoucher(userId, voucherType, pointsCost);
        return ResponseEntity.ok(voucher);
    }

    /**
     * Tạo loyalty account (tự động khi user đăng ký)
     */
    @PostMapping("/account")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<LoyaltyAccount> createLoyaltyAccount() {
        String userId = getCurrentUserId();
        LoyaltyAccount account = loyaltyService.createLoyaltyAccount(userId);
        return ResponseEntity.ok(account);
    }

    /**
     * Admin: Điều chỉnh điểm cho user
     */
    @PostMapping("/admin/adjust-points")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> adminAdjustPoints(@RequestBody Map<String, Object> request) {
        String userId = (String) request.get("userId");
        Integer points = (Integer) request.get("points");
        String reason = (String) request.get("reason");

        loyaltyService.adminAdjustPoints(userId, points, reason);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Điểm đã được điều chỉnh thành công");
        return ResponseEntity.ok(response);
    }

    /**
     * Admin: Xử lý điểm hết hạn
     */
    @PostMapping("/admin/process-expired")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> processExpiredPoints() {
        loyaltyService.processExpiredPoints();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Đã xử lý điểm hết hạn");
        return ResponseEntity.ok(response);
    }

    /**
     * Admin: Thống kê tổng quan loyalty
     */
    @GetMapping("/admin/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAdminOverview() {
        Map<String, Object> overview = new HashMap<>();
        overview.put("message", "Admin loyalty overview - implement as needed");
        return ResponseEntity.ok(overview);
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal())) {
            String token = getJwtFromCurrentRequest();
            if (token != null) {
                try {
                    return jwtTokenProvider.getUserIdFromToken(token);
                } catch (Exception e) {
                }
            }
        }
        throw new RuntimeException("Cannot extract userId from JWT");
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