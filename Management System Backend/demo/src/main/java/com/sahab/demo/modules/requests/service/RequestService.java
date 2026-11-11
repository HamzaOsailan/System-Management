
package com.sahab.demo.modules.requests.service;
import com.sahab.demo.service.FileStorageService;
import com.sahab.demo.service.LeaveService;
import com.sahab.demo.service.OutboxService;


import com.sahab.demo.modules.requests.dto.AdminRequestDTO;
import com.sahab.demo.modules.requests.dto.CreateRequestDTO;
import com.sahab.demo.modules.requests.dto.CreateRequestResponseDTO;
import com.sahab.demo.modules.requests.dto.RequestResponse;
import com.sahab.demo.modules.requests.dto.TeamRequestDTO;
import com.sahab.demo.entity.Request;
import com.sahab.demo.entity.RequestAttachment;
import com.sahab.demo.entity.SlaPolicy;
import com.sahab.demo.entity.User;
import com.sahab.demo.enums.RequestCategory;
import com.sahab.demo.enums.RequestStatus;
import com.sahab.demo.enums.Role;
import com.sahab.demo.exception.ResourceNotFoundException;
import com.sahab.demo.modules.requests.mapper.RequestMapper;
import com.sahab.demo.modules.requests.repository.RequestAttachmentRepository;
import com.sahab.demo.modules.requests.repository.RequestRepository;
import com.sahab.demo.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final RequestAttachmentRepository requestAttachmentRepository;
    private final RequestMapper requestMapper;
    private final LeaveService leaveService;
    private final FileStorageService fileStorageService;
    private final ApprovalWorkflowService approvalWorkflowService;
    private final AIRequestService aiRequestService;
    private final SlaPolicyService slaPolicyService;
    private final OutboxService outboxService;

    private static final long DEFAULT_SLA_HOURS = 4L;
    private static final long DEFAULT_SLA_WARNING_HOURS = 1L;

    // =========================================================
    // CREATE REQUEST
    // =========================================================

    @Transactional
    public Request createRequest(
            @Valid CreateRequestDTO dto,
            MultipartFile file
    ) {

        User currentUser = getCurrentUser();
        User assignedManager = currentUser.getManager();

        // VALIDATE ROLE / MANAGER ASSIGNMENT

        if (currentUser.getRole() == Role.USER) {

            if (assignedManager == null) {
                throw new IllegalArgumentException(
                        "You do not have an assigned manager. Please contact the administrator."
                );
            }

            if (assignedManager.getRole() != Role.MANAGER) {
                throw new IllegalArgumentException(
                        "The assigned manager is not a valid Manager."
                );
            }

        } else if (currentUser.getRole() == Role.MANAGER) {

            // Manager does not need another manager.

        } else if (currentUser.getRole() == Role.ADMIN) {

            // Admin does not need a manager.

        } else if (currentUser.getRole() == Role.HR) {

            // HR does not require an assigned manager.

        } else {

            throw new IllegalArgumentException(
                    "Your role is not allowed to create requests."
            );
        }

        // DURATION

        int durationDays = dto.getDurationDays() == null
                ? 1
                : dto.getDurationDays();

        if (durationDays < 1) {
            throw new IllegalArgumentException(
                    "Duration must be at least 1 day."
            );
        }

        leaveService.validateRequestDuration(
                dto.getCategory(),
                durationDays
        );

        // CREATE REQUEST

        Request request = new Request();

        request.setTitle(dto.getTitle());
        request.setDescription(dto.getDescription());
        request.setCategory(dto.getCategory());

        // NEW: SAVE PRIORITY IF PROVIDED
        if (dto.getPriority() != null) {
            request.setPriority(dto.getPriority());
        }

        request.setDurationDays(durationDays);
        request.setStatus(RequestStatus.PENDING);
        request.setUser(currentUser);

        // MANAGER SNAPSHOT

        if (currentUser.getRole() == Role.USER) {
            request.setManager(assignedManager);
        } else {
            request.setManager(null);
        }

        // CREATED AT / UPDATED AT

        LocalDateTime createdAt = LocalDateTime.now();

        request.setCreatedAt(createdAt);
        request.setUpdatedAt(createdAt);
        request.setCompletedAt(null);

        // SLA FLAGS

        request.setSlaBreached(false);
        request.setSlaWarningSent(false);
        request.setSlaEscalated(false);

        // SLA POLICY

        applySlaPolicy(request, currentUser, createdAt);

        // SAVE REQUEST

        Request savedRequest = requestRepository.save(request);
        outboxService.recordRequestEvent(
                savedRequest,
                "REQUEST_CREATED"
        );
        // ATTACHMENT

        if (file != null && !file.isEmpty()) {

            FileStorageService.StoredFile storedFile =
                    fileStorageService.store(file);

            RequestAttachment attachment = new RequestAttachment();

            attachment.setRequest(savedRequest);
            attachment.setOriginalName(storedFile.originalName());
            attachment.setStoredName(storedFile.storedName());
            attachment.setContentType(storedFile.contentType());
            attachment.setFileSize(storedFile.fileSize());
            attachment.setFilePath(storedFile.filePath());

            requestAttachmentRepository.save(attachment);
        }

        // APPROVAL WORKFLOW

        approvalWorkflowService.createWorkflow(savedRequest);

        // DEBUG

        System.out.println("========== CREATE REQUEST DEBUG ==========");
        System.out.println("Request ID: " + savedRequest.getId());
        System.out.println("Request User: " + currentUser.getEmail());
        System.out.println("User ID: " + currentUser.getId());
        System.out.println("User Role: " + currentUser.getRole());

        System.out.println(
                "Manager ID: "
                        + (assignedManager != null
                        ? assignedManager.getId()
                        : "NONE")
        );

        System.out.println(
                "Manager Name: "
                        + (assignedManager != null
                        ? assignedManager.getName()
                        : "NONE")
        );

        System.out.println(
                "Department: "
                        + (currentUser.getDepartment() != null
                        ? currentUser.getDepartment().getName()
                        : "NONE")
        );

        System.out.println("Category: " + savedRequest.getCategory());
        System.out.println("Priority: " + savedRequest.getPriority());
        System.out.println("Duration: " + savedRequest.getDurationDays());
        System.out.println("Created At: " + savedRequest.getCreatedAt());
        System.out.println("Updated At: " + savedRequest.getUpdatedAt());
        System.out.println("Completed At: " + savedRequest.getCompletedAt());
        System.out.println("SLA Warning At: " + savedRequest.getSlaWarningAt());
        System.out.println("SLA Deadline: " + savedRequest.getSlaDeadline());
        System.out.println("SLA Breached: " + savedRequest.getSlaBreached());
        System.out.println("SLA Warning Sent: " + savedRequest.getSlaWarningSent());
        System.out.println("SLA Escalated: " + savedRequest.getSlaEscalated());
        System.out.println("Has File: " + (file != null && !file.isEmpty()));
        System.out.println("==========================================");

        return savedRequest;
    }

    // =========================================================
    // APPLY SLA POLICY
    // =========================================================

    private void applySlaPolicy(
            Request request,
            User currentUser,
            LocalDateTime createdAt
    ) {

        long durationHours = DEFAULT_SLA_HOURS;
        long warningHours = DEFAULT_SLA_WARNING_HOURS;

        SlaPolicy slaPolicy = null;

        if (currentUser.getDepartment() != null) {

            Long departmentId = currentUser.getDepartment().getId();

            slaPolicy = slaPolicyService.getPolicyForDepartment(
                    departmentId
            );
        }

        if (slaPolicy != null) {

            durationHours = slaPolicy.getDurationHours();
            warningHours = slaPolicy.getWarningHours();

            if (warningHours < 0) {
                warningHours = 0;
            }

            if (warningHours > durationHours) {
                warningHours = durationHours;
            }

            LocalDateTime slaDeadline =
                    createdAt.plusHours(durationHours);

            LocalDateTime slaWarningAt =
                    slaDeadline.minusHours(warningHours);

            request.setSlaDeadline(slaDeadline);
            request.setSlaWarningAt(slaWarningAt);

            System.out.println("========== SLA POLICY APPLIED ==========");
            System.out.println(
                    "Department: "
                            + (currentUser.getDepartment() != null
                            ? currentUser.getDepartment().getName()
                            : "NONE")
            );
            System.out.println("Policy Duration Hours: " + durationHours);
            System.out.println("Policy Warning Hours: " + warningHours);
            System.out.println("Created At: " + createdAt);
            System.out.println("Warning At: " + slaWarningAt);
            System.out.println("Deadline: " + slaDeadline);
            System.out.println("=========================================");

            return;
        }

        LocalDateTime slaDeadline =
                createdAt.plusHours(DEFAULT_SLA_HOURS);

        LocalDateTime slaWarningAt =
                slaDeadline.minusHours(DEFAULT_SLA_WARNING_HOURS);

        request.setSlaDeadline(slaDeadline);
        request.setSlaWarningAt(slaWarningAt);

        System.out.println("========== SLA FALLBACK APPLIED ==========");
        System.out.println(
                "Department: "
                        + (currentUser.getDepartment() != null
                        ? currentUser.getDepartment().getName()
                        : "NONE")
        );
        System.out.println("No SLA policy found.");
        System.out.println("Fallback SLA Duration Hours: " + DEFAULT_SLA_HOURS);
        System.out.println("Fallback Warning Hours: " + DEFAULT_SLA_WARNING_HOURS);
        System.out.println("Created At: " + createdAt);
        System.out.println("Warning At: " + slaWarningAt);
        System.out.println("Deadline: " + slaDeadline);
        System.out.println("===========================================");
    }

    // =========================================================
    // ADMIN - GET ALL REQUESTS
    // =========================================================

    @Transactional(readOnly = true)
    public List<AdminRequestDTO> getAllRequestsForAdmin() {

        return requestRepository.findAll()
                .stream()
                .map(this::toAdminRequestDTO)
                .toList();
    }

    // =========================================================
    // ADMIN REQUEST DTO
    // =========================================================

    @Transactional(readOnly = true)
    private AdminRequestDTO toAdminRequestDTO(Request request) {

        AdminRequestDTO.UserSummary userSummary = null;

        if (request.getUser() != null) {
            userSummary = new AdminRequestDTO.UserSummary(
                    request.getUser().getId(),
                    request.getUser().getName(),
                    request.getUser().getEmail(),
                    request.getUser().getRole() != null
                            ? request.getUser().getRole().name()
                            : null
            );
        }

        boolean hasAttachment = requestAttachmentRepository
                .findByRequestId(request.getId())
                .isPresent();

        return new AdminRequestDTO(
                request.getId(),
                request.getTitle(),
                request.getDescription(),
                request.getStatus(),
                request.getCategory(),
                request.getPriority(),
                request.getCreatedAt(),
                request.getUpdatedAt(),
                request.getApprovedBy(),
                request.getApprovedAt(),
                userSummary,
                hasAttachment,
                request.getSlaDeadline(),
                request.getSlaWarningAt(),
                request.getSlaBreached(),
                request.getSlaWarningSent(),
                request.getSlaEscalated()
        );
    }

    // =========================================================
    // GET ALL REQUESTS
    // =========================================================

    @Transactional(readOnly = true)
    public List<RequestResponse> getAllRequests() {

        return requestRepository.findAll()
                .stream()
                .map(requestMapper::toResponse)
                .toList();
    }

    // =========================================================
    // PAGINATION
    // =========================================================

    @Transactional(readOnly = true)
    public Page<RequestResponse> getAllRequestsPage(Pageable pageable) {

        return requestRepository.findAll(pageable)
                .map(requestMapper::toResponse);
    }

    // =========================================================
    // MY REQUESTS
    // =========================================================

    @Transactional(readOnly = true)
    public List<RequestResponse> getMyRequests() {

        User currentUser = getCurrentUser();

        System.out.println("========== MY REQUESTS DEBUG ==========");
        System.out.println("Current user ID: " + currentUser.getId());
        System.out.println("Current user email: " + currentUser.getEmail());
        System.out.println("Current user role: " + currentUser.getRole());

        List<Request> requests = requestRepository.findByUser(currentUser);

        System.out.println("Requests found: " + requests.size());
        System.out.println("========================================");

        return requests.stream()
                .map(requestMapper::toResponse)
                .toList();
    }

    // =========================================================
    // MY REQUEST BY ID
    // =========================================================

    @Transactional(readOnly = true)
    public RequestResponse getMyRequestById(Long id) {

        User currentUser = getCurrentUser();
        Request request = getRequestById(id);

        if (request.getUser() == null
                || !request.getUser().getId().equals(currentUser.getId())) {

            throw new AccessDeniedException(
                    "You are not allowed to view this request."
            );
        }

        return requestMapper.toResponse(request);
    }

    // =========================================================
    // GET REQUEST BY ID
    // =========================================================

    @Transactional(readOnly = true)
    public Request getRequestById(Long id) {

        return requestRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Request not found")
                );
    }

    // =========================================================
    // REQUEST -> RESPONSE DTO
    // =========================================================

    @Transactional(readOnly = true)
    public RequestResponse toRequestResponse(Request request) {

        return requestMapper.toResponse(request);
    }

    // =========================================================
    // MANAGER REQUESTS
    // =========================================================

    @Transactional(readOnly = true)
    public List<AdminRequestDTO> getManagerRequests() {

        User currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.MANAGER) {
            throw new AccessDeniedException(
                    "Only managers can view manager requests."
            );
        }

        return requestRepository.findByManagerId(currentUser.getId())
                .stream()
                .map(this::toAdminRequestDTO)
                .toList();
    }

    // =========================================================
    // APPROVE REQUEST
    // =========================================================

    @Transactional
    public RequestResponse approveRequest(
            Long requestId,
            String comment
    ) {

        System.out.println(
                "🔥🔥🔥 APPROVE REQUEST SERVICE CALLED 🔥🔥🔥"
        );

        User currentUser = getCurrentUser();
        Request request = getRequestById(requestId);

        approvalWorkflowService.approve(request, currentUser, comment);

        System.out.println("========== AFTER WORKFLOW APPROVE ==========");
        System.out.println("Request ID: " + request.getId());
        System.out.println("Status: " + request.getStatus());
        System.out.println("Completed At: " + request.getCompletedAt());
        System.out.println("Updated At: " + request.getUpdatedAt());
        System.out.println("SLA Deadline: " + request.getSlaDeadline());
        System.out.println("============================================");

        Request savedRequest = requestRepository.save(request);
        outboxService.recordRequestEvent(
                savedRequest,
                "REQUEST_APPROVED"
        );
        if (savedRequest.getStatus() == RequestStatus.APPROVED) {

            LocalDateTime completedAt = LocalDateTime.now();

            savedRequest.setApprovedAt(completedAt);
            savedRequest.setApprovedBy(currentUser.getId());
            savedRequest.setCompletedAt(completedAt);
            savedRequest.setUpdatedAt(completedAt);

            savedRequest = requestRepository.save(savedRequest);

            try {
                aiRequestService.generateAnalysisForAdmin(
                        savedRequest.getId()
                );
            } catch (Exception e) {
                System.err.println(
                        "AI analysis generation failed: " + e.getMessage()
                );
            }
        } else {
            // موافقة على مرحلة من سير العمل، وليست موافقة نهائية
            outboxService.recordRequestEvent(
                    savedRequest,
                    "REQUEST_APPROVAL_STEP_COMPLETED"
            );
        }

        System.out.println("========== APPROVE DEBUG ==========");
        System.out.println("Saved Request ID: " + savedRequest.getId());
        System.out.println("Saved Request Status: " + savedRequest.getStatus());
        System.out.println(
                "Saved Request User ID: "
                        + (savedRequest.getUser() != null
                        ? savedRequest.getUser().getId()
                        : null)
        );
        System.out.println("Saved Request Completed At: " + savedRequest.getCompletedAt());
        System.out.println("Saved Request Updated At: " + savedRequest.getUpdatedAt());
        System.out.println("Saved Request SLA Deadline: " + savedRequest.getSlaDeadline());

        RequestResponse debugResponse =
                requestMapper.toResponse(savedRequest);

        System.out.println("DTO ID: " + debugResponse.getId());
        System.out.println("DTO Status: " + debugResponse.getStatus());
        System.out.println("DTO UserName: " + debugResponse.getUserName());
        System.out.println("DTO UpdatedAt: " + debugResponse.getUpdatedAt());
        System.out.println("🔥🔥🔥 APPROVE RETURNING RequestResponse DTO 🔥🔥🔥");
        System.out.println("DTO CLASS = " + debugResponse.getClass().getName());
        System.out.println("===================================");

        return debugResponse;
    }

    // =========================================================
    // REJECT REQUEST
    // =========================================================

    @Transactional
    public RequestResponse rejectRequest(
            Long requestId,
            String comment
    ) {

        User currentUser = getCurrentUser();
        Request request = getRequestById(requestId);

        approvalWorkflowService.reject(request, currentUser, comment);

        if (request.getStatus() == RequestStatus.REJECTED) {

            LocalDateTime completedAt = LocalDateTime.now();

            request.setCompletedAt(completedAt);
            request.setUpdatedAt(completedAt);
        }

        Request savedRequest = requestRepository.save(request);
        outboxService.recordRequestEvent(
                savedRequest,
                "REQUEST_REJECTED"
        );
        return requestMapper.toResponse(savedRequest);
    }

    // =========================================================
    // UPDATE MY REQUEST
    // =========================================================

    @Transactional
    public Request updateMyRequest(
            Long id,
            @Valid CreateRequestDTO dto
    ) {

        User currentUser = getCurrentUser();
        Request request = getRequestById(id);

        if (request.getUser() == null
                || !request.getUser().getId().equals(currentUser.getId())) {

            throw new AccessDeniedException(
                    "You are not allowed to update this request."
            );
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Only pending requests can be updated."
            );
        }

        int durationDays = dto.getDurationDays() == null
                ? 1
                : dto.getDurationDays();

        if (durationDays < 1) {
            throw new IllegalArgumentException(
                    "Duration must be at least 1 day."
            );
        }

        leaveService.validateRequestDuration(
                dto.getCategory(),
                durationDays
        );

        request.setTitle(dto.getTitle());
        request.setDescription(dto.getDescription());
        request.setCategory(dto.getCategory());

        // NEW: UPDATE PRIORITY IF PROVIDED
        if (dto.getPriority() != null) {
            request.setPriority(dto.getPriority());
        }

        request.setDurationDays(durationDays);
        request.setUpdatedAt(LocalDateTime.now());

        return requestRepository.save(request);
    }

    // =========================================================
    // DELETE MY REQUEST
    // =========================================================

    @Transactional
    public void deleteMyRequest(Long id) {

        User currentUser = getCurrentUser();
        Request request = getRequestById(id);

        if (request.getUser() == null
                || !request.getUser().getId().equals(currentUser.getId())) {

            throw new AccessDeniedException(
                    "You are not allowed to delete this request."
            );
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Only pending requests can be deleted."
            );
        }

        try {
            aiRequestService.deleteAnalysis(request.getId());
        } catch (Exception e) {
            System.err.println(
                    "AI analysis deletion failed: " + e.getMessage()
            );
        }

        requestAttachmentRepository.findByRequest(request)
                .ifPresent(attachment -> {

                    try {
                        fileStorageService.delete(attachment.getFilePath());
                    } catch (Exception e) {
                        System.err.println(
                                "Failed to delete physical file: " + e.getMessage()
                        );
                    }

                    requestAttachmentRepository.delete(attachment);
                });

        requestRepository.delete(request);
    }

    // =========================================================
    // UPDATE STATUS
    // =========================================================

    @Transactional
    public Request updateStatus(
            Long id,
            RequestStatus status
    ) {

        Request request = getRequestById(id);
        request.setStatus(status);

        LocalDateTime now = LocalDateTime.now();
        request.setUpdatedAt(now);

        if (status == RequestStatus.APPROVED
                || status == RequestStatus.REJECTED) {

            if (request.getCompletedAt() == null) {
                request.setCompletedAt(now);
            }

        } else {
            request.setCompletedAt(null);
        }

        return requestRepository.save(request);
    }

    // =========================================================
    // REQUESTS BY CATEGORY
    // =========================================================

    @Transactional(readOnly = true)
    public List<Request> getRequestsByCategory(RequestCategory category) {

        User currentUser = getCurrentUser();

        return requestRepository.findByUser(currentUser)
                .stream()
                .filter(request -> request.getCategory() == category)
                .toList();
    }

    // =========================================================
    // REQUESTS BY STATUS
    // =========================================================

    @Transactional(readOnly = true)
    public List<Request> getRequestsByStatus(RequestStatus status) {

        User currentUser = getCurrentUser();

        return requestRepository.findByUser(currentUser)
                .stream()
                .filter(request -> request.getStatus() == status)
                .toList();
    }

    // =========================================================
    // APPROVED LEAVE TOTAL
    // =========================================================

    @Transactional(readOnly = true)
    public Integer getApprovedLeaveTotal(int year) {

        User currentUser = getCurrentUser();

        Integer total =
                requestRepository.sumApprovedDurationByUserAndCategoryAndYear(
                        currentUser,
                        RequestCategory.LEAVE,
                        RequestStatus.APPROVED,
                        year
                );

        return total != null ? total : 0;
    }

    // =========================================================
    // APPROVED REMOTE WORK TOTAL
    // =========================================================

    @Transactional(readOnly = true)
    public Integer getApprovedRemoteWorkTotal(
            int year,
            int month
    ) {

        User currentUser = getCurrentUser();

        Integer total =
                requestRepository.sumApprovedDurationByUserAndCategoryAndMonth(
                        currentUser,
                        RequestCategory.REMOTE_WORK,
                        RequestStatus.APPROVED,
                        year,
                        month
                );

        return total != null ? total : 0;
    }

    // =========================================================
    // CREATE REQUEST RESPONSE DTO
    // =========================================================

    @Transactional(readOnly = true)
    public CreateRequestResponseDTO toCreateRequestResponse(
            Request request
    ) {

        CreateRequestResponseDTO response =
                new CreateRequestResponseDTO();

        response.setId(request.getId());
        response.setTitle(request.getTitle());
        response.setDescription(request.getDescription());
        response.setStatus(request.getStatus());
        response.setCategory(request.getCategory());
        response.setPriority(request.getPriority());
        response.setDurationDays(request.getDurationDays());
        response.setCreatedAt(request.getCreatedAt());
        response.setSlaDeadline(request.getSlaDeadline());
        response.setSlaWarningAt(request.getSlaWarningAt());
        response.setSlaBreached(request.getSlaBreached());
        response.setSlaWarningSent(request.getSlaWarningSent());
        response.setSlaEscalated(request.getSlaEscalated());

        if (request.getUser() != null) {
            response.setUserId(request.getUser().getId());
            response.setUserName(request.getUser().getName());
        }

        if (request.getManager() != null) {
            response.setManagerId(request.getManager().getId());
            response.setManagerName(request.getManager().getName());
        }

        return response;
    }

    // =========================================================
    // CURRENT USER
    // =========================================================

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException(
                    "User is not authenticated"
            );
        }

        String email = authentication.getName();

        if (email == null || email.isBlank()) {
            throw new AccessDeniedException(
                    "User is not authenticated"
            );
        }

        return userRepository.findByEmail(
                email.trim().toLowerCase()
        ).orElseThrow(() ->
                new ResourceNotFoundException("User not found")
        );
    }

    // =========================================================
    // MY TEAM REQUESTS - CURRENT MANAGER
    // =========================================================

    @Transactional(readOnly = true)
    public List<TeamRequestDTO> getMyTeamRequestsForCurrentUser() {

        User currentUser = getCurrentUser();

        return getMyTeamRequests(currentUser);
    }

    // =========================================================
    // MY TEAM REQUESTS
    // =========================================================

    @Transactional(readOnly = true)
    public List<TeamRequestDTO> getMyTeamRequests(User manager) {

        if (manager == null) {
            throw new ResourceNotFoundException("Manager not found");
        }

        if (manager.getRole() != Role.MANAGER) {
            throw new AccessDeniedException(
                    "Only Managers can access team requests"
            );
        }

        List<Request> requests =
                requestRepository.findByManagerId(manager.getId());

        return requests.stream()
                .map(request -> {

                    TeamRequestDTO dto = new TeamRequestDTO();

                    dto.setId(request.getId());
                    dto.setTitle(request.getTitle());
                    dto.setDescription(request.getDescription());
                    dto.setStatus(request.getStatus());
                    dto.setCategory(request.getCategory());
                    dto.setPriority(request.getPriority());
                    dto.setDurationDays(request.getDurationDays());
                    dto.setCreatedAt(request.getCreatedAt());
                    dto.setUpdatedAt(request.getUpdatedAt());
                    dto.setApprovedAt(request.getApprovedAt());
                    dto.setApprovedBy(request.getApprovedBy());

                    if (request.getUser() != null) {
                        dto.setUserId(request.getUser().getId());
                        dto.setUserName(request.getUser().getName());
                        dto.setUserEmail(request.getUser().getEmail());
                    }

                    if (request.getManager() != null) {
                        dto.setManagerId(request.getManager().getId());
                        dto.setManagerName(request.getManager().getName());
                    }

                    return dto;
                })
                .toList();
    }
}
