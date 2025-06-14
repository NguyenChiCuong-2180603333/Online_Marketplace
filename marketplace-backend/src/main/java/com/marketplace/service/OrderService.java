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

    public Order createOrderFromCart(String userId, String shippingAddress, String billingAddress) {
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
            // Note: You'll need to save the product - add this to ProductService
        }

        // Create order
        Order order = new Order(userId, user.getEmail(), orderItems, cart.getTotalAmount());
        order.setShippingAddress(shippingAddress);
        order.setBillingAddress(billingAddress);

        Order savedOrder = orderRepository.save(order);

        // Clear cart after successful order creation
        cartService.clearCart(userId);

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

        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());

        if ("DELIVERED".equals(status)) {
            order.setDeliveredAt(LocalDateTime.now());
        }

        return orderRepository.save(order);
    }

    public Order updatePaymentStatus(String orderId, String paymentStatus, String paymentId) {
        Order order = getOrderById(orderId);

        if (!isValidPaymentStatus(paymentStatus)) {
            throw new BadRequestException("Trạng thái thanh toán không hợp lệ: " + paymentStatus);
        }

        order.setPaymentStatus(paymentStatus);
        order.setPaymentId(paymentId);
        order.setUpdatedAt(LocalDateTime.now());

        // If payment completed, update order status to PROCESSING
        if ("COMPLETED".equals(paymentStatus) && "PENDING".equals(order.getStatus())) {
            order.setStatus("PROCESSING");
        }

        return orderRepository.save(order);
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

    public Order cancelOrder(String orderId, String userId) {
        Order order = getOrderById(orderId);

        // Verify user owns this order
        if (!order.getUserId().equals(userId)) {
            throw new BadRequestException("Bạn chỉ có thể hủy đơn hàng của chính mình");
        }

        // Only allow cancellation for PENDING or PROCESSING orders
        if (!("PENDING".equals(order.getStatus()) || "PROCESSING".equals(order.getStatus()))) {
            throw new BadRequestException("Không thể hủy đơn hàng có trạng thái: " + order.getStatus());
        }

        // Restore product stock
        for (Order.OrderItem item : order.getItems()) {
            try {
                Product product = productService.getProductById(item.getProductId());
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                product.setUpdatedAt(LocalDateTime.now());
                // Note: You'll need to save the product
            } catch (Exception e) {
                // Log error but don't fail the cancellation
                System.err.println("Không thể khôi phục số lượng kho cho sản phẩm: " + item.getProductId());
            }
        }

        order.setStatus("CANCELLED");
        order.setUpdatedAt(LocalDateTime.now());

        return orderRepository.save(order);
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
        return "PENDING".equals(status) || "PROCESSING".equals(status) ||
                "SHIPPED".equals(status) || "DELIVERED".equals(status) ||
                "CANCELLED".equals(status);
    }

    private boolean isValidPaymentStatus(String paymentStatus) {
        return "PENDING".equals(paymentStatus) || "COMPLETED".equals(paymentStatus) ||
                "FAILED".equals(paymentStatus);
    }
}