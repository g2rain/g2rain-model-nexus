package com.g2rain.model.nexus.enums;

import com.g2rain.common.exception.BusinessException;
import com.g2rain.common.exception.SystemErrorCode;
import com.g2rain.common.utils.Strings;
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
    INACTIVE;

    /**
     * 校验状态名是否为合法枚举值。
     *
     * @param name 状态字符串
     * @throws BusinessException 名称无效时抛出
     */
    public static void validate(String name) {
        if (Strings.isNotBlank(name)) {
            for (ModelStatus status : values()) {
                if (status.name().equals(name)) {
                    return;
                }
            }
        }
        throw new BusinessException(SystemErrorCode.PARAM_VAL_INVALID, name);
    }
}
