package com.g2rain.model.nexus.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.g2rain.model.nexus.schema.ChatMessage;
import com.g2rain.model.nexus.schema.ChatTool;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * OpenAI 风格 Chat Completions 请求体。
 * <p>
 * 对应 {@code POST /v1/chat/completions}：按 {@code model} 路由接入配置，
 * {@code messages} 为多轮对话上下文，可选 {@code tools} 供模型发起工具调用。
 *
 * @author alpha
 * @since 2026/8/1
 */
@Setter
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Chat Completions 请求")
public class ChatCompletionRequest {

    /**
     * 模型标识（对应目录 model_name）。
     */
    @Schema(description = "模型标识")
    private String model;

    /**
     * 对话消息列表；至少一条。
     */
    @Valid
    @NotEmpty
    @Schema(description = "对话消息列表")
    private List<ChatMessage> messages;

    /**
     * 可供模型选择的 tools；为空则纯文本对话。
     */
    @Valid
    @Schema(description = "可供模型选择的 tools；为空则纯文本对话")
    private List<ChatTool> tools;
}
