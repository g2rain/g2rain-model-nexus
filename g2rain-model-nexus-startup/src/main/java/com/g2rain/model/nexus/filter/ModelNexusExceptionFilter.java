package com.g2rain.model.nexus.filter;

import com.g2rain.common.exception.BaseError;
import com.g2rain.common.exception.BusinessException;
import com.g2rain.common.exception.ExceptionConverter;
import com.g2rain.common.exception.ExceptionProcessor;
import com.g2rain.common.json.FailIgnoreFieldMixIn;
import com.g2rain.common.json.JsonCodec;
import com.g2rain.common.json.JsonCodecBuilder;
import com.g2rain.common.json.JsonCodecFactory;
import com.g2rain.common.model.Result;
import com.g2rain.common.utils.Strings;
import com.g2rain.common.web.PrincipalContextHolder;
import com.g2rain.model.nexus.config.ModelNexusWebProperties;
import com.g2rain.model.nexus.exception.OpenAiErrorAssembler;
import com.g2rain.model.nexus.exception.OpenAiProtocolError;
import com.g2rain.web.autoconfigure.G2rainWebProperties;
import com.g2rain.web.filters.GlobalExceptionFilter;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * model-nexus 全局异常 Filter：协议路径走 OpenAI error，其余走平台 {@link Result}。
 * <p>
 * 继承 {@link GlobalExceptionFilter} 以便替换 starter 中 {@code ConditionalOnMissingBean} 注册的同名 Bean。
 *
 * @author alpha
 * @since 2026/7/20
 */
@Slf4j
public class ModelNexusExceptionFilter extends GlobalExceptionFilter {

    /**
     * 平台 Result 异常处理器。
     */
    private final ExceptionProcessor exceptionProcessor;

    /**
     * 本服务协议路径配置。
     */
    private final ModelNexusWebProperties modelNexusWebProperties;

    /**
     * OpenAI 错误组装器。
     */
    private final OpenAiErrorAssembler openAiErrorAssembler;

    /**
     * JSON 编解码。
     */
    private final JsonCodec jsonCodec;

    /**
     * Ant 路径匹配。
     */
    private final PathMatcher pathMatcher = new AntPathMatcher();

    /**
     * @param exceptionProcessor      平台异常处理器
     * @param webProperties           starter Web 配置（JSON MixIn 等）
     * @param modelNexusWebProperties 本服务协议路径配置
     * @param openAiErrorAssembler    OpenAI 错误组装器
     */
    public ModelNexusExceptionFilter(ExceptionProcessor exceptionProcessor,
                                     G2rainWebProperties webProperties,
                                     ModelNexusWebProperties modelNexusWebProperties,
                                     OpenAiErrorAssembler openAiErrorAssembler) {
        super(exceptionProcessor, webProperties);
        this.exceptionProcessor = exceptionProcessor;
        this.modelNexusWebProperties = modelNexusWebProperties;
        this.openAiErrorAssembler = openAiErrorAssembler;
        if (webProperties.isGlobalExceptionFilterUseCustomJson()) {
            this.jsonCodec = JsonCodecBuilder.builder().withDefaults().withConfig(jsonMapper -> {
                jsonMapper.addMixIn(Result.class, FailIgnoreFieldMixIn.class);
                jsonMapper.addMixIn(BaseError.class, FailIgnoreFieldMixIn.class);
            }).build();
        } else {
            this.jsonCodec = JsonCodecFactory.instance();
        }
    }

    /**
     * 捕获 Filter 链异常，按路径分流为 OpenAI error 或 Result。
     *
     * @param request  请求
     * @param response 响应
     * @param chain    过滤器链
     * @throws IOException 写出失败
     */
    @Override
    public void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response,
                                 @Nonnull FilterChain chain) throws IOException {
        Object failBody = null;
        Integer httpStatus = null;
        try {
            chain.doFilter(request, response);
        } catch (BusinessException e) {
            log.error("业务异常处理-{}: {}", e.getClass().getSimpleName(), e.getMessage(), e);
            if (isProtocolErrorPath(request)) {
                OpenAiProtocolError protocolError = openAiErrorAssembler.assemble(e);
                httpStatus = protocolError.status();
                failBody = protocolError.body();
            } else {
                httpStatus = HttpServletResponse.SC_OK;
                failBody = exceptionProcessor.process(e, PrincipalContextHolder.getAcceptLanguage());
            }
        } catch (Exception e) {
            log.error("系统异常处理-{}: {}", e.getClass().getSimpleName(), e.getMessage(), e);
            BusinessException be = ExceptionConverter.findBusinessExceptionOrDefault(e);
            if (isProtocolErrorPath(request)) {
                OpenAiProtocolError protocolError = openAiErrorAssembler.assemble(be);
                httpStatus = protocolError.status();
                failBody = protocolError.body();
            } else {
                httpStatus = HttpServletResponse.SC_OK;
                failBody = exceptionProcessor.process(be, PrincipalContextHolder.getAcceptLanguage());
            }
        } finally {
            if (Objects.nonNull(failBody) && !response.isCommitted()) {
                response.setStatus(httpStatus);
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                byte[] jsonBytes = jsonCodec.obj2byte(failBody);
                response.setContentLength(jsonBytes.length);
                response.getOutputStream().write(jsonBytes);
                response.getOutputStream().flush();
            }
        }
    }

    /**
     * 是否命中协议错误路径。
     *
     * @param request 当前请求
     * @return 命中为 true
     */
    private boolean isProtocolErrorPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (Strings.isNotBlank(contextPath) && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }

        if (Strings.isBlank(path)) {
            path = "/";
        }

        return OpenAiErrorAssembler.matchesPath(
            path,
            modelNexusWebProperties.getProtocolErrorPathPatterns(),
            pathMatcher
        );
    }
}
