
package com.sahab.demo.modules.hr.controller;

import com.sahab.demo.modules.hr.dto.AttendanceAdminDTO;
import com.sahab.demo.modules.hr.dto.AttendanceDTO;
import com.sahab.demo.modules.hr.dto.AttendanceEmployeeDTO;
import com.sahab.demo.entity.User;
import com.sahab.demo.modules.hr.service.AttendanceAdminService;
import com.sahab.demo.modules.hr.service.AttendanceService;
import com.sahab.demo.modules.identity.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AttendanceController {

    private final AttendanceService attendanceService;

    private final AttendanceAdminService attendanceAdminService;

    private final UserService userService;


    // =========================================================
    // TODAY ATTENDANCE
    // CURRENT USER
    // USER + ADMIN + MANAGER + HR
    // =========================================================

    @GetMapping("/today")
    @PreAuthorize(
            "hasAnyRole('USER', 'ADMIN', 'MANAGER', 'HR')"
    )
    public ResponseEntity<AttendanceDTO> today() {

        User currentUser =
                getCurrentUser();

        return ResponseEntity.ok(
                attendanceService.today(
                        currentUser
                )
        );
    }


    // =========================================================
    // CHECK IN / CHECK OUT
    //
    // EXISTING ATTENDANCE LOGIC
    // =========================================================

    @PostMapping("/check")
    @PreAuthorize(
            "hasAnyRole('USER', 'ADMIN', 'MANAGER', 'HR')"
    )
    public ResponseEntity<AttendanceDTO> checkInOrOut() {

        User currentUser =
                getCurrentUser();

        return ResponseEntity.ok(
                attendanceService.checkInOrOut(
                        currentUser
                )
        );
    }


    // =========================================================
    // HR - TODAY
    // ADMIN + MANAGER + HR
    //
    // GET:
    // /attendance/hr/today
    // =========================================================

    @GetMapping("/hr/today")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'MANAGER', 'HR')"
    )
    public ResponseEntity<List<AttendanceAdminDTO>>
    getTodayAttendance() {

        return ResponseEntity.ok(
                attendanceAdminService
                        .getTodayAttendance()
        );
    }


    // =========================================================
    // HR - EMPLOYEES
    // ADMIN + MANAGER + HR
    //
    // GET:
    // /attendance/hr/employees
    // =========================================================

    @GetMapping("/hr/employees")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'MANAGER', 'HR')"
    )
    public ResponseEntity<List<AttendanceEmployeeDTO>>
    getEmployees() {

        return ResponseEntity.ok(
                attendanceAdminService
                        .getEmployees()
        );
    }


    // =========================================================
    // HR - ALL ATTENDANCE
    // ADMIN + MANAGER + HR
    //
    // GET:
    // /attendance/hr
    //
    // Example:
    // /attendance/hr?startDate=2026-09-01&endDate=2026-09-29
    // =========================================================

    @GetMapping("/hr")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'MANAGER', 'HR')"
    )
    public ResponseEntity<List<AttendanceAdminDTO>>
    getAttendance(

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate startDate,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate endDate
    ) {

        return ResponseEntity.ok(
                attendanceAdminService
                        .getAllAttendance(
                                startDate,
                                endDate
                        )
        );
    }


    // =========================================================
    // HR - EMPLOYEE ATTENDANCE
    // ADMIN + MANAGER + HR
    //
    // GET:
    // /attendance/hr/employee/{userId}
    // =========================================================

    @GetMapping("/hr/employee/{userId}")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'MANAGER', 'HR')"
    )
    public ResponseEntity<List<AttendanceAdminDTO>>
    getEmployeeAttendance(

            @PathVariable Long userId,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate startDate,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate endDate
    ) {

        return ResponseEntity.ok(
                attendanceAdminService
                        .getEmployeeAttendance(
                                userId,
                                startDate,
                                endDate
                        )
        );
    }


    // =========================================================
    // CURRENT USER
    // =========================================================

    private User getCurrentUser() {

        String email =
                org.springframework.security.core.context
                        .SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        return userService.getUserByEmail(email);
    }
}
