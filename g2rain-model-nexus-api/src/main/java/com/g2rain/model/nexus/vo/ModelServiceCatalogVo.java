package com.g2rain.model.nexus.vo;

import com.g2rain.common.json.AdminCompanyCondition;
import com.g2rain.common.json.ConditionalJsonIgnore;
import com.g2rain.common.model.BaseVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 模型服务目录返回VO
 * 关联表名: model_service_catalog
 * 功能：封装接口返回数据，继承BaseVo复用基础字段逻辑，隔离数据库实体与前端展示层
 *
 * @author G2rain Generator
 */
@Setter
@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "模型服务目录 VO")
public class ModelServiceCatalogVo extends BaseVo {

    /**
     * 模型调用标识
     */
    @Schema(description = "模型调用标识")
    private String modelName;

    /**
     * 模型类型[LLM,VECTOR,RERANK]
     */
    @Schema(description = "模型类型[LLM,VECTOR,RERANK]")
    private String modelType;

    /**
     * 资源类型[MARKET:模型广场资源,CUSTOM:自定义资源]
     */
    @Schema(description = "资源类型[MARKET:模型广场资源,CUSTOM:自定义资源]")
    private String resourceType;

    /**
     * 模型供应商
     */
    @Schema(description = "模型供应商")
    private String provider;

    /**
     * 模型协议类型
     */
    @Schema(description = "模型协议类型")
    private String protocol;

    /**
     * 模型服务地址
     */
    @Schema(description = "模型服务地址")
    private String baseUrl;

    /**
     * 是否已配置访问密钥（不回传明文）
     */
    @Schema(description = "是否已配置访问密钥")
    private Boolean apiKeyConfigured;

    /**
     * 状态[ACTIVE:启用,INACTIVE:禁用]
     */
    @Schema(description = "状态[ACTIVE:启用,INACTIVE:禁用]")
    private String status;

    /**
     * 说明
     */
    @Schema(description = "说明")
    private String description;

    /**
     * 删除标识[0:未删除,1:已删除]
     */
    @Schema(description = "删除标识（0 未删除，1 已删除）", example = "false")
    @ConditionalJsonIgnore(adminCompany = AdminCompanyCondition.TRUE)
    private Boolean deleteFlag;
}
