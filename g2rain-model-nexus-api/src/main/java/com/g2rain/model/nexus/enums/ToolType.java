package com.g2rain.model.nexus.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.g2rain.common.utils.Strings;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Arrays;

/**
 * OpenAI tools / tool_calls 的 type 枚举。
 * <p>
 * 当前仅支持 {@link #FUNCTION}；序列化为小写 code。
 *
 * @author alpha
 * @since 2026/8/1
 */
@Schema(description = "Chat tool 类型")
public enum ToolType {

    @Schema(description = "function")
    FUNCTION("function");

    private final String code;

    ToolType(String code) {
        this.code = code;
    }

    /**
     * @return OpenAI 协议侧小写 type 字符串
     */
    @JsonValue
    public String code() {
        return code;
    }

    /**
     * 按协议 code 解析类型（忽略大小写）；空白或无法识别返回 null。
     *
     * @param raw 原始 type 字符串
     * @return 匹配的枚举，或 null
     */
    @JsonCreator
    public static ToolType ofCode(String raw) {
        if (Strings.isBlank(raw)) {
            return null;
        }

        String key = Strings.trim(raw);
        return Arrays.stream(values()).filter(type ->
            Strings.equalsIgnoreCase(type.code, key)
        ).findFirst().orElse(null);
    }
}
