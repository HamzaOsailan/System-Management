package com.sahab.requestservice.controller;

import com.sahab.requestservice.dto.CreateRequestDTO;
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
public class InternalRequestController {

    private final RequestApplicationService service;
    private final String internalApiKey;

    public InternalRequestController(
            RequestApplicationService service,
            @Value("${sahab.internal-api-key}") String internalApiKey) {
        this.service = service;
        this.internalApiKey = internalApiKey;
    }

    private void authorize(String suppliedKey) {
        if (suppliedKey == null || internalApiKey.isBlank()
                || !MessageDigest.isEqual(
                    suppliedKey.getBytes(StandardCharsets.UTF_8),
                    internalApiKey.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RequestRecord create(
            @RequestHeader(value = "X-Internal-Api-Key", required = false) String key,
            @Valid @RequestBody CreateRequestDTO dto) {
        authorize(key);
        return service.create(dto);
    }

    @GetMapping("/{id}")
    public RequestRecord getById(
            @RequestHeader(value = "X-Internal-Api-Key", required = false) String key,
            @PathVariable Long id) {
        authorize(key);
        return service.getById(id);
    }

    @GetMapping("/user/{userId}")
    public List<RequestRecord> getForUser(
            @RequestHeader(value = "X-Internal-Api-Key", required = false) String key,
            @PathVariable Long userId) {
        authorize(key);
        return service.getRequestsForUser(userId);
    }
}
