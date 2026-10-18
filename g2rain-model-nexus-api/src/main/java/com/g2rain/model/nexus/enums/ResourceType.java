package com.g2rain.model.nexus.enums;

/**
 * 模型目录资源类型：模型广场（MARKET）或私有（CUSTOM）。
 * <p>
 * 对应表字段 {@code model_service_catalog.resource_type}。
 */
public enum ResourceType {

    /**
     * 模型广场（运营方维护，租户可引用）。
     */
    MARKET,

    /**
     * 私有（随租户模型配置接入）。
     */
    CUSTOM
}
