package com.klu.raithubazaar.dto;

public record SupportComplaintRequest(Long orderId, String category, String subject, String message) {}
