package com.klu.raithubazaar.controller;

import com.klu.raithubazaar.dto.AuthRequest;
import com.klu.raithubazaar.dto.AuthResponse;
import com.klu.raithubazaar.model.Role;
import com.klu.raithubazaar.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) { this.authService = authService; }
    @PostMapping("/signup/customer")
    public AuthResponse customerSignup(@RequestBody AuthRequest request) {
        return authService.register(request, Role.CUSTOMER);
    }
    @PostMapping("/signup/seller")
    public AuthResponse sellerSignup(@RequestBody AuthRequest request) {
        return authService.register(request, Role.SELLER);
    }
    @PostMapping("/signin")
    public AuthResponse signIn(@RequestBody AuthRequest request) { return authService.signIn(request); }
}
