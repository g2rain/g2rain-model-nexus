package com.g2rain.model.nexus.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.g2rain.common.utils.Strings;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Arrays;

/**
 * OpenAI Chat messages[].role 枚举。
 * <p>
 * 序列化值为小写 code（system / user / assistant / tool）；
 * 反序列化忽略大小写，无法识别时返回 null。
 *
 * @author alpha
 * @since 2026/8/1
 */
@Schema(description = "Chat message 角色")
public enum MessageRole {

    @Schema(description = "system")
    SYSTEM("system"),

    @Schema(description = "user")
    USER("user"),

    @Schema(description = "assistant")
    ASSISTANT("assistant"),

    @Schema(description = "tool")
    TOOL("tool");

    private final String code;

    MessageRole(String code) {
        this.code = code;
    }

    /**
     * @return OpenAI 协议侧小写角色字符串
     */
    @JsonValue
    public String code() {
        return code;
    }

    /**
     * 按协议 code 解析角色（忽略大小写）；空白或无法识别返回 null。
     *
     * @param raw 原始角色字符串
     * @return 匹配的枚举，或 null
     */
    @JsonCreator
    public static MessageRole ofCode(String raw) {
        if (Strings.isBlank(raw)) {
            return null;
        }

        String key = Strings.trim(raw);
        return Arrays.stream(values()).filter(role ->
            Strings.equalsIgnoreCase(role.code, key)
        ).findFirst().orElse(null);
    }
}
