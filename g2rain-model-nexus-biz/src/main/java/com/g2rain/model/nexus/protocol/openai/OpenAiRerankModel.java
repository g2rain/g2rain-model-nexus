package com.g2rain.model.nexus.protocol.openai;


import com.g2rain.common.exception.BusinessException;
import com.g2rain.common.exception.SystemErrorCode;
import com.g2rain.common.json.JsonCodec;
import com.g2rain.common.json.JsonCodecFactory;
import com.g2rain.model.nexus.dto.RerankRequest;
import com.g2rain.model.nexus.protocol.RerankCallResult;
import com.g2rain.model.nexus.protocol.RerankModel;
import com.g2rain.model.nexus.support.OpenAiRerankConverter;
import org.springframework.web.client.RestClient;

/**
 * OpenAI 兼容网关的 Rerank 客户端：仅负责 HTTP 调用，编解码与 VO 转换见
 * {@link OpenAiRerankConverter}。
 *
 * @author alpha
 * @since 2026/8/1
 */
public class OpenAiRerankModel implements RerankModel {

    private static final JsonCodec JSON = JsonCodecFactory.instance();

    private final RestClient restClient;

    private final String modelName;

    /**
     * @param restClient 已配置 baseUrl / 鉴权 / 超时的客户端
     * @param modelName  接入配置中的模型名
     */
    public OpenAiRerankModel(RestClient restClient, String modelName) {
        this.restClient = restClient;
        this.modelName = modelName;
    }

    /**
     * {@code POST /rerank} → wire 解码 → {@link RerankCallResult}。
     */
    @Override
    public RerankCallResult call(RerankRequest request) {
        OpenAiRerankWireRequest wireRequest = OpenAiRerankConverter.toWireRequest(request, modelName);
        String raw;
        try {
            raw = restClient.post()
                .uri("/rerank")
                .body(JSON.obj2str(wireRequest))
                .retrieve()
                .body(String.class);
        } catch (Exception ex) {
            throw new BusinessException(
                SystemErrorCode.REMOTE_SERVICE_ERROR,
                "openai-rerank",
                ex.getMessage()
            );
        }

        try {
            OpenAiRerankWireResponse wire = JSON.str2obj(raw, OpenAiRerankWireResponse.class);
            return OpenAiRerankConverter.fromWireResponse(wire);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(
                SystemErrorCode.REMOTE_SERVICE_ERROR,
                "openai-rerank",
                "invalid rerank response: " + ex.getMessage()
            );
        }
    }
}
