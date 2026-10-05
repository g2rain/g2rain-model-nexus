package com.g2rain.model.nexus.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * model-nexus 本服务 Web 相关配置。
 *
 * @author alpha
 * @since 2026/7/20
 */
@Data
@ConfigurationProperties(prefix = "g2rain.model-nexus")
public class ModelNexusWebProperties {

    /**
     * 使用 OpenAI 兼容错误体的路径 Ant 模式；未命中仍返回平台 {@code Result}。
     */
    private List<String> protocolErrorPathPatterns = new ArrayList<>(List.of("/v1/**"));
}
