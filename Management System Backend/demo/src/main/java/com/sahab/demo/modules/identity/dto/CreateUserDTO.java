
package com.sahab.demo.modules.identity.dto;

import com.sahab.demo.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateUserDTO {

    // =========================================================
    // BASIC ACCOUNT INFORMATION
    // =========================================================

    @NotBlank(message = "Name is required")
    @Size(max = 255)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    @Size(max = 255)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must contain at least 6 characters")
    private String password;

    @NotNull(message = "Role is required")
    private Role role;


    // =========================================================
    // EMPLOYMENT INFORMATION
    // =========================================================

    @Size(max = 255)
    private String employeeId;

    @Size(max = 255)
    private String jobTitle;

    @Size(max = 255)
    private String department;

    private Long managerId;

    private LocalDate joiningDate;

    @Size(max = 255)
    private String employmentType;

    @Size(max = 255)
    private String employmentStatus;

    @Size(max = 255)
    private String workLocation;

    @Size(max = 255)
    private String workingHours;
}

