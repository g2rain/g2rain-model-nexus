package com.g2rain.model.nexus.exception;

import com.g2rain.common.exception.BusinessException;
import com.g2rain.common.exception.ExceptionProcessor;
import com.g2rain.common.exception.FieldError;
import com.g2rain.common.model.Result;
import com.g2rain.common.utils.Collections;
import com.g2rain.common.utils.Strings;
import com.g2rain.common.web.PrincipalContextHolder;
import com.g2rain.model.nexus.vo.OpenAiErrorResponseVo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.PathMatcher;

import java.util.List;
import java.util.Objects;

/**
 * 将 {@link BusinessException} 组装为 OpenAI 兼容错误响应。
 * <p>
 * {@code message} 经 {@link ExceptionProcessor} 按 {@code Accept-Language} 本地化；
 * {@code code}/{@code param} 复用异常字段；{@code type} 与 HTTP 状态由错误码末段推导。
 *
 * @author alpha
 * @since 2026/7/20
 */
@Component
public class OpenAiErrorAssembler {

    private final ExceptionProcessor exceptionProcessor;

    /**
     * @param exceptionProcessor 平台异常处理器（含可选 ErrorMessageRegistry 本地化）
     */
    public OpenAiErrorAssembler(ExceptionProcessor exceptionProcessor) {
        this.exceptionProcessor = exceptionProcessor;
    }

    /**
     * 组装协议错误（状态码 + body）。
     *
     * @param ex 业务异常
     * @return 协议错误
     */
    public OpenAiProtocolError assemble(BusinessException ex) {
        Result<Void> localized = exceptionProcessor.process(ex, PrincipalContextHolder.getAcceptLanguage());
        String code = localized.getErrorCode();
        String message = localized.getErrorMessage();
        String param = null;
        if (Collections.isNotEmpty(localized.getFieldErrors())) {
            FieldError first = localized.getFieldErrors().getFirst();
            param = first.getField();
            if (Strings.isNotBlank(first.getErrorMessage())) {
                message = first.getErrorMessage();
            }
        }
        int status = resolveHttpStatus(code);
        String type = resolveType(status);
        return new OpenAiProtocolError(status, OpenAiErrorResponseVo.of(message, type, param, code));
    }

    /**
     * 组装为 Spring {@link ResponseEntity}。
     *
     * @param ex 业务异常
     * @return HTTP 响应
     */
    public ResponseEntity<OpenAiErrorResponseVo> toResponseEntity(BusinessException ex) {
        OpenAiProtocolError protocolError = assemble(ex);
        return ResponseEntity.status(protocolError.status()).body(protocolError.body());
    }

    /**
     * 由错误码末段数字推导 HTTP 状态。
     *
     * @param errorCode 平台错误码，如 {@code model_nexus.40001}
     * @return HTTP 状态码数值
     */
    public static int resolveHttpStatus(String errorCode) {
        String numeric = extractNumericSuffix(errorCode);
        if (Strings.isBlank(numeric) || !Character.isDigit(numeric.charAt(0))) {
            return HttpStatus.INTERNAL_SERVER_ERROR.value();
        }

        if (numeric.startsWith("401")) {
            return HttpStatus.UNAUTHORIZED.value();
        }

        if (numeric.startsWith("403")) {
            return HttpStatus.FORBIDDEN.value();
        }

        if (numeric.startsWith("404")) {
            return HttpStatus.NOT_FOUND.value();
        }

        if (numeric.startsWith("405")) {
            return HttpStatus.METHOD_NOT_ALLOWED.value();
        }

        if (numeric.startsWith("406")) {
            return HttpStatus.NOT_ACCEPTABLE.value();
        }

        if (numeric.startsWith("415")) {
            return HttpStatus.UNSUPPORTED_MEDIA_TYPE.value();
        }

        if (numeric.startsWith("429")) {
            return HttpStatus.TOO_MANY_REQUESTS.value();
        }

        if (numeric.charAt(0) == '4') {
            return HttpStatus.BAD_REQUEST.value();
        }

        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

    /**
     * 由 HTTP 状态推导 OpenAI {@code error.type}。
     *
     * @param status HTTP 状态码
     * @return type 字符串
     */
    public static String resolveType(int status) {
        return switch (status) {
            case 401 -> "authentication_error";
            case 403 -> "permission_error";
            case 404 -> "not_found_error";
            case 429 -> "rate_limit_error";
            default -> status >= 400 && status < 500 ? "invalid_request_error" : "server_error";
        };
    }

    /**
     * 判断请求路径是否命中协议错误出口（Ant 风格）。
     *
     * @param path     去掉 context-path 后的 URI
     * @param patterns 路径模式列表
     * @param matcher  Ant 路径匹配器
     * @return 是否命中
     */
    public static boolean matchesPath(String path, List<String> patterns, PathMatcher matcher) {
        if (Strings.isBlank(path) || Collections.isEmpty(patterns) || Objects.isNull(matcher)) {
            return false;
        }

        for (String pattern : patterns) {
            if (Strings.isNotBlank(pattern) && matcher.match(pattern, path)) {
                return true;
            }
        }

        return false;
    }

    private static String extractNumericSuffix(String errorCode) {
        if (Strings.isBlank(errorCode)) {
            return null;
        }

        int dot = errorCode.lastIndexOf('.');
        return dot >= 0 ? errorCode.substring(dot + 1) : errorCode;
    }
}
