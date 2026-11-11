package com.sahab.demo.modules.ai.service;

import com.sahab.demo.modules.ai.dto.KnowledgeRetrievalResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KnowledgeServiceClientIntegrationProbeTest {

    @Test
    void retrievesKnowledgeFromIsolatedAiService() {
        String apiKey = System.getenv("AI_SERVICE_API_KEY");

        assertNotNull(apiKey, "AI_SERVICE_API_KEY is missing from Maven process");
        assertFalse(apiKey.isBlank(), "AI_SERVICE_API_KEY is empty");

        KnowledgeServiceClient client = new KnowledgeServiceClient(
                "http://127.0.0.1:8002",
                apiKey
        );

        KnowledgeRetrievalResponse response = client.retrieve(
                "اختبار تكامل Spring مع خدمة المعرفة",
                java.util.List.of("USER"),
                "1",
                null
        );

        assertNotNull(response, "Response must not be null");
        assertNotNull(response.results(), "Results must not be null");
        assertEquals(response.results().size(), response.count());

        System.out.println("SPRING CLIENT HTTP INTEGRATION: SUCCESS");
        System.out.println("Response count: " + response.count());
        System.out.println("Response structure: VALID");
    }
}