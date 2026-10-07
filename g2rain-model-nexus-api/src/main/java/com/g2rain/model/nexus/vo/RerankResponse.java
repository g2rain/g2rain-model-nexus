package com.g2rain.model.nexus.vo;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.g2rain.model.nexus.schema.RerankResult;
import com.g2rain.model.nexus.schema.RerankUsage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * OpenAI 兼容风格 rerank 响应体。
 * <p>
 * 对应 {@code POST /v1/rerank} 成功返回：results[] + 可选 usage。
 *
 * @author alpha
 * @since 2026/8/1
 */
@Setter
@Getter
@NoArgsConstructor
@Schema(description = "Rerank 响应")
@JsonIgnoreProperties(ignoreUnknown = true)
public class RerankResponse {

    /**
     * 厂商返回的请求 id（若有）。
     */
    @Schema(description = "请求 id（厂商可选返回）")
    private String id;

    /**
     * 请求 / 实际使用的模型标识。
     */
    @Schema(description = "模型标识")
    private String model;

    /**
     * 重排序结果，按相关度降序。
     */
    @Schema(description = "重排序结果，按相关度降序")
    private List<RerankResult> results;

    /**
     * Token 用量统计（厂商可选返回）。
     */
    @Schema(description = "Token 用量")
    private RerankUsage usage;
}
