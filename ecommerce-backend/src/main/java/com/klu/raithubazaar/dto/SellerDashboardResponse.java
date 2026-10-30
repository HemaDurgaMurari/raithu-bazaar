package com.klu.raithubazaar.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SellerDashboardResponse(
        Summary summary,
        List<SellerOrder> orders) {

    public record Summary(
            long paidOrderCount,
            long orderCount,
            BigDecimal todayTurnover,
            BigDecimal monthlyTurnover,
            BigDecimal totalTurnover) {}

    public record SellerOrder(
            Long orderId,
            String status,
            BigDecimal totalAmount,
            BigDecimal subtotalAmount,
            BigDecimal deliveryCharge,
            BigDecimal carryingCharge,
            BigDecimal gstAmount,
            LocalDateTime createdAt,
            LocalDateTime scheduledFor,
            Customer customer,
            String deliveryName,
            String deliveryAddress,
            String phone,
            List<OrderLine> items) {}

    public record Customer(Long customerId, String name, String email) {}

    public record OrderLine(Long productId, String productName, int quantity, BigDecimal unitPrice, BigDecimal lineTotal) {}
}
