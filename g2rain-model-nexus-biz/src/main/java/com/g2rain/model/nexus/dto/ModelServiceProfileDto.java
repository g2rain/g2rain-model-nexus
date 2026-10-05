package com.g2rain.model.nexus.dto;

import com.g2rain.common.model.BaseDto;
import com.g2rain.common.validation.CreateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
     * 模型服务目录标识
     */
    @NotNull(groups = CreateGroup.class)
    @Schema(description = "模型服务目录标识（新增必填）")
    private Long catalogId;

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
     * 状态[ACTIVE:启用, INACTIVE:禁用]
     */
    @NotBlank(groups = CreateGroup.class)
    @Size(max = 32)
    @Schema(description = "状态[ACTIVE:启用, INACTIVE:禁用]（新增必填）", maxLength = 32)
    private String status;

    /**
     * 模型说明
     */
    @Size(max = 500)
    @Schema(description = "模型说明", maxLength = 500)
    private String description;
}
