package com.klu.raithubazaar.dto;

import com.klu.raithubazaar.model.Role;

public record AuthResponse(Long userId, String name, String email, Role role, String token) {}
