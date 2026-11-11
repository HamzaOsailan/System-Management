package com.sahab.demo.modules.hr.service;

import com.sahab.demo.modules.hr.dto.LeaveSummaryDTO;
import com.sahab.demo.entity.User;
import com.sahab.demo.enums.RequestCategory;
import com.sahab.demo.enums.RequestStatus;
import com.sahab.demo.modules.requests.repository.RequestRepository;
import com.sahab.demo.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class LeaveService {

    private static final int ANNUAL_LEAVE_ALLOWANCE = 30;
    private static final int REMOTE_WORK_MONTHLY_ALLOWANCE = 2;

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;

    public LeaveService(
            RequestRepository requestRepository,
            UserRepository userRepository
    ) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
    }

    public LeaveSummaryDTO getCurrentUserLeaveSummary() {

        User currentUser = getCurrentUser();

        LocalDate now = LocalDate.now();

        int year = now.getYear();
        int month = now.getMonthValue();

        Integer leaveUsedValue =
                requestRepository.sumApprovedDurationByUserAndCategoryAndYear(
                        currentUser,
                        RequestCategory.LEAVE,
                        RequestStatus.APPROVED,
                        year
                );

        int leaveUsed =
                leaveUsedValue != null ? leaveUsedValue : 0;

        Integer remoteUsedValue =
                requestRepository.sumApprovedDurationByUserAndCategoryAndMonth(
                        currentUser,
                        RequestCategory.REMOTE_WORK,
                        RequestStatus.APPROVED,
                        year,
                        month
                );

        int remoteUsed =
                remoteUsedValue != null ? remoteUsedValue : 0;

        int leaveRemaining = Math.max(
                ANNUAL_LEAVE_ALLOWANCE - leaveUsed,
                0
        );

        int remoteRemaining = Math.max(
                REMOTE_WORK_MONTHLY_ALLOWANCE - remoteUsed,
                0
        );

        double leavePercentage =
                calculatePercentage(
                        leaveUsed,
                        ANNUAL_LEAVE_ALLOWANCE
                );

        double remotePercentage =
                calculatePercentage(
                        remoteUsed,
                        REMOTE_WORK_MONTHLY_ALLOWANCE
                );

        return new LeaveSummaryDTO(
                ANNUAL_LEAVE_ALLOWANCE,
                leaveUsed,
                leaveRemaining,
                leavePercentage,

                REMOTE_WORK_MONTHLY_ALLOWANCE,
                remoteUsed,
                remoteRemaining,
                remotePercentage,

                month,
                year
        );
    }

    public void validateRequestDuration(
            RequestCategory category,
            Integer durationDays
    ) {

        if (category == null) {
            return;
        }

        int days =
                durationDays == null || durationDays < 1
                        ? 1
                        : durationDays;

        User currentUser = getCurrentUser();

        LocalDate now = LocalDate.now();

        if (category == RequestCategory.LEAVE) {

            Integer usedValue =
                    requestRepository
                            .sumApprovedDurationByUserAndCategoryAndYear(
                                    currentUser,
                                    RequestCategory.LEAVE,
                                    RequestStatus.APPROVED,
                                    now.getYear()
                            );

            int used =
                    usedValue != null ? usedValue : 0;

            int remaining =
                    ANNUAL_LEAVE_ALLOWANCE - used;

            if (days > remaining) {
                throw new IllegalArgumentException(
                        "You only have "
                                + Math.max(remaining, 0)
                                + " annual leave day(s) remaining."
                );
            }
        }

        if (category == RequestCategory.REMOTE_WORK) {

            Integer usedValue =
                    requestRepository
                            .sumApprovedDurationByUserAndCategoryAndMonth(
                                    currentUser,
                                    RequestCategory.REMOTE_WORK,
                                    RequestStatus.APPROVED,
                                    now.getYear(),
                                    now.getMonthValue()
                            );

            int used =
                    usedValue != null ? usedValue : 0;

            int remaining =
                    REMOTE_WORK_MONTHLY_ALLOWANCE - used;

            if (days > remaining) {
                throw new IllegalArgumentException(
                        "You only have "
                                + Math.max(remaining, 0)
                                + " remote work day(s) remaining this month."
                );
            }
        }
    }

    private double calculatePercentage(
            int used,
            int allowance
    ) {

        if (allowance <= 0) {
            return 0;
        }

        double percentage =
                ((double) used / allowance) * 100;

        return Math.min(
                Math.round(percentage * 100.0) / 100.0,
                100.0
        );
    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (
                authentication == null ||
                        !authentication.isAuthenticated()
        ) {
            throw new RuntimeException(
                    "User is not authenticated"
            );
        }

        return userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );
    }
}