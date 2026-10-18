package com.g2rain.model.nexus.protocol;

/**
 * 模型接入端点（仅服务内部流转，不出现在 Controller 返回值中）。
 * <p>
 * 携带调用上游所需的明文 {@code apiKey}；对外接口请使用 VO 的 {@code apiKeyConfigured}。
 *
 * @param profileId 配置主键
 * @param catalogId 目录主键（用量落库必填）
 * @param protocol  协议类型
 * @param provider  供应商
 * @param baseUrl   服务地址
 * @param apiKey    访问密钥
 * @param modelName 模型调用标识
 * @param options   采样与超时参数
 */
public record ModelEndpoint(
    Long profileId,
    Long catalogId,
    String protocol,
    String provider,
    String baseUrl,
    String apiKey,
    String modelName,
    ModelOptions options
) {
}
