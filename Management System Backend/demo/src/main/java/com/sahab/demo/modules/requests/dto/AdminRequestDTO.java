package com.sahab.demo.modules.requests.dto;

import com.sahab.demo.enums.RequestCategory;
import com.sahab.demo.enums.RequestPriority;
import com.sahab.demo.enums.RequestStatus;

import java.time.LocalDateTime;

public record AdminRequestDTO(
        Long id,
        String title,
        String description,
        RequestStatus status,
        RequestCategory category,
        RequestPriority priority,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long approvedBy,
        LocalDateTime approvedAt,
        UserSummary user,
        boolean hasAttachment,

        // =========================
        // SLA
        // =========================

        LocalDateTime slaDeadline,
        LocalDateTime slaWarningAt,
        Boolean slaBreached,
        Boolean slaWarningSent,
        Boolean slaEscalated
) {

    public record UserSummary(
            Long id,
            String name,
            String email,
            String role
    ) {
    }
}