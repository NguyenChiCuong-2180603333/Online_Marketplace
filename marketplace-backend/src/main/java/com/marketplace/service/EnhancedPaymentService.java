package com.marketplace.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.marketplace.model.Order;
import com.marketplace.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EnhancedPaymentService {

    @Value("${stripe.api-key}")
    private String stripeSecretKey;

    @Autowired
    private OrderService orderService;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    public Map<String, Object> createPaymentIntent(String orderId, String paymentType) {
        try {
            Order order = orderService.getOrderById(orderId);

            if (!"PENDING".equals(order.getPaymentStatus())) {
                throw new BadRequestException("Thanh toán đơn hàng đã được xử lý");
            }

            long amountInVND = order.getTotalAmount().longValue();

            PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
                    .setAmount(amountInVND)
                    .setCurrency("vnd")
                    .putMetadata("orderId", orderId)
                    .putMetadata("userId", order.getUserId())
                    .setDescription("Đơn hàng #" + orderId);

            switch (paymentType.toLowerCase()) {
                case "card":
                    paramsBuilder.setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                    .build()
                    );
                    break;

                case "ewallet":
                    List<String> paymentMethodTypes = new ArrayList<>();
                    paymentMethodTypes.add("grabpay");
                    paymentMethodTypes.add("fpx"); 
                    paymentMethodTypes.add("alipay");

                    paramsBuilder.addAllPaymentMethodType(paymentMethodTypes);
                    break;

                case "bank_transfer":
                    List<String> bankMethods = new ArrayList<>();
                    bankMethods.add("fpx");
                    bankMethods.add("promptpay"); 

                    paramsBuilder.addAllPaymentMethodType(bankMethods);
                    break;

                default:
                    paramsBuilder.setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    );
            }

            PaymentIntent paymentIntent = PaymentIntent.create(paramsBuilder.build());

            orderService.updatePaymentStatus(orderId, "PENDING", paymentIntent.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("clientSecret", paymentIntent.getClientSecret());
            response.put("paymentIntentId", paymentIntent.getId());
            response.put("amount", order.getTotalAmount());
            response.put("currency", "vnd");
            response.put("paymentType", paymentType);
            response.put("availablePaymentMethods", getAvailablePaymentMethods());

            return response;

        } catch (StripeException e) {
            throw new BadRequestException("Không thể tạo thanh toán: " + e.getMessage());
        }
    }

    /**
     * Lấy danh sách payment methods có sẵn
     */
    public Map<String, Object> getAvailablePaymentMethods() {
        Map<String, Object> methods = new HashMap<>();

        Map<String, Object> cards = new HashMap<>();
        cards.put("enabled", true);
        cards.put("types", List.of("visa", "mastercard", "amex"));
        cards.put("displayName", "Thẻ tín dụng/ghi nợ");
        cards.put("icon", "💳");
        methods.put("card", cards);

        // E-wallets
        Map<String, Object> ewallets = new HashMap<>();
        ewallets.put("enabled", true);
        ewallets.put("providers", List.of(
                Map.of("name", "GrabPay", "code", "grabpay", "icon", "🟢", "countries", List.of("VN", "SG", "MY")),
                Map.of("name", "ShopeePay", "code", "shopeepay", "icon", "🛒", "countries", List.of("VN", "SG", "MY")),
                Map.of("name", "Alipay", "code", "alipay", "icon", "💙", "countries", List.of("VN", "CN")),
                Map.of("name", "ZaloPay", "code", "zalopay", "icon", "💰", "countries", List.of("VN"))
        ));
        ewallets.put("displayName", "Ví điện tử");
        methods.put("ewallet", ewallets);

        // Bank Transfer
        Map<String, Object> banking = new HashMap<>();
        banking.put("enabled", true);
        banking.put("providers", List.of(
                Map.of("name", "Internet Banking", "code", "fpx", "icon", "🏦"),
                Map.of("name", "PromptPay", "code", "promptpay", "icon", "📱")
        ));
        banking.put("displayName", "Chuyển khoản ngân hàng");
        methods.put("bank_transfer", banking);

        return methods;
    }

    /**
     * Confirm payment với handling cho different payment types
     */
    public Map<String, Object> confirmPayment(String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            String orderId = paymentIntent.getMetadata().get("orderId");

            Map<String, Object> response = new HashMap<>();
            response.put("paymentIntentId", paymentIntentId);
            response.put("status", paymentIntent.getStatus());

            switch (paymentIntent.getStatus()) {
                case "succeeded":
                    orderService.updatePaymentStatus(orderId, "COMPLETED", paymentIntentId);
                    response.put("message", "Thanh toán hoàn tất thành công");

                    // Send confirmation email
                    sendPaymentConfirmationEmail(orderId);
                    break;

                case "requires_payment_method":
                    response.put("message", "Thanh toán cần phương thức thanh toán");
                    break;

                case "requires_action":
                    response.put("message", "Thanh toán cần xác thực thêm");
                    response.put("clientSecret", paymentIntent.getClientSecret());
                    break;

                case "processing":
                    response.put("message", "Đang xử lý thanh toán");
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

    /**
     * Handle Stripe webhook events
     */
    public Map<String, Object> handleWebhook(String payload, String sigHeader) {
   

        Map<String, Object> response = new HashMap<>();
        response.put("received", true);
        return response;
    }

    /**
     * Cancel payment
     */
    public Map<String, Object> cancelPayment(String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            if (List.of("requires_payment_method", "requires_confirmation", "requires_action")
                    .contains(paymentIntent.getStatus())) {

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

    /**
     * Get payment status
     */
    public Map<String, Object> getPaymentStatus(String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", paymentIntent.getStatus());
            response.put("amount", paymentIntent.getAmount());
            response.put("currency", paymentIntent.getCurrency());
            response.put("paymentIntentId", paymentIntentId);
            response.put("paymentMethod", paymentIntent.getPaymentMethod());

            if (paymentIntent.getMetadata() != null) {
                response.put("orderId", paymentIntent.getMetadata().get("orderId"));
            }

            return response;

        } catch (StripeException e) {
            throw new BadRequestException("Không thể lấy trạng thái thanh toán: " + e.getMessage());
        }
    }

    /**
     * Send payment confirmation email
     */
    private void sendPaymentConfirmationEmail(String orderId) {
        try {
            Order order = orderService.getOrderById(orderId);
        } catch (Exception e) {
            System.err.println("Failed to send payment confirmation email: " + e.getMessage());
        }
    }
}