package com.klu.raithubazaar.service;

import com.klu.raithubazaar.dto.PaymentVerifyRequest;
import com.klu.raithubazaar.model.Payment;
import com.klu.raithubazaar.repository.OrderRepository;
import com.klu.raithubazaar.repository.PaymentRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class PaymentService {
    private final OrderRepository orders;
    private final PaymentRepository payments;
    private final BackgroundJobService jobs;
    private final JsonMapper objectMapper;
    private final String keyId;
    private final String keySecret;
    private final String webhookSecret;

    public PaymentService(OrderRepository orders, PaymentRepository payments, BackgroundJobService jobs,
                          JsonMapper objectMapper,
                          @Value("${app.payment.razorpay-key-id}") String keyId,
                          @Value("${app.payment.razorpay-key-secret}") String keySecret,
                          @Value("${app.payment.webhook-secret}") String webhookSecret) {
        this.orders = orders;
        this.payments = payments;
        this.jobs = jobs;
        this.objectMapper = objectMapper;
        this.keyId = keyId;
        this.keySecret = keySecret;
        this.webhookSecret = webhookSecret;
    }

    @Transactional
    public Map<String, Object> createRazorpayOrder(Long orderId, String email) {
        if (keyId.isBlank() || keySecret.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Razorpay is not configured. Add RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET to the backend environment.");
        }
        var order = orders.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        if (!order.getUser().getEmail().equalsIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This order belongs to another account");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This order is no longer payable");
        }
        Payment payment = payments.findByOrderOrderId(orderId).orElseGet(Payment::new);
        try {
            JSONObject options = new JSONObject();
            options.put("amount", order.getTotalAmount().movePointRight(2).longValueExact());
            options.put("currency", "INR");
            options.put("receipt", "raithu_order_" + orderId);
            com.razorpay.Order razorpayOrder = new RazorpayClient(keyId, keySecret).orders.create(options);
            String razorpayOrderId = razorpayOrder.get("id");
            payment.setOrder(order);
            payment.setRazorpayOrderId(razorpayOrderId);
            payment.setAmount(order.getTotalAmount());
            payment.setStatus("CREATED");
            payments.save(payment);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("keyId", keyId);
            result.put("orderId", orderId);
            result.put("razorpayOrderId", razorpayOrderId);
            result.put("amount", order.getTotalAmount().movePointRight(2).longValueExact());
            result.put("currency", "INR");
            return result;
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Could not create a Razorpay order: " + exception.getMessage());
        }
    }

    @Transactional
    public void verifyPayment(PaymentVerifyRequest request, String email) {
        if (request.orderId() == null || isBlank(request.razorpayOrderId())
                || isBlank(request.razorpayPaymentId()) || isBlank(request.razorpaySignature())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment verification fields are required");
        }
        Payment payment = payments.findByOrderOrderId(request.orderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment was not initialized"));
        if (!"PENDING".equals(payment.getOrder().getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This order is no longer payable");
        }
        if (!payment.getOrder().getUser().getEmail().equalsIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This order belongs to another account");
        }
        if (!payment.getRazorpayOrderId().equals(request.razorpayOrderId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment order does not match");
        }
        String expected = hmac(request.razorpayOrderId() + "|" + request.razorpayPaymentId(), keySecret);
        if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                request.razorpaySignature().getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment signature is invalid");
        }
        confirm(payment, request.razorpayPaymentId(), request.razorpaySignature());
    }

    @Transactional
    public void processWebhook(String payload, String signature) {
        if (webhookSecret.isBlank() || isBlank(signature)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Webhook signature is missing");
        }
        String expected = hmac(payload, webhookSecret);
        if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Webhook signature is invalid");
        }
        try {
            JsonNode root = objectMapper.readTree(payload);
            if (!"payment.captured".equals(root.path("event").asText())) return;
            JsonNode entity = root.path("payload").path("payment").path("entity");
            String razorpayOrderId = entity.path("order_id").asText();
            String paymentId = entity.path("id").asText();
            Payment payment = payments.findByRazorpayOrderId(razorpayOrderId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment order not found"));
            confirm(payment, paymentId, "webhook-verified");
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Webhook payload is invalid");
        }
    }

    private void confirm(Payment payment, String paymentId, String signature) {
        if ("PAID".equals(payment.getStatus())) return;
        if (!"PENDING".equals(payment.getOrder().getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This order has expired; contact the store if you were charged");
        }
        payment.setRazorpayPaymentId(paymentId);
        payment.setRazorpaySignature(signature);
        payment.setStatus("PAID");
        payment.getOrder().setStatus("PAID");
        payments.save(payment);
        orders.save(payment.getOrder());
        jobs.queueForOrder(payment.getOrder().getOrderId());
    }

    private String hmac(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Payment signature could not be verified", exception);
        }
    }

    private boolean isBlank(String value) { return value == null || value.isBlank(); }
}
