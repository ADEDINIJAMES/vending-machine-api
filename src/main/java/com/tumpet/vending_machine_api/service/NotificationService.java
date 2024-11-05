package com.tumpet.vending_machine_api.service;

import com.tumpet.vending_machine_api.dto.ProductNotification;
import org.springframework.mail.javamail.JavaMailSender;

public interface NotificationService {
   void sendNotification(String to, String subject, String body) throws InterruptedException;
}
