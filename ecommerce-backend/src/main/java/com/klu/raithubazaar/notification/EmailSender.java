package com.klu.raithubazaar.notification;

import com.klu.raithubazaar.model.NotificationStatus;
import com.klu.raithubazaar.repository.NotificationLogRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailSender {
    private final JavaMailSender mailSender;
    private final NotificationLogRepository logs;
    private final String fromAddress;

    public EmailSender(JavaMailSender mailSender, NotificationLogRepository logs,
                       @Value("${app.mail.from}") String fromAddress) {
        this.mailSender = mailSender;
        this.logs = logs;
        this.fromAddress = fromAddress;
    }

    @Async("taskExecutor")
    public void send(Long notificationId, String html, String text) {
        send(notificationId, html, text, null);
    }

    @Async("taskExecutor")
    public void send(Long notificationId, String html, String text, String replyTo) {
        try {
            var log = logs.findById(notificationId).orElseThrow();
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(log.getRecipient());
            helper.setSubject(log.getSubject());
            if (replyTo != null && !replyTo.isBlank()) {
                helper.setReplyTo(replyTo);
            }
            helper.setText(text, html);
            mailSender.send(message);
            log.setStatus(NotificationStatus.SENT);
            log.setErrorMessage(null);
            logs.save(log);
        } catch (MessagingException | RuntimeException exception) {
            logs.findById(notificationId).ifPresent(log -> {
                log.setStatus(NotificationStatus.FAILED);
                log.setErrorMessage(errorText(exception));
                logs.save(log);
            });
        }
    }

    private String errorText(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) message = exception.getClass().getSimpleName();
        return message.length() > 2000 ? message.substring(0, 2000) : message;
    }
}
