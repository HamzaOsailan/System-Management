package com.sahab.demo.modules.hr.repository;

import com.sahab.demo.entity.Attendance;
import com.sahab.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository
        extends JpaRepository<Attendance, Long> {

    // ==========================================
    // Employee - Today's Attendance
    // ==========================================

    Optional<Attendance> findByUserAndDate(
            User user,
            LocalDate date
    );


    // ==========================================
    // HR - Today's Attendance
    // Load User together with Attendance
    // ==========================================

    @Query("""
            SELECT a
            FROM Attendance a
            JOIN FETCH a.user u
            WHERE a.date = :date
            ORDER BY a.checkIn ASC
            """)
    List<Attendance> findByDateOrderByCheckInAsc(
            @Param("date") LocalDate date
    );


    // ==========================================
    // HR - Attendance Between Dates
    // Load User together with Attendance
    // ==========================================

    @Query("""
            SELECT a
            FROM Attendance a
            JOIN FETCH a.user u
            WHERE a.date BETWEEN :startDate AND :endDate
            ORDER BY a.date DESC, a.checkIn ASC
            """)
    List<Attendance> findByDateBetweenOrderByDateDescCheckInAsc(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );


    // ==========================================
    // HR - Specific Employee Between Dates
    // Load User together with Attendance
    // ==========================================

    @Query("""
            SELECT a
            FROM Attendance a
            JOIN FETCH a.user u
            WHERE u.id = :userId
              AND a.date BETWEEN :startDate AND :endDate
            ORDER BY a.date DESC
            """)
    List<Attendance> findByUser_IdAndDateBetweenOrderByDateDesc(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}

