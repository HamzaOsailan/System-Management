
package com.sahab.demo.modules.requests.controller;

import com.fasterxml.jackson.databind.JsonNode;

import com.sahab.demo.modules.requests.dto.AdminRequestDTO;
import com.sahab.demo.modules.requests.dto.CreateRequestDTO;
import com.sahab.demo.modules.requests.dto.PendingApprovalRequestDTO;
import com.sahab.demo.modules.requests.dto.RequestResponse;
import com.sahab.demo.modules.requests.dto.TeamRequestDTO;

import com.sahab.demo.entity.Request;
import com.sahab.demo.entity.User;
import org.springframework.web.bind.annotation.RequestHeader;
import com.sahab.demo.modules.requests.service.IdempotentRequestCreationService;
import com.sahab.demo.enums.RequestStatus;

import com.sahab.demo.repository.UserRepository;

import com.sahab.demo.modules.requests.service.ApprovalWorkflowService;
import com.sahab.demo.modules.requests.service.IdempotentRequestCreationService;
import com.sahab.demo.modules.requests.service.RequestService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.http.MediaType;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import com.sahab.demo.modules.requests.repository.RequestAttachmentRepository;
import java.util.List;

@RestController
@RequestMapping("/requests")
public class RequestController {

    private final RequestService requestService;
    private final ApprovalWorkflowService approvalWorkflowService;
    private final UserRepository userRepository;
    private final RequestAttachmentRepository requestAttachmentRepository;
    private final IdempotentRequestCreationService idempotentRequestCreationService;
    public RequestController(
            RequestService requestService,
            ApprovalWorkflowService approvalWorkflowService,
            UserRepository userRepository, RequestAttachmentRepository requestAttachmentRepository, IdempotentRequestCreationService idempotentRequestCreationService
    ) {
        this.requestService = requestService;
        this.approvalWorkflowService = approvalWorkflowService;
        this.userRepository = userRepository;
        this.requestAttachmentRepository = requestAttachmentRepository;
        this.idempotentRequestCreationService = idempotentRequestCreationService;
    }

    // =========================================================
    // CREATE REQUEST
    // =========================================================


    @PostMapping(
            value = "/CreateRequest",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public RequestResponse createRequest(
            @Valid @RequestPart("data") CreateRequestDTO dto,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestHeader(value = "Idempotency-Key", required = false)
            String idempotencyKey
    ) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            Request request = requestService.createRequest(dto, file);
            return requestService.toRequestResponse(request);
        }

        if (idempotencyKey.length() > 128) {
            throw new IllegalArgumentException(
                    "Idempotency-Key must not exceed 128 characters."
            );
        }

        return idempotentRequestCreationService.create(
                dto,
                file,
                idempotencyKey.trim()
        );
    }


    // =========================================================
    // ALL REQUESTS - ADMIN
    // =========================================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminRequestDTO> getAllRequests() {

        return requestService
                .getAllRequestsForAdmin();
    }

    // =========================================================
    // PAGINATED REQUESTS - ADMIN
    // =========================================================

    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<RequestResponse> getAllRequests(
            Pageable pageable
    ) {

        return requestService
                .getAllRequestsPage(
                        pageable
                );
    }

    // =========================================================
    // PENDING APPROVALS
    // MANAGER / ADMIN / HR
    // =========================================================


    @GetMapping("/pending-approvals")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'HR')")
    public List<PendingApprovalRequestDTO> getPendingApprovals() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()) {
            throw new RuntimeException("User is not authenticated");
        }

        User currentUser = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Request> requests =
                approvalWorkflowService.getPendingApprovals(currentUser);

        return requests.stream()
                .map(request -> {

                    PendingApprovalRequestDTO.UserSummary userSummary = null;

                    if (request.getUser() != null) {
                        userSummary =
                                new PendingApprovalRequestDTO.UserSummary(
                                        request.getUser().getId(),
                                        request.getUser().getName(),
                                        request.getUser().getEmail()
                                );
                    }

                    boolean hasAttachment =
                            requestAttachmentRepository
                                    .findByRequestId(request.getId())
                                    .isPresent();

                    return new PendingApprovalRequestDTO(
                            request.getId(),
                            request.getTitle(),
                            request.getDescription(),
                            request.getStatus(),
                            request.getCategory(),
                            request.getPriority(),
                            request.getCreatedAt(),
                            userSummary,
                            request.getDurationDays(),
                            request.getUpdatedAt(),
                            request.getApprovedAt(),
                            request.getApprovedBy(),
                            request.getSlaDeadline(),
                            request.getSlaWarningAt(),
                            request.getSlaBreached(),
                            request.getSlaWarningSent(),
                            request.getSlaEscalated(),
                            hasAttachment
                    );
                })
                .toList();
    }

    // =========================================================
    // MY REQUESTS
    // =========================================================

    @GetMapping("/my")
    public List<RequestResponse> getMyRequests() {

        return requestService
                .getMyRequests();
    }

    // =========================================================
    // MY REQUEST BY ID
    // =========================================================

    @GetMapping("/my/{id}")
    public RequestResponse getMyRequestById(
            @PathVariable Long id
    ) {

        return requestService
                .getMyRequestById(id);
    }

    // =========================================================
    // TEAM REQUESTS - MANAGER
    // =========================================================

    @GetMapping("/team")
    @PreAuthorize("hasRole('MANAGER')")
    public List<TeamRequestDTO>
    getMyTeamRequests() {

        return requestService
                .getMyTeamRequestsForCurrentUser();
    }

    // =========================================================
    // REQUESTS BY STATUS
    // ADMIN ONLY
    // =========================================================

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<RequestResponse> getRequestsByStatus(
            @PathVariable RequestStatus status
    ) {

        return requestService
                .getRequestsByStatus(status)
                .stream()
                .map(requestService::toRequestResponse)
                .toList();
    }

    // =========================================================
    // APPROVE REQUEST
    // MANAGER / ADMIN / HR
    // =========================================================

    @PutMapping("/{id}/approve")
    @PreAuthorize(
            "hasAnyRole('MANAGER', 'ADMIN', 'HR')"
    )
    public RequestResponse approveRequest(
            @PathVariable Long id,
            @RequestBody(required = false)
            JsonNode body
    ) {

        String comment = null;

        if (
                body != null &&
                        body.has("comment") &&
                        !body.get("comment").isNull()
        ) {

            comment =
                    body.get("comment")
                            .asText();
        }

        return requestService
                .approveRequest(
                        id,
                        comment
                );
    }

    // =========================================================
    // REJECT REQUEST
    // MANAGER / ADMIN / HR
    // =========================================================

    @PutMapping("/{id}/reject")
    @PreAuthorize(
            "hasAnyRole('MANAGER', 'ADMIN', 'HR')"
    )
    public RequestResponse rejectRequest(
            @PathVariable Long id,
            @RequestBody(required = false)
            JsonNode body
    ) {

        String comment = null;

        if (
                body != null &&
                        body.has("comment") &&
                        !body.get("comment").isNull()
        ) {

            comment =
                    body.get("comment")
                            .asText();
        }

        return requestService
                .rejectRequest(
                        id,
                        comment
                );
    }

    // =========================================================
    // UPDATE MY REQUEST
    // =========================================================

    @PutMapping("/{id}")
    public RequestResponse updateMyRequest(
            @PathVariable Long id,
            @Valid @RequestBody CreateRequestDTO dto
    ) {

        Request request =
                requestService.updateMyRequest(
                        id,
                        dto
                );

        return requestService.toRequestResponse(
                request
        );
    }

    // =========================================================
    // DELETE MY REQUEST
    // =========================================================

    @DeleteMapping("/{id}")
    public void deleteMyRequest(
            @PathVariable Long id
    ) {

        requestService.deleteMyRequest(
                id
        );
    }

    // =========================================================
    // REQUEST BY ID - ADMIN
    // =========================================================

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public RequestResponse getRequestById(
            @PathVariable Long id
    ) {

        Request request =
                requestService
                        .getRequestById(id);

        return requestService.toRequestResponse(
                request
        );
    }
}

