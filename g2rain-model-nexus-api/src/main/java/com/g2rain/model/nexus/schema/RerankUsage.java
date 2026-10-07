package com.g2rain.model.nexus.schema;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * rerank 响应中的 usage：prompt / total token 统计。
 *
 * @author alpha
 * @since 2026/8/1
 */
@Setter
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Rerank Token 用量")
public class RerankUsage {

    @JsonProperty("prompt_tokens")
    @Schema(description = "输入 token 数")
    private Integer promptTokens;

    @JsonProperty("total_tokens")
    @Schema(description = "总 token 数")
    private Integer totalTokens;
}
