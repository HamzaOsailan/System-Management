package com.sahab.demo.modules.infrastructure.email.event;

public record RequestNotificationEmailEvent(
        String toEmail,
        String recipientName,
        String subject,
        String body
) {
}