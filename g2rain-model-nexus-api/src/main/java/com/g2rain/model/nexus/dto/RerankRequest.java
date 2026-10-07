package com.g2rain.model.nexus.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * OpenAI 兼容风格 Rerank 请求体。
 * <p>
 * 对应常见兼容网关 {@code POST /v1/rerank}：query + documents，
 * 可选 {@code top_n} / {@code return_documents}。
 *
 * @author alpha
 * @since 2026/8/1
 */
@Setter
@Getter
@NoArgsConstructor
@Schema(description = "Rerank 请求")
@JsonIgnoreProperties(ignoreUnknown = true)
public class RerankRequest {

    /**
     * 模型标识（对应目录 model_name）。
     */
    @Schema(description = "模型标识")
    private String model;

    /**
     * 检索 / 排序所用的查询文本。
     */
    @NotBlank
    @Schema(description = "查询文本")
    private String query;

    /**
     * 待重排序的文档列表。
     */
    @NotEmpty
    @Schema(description = "待重排序文档列表")
    private List<String> documents;

    /**
     * 返回前 N 条结果；未指定则由厂商决定（通常返回全部）。
     */
    @JsonProperty("top_n")
    @Schema(description = "返回前 N 条结果（可选）")
    private Integer topN;

    /**
     * 是否在结果中回传文档原文；未指定则由厂商默认。
     */
    @JsonProperty("return_documents")
    @Schema(description = "是否回传文档原文（可选）")
    private Boolean returnDocuments;
}
