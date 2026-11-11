package com.sahab.demo.modules.departments.service;

import com.sahab.demo.modules.departments.dto.DepartmentDetailsDTO;
import com.sahab.demo.entity.Department;
import com.sahab.demo.entity.SlaPolicy;
import com.sahab.demo.entity.User;
import com.sahab.demo.enums.RequestStatus;
import com.sahab.demo.enums.Role;
import com.sahab.demo.exception.BadRequestException;
import com.sahab.demo.exception.ResourceNotFoundException;
import com.sahab.demo.modules.departments.repository.DepartmentRepository;
import com.sahab.demo.modules.requests.repository.RequestRepository;
import com.sahab.demo.modules.departments.repository.SlaPolicyRepository;
import com.sahab.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final SlaPolicyRepository slaPolicyRepository;

    // =========================================
    // GET ALL DEPARTMENTS
    // =========================================

    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    // =========================================
    // GET DEPARTMENT BY ID
    // =========================================

    public Department getDepartmentById(Long id) {

        return departmentRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Department not found"
                        )
                );
    }

    // =========================================
    // CREATE DEPARTMENT
    // =========================================

    @Transactional
    public Department createDepartment(String name) {

        String normalizedName = normalizeDepartmentName(name);

        if (departmentRepository.findByName(normalizedName).isPresent()) {
            throw new BadRequestException(
                    "Department already exists"
            );
        }

        Department department = new Department();

        department.setName(normalizedName);

        return departmentRepository.save(department);
    }

    // =========================================
    // UPDATE DEPARTMENT
    // =========================================

    @Transactional
    public Department updateDepartment(
            Long id,
            String newName
    ) {

        String normalizedName = normalizeDepartmentName(newName);

        Department department = getDepartmentById(id);

        if (!department.getName().equalsIgnoreCase(normalizedName)) {

            if (departmentRepository.findByName(normalizedName).isPresent()) {

                throw new BadRequestException(
                        "Department already exists"
                );
            }
        }

        department.setName(normalizedName);

        return departmentRepository.save(department);
    }

    // =========================================
    // DELETE DEPARTMENT
    // =========================================

    @Transactional
    public void deleteDepartment(Long id) {

        Department department = getDepartmentById(id);

        List<User> users =
                userRepository.findByDepartmentId(id);

        if (!users.isEmpty()) {

            throw new BadRequestException(
                    "Cannot delete department because employees are assigned to it"
            );
        }

        departmentRepository.delete(department);
    }

    // =========================================
    // AVAILABLE DEPARTMENT NAMES
    // =========================================
    //
    // لم نعد نستخدم DepartmentType enum.
    //
    // هذه الدالة ترجع أسماء الأقسام الموجودة حاليًا.
    // يمكن استخدامها لاحقًا في الـ Admin UI إذا احتجناها.
    // =========================================

    public List<String> getDepartmentNames() {

        return departmentRepository.findAll()
                .stream()
                .map(Department::getName)
                .sorted()
                .collect(Collectors.toList());
    }

    // =========================================
    // NORMALIZE DEPARTMENT NAME
    // =========================================

    private String normalizeDepartmentName(String name) {

        if (name == null || name.trim().isEmpty()) {

            throw new BadRequestException(
                    "Department name is required"
            );
        }

        String normalized =
                name.trim()
                        .replaceAll("\\s+", " ");

        if (normalized.length() > 100) {

            throw new BadRequestException(
                    "Department name cannot exceed 100 characters"
            );
        }

        return normalized
                .toUpperCase(Locale.ROOT);
    }

    // =========================================
    // DEPARTMENT DETAILS + ANALYTICS
    // =========================================

    @Transactional(readOnly = true)
    public DepartmentDetailsDTO getDepartmentDetails(
            Long departmentId
    ) {

        Department department =
                getDepartmentById(departmentId);

        List<User> departmentUsers =
                userRepository.findByDepartmentId(
                        departmentId
                );

        // =========================================
        // MANAGERS
        // =========================================

        List<DepartmentDetailsDTO.ManagerDTO> managers =
                departmentUsers.stream()
                        .filter(
                                user ->
                                        user.getRole()
                                                == Role.MANAGER
                        )
                        .map(
                                user ->
                                        new DepartmentDetailsDTO.ManagerDTO(
                                                user.getId(),
                                                user.getName(),
                                                user.getEmail()
                                        )
                        )
                        .collect(
                                Collectors.toList()
                        );

        // =========================================
        // EMPLOYEES
        // =========================================

        List<DepartmentDetailsDTO.EmployeeDTO> employees =
                departmentUsers.stream()
                        .filter(
                                user ->
                                        user.getRole()
                                                != Role.MANAGER
                        )
                        .map(
                                user ->
                                        new DepartmentDetailsDTO.EmployeeDTO(
                                                user.getId(),
                                                user.getName(),
                                                user.getEmail(),
                                                user.getRole() != null
                                                        ? user.getRole().name()
                                                        : null
                                        )
                        )
                        .collect(
                                Collectors.toList()
                        );

        long employeeCount =
                employees.size();

        // =========================================
        // REQUEST COUNTS
        // =========================================

        long requestCount =
                requestRepository.countByDepartmentId(
                        departmentId
                );

        long pending =
                requestRepository.countByDepartmentIdAndStatus(
                        departmentId,
                        RequestStatus.PENDING
                );

        long approved =
                requestRepository.countByDepartmentIdAndStatus(
                        departmentId,
                        RequestStatus.APPROVED
                );

        long rejected =
                requestRepository.countByDepartmentIdAndStatus(
                        departmentId,
                        RequestStatus.REJECTED
                );

        // =========================================
        // SLA CURRENT STATE
        // =========================================

        LocalDateTime now =
                LocalDateTime.now();

        long slaWarning =
                requestRepository.countWarningRequestsByDepartment(
                        departmentId,
                        RequestStatus.PENDING,
                        now
                );

        long slaBreached =
                requestRepository.countBreachedRequestsByDepartment(
                        departmentId,
                        RequestStatus.PENDING,
                        now
                );

        // =========================================
        // SLA COMPLIANCE
        // =========================================

        long completedRequestsWithSla =
                requestRepository
                        .countCompletedRequestsWithSlaByDepartment(
                                departmentId
                        );

        long completedRequestsWithinSla =
                requestRepository
                        .countCompletedRequestsWithinSlaByDepartment(
                                departmentId
                        );

        double slaCompliancePercentage;

        if (completedRequestsWithSla == 0) {

            slaCompliancePercentage = 0.0;

        } else {

            slaCompliancePercentage =
                    Math.round(
                            (
                                    (
                                            (double)
                                                    completedRequestsWithinSla
                                                    / completedRequestsWithSla
                                    )
                                            * 10000.0
                            )
                    )
                            / 100.0;
        }

        // =========================================
        // ANALYTICS DTO
        // =========================================

        DepartmentDetailsDTO.AnalyticsDTO analytics =
                new DepartmentDetailsDTO.AnalyticsDTO(
                        pending,
                        approved,
                        rejected,
                        slaWarning,
                        slaBreached,
                        slaCompliancePercentage
                );

        // =========================================
        // SLA POLICY
        // =========================================

        SlaPolicy slaPolicy =
                slaPolicyRepository
                        .findByDepartmentId(departmentId)
                        .orElse(null);

        DepartmentDetailsDTO.SlaDTO slaDTO =
                null;

        if (slaPolicy != null) {

            slaDTO =
                    new DepartmentDetailsDTO.SlaDTO(
                            slaPolicy.getDurationHours(),
                            slaPolicy.getWarningHours(),
                            slaPolicy.isActive()
                    );
        }

        // =========================================
        // FINAL RESPONSE
        // =========================================

        return new DepartmentDetailsDTO(
                department.getId(),
                department.getName(),
                managers,
                employees,
                employeeCount,
                requestCount,
                slaDTO,
                analytics
        );
    }
}