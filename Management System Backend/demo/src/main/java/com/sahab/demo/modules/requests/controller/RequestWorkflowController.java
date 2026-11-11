package com.sahab.demo.modules.requests.controller;

import com.sahab.demo.modules.requests.dto.RequestWorkflowDTO;
import com.sahab.demo.entity.Request;
import com.sahab.demo.entity.User;
import com.sahab.demo.modules.requests.repository.RequestRepository;
import com.sahab.demo.repository.UserRepository;
import com.sahab.demo.modules.requests.service.ApprovalWorkflowService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/requests")
public class RequestWorkflowController {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ApprovalWorkflowService approvalWorkflowService;

    public RequestWorkflowController(
            RequestRepository requestRepository,
            UserRepository userRepository,
            ApprovalWorkflowService approvalWorkflowService
    ) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.approvalWorkflowService = approvalWorkflowService;
    }

    @GetMapping("/{id}/workflow")
    public ResponseEntity<RequestWorkflowDTO> getWorkflow(
            @PathVariable Long id
    ) {

        // ==============================
        // 1. Get request
        // ==============================

        Request request = requestRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Request not found")
                );

        // ==============================
        // 2. Get current authenticated user
        // ==============================

        User currentUser = getCurrentUser();

        // ==============================
        // 3. Get workflow
        // ==============================

        return ResponseEntity.ok(
                approvalWorkflowService.getWorkflow(
                        request,
                        currentUser
                )
        );
    }

    // ============================================================
    // Get Current User
    // ============================================================

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (
                authentication == null
                        ||
                        authentication.getName() == null
                        ||
                        authentication.getName().isBlank()
        ) {
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