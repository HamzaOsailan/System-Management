package com.sahab.demo.modules.notifications.repository;

import com.sahab.demo.entity.Notification;
import com.sahab.demo.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

        // Get all notifications for a specific user
        List<Notification> findByUser(User user);

        // Get user notifications ordered from newest to oldest
        List<Notification> findByUserOrderByCreatedAtDesc(User user);

        // Count unread notifications for a specific user
        long countByUserAndReadFalse(User user);

        // Find a notification only if it belongs to the current user
        Optional<Notification> findByIdAndUser(
                Long id,
                User user
        );

        // Mark all unread notifications of the current user as read
        @Modifying
        @Query("""
        UPDATE Notification n
        SET n.read = true
        WHERE n.user = :user
          AND n.read = false
    """)
        int markAllAsRead(
                @Param("user") User user
        );

        // Mark one notification as read
        // Ownership is checked using the user parameter
        @Modifying
        @Query("""
        UPDATE Notification n
        SET n.read = true
        WHERE n.id = :notificationId
          AND n.user = :user
          AND n.read = false
    """)
        int markAsRead(
                @Param("notificationId") Long notificationId,
                @Param("user") User user
        );
}