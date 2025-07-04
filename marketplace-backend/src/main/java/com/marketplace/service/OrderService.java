package com.marketplace.service;

import com.marketplace.model.Cart;
import com.marketplace.model.Order;
import com.marketplace.model.Product;
import com.marketplace.model.User;
import com.marketplace.repository.OrderRepository;
import com.marketplace.exception.ResourceNotFoundException;
import com.marketplace.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private LoyaltyService loyaltyService;

    @Autowired
    private MarketplaceEventListener eventListener;

    public Order createOrderFromCart(String userId, String shippingAddress, String billingAddress, String paymentMethod, Double shippingFee) {
        User user = userService.getUserById(userId);
        Cart cart = cartService.getCartByUserId(userId);

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Giỏ hàng trống");
        }

        // Validate stock availability
        if (!cartService.validateCartStock(userId)) {
            throw new BadRequestException("Một số sản phẩm trong giỏ hàng đã hết hàng");
        }

        // Create order items from cart
        List<Order.OrderItem> orderItems = new ArrayList<>();
        for (Cart.CartItem cartItem : cart.getItems()) {
            Product product = productService.getProductById(cartItem.getProductId());

            Order.OrderItem orderItem = new Order.OrderItem(
                    cartItem.getProductId(),
                    cartItem.getProductName(),
                    cartItem.getProductPrice(),
                    cartItem.getProductImage(),
                    cartItem.getQuantity(),
                    product.getSellerId()
            );
            orderItems.add(orderItem);

            // Update product stock
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            product.setUpdatedAt(LocalDateTime.now());
            productService.saveProduct(product);
        }

        // Tính tổng tiền đã bao gồm phí vận chuyển
        double totalAmount = cart.getTotalAmount() + (shippingFee != null ? shippingFee : 0.0);

        // Create order với shippingFee từ frontend
        Order order = new Order(userId, user.getEmail(), orderItems, totalAmount, shippingFee);
        order.setShippingAddress(shippingAddress);
        order.setBillingAddress(billingAddress);
        order.setPaymentMethod(paymentMethod);

        Order savedOrder = orderRepository.save(order);

        // Clear cart after successful order creation
        cartService.clearCart(userId);

        eventListener.handleOrderCreated(
                savedOrder.getId(),
                user.getEmail(),
                user.getFirstName() + " " + user.getLastName(),
                savedOrder.getTotalAmount()
        );

        return savedOrder;
    }

    public List<Order> getOrdersByUserId(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Order getOrderById(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
    }

    public Order updateOrderStatus(String orderId, String status) {
        Order order = getOrderById(orderId);

        // Validate status
        if (!isValidStatus(status)) {
            throw new BadRequestException("Trạng thái đơn hàng không hợp lệ: " + status);
        }

        String oldStatus = order.getStatus();
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());

        if ("DELIVERED".equals(status)) {
            order.setDeliveredAt(LocalDateTime.now());

            // 🆕 NEW: Award loyalty points for completed order
            loyaltyService.awardPointsFromOrder(order);
        }

        Order savedOrder = orderRepository.save(order);

        // 🆕 NEW: Send status update email if status changed
        if (!oldStatus.equals(status)) {
            try {
                User user = userService.getUserById(order.getUserId());
                eventListener.handleOrderStatusUpdate(
                        orderId,
                        user.getEmail(),
                        user.getFirstName() + " " + user.getLastName(),
                        status
                );
            } catch (Exception e) {
                // Log but don't fail order update
                System.err.println("Failed to send order status email: " + e.getMessage());
            }
        }

        return savedOrder;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    public List<Order> getOrdersBySeller(String sellerId) {
        return orderRepository.findBySellerIdInItems(sellerId);
    }

    public boolean isOrderFromSeller(String orderId, String sellerId) {
        Order order = getOrderById(orderId);
        return order.getItems().stream()
                .anyMatch(item -> sellerId.equals(item.getSellerId()));
    }

    public Order cancelOrder(String orderId, String userId) {
        Order order = getOrderById(orderId);

        // Verify ownership
        if (!order.getUserId().equals(userId)) {
            throw new BadRequestException("Không có quyền hủy đơn hàng này");
        }

        // Check if order can be cancelled
        if (List.of("SHIPPED", "DELIVERED", "CANCELLED").contains(order.getStatus())) {
            throw new BadRequestException("Không thể hủy đơn hàng ở trạng thái hiện tại");
        }

        order.setStatus("CANCELLED");
        order.setUpdatedAt(LocalDateTime.now());

        // Restore product stock
        for (Order.OrderItem item : order.getItems()) {
            try {
                Product product = productService.getProductById(item.getProductId());
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                productService.saveProduct(product);
            } catch (Exception e) {
                // Log but continue with cancellation
                System.err.println("Failed to restore stock for product: " + item.getProductId());
            }
        }

        Order savedOrder = orderRepository.save(order);

        // 🆕 NEW: Send cancellation email
        try {
            User user = userService.getUserById(userId);
            eventListener.handleOrderStatusUpdate(
                    orderId,
                    user.getEmail(),
                    user.getFirstName() + " " + user.getLastName(),
                    "CANCELLED"
            );
        } catch (Exception e) {
            System.err.println("Failed to send cancellation email: " + e.getMessage());
        }

        return savedOrder;
    }

    // Admin statistics methods
    public long getTotalOrdersCount() {
        return orderRepository.count();
    }

    public long getOrdersCountByStatus(String status) {
        return orderRepository.countByStatus(status);
    }

    public double getTotalRevenue() {
        List<Order> completedOrders = orderRepository.findAllCompletedOrders();
        return completedOrders.stream()
                .mapToDouble(Order::getTotalAmount)
                .sum();
    }

    public List<Order> getRecentOrders(int limit) {
        // Note: You might want to add a method to repository for this
        return orderRepository.findAll()
                .stream()
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .limit(limit)
                .toList();
    }

    private boolean isValidStatus(String status) {
        return List.of("PENDING", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED")
                .contains(status);
    }

    private boolean isValidPaymentStatus(String paymentStatus) {
        return "PENDING".equals(paymentStatus) || "COMPLETED".equals(paymentStatus) ||
                "FAILED".equals(paymentStatus);
    }
    public void updatePaymentStatus(String orderId, String paymentStatus, String paymentId) {
        Order order = getOrderById(orderId);
        order.setPaymentStatus(paymentStatus);
        order.setPaymentId(paymentId);
        order.setUpdatedAt(LocalDateTime.now());

        if ("COMPLETED".equals(paymentStatus)) {
            order.setStatus("PROCESSING");
        }

        orderRepository.save(order);
    }

    public OrderRepository getOrderRepository() {
        return orderRepository;
    }
}