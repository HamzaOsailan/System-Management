package com.sahab.demo.modules.requests.dto;

import com.sahab.demo.enums.RequestCategory;
import com.sahab.demo.enums.RequestPriority;
import com.sahab.demo.enums.RequestStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TeamRequestDTO {

    private Long id;

    private String title;

    private String description;

    private RequestStatus status;

    private RequestCategory category;

    private RequestPriority priority;

    private Integer durationDays;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime approvedAt;

    private Long approvedBy;

    private Long userId;

    private String userName;

    private String userEmail;

    private Long managerId;

    private String managerName;

    public TeamRequestDTO() {
    }
}