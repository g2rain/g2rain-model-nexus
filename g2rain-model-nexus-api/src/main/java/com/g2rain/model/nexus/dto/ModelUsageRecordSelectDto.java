package com.g2rain.model.nexus.dto;

import com.g2rain.common.model.BaseSelectListDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * 模型用量记录查询入参DTO
 * 用于ModelUsageRecordDao.selectList方法的条件筛选
 * 表名: model_usage_record
 *
 * @author G2rain Generator
 */
@Setter
@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "模型用量记录查询入参 DTO")
public class ModelUsageRecordSelectDto extends BaseSelectListDto {

    /**
     * 机构标识
     */
    @Schema(description = "机构标识")
    private Long organId;

    /**
     * 模型服务配置标识
     */
    @Schema(description = "模型服务配置标识")
    private Long profileId;

    /**
     * 模型服务目录标识
     */
    @Schema(description = "模型服务目录标识")
    private Long catalogId;

    /**
     * 模型协议类型
     */
    @Schema(description = "模型协议类型")
    private String protocol;

    /**
     * 模型供应商
     */
    @Schema(description = "模型供应商")
    private String provider;

    /**
     * 调用模型标识
     */
    @Schema(description = "调用模型标识")
    private String modelName;

    /**
     * 输入 token 数
     */
    @Schema(description = "输入 token 数")
    private Integer promptTokens;

    /**
     * 输出 token 数
     */
    @Schema(description = "输出 token 数")
    private Integer completionTokens;

    /**
     * 总 token 数
     */
    @Schema(description = "总 token 数")
    private Integer totalTokens;
}
