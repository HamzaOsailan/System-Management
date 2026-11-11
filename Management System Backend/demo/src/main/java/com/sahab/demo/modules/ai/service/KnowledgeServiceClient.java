package com.sahab.demo.modules.ai.service;

import com.sahab.demo.modules.ai.dto.KnowledgeRetrievalRequest;
import com.sahab.demo.modules.ai.dto.KnowledgeRetrievalResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.util.List;

@Component
public class KnowledgeServiceClient {

    private final RestClient client;
    private final String apiKey;

    public KnowledgeServiceClient(
            @Value("${ai.service.base-url:http://127.0.0.1:8001}") String baseUrl,
            @Value("${ai.service.api-key:}") String apiKey
    ) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();

        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(10000);

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader(
                        HttpHeaders.ACCEPT,
                        MediaType.APPLICATION_JSON_VALUE
                );

        if (!this.apiKey.isBlank()) {
            builder.defaultHeader("X-Internal-Api-Key", this.apiKey);
        }

        this.client = builder.build();
    }

    public KnowledgeRetrievalResponse retrieve(
            String query,
            List<String> roles,
            String userId,
            String department
    ) {
        if (apiKey.isBlank()) {
            throw new IllegalStateException(
                    "AI service API key is not configured"
            );
        }

        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException(
                    "Knowledge query must not be blank"
            );
        }

        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one access role is required"
            );
        }

        KnowledgeRetrievalRequest request =
                new KnowledgeRetrievalRequest(
                        query,
                        new KnowledgeRetrievalRequest.Access(
                                List.copyOf(roles),
                                userId,
                                department
                        ),
                        5
                );

        try {
            KnowledgeRetrievalResponse response = client.post()
                    .uri("/internal/v1/knowledge/retrieve")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(KnowledgeRetrievalResponse.class);

            if (response == null || response.results() == null) {
                throw new IllegalStateException(
                        "AI service returned an invalid retrieval response"
                );
            }

            return response;

        } catch (RestClientException exception) {
            throw new IllegalStateException(
                    "AI service retrieval failed: "
                            + exception.getClass().getSimpleName(),
                    exception
            );
        }
    }
}