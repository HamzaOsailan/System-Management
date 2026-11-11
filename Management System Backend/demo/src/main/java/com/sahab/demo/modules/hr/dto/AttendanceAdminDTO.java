package com.sahab.demo.modules.hr.dto;

import com.sahab.demo.enums.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class AttendanceAdminDTO {

    private Long id;

    private Long userId;

    private String employeeName;

    private String employeeEmail;

    private LocalDate date;

    private LocalDateTime checkIn;

    private LocalDateTime checkOut;

    private AttendanceStatus status;
}