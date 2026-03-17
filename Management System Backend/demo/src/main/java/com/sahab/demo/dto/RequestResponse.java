package com.sahab.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RequestResponse {

    private Long id;

    private String title;

    private String description;

    private String status;

    private String userName;

    private LocalDateTime createdAt;

    private Long approvedBy;

    private LocalDateTime approvedAt;
}