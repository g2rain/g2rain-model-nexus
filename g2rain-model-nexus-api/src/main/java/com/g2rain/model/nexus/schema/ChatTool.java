package com.g2rain.model.nexus.schema;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.g2rain.model.nexus.enums.ToolType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * OpenAI Chat Completions 请求侧 tools[] 元素。
 * <p>
 * 当前主要支持 {@code type=function}，具体入参 schema 在 {@link ChatToolFunction}。
 *
 * @author alpha
 * @since 2026/8/1
 */
@Setter
@Getter
@NoArgsConstructor
@Schema(description = "Chat tool 定义")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatTool {

    @Schema(description = "工具类型，通常为 function")
    private ToolType type = ToolType.FUNCTION;

    @Valid
    @NotNull
    @Schema(description = "function 定义")
    private ChatToolFunction function;
}
