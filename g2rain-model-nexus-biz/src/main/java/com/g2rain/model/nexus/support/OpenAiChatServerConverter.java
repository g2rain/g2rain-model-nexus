package com.g2rain.model.nexus.support;

import com.g2rain.common.json.JsonCodec;
import com.g2rain.common.json.JsonCodecFactory;
import com.g2rain.common.utils.Collections;
import com.g2rain.common.utils.Strings;
import com.g2rain.model.nexus.enums.MessageRole;
import com.g2rain.model.nexus.schema.ChatCompletionChoice;
import com.g2rain.model.nexus.schema.ChatCompletionUsage;
import com.g2rain.model.nexus.schema.ChatMessage;
import com.g2rain.model.nexus.schema.ChatTool;
import com.g2rain.model.nexus.schema.ChatToolFunction;
import com.g2rain.model.nexus.vo.ChatCompletionResponse;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tool.ToolCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * nexus 服务端使用的 OpenAI Chat 转换器。
 * <p>
 * 负责：请求 messages/tools -> Spring AI {@link Prompt} / {@link ToolCallback}；
 * {@link ChatResponse} -> OpenAI 响应 VO。
 * <p>
 * 本类为无状态工具类，禁止实例化。
 *
 * @author alpha
 * @since 2026/8/1
 */
public final class OpenAiChatServerConverter {

    /**
     * JSON 编解码器，用于 schema 对象与字符串互转。
     */
    private static final JsonCodec JSON = JsonCodecFactory.instance();

    /**
     * 禁止实例化。
     */
    private OpenAiChatServerConverter() {

    }

    /**
     * 将 OpenAI tools[] 转为 Spring AI {@link ToolCallback} 列表。
     * <p>
     * 使用 {@link SchemaToolCallback}：只广告 schema，不在本进程执行。
     * function 缺失或 name 空白的项会被跳过。
     *
     * @param tools OpenAI 请求中的 tools
     * @return ToolCallback 列表；入参为空时返回空列表
     */
    public static List<ToolCallback> toToolCallbacks(List<ChatTool> tools) {
        // 请求未带 tools：直接空列表
        if (Collections.isEmpty(tools)) {
            return List.of();
        }

        // 收集可下发给 ChatModel 的 ToolCallback
        List<ToolCallback> callbacks = new ArrayList<>(tools.size());
        for (ChatTool tool : tools) {
            // 取出 function 定义
            ChatToolFunction function = tool.getFunction();
            // function 缺失或没有函数名：无法构成合法工具，跳过
            if (Objects.isNull(function)) {
                continue;
            }

            // 函数名（必填）
            String name = function.getName();
            if (Strings.isBlank(name)) {
                continue;
            }

            // 原始描述（可能为空）
            String desc = function.getDescription();
            // 描述为空时用 name 兜底，避免给模型空 description
            String description = Strings.isNotBlank(desc) ? desc : name;
            // parameters 转成 ToolDefinition 需要的 schema 字符串
            String inputSchema = toSchemaStr(function.getParameters());
            // 构造「只声明 schema、不可在本进程执行」的回调
            callbacks.add(SchemaToolCallback.of(
                name, description, inputSchema
            ));
        }
        // 返回可供 Prompt options 挂载的回调列表
        return callbacks;
    }

    /**
     * 将 OpenAI messages 转为 Spring AI {@link Prompt}（仅含 instructions，不含 options）。
     *
     * @param messages OpenAI 风格消息列表
     * @return 仅含消息的 Prompt
     */
    public static Prompt toPrompt(List<ChatMessage> messages) {
        // Spring AI Prompt 的 instructions 列表
        List<Message> instructions = new ArrayList<>();
        // 逐条转换；单条消息通常对应 1 条 Message
        for (ChatMessage message : messages) {
            instructions.addAll(toMessages(message));
        }

        // 仅消息、不含 tools options 的 Prompt（tools 由调用方另行挂载）
        return new Prompt(instructions);
    }

    /**
     * 将 Spring AI {@link ChatResponse} 转为 OpenAI {@code chat.completion} 响应 VO。
     * <p>
     * 外壳字段（id / created / model / finish_reason）优先取 metadata，缺失再本地兜底；
     * 正文取首个 Generation 的 assistant 输出（含可选 tool_calls）。
     *
     * @param response Spring AI 对话响应
     * @param model    请求侧模型名，作为 metadata.model 缺失时的回退
     * @return OpenAI 兼容响应 VO
     */
    public static ChatCompletionResponse fromChatResponse(ChatResponse response, String model) {
        // 取出响应元数据（含 id / model / usage / 扩展 created 等）
        ChatResponseMetadata metadata = response.getMetadata();

        // ---------- 组装 OpenAI 响应外壳字段 ----------
        ChatCompletionResponse vo = new ChatCompletionResponse();
        // id：优先厂商返回，否则本地生成
        vo.setId(resolveId(metadata));
        // object：协议固定值 chat.completion
        vo.setObject(SupportConstants.OBJECT_CHAT_COMPLETION);
        // created：优先 metadata 扩展字段，否则本地 Unix 秒
        vo.setCreated(resolveCreated(metadata));
        // model：优先 metadata，否则用请求入参 model
        vo.setModel(resolveModel(metadata, model));

        // ---------- 组装 choices[0].message ----------
        ChatMessage message = new ChatMessage();
        // 补全结果角色固定为 assistant
        message.setRole(MessageRole.ASSISTANT);
        // 标记是否含 tool_calls，供 finish_reason 兜底推断
        boolean hasToolCalls = false;
        // Spring AI 取第一条 Generation 作为主结果
        Generation generation = response.getResult();
        if (Objects.nonNull(generation)) {
            // Generation 的输出即 AssistantMessage
            AssistantMessage output = generation.getOutput();
            // 助手文本内容（可能为空，例如纯 tool_calls 场景）
            message.setContent(output.getText());
            // 若模型要求调工具，转换 tool_calls
            if (output.hasToolCalls()) {
                var calls = OpenAiChatConvertSupport.fromAssistantToolCalls(
                    output.getToolCalls()
                );
                message.setToolCalls(calls);
                hasToolCalls = true;
            }
        }

        // ---------- 组装唯一 choice ----------
        ChatCompletionChoice choice = new ChatCompletionChoice();
        // 当前只暴露第一条结果，index 固定 0
        choice.setIndex(0);
        // 挂上 assistant message
        choice.setMessage(message);
        // finish_reason：优先 Generation metadata，否则按 hasToolCalls 推断
        choice.setFinishReason(resolveFinishReason(generation, hasToolCalls));
        // OpenAI 响应要求 choices 为数组
        vo.setChoices(List.of(choice));

        // ---------- 组装 usage ----------
        Usage usage = metadata.getUsage();
        ChatCompletionUsage usageVo = new ChatCompletionUsage();
        // 输入 token
        usageVo.setPromptTokens(usage.getPromptTokens());
        // 输出 token
        usageVo.setCompletionTokens(usage.getCompletionTokens());
        // 总计 token
        usageVo.setTotalTokens(usage.getTotalTokens());
        vo.setUsage(usageVo);
        // 返回完整 chat.completion VO
        return vo;
    }

    /**
     * 单条 OpenAI message -> 一条或多条 Spring AI {@link Message}。
     * <p>
     * role 按枚举匹配；未识别 role 一律按 user 处理。
     *
     * @param message OpenAI 消息
     * @return Spring AI 消息列表（通常 size=1）
     */
    private static List<Message> toMessages(ChatMessage message) {
        // 取出角色；null 时按 user 兜底，避免 NPE / 丢消息
        MessageRole role = message.getRole();
        if (Objects.isNull(role)) {
            return List.of(new UserMessage(OpenAiChatConvertSupport.nullToEmpty(message.getContent())));
        }

        return switch (role) {
            // system：系统提示
            case SYSTEM -> List.of(new SystemMessage(OpenAiChatConvertSupport.nullToEmpty(message.getContent())));
            // assistant：文本 + 可选 tool_calls
            case ASSISTANT -> {
                AssistantMessage.Builder<?> builder = AssistantMessage.builder()
                    .content(OpenAiChatConvertSupport.nullToEmpty(message.getContent()));
                // 上一轮模型发起的工具调用，需原样带回多轮上下文
                if (Collections.isNotEmpty(message.getToolCalls())) {
                    builder.toolCalls(OpenAiChatConvertSupport.toAssistantToolCalls(message.getToolCalls()));
                }
                yield List.of(builder.build());
            }
            // tool：工具执行结果回执（Spring AI 用 ToolResponseMessage）
            case TOOL -> {
                // name 缺失时用字面量 "tool" 兜底
                String name = message.getName();
                String toolName = Strings.isNotBlank(name) ? name : MessageRole.TOOL.code();
                yield List.of(ToolResponseMessage.builder()
                    .responses(List.of(new ToolResponseMessage.ToolResponse(
                        OpenAiChatConvertSupport.nullToEmpty(message.getToolCallId()),
                        toolName,
                        OpenAiChatConvertSupport.nullToEmpty(message.getContent()))))
                    .build());
            }
            // user：普通用户输入
            case USER -> List.of(new UserMessage(OpenAiChatConvertSupport.nullToEmpty(message.getContent())));
        };
    }

    /**
     * 将 tools[].function.parameters 转为 JSON Schema 字符串，供 ToolDefinition 使用。
     * <p>
     * null / 空白字符串 / 序列化失败均回落为 {@link SupportConstants#EMPTY_OBJ_SCHEMA}。
     *
     * @param parameters 可能是 String 或已反序列化对象
     * @return JSON Schema 字符串
     */
    private static String toSchemaStr(Object parameters) {
        // parameters 未传：空 object schema
        if (Objects.isNull(parameters)) {
            return SupportConstants.EMPTY_OBJ_SCHEMA;
        }

        // 已经是字符串：非空白直接用，空白则兜底
        if (parameters instanceof String text) {
            if (Strings.isNotBlank(text)) {
                return text;
            }

            return SupportConstants.EMPTY_OBJ_SCHEMA;
        }

        try {
            // Map / JsonNode 等对象：序列化为 JSON 字符串
            String json = JSON.obj2str(parameters);
            if (Strings.isNotBlank(json)) {
                return json;
            }

            // 序列化结果异常为空：兜底
            return SupportConstants.EMPTY_OBJ_SCHEMA;
        } catch (RuntimeException ex) {
            // 序列化失败：兜底空 schema，保证 ToolDefinition 仍可构建
            return SupportConstants.EMPTY_OBJ_SCHEMA;
        }
    }

    /**
     * 解析响应 id：优先 metadata.id，否则生成 {@code chatcmpl_} + UUID。
     *
     * @param metadata Spring AI 响应元数据
     * @return 补全 ID
     */
    private static String resolveId(ChatResponseMetadata metadata) {
        // 厂商（尤其 OpenAI）已回填 id：直接用
        if (Objects.nonNull(metadata) && Strings.isNotBlank(metadata.getId())) {
            return metadata.getId();
        }

        // 未回 id：自动生成, 对齐 OpenAI 形态
        return SupportConstants.ID_PREFIX + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 解析 created（Unix 秒）：优先 metadata 扩展字段 {@code created}，否则用本地时间。
     *
     * @param metadata Spring AI 响应元数据
     * @return Unix 秒时间戳
     */
    private static long resolveCreated(ChatResponseMetadata metadata) {
        if (Objects.nonNull(metadata)) {
            // Spring AI OpenAI 适配把 created 放在扩展 map，key 为 "created"
            Object created = metadata.get(SupportConstants.METADATA_CREATED);
            // 必须是正数 Number；0 视为无效（部分兼容网关不返回该字段）
            if (created instanceof Number number && number.longValue() > 0) {
                return number.longValue();
            }
        }

        // 兜底：当前本地时间的 Unix 秒
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 解析响应 model：优先 metadata.model，否则使用请求入参。
     *
     * @param metadata Spring AI 响应元数据
     * @param model    请求侧模型名
     * @return 模型标识
     */
    private static String resolveModel(ChatResponseMetadata metadata, String model) {
        // metadata 带了实际响应 model（可能与请求别名不同）：优先用
        if (Objects.nonNull(metadata) && Strings.isNotBlank(metadata.getModel())) {
            return metadata.getModel();
        }

        // 否则回退请求入参 model
        return model;
    }

    /**
     * 解析 finish_reason：优先 Generation metadata；否则按是否含 tool_calls 推断。
     *
     * @param generation   首个 Generation，可为 null
     * @param hasToolCalls 输出是否包含 tool_calls
     * @return OpenAI finish_reason 字符串
     */
    private static String resolveFinishReason(Generation generation, boolean hasToolCalls) {
        if (Objects.nonNull(generation)) {
            // 厂商通过 Generation metadata 回传的 finish_reason（stop/length/tool_calls 等）
            String finishReason = generation.getMetadata().getFinishReason();
            if (Strings.isNotBlank(finishReason)) {
                return finishReason;
            }
        }

        // metadata 没有：有工具调用则 tool_calls，否则按正常结束 stop
        return hasToolCalls
            ? SupportConstants.FINISH_REASON_TOOL_CALLS
            : SupportConstants.FINISH_REASON_STOP;
    }
}
