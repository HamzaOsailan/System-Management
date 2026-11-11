package com.sahab.requestservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sahab.requestservice.dto.CreateRequestDTO;
import com.sahab.requestservice.entity.ApprovalStep;
import com.sahab.requestservice.entity.ApprovalStepStatus;
import com.sahab.requestservice.entity.OutboxEvent;
import com.sahab.requestservice.entity.RequestRecord;
import com.sahab.requestservice.entity.RequestStatus;
import com.sahab.requestservice.repository.ApprovalStepRepository;
import com.sahab.requestservice.repository.OutboxEventRepository;
import com.sahab.requestservice.repository.RequestRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RequestApplicationService {

    private final RequestRepository requestRepository;
    private final ApprovalStepRepository approvalRepository;
    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public RequestApplicationService(
            RequestRepository requestRepository,
            ApprovalStepRepository approvalRepository,
            OutboxEventRepository outboxRepository,
            ObjectMapper objectMapper) {
        this.requestRepository = requestRepository;
        this.approvalRepository = approvalRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public RequestRecord create(CreateRequestDTO dto) {
        RequestRecord request = new RequestRecord();
        request.setTitle(dto.title().trim());
        request.setDescription(dto.description().trim());
        request.setCategory(dto.category());
        request.setPriority(dto.priority());
        request.setDurationDays(dto.durationDays());
        request.setUserId(dto.userId());
        request.setManagerId(dto.managerId());
        request.setStatus(RequestStatus.PENDING);

        RequestRecord saved = requestRepository.saveAndFlush(request);

        // If a manager is assigned, the manager acts first.
        // Otherwise, the admin is the first approver.
        if (saved.getManagerId() != null
                && !saved.getManagerId().equals(saved.getUserId())) {
            createStep(saved.getId(), 1, "MANAGER",
                    saved.getManagerId(), ApprovalStepStatus.PENDING);
            createStep(saved.getId(), 2, "ADMIN",
                    null, ApprovalStepStatus.WAITING);
        } else {
            createStep(saved.getId(), 1, "ADMIN",
                    null, ApprovalStepStatus.PENDING);
        }

        publishEvent("REQUEST_CREATED", saved, null, null, null);
        return saved;
    }

    @Transactional
    public RequestRecord approve(
            Long requestId, Long approverId,
            String role, String comment) {
        return applyDecision(requestId, approverId, role, comment, true);
    }

    @Transactional
    public RequestRecord reject(
            Long requestId, Long approverId,
            String role, String comment) {
        return applyDecision(requestId, approverId, role, comment, false);
    }

    private RequestRecord applyDecision(
            Long requestId, Long approverId,
            String role, String comment, boolean approved) {

        if (approverId == null || approverId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Valid approverId is required");
        }

        String normalizedRole = role == null ? "" : role.trim().toUpperCase();
        if (!normalizedRole.equals("MANAGER")
                && !normalizedRole.equals("ADMIN")) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Role cannot approve requests");
        }

        RequestRecord request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Request not found"));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Request is no longer pending");
        }

        if (approverId.equals(request.getUserId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Users cannot approve their own requests");
        }

        List<ApprovalStep> steps =
                approvalRepository.findByRequestIdOrderByStepOrderAsc(requestId);

        ApprovalStep current = steps.stream()
                .filter(s -> s.getStatus() == ApprovalStepStatus.PENDING)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT, "No pending approval step"));

        if (!current.getRequiredRole().equalsIgnoreCase(normalizedRole)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "This approval step belongs to another role");
        }

        if ("MANAGER".equals(normalizedRole)
                && (request.getManagerId() == null
                || !request.getManagerId().equals(approverId))) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Only the assigned manager can approve");
        }

        current.setApproverId(approverId);
        current.setActionDate(LocalDateTime.now());
        current.setComment(normalizeComment(comment));
        current.setStatus(approved
                ? ApprovalStepStatus.APPROVED
                : ApprovalStepStatus.REJECTED);

        if (!approved) {
            request.setStatus(RequestStatus.REJECTED);
            requestRepository.save(request);
            approvalRepository.save(current);
            publishEvent("REQUEST_REJECTED", request,
                    approverId, normalizedRole, comment);
            return request;
        }

        ApprovalStep next = steps.stream()
                .filter(s -> s.getStepOrder() > current.getStepOrder())
                .filter(s -> s.getStatus() == ApprovalStepStatus.WAITING)
                .findFirst()
                .orElse(null);

        if (next != null) {
            next.setStatus(ApprovalStepStatus.PENDING);
            approvalRepository.save(next);
        } else {
            request.setStatus(RequestStatus.APPROVED);
        }

        approvalRepository.save(current);
        requestRepository.save(request);

        publishEvent(
                next == null ? "REQUEST_APPROVED" : "REQUEST_APPROVAL_STEP_APPROVED",
                request, approverId, normalizedRole, comment);

        return request;
    }

    private void createStep(
            Long requestId, int order, String role,
            Long approverId, ApprovalStepStatus status) {
        ApprovalStep step = new ApprovalStep();
        step.setRequestId(requestId);
        step.setStepOrder(order);
        step.setRequiredRole(role);
        step.setApproverId(approverId);
        step.setStatus(status);
        approvalRepository.save(step);
    }

    private String normalizeComment(String comment) {
        if (comment == null || comment.isBlank()) {
            return null;
        }
        return comment.trim();
    }

    private void publishEvent(
            String type, RequestRecord request,
            Long actorId, String actorRole, String comment) {

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("eventId", UUID.randomUUID().toString());
        payload.put("eventType", type);
        payload.put("aggregateType", "REQUEST");
        payload.put("aggregateId", request.getId());
        payload.put("requestId", request.getId());
        payload.put("userId", request.getUserId());
        payload.put("managerId", request.getManagerId());
        payload.put("status", request.getStatus().name());
        payload.put("actorId", actorId);
        payload.put("actorRole", actorRole);
        payload.put("comment", comment);
        payload.put("occurredAt", LocalDateTime.now().toString());

        OutboxEvent event = new OutboxEvent();
        event.setEventType(type);
        event.setAggregateType("REQUEST");
        event.setAggregateId(request.getId());

        try {
            event.setPayload(objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(
                    "Could not serialize request event", ex);
        }

        outboxRepository.save(event);
    }

    @Transactional(readOnly = true)
    public List<RequestRecord> getRequestsForUser(Long userId) {
        return requestRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public RequestRecord getById(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException(
                        "Request not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<ApprovalStep> getWorkflow(Long requestId) {
        if (!requestRepository.existsById(requestId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Request not found");
        }
        return approvalRepository.findByRequestIdOrderByStepOrderAsc(requestId);
    }
}