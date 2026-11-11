package com.sahab.demo.modules.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EmployeeProfileUpdateDTO {

    // =========================
    // Personal Information
    // =========================

    @NotBlank(message = "Name is required")
    @Size(max = 255)
    private String name;

    @Size(max = 50)
    private String phone;

    @Size(max = 255)
    private String address;

    private LocalDate dateOfBirth;

    @Size(max = 100)
    private String nationality;

    // =========================
    // Employment Information
    // =========================

    @Size(max = 255)
    private String jobTitle;

    @Size(max = 255)
    private String department;

    @Size(max = 255)
    private String employmentType;

    @Size(max = 255)
    private String workLocation;

    @Size(max = 255)
    private String workingHours;

    // =========================
    // Banking Information
    // =========================

    @Size(max = 255)
    private String bankName;

    @Size(max = 255)
    private String bankBranch;

    @Size(max = 255)
    private String accountNumber;

    @Size(max = 255)
    private String accountHolderName;

    @Size(max = 255)
    private String accountType;

    @Size(max = 255)
    private String iban;
}