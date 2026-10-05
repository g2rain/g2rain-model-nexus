package com.g2rain.model.nexus.schema;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * chat.completion 响应中的 choices[] 元素（候选结果）。
 *
 * @author alpha
 * @since 2026/8/1
 */
@Setter
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Chat Completions choice")
public class ChatCompletionChoice {

    @Schema(description = "choice 下标")
    private Integer index;

    @Schema(description = "助手消息")
    private ChatMessage message;

    @JsonProperty("finish_reason")
    @Schema(description = "结束原因：stop / tool_calls / length 等")
    private String finishReason;
}
