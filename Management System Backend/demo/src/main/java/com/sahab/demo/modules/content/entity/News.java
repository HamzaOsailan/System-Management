
        package com.sahab.demo.modules.content.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "news")
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /* =========================================================
       IMAGE
    ========================================================= */

    @Lob
    @Column(name = "image_data", columnDefinition = "LONGBLOB")
    private byte[] imageData;

    @Column(name = "image_content_type", length = 100)
    private String imageContentType;

    @Column(name = "image_name", length = 255)
    private String imageName;

    @Column(name = "has_image", nullable = false)
    private boolean hasImage = false;

    /* =========================================================
       PUBLISHING
    ========================================================= */

    @Column(nullable = false)
    private boolean published = true;

    /* =========================================================
       CREATOR
    ========================================================= */

    @Column(name = "created_by_name", length = 255)
    private String createdByName;

    /* =========================================================
       DATE
    ========================================================= */

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

