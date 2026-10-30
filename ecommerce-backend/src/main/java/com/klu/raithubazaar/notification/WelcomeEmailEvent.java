package com.klu.raithubazaar.notification;

public record WelcomeEmailEvent(Long userId, String email, String name) {}
