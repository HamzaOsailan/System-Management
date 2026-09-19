package com.sahab.demo.dto;

import com.sahab.demo.enums.RequestCategory;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRequestDTO {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private RequestCategory category;

    public CreateRequestDTO() {
    }
}