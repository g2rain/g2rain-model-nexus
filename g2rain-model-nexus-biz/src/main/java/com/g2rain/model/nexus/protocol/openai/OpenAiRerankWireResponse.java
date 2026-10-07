package com.g2rain.model.nexus.protocol.openai;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.JsonNode;

import java.util.List;

/**
 * OpenAI 兼容网关 {@code POST /rerank} 响应报文
 * （Cohere / SiliconFlow 等常见形态）。
 *
 * @author alpha
 * @since 2026/8/1
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenAiRerankWireResponse(String id, List<Result> results, Meta meta, TokenUsage usage) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Result(Integer index, @JsonProperty("relevance_score") Double relevanceScore, JsonNode document) {

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Meta(Tokens tokens) {

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Tokens(@JsonProperty("input_tokens") Integer inputTokens,
                         @JsonProperty("output_tokens") Integer outputTokens) {

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TokenUsage(@JsonProperty("prompt_tokens") Integer promptTokens,
                             @JsonProperty("total_tokens") Integer totalTokens) {

    }
}
