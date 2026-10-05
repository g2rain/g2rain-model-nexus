package com.g2rain.model.nexus.protocol;


import com.g2rain.common.utils.Strings;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Objects;

/**
 * 各协议客户端创建 Spring AI 模型时的公共参数处理。
 * <p>
 * 统一处理超时兜底、baseUrl 规范化、BigDecimal 转 Double 等，
 * 避免 OpenAI / Anthropic / Gemini 实现各自重复一套逻辑。
 * <p>
 * 本类为包内工具，禁止实例化。
 *
 * @author alpha
 * @since 2026/8/1
 */
final class ModelProtocolSupport {

    /**
     * 禁止实例化。
     */
    private ModelProtocolSupport() {

    }

    /**
     * 将配置表中的超时毫秒转为 {@link Duration}；非法值回落 60 秒。
     *
     * @param timeoutMs 配置毫秒；null 或 &lt;=0 视为无效
     * @return 非 null 的超时 Duration
     */
    static Duration resolveTimeout(Integer timeoutMs) {
        // 未配置、或配置了非正数：视为无效，走默认 60 秒
        if (Objects.isNull(timeoutMs) || timeoutMs <= 0) {
            return Duration.ofSeconds(60);
        }

        // 合法毫秒值：转为 Duration 交给 OpenAI / Anthropic 等 SDK
        return Duration.ofMillis(timeoutMs);
    }

    /**
     * 将配置表中的超时毫秒归一为 Integer 毫秒；非法值回落 60000。
     * <p>
     * 供 Gemini {@code HttpOptions} 等需要整型毫秒的 SDK 使用。
     *
     * @param timeoutMs 配置毫秒；null 或 &lt;=0 视为无效
     * @return 超时毫秒
     */
    static Integer resolveTimeoutMs(Integer timeoutMs) {
        // 与 resolveTimeout 同一套兜底语义，只是返回类型是毫秒 Integer（Gemini HttpOptions 用）
        if (Objects.isNull(timeoutMs) || timeoutMs <= 0) {
            return 60_000;
        }

        // 原样返回配置值
        return timeoutMs;
    }

    /**
     * 规范化服务根地址：去首尾空白、去掉末尾多余 {@code /}。
     * <p>
     * 空白入参原样返回，表示「未配置自定义 baseUrl，走厂商默认域名」。
     *
     * @param url 配置中的 baseUrl
     * @return 规范化后的根地址，或原空白值
     */
    static String normalizeBaseUrl(String url) {
        // 空/空白：不做处理，调用方可能依赖「未配置 baseUrl」表示用官方默认域名
        if (Strings.isBlank(url)) {
            return url;
        }

        // 去掉首尾空格，避免配置里误粘贴空格导致请求失败
        url = url.trim();
        // 返回干净的根地址，如 https://api.openai.com
        return !url.endsWith("/") ? url : url.replaceAll("/+$", "");
    }

    /**
     * BigDecimal 采样参数转 Double；null 表示不覆盖 SDK 默认值。
     *
     * @param value 配置表中的 temperature / topP 等
     * @return Double 或 null
     */
    static Double toDouble(BigDecimal value) {
        // 库表用 BigDecimal 存小数；Spring AI options 要 Double
        // null 表示「不设置该采样参数」，由模型/SDK 默认值生效
        if (Objects.isNull(value)) {
            return null;
        }

        // 非 null：转为 double 包装类型
        return value.doubleValue();
    }
}
