package com.klu.raithubazaar.controller;

import com.klu.raithubazaar.dto.OrderRequest;
import com.klu.raithubazaar.model.Order;
import com.klu.raithubazaar.service.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService service;
    public OrderController(OrderService service) { this.service = service; }
    @PostMapping
    public Order create(@RequestBody OrderRequest request, Authentication authentication) {
        return service.create(request, authentication.getName());
    }
}
