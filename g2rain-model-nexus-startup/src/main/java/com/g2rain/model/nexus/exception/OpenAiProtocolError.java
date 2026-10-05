package com.g2rain.model.nexus.exception;

import com.g2rain.model.nexus.vo.OpenAiErrorResponseVo;

/**
 * OpenAI 协议错误的 HTTP 状态码与响应体。
 *
 * @param status HTTP 状态码
 * @param body   OpenAI {@code error} 响应根对象
 * @author alpha
 * @since 2026/7/20
 */
public record OpenAiProtocolError(int status, OpenAiErrorResponseVo body) {
}
