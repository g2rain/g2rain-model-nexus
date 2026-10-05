package com.g2rain.model.nexus.schema;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * chat.completion 响应中的 usage：prompt / completion / total token 统计。
 *
 * @author alpha
 * @since 2026/8/1
 */
@Setter
@Getter
@NoArgsConstructor
@Schema(description = "Token 用量")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatCompletionUsage {

    @JsonProperty("prompt_tokens")
    @Schema(description = "输入 token 数")
    private Integer promptTokens;

    @JsonProperty("completion_tokens")
    @Schema(description = "输出 token 数")
    private Integer completionTokens;

    @JsonProperty("total_tokens")
    @Schema(description = "总 token 数")
    private Integer totalTokens;
}
