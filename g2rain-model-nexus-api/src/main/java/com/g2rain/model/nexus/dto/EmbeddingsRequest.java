package com.g2rain.model.nexus.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * OpenAI 风格 Embeddings 请求体。
 * <p>
 * {@code input} 兼容单字符串与字符串数组；服务端通过 {@link #resolveInputs()}
 * 归一为 {@code List<String>} 再交给 Spring AI。
 *
 * @author alpha
 * @since 2026/8/1
 */
@Setter
@Getter
@NoArgsConstructor
@Schema(description = "Embeddings 请求")
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmbeddingsRequest {

    /**
     * 模型标识（对应目录 model_name）。
     */
    @Schema(description = "模型标识")
    private String model;

    /**
     * 待向量化文本；可为 {@link String} 或字符串集合。
     */
    @NotNull
    @Schema(description = "待向量化文本，string 或 string[]")
    private Object input;

    /**
     * 可选输出维度覆盖（部分厂商 / 模型支持）。
     */
    @Schema(description = "可选维度覆盖（部分厂商支持）")
    private Integer dimensions;

    /**
     * Bean Validation：归一后的 input 不得为空。
     * <p>
     * {@code @NotNull} 只能挡住 null，挡不住空数组 / 空集合。
     *
     * @return 归一后非空则为 true
     */
    @JsonIgnore
    @AssertTrue(message = "input must not be empty")
    public boolean isInputNotEmpty() {
        return !resolveInputs().isEmpty();
    }

    /**
     * 将 {@code input} 归一为文本列表。
     * <ul>
     *   <li>null → 空列表</li>
     *   <li>String → 单元素列表</li>
     *   <li>Collection → 逐项 {@code toString}（null 项变空串）</li>
     *   <li>其它类型 → {@code String.valueOf}</li>
     * </ul>
     *
     * @return 非 null 的文本列表（可能为空）
     */
    public List<String> resolveInputs() {
        // null：无输入
        if (Objects.isNull(input)) {
            return List.of();
        }

        // 单字符串：包成单元素列表
        if (input instanceof String text) {
            return List.of(text);
        }

        // 数组 / 集合：逐项转字符串
        if (input instanceof Collection<?> collection) {
            List<String> texts = new ArrayList<>(
                collection.size()
            );

            for (Object item : collection) {
                texts.add(Objects.toString(item, ""));
            }

            return texts;
        }

        // 兜底：任意对象转字符串
        return List.of(String.valueOf(input));
    }
}
