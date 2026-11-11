package com.sahab.demo.modules.infrastructure.email.service;

import com.sahab.demo.modules.infrastructure.email.event.RequestNotificationEmailEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class RequestNotificationEmailListener {

    private static final Logger log =
            LoggerFactory.getLogger(RequestNotificationEmailListener.class);

    private final EmailService emailService;

    public RequestNotificationEmailListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(RequestNotificationEmailEvent event) {

        if (event == null || event.toEmail() == null || event.toEmail().isBlank()) {
            return;
        }

        try {
            emailService.sendNotificationEmail(
                    event.toEmail(),
                    event.recipientName(),
                    event.subject(),
                    event.body()
            );

            log.info("Request notification email processed successfully.");
        } catch (Exception ex) {
            log.error(
                    "Request notification email failed. Error type: {}",
                    ex.getClass().getSimpleName()
            );
        }
    }
}