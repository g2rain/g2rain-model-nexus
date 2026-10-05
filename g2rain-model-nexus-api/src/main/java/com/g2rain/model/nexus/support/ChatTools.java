package com.g2rain.model.nexus.support;

import com.g2rain.model.nexus.enums.ToolType;
import com.g2rain.model.nexus.schema.ChatTool;
import com.g2rain.model.nexus.schema.ChatToolFunction;

/**
 * {@link ChatTool} 组装器（请求侧 tools[] 定义）。
 * <p>
 * 默认 {@code type=function}，{@code parameters} 为空 object schema。
 *
 * <pre>{@code
 * ChatTool tool = ChatTools.function("get_weather")
 *     .description("查询天气")
 *     .parameters(schemaObject)
 *     .build();
 * }</pre>
 *
 * @author alpha
 * @since 2026/8/1
 */
public final class ChatTools {

    /**
     * 禁止实例化。
     */
    private ChatTools() {

    }

    /**
     * 以函数名启动 tools[] 项组装。
     *
     * @param name function.name
     * @return Builder
     */
    public static Builder function(String name) {
        return new Builder(name);
    }

    /**
     * 单条 {@link ChatTool} 链式组装器。
     */
    public static final class Builder {
        private final String name;
        private String description;
        private ToolType type = SupportConstants.DEFAULT_TOOL_TYPE;
        private Object parameters = SupportConstants.EMPTY_OBJ_SCHEMA_MAP;

        private Builder(String name) {
            this.name = name;
        }

        /**
         * 工具类型；默认 {@link SupportConstants#DEFAULT_TOOL_TYPE}。
         */
        public Builder type(ToolType type) {
            this.type = type;
            return this;
        }

        /**
         * 工具说明，展示给模型。
         */
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * function.parameters：JSON Schema 对象（Map / JsonNode 等）。
         *
         * @param parameters schema；未设置时保留空 object schema
         */
        public Builder parameters(Object parameters) {
            this.parameters = parameters;
            return this;
        }

        /**
         * @return 组装完成的 tools[] 元素
         */
        public ChatTool build() {
            ChatToolFunction function = new ChatToolFunction();
            function.setName(name);
            function.setDescription(description);
            function.setParameters(parameters);

            ChatTool tool = new ChatTool();
            tool.setType(type);
            tool.setFunction(function);
            return tool;
        }
    }
}
