package com.sahab.demo.service;

import com.sahab.demo.entity.Notification;
import com.sahab.demo.entity.User;
import com.sahab.demo.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Notification createNotification(
            User user,
            String message
    ) {
        Notification notification =
                new Notification();

        notification.setUser(user);
        notification.setMessage(message);

        return notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(User user) {

        return notificationRepository
                .findByUserOrderByCreatedAtDesc(user);
    }

    public long getUnreadCount(User user) {

        return notificationRepository
                .countByUserAndReadFalse(user);
    }

    public void markAllAsRead(User user) {

        List<Notification> notifications =
                notificationRepository.findByUser(user);

        for (Notification notification : notifications) {
            notification.setRead(true);
        }

        notificationRepository.saveAll(notifications);
    }
}