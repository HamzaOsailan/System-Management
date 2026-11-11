package com.sahab.demo.modules.infrastructure.email.event;

public record RequestDecisionEmailEvent(
        String toEmail,
        String userName,
        Long requestId,
        String requestTitle,
        String decision,
        String comment
) {
}