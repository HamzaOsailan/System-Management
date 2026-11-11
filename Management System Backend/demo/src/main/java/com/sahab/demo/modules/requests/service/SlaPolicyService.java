package com.sahab.demo.modules.requests.service;

import com.sahab.demo.modules.requests.dto.SlaPolicyDTO;
import com.sahab.demo.entity.Department;
import com.sahab.demo.entity.SlaPolicy;
import com.sahab.demo.exception.ResourceNotFoundException;
import com.sahab.demo.repository.DepartmentRepository;
import com.sahab.demo.modules.requests.repository.SlaPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SlaPolicyService {

    private final SlaPolicyRepository slaPolicyRepository;

    private final DepartmentRepository departmentRepository;

    // =========================================================
    // GET ALL
    // =========================================================

    public List<SlaPolicyDTO> getAllPolicies() {

        return slaPolicyRepository
                .findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // =========================================================
    // GET BY ID
    // =========================================================

    public SlaPolicyDTO getPolicyById(Long id) {

        SlaPolicy policy =
                slaPolicyRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "SLA policy not found"
                                )
                        );

        return toDTO(policy);
    }

    // =========================================================
    // CREATE
    // =========================================================

    @Transactional
    public SlaPolicyDTO createPolicy(
            Long departmentId,
            Integer durationHours,
            Integer warningHours,
            Boolean active
    ) {

        if (departmentId == null) {

            throw new RuntimeException(
                    "Department is required"
            );
        }

        if (durationHours == null ||
                durationHours <= 0) {

            throw new RuntimeException(
                    "Duration hours must be greater than 0"
            );
        }

        if (warningHours == null ||
                warningHours < 0) {

            throw new RuntimeException(
                    "Warning hours cannot be negative"
            );
        }

        if (warningHours >= durationHours) {

            throw new RuntimeException(
                    "Warning hours must be less than SLA duration"
            );
        }

        Department department =
                departmentRepository
                        .findById(departmentId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Department not found"
                                )
                        );

        if (slaPolicyRepository
                .findByDepartmentId(departmentId)
                .isPresent()) {

            throw new RuntimeException(
                    "SLA policy already exists for this department"
            );
        }

        SlaPolicy policy =
                new SlaPolicy();

        policy.setDepartment(
                department
        );

        policy.setDurationHours(
                durationHours
        );

        policy.setWarningHours(
                warningHours
        );

        policy.setActive(
                active == null || active
        );

        SlaPolicy saved =
                slaPolicyRepository.save(policy);

        return toDTO(saved);
    }

    // =========================================================
    // UPDATE
    // =========================================================

    @Transactional
    public SlaPolicyDTO updatePolicy(
            Long id,
            Long departmentId,
            Integer durationHours,
            Integer warningHours,
            Boolean active
    ) {

        SlaPolicy policy =
                slaPolicyRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "SLA policy not found"
                                )
                        );

        if (departmentId == null) {

            throw new RuntimeException(
                    "Department is required"
            );
        }

        if (durationHours == null ||
                durationHours <= 0) {

            throw new RuntimeException(
                    "Duration hours must be greater than 0"
            );
        }

        if (warningHours == null ||
                warningHours < 0) {

            throw new RuntimeException(
                    "Warning hours cannot be negative"
            );
        }

        if (warningHours >= durationHours) {

            throw new RuntimeException(
                    "Warning hours must be less than SLA duration"
            );
        }

        Department department =
                departmentRepository
                        .findById(departmentId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Department not found"
                                )
                        );

        slaPolicyRepository
                .findByDepartmentId(departmentId)
                .ifPresent(existing -> {

                    if (!existing.getId()
                            .equals(policy.getId())) {

                        throw new RuntimeException(
                                "SLA policy already exists for this department"
                        );
                    }
                });

        policy.setDepartment(
                department
        );

        policy.setDurationHours(
                durationHours
        );

        policy.setWarningHours(
                warningHours
        );

        if (active != null) {

            policy.setActive(
                    active
            );
        }

        SlaPolicy saved =
                slaPolicyRepository.save(policy);

        return toDTO(saved);
    }

    // =========================================================
    // DELETE
    // =========================================================

    @Transactional
    public void deletePolicy(Long id) {

        SlaPolicy policy =
                slaPolicyRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "SLA policy not found"
                                )
                        );

        slaPolicyRepository.delete(
                policy
        );
    }

    // =========================================================
    // FIND POLICY FOR DEPARTMENT
    // =========================================================

    public SlaPolicy getPolicyForDepartment(
            Long departmentId
    ) {

        return slaPolicyRepository
                .findByDepartmentId(
                        departmentId
                )
                .filter(SlaPolicy::isActive)
                .orElse(null);
    }

    // =========================================================
    // CONVERT ENTITY -> DTO
    // =========================================================

    private SlaPolicyDTO toDTO(
            SlaPolicy policy
    ) {

        SlaPolicyDTO dto =
                new SlaPolicyDTO();

        dto.setId(
                policy.getId()
        );

        if (policy.getDepartment() != null) {

            dto.setDepartmentId(
                    policy.getDepartment().getId()
            );

            // Department.name is now String
            dto.setDepartmentName(
                    policy.getDepartment().getName()
            );
        }

        dto.setDurationHours(
                policy.getDurationHours()
        );

        dto.setWarningHours(
                policy.getWarningHours()
        );

        dto.setActive(
                policy.isActive()
        );

        return dto;
    }
}