package com.g2rain.model.nexus.vo;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.g2rain.model.nexus.schema.OpenAiError;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author alpha
 * @since 2026/8/1
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OpenAI 错误响应")
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAiErrorResponseVo {

    /**
     * 错误详情。
     */
    @Schema(description = "错误详情")
    private OpenAiError error;

    /**
     * 快捷构造。
     *
     * @param message 错误描述
     * @param type    错误类型
     * @param param   相关参数，可为 null
     * @param code    业务错误码
     * @return 响应根对象
     */
    public static OpenAiErrorResponseVo of(String message, String type, String param, String code) {
        return new OpenAiErrorResponseVo(new OpenAiError(message, type, param, code));
    }
}
