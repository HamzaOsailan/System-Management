package com.sahab.demo.modules.ai.service;

import com.sahab.demo.modules.ai.dto.KnowledgeRetrievalResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KnowledgeServiceClientRuntimeTest {

    @Test
    void shouldAuthenticateAndRetrieveFromPythonAiService() {
        String apiKey = System.getenv("AI_SERVICE_API_KEY");

        assertNotNull(apiKey, "AI_SERVICE_API_KEY is missing from test environment");
        assertFalse(apiKey.isBlank(), "AI_SERVICE_API_KEY is blank");

        String baseUrl = System.getenv("AI_SERVICE_BASE_URL");
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://127.0.0.1:8001";
        }

        KnowledgeServiceClient client =
                new KnowledgeServiceClient(baseUrl, apiKey);

        KnowledgeRetrievalResponse response = client.retrieve(
                "runtime connectivity diagnostic",
                List.of("USER"),
                "runtime-diagnostic",
                "IT"
        );

        assertNotNull(response, "Python returned a null response");
        assertNotNull(response.results(), "Python response has no results list");
        assertTrue(response.count() >= 0, "Invalid result count");

        System.out.println("AI_SERVICE_KEY_PRESENT=true");
        System.out.println("PYTHON_RETRIEVAL_AUTHENTICATION=SUCCESS");
        System.out.println("PYTHON_RESULT_COUNT=" + response.count());
    }
}
