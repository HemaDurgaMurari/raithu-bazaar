package com.klu.raithubazaar.repository;

import com.klu.raithubazaar.model.OrderRating;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OrderRatingRepository extends JpaRepository<OrderRating, Long> {
    Optional<OrderRating> findByOrderOrderIdAndProductProductId(Long orderId, Long productId);
}
