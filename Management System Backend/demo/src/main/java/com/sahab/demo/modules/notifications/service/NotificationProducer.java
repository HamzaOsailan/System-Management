package com.sahab.demo.modules.notifications.service;

import com.sahab.demo.config.RabbitMQConfig;
import com.sahab.demo.modules.notifications.dto.NotificationMessage;
import com.sahab.demo.entity.User;

import lombok.RequiredArgsConstructor;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendNotification(
            User user,
            String message
    ) {

        if (user == null) {
            throw new IllegalArgumentException(
                    "Notification user cannot be null"
            );
        }

        if (user.getId() == null) {
            throw new IllegalArgumentException(
                    "Notification user ID cannot be null"
            );
        }

        NotificationMessage notificationMessage =
                new NotificationMessage(
                        user.getId(),
                        message
                );

        System.out.println(
                "========== RABBITMQ PRODUCER =========="
        );

        System.out.println(
                "User ID: "
                        + user.getId()
        );

        System.out.println(
                "Message: "
                        + message
        );

        System.out.println(
                "Exchange: "
                        + RabbitMQConfig.NOTIFICATION_EXCHANGE
        );

        System.out.println(
                "Routing Key: "
                        + RabbitMQConfig.NOTIFICATION_ROUTING_KEY
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                notificationMessage
        );

        System.out.println(
                "Notification sent to RabbitMQ."
        );

        System.out.println(
                "========================================"
        );
    }
}