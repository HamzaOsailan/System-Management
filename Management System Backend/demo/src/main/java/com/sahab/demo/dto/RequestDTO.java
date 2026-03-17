package com.sahab.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RequestDTO {

    @NotBlank(message= "Title is required")
    private String title;
    @NotBlank(message= "Description is required")
    private String description;
    @NotNull(message= "UserId is required")
    private Long userId;

    public RequestDTO(){

    }
    public String getTitle(){
        return title;
    }
    public void setTitle(String title){
        this.title=title;
    }
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}

