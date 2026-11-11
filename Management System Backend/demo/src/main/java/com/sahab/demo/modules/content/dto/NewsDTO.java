
        package com.sahab.demo.modules.content.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NewsDTO {

    private Long id;

    private String title;

    private String content;

    private LocalDateTime publishedAt;

    private String imageName;

    private String imageUrl;

    private String createdBy;
}

