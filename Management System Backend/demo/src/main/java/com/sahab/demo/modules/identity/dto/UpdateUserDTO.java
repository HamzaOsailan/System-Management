
package com.sahab.demo.modules.identity.dto;

import com.sahab.demo.enums.Role;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateUserDTO {

    private Role role;

    private String employeeId;

    private String jobTitle;

    private String department;

    private Long managerId;

    private LocalDate joiningDate;

    private String employmentType;

    private String employmentStatus;

    private String workLocation;

    private String workingHours;
}

