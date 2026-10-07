package com.g2rain.model.nexus.protocol.openai;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * OpenAI 兼容网关 {@code POST /rerank} 请求报文。
 *
 * @author alpha
 * @since 2026/8/1
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OpenAiRerankWireRequest(String model, String query, List<String> documents,
                                      @JsonProperty("top_n") Integer topN,
                                      @JsonProperty("return_documents") Boolean returnDocuments) {

}
