package com.g2rain.model.nexus.dto;

import com.g2rain.common.model.BaseDto;
import com.g2rain.common.validation.CreateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 模型服务目录查询DTO
 * 表名: model_service_catalog
 *
 * @author G2rain Generator
 */
@Setter
@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "模型服务目录 DTO")
public class ModelServiceCatalogDto extends BaseDto {

    /**
     * 模型调用标识
     */
    @NotBlank(groups = CreateGroup.class)
    @Size(max = 128)
    @Schema(description = "模型调用标识（新增必填）", maxLength = 128)
    private String modelName;

    /**
     * 模型类型[LLM,VECTOR,RERANK]
     */
    @NotBlank(groups = CreateGroup.class)
    @Size(max = 32)
    @Schema(description = "模型类型[LLM,VECTOR,RERANK]（新增必填）", maxLength = 32)
    private String modelType;

    /**
     * 资源类型[MARKET:模型广场资源,CUSTOM:自定义资源]
     */
    @NotBlank(groups = CreateGroup.class)
    @Size(max = 32)
    @Schema(description = "资源类型[MARKET:模型广场资源,CUSTOM:自定义资源]（新增必填）", maxLength = 32)
    private String resourceType;

    /**
     * 模型供应商
     */
    @NotBlank(groups = CreateGroup.class)
    @Size(max = 64)
    @Schema(description = "模型供应商（新增必填）", maxLength = 64)
    private String provider;

    /**
     * 模型协议类型
     */
    @NotBlank(groups = CreateGroup.class)
    @Size(max = 64)
    @Schema(description = "模型协议类型（新增必填）", maxLength = 64)
    private String protocol;

    /**
     * 模型服务地址
     */
    @NotBlank(groups = CreateGroup.class)
    @Size(max = 255)
    @Schema(description = "模型服务地址（新增必填）", maxLength = 255)
    private String baseUrl;

    /**
     * 访问密钥
     */
    @Size(max = 512)
    @Schema(description = "访问密钥", maxLength = 512)
    private String apiKey;

    /**
     * 说明
     */
    @Size(max = 500)
    @Schema(description = "说明", maxLength = 500)
    private String description;
}
