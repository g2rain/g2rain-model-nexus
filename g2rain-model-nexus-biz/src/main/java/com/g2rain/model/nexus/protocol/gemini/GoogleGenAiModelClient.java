package com.g2rain.model.nexus.protocol.gemini;


import com.g2rain.common.exception.BusinessException;
import com.g2rain.model.nexus.enums.ModelProtocol;
import com.g2rain.model.nexus.enums.NexusErrorCode;
import com.g2rain.model.nexus.protocol.ModelClient;
import com.g2rain.model.nexus.protocol.ModelEndpoint;
import com.g2rain.model.nexus.protocol.ModelProtocolSupport;
import com.g2rain.model.nexus.protocol.RerankModel;
import com.google.genai.Client;
import com.google.genai.types.HttpOptions;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Google Gemini（GenAI）协议客户端（{@link ModelProtocol#GEMINI}）。
 * <p>
 * 使用 Google GenAI {@link Client} + Spring AI {@link GoogleGenAiChatModel}：
 * 采样参数写入 {@link GoogleGenAiChatOptions}，鉴权与 HTTP（超时 / 可选 baseUrl）
 * 写入 {@link HttpOptions}。
 * <p>
 * 当前未接入 Embedding / Rerank；调用对应 create 方法会抛出业务异常。
 *
 * @author alpha
 * @since 2026/8/1
 */
@Component
public class GoogleGenAiModelClient implements ModelClient {

    /**
     * 返回本实现所对接的协议枚举。
     * <p>
     * 固定为 {@link ModelProtocol#GEMINI}，须与存库 {@code protocol} 一致，
     * 供注册表做 Map key 映射。
     * </p>
     *
     * @return {@link ModelProtocol#GEMINI}，不为 null
     */
    @Override
    public ModelProtocol protocol() {
        // 注册表 Map key，对应存库 protocol=GEMINI
        return ModelProtocol.GEMINI;
    }

    /**
     * 基于接入明细创建对话模型客户端。
     * <p>
     * 采样参数写入 {@link GoogleGenAiChatOptions}（最大输出对应 {@code maxOutputTokens}）；
     * 鉴权与 HTTP（超时毫秒 / 可选 baseUrl）写入 {@link HttpOptions}，再组装
     * {@link GoogleGenAiChatModel}。{@code HttpOptions.extraBody} 写入
     * {@code enable_thinking=false} 供兼容网关使用，官方 Gemini 通常忽略。
     * </p>
     *
     * @param endpoint 已启用的模型接入端点（含调用参数）
     * @return 可执行 {@code call(Prompt)} 的 Spring AI ChatModel
     */
    @Override
    public ChatModel createChatModel(ModelEndpoint endpoint) {

        // ---------- 1. 对话采样参数（注意 Gemini 字段名是 maxOutputTokens） ----------
        GoogleGenAiChatOptions options = GoogleGenAiChatOptions.builder()
            // 模型名，如 Gemini-2.0-flash
            .model(endpoint.modelName())
            // 温度；null 不覆盖
            .temperature(ModelProtocolSupport.toDouble(endpoint.options().temperature()))
            // top_p；null 不覆盖
            .topP(ModelProtocolSupport.toDouble(endpoint.options().topP()))
            // 最大输出 token（对应配置表 maxTokens）
            .maxOutputTokens(endpoint.options().maxTokens())
            .build();

        // ---------- 2. 构建 Google GenAI Client（鉴权 + HTTP 选项） ----------
        // apiKey 必填：官方 Gemini / 兼容代理均需
        Client.Builder clientBuilder = Client.builder().apiKey(endpoint.apiKey());
        // HTTP 选项：超时用毫秒整型（与 OpenAI Duration 路径不同）
        HttpOptions.Builder httpOptions = HttpOptions.builder()
            .timeout(ModelProtocolSupport.resolveTimeoutMs(endpoint.options().timeoutMs()))
            // 若配置了自定义 baseUrl（代理/私有化），写入 HttpOptions；空白则走官方默认域名
            .baseUrl(ModelProtocolSupport.normalizeBaseUrl(endpoint.baseUrl()))
            // 兼容网关：关闭 thinking；Gemini 通常忽略该扩展字段
            .extraBody(Map.of("chat_template_kwargs", Map.of("enable_thinking", false)));
        // 把 HTTP 选项挂到 Client
        clientBuilder.httpOptions(httpOptions.build());

        // ---------- 3. 组装 Spring AI GoogleGenAiChatModel ----------
        return GoogleGenAiChatModel.builder()
            // build() 得到最终 GenAI Client 实例
            .genAiClient(clientBuilder.build())
            .options(options)
            .build();
    }

    /**
     * 基于接入明细创建 Embedding 模型客户端。
     * <p>
     * Gemini Embedding 尚未实现，固定抛出
     * {@link NexusErrorCode#EMBEDDING_UNSUPPORTED}，避免返回 null 导致 NPE。
     * </p>
     *
     * @param endpoint 已启用的模型接入端点
     * @return 不会正常返回；始终抛业务异常
     * @throws BusinessException 协议不支持 Embedding
     */
    @Override
    public EmbeddingModel createEmbeddingModel(ModelEndpoint endpoint) {
        // 尚未实现 Gemini Embedding：明确失败，避免返回 null 导致 NPE
        throw new BusinessException(
            NexusErrorCode.EMBEDDING_UNSUPPORTED,
            protocol().name()
        );
    }

    /**
     * 基于接入明细创建 Rerank 模型客户端。
     * <p>
     * Gemini Rerank 尚未实现，固定抛出
     * {@link NexusErrorCode#RERANK_UNSUPPORTED}，避免返回 null 导致 NPE。
     * </p>
     *
     * @param endpoint 已启用的模型接入端点
     * @return 不会正常返回；始终抛业务异常
     * @throws BusinessException 协议不支持 Rerank
     */
    @Override
    public RerankModel createRerankModel(ModelEndpoint endpoint) {
        throw new BusinessException(
            NexusErrorCode.RERANK_UNSUPPORTED,
            protocol().name()
        );
    }
}
