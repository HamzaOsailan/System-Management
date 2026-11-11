package com.sahab.demo.modules.notifications.service;

import com.sahab.demo.config.RabbitMQConfig;
import com.sahab.demo.modules.notifications.dto.NotificationMessage;
import com.sahab.demo.entity.Notification;
import com.sahab.demo.entity.User;
import com.sahab.demo.modules.notifications.repository.NotificationRepository;
import com.sahab.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @RabbitListener(
            queues = RabbitMQConfig.NOTIFICATION_QUEUE
    )
    public void consumeNotification(
            NotificationMessage message
    ) {

        System.out.println(
                "========== RABBITMQ CONSUMER =========="
        );

        System.out.println(
                "User ID: "
                        + message.getUserId()
        );

        System.out.println(
                "Message: "
                        + message.getMessage()
        );

        User user =
                userRepository
                        .findById(message.getUserId())
                        .orElse(null);

        if (user == null) {

            System.out.println(
                    "Notification user not found: "
                            + message.getUserId()
            );

            return;
        }

        Notification notification =
                new Notification();

        notification.setUser(user);

        notification.setMessage(
                message.getMessage()
        );

        Notification saved =
                notificationRepository.save(
                        notification
                );

        System.out.println(
                "Notification saved to database."
        );

        System.out.println(
                "Notification ID: "
                        + saved.getId()
        );

        System.out.println(
                "========================================"
        );
    }
}