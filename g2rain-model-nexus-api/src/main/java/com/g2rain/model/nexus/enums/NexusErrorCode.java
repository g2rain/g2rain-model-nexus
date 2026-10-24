package com.g2rain.model.nexus.enums;

import com.g2rain.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 模型网关业务错误码。
 * <p>
 * 实现平台 {@link ErrorCode}，用于协议不支持、Embedding / Rerank 未接入、
 * 工具不可在网关执行等场景的明确失败语义。
 *
 * @author alpha
 * @since 2026/7/20
 */
@Schema(description = "模型网关错误码")
public enum NexusErrorCode implements ErrorCode {

    @Schema(description = "模型配置未启用")
    MODEL_INACTIVE("model_nexus.40001", "模型配置未启用：{0:model}"),

    @Schema(description = "模型协议不受支持")
    PROTOCOL_UNSUPPORTED("model_nexus.40002", "模型协议不受支持：{0:protocol}"),

    @Schema(description = "协议不支持 Embedding")
    EMBEDDING_UNSUPPORTED("model_nexus.40003", "协议 [{0:protocol}] 不支持 Embedding"),

    @Schema(description = "Tool 不可在 model-nexus 执行")
    TOOL_EXECUTION_UNSUPPORTED("model_nexus.40004", "Tool [{0:tool}] 不可在 model-nexus 执行"),

    @Schema(description = "机构下模型名称已存在")
    MODEL_NAME_DUPLICATE("model_nexus.40005", "同一机构下模型名称已存在：{0:modelName}"),

    @Schema(description = "参数无效")
    PARAM_INVALID("model_nexus.40006", "参数无效：{0:param}"),

    @Schema(description = "协议不支持 Rerank")
    RERANK_UNSUPPORTED("model_nexus.40007", "协议 [{0:protocol}] 不支持 Rerank"),

    @Schema(description = "模型目录仍被配置引用，不可删除")
    CATALOG_IN_USE("model_nexus.40008", "模型目录仍被模型配置引用，不可删除：{0:catalogId}"),

    @Schema(description = "非运营公司不可删除模型广场目录")
    CATALOG_MARKET_DELETE_DENIED("model_nexus.40009", "非运营公司不可删除模型广场目录"),

    @Schema(description = "默认模型配置不可删除或禁用")
    DEFAULT_PROFILE_LOCKED("model_nexus.40010", "默认模型配置不可删除或禁用，请先切换默认：{0:id}");

    private final String code;

    private final String messageTemplate;

    NexusErrorCode(String code, String messageTemplate) {
        this.code = code;
        this.messageTemplate = messageTemplate;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String messageTemplate() {
        return messageTemplate;
    }
}
