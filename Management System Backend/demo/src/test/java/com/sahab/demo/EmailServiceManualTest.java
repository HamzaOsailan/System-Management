package com.sahab.demo;

import com.sahab.demo.modules.infrastructure.email.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

class EmailServiceManualTest {

    private JavaMailSenderImpl createSender(
            String host, String username, String password) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(587);
        sender.setUsername(username);
        sender.setPassword(password);
        sender.setProtocol("smtp");

        Properties properties = sender.getJavaMailProperties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.starttls.required", "true");
        properties.put("mail.smtp.connectiontimeout", "10000");
        properties.put("mail.smtp.timeout", "10000");
        properties.put("mail.smtp.writetimeout", "10000");
        return sender;
    }

    @Test
    void sendDualProviderTestEmail() {
        String gmailUser = System.getenv("MAIL_USERNAME");
        String gmailPassword = System.getenv("MAIL_PASSWORD");
        String hotmailUser = System.getenv("HOTMAIL_USERNAME");
        String hotmailPassword = System.getenv("HOTMAIL_PASSWORD");

        if (gmailUser == null || gmailUser.isBlank()
                || gmailPassword == null || gmailPassword.isBlank()
                || hotmailUser == null || hotmailUser.isBlank()
                || hotmailPassword == null || hotmailPassword.isBlank()) {
            throw new IllegalStateException(
                    "SMTP credentials are missing from the test process.");
        }

        EmailService service = new EmailService(
                createSender("smtp.gmail.com", gmailUser, gmailPassword),
                createSender("smtp-mail.outlook.com", hotmailUser, hotmailPassword)
        );

        service.sendRequestDecisionEmail(
                hotmailUser,
                "SMTP Test",
                0L,
                "Dual SMTP connectivity test",
                "APPROVED",
                "This is a manually authorized SMTP connectivity test."
        );
    }
}