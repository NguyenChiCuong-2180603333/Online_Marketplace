package com.marketplace.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentIntentConfirmParams;
import com.marketplace.model.Order;
import com.marketplace.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {

    @Value("${stripe.api-key}")
    private String stripeSecretKey;

    @Autowired
    private OrderService orderService;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    public Map<String, Object> createPaymentIntent(String orderId, Long amount, String currency) {
        try {
            long amountInVND;
            String usedCurrency = (currency != null) ? currency : "vnd";
            String description = "Thanh toán qua Stripe";
            Map<String, String> metadata = new HashMap<>();

            if (orderId != null) {
                Order order = orderService.getOrderById(orderId);
                if (!"PENDING".equals(order.getPaymentStatus())) {
                    throw new BadRequestException("Thanh toán đơn hàng đã được xử lý");
                }
                amountInVND = order.getTotalAmount().longValue();
                metadata.put("orderId", orderId);
                metadata.put("userId", order.getUserId());
                description = "Đơn hàng #" + orderId;
            } else {
                if (amount == null) {
                    throw new BadRequestException("Thiếu số tiền thanh toán");
                }
                amountInVND = amount;
            }

            PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
                    .setAmount(amountInVND)
                    .setCurrency(usedCurrency)
                    .setDescription(description)
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    );
            for (Map.Entry<String, String> entry : metadata.entrySet()) {
                paramsBuilder.putMetadata(entry.getKey(), entry.getValue());
            }

            PaymentIntent paymentIntent = PaymentIntent.create(paramsBuilder.build());

            if (orderId != null) {
                orderService.updatePaymentStatus(orderId, "PENDING", paymentIntent.getId());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("clientSecret", paymentIntent.getClientSecret());
            response.put("paymentIntentId", paymentIntent.getId());
            response.put("amount", amountInVND);
            response.put("currency", usedCurrency);

            return response;

        } catch (StripeException e) {
            throw new BadRequestException("Không thể tạo thanh toán: " + e.getMessage());
        }
    }

    public Map<String, Object> confirmPayment(String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            if ("succeeded".equals(paymentIntent.getStatus())) {
                // Payment already succeeded
                String orderId = paymentIntent.getMetadata().get("orderId");
                orderService.updatePaymentStatus(orderId, "COMPLETED", paymentIntentId);

                Map<String, Object> response = new HashMap<>();
                response.put("status", "succeeded");
                response.put("orderId", orderId);
                response.put("message", "Thanh toán hoàn tất thành công");
                return response;
            }

            // If payment requires confirmation
            if ("requires_confirmation".equals(paymentIntent.getStatus())) {
                PaymentIntentConfirmParams confirmParams = PaymentIntentConfirmParams.builder()
                        .setReturnUrl("https://your-website.com/return") // Update with your URL
                        .build();

                paymentIntent = paymentIntent.confirm(confirmParams);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("status", paymentIntent.getStatus());
            response.put("paymentIntentId", paymentIntentId);

            // Handle different payment statuses
            String orderId = paymentIntent.getMetadata().get("orderId");
            switch (paymentIntent.getStatus()) {
                case "succeeded":
                    orderService.updatePaymentStatus(orderId, "COMPLETED", paymentIntentId);
                    response.put("message", "Thanh toán hoàn tất thành công");
                    break;
                case "requires_payment_method":
                    response.put("message", "Thanh toán cần phương thức thanh toán");
                    break;
                case "requires_action":
                    response.put("message", "Thanh toán cần xác thực thêm");
                    response.put("clientSecret", paymentIntent.getClientSecret());
                    break;
                default:
                    response.put("message", "Trạng thái thanh toán: " + paymentIntent.getStatus());
            }

            response.put("orderId", orderId);
            return response;

        } catch (StripeException e) {
            throw new BadRequestException("Không thể xác nhận thanh toán: " + e.getMessage());
        }
    }

    public Map<String, Object> handleWebhook(String payload, String sigHeader) {

        Map<String, Object> response = new HashMap<>();
        response.put("received", true);
        return response;
    }

    public Map<String, Object> cancelPayment(String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            if ("requires_payment_method".equals(paymentIntent.getStatus()) ||
                    "requires_confirmation".equals(paymentIntent.getStatus()) ||
                    "requires_action".equals(paymentIntent.getStatus())) {

                paymentIntent = paymentIntent.cancel();

                String orderId = paymentIntent.getMetadata().get("orderId");
                orderService.updatePaymentStatus(orderId, "FAILED", paymentIntentId);

                Map<String, Object> response = new HashMap<>();
                response.put("status", "cancelled");
                response.put("orderId", orderId);
                response.put("message", "Hủy thanh toán thành công");
                return response;
            } else {
                throw new BadRequestException("Không thể hủy thanh toán ở trạng thái hiện tại: " + paymentIntent.getStatus());
            }

        } catch (StripeException e) {
            throw new BadRequestException("Không thể hủy thanh toán: " + e.getMessage());
        }
    }

    public Map<String, Object> getPaymentStatus(String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", paymentIntent.getStatus());
            response.put("amount", paymentIntent.getAmount());
            response.put("currency", paymentIntent.getCurrency());
            response.put("paymentIntentId", paymentIntentId);

            if (paymentIntent.getMetadata() != null) {
                response.put("orderId", paymentIntent.getMetadata().get("orderId"));
            }

            return response;

        } catch (StripeException e) {
            throw new BadRequestException("Không thể lấy trạng thái thanh toán: " + e.getMessage());
        }
    }
}