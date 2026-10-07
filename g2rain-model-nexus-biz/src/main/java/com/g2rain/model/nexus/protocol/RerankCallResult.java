package com.g2rain.model.nexus.protocol;


import com.g2rain.model.nexus.schema.RerankResult;
import org.springframework.ai.chat.metadata.Usage;

import java.util.List;

/**
 * 协议层 Rerank 调用结果：厂商结果 + 可选 usage，由 Service 组装对外 VO。
 *
 * @param id      厂商请求 id，可为 null
 * @param results 重排序结果，不为 null
 * @param usage   token 用量，可为 null
 * @author alpha
 * @since 2026/8/1
 */
public record RerankCallResult(String id, List<RerankResult> results, Usage usage) {

}
