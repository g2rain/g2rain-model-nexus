package com.g2rain.model.nexus.protocol.openai;


import com.g2rain.model.nexus.enums.ModelProtocol;
import com.g2rain.model.nexus.protocol.ModelClient;
import com.g2rain.model.nexus.protocol.ModelClientRegistry;
import com.g2rain.model.nexus.protocol.ModelEndpoint;
import com.g2rain.model.nexus.protocol.ModelProtocolSupport;
import com.g2rain.model.nexus.protocol.RerankModel;
import com.openai.client.OpenAIClient;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.http.okhttp.OpenAiHttpClientBuilderCustomizer;
import org.springframework.ai.openai.setup.OpenAiSetup;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * OpenAI 兼容协议客户端（{@link ModelProtocol#OPENAI}）。
 * <p>
 * 同时支持官方 OpenAI 以及 DeepSeek 等 OpenAI API 兼容网关：
 * 只要提供正确的 {@code baseUrl} / {@code apiKey} / {@code modelName} 即可。
 * <p>
 * Chat / Embedding 基于 Spring AI OpenAI 适配与官方 Java SDK；
 * Rerank 因 SDK 无官方接口，走兼容网关 HTTP {@code POST /rerank}。
 *
 * @author alpha
 * @since 2026/8/1
 */
@Component
public class OpenAiModelClient implements ModelClient {

    /**
     * 给官方 SDK 的 OkHttp 客户端挂上报文轨迹拦截器。
     */
    private static final OpenAiHttpClientBuilderCustomizer TRACE = builder -> builder.interceptor(UpstreamTrace.INSTANCE);

    /**
     * 返回本实现所对接的协议枚举。
     * <p>
     * 固定为 {@link ModelProtocol#OPENAI}，须与存库 {@code protocol} 一致，
     * 供 {@link ModelClientRegistry} 做 Map key 映射。
     * </p>
     *
     * @return {@link ModelProtocol#OPENAI}，不为 null
     */
    @Override
    public ModelProtocol protocol() {
        // 注册表用该返回值作为 Map key，必须与存库 protocol=OPENAI 对齐
        return ModelProtocol.OPENAI;
    }

    /**
     * 基于接入明细创建对话模型客户端。
     * <p>
     * 将 baseUrl / apiKey / modelName / 采样参数映射为 {@link OpenAiChatOptions}，
     * 经 {@link OpenAiSetup} 创建同步 {@link OpenAIClient}，再组装 {@link OpenAiChatModel}。
     * {@code extraBody} 写入 {@code chat_template_kwargs.enable_thinking=false}，
     * 用于兼容网关关闭 thinking；原生 OpenAI 通常忽略该扩展字段。
     * </p>
     *
     * @param endpoint 已启用的模型接入端点（含调用参数）
     * @return 可执行 {@code call(Prompt)} 的 Spring AI ChatModel
     */
    @Override
    public ChatModel createChatModel(ModelEndpoint endpoint) {
        Duration timeout = ModelProtocolSupport.resolveTimeout(
            endpoint.options().timeoutMs()
        );

        // ---------- 1. 用接入配置组装 OpenAiChatOptions ----------
        OpenAiChatOptions options = OpenAiChatOptions.builder()
            // 服务根地址（已去尾斜杠）；兼容自建/DeepSeek 等网关
            .baseUrl(ModelProtocolSupport.normalizeBaseUrl(endpoint.baseUrl()))
            // 鉴权密钥
            .apiKey(endpoint.apiKey())
            // 实际调用的模型名（如 gpt-4o、deepseek-chat）
            .model(endpoint.modelName())
            // 兼容网关：关闭 thinking；原生 OpenAI通常忽略该扩展字段
            .extraBody(Map.of("chat_template_kwargs", Map.of("enable_thinking", false)))
            // 采样温度；配置为 null 则不覆盖 SDK 默认
            .temperature(ModelProtocolSupport.toDouble(endpoint.options().temperature()))
            // nucleus 采样 top_p；null 表示不覆盖
            .topP(ModelProtocolSupport.toDouble(endpoint.options().topP()))
            // 单次补全最大输出 token
            .maxTokens(endpoint.options().maxTokens())
            // HTTP 超时；非法值由 Support 兜底 60s
            .timeout(timeout)
            .build();

        // ---------- 2. 按 options 创建官方 OpenAI Java 同步客户端 ----------
        // 参数顺序与 OpenAiSetup.setupSyncClient 签名一致；未使用的能力传默认/空
        OpenAIClient client = OpenAiSetup.setupSyncClient(
            // 请求基址
            options.getBaseUrl(),
            // API Key
            options.getApiKey(),
            // 其它凭证（一般为空）
            options.getCredential(),
            // OpenAI Organization Id（可选）
            options.getOrganizationId(),
            // Azure/Foundry 专用版本号（非 Foundry 场景通常为空）
            options.getMicrosoftFoundryServiceVersion(),
            // 部署名（Foundry/Azure 场景）
            options.getDeploymentName(),
            // 是否 Microsoft Foundry
            options.isMicrosoftFoundry(),
            // 是否 GitHub Models
            options.isGitHubModels(),
            // 默认模型名（与 options.model 一致即可）
            endpoint.modelName(),
            // 超时
            timeout,
            // 失败重试次数
            options.getMaxRetries(),
            // 代理
            options.getProxy(),
            // 自定义请求头
            options.getCustomHeaders(),
            // 不做 Micrometer 观测
            ObservationRegistry.NOOP,
            // 不采集 Micrometer 指标
            null,
            // 报文轨迹：DEBUG 打印上游请求与响应，响应体不合规时附带原文
            List.of(TRACE)
        );

        // ---------- 3. 包装为 Spring AI ChatModel，供网关 call(Prompt) ----------
        return OpenAiChatModel.builder()
            .openAiClient(client)
            .options(options)
            .build();
    }

    /**
     * 基于接入明细创建 Embedding 模型客户端。
     * <p>
     * 将接入明细映射为 {@link OpenAiEmbeddingOptions}，再通过 {@link OpenAiSetup} 创建同步
     * {@link OpenAIClient}，最终组装 {@link OpenAiEmbeddingModel}。
     * </p>
     *
     * @param endpoint 已启用的模型接入端点
     * @return 可执行 Embedding 调用的 Spring AI EmbeddingModel
     */
    @Override
    public EmbeddingModel createEmbeddingModel(ModelEndpoint endpoint) {
        Duration timeout = ModelProtocolSupport.resolveTimeout(
            endpoint.options().timeoutMs()
        );

        // ---------- 1. Embedding 专用 options（无对话采样参数） ----------
        OpenAiEmbeddingOptions options = OpenAiEmbeddingOptions.builder()
            // 服务根地址
            .baseUrl(ModelProtocolSupport.normalizeBaseUrl(endpoint.baseUrl()))
            // 鉴权密钥
            .apiKey(endpoint.apiKey())
            // Embedding 模型名（如 text-embedding-3-small）
            .model(endpoint.modelName())
            // HTTP 超时
            .timeout(timeout)
            .build();

        // ---------- 2. 同样走 OpenAiSetup 创建同步客户端 ----------
        OpenAIClient client = OpenAiSetup.setupSyncClient(
            options.getBaseUrl(),
            options.getApiKey(),
            options.getCredential(),
            options.getOrganizationId(),
            options.getMicrosoftFoundryServiceVersion(),
            options.getDeploymentName(),
            options.isMicrosoftFoundry(),
            options.isGitHubModels(),
            endpoint.modelName(),
            timeout,
            options.getMaxRetries(),
            options.getProxy(),
            options.getCustomHeaders(),
            ObservationRegistry.NOOP,
            null,
            List.of(TRACE)
        );

        // ---------- 3. 包装为 Spring AI EmbeddingModel ----------
        return OpenAiEmbeddingModel.builder()
            .openAiClient(client)
            .options(options)
            .build();
    }

    /**
     * 基于接入明细创建 Rerank 模型客户端。
     * <p>
     * 使用与 Chat / Embedding 相同的 baseUrl / apiKey / modelName / timeout，
     * 组装 {@link OpenAiRerankModel} 调用兼容网关 {@code /rerank}。
     * </p>
     *
     * @param endpoint 已启用的模型接入端点
     * @return 可执行 Rerank 调用的 {@link RerankModel}
     */
    @Override
    public RerankModel createRerankModel(ModelEndpoint endpoint) {
        return new OpenAiRerankModel(
            ModelProtocolSupport.createBearerRestClient(
                ModelProtocolSupport.normalizeBaseUrl(endpoint.baseUrl()),
                endpoint.apiKey(),
                ModelProtocolSupport.resolveTimeout(endpoint.options().timeoutMs())
            ),
            endpoint.modelName()
        );
    }
}
