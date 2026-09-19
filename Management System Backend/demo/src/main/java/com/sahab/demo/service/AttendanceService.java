package com.sahab.demo.service;

import com.sahab.demo.dto.AttendanceDTO;
import com.sahab.demo.entity.Attendance;
import com.sahab.demo.entity.User;
import com.sahab.demo.enums.AttendanceStatus;
import com.sahab.demo.repository.AttendanceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class AttendanceService {

    private final AttendanceRepository repository;

    public AttendanceService(
            AttendanceRepository repository
    ) {
        this.repository = repository;
    }

    public AttendanceDTO today(
            User user
    ) {

        LocalDate today =
                LocalDate.now();

        return repository
                .findByUserAndDate(
                        user,
                        today
                )
                .map(this::toDTO)
                .orElse(
                        new AttendanceDTO(
                                null,
                                today,
                                null,
                                null,
                                AttendanceStatus.NOT_CHECKED_IN
                        )
                );
    }

    public AttendanceDTO checkInOrOut(
            User user
    ) {

        LocalDate today =
                LocalDate.now();

        Attendance attendance =
                repository
                        .findByUserAndDate(
                                user,
                                today
                        )
                        .orElseGet(() -> {

                            Attendance a =
                                    new Attendance();

                            a.setUser(user);
                            a.setDate(today);
                            a.setStatus(
                                    AttendanceStatus.NOT_CHECKED_IN
                            );

                            return a;
                        });

        LocalDateTime now =
                LocalDateTime.now();

        if (
                attendance.getStatus()
                        == AttendanceStatus.NOT_CHECKED_IN
        ) {

            attendance.setCheckIn(now);

            attendance.setStatus(
                    AttendanceStatus.CHECKED_IN
            );

        } else if (
                attendance.getStatus()
                        == AttendanceStatus.CHECKED_IN
        ) {

            attendance.setCheckOut(now);

            attendance.setStatus(
                    AttendanceStatus.CHECKED_OUT
            );

        } else {

            throw new RuntimeException(
                    "Attendance already completed for today"
            );
        }

        return toDTO(
                repository.save(attendance)
        );
    }

    private AttendanceDTO toDTO(
            Attendance attendance
    ) {

        return new AttendanceDTO(
                attendance.getId(),
                attendance.getDate(),
                attendance.getCheckIn(),
                attendance.getCheckOut(),
                attendance.getStatus()
        );
    }
}