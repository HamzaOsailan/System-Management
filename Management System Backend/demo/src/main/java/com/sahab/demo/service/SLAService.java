package com.sahab.demo.service;

import com.sahab.demo.dto.SLADashboardResponse;
import com.sahab.demo.dto.SLAPredictionResponse;
import com.sahab.demo.entity.Request;
import com.sahab.demo.enums.RequestStatus;
import com.sahab.demo.enums.SLAStatus;
import com.sahab.demo.repository.RequestRepository;
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
     * Default SLA values.
     *
     * Later we can move these values to the database
     * and make them configurable from Admin.
     */
    private static final double DEFAULT_SLA_HOURS = 4.0;

    /**
     * Get SLA prediction for one request.
     */
    public SLAPredictionResponse predictRequest(Request request) {

        if (request.getCreatedAt() == null) {
            throw new RuntimeException("Request creation date is missing");
        }

        LocalDateTime now = LocalDateTime.now();

        long elapsedMinutes = Duration.between(
                request.getCreatedAt(),
                now
        ).toMinutes();

        double elapsedHours = elapsedMinutes / 60.0;

        double slaHours = getSlaHours(request);

        long remainingMinutes = Math.max(
                0,
                (long) (slaHours * 60 - elapsedMinutes)
        );

        double probability = calculateBreachProbability(
                request,
                elapsedHours,
                slaHours
        );

        SLAStatus riskLevel = calculateRiskLevel(
                elapsedHours,
                slaHours,
                probability
        );

        List<String> reasons = buildReasons(
                request,
                elapsedHours,
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
                round(elapsedHours),
                remainingMinutes,
                probability,
                riskLevel,
                reasons
        );
    }

    /**
     * Calculate breach probability.
     *
     * This is a rule-based risk engine for now.
     * Later Claude / ML can use these signals.
     */
    private double calculateBreachProbability(
            Request request,
            double elapsedHours,
            double slaHours
    ) {

        double score = 0;

        /*
         * 1. Time factor
         */
        double timeRatio = elapsedHours / slaHours;

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
         * 2. Priority factor
         */
        String priority = getPriority(request);

        if ("CRITICAL".equalsIgnoreCase(priority)) {
            score += 20;
        } else if ("HIGH".equalsIgnoreCase(priority)) {
            score += 15;
        } else if ("MEDIUM".equalsIgnoreCase(priority)) {
            score += 8;
        }

        /*
         * 3. Department workload
         */
        long pendingRequests = countPendingRequests(
                request
        );

        if (pendingRequests >= 15) {
            score += 15;
        } else if (pendingRequests >= 10) {
            score += 10;
        } else if (pendingRequests >= 5) {
            score += 5;
        }

        /*
         * Maximum probability = 99%
         */
        return Math.min(
                99.0,
                Math.round(score * 100.0) / 100.0
        );
    }

    /**
     * Determine SLA risk level.
     */
    private SLAStatus calculateRiskLevel(
            double elapsedHours,
            double slaHours,
            double probability
    ) {

        if (elapsedHours >= slaHours) {
            return SLAStatus.BREACHED;
        }

        if (probability >= 70) {
            return SLAStatus.AT_RISK;
        }

        return SLAStatus.WITHIN_SLA;
    }

    /**
     * Generate explanations for the Admin.
     */
    private List<String> buildReasons(
            Request request,
            double elapsedHours,
            double slaHours,
            double probability
    ) {

        List<String> reasons = new ArrayList<>();

        double timeRatio = elapsedHours / slaHours;

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

        String priority = getPriority(request);

        if ("CRITICAL".equalsIgnoreCase(priority)) {

            reasons.add(
                    "The request has CRITICAL priority."
            );

        } else if ("HIGH".equalsIgnoreCase(priority)) {

            reasons.add(
                    "The request has HIGH priority."
            );
        }

        long pending = countPendingRequests(request);

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

        if (reasons.isEmpty()) {

            reasons.add(
                    "The request is currently progressing within SLA."
            );
        }

        return reasons;
    }

    /**
     * Count pending requests for the same category.
     *
     * If your Request entity uses department instead of category,
     * we can change this method later.
     */
    private long countPendingRequests(Request request) {

        if (request.getCategory() == null) {
            return requestRepository
                    .countByStatus(RequestStatus.PENDING);
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
     * Later this can depend on category / priority.
     */
    private double getSlaHours(Request request) {

        String priority = getPriority(request);

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
     * Your current Request entity may not have priority yet.
     *
     * For now we safely return MEDIUM.
     */
    private String getPriority(Request request) {

        try {

            if (request.getPriority() != null) {
                return request.getPriority().name();
            }

        } catch (Exception ignored) {
        }

        return "MEDIUM";
    }

    /**
     * Admin dashboard.
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
             * Usually we only analyze active requests.
             */
            if (request.getStatus() == RequestStatus.APPROVED
                    || request.getStatus() == RequestStatus.REJECTED) {
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
                    predictions.add(prediction);
                    break;

                case BREACHED:
                    breached++;
                    predictions.add(prediction);
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
         * Show only top 5 critical/at-risk requests.
         */
        List<SLAPredictionResponse> criticalRequests =
                predictions.stream()
                        .limit(5)
                        .toList();

        return new SLADashboardResponse(
                withinSLA,
                atRisk,
                breached,
                withinSLA + atRisk + breached,
                criticalRequests
        );
    }

    private double round(double value) {

        return Math.round(value * 100.0) / 100.0;
    }
}