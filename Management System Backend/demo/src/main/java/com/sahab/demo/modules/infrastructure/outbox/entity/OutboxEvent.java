
package com.sahab.demo.modules.infrastructure.outbox.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "outbox_events",
        indexes = {
                @Index(
                        name = "idx_outbox_status_created",
                        columnList = "status, created_at"
                )
        }
)
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "event_type",
            nullable = false,
            length = 100
    )
    private String eventType;

    @Column(
            name = "aggregate_type",
            nullable = false,
            length = 100
    )
    private String aggregateType;

    @Column(
            name = "aggregate_id",
            nullable = false
    )
    private Long aggregateId;

    @Lob
    @Column(
            name = "payload",
            nullable = false,
            columnDefinition = "LONGTEXT"
    )
    private String payload;

    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private String status = "PENDING";

    @Column(
            name = "attempts",
            nullable = false
    )
    private int attempts = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "published_at")
    private LocalDateTime publishedAt;
}
