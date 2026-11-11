package com.sahab.demo.modules.requests.repository;

import com.sahab.demo.entity.ApprovalStep;
import com.sahab.demo.entity.Request;
import com.sahab.demo.enums.ApprovalStepStatus;
import com.sahab.demo.enums.Role;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ApprovalStepRepository
        extends JpaRepository<ApprovalStep, Long> {

    // =========================================================
    // GET WORKFLOW STEPS FOR A REQUEST
    // =========================================================

    List<ApprovalStep> findByRequestOrderByStepOrderAsc(
            Request request
    );


    // =========================================================
    // GET CURRENT PENDING STEP
    // =========================================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ApprovalStep>
    findFirstByRequestAndStatusOrderByStepOrderAsc(
            Request request,
            ApprovalStepStatus status
    );


    // =========================================================
    // ADMIN
    // =========================================================

    @EntityGraph(attributePaths = {
            "request",
            "request.user"
    })
    List<ApprovalStep>
    findByStatusAndRequiredRoleAndApproverIsNullOrderByStepOrderAsc(
            ApprovalStepStatus status,
            Role requiredRole
    );


    // =========================================================
    // MANAGER
    // =========================================================

    @EntityGraph(attributePaths = {
            "request",
            "request.user"
    })
    List<ApprovalStep>
    findByStatusAndRequiredRoleAndApproverIdOrderByStepOrderAsc(
            ApprovalStepStatus status,
            Role requiredRole,
            Long approverId
    );


    // =========================================================
    // SLA COMPLIANCE
    //
    // Gets the last action date of the workflow for a request.
    //
    // MAX(actionDate) represents the final workflow action.
    // =========================================================

    @Query("""
        SELECT MAX(s.actionDate)
        FROM ApprovalStep s
        WHERE s.request.id = :requestId
          AND s.actionDate IS NOT NULL
    """)
    Optional<LocalDateTime> findLastActionDateByRequestId(
            @Param("requestId") Long requestId
    );


    // =========================================================
    // DELETE WORKFLOW WHEN REQUEST IS DELETED
    // =========================================================

    void deleteByRequest(Request request);
}