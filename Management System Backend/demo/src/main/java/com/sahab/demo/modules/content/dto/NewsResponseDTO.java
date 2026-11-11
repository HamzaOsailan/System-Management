package com.sahab.demo.modules.content.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsResponseDTO {

    private Long id;

    private String title;

    private String content;

    private LocalDateTime createdAt;

    private String createdByName;

    private boolean hasImage;
}