package com.g2rain.model.nexus.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 模型服务配置下拉选项（仅 id + modelName，省带宽）。
 */
@Getter
@Setter
@NoArgsConstructor
public class ModelServiceOptionVo {

    /**
     * 模型服务配置标识
     */
    private Long id;

    /**
     * 模型调用标识
     */
    private String modelName;
}
