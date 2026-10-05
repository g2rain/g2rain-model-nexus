package com.g2rain.model.nexus.schema;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * embeddings 响应 data[] 元素：单条向量及其下标。
 *
 * @author alpha
 * @since 2026/8/1
 */
@Setter
@Getter
@NoArgsConstructor
@Schema(description = "Embedding data")
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmbeddingData {

    @Schema(description = "对象类型，固定 embedding")
    private String object;

    @Schema(description = "对应 input 下标")
    private Integer index;

    @Schema(description = "向量")
    private float[] embedding;
}
