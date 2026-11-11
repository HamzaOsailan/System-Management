package com.sahab.demo.modules.departments.repository;

import com.sahab.demo.entity.Department;
import com.sahab.demo.entity.SlaPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SlaPolicyRepository
        extends JpaRepository<SlaPolicy, Long> {

    Optional<SlaPolicy> findByDepartment(
            Department department
    );

    Optional<SlaPolicy> findByDepartmentId(
            Long departmentId
    );
}