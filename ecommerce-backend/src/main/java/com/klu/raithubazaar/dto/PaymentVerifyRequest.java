package com.klu.raithubazaar.dto;

public record PaymentVerifyRequest(Long orderId, String razorpayOrderId, String razorpayPaymentId,
                                   String razorpaySignature) {}
