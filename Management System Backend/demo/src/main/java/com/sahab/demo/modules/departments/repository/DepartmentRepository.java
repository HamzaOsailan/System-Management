
package com.sahab.demo.modules.departments.repository;

import com.sahab.demo.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepartmentRepository
        extends JpaRepository<Department, Long> {

    Optional<Department> findByName(String name);

    Optional<Department> findByNameIgnoreCase(String name);
}
