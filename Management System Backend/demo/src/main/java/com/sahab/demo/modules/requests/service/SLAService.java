package com.sahab.demo.modules.requests.service;

import com.sahab.demo.modules.requests.dto.SLADashboardResponse;
import com.sahab.demo.modules.requests.dto.SLAPredictionResponse;
import com.sahab.demo.entity.Request;
import com.sahab.demo.enums.RequestStatus;
import com.sahab.demo.enums.SLAStatus;
import com.sahab.demo.modules.requests.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SLAService {

    private final RequestRepository requestRepository;

    /*
     * Fallback SLA values.
     *
     * If a department does not have an active SLA policy,
     * these values will be used.
     */
    private static final double DEFAULT_SLA_HOURS = 4.0;

    /**
     * Get SLA prediction for one request.
     *
     * SLA lifecycle:
     *
     * PENDING
     *  -> SLA is active and calculated against NOW.
     *
     * APPROVED / REJECTED
     *  -> SLA is completed and calculated against COMPLETED_AT.
     *
     * This prevents the SLA countdown from continuing
     * after the request reaches its final state.
     */
    public SLAPredictionResponse predictRequest(Request request) {

        if (request.getCreatedAt() == null) {
            throw new RuntimeException(
                    "Request creation date is missing"
            );
        }

        /*
         * Determine the SLA duration.
         *
         * The actual request deadline is preferred because
         * RequestService calculates it from the department policy
         * when the request is created.
         */
        double slaHours = getSlaHours(request);

        /*
         * -----------------------------------------------------
         * DETERMINE SLA END TIME
         * -----------------------------------------------------
         *
         * Active request:
         *     NOW
         *
         * Completed request:
         *     COMPLETED_AT
         *
         * This is the key fix.
         */
        LocalDateTime endTime;

        boolean completed =
                request.getStatus() == RequestStatus.APPROVED
                        || request.getStatus() == RequestStatus.REJECTED;

        if (completed && request.getCompletedAt() != null) {

            endTime = request.getCompletedAt();

        } else {

            endTime = LocalDateTime.now();
        }

        /*
         * -----------------------------------------------------
         * ELAPSED TIME
         * -----------------------------------------------------
         */
        long elapsedMinutes = Math.max(
                0,
                Duration.between(
                        request.getCreatedAt(),
                        endTime
                ).toMinutes()
        );

        double elapsedHours =
                elapsedMinutes / 60.0;

        /*
         * -----------------------------------------------------
         * SLA DEADLINE
         * -----------------------------------------------------
         *
         * Prefer the deadline already stored on the request.
         *
         * This is important because the request may have been
         * created using a department-specific SLA policy.
         */
        LocalDateTime slaDeadline =
                request.getSlaDeadline();

        /*
         * Historical requests may not have a stored SLA deadline.
         *
         * For those requests we calculate a fallback deadline.
         */
        if (slaDeadline == null) {

            slaDeadline =
                    request.getCreatedAt()
                            .plusMinutes(
                                    (long) (slaHours * 60)
                            );
        }

        /*
         * -----------------------------------------------------
         * REMAINING TIME
         * -----------------------------------------------------
         *
         * For completed requests:
         *
         *     remaining time is frozen at completion.
         *
         * For pending requests:
         *
         *     remaining time continues to count down.
         */
        long remainingMinutes;

        if (completed) {

            remainingMinutes =
                    Math.max(
                            0,
                            Duration.between(
                                    endTime,
                                    slaDeadline
                            ).toMinutes()
                    );

        } else {

            remainingMinutes =
                    Math.max(
                            0,
                            Duration.between(
                                    LocalDateTime.now(),
                                    slaDeadline
                            ).toMinutes()
                    );
        }

        /*
         * -----------------------------------------------------
         * SLA RATIO
         * -----------------------------------------------------
         */
        double effectiveElapsedHours =
                elapsedHours;

        /*
         * Calculate probability.
         */
        double probability =
                calculateBreachProbability(
                        request,
                        effectiveElapsedHours,
                        slaHours
                );

        /*
         * -----------------------------------------------------
         * RISK LEVEL
         * -----------------------------------------------------
         */
        SLAStatus riskLevel =
                calculateRiskLevel(
                        request,
                        endTime,
                        slaDeadline,
                        effectiveElapsedHours,
                        slaHours,
                        probability
                );

        /*
         * -----------------------------------------------------
         * REASONS
         * -----------------------------------------------------
         */
        List<String> reasons =
                buildReasons(
                        request,
                        endTime,
                        slaDeadline,
                        effectiveElapsedHours,
                        slaHours,
                        probability
                );

        return new SLAPredictionResponse(
                request.getId(),
                request.getTitle(),
                request.getCategory() != null
                        ? request.getCategory().name()
                        : null,
                getPriority(request),
                request.getStatus() != null
                        ? request.getStatus().name()
                        : null,
                slaHours,
                round(effectiveElapsedHours),
                remainingMinutes,
                probability,
                riskLevel,
                reasons
        );
    }

    /**
     * Calculate breach probability.
     */
    private double calculateBreachProbability(
            Request request,
            double elapsedHours,
            double slaHours
    ) {

        if (slaHours <= 0) {
            return 99.0;
        }

        double score = 0;

        /*
         * -----------------------------------------------------
         * TIME FACTOR
         * -----------------------------------------------------
         */
        double timeRatio =
                elapsedHours / slaHours;

        if (timeRatio >= 1.0) {

            score += 70;

        } else if (timeRatio >= 0.90) {

            score += 60;

        } else if (timeRatio >= 0.75) {

            score += 45;

        } else if (timeRatio >= 0.50) {

            score += 25;

        } else {

            score += 10;
        }

        /*
         * -----------------------------------------------------
         * PRIORITY FACTOR
         * -----------------------------------------------------
         */
        String priority =
                getPriority(request);

        if ("CRITICAL".equalsIgnoreCase(priority)) {

            score += 20;

        } else if ("HIGH".equalsIgnoreCase(priority)) {

            score += 15;

        } else if ("MEDIUM".equalsIgnoreCase(priority)) {

            score += 8;
        }

        /*
         * -----------------------------------------------------
         * DEPARTMENT WORKLOAD
         * -----------------------------------------------------
         */
        long pendingRequests =
                countPendingRequests(request);

        if (pendingRequests >= 15) {

            score += 15;

        } else if (pendingRequests >= 10) {

            score += 10;

        } else if (pendingRequests >= 5) {

            score += 5;
        }

        /*
         * Maximum probability = 99%.
         */
        return Math.min(
                99.0,
                Math.round(score * 100.0) / 100.0
        );
    }

    /**
     * Determine SLA risk level.
     *
     * Important:
     * Final requests are evaluated at completedAt,
     * not against the current time.
     */
    private SLAStatus calculateRiskLevel(
            Request request,
            LocalDateTime endTime,
            LocalDateTime slaDeadline,
            double elapsedHours,
            double slaHours,
            double probability
    ) {

        /*
         * -----------------------------------------------------
         * FINAL STATE
         * -----------------------------------------------------
         */
        boolean completed =
                request.getStatus() == RequestStatus.APPROVED
                        || request.getStatus() == RequestStatus.REJECTED;

        /*
         * If the request was completed after the SLA deadline,
         * it genuinely breached the SLA.
         */
        if (endTime.isAfter(slaDeadline)) {

            return SLAStatus.BREACHED;
        }

        /*
         * If completed before deadline, it is no longer active.
         *
         * We return WITHIN_SLA because it completed successfully
         * within the allowed processing time.
         */
        if (completed) {

            return SLAStatus.WITHIN_SLA;
        }

        /*
         * -----------------------------------------------------
         * ACTIVE REQUEST
         * -----------------------------------------------------
         */
        if (elapsedHours >= slaHours) {

            return SLAStatus.BREACHED;
        }

        if (probability >= 70) {

            return SLAStatus.AT_RISK;
        }

        return SLAStatus.WITHIN_SLA;
    }

    /**
     * Generate SLA explanations.
     */
    private List<String> buildReasons(
            Request request,
            LocalDateTime endTime,
            LocalDateTime slaDeadline,
            double elapsedHours,
            double slaHours,
            double probability
    ) {

        List<String> reasons =
                new ArrayList<>();

        boolean completed =
                request.getStatus() == RequestStatus.APPROVED
                        || request.getStatus() == RequestStatus.REJECTED;

        /*
         * -----------------------------------------------------
         * COMPLETED REQUEST
         * -----------------------------------------------------
         */
        if (completed) {

            if (endTime.isAfter(slaDeadline)) {

                reasons.add(
                        "The request was completed after its SLA deadline."
                );

            } else {

                reasons.add(
                        "The request was completed within its SLA."
                );
            }

        } else {

            /*
             * -------------------------------------------------
             * ACTIVE REQUEST
             * -------------------------------------------------
             */
            double timeRatio =
                    slaHours > 0
                            ? elapsedHours / slaHours
                            : 1.0;

            if (elapsedHours >= slaHours) {

                reasons.add(
                        "The request has exceeded its SLA."
                );

            } else if (timeRatio >= 0.90) {

                reasons.add(
                        "The request is very close to its SLA deadline."
                );

            } else if (timeRatio >= 0.75) {

                reasons.add(
                        "Most of the SLA time has already elapsed."
                );
            }
        }

        /*
         * -----------------------------------------------------
         * PRIORITY
         * -----------------------------------------------------
         */
        String priority =
                getPriority(request);

        if ("CRITICAL".equalsIgnoreCase(priority)) {

            reasons.add(
                    "The request has CRITICAL priority."
            );

        } else if ("HIGH".equalsIgnoreCase(priority)) {

            reasons.add(
                    "The request has HIGH priority."
            );
        }

        /*
         * -----------------------------------------------------
         * WORKLOAD
         * -----------------------------------------------------
         */
        long pending =
                countPendingRequests(request);

        if (pending >= 15) {

            reasons.add(
                    "The department currently has "
                            + pending
                            + " pending requests."
            );

        } else if (pending >= 10) {

            reasons.add(
                    "The department currently has a high workload."
            );
        }

        /*
         * -----------------------------------------------------
         * DEFAULT REASON
         * -----------------------------------------------------
         */
        if (reasons.isEmpty()) {

            if (completed) {

                reasons.add(
                        "The request was completed within SLA."
                );

            } else {

                reasons.add(
                        "The request is currently progressing within SLA."
                );
            }
        }

        return reasons;
    }

    /**
     * Count pending requests for the same category.
     */
    private long countPendingRequests(
            Request request
    ) {

        if (request.getCategory() == null) {

            return requestRepository
                    .countByStatus(
                            RequestStatus.PENDING
                    );
        }

        return requestRepository
                .countByCategoryAndStatus(
                        request.getCategory(),
                        RequestStatus.PENDING
                );
    }

    /**
     * Return SLA hours.
     *
     * The request already has its actual deadline.
     *
     * Therefore, for historical requests where the deadline
     * exists, calculate the duration from createdAt to deadline.
     *
     * For older records without a deadline, use fallback values.
     */
    private double getSlaHours(
            Request request
    ) {

        if (
                request.getCreatedAt() != null
                        && request.getSlaDeadline() != null
        ) {

            long minutes =
                    Duration.between(
                            request.getCreatedAt(),
                            request.getSlaDeadline()
                    ).toMinutes();

            if (minutes > 0) {

                return minutes / 60.0;
            }
        }

        /*
         * Fallback for old records.
         */
        String priority =
                getPriority(request);

        if ("CRITICAL".equalsIgnoreCase(priority)) {

            return 2.0;
        }

        if ("HIGH".equalsIgnoreCase(priority)) {

            return 4.0;
        }

        if ("MEDIUM".equalsIgnoreCase(priority)) {

            return 8.0;
        }

        return DEFAULT_SLA_HOURS;
    }

    /**
     * Return request priority.
     *
     * If no priority exists, keep the existing fallback behavior.
     */
    private String getPriority(
            Request request
    ) {

        try {

            if (request.getPriority() != null) {

                return request
                        .getPriority()
                        .name();
            }

        } catch (Exception ignored) {
        }

        return "MEDIUM";
    }

    /**
     * Admin SLA dashboard.
     *
     * Only active requests are analyzed here.
     */
    public SLADashboardResponse getDashboard() {

        List<Request> requests =
                requestRepository.findAll();

        long withinSLA = 0;
        long atRisk = 0;
        long breached = 0;

        List<SLAPredictionResponse> predictions =
                new ArrayList<>();

        for (Request request : requests) {

            /*
             * Final requests are excluded from the active SLA
             * dashboard.
             *
             * Their historical SLA result can still be obtained
             * from /sla/requests/{id}.
             */
            if (
                    request.getStatus()
                            == RequestStatus.APPROVED
                            ||
                            request.getStatus()
                                    == RequestStatus.REJECTED
            ) {

                continue;
            }

            SLAPredictionResponse prediction =
                    predictRequest(request);

            switch (prediction.getRiskLevel()) {

                case WITHIN_SLA:

                    withinSLA++;

                    break;

                case AT_RISK:

                    atRisk++;

                    predictions.add(
                            prediction
                    );

                    break;

                case BREACHED:

                    breached++;

                    predictions.add(
                            prediction
                    );

                    break;
            }
        }

        /*
         * Highest risk first.
         */
        predictions.sort(
                Comparator.comparingDouble(
                        SLAPredictionResponse::getBreachProbability
                ).reversed()
        );

        /*
         * Show top 5 critical / at-risk requests.
         */
        List<SLAPredictionResponse> criticalRequests =
                predictions.stream()
                        .limit(5)
                        .toList();

        return new SLADashboardResponse(
                withinSLA,
                atRisk,
                breached,
                withinSLA
                        + atRisk
                        + breached,
                criticalRequests
        );
    }

    private double round(
            double value
    ) {

        return Math.round(
                value * 100.0
        ) / 100.0;
    }
}