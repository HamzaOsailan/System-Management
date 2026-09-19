
        package com.sahab.demo.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.sahab.demo.dto.CreateRequestDTO;
import com.sahab.demo.dto.PendingApprovalRequestDTO;
import com.sahab.demo.entity.Request;
import com.sahab.demo.entity.User;
import com.sahab.demo.repository.UserRepository;
import com.sahab.demo.service.ApprovalWorkflowService;
import com.sahab.demo.service.RequestService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/requests")
@CrossOrigin("*")
public class RequestController {

    private final RequestService requestService;
    private final UserRepository userRepository;
    private final ApprovalWorkflowService approvalWorkflowService;

    public RequestController(
            RequestService requestService,
            UserRepository userRepository,
            ApprovalWorkflowService approvalWorkflowService
    ) {
        this.requestService = requestService;
        this.userRepository = userRepository;
        this.approvalWorkflowService = approvalWorkflowService;
    }

    // =========================================================
    // CREATE REQUEST
    // =========================================================

    @PostMapping("/CreateRequest")
    public Request createRequest(
            @Valid @RequestBody CreateRequestDTO dto
    ) {
        return requestService.createRequest(dto);
    }

    // =========================================================
    // GET PENDING APPROVALS
    // ADMIN / MANAGER
    // =========================================================


    @GetMapping("/pending-approvals")
    public List<PendingApprovalRequestDTO> getPendingApprovals() {

        User currentUser = getCurrentUser();

        return approvalWorkflowService
                .getPendingApprovals(currentUser)
                .stream()
                .map(request ->
                        new PendingApprovalRequestDTO(
                                request.getId(),
                                request.getTitle(),
                                request.getDescription(),
                                request.getStatus(),
                                request.getCategory(),
                                request.getPriority(),
                                request.getCreatedAt(),
                                request.getUser() == null
                                        ? null
                                        : new PendingApprovalRequestDTO.UserSummary(
                                        request.getUser().getId(),
                                        request.getUser().getName(),
                                        request.getUser().getEmail()
                                )
                        )
                )
                .toList();
    }


    // =========================================================
    // GET ALL REQUESTS
    // =========================================================

    @GetMapping
    public List<Request> getAllRequests() {

        return requestService.getAllRequests();
    }

    // =========================================================
    // GET REQUESTS WITH PAGINATION
    // =========================================================

    @GetMapping("/page")
    public Page<Request> getAllRequests(
            Pageable pageable
    ) {

        return requestService.getAllRequests(
                pageable
        );
    }

    // =========================================================
    // GET MY REQUESTS
    // =========================================================

    @GetMapping("/my")
    public List<Request> getMyRequests() {

        return requestService.getMyRequests();
    }

    // =========================================================
    // GET MY REQUEST BY ID
    // =========================================================

    @GetMapping("/my/{id}")
    public Request getMyRequestById(
            @PathVariable Long id
    ) {

        return requestService.getMyRequestById(id);
    }

    // =========================================================
    // APPROVE REQUEST
    // =========================================================

    @PutMapping("/{id}/approve")
    public Request approveRequest(
            @PathVariable Long id,
            @RequestBody(required = false) JsonNode body
    ) {

        String comment = null;

        if (body != null &&
                body.has("comment") &&
                !body.get("comment").isNull()) {

            comment = body.get("comment").asText();
        }

        return requestService.approveRequest(
                id,
                comment
        );
    }

    // =========================================================
    // REJECT REQUEST
    // =========================================================

    @PutMapping("/{id}/reject")
    public Request rejectRequest(
            @PathVariable Long id,
            @RequestBody(required = false) JsonNode body
    ) {

        String comment = null;

        if (body != null &&
                body.has("comment") &&
                !body.get("comment").isNull()) {

            comment = body.get("comment").asText();
        }

        return requestService.rejectRequest(
                id,
                comment
        );
    }

    // =========================================================
    // UPDATE MY REQUEST
    // =========================================================

    @PutMapping("/{id}")
    public Request updateMyRequest(
            @PathVariable Long id,
            @Valid @RequestBody CreateRequestDTO dto
    ) {

        return requestService.updateMyRequest(
                id,
                dto
        );
    }

    // =========================================================
    // DELETE MY REQUEST
    // =========================================================

    @DeleteMapping("/{id}")
    public void deleteMyRequest(
            @PathVariable Long id
    ) {

        requestService.deleteMyRequest(id);
    }

    // =========================================================
    // GET REQUESTS BY STATUS
    // =========================================================

    @GetMapping("/status/{status}")
    public List<Request> getRequestsByStatus(
            @PathVariable String status
    ) {

        return requestService.getRequestsByStatus(
                status
        );
    }

    // =========================================================
    // CURRENT USER
    // =========================================================

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication.getName() == null) {

            throw new RuntimeException(
                    "User is not authenticated"
            );
        }

        return userRepository
                .findByEmail(
                        authentication.getName()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );
    }
}

