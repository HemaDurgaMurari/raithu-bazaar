package com.klu.raithubazaar.controller;

import com.klu.raithubazaar.dto.PaymentVerifyRequest;
import com.klu.raithubazaar.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    private final PaymentService service;
    public PaymentController(PaymentService service) { this.service = service; }
    @PostMapping("/create/{orderId}")
    public Map<String, Object> create(@PathVariable Long orderId, Authentication authentication) {
        return service.createRazorpayOrder(orderId, authentication.getName());
    }
    @PostMapping("/verify")
    public ResponseEntity<Map<String, String>> verify(@RequestBody PaymentVerifyRequest request,
                                                       Authentication authentication) {
        service.verifyPayment(request, authentication.getName());
        return ResponseEntity.ok(Map.of("message", "Payment verified and order confirmed"));
    }
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody String payload,
                                        @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {
        service.processWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }
}
