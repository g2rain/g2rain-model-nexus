package com.g2rain.model.nexus.protocol;

import com.anthropic.errors.AnthropicServiceException;
import com.g2rain.common.json.JsonCodec;
import com.g2rain.common.json.JsonCodecFactory;
import com.g2rain.common.utils.Strings;
import com.g2rain.model.nexus.protocol.openai.UpstreamTrace;
import com.google.genai.errors.ApiException;
import com.openai.errors.OpenAIInvalidDataException;
import com.openai.errors.OpenAIServiceException;
import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.web.client.RestClientResponseException;

import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * 上游厂商返回的错误：保留原始 HTTP 状态码、厂商错误码与（脱敏后的）原文。
 *
 * <p>由 {@link #of(Throwable, String)} 沿异常链识别 OpenAI / Anthropic / Gemini SDK、
 * RestClient（Rerank）、上游响应体不合规与超时异常；识别不了的仍按平台 {@code REMOTE_SERVICE_ERROR} 处理。</p>
 */
@Getter
public class UpstreamException extends RuntimeException {

    /**
     * 超时统一返回的状态码与错误码。
     */
    private static final int TIMEOUT_STATUS = 504;

    private static final String TIMEOUT_CODE = "timeout";

    /**
     * 响应体不合规统一返回的状态码与错误码。
     */
    private static final int INVALID_STATUS = 502;

    private static final String INVALID_CODE = "invalid_response";

    /**
     * 原文最大长度。
     */
    private static final int MESSAGE_MAX = 500;

    /**
     * 密钥脱敏：sk- 开头的 key 与 Bearer 令牌。
     */
    private static final Pattern SECRET = Pattern.compile("(sk-[A-Za-z0-9_\\-]{6})[A-Za-z0-9_\\-]+|(Bearer\\s+)\\S+");

    /**
     * 地址脱敏：不回显供应商私有地址。
     */
    private static final Pattern URL = Pattern.compile("https?://\\S+");

    private static final JsonCodec JSON = JsonCodecFactory.instance();

    /**
     * 上游 HTTP 状态码。
     */
    private final int status;

    /**
     * 厂商错误码，如 {@code insufficient_quota}、{@code rate_limit_error}、{@code RESOURCE_EXHAUSTED}；可空。
     */
    private final @Nullable String code;

    /**
     * 模型供应商。
     */
    private final String provider;

    /**
     * @param status   上游 HTTP 状态码
     * @param code     厂商错误码；可空
     * @param message  厂商原文（已脱敏、截断）
     * @param provider 模型供应商
     * @param cause    原始异常
     */
    public UpstreamException(int status, @Nullable String code, String message, String provider, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.code = code;
        this.provider = provider;
    }

    /**
     * 沿异常链识别上游错误。
     *
     * @param ex       调用异常
     * @param provider 模型供应商
     * @return 上游错误；识别不了返回 {@code null}
     */
    public static @Nullable UpstreamException of(Throwable ex, String provider) {
        for (Throwable t = ex; Objects.nonNull(t); t = t.getCause()) {
            switch (t) {
                case OpenAIServiceException e -> {
                    Map<?, ?> body = bodyOf(() -> e.body().convert(Map.class));
                    String code = e.code().or(e::type).orElse(null);
                    return new UpstreamException(e.statusCode(), code, messageOf(body, e), provider, ex);
                }
                case AnthropicServiceException e -> {
                    Map<?, ?> body = bodyOf(() -> e.body().convert(Map.class));
                    String code = e.errorType().map(Object::toString).orElse(null);
                    return new UpstreamException(e.statusCode(), code, messageOf(body, e), provider, ex);
                }
                case ApiException e -> {
                    return new UpstreamException(e.code(), e.status(), clean(e.message()), provider, ex);
                }
                case RestClientResponseException e -> {
                    Map<?, ?> body = bodyOf(() -> JSON.str2map(e.getResponseBodyAsString()));
                    String code = Objects.toString(nested(body, "code"), null);
                    return new UpstreamException(e.getStatusCode().value(), code, messageOf(body, e), provider, ex);
                }
                case OpenAIInvalidDataException e -> {
                    // 上游 200 但响应体不符合协议（如缺 choices）：附带原始响应便于排查
                    String raw = Objects.requireNonNullElse(UpstreamTrace.take(), "");
                    return new UpstreamException(INVALID_STATUS, INVALID_CODE,
                        clean(e.getMessage() + "；原始响应：" + raw), provider, ex);
                }
                case SocketTimeoutException _, HttpTimeoutException _ -> {
                    return new UpstreamException(TIMEOUT_STATUS, TIMEOUT_CODE, clean(t.getMessage()), provider, ex);
                }
                default -> {
                    // 继续沿 cause 查找
                }
            }
        }
        return null;
    }

    /**
     * 安全读取错误体；格式不符返回空 Map。
     */
    private static Map<?, ?> bodyOf(Supplier<? extends Map<?, ?>> reader) {
        try {
            return Objects.requireNonNullElse(reader.get(), Map.of());
        } catch (RuntimeException _) {
            return Map.of();
        }
    }

    /**
     * 取错误原文：优先 {@code error.message}，其次 {@code message}，都没有用异常信息。
     */
    private static String messageOf(Map<?, ?> body, Throwable fallback) {
        Object message = nested(body, "message");
        return clean(Objects.nonNull(message) ? message.toString() : fallback.getMessage());
    }

    /**
     * 读取 {@code error.<key>}，没有时读顶层 {@code <key>}。
     */
    private static @Nullable Object nested(Map<?, ?> body, String key) {
        if (body.get("error") instanceof Map<?, ?> error && Objects.nonNull(error.get(key))) {
            return error.get(key);
        }
        return body.get(key);
    }

    /**
     * 脱敏（密钥、地址）并截断原文。
     */
    private static String clean(@Nullable String message) {
        String text = SECRET.matcher(Strings.defaultIfBlank(message, "")).replaceAll("$1$2***");
        text = URL.matcher(text).replaceAll("[url]");
        return text.length() > MESSAGE_MAX ? text.substring(0, MESSAGE_MAX) : text;
    }
}
