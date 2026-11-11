package com.sahab.demo.modules.content.entity;

import com.sahab.demo.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "news_reads",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_news_reads_user_news",
                        columnNames = {"user_id", "news_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_news_reads_news_id",
                        columnList = "news_id"
                )
        }
)
public class NewsRead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "news_id", nullable = false)
    private News news;

    @Column(name = "read_at", nullable = false)
    private LocalDateTime readAt;

    @PrePersist
    protected void onCreate() {
        if (readAt == null) {
            readAt = LocalDateTime.now();
        }
    }
}