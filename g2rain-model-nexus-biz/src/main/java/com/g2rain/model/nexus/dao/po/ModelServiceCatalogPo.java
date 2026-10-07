package com.g2rain.model.nexus.dao.po;

import com.g2rain.common.model.BasePo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * 模型服务目录返回Po
 * 关联表名: model_service_catalog
 * 功能：封装实体数据，继承BasePo复用基础字段逻辑
 *
 * @author G2rain Generator
 */
@Setter
@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ModelServiceCatalogPo extends BasePo {

    /**
     * 模型调用标识
     */
    private String modelName;

    /**
     * 模型类型[LLM,VECTOR,RERANK]
     */
    private String modelType;

    /**
     * 资源类型[MARKET:模型广场资源,CUSTOM:自定义资源]
     */
    private String resourceType;

    /**
     * 模型供应商
     */
    private String provider;

    /**
     * 模型协议类型
     */
    private String protocol;

    /**
     * 模型服务地址
     */
    private String baseUrl;

    /**
     * 访问密钥
     */
    private String apiKey;

    /**
     * 状态[ACTIVE:启用,INACTIVE:禁用]
     */
    private String status;

    /**
     * 说明
     */
    private String description;

    /**
     * 删除标识[0:未删除,1:已删除]
     */
    private Boolean deleteFlag;
}