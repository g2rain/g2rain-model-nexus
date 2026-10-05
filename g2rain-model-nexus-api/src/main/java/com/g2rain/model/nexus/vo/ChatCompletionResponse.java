package com.g2rain.model.nexus.vo;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.g2rain.model.nexus.schema.ChatCompletionChoice;
import com.g2rain.model.nexus.schema.ChatCompletionUsage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * OpenAI 风格 chat.completion 响应体。
 * <p>
 * 对应 {@code POST /v1/chat/completions} 成功返回：含 id / model / choices / usage。
 *
 * @author alpha
 * @since 2026/8/1
 */
@Setter
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Chat Completions 响应")
public class ChatCompletionResponse {

    /**
     * 补全 ID（厂商回填或本地生成）。
     */
    @Schema(description = "补全 ID")
    private String id;

    /**
     * 对象类型，固定 {@code chat.completion}。
     */
    @Schema(description = "对象类型, 固定 chat.completion")
    private String object;

    /**
     * 创建时间（Unix 秒）。
     */
    @Schema(description = "创建时间（Unix 秒）")
    private Long created;

    /**
     * 实际响应模型标识。
     */
    @Schema(description = "模型标识")
    private String model;

    /**
     * 候选结果列表（当前网关通常只填一条）。
     */
    @Schema(description = "候选结果列表")
    private List<ChatCompletionChoice> choices;

    /**
     * Token 用量统计。
     */
    @Schema(description = "Token 用量")
    private ChatCompletionUsage usage;
}
