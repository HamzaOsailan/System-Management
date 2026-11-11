package com.sahab.demo.modules.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record KnowledgeRetrievalRequest(
        String query,
        Access access,
        int limit
) {
    public record Access(
            List<String> roles,
            @JsonProperty("user_id") String userId,
            String department
    ) {}
}