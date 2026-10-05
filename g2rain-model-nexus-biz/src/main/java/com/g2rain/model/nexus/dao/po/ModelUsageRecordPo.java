package com.g2rain.model.nexus.dao.po;

import com.g2rain.common.model.BasePo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * 模型用量记录返回Po
 * 关联表名: model_usage_record
 * 功能：封装实体数据，继承BasePo复用基础字段逻辑
 *
 * @author G2rain Generator
 */
@Setter
@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ModelUsageRecordPo extends BasePo {

    /**
     * 机构标识
     */
    private Long organId;

    /**
     * 模型服务配置标识
     */
    private Long profileId;

    /**
     * 模型服务目录标识
     */
    private Long catalogId;

    /**
     * 模型协议类型
     */
    private String protocol;

    /**
     * 模型供应商
     */
    private String provider;

    /**
     * 调用模型标识
     */
    private String modelName;

    /**
     * 输入 token 数
     */
    private Integer promptTokens;

    /**
     * 输出 token 数
     */
    private Integer completionTokens;

    /**
     * 总 token 数
     */
    private Integer totalTokens;
}