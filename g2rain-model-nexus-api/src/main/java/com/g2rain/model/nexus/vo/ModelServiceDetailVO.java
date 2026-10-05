package com.g2rain.model.nexus.vo;


import com.g2rain.common.json.AdminCompanyCondition;
import com.g2rain.common.json.ConditionalJsonIgnore;
import com.g2rain.common.model.BaseVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @author alpha
 * @since 2026/8/1
 */
@Setter
@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "模型服务明细 VO")
public class ModelServiceDetailVO extends BaseVo {

    /**
     * 机构标识
     */
    @Schema(description = "机构标识")
    private Long organId;

    /**
     * 模型服务目录标识
     */
    @Schema(description = "模型服务目录标识")
    private Long catalogId;

    /**
     * 模型调用标识
     */
    @Schema(description = "模型调用标识")
    private String modelName;

    /**
     * 模型类型[LLM,VECTOR]
     */
    @Schema(description = "模型类型[LLM,VECTOR]")
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
    @Schema(description = "状态[ACTIVE:启用, INACTIVE:禁用]")
    private String status;

    /**
     * 模型说明
     */
    @Schema(description = "模型说明")
    private String description;

    /**
     * 删除标识[0:未删除, 1:已删除]
     */
    @Schema(description = "删除标识（0 未删除，1 已删除）", example = "false")
    @ConditionalJsonIgnore(adminCompany = AdminCompanyCondition.TRUE)
    private Boolean deleteFlag;
}
