package com.sahab.demo.service;

import com.sahab.demo.entity.Notification;
import com.sahab.demo.entity.User;
import com.sahab.demo.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void createNotification(User user, String message){
        Notification notification=new Notification();
        notification.setUser(user);
        notification.setMessage(message);

        notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(User user){
        return notificationRepository.findByUser(user);
    }
}
