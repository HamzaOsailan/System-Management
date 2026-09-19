package com.sahab.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OllamaResponse {

    private String model;

    private String response;

    private boolean done;
}