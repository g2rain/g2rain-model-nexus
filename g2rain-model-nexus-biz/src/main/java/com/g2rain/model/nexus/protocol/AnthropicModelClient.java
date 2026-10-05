package com.g2rain.model.nexus.protocol;


import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.Model;
import com.g2rain.common.exception.BusinessException;
import com.g2rain.model.nexus.enums.ModelProtocol;
import com.g2rain.model.nexus.enums.NexusErrorCode;
import com.g2rain.model.nexus.vo.ModelServiceDetailVO;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.AnthropicSetup;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Component;

/**
 * Anthropic Messages 协议客户端（{@link ModelProtocol#ANTHROPIC}）。
 * <p>
 * 将接入明细中的 baseUrl / apiKey / modelName / 采样参数映射为
 * {@link AnthropicChatOptions}，经 {@link AnthropicSetup} 创建同步
 * {@link AnthropicClient}，再组装 Spring AI {@link AnthropicChatModel}。
 * <p>
 * 当前未接入 Embedding；调用 {@link #createEmbeddingModel} 会抛出业务异常。
 *
 * @author alpha
 * @since 2026/8/1
 */
@Component
public class AnthropicModelClient implements ModelClient {

    /**
     * 返回本实现所对接的协议枚举。
     * <p>
     * 固定为 {@link ModelProtocol#ANTHROPIC}，须与存库 {@code protocol} 一致，
     * 供注册表做 Map key 映射。
     * </p>
     *
     * @return {@link ModelProtocol#ANTHROPIC}，不为 null
     */
    @Override
    public ModelProtocol protocol() {
        // 注册表 Map key，对应存库 protocol=ANTHROPIC
        return ModelProtocol.ANTHROPIC;
    }

    /**
     * 基于接入明细创建对话模型客户端。
     * <p>
     * 将 baseUrl / apiKey / 采样参数映射为 {@link AnthropicChatOptions}；
     * model 字段通过 Anthropic SDK {@link Model#of(String)} 包装配置中的模型名，
     * 再经 {@link AnthropicSetup} 创建同步客户端并组装 {@link AnthropicChatModel}。
     * 本协议无 OpenAI 风格 {@code extraBody}；关 thinking 应使用 Options 自带的 thinking API。
     * </p>
     *
     * @param profile 已启用的模型服务明细（含目录元数据与调用参数）
     * @return 可执行 {@code call(Prompt)} 的 Spring AI ChatModel
     */
    @Override
    public ChatModel createChatModel(ModelServiceDetailVO profile) {
        // ---------- 1. 接入配置 -> AnthropicChatOptions ----------
        AnthropicChatOptions options = AnthropicChatOptions.builder()
            // Anthropic API 或兼容代理的根地址
            .baseUrl(ModelProtocolSupport.normalizeBaseUrl(profile.getBaseUrl()))
            // x-api-key
            .apiKey(profile.getApiKey())
            // Anthropic SDK 要求 Model 类型，用 of(String) 包装配置中的模型名
            .model(Model.of(profile.getModelName()))
            // 兼容网关：关闭 thinking
            .thinkingDisabled()
            // 采样温度；null 不覆盖默认
            .temperature(ModelProtocolSupport.toDouble(profile.getTemperature()))
            // top_p；null 不覆盖默认
            .topP(ModelProtocolSupport.toDouble(profile.getTopP()))
            // 最大输出 token（Anthropic 侧必填语义较强，由配置给出）
            .maxTokens(profile.getMaxTokens())
            // 请求超时
            .timeout(ModelProtocolSupport.resolveTimeout(profile.getTimeoutMs()))
            .build();

        // ---------- 2. 创建 Anthropic 同步 HTTP 客户端 ----------
        AnthropicClient client = AnthropicSetup.setupSyncClient(
            // 基址
            options.getBaseUrl(),
            // API Key
            options.getApiKey(),
            // 超时
            options.getTimeout(),
            // 重试次数
            options.getMaxRetries(),
            // 代理
            options.getProxy(),
            // 自定义头
            options.getCustomHeaders(),
            // 不做观测
            ObservationRegistry.NOOP,
            // 无额外定制
            null
        );

        // ---------- 3. 包装为 Spring AI AnthropicChatModel ----------
        return AnthropicChatModel.builder()
            .anthropicClient(client)
            .options(options)
            .build();
    }

    /**
     * 基于接入明细创建 Embedding 模型客户端。
     * <p>
     * Anthropic 路径尚未接入 Embedding，固定抛出
     * {@link NexusErrorCode#EMBEDDING_UNSUPPORTED}，避免返回 null 导致 NPE。
     * </p>
     *
     * @param profile 已启用的模型服务明细
     * @return 不会正常返回；始终抛业务异常
     * @throws BusinessException 协议不支持 Embedding
     */
    @Override
    public EmbeddingModel createEmbeddingModel(ModelServiceDetailVO profile) {
        // 本协议未接入 Embedding：抛业务异常，由网关返回明确错误给调用方
        // 第二个参数填入协议名，便于错误文案展示
        throw new BusinessException(
            NexusErrorCode.EMBEDDING_UNSUPPORTED,
            protocol().name()
        );
    }
}
