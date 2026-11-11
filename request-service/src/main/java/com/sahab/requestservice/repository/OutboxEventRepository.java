package com.sahab.requestservice.repository;

import com.sahab.requestservice.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {
    List<OutboxEvent> findTop100ByStatusOrderByCreatedAtAsc(String status);
}
