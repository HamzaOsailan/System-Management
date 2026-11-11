
package com.sahab.demo.modules.infrastructure.outbox.repository;

import com.sahab.demo.modules.infrastructure.outbox.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventRepository
        extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findTop100ByStatusOrderByCreatedAtAsc(
            String status
    );
}
