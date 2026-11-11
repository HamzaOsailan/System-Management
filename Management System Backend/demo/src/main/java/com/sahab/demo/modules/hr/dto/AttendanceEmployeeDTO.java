package com.sahab.demo.modules.hr.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AttendanceEmployeeDTO {

    private Long id;

    private String name;

    private String email;
}