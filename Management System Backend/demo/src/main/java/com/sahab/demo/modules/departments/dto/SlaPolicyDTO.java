package com.sahab.demo.modules.departments.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SlaPolicyDTO {

    private Long id;

    private Long departmentId;

    private String departmentName;

    private Integer durationHours;

    private Integer warningHours;

    private boolean active;
}