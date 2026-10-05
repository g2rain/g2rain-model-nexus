package com.g2rain.model.nexus.exception;

import com.g2rain.common.exception.BusinessException;
import com.g2rain.common.exception.ExceptionConverter;
import com.g2rain.common.exception.FieldError;
import com.g2rain.common.exception.SystemErrorCode;
import com.g2rain.model.nexus.api.OpenAiCompatibleApi;
import com.g2rain.model.nexus.vo.OpenAiErrorResponseVo;
import com.g2rain.web.exception.ConstraintFormatter;
import jakarta.validation.ConstraintViolationException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Objects;

/**
 * OpenAI 兼容协议口异常处理（仅 {@link OpenAiCompatibleApi} 实现类）。
 * <p>
 * 优先级高于全局 {@code GlobalExceptionHandler}，避免 {@code /v1} 异常被写成平台 {@code Result}。
 *
 * @author alpha
 * @since 2026/7/20
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@RestControllerAdvice(assignableTypes = {OpenAiCompatibleApi.class})
public class OpenAiExceptionAdvice {

    private final OpenAiErrorAssembler openAiErrorAssembler;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<@NonNull OpenAiErrorResponseVo> handle(BusinessException ex) {
        log.error("OpenAI 协议业务异常: {}", ex.getMessage(), ex);
        return openAiErrorAssembler.toResponseEntity(ex);
    }

    /**
     * 覆盖 {@code MethodArgumentNotValidException}（其子类）与表单绑定失败。
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<@NonNull OpenAiErrorResponseVo> handle(BindException ex) {
        log.error("OpenAI 协议参数校验失败: {}", ex.getMessage(), ex);
        var fieldError = ex.getBindingResult().getFieldError();
        List<FieldError> errors = Objects.isNull(fieldError)
            ? List.of()
            : List.of(new FieldError(fieldError.getField(), SystemErrorCode.PARAM_VAL_INVALID, fieldError.getField()));
        return openAiErrorAssembler.toResponseEntity(new BusinessException(SystemErrorCode.PARAM_INVALID, errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<@NonNull OpenAiErrorResponseVo> handle(ConstraintViolationException ex) {
        log.error("OpenAI 协议约束校验失败: {}", ex.getMessage(), ex);
        List<FieldError> errors = ex.getConstraintViolations().stream().map(v -> {
            String field = ConstraintFormatter.extractProperty(v.getPropertyPath().toString());
            return new FieldError(field, SystemErrorCode.PARAM_VAL_INVALID, field);
        }).toList();
        return openAiErrorAssembler.toResponseEntity(new BusinessException(SystemErrorCode.PARAM_INVALID, errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<@NonNull OpenAiErrorResponseVo> handle(Exception ex) {
        log.error("OpenAI 协议系统异常: {}", ex.getMessage(), ex);
        return openAiErrorAssembler.toResponseEntity(ExceptionConverter.findBusinessExceptionOrDefault(ex));
    }
}
