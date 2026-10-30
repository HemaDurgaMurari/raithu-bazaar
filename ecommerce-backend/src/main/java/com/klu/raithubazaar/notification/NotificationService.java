package com.klu.raithubazaar.notification;

import com.klu.raithubazaar.model.*;
import com.klu.raithubazaar.repository.NotificationLogRepository;
import com.klu.raithubazaar.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class NotificationService {
    private final NotificationLogRepository logs;
    private final OrderRepository orders;
    private final EmailSender sender;
    private final String frontendUrl;

    public NotificationService(NotificationLogRepository logs, OrderRepository orders, EmailSender sender,
                               @Value("${app.frontend-url}") String frontendUrl) {
        this.logs = logs;
        this.orders = orders;
        this.sender = sender;
        this.frontendUrl = StringUtils.trimTrailingCharacter(frontendUrl, '/');
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onWelcome(WelcomeEmailEvent event) {
        if (logs.findByTypeAndRecipientIgnoreCase(NotificationType.WELCOME, event.email()).isPresent()) return;
        String subject = "Welcome to Raithu Bazaar";
        var log = logs.saveAndFlush(new NotificationLog(NotificationType.WELCOME, event.email(), subject, null));
        String safeName = escape(event.name());
        String url = escape(frontendUrl);
        String html = wrapper("Welcome to Raithu Bazaar",
                "<h2>Welcome, " + safeName + "!</h2>"
                + "<p>Your Raithu Bazaar account is ready. Browse fresh farm products and place your next order.</p>"
                + button(url + "/", "Visit Raithu Bazaar"));
        sender.send(log.getId(), html, "Welcome, " + event.name() + "! Visit " + frontendUrl + "/");
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onOrderConfirmed(OrderConfirmationEmailEvent event) {
        if (logs.findByTypeAndOrderId(NotificationType.ORDER_CONFIRMATION, event.orderId()).isPresent()) return;
        Order order = orders.findById(event.orderId()).orElse(null);
        if (order == null || !"PAID".equals(order.getStatus())) return;
        User user = order.getUser();
        String receiptNumber = receiptNumber(order.getOrderId());
        String subject = "Raithu Bazaar receipt - " + receiptNumber;
        var log = logs.saveAndFlush(new NotificationLog(
                NotificationType.ORDER_CONFIRMATION, user.getEmail(), subject, order.getOrderId()));

        StringBuilder rows = new StringBuilder();
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItem item : order.getItems()) {
            BigDecimal lineTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(lineTotal);
            rows.append("<tr><td style=\"padding:14px 0;border-bottom:1px solid #e8ece9\">")
                    .append(escape(item.getProduct().getProductName()))
                    .append("<br><span style=\"color:#717770;font-size:12px\">Quantity: ")
                    .append(item.getQuantity()).append("</span></td>")
                    .append("<td style=\"padding:14px 0;border-bottom:1px solid #e8ece9;text-align:right\">")
                    .append(money(lineTotal))
                    .append("</td></tr>");
        }
        String date = order.getCreatedAt() == null ? ""
                : order.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
        String safeName = escape(order.getDeliveryName());
        String safeAddress = escape(order.getAddress()).replace("\n", "<br>");
        String url = escape(frontendUrl + "/orders/" + order.getOrderId());
        String html = receiptTemplate(receiptNumber, date, escape(user.getName()), rows.toString(),
                subtotal, order.getTotalAmount(), safeName, safeAddress, url);
        StringBuilder text = new StringBuilder("Order #").append(order.getOrderId()).append(" confirmed.\n");
        text.append("Total: ").append(money(order.getTotalAmount())).append("\n");
        text.append("Payment: PAID\nView: ").append(frontendUrl).append("/orders/").append(order.getOrderId());
        sender.send(log.getId(), html, text.toString());
    }

    public void sendTest(String recipient) {
        String subject = "Raithu Bazaar test email";
        var log = logs.saveAndFlush(new NotificationLog(NotificationType.TEST, recipient, subject, null));
        sender.send(log.getId(),
                wrapper("Email configuration works", "<p>Your Raithu Bazaar Gmail settings are working.</p>"),
                "Your Raithu Bazaar Gmail settings are working.");
    }

    public void sendComplaint(String supportEmail, String customerEmail, String customerName,
                              Long orderId, String category, String subject, String message) {
        String safeOrder = orderId == null ? "Not linked" : String.valueOf(orderId);
        String mailSubject = "[Raithu Bazaar Complaint " + safeOrder + "] " + subject;
        var log = logs.saveAndFlush(new NotificationLog(
                NotificationType.COMPLAINT, supportEmail, mailSubject,
                orderId));
        String html = wrapper("New customer complaint",
                "<p><strong>Customer:</strong> " + escape(customerName) + " (" + escape(customerEmail) + ")<br>"
                + "<strong>Category:</strong> " + escape(category) + "<br>"
                + "<strong>Order ID:</strong> " + escape(safeOrder) + "</p>"
                + "<h2>" + escape(subject) + "</h2><p>" + escape(message).replace("\n", "<br>") + "</p>");
        String text = "Customer: " + customerName + " (" + customerEmail + ")\n"
                + "Category: " + category + "\nOrder: " + safeOrder + "\n\n"
                + subject + "\n" + message;
        sender.send(log.getId(), html, text, customerEmail);
    }

    private String wrapper(String title, String body) {
        return "<div style=\"max-width:650px;margin:auto;font-family:Arial,sans-serif;color:#263238\">"
                + "<div style=\"background:#2e7d32;color:white;padding:18px;font-size:22px;font-weight:bold\">Raithu Bazaar</div>"
                + "<div style=\"padding:24px;background:#ffffff;border:1px solid #e0e0e0\">"
                + "<h1 style=\"font-size:24px;color:#2e7d32\">" + escape(title) + "</h1>" + body
                + "</div><p style=\"font-size:12px;color:#777\">Fresh products from local farmers.</p></div>";
    }

    private String button(String url, String label) {
        return "<p style=\"margin-top:24px\"><a href=\"" + url
                + "\" style=\"background:#f9a825;color:#263238;padding:12px 18px;text-decoration:none;border-radius:4px;font-weight:bold\">"
                + escape(label) + "</a></p>";
    }

    private String receiptTemplate(String receiptNumber, String date, String customerName, String rows,
                                   BigDecimal subtotal, BigDecimal total, String deliveryName,
                                   String deliveryAddress, String orderUrl) {
        return "<div style=\"margin:0;padding:24px;background:#f5f7f4;font-family:Arial,sans-serif;color:#202124\">"
                + "<div style=\"max-width:620px;margin:auto;background:#fff;border-radius:3px;overflow:hidden;box-shadow:0 1px 4px rgba(0,0,0,.12)\">"
                + "<div style=\"padding:22px 28px;border-bottom:1px solid #e4e7e3\"><span style=\"color:#1b6849;font-size:21px;font-weight:700\">● Raithu Bazaar</span></div>"
                + "<div style=\"padding:34px 28px\">"
                + "<h1 style=\"margin:0 0 8px;font-size:27px;font-weight:500;color:#202124\">Thank you</h1>"
                + "<p style=\"margin:0 0 28px;color:#5f6368;line-height:1.6\">Thank you for your purchase from Raithu Bazaar.</p>"
                + "<table role=\"presentation\" style=\"width:100%;border-collapse:collapse;margin-bottom:28px;font-size:13px\">"
                + "<tr><td style=\"padding:7px 0;color:#70757a\">Order ID</td><td style=\"padding:7px 0;text-align:right;color:#202124\">" + extractOrderId(receiptNumber) + "</td></tr>"
                + "<tr><td style=\"padding:7px 0;color:#70757a\">Order number</td><td style=\"padding:7px 0;text-align:right;color:#202124;font-weight:bold\">" + receiptNumber + "</td></tr>"
                + "<tr><td style=\"padding:7px 0;color:#70757a\">Order date</td><td style=\"padding:7px 0;text-align:right;color:#202124\">" + date + "</td></tr>"
                + "<tr><td style=\"padding:7px 0;color:#70757a\">Customer</td><td style=\"padding:7px 0;text-align:right;color:#202124\">" + customerName + "</td></tr></table>"
                + "<table role=\"presentation\" style=\"width:100%;border-collapse:collapse;font-size:14px\">"
                + "<tr><td style=\"padding-bottom:12px;color:#70757a;border-bottom:1px solid #ccd0cd\">Item</td><td style=\"padding-bottom:12px;color:#70757a;border-bottom:1px solid #ccd0cd;text-align:right\">Price</td></tr>"
                + rows + "</table>"
                + "<table role=\"presentation\" style=\"width:260px;margin:22px 0 22px auto;font-size:14px\">"
                + "<tr><td style=\"padding:5px;color:#70757a\">Subtotal</td><td style=\"padding:5px;text-align:right\">" + money(subtotal) + "</td></tr>"
                + "<tr><td style=\"padding:5px;color:#70757a\">Tax/GST</td><td style=\"padding:5px;text-align:right\">Included</td></tr>"
                + "<tr><td style=\"padding:10px 5px;border-top:1px solid #ccd0cd;font-weight:bold;font-size:16px\">Total</td><td style=\"padding:10px 5px;border-top:1px solid #ccd0cd;text-align:right;font-weight:bold;font-size:16px\">" + money(total) + "</td></tr>"
                + "</table>"
                + "<div style=\"padding-top:18px;border-top:1px solid #e4e7e3;font-size:13px;color:#5f6368;line-height:1.7\">"
                + "<b style=\"color:#202124\">Payment method</b><br>Razorpay &nbsp;·&nbsp; PAID<br>"
                + "<b style=\"color:#202124\">Delivery to</b><br>" + deliveryName + "<br>" + deliveryAddress
                + "<br><a href=\"" + orderUrl + "\" style=\"display:inline-block;margin-top:16px;color:#1a73e8;text-decoration:none;font-weight:bold\">View my order</a></div>"
                + "</div>"
                + "<div style=\"padding:20px 28px;background:#f8f9f8;color:#70757a;font-size:12px;line-height:1.7\">"
                + "Need help? Contact <a style=\"color:#1b6849\" href=\"mailto:no.reply.raithubazaar@gmail.com\">Raithu Bazaar Support</a>.<br>"
                + "<b>Please do not reply to this message.</b></div>"
                + "</div></div>";
    }

    private String receiptNumber(Long orderId) {
        return "RB-2728-0406-" + String.format("%04d", orderId);
    }

    private String extractOrderId(String receiptNumber) {
        String[] parts = receiptNumber.split("-");
        return parts.length == 4 ? String.valueOf(Integer.parseInt(parts[3])) : "";
    }

    private String escape(String value) {
        return HtmlUtils.htmlEscape(value == null ? "" : value);
    }

    private String money(BigDecimal amount) {
        if (amount == null) return "₹0.00";
        return String.format(Locale.ENGLISH, "₹%,.2f", amount);
    }
}
