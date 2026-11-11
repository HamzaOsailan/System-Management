package com.sahab.requestservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ApprovalActionDTO(
        @NotNull @Positive Long approverId,
        @NotBlank String role,
        @Size(max = 2000) String comment
) {
}