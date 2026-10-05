package com.g2rain.model.nexus.support;

import com.g2rain.common.exception.BusinessException;
import com.g2rain.model.nexus.enums.NexusErrorCode;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

/**
 * 仅携带 Tool JSON Schema 的 {@link ToolCallback} 实现。
 * <p>
 * 本模块作为模型网关只负责把 tools 定义「广告」给大模型，
 * 真正的工具执行由上游调用方完成。因此：
 * <ul>
 *   <li>{@link #getToolDefinition()} 正常返回 name/description/inputSchema</li>
 *   <li>{@link #call(String)} 一律抛出 {@link NexusErrorCode#TOOL_EXECUTION_UNSUPPORTED}</li>
 * </ul>
 *
 * @author alpha
 * @since 2026/7/20
 */
public final class SchemaToolCallback implements ToolCallback {

    /**
     * Spring AI 工具定义（名称、描述、入参 JSON Schema）。
     */
    private final ToolDefinition toolDefinition;

    /**
     * 使用已构造好的 {@link ToolDefinition} 创建回调。
     *
     * @param toolDefinition 工具定义，不为 null
     */
    public SchemaToolCallback(ToolDefinition toolDefinition) {
        this.toolDefinition = toolDefinition;
    }

    /**
     * 便捷工厂：由 name / description / inputSchema 组装 {@link ToolDefinition}。
     *
     * @param name        工具函数名（对应 OpenAI tools[].function.name）
     * @param description 工具说明；可与 name 相同作为兜底
     * @param inputSchema 入参 JSON Schema 字符串
     * @return 仅 schema、不可执行的 ToolCallback
     */
    public static SchemaToolCallback of(String name, String description, String inputSchema) {
        return new SchemaToolCallback(ToolDefinition.builder()
            .name(name)
            .description(description)
            .inputSchema(inputSchema)
            .build());
    }

    /**
     * 返回工具定义，供 ChatModel 在请求中声明可用 tools。
     *
     * @return 工具定义
     */
    @Override
    public @NonNull ToolDefinition getToolDefinition() {
        return toolDefinition;
    }

    /**
     * 拒绝在本进程执行工具。
     * <p>
     * 若 Spring AI 或误用方触发执行，抛出业务异常提示应在上游执行。
     *
     * @param toolInput 模型给出的工具入参 JSON（本方法不会使用）
     * @return 不会正常返回
     * @throws BusinessException 错误码 {@link NexusErrorCode#TOOL_EXECUTION_UNSUPPORTED}
     */
    @Override
    public @NonNull String call(@NonNull String toolInput) {
        // 本网关不执行工具，避免误跑业务副作用
        throw new BusinessException(
            NexusErrorCode.TOOL_EXECUTION_UNSUPPORTED,
            toolDefinition.name()
        );
    }
}
