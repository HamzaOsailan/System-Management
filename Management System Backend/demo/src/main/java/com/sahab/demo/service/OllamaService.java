package com.sahab.demo.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OllamaService {

    private final ObjectMapper objectMapper;

    @Value("${ollama.url:http://localhost:11434}")
    private String ollamaUrl;

    @Value("${ollama.model:gemma4:26b}")
    private String ollamaModel;

    public OllamaService(
            ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
    }

    public String analyze(String prompt) {

        RestClient client =
                RestClient.builder()
                        .baseUrl(ollamaUrl)
                        .build();

        Map<String, String> systemMessage =
                Map.of(
                        "role",
                        "system",
                        "content",
                        """
                        You are an expert AI assistant for an
                        internal company request management system.

                        Understand the actual meaning of the request.

                        The request may be Arabic, English,
                        or mixed.

                        Do not blindly trust the original category.

                        Return meaningful analysis.

                        Never return placeholder values such as:
                        "Recommended action"
                        "Reason for the analysis"
                        "N/A"

                        Return ONLY valid JSON.
                        """
                );

        Map<String, String> userMessage =
                Map.of(
                        "role",
                        "user",
                        "content",
                        prompt
                );

        Map<String, Object> options =
                new HashMap<>();

        options.put(
                "temperature",
                0
        );

        Map<String, Object> body =
                new HashMap<>();

        body.put(
                "model",
                ollamaModel
        );

        body.put(
                "messages",
                List.of(
                        systemMessage,
                        userMessage
                )
        );

        body.put(
                "stream",
                false
        );

        body.put(
                "format",
                "json"
        );

        body.put(
                "options",
                options
        );

        String response =
                client.post()
                        .uri("/api/chat")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .body(body)
                        .retrieve()
                        .body(String.class);

        try {

            JsonNode root =
                    objectMapper.readTree(response);

            JsonNode message =
                    root.get("message");

            if (message == null
                    || !message.has("content")) {

                throw new RuntimeException(
                        "Invalid Ollama response"
                );
            }

            return message
                    .get("content")
                    .asText();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to read Ollama response",
                    e
            );
        }
    }
}