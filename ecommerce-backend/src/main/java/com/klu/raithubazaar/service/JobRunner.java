package com.klu.raithubazaar.service;

import com.klu.raithubazaar.model.BackgroundJob;
import com.klu.raithubazaar.repository.BackgroundJobRepository;
import com.klu.raithubazaar.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class JobRunner {
    private final BackgroundJobRepository jobs;
    private final OrderRepository orders;
    private final JobNotificationService notifier;
    private final JavaMailSender mailSender;
    private final String mailFrom;

    public JobRunner(BackgroundJobRepository jobs, OrderRepository orders, JobNotificationService notifier,
                    JavaMailSender mailSender, @Value("${app.mail.from:}") String mailFrom) {
        this.jobs = jobs;
        this.orders = orders;
        this.notifier = notifier;
        this.mailSender = mailSender;
        this.mailFrom = mailFrom;
    }

    @Async("taskExecutor")
    public void execute(Long jobId) {
        BackgroundJob job = jobs.findById(jobId).orElse(null);
        if (job == null) return;
        try {
            job.setStatus("PROCESSING");
            job.setMessage("Working on your " + job.getJobType().toLowerCase() + " task");
            publish(job);
            switch (job.getJobType()) {
                case "EMAIL" -> sendOrderEmail(job.getOrderId());
                case "INVOICE" -> Thread.sleep(350);
                case "NOTIFICATION" -> Thread.sleep(200);
                default -> throw new IllegalStateException("Unknown background task: " + job.getJobType());
            }
            job.setStatus("COMPLETED");
            job.setMessage(switch (job.getJobType()) {
                case "EMAIL" -> "Order confirmation email sent";
                case "INVOICE" -> "Invoice prepared";
                default -> "Order notification delivered";
            });
        } catch (Exception exception) {
            job.setStatus("FAILED");
            job.setMessage(exception.getMessage() == null ? "Task could not be completed" : exception.getMessage());
        }
        publish(job);
    }

    private void sendOrderEmail(Long orderId) {
        var order = orders.findById(orderId).orElseThrow();
        if (mailFrom == null || mailFrom.isBlank()) {
            throw new IllegalStateException("Email is not configured. Set MAIL_USERNAME and MAIL_PASSWORD.");
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(order.getUser().getEmail());
        message.setSubject("Your Raithu Bazaar order is confirmed");
        message.setText("Namaste " + order.getUser().getName() + ",\n\n"
                + "Your order #" + orderId + " is confirmed. Total: ₹" + order.getTotalAmount()
                + "\n\nThank you for supporting local farmers!\nRaithu Bazaar");
        mailSender.send(message);
    }

    private void publish(BackgroundJob job) {
        jobs.save(job);
        notifier.publish(job);
    }
}
