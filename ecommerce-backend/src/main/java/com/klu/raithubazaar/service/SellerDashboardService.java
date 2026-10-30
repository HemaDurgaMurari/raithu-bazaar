package com.klu.raithubazaar.service;

import com.klu.raithubazaar.dto.SellerDashboardResponse;
import com.klu.raithubazaar.dto.SellerDashboardResponse.*;
import com.klu.raithubazaar.model.Order;
import com.klu.raithubazaar.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SellerDashboardService {
    private static final BigDecimal DELIVERY_CHARGE = new BigDecimal("49.00");
    private static final BigDecimal CARRYING_CHARGE = new BigDecimal("2.00");
    private static final BigDecimal GST_RATE = new BigDecimal("0.05");
    private final OrderRepository orders;

    public SellerDashboardService(OrderRepository orders) {
        this.orders = orders;
    }

    @Transactional(readOnly = true)
    public SellerDashboardResponse dashboard() {
        List<Order> allOrders = orders.findAllByOrderByCreatedAtDesc();
        List<Order> paidOrders = allOrders.stream()
                .filter(order -> "PAID".equalsIgnoreCase(order.getStatus())
                        || "DELIVERED".equalsIgnoreCase(order.getStatus()))
                .toList();

        LocalDate today = LocalDate.now();
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime nextDay = dayStart.plusDays(1);

        BigDecimal todayTurnover = sum(paidOrders, dayStart, nextDay);
        BigDecimal monthlyTurnover = sum(paidOrders, monthStart, null);
        BigDecimal totalTurnover = sum(paidOrders, null, null);

        List<SellerOrder> sellerOrders = allOrders.stream().map(order -> {
            BigDecimal subtotal = order.getItems().stream()
                    .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .filter(java.util.Objects::nonNull)
                    .reduce(BigDecimal.ZERO, (left, right) -> left.add(right))
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal gst = subtotal.multiply(GST_RATE).setScale(2, RoundingMode.HALF_UP);
            return new SellerOrder(
                order.getOrderId(),
                order.getStatus(),
                order.getTotalAmount(),
                subtotal,
                DELIVERY_CHARGE,
                CARRYING_CHARGE,
                gst,
                order.getCreatedAt(),
                order.getScheduledFor(),
                new Customer(order.getUser().getUserId(), order.getUser().getName(), order.getUser().getEmail()),
                order.getDeliveryName(),
                order.getAddress(),
                order.getPhone(),
                order.getItems().stream().map(item -> new OrderLine(
                        item.getProduct().getProductId(),
                        item.getProduct().getProductName(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                )).toList());
        }).toList();

        return new SellerDashboardResponse(
                new Summary(paidOrders.size(), allOrders.size(), todayTurnover, monthlyTurnover, totalTurnover),
                sellerOrders);
    }

    private BigDecimal sum(List<Order> orders, LocalDateTime start, LocalDateTime end) {
        BigDecimal total = BigDecimal.ZERO;
        for (Order order : orders) {
            if (start != null && order.getCreatedAt() != null && order.getCreatedAt().isBefore(start)) continue;
            if (end != null && order.getCreatedAt() != null && !order.getCreatedAt().isBefore(end)) continue;
            if (order.getCreatedAt() == null && start != null) continue;
            if (order.getTotalAmount() != null) {
                total = total.add(order.getTotalAmount());
            }
        }
        return total;
    }
}
