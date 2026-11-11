
package com.sahab.demo.modules.requests.service;
import com.sahab.demo.service.IdempotencyReservationService;
import com.sahab.demo.service.IdempotencyService;


import com.sahab.demo.modules.requests.dto.CreateRequestDTO;
import com.sahab.demo.modules.requests.dto.RequestResponse;
import com.sahab.demo.entity.IdempotencyRecord;
import com.sahab.demo.entity.Request;
import com.sahab.demo.exception.IdempotencyConflictException;
import com.sahab.demo.repository.IdempotencyRecordRepository;
import com.sahab.demo.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.TimeUnit;

@Service
public class IdempotentRequestCreationService {

    private static final long WAIT_TIMEOUT_MS = 2000L;
    private static final long POLL_INTERVAL_MS = 50L;

    private final RequestService requestService;
    private final UserRepository userRepository;
    private final IdempotencyService idempotencyService;
    private final IdempotencyReservationService reservationService;
    private final IdempotencyRecordRepository recordRepository;
    private final IdempotentRequestExecutionService executionService;

    public IdempotentRequestCreationService(
            RequestService requestService,
            UserRepository userRepository,
            IdempotencyService idempotencyService,
            IdempotencyReservationService reservationService,
            IdempotencyRecordRepository recordRepository,
            IdempotentRequestExecutionService executionService
    ) {
        this.requestService = requestService;
        this.userRepository = userRepository;
        this.idempotencyService = idempotencyService;
        this.reservationService = reservationService;
        this.recordRepository = recordRepository;
        this.executionService = executionService;
    }

    public RequestResponse create(
            CreateRequestDTO dto,
            MultipartFile file,
            String idempotencyKey
    ) {
        Long userId = getCurrentUserId();

        String requestHash =
                idempotencyService.createRequestHash(dto, file);

        IdempotencyRecord record;

        try {
            record = reservationService.reserve(
                    userId,
                    idempotencyKey,
                    requestHash
            );
        } catch (DataIntegrityViolationException ex) {
            return handleExistingRecord(
                    userId,
                    idempotencyKey,
                    requestHash
            );
        }

        try {
            return executionService.createAndComplete(
                    dto,
                    file,
                    record.getId()
            );
        } catch (RuntimeException ex) {
            reservationService.deleteReservation(record.getId());
            throw ex;
        }
    }

    private RequestResponse handleExistingRecord(
            Long userId,
            String idempotencyKey,
            String requestHash
    ) {
        IdempotencyRecord existing = reservationService.find(
                userId,
                idempotencyKey
        );

        if (existing == null) {
            throw new IdempotencyConflictException(
                    "The request is being processed. Please retry shortly."
            );
        }

        // A key cannot be reused for different request data.
        idempotencyService.validateSameRequest(
                existing.getRequestHash(),
                requestHash
        );

        // Return the cached response immediately if already completed.
        if (isCompleted(existing)) {
            return deserializeResponse(existing);
        }

        // If the first request is still running, wait briefly for it.
        long deadline = System.nanoTime()
                + TimeUnit.MILLISECONDS.toNanos(WAIT_TIMEOUT_MS);

        while (System.nanoTime() < deadline) {
            try {
                Thread.sleep(POLL_INTERVAL_MS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();

                throw new IdempotencyConflictException(
                        "Waiting for the original request was interrupted. Please retry."
                );
            }

            // find() is called again to retrieve the latest database state.
            existing = reservationService.find(
                    userId,
                    idempotencyKey
            );

            if (existing == null) {
                throw new IdempotencyConflictException(
                        "The original request could not be found. Please retry."
                );
            }

            idempotencyService.validateSameRequest(
                    existing.getRequestHash(),
                    requestHash
            );

            if (isCompleted(existing)) {
                return deserializeResponse(existing);
            }
        }

        throw new IdempotencyConflictException(
                "The request is still being processed. Please retry shortly."
        );
    }

    private boolean isCompleted(IdempotencyRecord record) {
        return "COMPLETED".equals(record.getOperationStatus())
                && record.getResponseBody() != null;
    }

    private RequestResponse deserializeResponse(
            IdempotencyRecord record
    ) {
        return idempotencyService.deserializeResponse(
                record.getResponseBody()
        );
    }

    @Transactional
    public RequestResponse createAndComplete(
            CreateRequestDTO dto,
            MultipartFile file,
            Long reservationId
    ) {
        Request request = requestService.createRequest(dto, file);

        RequestResponse response =
                requestService.toRequestResponse(request);

        reservationService.complete(
                reservationId,
                request.getId(),
                idempotencyService.serializeResponse(response),
                HttpStatus.OK.value()
        );

        return response;
    }

    private Long getCurrentUserId() {
        org.springframework.security.core.Authentication authentication =
                org.springframework.security.core.context.SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Authentication is required."
            );
        }

        return userRepository.findByEmail(
                authentication.getName().trim().toLowerCase()
        ).orElseThrow(() ->
                new org.springframework.security.access.AccessDeniedException(
                        "Authenticated user was not found."
                )
        ).getId();
    }
}
