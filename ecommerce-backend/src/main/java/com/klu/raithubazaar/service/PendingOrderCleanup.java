package com.klu.raithubazaar.service;

import com.klu.raithubazaar.repository.OrderRepository;
import com.klu.raithubazaar.repository.PaymentRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Component
public class PendingOrderCleanup {
    private final OrderRepository orders;
    private final PaymentRepository payments;
    public PendingOrderCleanup(OrderRepository orders, PaymentRepository payments) {
        this.orders = orders;
        this.payments = payments;
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void expireOldOrders() {
        orders.findByStatusAndCreatedAtBefore("PENDING", LocalDateTime.now().minusMinutes(30)).forEach(order -> {
            order.getItems().forEach(item -> {
                var product = item.getProduct();
                product.setStock(product.getStock() + item.getQuantity());
                product.setStatus("AVAILABLE");
            });
            order.setStatus("EXPIRED");
            payments.findByOrderOrderId(order.getOrderId()).ifPresent(payment -> {
                payment.setStatus("EXPIRED");
                payments.save(payment);
            });
        });
    }
}
