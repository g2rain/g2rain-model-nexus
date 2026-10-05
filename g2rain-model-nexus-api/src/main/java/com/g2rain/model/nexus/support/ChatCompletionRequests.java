package com.g2rain.model.nexus.support;

import com.g2rain.common.utils.Collections;
import com.g2rain.model.nexus.dto.ChatCompletionRequest;
import com.g2rain.model.nexus.enums.MessageRole;
import com.g2rain.model.nexus.schema.ChatMessage;
import com.g2rain.model.nexus.schema.ChatTool;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * {@link ChatCompletionRequest} 组装入口（网关侧 SDK）。
 * <p>
 * 覆盖当前请求体全部结构：model、messages（含 tool_calls / tool 回执）、tools。
 *
 * <pre>{@code
 * ChatCompletionRequest req = ChatCompletionRequests.of(model)
 *     .system("you are helpful")
 *     .user("weather?")
 *     .assistant(a -> a
 *         .toolCall(tc -> tc.id("c1").function("get_weather", "{\"city\":\"SZ\"}")))
 *     .toolResult("c1", "{\"ok\":true}")
 *     .addTool("get_weather", t -> t.description("查询天气").parameters(schema))
 *     .build();
 * }</pre>
 *
 * @author alpha
 * @since 2026/8/1
 */
public final class ChatCompletionRequests {

    /**
     * 禁止实例化。
     */
    private ChatCompletionRequests() {

    }

    /**
     * 以指定模型标识启动组装。
     *
     * @param model 模型标识（对应目录 model_name）
     * @return 请求 Builder
     */
    public static Builder of(String model) {
        return new Builder(model);
    }

    /**
     * 不预置 model，稍后由调用方自行填充或保持 null。
     *
     * @return 请求 Builder
     */
    public static Builder of() {
        return new Builder();
    }

    /**
     * {@link ChatCompletionRequest} 链式组装器。
     */
    public static final class Builder {
        private String model;
        private final List<ChatTool> tools = new ArrayList<>();
        private final List<ChatMessage> messages = new ArrayList<>();

        private Builder(String model) {
            this.model = model;
        }

        private Builder() {

        }

        /**
         * 追加一条 system 消息。
         */
        public Builder system(String content) {
            return message(ChatMessages.system(content));
        }

        /**
         * 追加一条可进一步配置的 system 消息。
         */
        public Builder system(Consumer<ChatMessages.Builder> configurer) {
            return message(configureMessage(ChatMessages.system(), configurer));
        }

        /**
         * 追加一条 user 消息。
         */
        public Builder user(String content) {
            return message(ChatMessages.user(content));
        }

        /**
         * 追加一条可进一步配置的 user 消息。
         */
        public Builder user(Consumer<ChatMessages.Builder> configurer) {
            return message(configureMessage(ChatMessages.user(), configurer));
        }

        /**
         * 追加一条 assistant 文本消息。
         */
        public Builder assistant(String content) {
            return message(ChatMessages.assistant(content));
        }

        /**
         * 追加一条可配置 tool_calls 的 assistant 消息。
         */
        public Builder assistant(Consumer<ChatMessages.Builder> configurer) {
            return message(configureMessage(ChatMessages.assistant(), configurer));
        }

        /**
         * 追加 {@code role=tool} 回执消息。
         *
         * @param toolCallId 对应的 tool_call.id
         * @param content    工具执行结果（通常为 JSON 字符串）
         */
        public Builder toolResult(String toolCallId, String content) {
            return message(ChatMessages.tool(toolCallId).content(content).build());
        }

        /**
         * 追加 {@code role=tool} 回执消息（可配 name 等）。
         *
         * @param toolCallId 对应的 tool_call.id
         * @param configurer 额外字段配置
         */
        public Builder toolResult(String toolCallId, Consumer<ChatMessages.Builder> configurer) {
            return message(configureMessage(ChatMessages.tool(toolCallId), configurer));
        }

        /**
         * 按角色追加一条简单文本消息。
         */
        public Builder message(MessageRole role, String content) {
            ChatMessage message = new ChatMessage();
            message.setRole(role);
            message.setContent(content);
            return message(message);
        }

        /**
         * 追加已构造好的消息；null 忽略。
         */
        public Builder message(ChatMessage message) {
            if (Objects.nonNull(message)) {
                this.messages.add(message);
            }

            return this;
        }

        /**
         * 批量追加消息；空列表忽略。
         */
        public Builder messages(List<ChatMessage> messages) {
            if (Collections.isNotEmpty(messages)) {
                this.messages.addAll(messages);
            }

            return this;
        }

        /**
         * 追加一条 tools[] 定义；null 忽略。
         */
        public Builder addTool(ChatTool tool) {
            if (Objects.nonNull(tool)) {
                this.tools.add(tool);
            }

            return this;
        }

        /**
         * 以函数名启动嵌套 {@link ChatTools.Builder}，组装后写入 tools[]。
         *
         * @param name       function.name
         * @param configurer 描述 / parameters 等配置
         */
        @SuppressWarnings("UnusedReturnValue")
        public Builder addTool(String name, Consumer<ChatTools.Builder> configurer) {
            ChatTools.Builder nested = ChatTools.function(name);
            configurer.accept(nested);
            return addTool(nested.build());
        }

        /**
         * 批量追加 tools[]；空列表忽略。
         */
        public Builder tools(List<ChatTool> tools) {
            if (Collections.isNotEmpty(tools)) {
                this.tools.addAll(tools);
            }

            return this;
        }

        /**
         * 生成不可变快照形式的请求体（messages / tools 使用 {@link List#copyOf}）。
         *
         * @return 组装完成的请求体
         */
        public ChatCompletionRequest build() {
            ChatCompletionRequest req = new ChatCompletionRequest();
            req.setModel(model);
            req.setTools(List.copyOf(tools));
            req.setMessages(List.copyOf(messages));
            return req;
        }

        /**
         * 对嵌套消息 Builder 应用配置并 build。
         */
        private static ChatMessage configureMessage(ChatMessages.Builder builder,
                                                    Consumer<ChatMessages.Builder> configurer) {
            configurer.accept(builder);
            return builder.build();
        }
    }
}
