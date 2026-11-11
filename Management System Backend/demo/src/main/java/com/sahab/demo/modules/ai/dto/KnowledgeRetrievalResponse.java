package com.sahab.demo.modules.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public record KnowledgeRetrievalResponse(
        List<Chunk> results,
        int count
) {
    public record Chunk(
            @JsonProperty("document_id") String documentId,
            @JsonProperty("chunk_index") int chunkIndex,
            String content,
            String title,
            String source,
            String category,
            String language,
            double score,
            Map<String, Object> metadata
    ) {}
}