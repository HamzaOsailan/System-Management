package com.sahab.demo.repository;

import com.sahab.demo.entity.Notification;
import com.sahab.demo.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

        List<Notification> findByUser(User user);

        List<Notification> findByUserOrderByCreatedAtDesc(User user);

        long countByUserAndReadFalse(User user);
}