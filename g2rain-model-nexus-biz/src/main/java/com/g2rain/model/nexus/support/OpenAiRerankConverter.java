package com.g2rain.model.nexus.support;


import com.g2rain.common.utils.Collections;
import com.g2rain.model.nexus.dto.RerankRequest;
import com.g2rain.model.nexus.protocol.RerankCallResult;
import com.g2rain.model.nexus.protocol.openai.OpenAiRerankWireRequest;
import com.g2rain.model.nexus.protocol.openai.OpenAiRerankWireResponse;
import com.g2rain.model.nexus.schema.RerankResult;
import com.g2rain.model.nexus.schema.RerankUsage;
import com.g2rain.model.nexus.vo.RerankResponse;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.metadata.Usage;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * OpenAI 兼容 Rerank：网关入站请求 / 厂商 wire / 对外 VO 互转。
 *
 * @author alpha
 * @since 2026/8/1
 */
public final class OpenAiRerankConverter {

    private OpenAiRerankConverter() {

    }

    /**
     * 入站请求 → 厂商 wire 请求；{@code model} 使用接入配置中的真实模型名。
     */
    public static OpenAiRerankWireRequest toWireRequest(RerankRequest request, String modelName) {
        Integer topN = request.getTopN();
        if (Objects.nonNull(topN) && topN <= 0) {
            topN = null;
        }

        return new OpenAiRerankWireRequest(
            modelName,
            request.getQuery(),
            request.getDocuments(),
            topN,
            request.getReturnDocuments()
        );
    }

    /**
     * 厂商 wire 响应 → 协议层调用结果。
     */
    public static RerankCallResult fromWireResponse(OpenAiRerankWireResponse wire) {
        if (Objects.isNull(wire)) {
            return new RerankCallResult(null, List.of(), null);
        }

        List<RerankResult> results = new ArrayList<>();
        if (Collections.isNotEmpty(wire.results())) {
            for (OpenAiRerankWireResponse.Result item : wire.results()) {
                if (Objects.isNull(item)) {
                    continue;
                }

                RerankResult result = new RerankResult();
                result.setIndex(item.index());
                result.setRelevanceScore(item.relevanceScore());
                result.setDocument(resolveDocumentText(item.document()));
                results.add(result);
            }
        }

        return new RerankCallResult(wire.id(), results, resolveUsage(wire));
    }

    /**
     * 协议层结果 → 对外 RerankResponse。
     */
    public static RerankResponse toResponse(RerankCallResult callResult, String model) {
        RerankResponse response = new RerankResponse();
        response.setId(callResult.id());
        response.setModel(model);
        response.setResults(callResult.results());
        Usage usage = callResult.usage();
        if (Objects.nonNull(usage)) {
            RerankUsage usageVo = new RerankUsage();
            usageVo.setPromptTokens(usage.getPromptTokens());
            usageVo.setTotalTokens(usage.getTotalTokens());
            response.setUsage(usageVo);
        }

        return response;
    }

    private static String resolveDocumentText(JsonNode document) {
        if (Objects.isNull(document) || document.isNull()) {
            return null;
        }

        if (document.isString()) {
            return document.asString();
        }

        JsonNode text = document.get("text");
        if (Objects.nonNull(text) && text.isString()) {
            return text.asString();
        }

        return document.toString();
    }

    private static Usage resolveUsage(OpenAiRerankWireResponse wire) {
        if (Objects.nonNull(wire.meta()) && Objects.nonNull(wire.meta().tokens())) {
            OpenAiRerankWireResponse.Tokens tokens = wire.meta().tokens();
            Integer prompt = tokens.inputTokens();
            Integer output = tokens.outputTokens();
            if (Objects.nonNull(prompt) || Objects.nonNull(output)) {
                int p = Objects.nonNull(prompt) ? prompt : 0;
                int o = Objects.nonNull(output) ? output : 0;
                return new DefaultUsage(p, o, p + o);
            }
        }

        OpenAiRerankWireResponse.TokenUsage tokenUsage = wire.usage();
        if (Objects.isNull(tokenUsage)) {
            return null;
        }

        Integer prompt = tokenUsage.promptTokens();
        Integer total = tokenUsage.totalTokens();
        if (Objects.isNull(total) && Objects.nonNull(prompt)) {
            total = prompt;
        }

        if (Objects.isNull(prompt) && Objects.isNull(total)) {
            return null;
        }

        int p = Objects.nonNull(prompt) ? prompt : 0;
        return new DefaultUsage(p, 0, total);
    }
}
