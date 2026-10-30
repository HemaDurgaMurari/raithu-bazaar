package com.klu.raithubazaar.service;

import com.klu.raithubazaar.dto.OrderRatingRequest;
import com.klu.raithubazaar.dto.SupportComplaintRequest;
import com.klu.raithubazaar.model.*;
import com.klu.raithubazaar.notification.NotificationService;
import com.klu.raithubazaar.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CustomerFeatureService {
    private final UserRepository users;
    private final OrderRepository orders;
    private final ProductRepository products;
    private final OrderRatingRepository ratings;
    private final SupportComplaintRepository complaints;
    private final NotificationService notifications;
    private final String supportEmail;

    public CustomerFeatureService(UserRepository users, OrderRepository orders, ProductRepository products,
                                  OrderRatingRepository ratings, SupportComplaintRepository complaints,
                                  NotificationService notifications,
                                  @Value("${app.mail.support:}") String supportEmail) {
        this.users = users;
        this.orders = orders;
        this.products = products;
        this.ratings = ratings;
        this.complaints = complaints;
        this.notifications = notifications;
        this.supportEmail = supportEmail;
    }

    @Transactional
    public OrderRating rate(Long orderId, OrderRatingRequest request, String email) {
        User user = currentUser(email);
        Order order = ownedOrder(orderId, user);
        if (!"PAID".equals(order.getStatus()) && !"DELIVERED".equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Rate this order after payment is confirmed");
        }
        if (request.rating() == null || request.rating() < 1 || request.rating() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5 stars");
        }
        Product product = products.findById(request.productId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        boolean inOrder = order.getItems().stream()
                .anyMatch(item -> item.getProduct().getProductId().equals(product.getProductId()));
        if (!inOrder) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "This product is not in the order");

        OrderRating rating = ratings.findByOrderOrderIdAndProductProductId(orderId, product.getProductId())
                .orElse(new OrderRating());
        rating.setOrder(order);
        rating.setProduct(product);
        rating.setUser(user);
        rating.setRating(request.rating());
        rating.setReview(request.review() == null ? null : request.review().trim());
        return ratings.save(rating);
    }

    @Transactional
    public SupportComplaint complain(SupportComplaintRequest request, String email) {
        User user = currentUser(email);
        if (isBlank(request.category()) || isBlank(request.subject()) || isBlank(request.message())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category, subject, and message are required");
        }
        if (request.orderId() != null) ownedOrder(request.orderId(), user);
        SupportComplaint complaint = new SupportComplaint();
        complaint.setUser(user);
        complaint.setOrderId(request.orderId());
        complaint.setCategory(request.category().trim());
        complaint.setSubject(request.subject().trim());
        complaint.setMessage(request.message().trim());
        complaints.save(complaint);
        String recipient = supportEmail == null || supportEmail.isBlank()
                ? "no.reply.raithubazaar@gmail.com" : supportEmail.trim();
        notifications.sendComplaint(recipient, user.getEmail(), user.getName(), request.orderId(),
                complaint.getCategory(), complaint.getSubject(), complaint.getMessage());
        return complaint;
    }

    private User currentUser(String email) {
        return users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));
    }

    private Order ownedOrder(Long orderId, User user) {
        Order order = orders.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        if (!order.getUser().getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This order belongs to another account");
        }
        return order;
    }

    private boolean isBlank(String value) { return value == null || value.isBlank(); }
}
