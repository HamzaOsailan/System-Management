
package com.sahab.demo.modules.requests.service;
import com.sahab.demo.service.NotificationService;


import com.sahab.demo.modules.requests.dto.RequestWorkflowDTO;
import com.sahab.demo.entity.ApprovalStep;
import com.sahab.demo.entity.Request;
import com.sahab.demo.entity.User;
import com.sahab.demo.enums.ApprovalStepStatus;
import com.sahab.demo.enums.RequestCategory;
import com.sahab.demo.enums.RequestStatus;
import com.sahab.demo.enums.Role;
import com.sahab.demo.modules.requests.repository.ApprovalStepRepository;
import com.sahab.demo.modules.requests.repository.RequestRepository;
import com.sahab.demo.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApprovalWorkflowService {

    private final ApprovalStepRepository approvalStepRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final NotificationService notificationService;

    public ApprovalWorkflowService(
            ApprovalStepRepository approvalStepRepository,
            UserRepository userRepository,
            RequestRepository requestRepository,
            NotificationService notificationService
    ) {
        this.approvalStepRepository = approvalStepRepository;
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.notificationService = notificationService;
    }

    // =========================================================
    // CREATE APPROVAL WORKFLOW
    // =========================================================

    @Transactional
    public void createWorkflow(Request request) {

        if (request == null || request.getId() == null) {
            throw new RuntimeException(
                    "Request not found"
            );
        }

        // =====================================================
        // LOCK REQUEST
        // =====================================================

        Request lockedRequest =
                requestRepository
                        .findByIdForUpdate(request.getId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Request not found"
                                )
                        );

        request = lockedRequest;

        // =====================================================
        // ONLY PENDING REQUESTS CAN HAVE A NEW WORKFLOW
        // =====================================================

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException(
                    "Only pending requests can have an approval workflow"
            );
        }

        // =====================================================
        // PREVENT DUPLICATE WORKFLOW
        // =====================================================

        if (!approvalStepRepository
                .findByRequestOrderByStepOrderAsc(request)
                .isEmpty()) {

            return;
        }

        User requestOwner = request.getUser();

        if (requestOwner == null) {
            throw new RuntimeException(
                    "Request owner not found"
            );
        }

        // =====================================================
        // LEAVE WORKFLOW
        // =====================================================

        if (request.getCategory() == RequestCategory.LEAVE) {

            createLeaveWorkflow(request);

            return;
        }

        // =====================================================
        // USER
        // USER -> MANAGER -> ADMIN
        // =====================================================

        if (requestOwner.getRole() == Role.USER) {

            createUserNormalWorkflow(request);

            return;
        }

        // =====================================================
        // MANAGER
        // MANAGER -> ADMIN
        // =====================================================

        if (requestOwner.getRole() == Role.MANAGER) {

            createManagerNormalWorkflow(request);

            return;
        }

        // =====================================================
        // ADMIN
        // ADMIN -> ADMIN
        // =====================================================

        if (requestOwner.getRole() == Role.ADMIN) {

            createAdminOnlyWorkflow(request);

            return;
        }

        // =====================================================
        // HR
        // HR -> ADMIN
        // =====================================================

        if (requestOwner.getRole() == Role.HR) {

            createHrNormalWorkflow(request);

            return;
        }

        throw new RuntimeException(
                "This user role cannot create this request workflow"
        );
    }

    // =========================================================
    // USER NORMAL WORKFLOW
    // USER -> MANAGER -> ADMIN
    // =========================================================

    private void createUserNormalWorkflow(Request request) {

        User assignedManager = request.getManager();

        if (assignedManager == null) {
            throw new RuntimeException(
                    "No manager assigned to this request"
            );
        }

        if (assignedManager.getRole() != Role.MANAGER) {
            throw new RuntimeException(
                    "Assigned user is not a Manager"
            );
        }

        // =====================================================
        // STEP 1 - MANAGER
        // =====================================================

        ApprovalStep managerStep = new ApprovalStep();

        managerStep.setRequest(request);
        managerStep.setStepOrder(1);
        managerStep.setRequiredRole(Role.MANAGER);
        managerStep.setStatus(ApprovalStepStatus.PENDING);
        managerStep.setApprover(assignedManager);

        // =====================================================
        // STEP 2 - ADMIN
        // =====================================================

        ApprovalStep adminStep = new ApprovalStep();

        adminStep.setRequest(request);
        adminStep.setStepOrder(2);
        adminStep.setRequiredRole(Role.ADMIN);
        adminStep.setStatus(ApprovalStepStatus.WAITING);
        adminStep.setApprover(null);

        approvalStepRepository.save(managerStep);
        approvalStepRepository.save(adminStep);

        notificationService.createNotification(
                assignedManager,
                "A new request is waiting for your approval."
        );
    }

    // =========================================================
    // MANAGER NORMAL WORKFLOW
    // MANAGER -> ADMIN
    // =========================================================

    private void createManagerNormalWorkflow(Request request) {

        ApprovalStep adminStep = new ApprovalStep();

        adminStep.setRequest(request);
        adminStep.setStepOrder(1);
        adminStep.setRequiredRole(Role.ADMIN);
        adminStep.setStatus(ApprovalStepStatus.PENDING);
        adminStep.setApprover(null);

        approvalStepRepository.save(adminStep);

        List<User> admins =
                userRepository.findByRole(Role.ADMIN);

        for (User admin : admins) {

            notificationService.createNotification(
                    admin,
                    "A new request from a Manager is waiting for your approval."
            );
        }

        notificationService.createNotification(
                request.getUser(),
                "Your request has been submitted and is waiting for Admin approval."
        );
    }

    // =========================================================
    // ADMIN NORMAL WORKFLOW
    // ADMIN -> ADMIN
    // =========================================================

    private void createAdminOnlyWorkflow(Request request) {

        ApprovalStep adminStep = new ApprovalStep();

        adminStep.setRequest(request);
        adminStep.setStepOrder(1);
        adminStep.setRequiredRole(Role.ADMIN);
        adminStep.setStatus(ApprovalStepStatus.PENDING);
        adminStep.setApprover(null);

        approvalStepRepository.save(adminStep);
    }

    // =========================================================
    // HR NORMAL WORKFLOW
    // HR -> ADMIN
    // =========================================================

    private void createHrNormalWorkflow(Request request) {

        ApprovalStep adminStep = new ApprovalStep();

        adminStep.setRequest(request);
        adminStep.setStepOrder(1);
        adminStep.setRequiredRole(Role.ADMIN);
        adminStep.setStatus(ApprovalStepStatus.PENDING);
        adminStep.setApprover(null);

        approvalStepRepository.save(adminStep);

        // =====================================================
        // NOTIFY ADMINS
        // =====================================================

        List<User> admins =
                userRepository.findByRole(Role.ADMIN);

        for (User admin : admins) {

            notificationService.createNotification(
                    admin,
                    "A new request from an HR employee is waiting for your approval."
            );
        }

        // =====================================================
        // NOTIFY HR REQUEST OWNER
        // =====================================================

        notificationService.createNotification(
                request.getUser(),
                "Your request has been submitted and is waiting for Admin approval."
        );
    }

    // =========================================================
    // CREATE LEAVE WORKFLOW
    // =========================================================

    private void createLeaveWorkflow(Request request) {

        User requestOwner = request.getUser();

        if (requestOwner == null) {
            throw new RuntimeException(
                    "Request owner not found"
            );
        }

        // =====================================================
        // USER -> MANAGER -> HR
        // =====================================================

        if (requestOwner.getRole() == Role.USER) {

            User assignedManager = request.getManager();

            if (assignedManager == null) {
                throw new RuntimeException(
                        "No manager assigned to this leave request"
                );
            }

            if (assignedManager.getRole() != Role.MANAGER) {
                throw new RuntimeException(
                        "Assigned user is not a Manager"
                );
            }

            // =================================================
            // STEP 1 - MANAGER
            // =================================================

            ApprovalStep managerStep = new ApprovalStep();

            managerStep.setRequest(request);
            managerStep.setStepOrder(1);
            managerStep.setRequiredRole(Role.MANAGER);
            managerStep.setStatus(ApprovalStepStatus.PENDING);
            managerStep.setApprover(assignedManager);

            // =================================================
            // STEP 2 - HR
            // =================================================

            ApprovalStep hrStep = new ApprovalStep();

            hrStep.setRequest(request);
            hrStep.setStepOrder(2);
            hrStep.setRequiredRole(Role.HR);
            hrStep.setStatus(ApprovalStepStatus.WAITING);
            hrStep.setApprover(null);

            approvalStepRepository.save(managerStep);
            approvalStepRepository.save(hrStep);

            notificationService.createNotification(
                    assignedManager,
                    "A new leave request is waiting for your approval."
            );

            return;
        }

        // =====================================================
        // MANAGER -> HR
        // =====================================================

        if (requestOwner.getRole() == Role.MANAGER) {

            createHrOnlyWorkflow(
                    request,
                    "A new leave request from a Manager is waiting for HR approval."
            );

            return;
        }

        // =====================================================
        // ADMIN -> HR
        // =====================================================

        if (requestOwner.getRole() == Role.ADMIN) {

            createHrOnlyWorkflow(
                    request,
                    "A new leave request from an Administrator is waiting for HR approval."
            );

            return;
        }

        // =====================================================
        // HR -> HR
        //
        // HR leave requests are reviewed by another HR user.
        // Self-approval is prevented by validateApprover().
        // =====================================================

        if (requestOwner.getRole() == Role.HR) {

            createHrOnlyWorkflow(
                    request,
                    "A new leave request from an HR employee is waiting for HR approval."
            );

            return;
        }

        throw new RuntimeException(
                "This user role cannot create a leave request"
        );
    }

    // =========================================================
    // HR ONLY WORKFLOW
    // =========================================================

    private void createHrOnlyWorkflow(
            Request request,
            String notificationMessage
    ) {

        ApprovalStep hrStep = new ApprovalStep();

        hrStep.setRequest(request);
        hrStep.setStepOrder(1);
        hrStep.setRequiredRole(Role.HR);
        hrStep.setStatus(ApprovalStepStatus.PENDING);
        hrStep.setApprover(null);

        approvalStepRepository.save(hrStep);

        List<User> hrUsers =
                userRepository.findByRole(Role.HR);

        for (User hr : hrUsers) {

            notificationService.createNotification(
                    hr,
                    notificationMessage
            );
        }
    }

    // =========================================================
    // APPROVE REQUEST
    // =========================================================

    @Transactional
    public boolean approve(
            Request request,
            User currentUser,
            String comment
    ) {

        if (request == null || request.getId() == null) {
            throw new RuntimeException(
                    "Request not found"
            );
        }

        if (currentUser == null || currentUser.getId() == null) {
            throw new AccessDeniedException(
                    "User not authenticated"
            );
        }

        // =====================================================
        // LOCK REQUEST
        // =====================================================

        Request lockedRequest =
                requestRepository
                        .findByIdForUpdate(request.getId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Request not found"
                                )
                        );

        request = lockedRequest;

        // =====================================================
        // REQUEST MUST STILL BE PENDING
        // =====================================================

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException(
                    "Request is no longer pending"
            );
        }

        // =====================================================
        // GET CURRENT PENDING STEP
        // =====================================================

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

        // =====================================================
        // APPROVE CURRENT STEP
        // =====================================================

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

        approvalStepRepository.save(currentStep);

        // =====================================================
        // LEAVE - MANAGER -> HR
        // =====================================================

        if (
                request.getCategory() == RequestCategory.LEAVE
                        &&
                        currentStep.getRequiredRole() == Role.MANAGER
        ) {

            ApprovalStep hrStep =
                    approvalStepRepository
                            .findFirstByRequestAndStatusOrderByStepOrderAsc(
                                    request,
                                    ApprovalStepStatus.WAITING
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "HR approval step not found"
                                    )
                            );

            hrStep.setStatus(
                    ApprovalStepStatus.PENDING
            );

            approvalStepRepository.save(hrStep);

            List<User> hrUsers =
                    userRepository.findByRole(Role.HR);

            for (User hr : hrUsers) {

                notificationService.createNotification(
                        hr,
                        "A leave request has been approved by the Manager and is waiting for HR approval."
                );
            }

            notificationService.createNotification(
                    request.getUser(),
                    "Your leave request has been approved by the Manager and forwarded to HR."
            );

            return false;
        }

        // =====================================================
        // NORMAL - MANAGER -> ADMIN
        // =====================================================

        if (
                request.getCategory() != RequestCategory.LEAVE
                        &&
                        currentStep.getRequiredRole() == Role.MANAGER
        ) {

            ApprovalStep adminStep =
                    approvalStepRepository
                            .findFirstByRequestAndStatusOrderByStepOrderAsc(
                                    request,
                                    ApprovalStepStatus.WAITING
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Admin approval step not found"
                                    )
                            );

            adminStep.setStatus(
                    ApprovalStepStatus.PENDING
            );

            approvalStepRepository.save(adminStep);

            List<User> admins =
                    userRepository.findByRole(Role.ADMIN);

            for (User admin : admins) {

                notificationService.createNotification(
                        admin,
                        "A request has been approved by the Manager and is waiting for Admin approval."
                );
            }

            notificationService.createNotification(
                    request.getUser(),
                    "Your request has been approved by the Manager and forwarded to the Administrator."
            );

            return false;
        }

        // =====================================================
        // LEAVE - HR APPROVED
        // =====================================================

        if (
                request.getCategory() == RequestCategory.LEAVE
                        &&
                        currentStep.getRequiredRole() == Role.HR
        ) {

            request.setStatus(
                    RequestStatus.APPROVED
            );

            request.setCompletedAt(
                    LocalDateTime.now()
            );

            notificationService.createNotification(
                    request.getUser(),
                    "Your leave request has been fully approved by HR."
            );

            return true;
        }

        // =====================================================
        // NORMAL - ADMIN APPROVED
        // =====================================================

        if (
                request.getCategory() != RequestCategory.LEAVE
                        &&
                        currentStep.getRequiredRole() == Role.ADMIN
        ) {

            request.setStatus(
                    RequestStatus.APPROVED
            );

            request.setCompletedAt(
                    LocalDateTime.now()
            );

            notificationService.createNotification(
                    request.getUser(),
                    "Your request has been fully approved."
            );

            return true;
        }

        return false;
    }

    // =========================================================
    // REJECT REQUEST
    // =========================================================

    @Transactional
    public void reject(
            Request request,
            User currentUser,
            String comment
    ) {

        if (request == null || request.getId() == null) {
            throw new RuntimeException(
                    "Request not found"
            );
        }

        if (currentUser == null || currentUser.getId() == null) {
            throw new AccessDeniedException(
                    "User not authenticated"
            );
        }

        // =====================================================
        // LOCK REQUEST
        // =====================================================

        Request lockedRequest =
                requestRepository
                        .findByIdForUpdate(request.getId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Request not found"
                                )
                        );

        request = lockedRequest;

        // =====================================================
        // REQUEST MUST STILL BE PENDING
        // =====================================================

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException(
                    "Request is no longer pending"
            );
        }

        // =====================================================
        // GET CURRENT PENDING STEP
        // =====================================================

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

        // =====================================================
        // REJECT CURRENT STEP
        // =====================================================

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

        approvalStepRepository.save(currentStep);

        // =====================================================
        // REJECT REQUEST
        // =====================================================

        request.setStatus(
                RequestStatus.REJECTED
        );

        request.setCompletedAt(
                LocalDateTime.now()
        );

        notificationService.createNotification(
                request.getUser(),
                "Your request has been rejected."
        );
    }

    // =========================================================
    // GET PENDING APPROVALS
    // =========================================================

    public List<Request> getPendingApprovals(
            User currentUser
    ) {

        if (currentUser == null || currentUser.getId() == null) {
            throw new AccessDeniedException(
                    "User not authenticated"
            );
        }

        // =====================================================
        // ADMIN
        // =====================================================

        if (currentUser.getRole() == Role.ADMIN) {

            return approvalStepRepository
                    .findByStatusAndRequiredRoleAndApproverIsNullOrderByStepOrderAsc(
                            ApprovalStepStatus.PENDING,
                            Role.ADMIN
                    )
                    .stream()
                    .map(ApprovalStep::getRequest)
                    .filter(request ->
                            request.getUser() != null
                                    &&
                                    !request.getUser()
                                            .getId()
                                            .equals(currentUser.getId())
                    )
                    .toList();
        }

        // =====================================================
        // MANAGER
        // =====================================================

        if (currentUser.getRole() == Role.MANAGER) {

            return approvalStepRepository
                    .findByStatusAndRequiredRoleAndApproverIdOrderByStepOrderAsc(
                            ApprovalStepStatus.PENDING,
                            Role.MANAGER,
                            currentUser.getId()
                    )
                    .stream()
                    .map(ApprovalStep::getRequest)
                    .toList();
        }

        // =====================================================
        // HR
        // HR CAN APPROVE LEAVE REQUESTS
        // =====================================================

        if (currentUser.getRole() == Role.HR) {

            return approvalStepRepository
                    .findByStatusAndRequiredRoleAndApproverIsNullOrderByStepOrderAsc(
                            ApprovalStepStatus.PENDING,
                            Role.HR
                    )
                    .stream()
                    .map(ApprovalStep::getRequest)
                    .filter(request ->
                            request.getCategory()
                                    == RequestCategory.LEAVE
                    )
                    .filter(request ->
                            request.getUser() != null
                                    &&
                                    !request.getUser()
                                            .getId()
                                            .equals(currentUser.getId())
                    )
                    .toList();
        }

        throw new AccessDeniedException(
                "You do not have approval permissions"
        );
    }

    // =========================================================
    // GET REQUEST WORKFLOW
    // =========================================================

    @Transactional
    public RequestWorkflowDTO getWorkflow(
            Request request,
            User currentUser
    ) {

        if (request == null) {
            throw new RuntimeException(
                    "Request not found"
            );
        }

        if (currentUser == null || currentUser.getId() == null) {
            throw new AccessDeniedException(
                    "User not authenticated"
            );
        }

        // =====================================================
        // GET EXISTING WORKFLOW STEPS
        // =====================================================

        List<ApprovalStep> steps =
                approvalStepRepository
                        .findByRequestOrderByStepOrderAsc(
                                request
                        );

        // =====================================================
        // CREATE WORKFLOW IF MISSING
        // =====================================================

        if (steps.isEmpty()) {

            if (
                    request.getStatus()
                            == RequestStatus.PENDING
            ) {

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

        // =====================================================
        // WORKFLOW ACCESS SECURITY
        // =====================================================

        validateWorkflowViewer(
                request,
                currentUser,
                steps
        );

        // =====================================================
        // CURRENT PENDING STEP
        // =====================================================

        ApprovalStep currentStep =
                steps.stream()
                        .filter(step ->
                                step.getStatus()
                                        == ApprovalStepStatus.PENDING
                        )
                        .findFirst()
                        .orElse(null);

        // =====================================================
        // CURRENT STEP NAME
        // =====================================================

        String currentStepName =
                getCurrentStepName(
                        currentStep,
                        request
                );

        // =====================================================
        // CURRENT APPROVER
        // =====================================================

        String currentApproverName =
                currentStep != null
                        &&
                        currentStep.getApprover() != null
                        ?
                        currentStep
                                .getApprover()
                                .getName()
                        :
                        null;

        // =====================================================
        // CURRENT APPROVER ROLE
        // =====================================================

        Role currentApproverRole =
                currentStep != null
                        ?
                        currentStep.getRequiredRole()
                        :
                        null;

        // =====================================================
        // DTO STEPS
        // =====================================================

        List<RequestWorkflowDTO.StepDTO> stepDTOs =
                steps.stream()
                        .map(step ->
                                new RequestWorkflowDTO.StepDTO(
                                        step.getStepOrder(),
                                        step.getRequiredRole(),
                                        step.getApprover() != null
                                                ?
                                                step.getApprover().getName()
                                                :
                                                null,
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

    // =========================================================
    // VALIDATE WORKFLOW VIEWER
    // =========================================================

    private void validateWorkflowViewer(
            Request request,
            User currentUser,
            List<ApprovalStep> steps
    ) {

        // =====================================================
        // REQUEST OWNER
        // =====================================================

        if (
                request.getUser() != null
                        &&
                        request.getUser()
                                .getId()
                                .equals(currentUser.getId())
        ) {
            return;
        }

        // =====================================================
        // ADMIN
        // =====================================================

        if (currentUser.getRole() == Role.ADMIN) {
            return;
        }

        // =====================================================
        // MANAGER
        // =====================================================

        if (currentUser.getRole() == Role.MANAGER) {

            if (
                    request.getManager() != null
                            &&
                            request.getManager()
                                    .getId()
                                    .equals(currentUser.getId())
            ) {
                return;
            }

            if (steps != null) {

                boolean isAssignedManager =
                        steps.stream()
                                .anyMatch(step ->
                                        step.getRequiredRole()
                                                == Role.MANAGER
                                                &&
                                                step.getApprover() != null
                                                &&
                                                step.getApprover()
                                                        .getId()
                                                        .equals(currentUser.getId())
                                );

                if (isAssignedManager) {
                    return;
                }
            }
        }

        // =====================================================
        // HR
        //
        // HR can view:
        // 1. Its own requests through owner check above.
        // 2. Other LEAVE workflows.
        // =====================================================

        if (
                currentUser.getRole() == Role.HR
                        &&
                        request.getCategory() == RequestCategory.LEAVE
        ) {
            return;
        }

        throw new AccessDeniedException(
                "You are not allowed to view this workflow"
        );
    }

    // =========================================================
    // VALIDATE APPROVER
    // =========================================================

    private void validateApprover(
            ApprovalStep step,
            User user
    ) {

        if (step == null) {
            throw new RuntimeException(
                    "Approval step not found"
            );
        }

        if (user == null || user.getId() == null) {
            throw new AccessDeniedException(
                    "User not authenticated"
            );
        }

        // =====================================================
        // ROLE VALIDATION
        // =====================================================

        if (
                user.getRole()
                        != step.getRequiredRole()
        ) {

            throw new AccessDeniedException(
                    "You are not allowed to approve this step"
            );
        }

        // =====================================================
        // PREVENT SELF APPROVAL
        // =====================================================

        Request request =
                step.getRequest();

        if (
                request.getUser() != null
                        &&
                        request.getUser()
                                .getId()
                                .equals(user.getId())
        ) {

            throw new AccessDeniedException(
                    "You cannot approve your own request"
            );
        }

        // =====================================================
        // MANAGER VALIDATION
        // =====================================================

        if (
                step.getRequiredRole()
                        == Role.MANAGER
        ) {

            if (request.getManager() == null) {

                throw new AccessDeniedException(
                        "No manager assigned to this request"
                );
            }

            if (
                    !request.getManager()
                            .getId()
                            .equals(user.getId())
            ) {

                throw new AccessDeniedException(
                        "You are not the manager assigned to this request"
                );
            }
        }

        // =====================================================
        // SPECIFIC APPROVER
        // =====================================================

        if (
                step.getApprover() != null
                        &&
                        !step.getApprover()
                                .getId()
                                .equals(user.getId())
        ) {

            throw new AccessDeniedException(
                    "This request is assigned to another approver"
            );
        }
    }

    // =========================================================
    // CURRENT STEP NAME
    // =========================================================

    private String getCurrentStepName(
            ApprovalStep currentStep,
            Request request
    ) {

        if (currentStep == null) {

            if (
                    request.getStatus()
                            == RequestStatus.APPROVED
            ) {

                return "COMPLETED";
            }

            if (
                    request.getStatus()
                            == RequestStatus.REJECTED
            ) {

                return "REJECTED";
            }

            return "WAITING";
        }

        if (
                currentStep.getRequiredRole()
                        == Role.MANAGER
        ) {

            return "MANAGER_APPROVAL";
        }

        if (
                currentStep.getRequiredRole()
                        == Role.ADMIN
        ) {

            return "ADMIN_APPROVAL";
        }

        if (
                currentStep.getRequiredRole()
                        == Role.HR
        ) {

            return "HR_APPROVAL";
        }

        return "UNKNOWN";
    }
}

