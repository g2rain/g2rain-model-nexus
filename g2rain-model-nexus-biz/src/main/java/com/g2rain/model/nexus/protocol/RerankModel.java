package com.g2rain.model.nexus.protocol;


import com.g2rain.model.nexus.dto.RerankRequest;

/**
 * Rerank 模型客户端抽象。
 * <p>
 * Spring AI 2.x 无官方 Rerank 一等抽象；本接口对齐 {@code EmbeddingModel}
 * 在协议层的角色，由各 {@link ModelClient} 按协议创建实现。
 *
 * @author alpha
 * @see ModelClient
 * @since 2026/8/1
 */
public interface RerankModel {

    /**
     * 执行文档重排序。
     *
     * @param request 已校验的 rerank 请求（model 可由实现覆盖为接入配置中的 modelName）
     * @return 协议层结果（results + 可选 usage），由 Service 组装对外 VO
     */
    RerankCallResult call(RerankRequest request);
}
