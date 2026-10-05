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

/**
 * 模型用量记录查询DTO
 * 表名: model_usage_record
 *
 * @author G2rain Generator
 */
@Setter
@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "模型用量记录 DTO")
public class ModelUsageRecordDto extends BaseDto {

    /**
     * 机构标识
     */
    @NotNull(groups = CreateGroup.class)
    @Schema(description = "机构标识（新增必填）")
    private Long organId;

    /**
     * 模型服务配置标识
     */
    @NotNull(groups = CreateGroup.class)
    @Schema(description = "模型服务配置标识（新增必填）")
    private Long profileId;

    /**
     * 模型服务目录标识
     */
    @NotNull(groups = CreateGroup.class)
    @Schema(description = "模型服务目录标识（新增必填）")
    private Long catalogId;

    /**
     * 模型协议类型
     */
    @NotBlank(groups = CreateGroup.class)
    @Size(max = 64)
    @Schema(description = "模型协议类型（新增必填）", maxLength = 64)
    private String protocol;

    /**
     * 模型供应商
     */
    @NotBlank(groups = CreateGroup.class)
    @Size(max = 64)
    @Schema(description = "模型供应商（新增必填）", maxLength = 64)
    private String provider;

    /**
     * 调用模型标识
     */
    @NotBlank(groups = CreateGroup.class)
    @Size(max = 128)
    @Schema(description = "调用模型标识（新增必填）", maxLength = 128)
    private String modelName;

    /**
     * 输入 token 数
     */
    @NotNull(groups = CreateGroup.class)
    @Schema(description = "输入 token 数（新增必填）")
    private Integer promptTokens;

    /**
     * 输出 token 数
     */
    @NotNull(groups = CreateGroup.class)
    @Schema(description = "输出 token 数（新增必填）")
    private Integer completionTokens;

    /**
     * 总 token 数
     */
    @NotNull(groups = CreateGroup.class)
    @Schema(description = "总 token 数（新增必填）")
    private Integer totalTokens;
}
