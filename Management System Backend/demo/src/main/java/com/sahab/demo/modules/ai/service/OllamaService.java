
package com.sahab.demo.modules.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OllamaService {

    private static final String ANTHROPIC_API_URL =
            "https://api.anthropic.com";

    private static final String ANTHROPIC_API_VERSION =
            "2023-06-01";

    private final ObjectMapper objectMapper;
    private final RestClient client;

    @Value("${anthropic.api-key:}")
    private String anthropicApiKey;

    @Value("${anthropic.model:claude-sonnet-4-5}")
    private String anthropicModel;

    public OllamaService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        this.client = RestClient.builder()
                .baseUrl(ANTHROPIC_API_URL)
                .defaultHeader(
                        HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON_VALUE
                )
                .defaultHeader(
                        "anthropic-version",
                        ANTHROPIC_API_VERSION
                )
                .build();
    }

    // =====================================================
    // AI REQUEST ANALYSIS
    // =====================================================

    public String analyze(String prompt) {

        String systemPrompt = """
                You are an expert AI assistant for an internal
                company request management system.

                Understand the actual meaning of the request.

                The request may be Arabic, English, or mixed.

                Do not blindly trust the original category.

                Return meaningful analysis.

                Never return placeholder values such as:
                "Recommended action"
                "Reason for the analysis"
                "N/A"

                Return ONLY valid JSON.
                Do not include Markdown code fences.
                """;

        return sendMessage(
                systemPrompt,
                prompt,
                0.0,
                4096
        );
    }

    // =====================================================
    // CHATBOT
    // =====================================================

    public String chat(
            String systemPrompt,
            String userPrompt
    ) {

        return sendMessage(
                systemPrompt,
                userPrompt,
                0.2,
                4096
        );
    }

    // =====================================================
    // CLAUDE API REQUEST
    // =====================================================

    private String sendMessage(
            String systemPrompt,
            String userPrompt,
            double temperature,
            int maxTokens
    ) {

        if (anthropicApiKey == null
                || anthropicApiKey.isBlank()) {

            throw new IllegalStateException(
                    "Anthropic API key is missing. "
                            + "Set the ANTHROPIC_API_KEY "
                            + "environment variable."
            );
        }

        if (userPrompt == null || userPrompt.isBlank()) {
            throw new IllegalArgumentException(
                    "User prompt cannot be empty."
            );
        }

        Map<String, Object> body = new HashMap<>();

        body.put("model", anthropicModel);
        body.put("max_tokens", maxTokens);
        body.put("temperature", temperature);
        body.put("system", systemPrompt);

        body.put(
                "messages",
                List.of(
                        Map.of(
                                "role", "user",
                                "content", userPrompt
                        )
                )
        );

        try {

            String response = client.post()
                    .uri("/v1/messages")
                    .header(
                            "x-api-key",
                            anthropicApiKey
                    )
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            return extractContent(response);

        } catch (Exception e) {

            // Do not log prompts, API keys, or raw responses.
            System.err.println(
                    "Claude API request failed: "
                            + e.getClass().getSimpleName()
            );

            throw new RuntimeException(
                    "Claude AI request failed. "
                            + "Check API configuration, "
                            + "model availability, and API usage limits.",
                    e
            );
        }
    }

    // =====================================================
    // EXTRACT CLAUDE RESPONSE
    // =====================================================

    private String extractContent(String response) {

        if (response == null || response.isBlank()) {
            throw new RuntimeException(
                    "Claude returned an empty response."
            );
        }

        try {

            JsonNode root = objectMapper.readTree(response);

            JsonNode content = root.path("content");

            if (!content.isArray()) {
                throw new RuntimeException(
                        "Claude response does not contain "
                                + "a valid content array."
                );
            }

            StringBuilder result = new StringBuilder();

            for (JsonNode block : content) {

                if ("text".equals(
                        block.path("type").asText()
                )) {

                    String text = block.path("text").asText("");

                    if (!text.isBlank()) {

                        if (!result.isEmpty()) {
                            result.append("\n");
                        }

                        result.append(text);
                    }
                }
            }

            String extracted = result.toString().trim();

            if (extracted.isBlank()) {
                throw new RuntimeException(
                        "Claude response contains no text."
                );
            }

            return extracted;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to parse Claude response: "
                            + e.getMessage(),
                    e
            );
        }
    }
}
