package com.klu.raithubazaar.repository;

import com.klu.raithubazaar.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatusAndCreatedAtBefore(String status, LocalDateTime createdAt);
    @EntityGraph(attributePaths = {"user", "items", "items.product"})
    List<Order> findAllByOrderByCreatedAtDesc();
    @EntityGraph(attributePaths = {"user", "items", "items.product"})
    List<Order> findAllByUserUserIdOrderByCreatedAtDesc(Long userId);
}
