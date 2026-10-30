package com.klu.raithubazaar.controller;

import com.klu.raithubazaar.dto.SellerDashboardResponse;
import com.klu.raithubazaar.service.SellerDashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seller")
public class SellerDashboardController {
    private final SellerDashboardService service;

    public SellerDashboardController(SellerDashboardService service) {
        this.service = service;
    }

    @GetMapping("/dashboard")
    public SellerDashboardResponse dashboard() {
        return service.dashboard();
    }
}
