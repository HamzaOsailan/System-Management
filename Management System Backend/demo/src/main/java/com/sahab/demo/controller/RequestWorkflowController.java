
        package com.sahab.demo.controller;

import com.sahab.demo.dto.RequestWorkflowDTO;
import com.sahab.demo.entity.Request;
import com.sahab.demo.entity.User;
import com.sahab.demo.repository.RequestRepository;
import com.sahab.demo.repository.UserRepository;
import com.sahab.demo.service.ApprovalWorkflowService;
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

        Request request = requestRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Request not found")
                );

        User currentUser = getCurrentUser();

        boolean isOwner =
                request.getUser()
                        .getId()
                        .equals(currentUser.getId());

        boolean isAdmin =
                currentUser.getRole().name()
                        .equals("ADMIN");

        boolean isManager =
                currentUser.getRole().name()
                        .equals("MANAGER");

        if (!isOwner && !isAdmin && !isManager) {
            throw new RuntimeException(
                    "You are not allowed to view this workflow"
            );
        }

        return ResponseEntity.ok(
                approvalWorkflowService.getWorkflow(request)
        );
    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
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

