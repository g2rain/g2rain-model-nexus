package com.g2rain.model.nexus.support;

import com.g2rain.common.utils.Strings;
import com.g2rain.model.nexus.enums.ToolType;
import com.g2rain.model.nexus.schema.ChatToolCall;
import com.g2rain.model.nexus.schema.ChatToolCallFunction;
import org.springframework.ai.chat.messages.AssistantMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Client / Server Converter 共用的 tool_calls 与字符串工具。
 * <p>
 * 提供双向转换：
 * <ul>
 *   <li>Spring AI {@link AssistantMessage.ToolCall} ↔ OpenAI {@link ChatToolCall}</li>
 *   <li>null 安全字符串兜底</li>
 * </ul>
 * 本类为无状态工具，禁止实例化。
 *
 * @author alpha
 * @since 2026/8/1
 */
public final class OpenAiChatConvertSupport {

    /**
     * 禁止实例化。
     */
    private OpenAiChatConvertSupport() {

    }

    /**
     * Spring AI assistant toolCalls -> OpenAI tool_calls。
     * <p>
     * type / arguments 为空时分别回落为 {@code function} / {@code {}}。
     *
     * @param toolCalls Spring AI 工具调用列表
     * @return OpenAI tool_calls
     */
    public static List<ChatToolCall> fromAssistantToolCalls(List<AssistantMessage.ToolCall> toolCalls) {
        // 结果列表，容量与入参一致
        List<ChatToolCall> result = new ArrayList<>(toolCalls.size());
        // 逐条转换
        for (AssistantMessage.ToolCall toolCall : toolCalls) {
            // arguments：空白则用 {}，保证是合法 JSON 对象字符串
            String args = toolCall.arguments();
            args = Strings.isNotBlank(args) ? args : SupportConstants.EMPTY_JSON_OBJECT;

            // type：空白则默认 function
            String typeCode = toolCall.type();
            ToolType type = ToolType.ofCode(typeCode);
            if (Objects.isNull(type)) {
                type = SupportConstants.DEFAULT_TOOL_TYPE;
            }

            // 填充 function 子对象
            ChatToolCallFunction function = new ChatToolCallFunction();
            function.setName(toolCall.name());
            function.setArguments(args);
            // 填充外层 tool_call：id + type + function
            ChatToolCall dto = new ChatToolCall();
            dto.setId(toolCall.id());
            dto.setType(type);
            dto.setFunction(function);
            result.add(dto);
        }

        // 返回 OpenAI tool_calls
        return result;
    }

    /**
     * OpenAI tool_calls -> Spring AI {@link AssistantMessage.ToolCall}。
     * <p>
     * function 缺失或 name 空白的项跳过；id 仅做 null->空串，type/arguments 空白用协议默认值。
     *
     * @param toolCalls OpenAI tool_calls
     * @return Spring AI ToolCall 列表
     */
    public static List<AssistantMessage.ToolCall> toAssistantToolCalls(List<ChatToolCall> toolCalls) {
        // 结果列表
        List<AssistantMessage.ToolCall> result = new ArrayList<>(toolCalls.size());
        for (ChatToolCall toolCall : toolCalls) {
            // 取出 function；无 function 或无 name 则跳过本条
            ChatToolCallFunction function = toolCall.getFunction();
            if (Objects.isNull(function)) {
                continue;
            }

            // 函数名（必填）
            String name = function.getName();
            if (Strings.isBlank(name)) {
                continue;
            }

            // id：null 转空串（Spring AI ToolCall 要求非 null 字符串）
            String id = nullToEmpty(toolCall.getId());

            // type：空白默认 function
            ToolType toolType = toolCall.getType();
            String type = Objects.nonNull(toolType)
                ? toolType.code()
                : SupportConstants.DEFAULT_TOOL_TYPE.code();

            // arguments：空白默认 {}
            String args = function.getArguments();
            args = Strings.isNotBlank(args)
                ? args
                : SupportConstants.EMPTY_JSON_OBJECT;

            // 构造 Spring AI 不可变 ToolCall 记录并加入结果
            result.add(new AssistantMessage.ToolCall(
                id, type, name, args
            ));
        }

        // 返回可挂到 AssistantMessage 的 ToolCall 列表
        return result;
    }

    /**
     * null 安全转空串。
     *
     * @param value 可为 null 的字符串
     * @return 原值或 {@code ""}
     */
    public static String nullToEmpty(String value) {
        return Objects.toString(value, "");
    }
}
