package com.sahab.demo.modules.departments.controller;

import com.sahab.demo.modules.departments.dto.DepartmentDetailsDTO;
import com.sahab.demo.entity.Department;
import com.sahab.demo.modules.departments.service.DepartmentService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    // =========================================
    // GET ALL DEPARTMENTS
    // =========================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Department>> getAllDepartments() {

        return ResponseEntity.ok(
                departmentService.getAllDepartments()
        );
    }

    // =========================================
    // GET DEPARTMENT BY ID
    // =========================================

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Department> getDepartmentById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                departmentService.getDepartmentById(id)
        );
    }

    // =========================================
    // GET DEPARTMENT DETAILS
    // =========================================

    @GetMapping("/{id}/details")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentDetailsDTO> getDepartmentDetails(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                departmentService.getDepartmentDetails(id)
        );
    }

    // =========================================
    // CREATE DEPARTMENT
    // =========================================
    //
    // Example:
    //
    // POST /departments?name=LEGAL
    //
    // =========================================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Department> createDepartment(
            @RequestParam String name
    ) {

        Department department =
                departmentService.createDepartment(name);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(department);
    }

    // =========================================
    // UPDATE DEPARTMENT
    // =========================================
    //
    // Example:
    //
    // PUT /departments/7?name=MARKETING
    //
    // =========================================

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Department> updateDepartment(
            @PathVariable Long id,
            @RequestParam String name
    ) {

        Department department =
                departmentService.updateDepartment(
                        id,
                        name
                );

        return ResponseEntity.ok(department);
    }

    // =========================================
    // DELETE DEPARTMENT
    // =========================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDepartment(
            @PathVariable Long id
    ) {

        departmentService.deleteDepartment(id);

        return ResponseEntity.noContent().build();
    }

    // =========================================
    // GET DEPARTMENT NAMES
    // =========================================
    //
    // Returns currently existing department names.
    //
    // Example:
    //
    // GET /departments/names
    //
    // =========================================

    @GetMapping("/names")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> getDepartmentNames() {

        return ResponseEntity.ok(
                departmentService.getDepartmentNames()
        );
    }
}