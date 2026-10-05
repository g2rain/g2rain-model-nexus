package com.g2rain.model.nexus.schema;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.g2rain.model.nexus.enums.ToolType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * assistant 消息上的 tool_calls[] 元素：模型发起的一次工具调用。
 *
 * @author alpha
 * @since 2026/8/1
 */
@Setter
@Getter
@NoArgsConstructor
@Schema(description = "Tool call")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatToolCall {

    @Schema(description = "Tool call id")
    private String id;

    @Schema(description = "类型，通常为 function")
    private ToolType type = ToolType.FUNCTION;

    @Schema(description = "function 调用内容")
    private ChatToolCallFunction function;
}
