package com.sahab.requestservice.dto;

import jakarta.validation.constraints.*;

public record CreateRequestDTO(
    @NotBlank @Size(max = 200) String title,
    @NotBlank String description,
    @Size(max = 80) String category,
    @Size(max = 30) String priority,
    @NotNull @Min(1) @Max(365) Integer durationDays,
    @NotNull @Positive Long userId,
    @Positive Long managerId
) {}
