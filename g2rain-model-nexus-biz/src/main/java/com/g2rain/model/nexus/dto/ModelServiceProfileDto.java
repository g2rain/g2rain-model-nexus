package com.g2rain.model.nexus.dto;

import com.g2rain.common.model.BaseDto;
import com.g2rain.common.validation.CreateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 模型服务配置查询DTO
 * 表名: model_service_profile
 *
 * @author G2rain Generator
 */
@Setter
@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "模型服务配置 DTO")
public class ModelServiceProfileDto extends BaseDto {

    /**
     * 机构标识
     */
    @NotNull(groups = CreateGroup.class)
    @Schema(description = "机构标识（新增必填）")
    private Long organId;

    /**
     * 模型服务目录标识（引用模型广场时必填；自建私有时由服务端写入）
     */
    @Schema(description = "模型服务目录标识")
    private Long catalogId;

    /**
     * 私有接入：模型名称（有值则走 CUSTOM 聚合保存）
     */
    @Size(max = 128)
    @Schema(description = "私有接入：模型名称")
    private String modelName;

    /**
     * 私有接入：模型类型
     */
    @Size(max = 32)
    @Schema(description = "私有接入：模型类型")
    private String modelType;

    /**
     * 私有接入：供应商
     */
    @Size(max = 64)
    @Schema(description = "私有接入：供应商")
    private String provider;

    /**
     * 私有接入：协议
     */
    @Size(max = 64)
    @Schema(description = "私有接入：协议")
    private String protocol;

    /**
     * 私有接入：服务地址
     */
    @Size(max = 255)
    @Schema(description = "私有接入：服务地址")
    private String baseUrl;

    /**
     * 私有接入：访问密钥（留空不修改）
     */
    @Size(max = 512)
    @Schema(description = "私有接入：访问密钥")
    private String apiKey;

    /**
     * 采样温度
     */
    @Schema(description = "采样温度")
    private BigDecimal temperature;

    /**
     * Top P 采样
     */
    @Schema(description = "Top P 采样")
    private BigDecimal topP;

    /**
     * 最大输出 token 数
     */
    @Schema(description = "最大输出 token 数")
    private Integer maxTokens;

    /**
     * 请求超时毫秒数
     */
    @Schema(description = "请求超时毫秒数")
    private Integer timeoutMs;

    /**
     * 状态（save 忽略；新增由服务端写 ACTIVE，变更走 updateStatus）
     */
    @Size(max = 32)
    @Schema(description = "状态[ACTIVE:启用, INACTIVE:禁用]（save 忽略）", maxLength = 32)
    private String status;

    /**
     * 模型说明
     */
    @Size(max = 500)
    @Schema(description = "模型说明", maxLength = 500)
    private String description;
}
