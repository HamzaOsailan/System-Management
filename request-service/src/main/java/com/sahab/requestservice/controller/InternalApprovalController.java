package com.sahab.requestservice.controller;

import com.sahab.requestservice.dto.ApprovalActionDTO;
import com.sahab.requestservice.entity.ApprovalStep;
import com.sahab.requestservice.entity.RequestRecord;
import com.sahab.requestservice.service.RequestApplicationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@RestController
@RequestMapping("/internal/requests")
public class InternalApprovalController {

    private final RequestApplicationService service;
    private final String internalApiKey;

    public InternalApprovalController(
            RequestApplicationService service,
            @Value("${sahab.internal-api-key}") String internalApiKey) {
        this.service = service;
        this.internalApiKey = internalApiKey;
    }

    private void authorize(String suppliedKey) {
        if (suppliedKey == null || internalApiKey == null
                || internalApiKey.isBlank()
                || !MessageDigest.isEqual(
                    suppliedKey.getBytes(StandardCharsets.UTF_8),
                    internalApiKey.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
    }

    @GetMapping("/{id}/workflow")
    public List<ApprovalStep> workflow(
            @RequestHeader(value = "X-Internal-Api-Key", required = false)
            String key,
            @PathVariable Long id) {
        authorize(key);
        return service.getWorkflow(id);
    }

    @PostMapping("/{id}/approve")
    public RequestRecord approve(
            @RequestHeader(value = "X-Internal-Api-Key", required = false)
            String key,
            @PathVariable Long id,
            @Valid @RequestBody ApprovalActionDTO dto) {
        authorize(key);
        return service.approve(
                id, dto.approverId(), dto.role(), dto.comment());
    }

    @PostMapping("/{id}/reject")
    public RequestRecord reject(
            @RequestHeader(value = "X-Internal-Api-Key", required = false)
            String key,
            @PathVariable Long id,
            @Valid @RequestBody ApprovalActionDTO dto) {
        authorize(key);
        return service.reject(
                id, dto.approverId(), dto.role(), dto.comment());
    }
}