package com.sahab.demo.modules.hr.dto;

import com.sahab.demo.enums.AttendanceStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AttendanceDTO(
        Long id,
        LocalDate date,
        LocalDateTime checkIn,
        LocalDateTime checkOut,
        AttendanceStatus status
) {
}