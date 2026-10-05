package com.g2rain.model.nexus.schema;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * tools[].function：函数名、说明与 parameters JSON Schema。
 *
 * @author alpha
 * @since 2026/8/1
 */
@Setter
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Chat tool function 定义")
public class ChatToolFunction {

    @NotBlank
    @Schema(description = "工具名称")
    private String name;

    @Schema(description = "工具说明")
    private String description;

    @Schema(description = "参数 JSON Schema 对象")
    private Object parameters;
}
