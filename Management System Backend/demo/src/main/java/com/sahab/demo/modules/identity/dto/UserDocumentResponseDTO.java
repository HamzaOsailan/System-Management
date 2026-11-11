package com.sahab.demo.modules.identity.dto;

import com.sahab.demo.enums.DocumentType;

import java.time.LocalDateTime;

public class UserDocumentResponseDTO {

    private Long id;
    private String originalName;
    private String contentType;
    private Long fileSize;
    private DocumentType type;
    private LocalDateTime createdAt;

    public UserDocumentResponseDTO() {
    }

    public UserDocumentResponseDTO(
            Long id,
            String originalName,
            String contentType,
            Long fileSize,
            DocumentType type,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.originalName = originalName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.type = type;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public DocumentType getType() {
        return type;
    }

    public void setType(DocumentType type) {
        this.type = type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}