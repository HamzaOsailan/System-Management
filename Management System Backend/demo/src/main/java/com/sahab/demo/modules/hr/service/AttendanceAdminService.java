
package com.sahab.demo.modules.hr.service;

import com.sahab.demo.modules.hr.dto.AttendanceAdminDTO;
import com.sahab.demo.modules.hr.dto.AttendanceEmployeeDTO;
import com.sahab.demo.entity.Attendance;
import com.sahab.demo.modules.hr.repository.AttendanceRepository;
import com.sahab.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AttendanceAdminService {

    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;

    public AttendanceAdminService(
            AttendanceRepository attendanceRepository,
            UserRepository userRepository
    ) {
        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
    }


    // =========================================================
    // GET ALL ATTENDANCE
    // =========================================================

    public List<AttendanceAdminDTO> getAllAttendance(
            LocalDate startDate,
            LocalDate endDate
    ) {

        List<Attendance> records =
                attendanceRepository
                        .findByDateBetweenOrderByDateDescCheckInAsc(
                                startDate,
                                endDate
                        );

        return records.stream()
                .map(this::toDTO)
                .toList();
    }


    // =========================================================
    // GET TODAY ATTENDANCE
    // =========================================================

    public List<AttendanceAdminDTO> getTodayAttendance() {

        LocalDate today =
                LocalDate.now();

        return attendanceRepository
                .findByDateOrderByCheckInAsc(today)
                .stream()
                .map(this::toDTO)
                .toList();
    }


    // =========================================================
    // GET EMPLOYEE ATTENDANCE
    // =========================================================

    public List<AttendanceAdminDTO> getEmployeeAttendance(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    ) {

        return attendanceRepository
                .findByUser_IdAndDateBetweenOrderByDateDesc(
                        userId,
                        startDate,
                        endDate
                )
                .stream()
                .map(this::toDTO)
                .toList();
    }


    // =========================================================
    // GET EMPLOYEES
    // Used by HR Attendance page
    // ADMIN + MANAGER
    // =========================================================

    public List<AttendanceEmployeeDTO> getEmployees() {

        return userRepository
                .findAll()
                .stream()
                .map(user ->
                        new AttendanceEmployeeDTO(
                                user.getId(),
                                user.getName(),
                                user.getEmail()
                        )
                )
                .toList();
    }


    // =========================================================
    // CONVERT ATTENDANCE TO DTO
    // =========================================================

    private AttendanceAdminDTO toDTO(
            Attendance attendance
    ) {

        return new AttendanceAdminDTO(
                attendance.getId(),
                attendance.getUser().getId(),
                attendance.getUser().getName(),
                attendance.getUser().getEmail(),
                attendance.getDate(),
                attendance.getCheckIn(),
                attendance.getCheckOut(),
                attendance.getStatus()
        );
    }
}