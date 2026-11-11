package com.sahab.demo.modules.notifications.service;

import com.sahab.demo.entity.Notification;
import com.sahab.demo.entity.User;
import com.sahab.demo.modules.notifications.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    private final NotificationProducer notificationProducer;


    // =========================================================
    // CREATE NOTIFICATION
    // =========================================================

    public Notification createNotification(
            User user,
            String message
    ) {

        if (user == null) {
            throw new IllegalArgumentException(
                    "Notification user cannot be null"
            );
        }

        if (
                message == null ||
                        message.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Notification message cannot be empty"
            );
        }

        /*
         * NotificationProducer is responsible for
         * creating/sending the notification.
         */
        notificationProducer.sendNotification(
                user,
                message
        );

        /*
         * The producer handles the actual persistence.
         * Therefore there is no Notification entity to return here.
         */
        return null;
    }


    // =========================================================
    // GET USER NOTIFICATIONS
    // =========================================================

    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(
            User user
    ) {

        validateUser(user);

        return notificationRepository
                .findByUserOrderByCreatedAtDesc(user);
    }


    // =========================================================
    // GET UNREAD COUNT
    // =========================================================

    @Transactional(readOnly = true)
    public long getUnreadCount(
            User user
    ) {

        validateUser(user);

        return notificationRepository
                .countByUserAndReadFalse(user);
    }


    // =========================================================
    // MARK ALL AS READ
    // =========================================================

    @Transactional
    public int markAllAsRead(
            User user
    ) {

        validateUser(user);

        /*
         * Uses one database UPDATE query instead of:
         *
         * find all notifications
         * -> loop
         * -> set read = true
         * -> saveAll
         *
         * This is faster and safer for production.
         */
        return notificationRepository
                .markAllAsRead(user);
    }


    // =========================================================
    // MARK ONE NOTIFICATION AS READ
    // =========================================================

    @Transactional
    public boolean markAsRead(
            Long notificationId,
            User user
    ) {

        if (notificationId == null) {
            throw new IllegalArgumentException(
                    "Notification ID cannot be null"
            );
        }

        validateUser(user);

        /*
         * The repository query checks BOTH:
         *
         * notification ID
         * current user
         *
         * This prevents a user from marking
         * another user's notification as read.
         */
        int updated =
                notificationRepository.markAsRead(
                        notificationId,
                        user
                );

        return updated > 0;
    }


    // =========================================================
    // VALIDATE USER
    // =========================================================

    private void validateUser(
            User user
    ) {

        if (user == null) {
            throw new IllegalArgumentException(
                    "Notification user cannot be null"
            );
        }
    }
}