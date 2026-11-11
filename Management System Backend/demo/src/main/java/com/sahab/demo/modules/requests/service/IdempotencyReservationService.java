package com.sahab.demo.modules.requests.service;

import com.sahab.demo.entity.IdempotencyRecord;
import com.sahab.demo.modules.requests.repository.IdempotencyRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdempotencyReservationService {

    private final IdempotencyRecordRepository repository;

    public IdempotencyReservationService(
            IdempotencyRecordRepository repository
    ) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public IdempotencyRecord reserve(
            Long userId,
            String idempotencyKey,
            String requestHash
    ) {
        IdempotencyRecord record = new IdempotencyRecord();

        record.setUserId(userId);
        record.setIdempotencyKey(idempotencyKey);
        record.setRequestHash(requestHash);
        record.setOperationStatus("PROCESSING");

        return repository.saveAndFlush(record);
    }

    @Transactional(readOnly = true)
    public IdempotencyRecord find(
            Long userId,
            String idempotencyKey
    ) {
        return repository
                .findByUserIdAndIdempotencyKey(
                        userId,
                        idempotencyKey
                )
                .orElse(null);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void complete(
            Long recordId,
            Long requestId,
            String responseBody,
            int responseStatus
    ) {
        IdempotencyRecord record = repository.findById(recordId)
                .orElseThrow(() -> new IllegalStateException(
                        "Idempotency record not found"
                ));

        record.setRequestId(requestId);
        record.setResponseBody(responseBody);
        record.setResponseStatus(responseStatus);
        record.setOperationStatus("COMPLETED");
        record.setCompletedAt(java.time.LocalDateTime.now());

        repository.save(record);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteReservation(Long recordId) {
        if (recordId == null) {
            return;
        }

        repository.deleteById(recordId);
    }

}
