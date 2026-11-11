package com.sahab.demo.modules.notifications.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private String message;
}