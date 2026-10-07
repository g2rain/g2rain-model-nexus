package com.g2rain.model.nexus.dto;

import com.g2rain.common.model.BaseSelectListDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * 模型服务目录查询入参DTO
 * 用于ModelServiceCatalogDao.selectList方法的条件筛选
 * 表名: model_service_catalog
 *
 * @author G2rain Generator
 */
@Setter
@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "模型服务目录查询入参 DTO")
public class ModelServiceCatalogSelectDto extends BaseSelectListDto {

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
     * 访问密钥
     */
    @Schema(description = "访问密钥")
    private String apiKey;

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
}
