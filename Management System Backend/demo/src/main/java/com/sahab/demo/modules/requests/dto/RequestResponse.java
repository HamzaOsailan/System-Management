package com.sahab.demo.modules.requests.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RequestResponse {

    private Long id;

    private String title;

    private String description;

    private String status;

    // =========================================================
    // REQUEST OWNER
    // =========================================================

    private Long userId;

    private String userName;

    private String userEmail;

    // =========================================================
    // DATES
    // =========================================================

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // =========================================================
    // REQUEST DETAILS
    // =========================================================

    private String category;

    private String priority;

    private Integer durationDays;

    // =========================================================
    // APPROVAL
    // =========================================================

    private Long approvedBy;

    private LocalDateTime approvedAt;

    // =========================================================
    // SLA
    // =========================================================

    private LocalDateTime slaDeadline;

    private LocalDateTime slaWarningAt;

    private Boolean slaBreached;

    private Boolean slaWarningSent;

    private Boolean slaEscalated;
}