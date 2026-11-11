
package com.sahab.demo.modules.infrastructure.outbox.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sahab.demo.config.DomainEventsRabbitMQConfig;
import com.sahab.demo.modules.infrastructure.outbox.entity.OutboxEvent;
import com.sahab.demo.modules.infrastructure.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OutboxPublisher {

    private static final int MAX_ATTEMPTS = 10;
    private static final long CONFIRM_TIMEOUT_SECONDS = 10;

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Periodically publishes pending outbox events.
     */
    @Scheduled(fixedDelayString = "${sahab.outbox.poll-delay-ms:5000}")
    public void publishPendingEvents() {

        List<OutboxEvent> events =
                outboxEventRepository
                        .findTop100ByStatusOrderByCreatedAtAsc("PENDING");

        for (OutboxEvent event : events) {
            publishEvent(event);
        }
    }

    /**
     * Publishes an event and waits for RabbitMQ confirmation.
     */
    private void publishEvent(OutboxEvent event) {

        try {
            ObjectNode payload = createMessagePayload(event);

            CorrelationData correlationData =
                    new CorrelationData("outbox-" + event.getId());

            rabbitTemplate.convertAndSend(
                    DomainEventsRabbitMQConfig.EXCHANGE,
                    DomainEventsRabbitMQConfig.ROUTING_KEY,
                    payload,
                    message -> {
                        message.getMessageProperties().setMessageId(
                                String.valueOf(event.getId())
                        );

                        message.getMessageProperties().setHeader(
                                "eventId", event.getId()
                        );

                        message.getMessageProperties().setHeader(
                                "eventType", event.getEventType()
                        );

                        message.getMessageProperties().setDeliveryMode(
                                MessageDeliveryMode.PERSISTENT
                        );

                        return message;
                    },
                    correlationData
            );

            CorrelationData.Confirm confirm =
                    correlationData.getFuture().get(
                            CONFIRM_TIMEOUT_SECONDS,
                            TimeUnit.SECONDS
                    );

            if (!confirm.isAck()) {
                throw new IllegalStateException(
                        "RabbitMQ rejected event: " + confirm.getReason()
                );
            }

            var returnedMessage = correlationData.getReturned();

            if (returnedMessage != null) {
                throw new IllegalStateException(
                        "RabbitMQ returned the message because it could not be routed."
                );
            }

            markPublished(event.getId());

            System.out.println(
                    "Outbox event published. ID: " + event.getId()
                            + ", type: " + event.getEventType()
            );

        } catch (Exception exception) {

            recordFailure(event.getId());

            System.err.println(
                    "Outbox publishing failed. ID: " + event.getId()
                            + ", error: " + exception.getMessage()
            );
        }
    }

    /**
     * Adds event metadata to the JSON payload.
     */
    private ObjectNode createMessagePayload(OutboxEvent event)
            throws Exception {

        JsonNode payload = objectMapper.readTree(event.getPayload());

        if (!(payload instanceof ObjectNode objectNode)) {
            throw new IllegalArgumentException(
                    "Outbox payload must be a JSON object."
            );
        }

        objectNode.put("eventId", event.getId());
        objectNode.put("eventType", event.getEventType());
        objectNode.put("aggregateType", event.getAggregateType());
        objectNode.put("aggregateId", event.getAggregateId());

        return objectNode;
    }

    /**
     * Marks the event as published after confirmation.
     */
    @Transactional
    public void markPublished(Long eventId) {

        OutboxEvent event = outboxEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalStateException(
                        "Outbox event not found: " + eventId
                ));

        event.setStatus("PUBLISHED");
        event.setPublishedAt(LocalDateTime.now());

        outboxEventRepository.save(event);
    }

    /**
     * Records a failed attempt.
     */
    @Transactional
    public void recordFailure(Long eventId) {

        outboxEventRepository.findById(eventId).ifPresent(event -> {

            int attempts = event.getAttempts() + 1;
            event.setAttempts(attempts);

            if (attempts >= MAX_ATTEMPTS) {
                event.setStatus("FAILED");
            } else {
                event.setStatus("PENDING");
            }

            outboxEventRepository.save(event);
        });
    }
}
