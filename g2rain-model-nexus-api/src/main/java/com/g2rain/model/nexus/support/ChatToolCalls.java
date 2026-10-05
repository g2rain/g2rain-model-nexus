package com.g2rain.model.nexus.support;

import com.g2rain.model.nexus.enums.ToolType;
import com.g2rain.model.nexus.schema.ChatToolCall;
import com.g2rain.model.nexus.schema.ChatToolCallFunction;

/**
 * {@link ChatToolCall} 组装器（通常挂在 assistant 消息上）。
 * <p>
 * 默认 {@code type=function}，{@code arguments} 为 {@code {}}。
 *
 * <pre>{@code
 * ChatToolCall call = ChatToolCalls.builder()
 *     .id("call_1")
 *     .function("search", "{\"q\":\"weather\"}")
 *     .build();
 * }</pre>
 *
 * @author alpha
 * @since 2026/8/1
 */
public final class ChatToolCalls {

    /**
     * 禁止实例化。
     */
    private ChatToolCalls() {

    }

    /**
     * @return 空 Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 以 tool_call.id 启动组装。
     *
     * @param id tool_call.id
     * @return 已设置 id 的 Builder
     */
    public static Builder of(String id) {
        return builder().id(id);
    }

    /**
     * 单条 {@link ChatToolCall} 链式组装器。
     */
    public static final class Builder {
        private String id;
        private String functionName;
        private ToolType type = SupportConstants.DEFAULT_TOOL_TYPE;
        private String arguments = SupportConstants.EMPTY_JSON_OBJECT;

        /**
         * tool_call.id。
         */
        public Builder id(String id) {
            this.id = id;
            return this;
        }

        /**
         * tool_call.type；默认 function。
         */
        public Builder type(ToolType type) {
            this.type = type;
            return this;
        }

        /**
         * 同时设置 function.name 与 function.arguments。
         *
         * @param name      函数名
         * @param arguments JSON 参数字符串
         */
        public Builder function(String name, String arguments) {
            this.functionName = name;
            this.arguments = arguments;
            return this;
        }

        /**
         * 仅设置 function.name。
         */
        public Builder functionName(String name) {
            this.functionName = name;
            return this;
        }

        /**
         * 仅设置 function.arguments（JSON 字符串）。
         */
        public Builder arguments(String arguments) {
            this.arguments = arguments;
            return this;
        }

        /**
         * @return 组装完成的 tool_call
         */
        public ChatToolCall build() {
            ChatToolCallFunction function = new ChatToolCallFunction();
            function.setName(functionName);
            function.setArguments(arguments);

            ChatToolCall call = new ChatToolCall();
            call.setId(id);
            call.setType(type);
            call.setFunction(function);
            return call;
        }
    }
}
