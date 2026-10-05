package com.g2rain.model.nexus.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 模型接入 / 目录启用状态。
 * <p>
 * 调用链路只路由 {@link #ACTIVE} 的配置；停用配置不可被 chat / embeddings 选中。
 *
 * @author alpha
 * @since 2026/8/1
 */
@Schema(description = "模型接入配置启用状态")
public enum ModelStatus {

    @Schema(description = "已启用")
    ACTIVE,

    @Schema(description = "已停用")
    INACTIVE
}
