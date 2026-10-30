package com.klu.raithubazaar.service;

import com.klu.raithubazaar.dto.OrderRequest;
import com.klu.raithubazaar.model.Order;
import com.klu.raithubazaar.model.OrderItem;
import com.klu.raithubazaar.model.User;
import com.klu.raithubazaar.repository.OrderRepository;
import com.klu.raithubazaar.repository.ProductRepository;
import com.klu.raithubazaar.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

@Service
public class OrderService {
    private static final BigDecimal DELIVERY_CHARGE = new BigDecimal("49.00");
    private static final BigDecimal CARRYING_CHARGE = new BigDecimal("2.00");
    private static final BigDecimal GST_RATE = new BigDecimal("0.05");
    private final UserRepository users;
    private final ProductRepository products;
    private final OrderRepository orders;

    public OrderService(UserRepository users, ProductRepository products, OrderRepository orders) {
        this.users = users;
        this.products = products;
        this.orders = orders;
    }

    @Transactional
    public Order create(OrderRequest request, String email) {
        if (request.items() == null || request.items().isEmpty()
                || request.deliveryName() == null || request.deliveryName().isBlank()
                || request.phone() == null || !request.phone().matches("^[0-9]{10}$")
                || request.address() == null || request.address().trim().length() < 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Add products and valid delivery name, 10-digit phone, and address");
        }
        User user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));
        Order order = new Order();
        order.setUser(user);
        order.setDeliveryName(request.deliveryName().trim());
        order.setPhone(request.phone());
        order.setAddress(request.address().trim());
        if (request.scheduledFor() != null) {
            if (request.scheduledFor().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Scheduled delivery must be at least 2 hours from now");
            }
            order.setScheduledFor(request.scheduledFor());
        }
        order.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        BigDecimal total = BigDecimal.ZERO;
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (var line : request.items()) {
            if (line.productId() == null || line.quantity() == null || line.quantity() < 1 || line.quantity() > 100) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Each cart quantity must be between 1 and 100");
            }
            long productId = line.productId();
            int quantity = line.quantity();
            Integer existingQuantity = quantities.get(productId);
            int totalQuantity = existingQuantity == null ? quantity : existingQuantity + quantity;
            quantities.put(productId, totalQuantity);
            if (totalQuantity > 100) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Each product quantity cannot exceed 100");
            }
        }
        for (var line : quantities.entrySet()) {
            var product = products.findByIdForUpdate(line.getKey())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "A cart product no longer exists"));
            if (product.getStock() == null || product.getStock() < line.getValue()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        product.getProductName() + " does not have enough stock");
            }
            BigDecimal unitPrice = product.getPrice();
            total = total.add(unitPrice.multiply(BigDecimal.valueOf(line.getValue())));
            product.setStock(product.getStock() - line.getValue());
            product.setStatus(product.getStock() > 0 ? "AVAILABLE" : "OUT_OF_STOCK");
            order.addItem(new OrderItem(product, line.getValue(), unitPrice));
        }
        BigDecimal gst = total.multiply(GST_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal grandTotal = total.add(DELIVERY_CHARGE).add(CARRYING_CHARGE).add(gst)
                .setScale(2, RoundingMode.HALF_UP);
        order.setTotalAmount(grandTotal);
        return orders.save(order);
    }

    @Transactional(readOnly = true)
    public List<Order> myOrders(String email) {
        User user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));
        return orders.findAllByUserUserIdOrderByCreatedAtDesc(user.getUserId());
    }
}
