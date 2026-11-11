
package com.sahab.demo.modules.requests.dto;

import com.sahab.demo.enums.RequestCategory;
import com.sahab.demo.enums.RequestPriority;
import com.sahab.demo.enums.RequestStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class EmployeeRequestDTO {

    private Long id;

    private String title;

    private String description;

    private RequestStatus status;

    private RequestCategory category;

    private RequestPriority priority;

    private Integer durationDays;

    private LocalDateTime createdAt;

    private LocalDateTime approvedAt;

    private Long approvedBy;

    // =========================================================
    // ATTACHMENT
    // =========================================================

    private boolean hasAttachment;

    public EmployeeRequestDTO() {
    }
}

