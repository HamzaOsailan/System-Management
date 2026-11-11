package com.sahab.demo.modules.requests.scheduler;

import com.sahab.demo.entity.Request;
import com.sahab.demo.entity.User;
import com.sahab.demo.enums.RequestStatus;
import com.sahab.demo.modules.requests.repository.RequestRepository;
import com.sahab.demo.modules.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SlaScheduler {

    private final RequestRepository requestRepository;

    private final NotificationService notificationService;

    // =========================================================
    // RUN EVERY 30 SECONDS
    // =========================================================

    @Scheduled(fixedRate = 30000)
    @Transactional
    public void checkSlaRequests() {

        LocalDateTime now =
                LocalDateTime.now();

        System.out.println(
                "========== SLA SCHEDULER =========="
        );

        System.out.println(
                "Checking SLA at: " + now
        );

        checkWarnings(now);

        checkBreaches(now);

        System.out.println(
                "==================================="
        );
    }

    // =========================================================
    // SLA WARNING
    // =========================================================

    private void checkWarnings(
            LocalDateTime now
    ) {

        List<Request> requests =
                requestRepository
                        .findRequestsReachingSlaWarning(
                                RequestStatus.PENDING,
                                now
                        );

        if (requests.isEmpty()) {

            System.out.println(
                    "No SLA warnings."
            );

            return;
        }

        for (Request request : requests) {

            // -------------------------------------------------
            // SAFETY CHECK
            //
            // Only PENDING requests can have an active SLA.
            // -------------------------------------------------

            if (
                    request.getStatus()
                            != RequestStatus.PENDING
            ) {
                continue;
            }

            // -------------------------------------------------
            // WARNING ALREADY SENT
            // -------------------------------------------------

            if (Boolean.TRUE.equals(
                    request.getSlaWarningSent()
            )) {
                continue;
            }

            User requestUser =
                    request.getUser();

            if (requestUser != null) {

                String message =
                        "⚠️ SLA Warning: "
                                + "Request #"
                                + request.getId()
                                + " is approaching "
                                + "its SLA deadline.";

                notificationService.createNotification(
                        requestUser,
                        message
                );
            }

            request.setSlaWarningSent(
                    true
            );

            request.setUpdatedAt(
                    LocalDateTime.now()
            );

            requestRepository.save(
                    request
            );

            System.out.println(
                    "SLA WARNING SENT - Request ID: "
                            + request.getId()
            );
        }
    }

    // =========================================================
    // SLA BREACH + ESCALATION
    // =========================================================

    private void checkBreaches(
            LocalDateTime now
    ) {

        List<Request> requests =
                requestRepository
                        .findRequestsPastSlaDeadline(
                                RequestStatus.PENDING,
                                now
                        );

        if (requests.isEmpty()) {

            System.out.println(
                    "No SLA breaches."
            );

            return;
        }

        for (Request request : requests) {

            // -------------------------------------------------
            // SAFETY CHECK
            //
            // Only PENDING requests can be breached.
            // -------------------------------------------------

            if (
                    request.getStatus()
                            != RequestStatus.PENDING
            ) {
                continue;
            }

            // -------------------------------------------------
            // BREACH
            // -------------------------------------------------

            if (!Boolean.TRUE.equals(
                    request.getSlaBreached()
            )) {

                request.setSlaBreached(
                        true
                );

                request.setUpdatedAt(
                        LocalDateTime.now()
                );

                requestRepository.save(
                        request
                );

                System.out.println(
                        "SLA BREACHED - Request ID: "
                                + request.getId()
                );

                // -------------------------------------------------
                // NOTIFY REQUEST OWNER
                // -------------------------------------------------

                User requestUser =
                        request.getUser();

                if (requestUser != null) {

                    String message =
                            "🚨 SLA Breached: "
                                    + "Request #"
                                    + request.getId()
                                    + " has exceeded "
                                    + "its SLA deadline.";

                    notificationService.createNotification(
                            requestUser,
                            message
                    );
                }
            }

            // -------------------------------------------------
            // ESCALATION
            // -------------------------------------------------

            if (!Boolean.TRUE.equals(
                    request.getSlaEscalated()
            )) {

                escalateRequest(
                        request
                );
            }
        }
    }

    // =========================================================
    // ESCALATE TO MANAGER
    // =========================================================

    private void escalateRequest(
            Request request
    ) {

        // -----------------------------------------------------
        // FINAL SAFETY CHECK
        // -----------------------------------------------------

        if (
                request.getStatus()
                        != RequestStatus.PENDING
        ) {

            return;
        }

        User manager =
                request.getManager();

        if (manager != null) {

            String message =
                    "🚨 SLA Escalation: "
                            + "Request #"
                            + request.getId()
                            + " has exceeded "
                            + "its SLA deadline and "
                            + "requires your attention.";

            notificationService.createNotification(
                    manager,
                    message
            );

            System.out.println(
                    "SLA ESCALATED TO MANAGER - "
                            + "Request ID: "
                            + request.getId()
                            + " Manager: "
                            + manager.getName()
            );
        }

        request.setSlaEscalated(
                true
        );

        request.setUpdatedAt(
                LocalDateTime.now()
        );

        requestRepository.save(
                request
        );
    }
}