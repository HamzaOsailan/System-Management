package com.sahab.demo.modules.ai.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OllamaResponse {

    private String model;

    private String response;

    private boolean done;
}