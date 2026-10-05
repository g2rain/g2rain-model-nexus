package com.g2rain.model.nexus.schema;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * OpenAI 风格 error 对象（错误响应体中的 {@code error} 字段）。
 *
 * @author alpha
 * @since 2026/8/1
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OpenAI error 对象")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAiError {

    /**
     * 面向调用方的错误描述。
     */
    @Schema(description = "错误描述")
    private String message;

    /**
     * 错误大类，如 invalid_request_error、authentication_error。
     */
    @Schema(description = "错误类型")
    private String type;

    /**
     * 出错的请求参数名；无则 null。
     */
    @Schema(description = "相关参数名")
    private String param;

    /**
     * 机器可读错误码，复用平台 ErrorCode。
     */
    @Schema(description = "错误码")
    private String code;
}
