package com.g2rain.model.nexus.protocol.openai;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;

/**
 * OpenAI 协议上游调用的报文轨迹（OkHttp 拦截器）。
 *
 * <ul>
 *   <li>DEBUG：打印实际发给上游的请求体与上游原始响应体（各截取前 {@value #LOG_MAX} 字），
 *       用于核对模型参数、tools 与消息</li>
 *   <li>WARN：上游返回非 2xx 时打印原始响应体</li>
 *   <li>记下本线程最近一次的原始响应片段，供 {@code UpstreamException} 在响应体不合规时附带原文</li>
 * </ul>
 *
 * <p>鉴权头不打印；请求体中不含密钥。调用为同步执行，拦截器与业务在同一线程。</p>
 */
@Slf4j
public final class UpstreamTrace implements Interceptor {

    /**
     * 单例：拦截器无状态（响应片段存于线程局部变量）。
     */
    public static final UpstreamTrace INSTANCE = new UpstreamTrace();

    /**
     * 日志中报文的最大字数。
     */
    private static final int LOG_MAX = 4000;

    /**
     * 本线程最近一次上游响应的原文片段。
     */
    private static final ThreadLocal<String> LAST_RESPONSE = new ThreadLocal<>();

    private UpstreamTrace() {
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        LAST_RESPONSE.remove();
        if (log.isDebugEnabled()) {
            log.debug("upstream request: {} {} body={}", request.method(), request.url(), bodyOf(request.body()));
        }

        long start = System.nanoTime();
        Response response = chain.proceed(request);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000L;
        // peekBody 不消费原响应流，SDK 仍能正常解析
        String body = clip(response.peekBody(LOG_MAX * 4L).string());
        LAST_RESPONSE.set(body);
        if (!response.isSuccessful()) {
            log.warn("upstream response: status={} elapsedMs={} body={}", response.code(), elapsedMs, body);
        } else if (log.isDebugEnabled()) {
            log.debug("upstream response: status={} elapsedMs={} body={}", response.code(), elapsedMs, body);
        }
        return response;
    }

    /**
     * 取出并清除本线程最近一次的上游原始响应片段。
     *
     * @return 原始响应片段；没有为空
     */
    public static @Nullable String take() {
        String body = LAST_RESPONSE.get();
        LAST_RESPONSE.remove();
        return body;
    }

    private static String bodyOf(@Nullable RequestBody body) throws IOException {
        if (Objects.isNull(body)) {
            return "";
        }
        Buffer buffer = new Buffer();
        body.writeTo(buffer);
        return clip(buffer.readUtf8());
    }

    private static String clip(String text) {
        return text.length() > LOG_MAX ? text.substring(0, LOG_MAX) + "…" : text;
    }
}
