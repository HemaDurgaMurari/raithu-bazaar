package com.klu.raithubazaar.service;

import com.klu.raithubazaar.model.BackgroundJob;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class JobNotificationService {
    private final SimpMessagingTemplate messaging;
    public JobNotificationService(SimpMessagingTemplate messaging) { this.messaging = messaging; }
    public void publish(BackgroundJob job) { messaging.convertAndSend("/topic/jobs", job); }
}
