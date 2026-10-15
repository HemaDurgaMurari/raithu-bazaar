package com.klu.raithubazaar.repository;

import com.klu.raithubazaar.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    boolean existsByProductProductId(Long productId);
}
