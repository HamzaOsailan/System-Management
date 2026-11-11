package com.example.notification_service.rabbit;

import com.example.notification_service.service.NotificationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RabbitNotificationListener {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public RabbitNotificationListener(
            NotificationService notificationService,
            ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = RabbitNotificationConfig.QUEUE)
    public void consume(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getBody());

        JsonNode userNode = payload.get("userId");
        JsonNode messageNode = payload.get("message");
        JsonNode eventNode = payload.get("eventId");

        if (userNode == null || !userNode.canConvertToLong()
                || messageNode == null || messageNode.asText().isBlank()) {
            throw new IllegalArgumentException(
                    "Invalid notification message received from RabbitMQ"
            );
        }

        String eventId = eventNode == null || eventNode.asText().isBlank()
                ? UUID.randomUUID().toString()
                : eventNode.asText();

        notificationService.createForUser(
                eventId,
                userNode.asLong(),
                messageNode.asText()
        );
    }
}
