package com.klu.raithubazaar.repository;

import com.klu.raithubazaar.model.NotificationLog;
import com.klu.raithubazaar.model.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    Optional<NotificationLog> findByTypeAndRecipientIgnoreCase(NotificationType type, String recipient);
    Optional<NotificationLog> findByTypeAndOrderId(NotificationType type, Long orderId);
}
