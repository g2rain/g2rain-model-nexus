package com.g2rain.model.nexus.service;


import com.g2rain.model.nexus.dto.ChatCompletionRequest;
import com.g2rain.model.nexus.dto.EmbeddingsRequest;
import com.g2rain.model.nexus.dto.RerankRequest;
import com.g2rain.model.nexus.vo.ChatCompletionResponse;
import com.g2rain.model.nexus.vo.EmbeddingsResponse;
import com.g2rain.model.nexus.vo.RerankResponse;

/**
 * OpenAI 兼容模型调用服务。
 * <p>
 * 负责按 {@code model} 解析启用中的接入明细、按协议创建客户端、
 * 执行 Chat / Embedding / Rerank，并转换为 OpenAI 风格响应。
 *
 * @author alpha
 * @see com.g2rain.model.nexus.service.impl.OpenAiCompatibleServiceImpl
 * @since 2026/8/1
 */
public interface OpenAiCompatibleService {

    /**
     * 执行对话补全。
     *
     * @param request OpenAI 风格 chat.completions 请求
     * @return OpenAI 风格 chat.completion 响应
     */
    ChatCompletionResponse chat(ChatCompletionRequest request);

    /**
     * 执行文本向量化。
     *
     * @param request OpenAI 风格 embeddings 请求
     * @return OpenAI 风格 embeddings 响应
     */
    EmbeddingsResponse embed(EmbeddingsRequest request);

    /**
     * 执行文档重排序。
     *
     * @param request OpenAI 兼容风格 rerank 请求
     * @return OpenAI 兼容风格 rerank 响应
     */
    RerankResponse rerank(RerankRequest request);
}
