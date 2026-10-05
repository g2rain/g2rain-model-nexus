package com.g2rain.model.nexus.support;

import com.g2rain.common.utils.Collections;
import com.g2rain.model.nexus.enums.MessageRole;
import com.g2rain.model.nexus.schema.ChatMessage;
import com.g2rain.model.nexus.schema.ChatToolCall;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * {@link ChatMessage} 组装器。
 * <p>
 * 按 OpenAI messages[] 角色提供工厂方法；assistant 可挂 tool_calls，
 * tool 角色通过 {@link #tool(String)} 绑定 {@code tool_call_id}。
 *
 * <pre>{@code
 * ChatMessage msg = ChatMessages.assistant()
 *     .content("call tools")
 *     .toolCall(tc -> tc.id("call_1").function("search", "{\"q\":\"a\"}"))
 *     .build();
 *
 * ChatMessage tool = ChatMessages.tool("call_1")
 *     .name("search")
 *     .content("{\"ok\":true}")
 *     .build();
 * }</pre>
 *
 * @author alpha
 * @since 2026/8/1
 */
public final class ChatMessages {

    /**
     * 禁止实例化。
     */
    private ChatMessages() {

    }

    /**
     * @return role=system 的 Builder
     */
    public static Builder system() {
        return new Builder(MessageRole.SYSTEM);
    }

    /**
     * @return role=user 的 Builder
     */
    public static Builder user() {
        return new Builder(MessageRole.USER);
    }

    /**
     * @return role=assistant 的 Builder
     */
    public static Builder assistant() {
        return new Builder(MessageRole.ASSISTANT);
    }

    /**
     * 启动 tool 回执消息，并预填 {@code tool_call_id}。
     *
     * @param toolCallId 对应 assistant.tool_calls[].id
     * @return role=tool 的 Builder
     */
    public static Builder tool(String toolCallId) {
        return new Builder(MessageRole.TOOL).toolCallId(toolCallId);
    }

    /**
     * 按任意 {@link MessageRole} 启动 Builder。
     *
     * @param role 消息角色
     * @return Builder
     */
    public static Builder role(MessageRole role) {
        return new Builder(role);
    }

    /**
     * 快捷构造仅含 content 的 system 消息。
     */
    public static ChatMessage system(String content) {
        return system().content(content).build();
    }

    /**
     * 快捷构造仅含 content 的 user 消息。
     */
    public static ChatMessage user(String content) {
        return user().content(content).build();
    }

    /**
     * 快捷构造仅含 content 的 assistant 消息。
     */
    public static ChatMessage assistant(String content) {
        return assistant().content(content).build();
    }

    /**
     * 单条 {@link ChatMessage} 链式组装器。
     */
    public static final class Builder {
        private String name;
        private String content;
        private String toolCallId;
        private final MessageRole role;
        private final List<ChatToolCall> toolCalls = new ArrayList<>();

        private Builder(MessageRole role) {
            this.role = role;
        }

        /**
         * 消息文本内容。
         */
        public Builder content(String content) {
            this.content = content;
            return this;
        }

        /**
         * tool 角色专用：关联的 tool_call.id。
         */
        public Builder toolCallId(String toolCallId) {
            this.toolCallId = toolCallId;
            return this;
        }

        /**
         * 可选参与者名（如 tool 函数名）。
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * 追加一条 tool_call；null 忽略。
         */
        public Builder toolCall(ChatToolCall toolCall) {
            if (Objects.nonNull(toolCall)) {
                this.toolCalls.add(toolCall);
            }

            return this;
        }

        /**
         * 嵌套组装一条 tool_call 后追加。
         */
        public Builder toolCall(Consumer<ChatToolCalls.Builder> configurer) {
            ChatToolCalls.Builder nested = ChatToolCalls.builder();
            configurer.accept(nested);
            return toolCall(nested.build());
        }

        /**
         * 批量追加 tool_calls；空列表忽略。
         */
        public Builder toolCalls(List<ChatToolCall> toolCalls) {
            if (Collections.isNotEmpty(toolCalls)) {
                this.toolCalls.addAll(toolCalls);
            }

            return this;
        }

        /**
         * @return 组装完成的消息（toolCalls 为不可变快照）
         */
        public ChatMessage build() {
            ChatMessage message = new ChatMessage();
            message.setRole(role);
            message.setName(name);
            message.setContent(content);
            message.setToolCallId(toolCallId);
            message.setToolCalls(List.copyOf(toolCalls));
            return message;
        }
    }
}
