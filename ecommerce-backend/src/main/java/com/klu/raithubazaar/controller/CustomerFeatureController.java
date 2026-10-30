package com.klu.raithubazaar.controller;

import com.klu.raithubazaar.dto.OrderRatingRequest;
import com.klu.raithubazaar.dto.SupportComplaintRequest;
import com.klu.raithubazaar.model.OrderRating;
import com.klu.raithubazaar.model.SupportComplaint;
import com.klu.raithubazaar.service.CustomerFeatureService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
public class CustomerFeatureController {
    private final CustomerFeatureService service;

    public CustomerFeatureController(CustomerFeatureService service) { this.service = service; }

    @PostMapping("/orders/{orderId}/ratings")
    public OrderRating rate(@PathVariable Long orderId, @RequestBody OrderRatingRequest request,
                            Authentication authentication) {
        return service.rate(orderId, request, authentication.getName());
    }

    @PostMapping("/complaints")
    public SupportComplaint complain(@RequestBody SupportComplaintRequest request,
                                     Authentication authentication) {
        return service.complain(request, authentication.getName());
    }
}
