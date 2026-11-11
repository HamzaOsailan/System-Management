package com.sahab.demo.modules.identity.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateEmployeeProfileDTO {

    // =========================================================
    // BASIC
    // =========================================================

    private String name;


    // =========================================================
    // PERSONAL
    // =========================================================

    private String phone;

    private String dateOfBirth;

    private String nationality;

    private String address;


    // =========================================================
    // EMPLOYEE
    // =========================================================

    private String employeeId;

    private String jobTitle;

    private String department;

    private String managerName;

    private String joiningDate;

    private String employmentType;

    private String employmentStatus;

    private String workLocation;

    private String workingHours;


    // =========================================================
    // BANK
    // =========================================================

    private String bankName;

    private String accountHolderName;

    private String iban;

    private String accountNumber;

    private String accountType;

    private String bankBranch;
}