package com.marketplace.controller;

import com.marketplace.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/create-intent")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createPaymentIntent(@RequestBody Map<String, Object> request) {
        String orderId = request.get("orderId") != null ? request.get("orderId").toString() : null;
        Long amount = request.get("amount") != null ? Long.valueOf(request.get("amount").toString()) : null;
        String currency = request.get("currency") != null ? request.get("currency").toString() : "vnd";
        Map<String, Object> response = paymentService.createPaymentIntent(orderId, amount, currency);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> confirmPayment(@RequestBody Map<String, String> request) {
        String paymentIntentId = request.get("paymentIntentId");
        Map<String, Object> response = paymentService.confirmPayment(paymentIntentId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cancel")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> cancelPayment(@RequestBody Map<String, String> request) {
        String paymentIntentId = request.get("paymentIntentId");
        Map<String, Object> response = paymentService.cancelPayment(paymentIntentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{paymentIntentId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPaymentStatus(@PathVariable String paymentIntentId) {
        Map<String, Object> response = paymentService.getPaymentStatus(paymentIntentId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        Map<String, Object> response = paymentService.handleWebhook(payload, sigHeader);
        return ResponseEntity.ok(response);
    }
}