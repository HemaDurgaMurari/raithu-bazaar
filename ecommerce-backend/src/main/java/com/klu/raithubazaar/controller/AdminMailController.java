package com.klu.raithubazaar.controller;

import com.klu.raithubazaar.notification.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminMailController {
    private final NotificationService notifications;

    public AdminMailController(NotificationService notifications) { this.notifications = notifications; }

    @GetMapping("/test-mail")
    public ResponseEntity<String> testMail(@RequestParam String to) {
        notifications.sendTest(to.trim());
        return ResponseEntity.accepted().body("Test email queued");
    }
}
