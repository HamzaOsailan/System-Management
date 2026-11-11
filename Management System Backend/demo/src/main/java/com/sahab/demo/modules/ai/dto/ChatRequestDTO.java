package com.sahab.demo.modules.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRequestDTO {

    @NotBlank(message = "Message is required")
    private String message;
}