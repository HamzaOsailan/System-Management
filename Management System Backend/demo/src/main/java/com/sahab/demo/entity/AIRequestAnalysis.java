package com.sahab.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "ai_request_analysis")
public class AIRequestAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "request_id", nullable = false, unique = true)
    private Request request;

    private String category;

    private String subCategory;

    private String priority;

    private Integer urgency;

    @Column(columnDefinition = "TEXT")
    private String summary;

    private String suggestedDepartment;

    @Column(columnDefinition = "TEXT")
    private String suggestedAction;

    @Column(columnDefinition = "TEXT")
    private String reason;

    private LocalDateTime createdAt = LocalDateTime.now();
}