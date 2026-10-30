package com.klu.raithubazaar.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_ratings", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"order_id", "product_id"})
})
public class OrderRating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id")
    private Order order;
    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id")
    private Product product;
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;
    @Column(nullable = false)
    private Integer rating;
    @Column(length = 1000)
    private String review;
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() { createdAt = LocalDateTime.now(); }
    public OrderRating() {}
    public Long getId() { return id; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getReview() { return review; }
    public void setReview(String review) { this.review = review; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
