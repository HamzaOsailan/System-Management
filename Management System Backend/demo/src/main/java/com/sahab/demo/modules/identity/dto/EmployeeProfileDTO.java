package com.sahab.demo.modules.identity.dto;
import com.sahab.demo.modules.requests.dto.EmployeeRequestDTO;


import com.sahab.demo.enums.Role;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class EmployeeProfileDTO {

    // =========================================================
    // BASIC INFORMATION
    // =========================================================

    private Long id;

    private String name;

    private String email;

    private String role;


    // =========================================================
    // PERSONAL INFORMATION
    // =========================================================

    private String phone;

    private String address;

    private LocalDate dateOfBirth;

    private String nationality;


    // =========================================================
    // EMPLOYEE INFORMATION
    // =========================================================

    private String employeeId;

    private String jobTitle;

    private String department;

    private String managerName;

    private Long managerId;

    private LocalDate joiningDate;

    private String employmentType;

    private String employmentStatus;

    private String workLocation;

    private String workingHours;


    // =========================================================
    // BANK INFORMATION
    // =========================================================

    private String bankName;

    private String bankBranch;

    private String accountHolderName;

    private String accountType;

    private String iban;

    private String accountNumber;


    // =========================================================
    // EMPLOYEE REQUESTS
    // =========================================================

    private List<EmployeeRequestDTO> requests;
}