
package com.sahab.demo.modules.identity.dto;

import com.sahab.demo.enums.Role;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UserResponseDTO {

    private Long id;

    private String name;

    private String email;

    private Role role;

    private String phone;

    private LocalDate dateOfBirth;

    private String nationality;

    private String address;

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

    private String bankName;

    private String accountHolderName;

    private String accountType;

    private String bankBranch;

    public UserResponseDTO() {
    }
}

