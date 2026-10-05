package com.g2rain.model.nexus.schema;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * tool_calls[].function：被调用的函数名与 JSON 参数字符串。
 *
 * @author alpha
 * @since 2026/8/1
 */
@Setter
@Getter
@NoArgsConstructor
@Schema(description = "Tool call function")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatToolCallFunction {

    @Schema(description = "工具名称")
    private String name;

    @Schema(description = "参数 JSON 字符串")
    private String arguments;
}
