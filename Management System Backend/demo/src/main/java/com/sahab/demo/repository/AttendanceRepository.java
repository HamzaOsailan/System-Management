package com.sahab.demo.repository;

import com.sahab.demo.entity.Attendance;
import com.sahab.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface AttendanceRepository
        extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByUserAndDate(
            User user,
            LocalDate date
    );
}