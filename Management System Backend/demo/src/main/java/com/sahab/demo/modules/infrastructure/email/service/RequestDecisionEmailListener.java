package com.sahab.demo.modules.infrastructure.email.service;

import com.sahab.demo.modules.infrastructure.email.event.RequestDecisionEmailEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class RequestDecisionEmailListener {

    private static final Logger log =
            LoggerFactory.getLogger(RequestDecisionEmailListener.class);

    private final EmailService emailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRequestDecision(RequestDecisionEmailEvent event) {
        try {
            emailService.sendRequestDecisionEmail(
                    event.toEmail(),
                    event.userName(),
                    event.requestId(),
                    event.requestTitle(),
                    event.decision(),
                    event.comment()
            );

            log.info("Decision email sent for requestId={}, decision={}",
                    event.requestId(), event.decision());
        } catch (Exception exception) {
            log.error("Decision email failed for requestId={}, errorType={}",
                    event.requestId(),
                    exception.getClass().getSimpleName());
        }
    }
}