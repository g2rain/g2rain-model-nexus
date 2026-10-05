package com.g2rain.model.nexus.enums;

import com.g2rain.common.utils.Strings;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Arrays;

/**
 * 模型接入协议枚举（存库 {@code protocol} 字段）。
 * <p>
 * 与各协议客户端声明的 protocol 对齐，供注册表按协议路由到对应厂商实现。
 *
 * @author alpha
 * @since 2026/8/1
 */
@Schema(description = "模型接入协议")
public enum ModelProtocol {

    @Schema(description = "OpenAI 兼容协议")
    OPENAI,

    @Schema(description = "Anthropic 协议")
    ANTHROPIC,

    @Schema(description = "Gemini API")
    GEMINI;

    /**
     * 按枚举名解析协议（忽略大小写）；空白或无法识别返回 null。
     *
     * @param name 存库协议字符串
     * @return 匹配的枚举，或 null
     */
    public static ModelProtocol nameOf(String name) {
        if (Strings.isBlank(name)) {
            return null;
        }

        String trimmed = Strings.trim(name);
        return Arrays.stream(values()).filter(protocol ->
            Strings.equalsIgnoreCase(protocol.name(), trimmed)
        ).findFirst().orElse(null);
    }
}
