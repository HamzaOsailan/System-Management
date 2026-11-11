package com.sahab.demo;

import com.sahab.demo.modules.infrastructure.email.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.sla.scheduler.enabled=false")
class EmailDeliverySmokeTest {

    @Autowired
    private EmailService emailService;

    @Test
    void sendExactlyOneTestEmail() {
        String recipient = System.getenv("MAIL_USERNAME");

        if (recipient == null || recipient.isBlank()) {
            throw new IllegalStateException("MAIL_USERNAME is missing.");
        }

        emailService.sendNotificationEmail(
                recipient,
                "Management System",
                "Management System - Email Delivery Test",
                "This is a single email delivery test from the Management System application.\n\n"
                        + "No request was created. No approval or notification workflow was triggered."
        );

        System.out.println("EMAIL_DELIVERY_TEST_COMPLETED");
    }
}