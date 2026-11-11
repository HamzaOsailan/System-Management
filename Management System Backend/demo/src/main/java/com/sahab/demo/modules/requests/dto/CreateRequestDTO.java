
package com.sahab.demo.modules.requests.dto;

import com.sahab.demo.enums.RequestCategory;
import com.sahab.demo.enums.RequestPriority;
import jakarta.validation.constraints.Min;
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

    private RequestPriority priority;

    @Min(value = 1, message = "Duration must be at least 1 day")
    private Integer durationDays;

    public CreateRequestDTO() {
    }
}
