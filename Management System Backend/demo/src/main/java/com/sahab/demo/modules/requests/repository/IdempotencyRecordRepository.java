package com.sahab.demo.modules.requests.repository;
import com.sahab.demo.entity.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyRecordRepository
        extends JpaRepository<IdempotencyRecord, Long> {

    Optional<IdempotencyRecord> findByUserIdAndIdempotencyKey(
            Long userId,
            String idempotencyKey
    );

    boolean existsByUserIdAndIdempotencyKey(
            Long userId,
            String idempotencyKey
    );

}
