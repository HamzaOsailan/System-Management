package com.sahab.requestservice.repository;

import com.sahab.requestservice.entity.ApprovalStep;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApprovalStepRepository extends JpaRepository<ApprovalStep, Long> {
    List<ApprovalStep> findByRequestIdOrderByStepOrderAsc(Long requestId);
}
