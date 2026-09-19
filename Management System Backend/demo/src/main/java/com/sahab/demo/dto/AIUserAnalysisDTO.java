package com.sahab.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AIUserAnalysisDTO {

    private Long requestId;

    private String requestTitle;

    private String requestStatus;

    private String category;

    private String priority;

    private Integer urgency;

    private String summary;

    private String suggestedDepartment;

    private String suggestedAction;
}