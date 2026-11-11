package com.sahab.requestservice.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sahab.requestservice.entity.OutboxEvent;
import com.sahab.requestservice.repository.OutboxEventRepository;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class RequestOutboxPublisher {

    private final OutboxEventRepository repository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final String exchange;
    private final String routingKey;

    public RequestOutboxPublisher(
            OutboxEventRepository repository,
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper,
            @Value("${sahab.events.exchange}") String exchange,
            @Value("${sahab.events.routing-key}") String routingKey) {
        this.repository = repository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    @Scheduled(fixedDelayString = "${sahab.outbox.poll-delay-ms:5000}")
    public void publishPendingEvents() {
        List<OutboxEvent> events =
                repository.findTop100ByStatusOrderByCreatedAtAsc("PENDING");

        for (OutboxEvent event : events) {
            try {
                JsonNode payload = objectMapper.readTree(event.getPayload());
                var node = (com.fasterxml.jackson.databind.node.ObjectNode) payload;
                node.put("eventId", event.getId());
                node.put("eventType", event.getEventType());
                node.put("aggregateType", event.getAggregateType());
                node.put("aggregateId", event.getAggregateId());

                Message message = MessageBuilder
                        .withBody(objectMapper.writeValueAsBytes(node))
                        .setContentType("application/json")
                        .setMessageId(event.getId())
                        .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                        .setHeader("eventId", event.getId())
                        .setHeader("eventType", event.getEventType())
                        .build();

                CorrelationData correlation = new CorrelationData(event.getId());
                rabbitTemplate.send(exchange, routingKey, message, correlation);

                CorrelationData.Confirm confirm =
                        correlation.getFuture().get(10, TimeUnit.SECONDS);

                if (!confirm.isAck()) {
                    throw new IllegalStateException(
                            "RabbitMQ rejected event: " + confirm.getReason());
                }

                if (correlation.getReturned() != null) {
                    throw new IllegalStateException("RabbitMQ returned unroutable event");
                }

                markPublished(event.getId());
            } catch (Exception ex) {
                markFailedAttempt(event.getId());
            }
        }
    }

    @Transactional
    protected void markPublished(String id) {
        repository.findById(id).ifPresent(event -> {
            event.setStatus("PUBLISHED");
            event.setPublishedAt(LocalDateTime.now());
            repository.save(event);
        });
    }

    @Transactional
    protected void markFailedAttempt(String id) {
        repository.findById(id).ifPresent(event -> {
            int attempts = event.getAttempts() + 1;
            event.setAttempts(attempts);
            event.setStatus(attempts >= 10 ? "FAILED" : "PENDING");
            repository.save(event);
        });
    }
}
