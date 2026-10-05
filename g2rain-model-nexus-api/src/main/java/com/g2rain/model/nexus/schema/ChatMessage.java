package com.g2rain.model.nexus.schema;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.g2rain.model.nexus.enums.MessageRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * OpenAI Chat Completions 单条消息（messages[] 元素）。
 * <p>
 * 按 {@link MessageRole} 承载 system / user / assistant / tool 语义；
 * assistant 可带 {@code tool_calls}，tool 回执通过 {@code tool_call_id} 关联。
 *
 * @author alpha
 * @since 2026/8/1
 */
@Setter
@Getter
@NoArgsConstructor
@Schema(description = "Chat message")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatMessage {
    @NotNull
    @Schema(description = "角色：system / user / assistant / tool")
    private MessageRole role;

    @Schema(description = "文本内容；assistant 仅 tool_calls 时可为空")
    private String content;

    @JsonProperty("tool_calls")
    @Schema(description = "assistant 发起的 tool_calls")
    private List<ChatToolCall> toolCalls;

    @JsonProperty("tool_call_id")
    @Schema(description = "tool 消息对应的 tool_call_id")
    private String toolCallId;

    @Schema(description = "tool 名称（可选）")
    private String name;
}
