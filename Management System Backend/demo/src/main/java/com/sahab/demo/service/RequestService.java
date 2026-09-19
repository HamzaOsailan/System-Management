
        package com.sahab.demo.service;

import com.sahab.demo.dto.CreateRequestDTO;
import com.sahab.demo.entity.Request;
import com.sahab.demo.entity.User;
import com.sahab.demo.enums.RequestStatus;
import com.sahab.demo.exception.ResourceNotFoundException;
import com.sahab.demo.repository.RequestRepository;
import com.sahab.demo.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RequestService {

    private static final Logger logger =
            LoggerFactory.getLogger(RequestService.class);

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AIRequestService aiRequestService;
    private final ApprovalWorkflowService approvalWorkflowService;

    public RequestService(
            RequestRepository requestRepository,
            UserRepository userRepository,
            NotificationService notificationService,
            AIRequestService aiRequestService,
            ApprovalWorkflowService approvalWorkflowService
    ) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.aiRequestService = aiRequestService;
        this.approvalWorkflowService = approvalWorkflowService;
    }

    // =========================================================
    // CREATE REQUEST
    // =========================================================

    @Transactional

    public Request createRequest(CreateRequestDTO dto) {
        User user = getCurrentUser();
        Request request = new Request();
        request.setTitle(dto.getTitle());
        request.setDescription(dto.getDescription());
        request.setUser(user);
        if (dto.getCategory() != null) {
            request.setCategory(dto.getCategory());
        }
        request.setStatus(RequestStatus.PENDING);
        request.setCreatedAt(LocalDateTime.now());
        Request saved = requestRepository.save(request);
        approvalWorkflowService.createWorkflow(saved);
        logger.info( "Request created with workflow. id={}, user={}", saved.getId(), user.getEmail() );
        return saved;
    }

    // =========================================================
    // GET ALL REQUESTS
    // =========================================================

    public List<Request> getAllRequests() {

        return requestRepository.findAll();
    }

    // =========================================================
    // GET ALL REQUESTS WITH PAGINATION
    // =========================================================

    public Page<Request> getAllRequests(Pageable pageable) {

        return requestRepository.findAll(pageable);
    }

    // =========================================================
    // GET MY REQUESTS
    // =========================================================

    public List<Request> getMyRequests() {

        User user = getCurrentUser();

        return requestRepository.findByUser(user);
    }

    // =========================================================
    // GET MY REQUEST BY ID
    // =========================================================

    public Request getMyRequestById(Long id) {

        User currentUser = getCurrentUser();

        Request request =
                requestRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Request not found"
                                )
                        );

        if (request.getUser() == null ||
                !request.getUser()
                        .getId()
                        .equals(currentUser.getId())) {

            throw new RuntimeException(
                    "You are not allowed to view this request"
            );
        }

        return request;
    }

    // =========================================================
    // GET REQUESTS BY USER ID
    // =========================================================

    public List<Request> getMyRequests(Long userId) {

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                )
                        );

        return requestRepository.findByUser(user);
    }

    // =========================================================
    // APPROVE REQUEST
    // =========================================================

    public Request approveRequest(
            Long requestId,
            String comment
    ) {

        Request request =
                requestRepository
                        .findById(requestId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Request not found"
                                )
                        );

        User currentUser = getCurrentUser();

        boolean fullyApproved =
                approvalWorkflowService.approve(
                        request,
                        currentUser,
                        comment
                );

        Request saved =
                requestRepository.save(request);

        // Final approval only
        if (fullyApproved) {

            saved.setApprovedAt(
                    LocalDateTime.now()
            );

            saved.setApprovedBy(
                    currentUser.getId()
            );

            saved =
                    requestRepository.save(saved);

            // AI starts only after all approvals
            try {

                aiRequestService.analyzeRequest(saved);

            } catch (Exception e) {

                logger.error(
                        "AI analysis failed for request {}",
                        saved.getId(),
                        e
                );
            }
        }

        return saved;
    }

    // =========================================================
    // REJECT REQUEST
    // =========================================================

    public Request rejectRequest(
            Long requestId,
            String comment
    ) {

        logger.info(
                "Attempting to reject request id: {}",
                requestId
        );

        Request request =
                requestRepository
                        .findById(requestId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Request not found"
                                )
                        );

        User currentUser = getCurrentUser();

        approvalWorkflowService.reject(
                request,
                currentUser,
                comment
        );

        Request saved =
                requestRepository.save(request);

        logger.info(
                "Request rejected successfully. id={}, user={}",
                requestId,
                currentUser.getEmail()
        );

        return saved;
    }

    // =========================================================
    // GET REQUESTS BY STATUS
    // =========================================================

    public List<Request> getRequestsByStatus(
            String status
    ) {

        RequestStatus requestStatus;

        try {

            requestStatus =
                    RequestStatus.valueOf(
                            status.toUpperCase()
                    );

        } catch (IllegalArgumentException e) {

            throw new IllegalArgumentException(
                    "Invalid request status: " + status
            );
        }

        return requestRepository.findByStatus(
                requestStatus
        );
    }

    // =========================================================
    // UPDATE MY REQUEST
    // =========================================================

    public Request updateMyRequest(
            Long id,
            CreateRequestDTO dto
    ) {

        User currentUser = getCurrentUser();

        Request request =
                requestRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Request not found"
                                )
                        );

        if (request.getUser() == null ||
                !request.getUser()
                        .getId()
                        .equals(currentUser.getId())) {

            throw new RuntimeException(
                    "You are not allowed to edit this request"
            );
        }

        if (request.getStatus() !=
                RequestStatus.PENDING) {

            throw new RuntimeException(
                    "Only pending requests can be edited"
            );
        }

        request.setTitle(dto.getTitle());
        request.setDescription(dto.getDescription());

        if (dto.getCategory() != null) {
            request.setCategory(dto.getCategory());
        }

        return requestRepository.save(request);
    }

    // =========================================================
    // DELETE MY REQUEST
    // =========================================================

    public void deleteMyRequest(Long id) {

        User currentUser = getCurrentUser();

        Request request =
                requestRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Request not found"
                                )
                        );

        if (request.getUser() == null ||
                !request.getUser()
                        .getId()
                        .equals(currentUser.getId())) {

            throw new RuntimeException(
                    "You are not allowed to delete this request"
            );
        }

        if (request.getStatus() !=
                RequestStatus.PENDING) {

            throw new RuntimeException(
                    "Only pending requests can be deleted"
            );
        }

        aiRequestService.deleteAnalysis(id);

        requestRepository.delete(request);

        logger.info(
                "Request deleted successfully. id={}, user={}",
                id,
                currentUser.getEmail()
        );
    }

    // =========================================================
    // CURRENT USER
    // =========================================================

    private User getCurrentUser() {

        Authentication auth =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (auth == null ||
                !auth.isAuthenticated()) {

            throw new RuntimeException(
                    "User is not authenticated"
            );
        }

        return userRepository
                .findByEmail(auth.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );
    }
}
