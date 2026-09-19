
        package com.sahab.demo.service;

import com.sahab.demo.dto.RequestWorkflowDTO;
import com.sahab.demo.entity.ApprovalStep;
import com.sahab.demo.entity.Request;
import com.sahab.demo.entity.User;
import com.sahab.demo.enums.ApprovalStepStatus;
import com.sahab.demo.enums.RequestStatus;
import com.sahab.demo.enums.Role;
import com.sahab.demo.repository.ApprovalStepRepository;
import com.sahab.demo.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApprovalWorkflowService {

    private final ApprovalStepRepository approvalStepRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ApprovalWorkflowService(
            ApprovalStepRepository approvalStepRepository,
            UserRepository userRepository,
            NotificationService notificationService
    ) {
        this.approvalStepRepository = approvalStepRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    // =====================================================
    // CREATE WORKFLOW
    // =====================================================

    @Transactional
    public void createWorkflow(Request request) {

        if (!approvalStepRepository
                .findByRequestOrderByStepOrderAsc(request)
                .isEmpty()) {

            return;
        }

        User manager =
                userRepository
                        .findFirstByRole(Role.MANAGER)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No manager found in the system"
                                )
                        );

        // -------------------------------------------------
        // STEP 1 - ADMIN
        // -------------------------------------------------

        ApprovalStep adminStep =
                new ApprovalStep();

        adminStep.setRequest(request);
        adminStep.setStepOrder(1);
        adminStep.setRequiredRole(Role.ADMIN);
        adminStep.setStatus(
                ApprovalStepStatus.PENDING
        );

        // -------------------------------------------------
        // STEP 2 - MANAGER
        // -------------------------------------------------

        ApprovalStep managerStep =
                new ApprovalStep();

        managerStep.setRequest(request);
        managerStep.setStepOrder(2);
        managerStep.setRequiredRole(Role.MANAGER);
        managerStep.setApprover(manager);
        managerStep.setStatus(
                ApprovalStepStatus.WAITING
        );

        approvalStepRepository.save(adminStep);
        approvalStepRepository.save(managerStep);
    }

    // =====================================================
    // APPROVE CURRENT STEP
    // =====================================================

    @Transactional
    public boolean approve(
            Request request,
            User currentUser,
            String comment
    ) {

        ApprovalStep currentStep =
                approvalStepRepository
                        .findFirstByRequestAndStatusOrderByStepOrderAsc(
                                request,
                                ApprovalStepStatus.PENDING
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No pending approval step found"
                                )
                        );

        validateApprover(
                currentStep,
                currentUser
        );

        currentStep.setStatus(
                ApprovalStepStatus.APPROVED
        );

        currentStep.setApprover(
                currentUser
        );

        currentStep.setActionDate(
                LocalDateTime.now()
        );

        currentStep.setComment(
                comment
        );

        approvalStepRepository.save(
                currentStep
        );

        // =================================================
        // ADMIN -> MANAGER
        // =================================================


        if (currentStep.getRequiredRole() == Role.ADMIN) {

            ApprovalStep managerStep =
                    approvalStepRepository
                            .findFirstByRequestAndStatusOrderByStepOrderAsc(
                                    request,
                                    ApprovalStepStatus.WAITING
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Manager approval step not found"
                                    )
                            );

            managerStep.setStatus(
                    ApprovalStepStatus.PENDING
            );

            approvalStepRepository.save(
                    managerStep
            );

            // Notify manager
            notificationService.createNotification(
                    managerStep.getApprover(),
                    "A request is waiting for your approval."
            );

            // Notify request owner
            notificationService.createNotification(
                    request.getUser(),
                    "Your request has been approved by the Administrator and forwarded to the Manager."
            );

            return false;
        }



        // =================================================
        // MANAGER -> FINAL APPROVAL
        // =================================================

        if (currentStep.getRequiredRole()
                == Role.MANAGER) {

            request.setStatus(
                    RequestStatus.APPROVED
            );

            notificationService.createNotification(
                    request.getUser(),
                    "Your request has been fully approved."
            );

            return true;
        }

        return false;
    }

    // =====================================================
    // REJECT CURRENT STEP
    // =====================================================

    @Transactional
    public void reject(
            Request request,
            User currentUser,
            String comment
    ) {

        ApprovalStep currentStep =
                approvalStepRepository
                        .findFirstByRequestAndStatusOrderByStepOrderAsc(
                                request,
                                ApprovalStepStatus.PENDING
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No pending approval step found"
                                )
                        );

        validateApprover(
                currentStep,
                currentUser
        );

        currentStep.setStatus(
                ApprovalStepStatus.REJECTED
        );

        currentStep.setApprover(
                currentUser
        );

        currentStep.setActionDate(
                LocalDateTime.now()
        );

        currentStep.setComment(
                comment
        );

        approvalStepRepository.save(
                currentStep
        );

        request.setStatus(
                RequestStatus.REJECTED
        );

        notificationService.createNotification(
                request.getUser(),
                "Your request has been rejected."
        );
    }

    // =====================================================
    // GET PENDING APPROVALS
    // =====================================================

    public List<Request> getPendingApprovals(
            User currentUser
    ) {

        if (currentUser.getRole() == Role.ADMIN) {

            return approvalStepRepository
                    .findByStatusAndRequiredRoleAndApproverIsNullOrderByStepOrderAsc(
                            ApprovalStepStatus.PENDING,
                            Role.ADMIN
                    )
                    .stream()
                    .map(ApprovalStep::getRequest)
                    .toList();
        }

        if (currentUser.getRole() == Role.MANAGER) {

            return approvalStepRepository
                    .findByStatusAndRequiredRoleAndApproverOrderByStepOrderAsc(
                            ApprovalStepStatus.PENDING,
                            Role.MANAGER,
                            currentUser
                    )
                    .stream()
                    .map(ApprovalStep::getRequest)
                    .toList();
        }

        throw new RuntimeException(
                "You do not have approval permissions"
        );
    }

    // =====================================================
    // GET WORKFLOW
    // =====================================================

    public RequestWorkflowDTO getWorkflow(
            Request request
    ) {

        List<ApprovalStep> steps =
                approvalStepRepository
                        .findByRequestOrderByStepOrderAsc(
                                request
                        );

        // -------------------------------------------------
        // Legacy pending request
        // -------------------------------------------------

        if (steps.isEmpty()) {

            if (request.getStatus()
                    == RequestStatus.PENDING) {

                createWorkflow(request);

                steps =
                        approvalStepRepository
                                .findByRequestOrderByStepOrderAsc(
                                        request
                                );

            } else {

                throw new RuntimeException(
                        "Approval workflow not found"
                );
            }
        }

        // -------------------------------------------------
        // Current pending step
        // -------------------------------------------------

        ApprovalStep currentStep =
                steps.stream()
                        .filter(step ->
                                step.getStatus()
                                        == ApprovalStepStatus.PENDING
                        )
                        .findFirst()
                        .orElse(null);

        String currentStepName =
                getCurrentStepName(
                        currentStep,
                        request
                );

        String currentApproverName =
                currentStep != null &&
                        currentStep.getApprover() != null
                        ? currentStep.getApprover().getName()
                        : null;

        Role currentApproverRole =
                currentStep != null
                        ? currentStep.getRequiredRole()
                        : null;

        List<RequestWorkflowDTO.StepDTO>
                stepDTOs =
                steps.stream()
                        .map(step ->
                                new RequestWorkflowDTO.StepDTO(
                                        step.getStepOrder(),
                                        step.getRequiredRole(),
                                        step.getApprover() != null
                                                ? step.getApprover().getName()
                                                : null,
                                        step.getStatus(),
                                        step.getActionDate(),
                                        step.getComment()
                                )
                        )
                        .toList();

        return new RequestWorkflowDTO(
                request.getId(),
                currentStepName,
                currentApproverName,
                currentApproverRole,
                stepDTOs
        );
    }

    // =====================================================
    // VALIDATE APPROVER
    // =====================================================

    private void validateApprover(
            ApprovalStep step,
            User user
    ) {

        if (user.getRole() !=
                step.getRequiredRole()) {

            throw new RuntimeException(
                    "You are not allowed to approve this step"
            );
        }

        if (step.getApprover() != null &&
                !step.getApprover()
                        .getId()
                        .equals(user.getId())) {

            throw new RuntimeException(
                    "This request is assigned to another approver"
            );
        }
    }

    // =====================================================
    // CURRENT STEP NAME
    // =====================================================

    private String getCurrentStepName(
            ApprovalStep currentStep,
            Request request
    ) {

        if (currentStep == null) {

            if (request.getStatus()
                    == RequestStatus.APPROVED) {

                return "COMPLETED";
            }

            if (request.getStatus()
                    == RequestStatus.REJECTED) {

                return "REJECTED";
            }

            return "WAITING";
        }

        if (currentStep.getRequiredRole()
                == Role.ADMIN) {

            return "ADMIN_APPROVAL";
        }

        if (currentStep.getRequiredRole()
                == Role.MANAGER) {

            return "MANAGER_APPROVAL";
        }

        return "UNKNOWN";
    }
}

