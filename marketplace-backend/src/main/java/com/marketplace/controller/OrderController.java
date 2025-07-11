package com.marketplace.controller;

import com.marketplace.model.Order;
import com.marketplace.service.OrderService;
import com.marketplace.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping("/create")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Order> createOrder(@RequestBody Map<String, Object> request) {
        String userId = getCurrentUserId();
        String shippingAddress = (String) request.get("shippingAddress");
        String billingAddress = (String) request.get("billingAddress");
        String paymentMethod = (String) request.getOrDefault("paymentMethod", "cod");
        Double shippingFee = 0.0;
        if (request.containsKey("shippingFee")) {
            try {
                shippingFee = Double.valueOf(request.get("shippingFee").toString());
            } catch (Exception e) {
                shippingFee = 0.0;
            }
        }
        Integer loyaltyPointsToUse = null;
        if (request.containsKey("loyaltyPointsToUse")) {
            try {
                loyaltyPointsToUse = Integer.valueOf(request.get("loyaltyPointsToUse").toString());
            } catch (Exception e) {
                loyaltyPointsToUse = null;
            }
        }
        Order order = orderService.createOrderFromCart(userId, shippingAddress, billingAddress, paymentMethod, shippingFee, loyaltyPointsToUse);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<Order>> getMyOrders() {
        String userId = getCurrentUserId();
        List<Order> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Order> getOrderById(@PathVariable String id) {
        String userId = getCurrentUserId();
        Order order = orderService.getOrderById(id);
        
        if (!order.getUserId().equals(userId) && !isAdmin()) {
            return ResponseEntity.status(403).build();
        }
        
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Order> cancelOrder(@PathVariable String id) {
        String userId = getCurrentUserId();
        Order order = orderService.cancelOrder(id, userId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/seller/my-orders")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<Order>> getSellerOrders() {
        String sellerId = getCurrentUserId();
        List<Order> orders = orderService.getOrdersBySeller(sellerId);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable String orderId,
            @RequestBody Map<String, String> request) {
        String status = request.get("status");
        Order order = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{orderId}/seller-status")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Order> updateSellerOrderStatus(
            @PathVariable String orderId,
            @RequestBody Map<String, String> request) {
        String sellerId = getCurrentUserId();
        String status = request.get("status");
        
        Order order = orderService.getOrderById(orderId);
        if (!orderService.isOrderFromSeller(orderId, sellerId)) {
            return ResponseEntity.status(403).body(null);
        }
        
        Order updatedOrder = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(updatedOrder);
    }

    @PostMapping("/validate-promo")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> validatePromoCode(@RequestBody Map<String, String> request) {
        String promoCode = request.get("promoCode");
        String userId = getCurrentUserId();
        
        Map<String, Object> result = new HashMap<>();
        
        // Mock validation for now
        if ("COSMIC20".equals(promoCode) || "GALAXY15".equals(promoCode) || "SPACE10".equals(promoCode)) {
            result.put("valid", true);
            result.put("message", "Mã giảm giá hợp lệ");
            result.put("discount", promoCode.equals("COSMIC20") ? 20 : promoCode.equals("GALAXY15") ? 15 : 10);
        } else {
            result.put("valid", false);
            result.put("message", "Mã giảm giá không hợp lệ hoặc đã hết hạn");
        }
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getPrincipal().equals("anonymousUser")) {
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

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && 
               authentication.getAuthorities().stream()
                   .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}