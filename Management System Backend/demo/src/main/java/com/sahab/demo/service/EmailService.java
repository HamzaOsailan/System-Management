package com.sahab.demo.service;

import lombok.RequiredArgsConstructor;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendPasswordResetEmail(String toEmail, String resetLink) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(toEmail);
        message.setSubject("Reset your password");
        message.setText(
                "We received a request to reset your password.\n\n" +
                        "Click the link below to choose a new one:\n" +
                        resetLink + "\n\n" +
                        "This link expires in 30 minutes. If you didn't request " +
                        "this, you can safely ignore this email."
        );

        mailSender.send(message);
    }
}