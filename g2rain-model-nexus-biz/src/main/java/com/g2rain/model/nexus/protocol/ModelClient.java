package com.g2rain.model.nexus.protocol;


import com.g2rain.model.nexus.enums.ModelProtocol;
import com.g2rain.model.nexus.vo.ModelServiceDetailVO;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;

/**
 * 模型协议客户端策略接口。
 * <p>
 * 每种 {@link ModelProtocol} 对应一个实现类，负责把接入明细
 * {@link ModelServiceDetailVO} 转成可调用的
 * {@link ChatModel} / {@link EmbeddingModel} / {@link RerankModel}。
 * <p>
 * 由 {@link ModelClientRegistry} 收集全部实现并按协议路由。
 *
 * @author alpha
 * @see ModelClientRegistry
 * @see com.g2rain.model.nexus.protocol.openai.OpenAiModelClient
 * @see com.g2rain.model.nexus.protocol.anthropic.AnthropicModelClient
 * @see com.g2rain.model.nexus.protocol.gemini.GoogleGenAiModelClient
 * @since 2026/8/1
 */
public interface ModelClient {

    /**
     * 返回本实现所对接的协议枚举。
     * <p>
     * 必须与存库字段 {@code protocol}（枚举 {@link ModelProtocol#name()}）一致，
     * 供注册表做 key 映射。
     *
     * @return 协议枚举，不为 null
     */
    ModelProtocol protocol();

    /**
     * 基于接入明细创建对话模型客户端。
     * <p>
     * 通常会使用配置中的 baseUrl、apiKey、modelName、temperature、topP、maxTokens、timeoutMs 等字段。
     *
     * @param profile 已启用的模型服务明细（含目录元数据与调用参数）
     * @return 可执行 {@code call(Prompt)} 的 Spring AI ChatModel
     */
    ChatModel createChatModel(ModelServiceDetailVO profile);

    /**
     * 基于接入明细创建 Embedding 模型客户端。
     * <p>
     * 若当前协议不支持 Embedding，实现应抛出业务异常
     * （如 {@link com.g2rain.model.nexus.enums.NexusErrorCode#EMBEDDING_UNSUPPORTED}），
     * 而不是返回 null。
     *
     * @param profile 已启用的模型服务明细
     * @return 可执行 Embedding 调用的 Spring AI EmbeddingModel
     */
    EmbeddingModel createEmbeddingModel(ModelServiceDetailVO profile);

    /**
     * 基于接入明细创建 Rerank 模型客户端。
     * <p>
     * 若当前协议不支持 Rerank，实现应抛出业务异常
     * （如 {@link com.g2rain.model.nexus.enums.NexusErrorCode#RERANK_UNSUPPORTED}），
     * 而不是返回 null。
     *
     * @param profile 已启用的模型服务明细
     * @return 可执行 Rerank 调用的 {@link RerankModel}
     */
    RerankModel createRerankModel(ModelServiceDetailVO profile);
}
