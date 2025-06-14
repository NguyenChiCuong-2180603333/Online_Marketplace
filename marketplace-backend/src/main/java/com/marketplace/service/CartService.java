package com.marketplace.service;

import com.marketplace.model.Cart;
import com.marketplace.model.Product;
import com.marketplace.repository.CartRepository;
import com.marketplace.exception.ResourceNotFoundException;
import com.marketplace.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductService productService;

    public Cart getCartByUserId(String userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));
    }

    private Cart createNewCart(String userId) {
        Cart cart = new Cart(userId);
        return cartRepository.save(cart);
    }

    public Cart addItemToCart(String userId, String productId, int quantity) {
        Cart cart = getCartByUserId(userId);
        Product product = productService.getProductById(productId);

        if (!product.isActive()) {
            throw new BadRequestException("Sản phẩm hiện không khả dụng");
        }

        if (product.getStockQuantity() < quantity) {
            throw new BadRequestException("Không đủ hàng trong kho. Còn lại: " + product.getStockQuantity() + " sản phẩm");
        }

        // Check if item already exists in cart
        Optional<Cart.CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            // Update quantity
            Cart.CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;

            if (product.getStockQuantity() < newQuantity) {
                throw new BadRequestException("Không đủ hàng trong kho. Còn lại: " + product.getStockQuantity() + " sản phẩm");
            }

            item.setQuantity(newQuantity);
            item.setSubtotal(item.getProductPrice() * newQuantity);
        } else {
            // Add new item
            String imageUrl = product.getImages().isEmpty() ? null : product.getImages().get(0);
            Cart.CartItem newItem = new Cart.CartItem(
                    productId,
                    product.getName(),
                    product.getPrice(),
                    imageUrl,
                    quantity
            );
            cart.getItems().add(newItem);
        }

        updateCartTotals(cart);
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepository.save(cart);
    }

    public Cart updateItemQuantity(String userId, String productId, int quantity) {
        Cart cart = getCartByUserId(userId);
        Product product = productService.getProductById(productId);

        if (quantity <= 0) {
            return removeItemFromCart(userId, productId);
        }

        if (product.getStockQuantity() < quantity) {
            throw new BadRequestException("Không đủ hàng trong kho. Còn lại: " + product.getStockQuantity() + " sản phẩm");
        }

        Cart.CartItem item = cart.getItems().stream()
                .filter(cartItem -> cartItem.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm trong giỏ hàng"));

        item.setQuantity(quantity);
        item.setSubtotal(item.getProductPrice() * quantity);

        updateCartTotals(cart);
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepository.save(cart);
    }

    public Cart removeItemFromCart(String userId, String productId) {
        Cart cart = getCartByUserId(userId);

        cart.getItems().removeIf(item -> item.getProductId().equals(productId));

        updateCartTotals(cart);
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepository.save(cart);
    }

    public void clearCart(String userId) {
        Cart cart = getCartByUserId(userId);
        cart.setItems(new ArrayList<>());
        cart.setTotalAmount(0.0);
        cart.setTotalItems(0);
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }

    private void updateCartTotals(Cart cart) {
        double totalAmount = cart.getItems().stream()
                .mapToDouble(Cart.CartItem::getSubtotal)
                .sum();

        int totalItems = cart.getItems().stream()
                .mapToInt(Cart.CartItem::getQuantity)
                .sum();

        cart.setTotalAmount(totalAmount);
        cart.setTotalItems(totalItems);
    }

    public boolean validateCartStock(String userId) {
        Cart cart = getCartByUserId(userId);

        for (Cart.CartItem item : cart.getItems()) {
            Product product = productService.getProductById(item.getProductId());
            if (!product.isActive() || product.getStockQuantity() < item.getQuantity()) {
                return false;
            }
        }
        return true;
    }
}