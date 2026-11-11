
package com.sahab.demo.modules.infrastructure.outbox.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sahab.demo.modules.infrastructure.outbox.entity.OutboxEvent;
import com.sahab.demo.entity.Request;
import com.sahab.demo.modules.infrastructure.outbox.repository.OutboxEventRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.MANDATORY)
    public OutboxEvent recordRequestEvent(
            Request request,
            String eventType
    ) {
        if (request == null || request.getId() == null) {
            throw new IllegalArgumentException(
                    "A saved request is required."
            );
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("eventType", eventType);
        payload.put("requestId", request.getId());
        payload.put("title", request.getTitle());
        payload.put(
                "status",
                request.getStatus() != null
                        ? request.getStatus().name()
                        : null
        );
        payload.put(
                "category",
                request.getCategory() != null
                        ? request.getCategory().name()
                        : null
        );
        payload.put(
                "userId",
                request.getUser() != null
                        ? request.getUser().getId()
                        : null
        );
        payload.put("occurredAt", LocalDateTime.now());

        final String serializedPayload;

        try {
            serializedPayload =
                    objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Could not serialize outbox event.",
                    exception
            );
        }

        OutboxEvent event = new OutboxEvent();
        event.setEventType(eventType);
        event.setAggregateType("REQUEST");
        event.setAggregateId(request.getId());
        event.setPayload(serializedPayload);
        event.setStatus("PENDING");
        event.setAttempts(0);
        event.setCreatedAt(LocalDateTime.now());

        return outboxEventRepository.save(event);
    }
}
