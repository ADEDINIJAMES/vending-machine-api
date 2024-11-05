package com.tumpet.vending_machine_api.serviceImp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tumpet.vending_machine_api.dto.ProductNotification;
import com.tumpet.vending_machine_api.service.NotificationService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImplementation implements NotificationService {
    private final JavaMailSender mailSender;

    @Value("${Queues}")
    private String queueName;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @RabbitListener(queues = "${Queues}")
    public void receiveNotification(String jsonPayload) {
        try {
            // Deserialize the JSON payload into a ProductNotification object
            ProductNotification productNotification = new ObjectMapper().readValue(jsonPayload, ProductNotification.class);
            processAndSendNotification(productNotification);
        } catch (Exception e) {
            log.error("Error processing notification message: {}", e.getMessage());
        }
    }

    public void sendNotification(String to, String subject, String body) {
        int attempts = 0;
        int maxAttempts = 3;
        while (attempts < maxAttempts) {
            try {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
                mimeMessageHelper.setFrom(fromEmail);
                mimeMessageHelper.setTo(to);
                mimeMessageHelper.setSubject(subject);
                mimeMessageHelper.setText(body);

                mailSender.send(mimeMessage);
                log.info("MESSAGE SENT SUCCESSFULLY to {}", to);
                break;
            } catch (Exception e) {
                attempts++;
                if (attempts >= maxAttempts) {
                    log.error("Failed to send email after {} attempts: {}", attempts, e.getMessage());
                } else {
                    log.warn("Retrying email send (attempt {} of {})", attempts + 1, maxAttempts);
                    try {
                        Thread.sleep(3000); // wait 3 seconds before retry
                    } catch (InterruptedException interruptedException) {
                        Thread.currentThread().interrupt(); // restore interrupt status
                        log.error("Retry interrupted: {}", interruptedException.getMessage());
                    }
                }
            }
        }
    }

    public void processAndSendNotification(ProductNotification productNotification) {
        String to = productNotification.getBuyerEmail();
        String subject = "Product Purchase Confirmation";
        String body = String.format("Thank you for your purchase of %d x %s. Total Spent: %d Naira.",
                productNotification.getQuantity(), productNotification.getProductName(), productNotification.getTotalSpent());

        sendNotification(to, subject, body);
    }
}
