package com.g2rain.model.nexus.vo;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.g2rain.model.nexus.schema.EmbeddingData;
import com.g2rain.model.nexus.schema.EmbeddingUsage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * OpenAI 风格 embeddings 响应体。
 * <p>
 * 对应 {@code POST /v1/embeddings} 成功返回：{@code object=list} + data[] + usage。
 *
 * @author alpha
 * @since 2026/8/1
 */
@Setter
@Getter
@NoArgsConstructor
@Schema(description = "Embeddings 响应")
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmbeddingsResponse {

    /**
     * 对象类型，固定 {@code list}。
     */
    @Schema(description = "对象类型，固定 list")
    private String object;

    /**
     * 请求 / 实际使用的模型标识。
     */
    @Schema(description = "模型标识")
    private String model;

    /**
     * 向量数据，与 input 顺序一一对应。
     */
    @Schema(description = "向量数据，与 input 一一对应")
    private List<EmbeddingData> data;

    /**
     * Token 用量统计。
     */
    @Schema(description = "Token 用量")
    private EmbeddingUsage usage;
}
