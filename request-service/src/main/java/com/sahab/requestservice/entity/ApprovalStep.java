package com.sahab.requestservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "request_approval_steps",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_approval_request_step",
        columnNames = {"request_id", "step_order"}
    )
)
public class ApprovalStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(nullable = false, length = 30)
    private String requiredRole;

    @Column
    private Long approverId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApprovalStepStatus status = ApprovalStepStatus.WAITING;

    private LocalDateTime actionDate;

    @Column(columnDefinition = "TEXT")
    private String comment;

    public Long getId() { return id; }
    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }
    public Integer getStepOrder() { return stepOrder; }
    public void setStepOrder(Integer stepOrder) { this.stepOrder = stepOrder; }
    public String getRequiredRole() { return requiredRole; }
    public void setRequiredRole(String requiredRole) { this.requiredRole = requiredRole; }
    public Long getApproverId() { return approverId; }
    public void setApproverId(Long approverId) { this.approverId = approverId; }
    public ApprovalStepStatus getStatus() { return status; }
    public void setStatus(ApprovalStepStatus status) { this.status = status; }
    public LocalDateTime getActionDate() { return actionDate; }
    public void setActionDate(LocalDateTime actionDate) { this.actionDate = actionDate; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
