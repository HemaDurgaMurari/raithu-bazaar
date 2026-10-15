package com.klu.raithubazaar.repository;

import com.klu.raithubazaar.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderOrderId(Long orderId);
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);
}
