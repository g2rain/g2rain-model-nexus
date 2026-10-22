package com.g2rain.model.nexus.support;


import lombok.extern.slf4j.Slf4j;
import com.g2rain.common.json.JsonCodec;
import com.g2rain.common.json.JsonCodecFactory;
import com.g2rain.common.utils.Collections;
import com.g2rain.common.utils.Strings;
import com.g2rain.model.nexus.enums.MessageRole;
import com.g2rain.model.nexus.schema.ChatCompletionChoice;
import com.g2rain.model.nexus.schema.ChatMessage;
import com.g2rain.model.nexus.schema.ChatTool;
import com.g2rain.model.nexus.schema.ChatToolFunction;
import com.g2rain.model.nexus.vo.ChatCompletionResponse;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.tool.ToolCallback;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 网关调用方使用的 OpenAI Chat 转换器。
 * <p>
 * 负责：Spring AI {@link Prompt} -> 请求 messages/tools；响应 VO -> {@link ChatResponse} / 首条 message。
 * <p>
 * 本类为无状态工具类，禁止实例化。
 *
 * @author alpha
 * @since 2026/8/1
 */
@Slf4j
public final class OpenAiChatClientConverter {

    /**
     * JSON 编解码器，用于 schema 字符串与对象互转。
     */
    private static final JsonCodec JSON = JsonCodecFactory.instance();

    /**
     * 禁止实例化。
     */
    private OpenAiChatClientConverter() {

    }

    /**
     * 将 Spring AI {@link Prompt} 中的消息转为 OpenAI messages 列表。
     * <p>
     * {@link ToolResponseMessage} 会拆成多条 {@code role=tool} 的消息；
     * 普通消息按 MessageType 映射 role，assistant 若含 toolCalls 则一并转换。
     *
     * @param prompt Spring AI Prompt（读取 instructions）
     * @return OpenAI 风格消息列表
     */
    public static List<ChatMessage> fromPrompt(Prompt prompt) {
        // 用于收集转换后的 OpenAI messages
        List<ChatMessage> messages = new ArrayList<>();
        // 遍历 Prompt 中的每一条 Spring AI Message
        for (Message message : prompt.getInstructions()) {
            // 分支一：工具执行结果消息（Spring AI 用 ToolResponseMessage 承载）
            if (message instanceof ToolResponseMessage msg) {
                // 一条 ToolResponseMessage 内可包含多个工具回执，需拆成多条 role=tool
                for (ToolResponseMessage.ToolResponse rsp : msg.getResponses()) {
                    // 新建一条 OpenAI tool 消息
                    ChatMessage dto = new ChatMessage();
                    // OpenAI 约定：工具回执 role 固定为 tool
                    dto.setRole(MessageRole.TOOL);
                    // 工具输出内容；null 转空串，避免序列化出 null content
                    dto.setContent(OpenAiChatConvertSupport.nullToEmpty(rsp.responseData()));
                    // 对应此前 assistant tool_call 的 id，用于多轮对齐
                    dto.setToolCallId(rsp.id());
                    // 工具函数名（可选，但便于排查）
                    dto.setName(rsp.name());
                    // 追加到结果列表
                    messages.add(dto);
                }

                // 本条 Message 已处理完，进入下一条
                continue;
            }

            // 分支二：system / user / assistant 等普通消息
            ChatMessage dto = new ChatMessage();
            // role：Spring AI MessageType 值映射为 MessageRole；未知回落 USER
            MessageRole role = MessageRole.ofCode(message.getMessageType().getValue());
            dto.setRole(Objects.nonNull(role) ? role : MessageRole.USER);
            // 文本正文
            dto.setContent(message.getText());
            // 若是 assistant 且带有工具调用，需要额外转换 tool_calls 字段
            if (message instanceof AssistantMessage msg && msg.hasToolCalls()) {
                // Spring AI ToolCall 列表 -> OpenAI tool_calls
                var calls = OpenAiChatConvertSupport.fromAssistantToolCalls(
                    msg.getToolCalls()
                );
                dto.setToolCalls(calls);
            }
            // 普通消息追加到结果
            messages.add(dto);
        }

        // 返回完整 OpenAI messages
        return messages;
    }

    /**
     * 从 Prompt 的 {@link ToolCallingChatOptions} 提取 OpenAI tools[] 定义。
     * <p>
     * Prompt 为空、options 非 ToolCallingChatOptions、或 callbacks 为空时返回空列表。
     *
     * @param prompt 可能携带 toolCallbacks 的 Prompt
     * @return OpenAI tools 列表；无工具时为空列表
     */
    public static List<ChatTool> toolsFromPrompt(Prompt prompt) {
        // Prompt 本身为空，或 options 不是可挂 tools 的 ToolCallingChatOptions：无工具可提取
        if (Objects.isNull(prompt)) {
            return List.of();
        }

        if (!(prompt.getOptions() instanceof ToolCallingChatOptions options)) {
            return List.of();
        }

        // 取出挂在 options 上的 ToolCallback 列表
        List<ToolCallback> callbacks = options.getToolCallbacks();
        // callbacks 为空则同样返回空 tools
        if (Collections.isEmpty(callbacks)) {
            return List.of();
        }

        // 预分配容量，逐个把 ToolDefinition 映射成 OpenAI tools[].function
        List<ChatTool> tools = new ArrayList<>(callbacks.size());
        for (ToolCallback callback : callbacks) {
            // function 子对象：name / description / parameters
            ChatToolFunction function = new ChatToolFunction();
            // 工具函数名
            function.setName(callback.getToolDefinition().name());
            // 工具说明，供模型理解何时调用
            function.setDescription(callback.getToolDefinition().description());
            // 入参 JSON Schema：字符串解析为对象，便于序列化进 OpenAI 请求体
            function.setParameters(parseSchema(
                callback.getToolDefinition().inputSchema()
            ));

            // 外层 tool 对象：type 固定 function + function 体
            ChatTool tool = new ChatTool();
            tool.setType(SupportConstants.DEFAULT_TOOL_TYPE);
            tool.setFunction(function);
            tools.add(tool);
        }

        // 返回 OpenAI tools[]
        return tools;
    }

    /**
     * 将 OpenAI 聊天响应 VO 还原为简易 {@link ChatResponse}（供上游调用方回灌）。
     * <p>
     * 只取 choices 首条 message；无 tool_calls 时仅含文本 content。
     *
     * @param vo OpenAI 兼容响应
     * @return 仅含一个 Generation 的 ChatResponse
     */
    public static ChatResponse toChatResponse(ChatCompletionResponse vo) {
        // 取 choices[0].message；结构不完整时可能为 null
        ChatMessage message = firstMessage(vo);
        // message 为空则 content 置空串，避免 NPE
        String content = Objects.nonNull(message)
            ? OpenAiChatConvertSupport.nullToEmpty(message.getContent())
            : "";

        // 先用文本构建 AssistantMessage
        AssistantMessage.Builder<?> messageBuilder = AssistantMessage
            .builder()
            .content(content);

        // 若存在 tool_calls，一并还原到 AssistantMessage
        if (Objects.nonNull(message) && Collections.isNotEmpty(message.getToolCalls())) {
            var calls = OpenAiChatConvertSupport.toAssistantToolCalls(
                message.getToolCalls()
            );

            messageBuilder.toolCalls(calls);
        }

        // 包装成仅含一个 Generation 的 ChatResponse
        return ChatResponse.builder().generations(List.of(
            new Generation(messageBuilder.build())
        )).build();
    }

    /**
     * 取出响应中第一条 choice 的 message；结构不完整时返回 null。
     *
     * @param vo OpenAI 兼容响应
     * @return 首条 assistant/message，或 null
     */
    public static ChatMessage firstMessage(ChatCompletionResponse vo) {
        // vo 为空或 choices 为空：无法取 message
        if (Objects.isNull(vo)) {
            return null;
        }

        List<ChatCompletionChoice> choices = vo.getChoices();
        if (Collections.isEmpty(choices)) {
            return null;
        }

        // 取第一条 choice（OpenAI 常规场景 n=1）
        ChatCompletionChoice choice = choices.getFirst();
        // choice 元素本身为 null 的防御
        if (Objects.isNull(choice)) {
            return null;
        }

        // 返回该 choice 上的 message（仍可能为 null）
        return choice.getMessage();
    }

    /**
     * 将 inputSchema 字符串解析为可放入 parameters 的对象。
     * <p>
     * 空白则返回空 object schema；解析失败则保留原始字符串，避免因非法 JSON 中断整次转换。
     *
     * @param inputSchema JSON Schema 字符串
     * @return JsonNode 或原始字符串
     */
    private static Object parseSchema(String inputSchema) {
        // 未提供 schema：用空 object schema，避免 parameters 为 null
        if (Strings.isBlank(inputSchema)) {
            return JSON.str2obj(
                SupportConstants.EMPTY_OBJ_SCHEMA,
                JsonNode.class
            );
        }

        try {
            // 正常路径：解析为 JsonNode，后续序列化进 OpenAI 请求
            return JSON.str2obj(inputSchema, JsonNode.class);
        } catch (RuntimeException ex) {
            log.error("failed to parse schema", ex);
            // 非法 JSON：原样回传字符串，不因单条 schema 坏掉整次转换
            return inputSchema;
        }
    }
}
