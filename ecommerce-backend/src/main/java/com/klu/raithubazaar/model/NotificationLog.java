package com.klu.raithubazaar.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_logs")
public class NotificationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;
    @Column(nullable = false)
    private String recipient;
    @Column(nullable = false)
    private String subject;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status = NotificationStatus.PENDING;
    @Column(length = 2000)
    private String errorMessage;
    private Long orderId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() { updatedAt = LocalDateTime.now(); }

    public NotificationLog() {}
    public NotificationLog(NotificationType type, String recipient, String subject, Long orderId) {
        this.type = type;
        this.recipient = recipient;
        this.subject = subject;
        this.orderId = orderId;
    }

    public Long getId() { return id; }
    public NotificationType getType() { return type; }
    public String getRecipient() { return recipient; }
    public String getSubject() { return subject; }
    public NotificationStatus getStatus() { return status; }
    public void setStatus(NotificationStatus status) { this.status = status; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Long getOrderId() { return orderId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
