package com.sahab.demo.dto;


import com.sahab.demo.enums.ApprovalStepStatus;
import com.sahab.demo.enums.Role;

import java.time.LocalDateTime;
import java.util.List;

public record RequestWorkflowDTO(
        Long requestId,
        String currentStep,
        String currentApproverName,
        Role currentApproverRole,
        List<StepDTO> steps
) {

    public record StepDTO(
            Integer stepOrder,
            Role requiredRole,
            String approverName,
            ApprovalStepStatus status,
            LocalDateTime actionDate,
            String comment
    ) {
    }
}

