package com.g2rain.model.nexus.enums;


/**
 * 模型能力类型：对话（LLM）、向量（VECTOR）或重排序（RERANK）。
 * <p>
 * 用于按请求路径筛选启用中的接入明细
 * （chat 查 LLM，embeddings 查 VECTOR，rerank 查 RERANK）。
 *
 * @author alpha
 * @since 2026/8/1
 */
public enum ModelType {
    /**
     * 对话 / 补全模型。
     */
    LLM,

    /**
     * 文本向量化模型。
     */
    VECTOR,

    /**
     * 文档重排序模型。
     */
    RERANK
}
