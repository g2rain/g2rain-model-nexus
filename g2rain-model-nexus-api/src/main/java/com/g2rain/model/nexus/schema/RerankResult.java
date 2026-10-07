package com.g2rain.model.nexus.schema;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * rerank 响应 results[] 元素：原文档下标、相关度分数与可选原文。
 *
 * @author alpha
 * @since 2026/8/1
 */
@Setter
@Getter
@NoArgsConstructor
@Schema(description = "Rerank result")
@JsonIgnoreProperties(ignoreUnknown = true)
public class RerankResult {

    @Schema(description = "对应请求 documents 下标")
    private Integer index;

    @JsonProperty("relevance_score")
    @Schema(description = "相关度分数")
    private Double relevanceScore;

    @Schema(description = "文档原文（return_documents=true 时可能有值）")
    private String document;
}
