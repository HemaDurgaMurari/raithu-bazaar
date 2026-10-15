package com.klu.raithubazaar.repository;

import com.klu.raithubazaar.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatusAndCreatedAtBefore(String status, LocalDateTime createdAt);
    List<Order> findAllByOrderByCreatedAtDesc();
}
