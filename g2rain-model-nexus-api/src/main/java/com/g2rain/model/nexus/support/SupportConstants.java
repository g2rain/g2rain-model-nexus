package com.g2rain.model.nexus.support;

import com.g2rain.model.nexus.enums.ToolType;

import java.util.Map;

/**
 * {@code support} 包内共享字面量。
 * <p>
 * 仅包内可见，供 Builder / Converter 统一引用，避免魔法字符串散落各处。
 * 本类禁止实例化。
 *
 * @author alpha
 * @since 2026/8/1
 */
final class SupportConstants {

    /**
     * tool_call.function.arguments / 默认空 JSON 对象。
     */
    static final String EMPTY_JSON_OBJECT = "{}";

    /**
     * function.parameters 缺失时的空 object schema（字符串，供 Converter 等用）。
     */
    static final String EMPTY_OBJ_SCHEMA = "{\"type\":\"object\",\"properties\":{}}";

    /**
     * function.parameters 缺失时的空 object schema（对象，供 Builder 写入 DTO）。
     */
    static final Map<String, Object> EMPTY_OBJ_SCHEMA_MAP = Map.of(
        "type", "object",
        "properties", Map.of()
    );

    /**
     * OpenAI 响应顶层 object 固定值。
     */
    static final String OBJECT_CHAT_COMPLETION = "chat.completion";

    /**
     * 自造补全 ID 前缀。
     */
    static final String ID_PREFIX = "chatcmpl_";

    /**
     * Spring AI metadata 中 created 时间戳 key。
     */
    static final String METADATA_CREATED = "created";

    /**
     * finish_reason：正常结束。
     */
    static final String FINISH_REASON_STOP = "stop";

    /**
     * finish_reason：模型请求调用工具。
     */
    static final String FINISH_REASON_TOOL_CALLS = "tool_calls";

    /**
     * tools / tool_calls 默认 type。
     */
    static final ToolType DEFAULT_TOOL_TYPE = ToolType.FUNCTION;

    /**
     * 禁止实例化。
     */
    private SupportConstants() {

    }
}
