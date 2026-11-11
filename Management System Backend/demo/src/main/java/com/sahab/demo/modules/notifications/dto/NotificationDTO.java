package com.sahab.demo.modules.notifications.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NotificationDTO {

    private Long id;

    private String message;

    private boolean isRead;

    private LocalDateTime createdAt;

    public NotificationDTO() {
    }

    public NotificationDTO(
            Long id,
            String message,
            boolean isRead,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.message = message;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        this.isRead = read;
    }
}