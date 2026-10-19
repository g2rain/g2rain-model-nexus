package com.g2rain.model.nexus.service.impl;

import com.g2rain.common.exception.BusinessException;
import com.g2rain.common.exception.SystemErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OpenAiCompatibleServiceImplTest {

    @Test
    void defaultsBlankEncodingFormatToBase64() {
        assertEquals(OpenAiEmbeddingOptions.EncodingFormat.BASE64, OpenAiCompatibleServiceImpl.resolveEncodingFormat(null));
        assertEquals(OpenAiEmbeddingOptions.EncodingFormat.BASE64, OpenAiCompatibleServiceImpl.resolveEncodingFormat("  "));
    }

    @Test
    void acceptsFloatAndBase64IgnoringCase() {
        assertEquals(OpenAiEmbeddingOptions.EncodingFormat.FLOAT, OpenAiCompatibleServiceImpl.resolveEncodingFormat("Float"));
        assertEquals(OpenAiEmbeddingOptions.EncodingFormat.BASE64, OpenAiCompatibleServiceImpl.resolveEncodingFormat(" BASE64 "));
    }

    @Test
    void rejectsUnknownEncodingFormat() {
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> OpenAiCompatibleServiceImpl.resolveEncodingFormat("binary")
        );
        assertEquals(SystemErrorCode.PARAM_VAL_INVALID.code(), exception.getErrorCode());
    }
}
