package com.sahab.requestservice.repository;

import com.sahab.requestservice.entity.RequestRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RequestRepository extends JpaRepository<RequestRecord, Long> {
    List<RequestRecord> findByUserIdOrderByCreatedAtDesc(Long userId);
}
