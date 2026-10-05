package com.g2rain.model.nexus.config;

import com.g2rain.common.exception.ExceptionProcessor;
import com.g2rain.model.nexus.exception.OpenAiErrorAssembler;
import com.g2rain.model.nexus.filter.ModelNexusExceptionFilter;
import com.g2rain.web.autoconfigure.G2rainWebProperties;
import com.g2rain.web.filters.GlobalExceptionFilter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 注册本服务异常 Filter，替换 starter 默认 {@link GlobalExceptionFilter}。
 *
 * @author alpha
 * @since 2026/7/20
 */
@Configuration
@EnableConfigurationProperties(ModelNexusWebProperties.class)
public class ExceptionFilterConfig {

    /**
     * 提供带协议路径分流能力的全局异常 Filter。
     *
     * @param exceptionProcessor      平台异常处理器
     * @param webProperties           starter Web 配置
     * @param modelNexusWebProperties 本服务协议路径配置
     * @param openAiErrorAssembler    OpenAI 错误组装器
     * @return 本服务 Filter（类型为 {@link GlobalExceptionFilter}，供 starter 的 RegistrationBean 注入）
     */
    @Bean
    public GlobalExceptionFilter globalExceptionFilter(ExceptionProcessor exceptionProcessor,
                                                       G2rainWebProperties webProperties,
                                                       ModelNexusWebProperties modelNexusWebProperties,
                                                       OpenAiErrorAssembler openAiErrorAssembler) {
        return new ModelNexusExceptionFilter(exceptionProcessor, webProperties, modelNexusWebProperties, openAiErrorAssembler);
    }
}
