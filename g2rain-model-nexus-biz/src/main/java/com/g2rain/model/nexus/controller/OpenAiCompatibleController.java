package com.g2rain.model.nexus.controller;


import com.g2rain.model.nexus.api.OpenAiCompatibleApi;
import com.g2rain.model.nexus.dto.ChatCompletionRequest;
import com.g2rain.model.nexus.dto.EmbeddingsRequest;
import com.g2rain.model.nexus.dto.RerankRequest;
import com.g2rain.model.nexus.service.OpenAiCompatibleService;
import com.g2rain.model.nexus.vo.ChatCompletionResponse;
import com.g2rain.model.nexus.vo.EmbeddingsResponse;
import com.g2rain.model.nexus.vo.RerankResponse;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * OpenAI 兼容接口的 HTTP 入口。
 * <p>
 * 挂载前缀 {@code /v1}，实现 {@link OpenAiCompatibleApi} 的方法映射，
 * 将请求原样转交给 {@link OpenAiCompatibleService}，不做业务编排。
 *
 * @author alpha
 * @see OpenAiCompatibleApi
 * @see OpenAiCompatibleService
 * @since 2026/8/1
 */
@RestController
@RequestMapping("/v1")
public class OpenAiCompatibleController implements OpenAiCompatibleApi {

    /**
     * OpenAI 兼容调用服务。
     */
    @Resource(name = "openAiCompatibleServiceImpl")
    private OpenAiCompatibleService openAiCompatibleService;

    /**
     * 对话补全：按请求 {@code model} 路由到启用中的 LLM 接入配置并调用厂商。
     * <p>
     * HTTP 入口仅转发到 {@link OpenAiCompatibleService#chat}，不做业务编排。
     * </p>
     *
     * @param request OpenAI 风格 chat.completions 请求体
     * @return OpenAI 风格 chat.completion 响应
     */
    @Override
    public ChatCompletionResponse chatCompletions(ChatCompletionRequest request) {
        return openAiCompatibleService.chat(request);
    }

    /**
     * 文本向量化：按请求 {@code model} 路由到启用中的 VECTOR 接入配置并调用厂商。
     * <p>
     * HTTP 入口仅转发到 {@link OpenAiCompatibleService#embed}，不做业务编排。
     * </p>
     *
     * @param request OpenAI 风格 embeddings 请求体
     * @return OpenAI 风格 embeddings 响应
     */
    @Override
    public EmbeddingsResponse embeddings(EmbeddingsRequest request) {
        return openAiCompatibleService.embed(request);
    }

    /**
     * 文档重排序：按请求 {@code model} 路由到启用中的 RERANK 接入配置并调用厂商。
     * <p>
     * HTTP 入口仅转发到 {@link OpenAiCompatibleService#rerank}，不做业务编排。
     * </p>
     *
     * @param request OpenAI 兼容风格 rerank 请求体
     * @return OpenAI 兼容风格 rerank 响应
     */
    @Override
    public RerankResponse rerank(RerankRequest request) {
        return openAiCompatibleService.rerank(request);
    }
}
