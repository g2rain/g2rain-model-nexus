package com.g2rain.model.nexus.dao.po;

import com.g2rain.common.model.BasePo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 模型服务配置返回Po
 * 关联表名: model_service_profile
 * 功能：封装实体数据，继承BasePo复用基础字段逻辑
 *
 * @author G2rain Generator
 */
@Setter
@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ModelServiceProfilePo extends BasePo {

    /**
     * 机构标识
     */
    private Long organId;

    /**
     * 模型服务目录标识
     */
    private Long catalogId;

    /**
     * 模型类型（取自目录，新增后不可变）
     */
    private String modelType;

    /**
     * 展示名称
     */
    private String displayName;

    /**
     * 对话可选
     */
    private Boolean selectable;

    /**
     * 默认标识（同机构同类型唯一）
     */
    private Boolean defaultFlag;

    /**
     * 采样温度
     */
    private BigDecimal temperature;

    /**
     * Top P 采样
     */
    private BigDecimal topP;

    /**
     * 最大输出 token 数
     */
    private Integer maxTokens;

    /**
     * 请求超时毫秒数
     */
    private Integer timeoutMs;

    /**
     * 状态[ACTIVE:启用, INACTIVE:禁用]
     */
    private String status;

    /**
     * 模型说明
     */
    private String description;

    /**
     * 删除标识[0:未删除, 1:已删除]
     */
    private Boolean deleteFlag;
}
