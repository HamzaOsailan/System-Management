package com.sahab.demo.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AIAnalysisDTO {

    private Long id;

    private Long requestId;

    private String category;

    private String subCategory;

    private String priority;

    private Integer urgency;

    private String summary;

    private String suggestedDepartment;

    private String suggestedAction;

    private String reason;

    private LocalDateTime createdAt;
}