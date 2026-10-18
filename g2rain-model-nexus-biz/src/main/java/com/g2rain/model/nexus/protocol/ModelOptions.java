package com.g2rain.model.nexus.protocol;

import java.math.BigDecimal;

/**
 * 模型调用采样与超时参数（仅服务内部使用）。
 *
 * @param temperature 采样温度
 * @param topP        Top P 采样
 * @param maxTokens   最大输出 token 数
 * @param timeoutMs   请求超时毫秒数
 */
public record ModelOptions(
    BigDecimal temperature,
    BigDecimal topP,
    Integer maxTokens,
    Integer timeoutMs
) {
}
