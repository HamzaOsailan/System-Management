
        package com.sahab.demo.dto;

import com.sahab.demo.enums.RequestCategory;
import com.sahab.demo.enums.RequestPriority;
import com.sahab.demo.enums.RequestStatus;

import java.time.LocalDateTime;

public record PendingApprovalRequestDTO(
        Long id,
        String title,
        String description,
        RequestStatus status,
        RequestCategory category,
        RequestPriority priority,
        LocalDateTime createdAt,
        UserSummary user
) {

    public record UserSummary(
            Long id,
            String name,
            String email
    ) {
    }
}
