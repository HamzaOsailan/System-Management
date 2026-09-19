
        package com.sahab.demo.repository;

import com.sahab.demo.entity.ApprovalStep;
import com.sahab.demo.entity.Request;
import com.sahab.demo.entity.User;
import com.sahab.demo.enums.ApprovalStepStatus;
import com.sahab.demo.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApprovalStepRepository
        extends JpaRepository<ApprovalStep, Long> {

    List<ApprovalStep> findByRequestOrderByStepOrderAsc(
            Request request
    );

    Optional<ApprovalStep>
    findFirstByRequestAndStatusOrderByStepOrderAsc(
            Request request,
            ApprovalStepStatus status
    );

    // Admin: pending steps that are not assigned to a specific person
    List<ApprovalStep>
    findByStatusAndRequiredRoleAndApproverIsNullOrderByStepOrderAsc(
            ApprovalStepStatus status,
            Role requiredRole
    );

    // Manager: only steps assigned to this exact manager
    List<ApprovalStep>
    findByStatusAndRequiredRoleAndApproverOrderByStepOrderAsc(
            ApprovalStepStatus status,
            Role requiredRole,
            User approver
    );

    // Used later when deleting a request
    void deleteByRequest(Request request);
}

