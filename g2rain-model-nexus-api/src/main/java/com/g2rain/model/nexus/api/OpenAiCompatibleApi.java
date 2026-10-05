package com.g2rain.model.nexus.api;


import com.g2rain.model.nexus.dto.ChatCompletionRequest;
import com.g2rain.model.nexus.dto.EmbeddingsRequest;
import com.g2rain.model.nexus.vo.ChatCompletionResponse;
import com.g2rain.model.nexus.vo.EmbeddingsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * OpenAI 兼容 HTTP API 契约。
 * <p>
 * 对外暴露与 OpenAI 相近的路径语义：
 * <ul>
 *   <li>{@code POST /chat/completions}：对话补全</li>
 *   <li>{@code POST /embeddings}：文本向量化</li>
 * </ul>
 * 具体路径前缀（如 {@code /v1}）由实现侧 Controller 声明；
 * 本接口只约定方法映射与入参/出参类型。
 *
 * @author alpha
 * @since 2026/8/1
 */
@Tag(name = "OpenAI Compatible", description = "OpenAI 兼容模型调用接口")
public interface OpenAiCompatibleApi {

    /**
     * 对话补全：按请求 {@code model} 路由到启用中的 LLM 接入配置并调用厂商。
     *
     * @param request OpenAI 风格 chat.completions 请求体
     * @return OpenAI 风格 chat.completion 响应
     */
    @PostMapping("/chat/completions")
    @Operation(summary = "Chat Completions", description = "根据 model 路由厂商对话模型")
    ChatCompletionResponse chatCompletions(@RequestBody @Validated ChatCompletionRequest request);

    /**
     * 文本向量化：按请求 {@code model} 路由到启用中的 VECTOR 接入配置并调用厂商。
     *
     * @param request OpenAI 风格 embeddings 请求体
     * @return OpenAI 风格 embeddings 响应
     */
    @PostMapping("/embeddings")
    @Operation(summary = "Embeddings", description = "根据 model 路由厂商 embedding 模型")
    EmbeddingsResponse embeddings(@RequestBody @Validated EmbeddingsRequest request);
}
