package com.sahab.demo.modules.hr.service;

import com.sahab.demo.modules.hr.dto.LeaveBalanceDTO;
import com.sahab.demo.entity.Request;
import com.sahab.demo.entity.User;
import com.sahab.demo.enums.RequestCategory;
import com.sahab.demo.enums.RequestStatus;
import com.sahab.demo.modules.requests.repository.RequestRepository;
import com.sahab.demo.repository.UserRepository;
import com.sahab.demo.exception.ResourceNotFoundException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class LeaveBalanceService {

    private static final int ANNUAL_LEAVE_LIMIT = 30;

    private static final int REMOTE_WORK_MONTHLY_LIMIT = 2;

    private final RequestRepository requestRepository;

    private final UserRepository userRepository;

    public LeaveBalanceService(
            RequestRepository requestRepository,
            UserRepository userRepository
    ) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
    }

    public LeaveBalanceDTO getCurrentUserBalance() {

        User currentUser = getCurrentUser();

        int currentYear =
                LocalDate.now().getYear();

        int currentMonth =
                LocalDate.now().getMonthValue();

        // =========================================
        // Annual Leave
        // =========================================

        List<Request> approvedLeaves =
                requestRepository
                        .findByUserAndCategoryAndStatus(
                                currentUser,
                                RequestCategory.LEAVE,
                                RequestStatus.APPROVED
                        );

        int annualLeaveUsed = 0;

        for (Request request : approvedLeaves) {

            if (request.getApprovedAt() == null) {
                continue;
            }

            if (request.getApprovedAt().getYear()
                    != currentYear) {
                continue;
            }

            int duration =
                    request.getDurationDays() == null
                            ? 1
                            : request.getDurationDays();

            annualLeaveUsed += duration;
        }

        // حماية من تجاوز الرصيد
        annualLeaveUsed =
                Math.min(
                        annualLeaveUsed,
                        ANNUAL_LEAVE_LIMIT
                );

        int annualLeaveRemaining =
                ANNUAL_LEAVE_LIMIT
                        - annualLeaveUsed;

        double annualLeavePercentage =
                calculatePercentage(
                        annualLeaveUsed,
                        ANNUAL_LEAVE_LIMIT
                );

        // =========================================
        // Remote Work
        // =========================================

        List<Request> approvedRemoteWork =
                requestRepository
                        .findByUserAndCategoryAndStatus(
                                currentUser,
                                RequestCategory.REMOTE_WORK,
                                RequestStatus.APPROVED
                        );

        int remoteWorkUsed = 0;

        for (Request request : approvedRemoteWork) {

            if (request.getApprovedAt() == null) {
                continue;
            }

            if (request.getApprovedAt().getYear()
                    != currentYear) {
                continue;
            }

            if (request.getApprovedAt().getMonthValue()
                    != currentMonth) {
                continue;
            }

            remoteWorkUsed += 1;
        }

        remoteWorkUsed =
                Math.min(
                        remoteWorkUsed,
                        REMOTE_WORK_MONTHLY_LIMIT
                );

        int remoteWorkRemaining =
                REMOTE_WORK_MONTHLY_LIMIT
                        - remoteWorkUsed;

        double remoteWorkPercentage =
                calculatePercentage(
                        remoteWorkUsed,
                        REMOTE_WORK_MONTHLY_LIMIT
                );

        return new LeaveBalanceDTO(
                currentYear,

                ANNUAL_LEAVE_LIMIT,
                annualLeaveUsed,
                annualLeaveRemaining,
                annualLeavePercentage,

                REMOTE_WORK_MONTHLY_LIMIT,
                remoteWorkUsed,
                remoteWorkRemaining,
                remoteWorkPercentage,

                currentMonth
        );
    }

    private double calculatePercentage(
            int used,
            int limit
    ) {

        if (limit <= 0) {
            return 0;
        }

        return Math.round(
                ((double) used / limit) * 1000
        ) / 10.0;
    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new RuntimeException(
                    "User is not authenticated"
            );
        }

        return userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );
    }
}