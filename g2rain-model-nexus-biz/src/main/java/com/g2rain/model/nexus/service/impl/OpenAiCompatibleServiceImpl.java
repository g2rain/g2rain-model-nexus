package com.g2rain.model.nexus.service.impl;


import com.g2rain.common.exception.BusinessException;
import com.g2rain.common.exception.SystemErrorCode;
import com.g2rain.common.utils.Asserts;
import com.g2rain.common.utils.Strings;
import com.g2rain.common.web.PrincipalContextHolder;
import com.g2rain.model.nexus.converter.ModelUsageRecordConverter;
import com.g2rain.model.nexus.dto.ChatCompletionRequest;
import com.g2rain.model.nexus.dto.EmbeddingsRequest;
import com.g2rain.model.nexus.dto.ModelServiceProfileSelectDto;
import com.g2rain.model.nexus.dto.ModelUsageRecordDto;
import com.g2rain.model.nexus.dto.RerankRequest;
import com.g2rain.model.nexus.enums.ModelStatus;
import com.g2rain.model.nexus.enums.ModelType;
import com.g2rain.model.nexus.protocol.ModelClient;
import com.g2rain.model.nexus.protocol.ModelClientRegistry;
import com.g2rain.model.nexus.protocol.ModelEndpoint;
import com.g2rain.model.nexus.protocol.RerankCallResult;
import com.g2rain.model.nexus.protocol.RerankModel;
import com.g2rain.model.nexus.protocol.UpstreamException;
import com.g2rain.model.nexus.schema.EmbeddingData;
import com.g2rain.model.nexus.schema.EmbeddingUsage;
import com.g2rain.model.nexus.service.ModelServiceProfileService;
import com.g2rain.model.nexus.service.ModelUsageRecordService;
import com.g2rain.model.nexus.service.OpenAiCompatibleService;
import com.g2rain.model.nexus.support.OpenAiChatServerConverter;
import com.g2rain.model.nexus.support.OpenAiRerankConverter;
import com.g2rain.model.nexus.vo.ChatCompletionResponse;
import com.g2rain.model.nexus.vo.EmbeddingsResponse;
import com.g2rain.model.nexus.vo.RerankResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * {@link OpenAiCompatibleService} 的默认实现。
 * <p>
 * 职责：
 * <ul>
 *   <li>按 model 名称解析启用中的 {@link ModelEndpoint}</li>
 *   <li>通过 {@link ModelClientRegistry} 按协议获取 {@link ModelClient}</li>
 *   <li>调用 Chat / Embedding / Rerank，并转换为 OpenAI 兼容响应</li>
 *   <li>尽力写入用量记录（失败只打日志，不影响主流程）</li>
 * </ul>
 *
 * @author alpha
 * @see OpenAiCompatibleService
 * @since 2026/8/1
 */
@Slf4j
@Service(value = "openAiCompatibleServiceImpl")
public class OpenAiCompatibleServiceImpl implements OpenAiCompatibleService {

    /**
     * 模型服务明细查询：按 modelName / 类型 / 状态解析启用配置。
     */
    @Resource(name = "modelServiceProfileServiceImpl")
    private ModelServiceProfileService modelServiceProfileService;

    /**
     * 用量记录服务，将每次调用的 token 用量落库。
     */
    @Resource(name = "modelUsageRecordServiceImpl")
    private ModelUsageRecordService modelUsageRecordService;

    /**
     * 协议客户端注册表，按 {@code protocol} 路由到 OpenAI / Anthropic / Gemini 等实现。
     */
    @Resource
    private ModelClientRegistry modelClientRegistry;

    /**
     * 执行对话补全。
     * <p>
     * 主流程：解析 ACTIVE 的 LLM 明细 → 按协议创建 ChatModel → messages/tools 转 Prompt →
     * 同步 call → 尽力落库用量 → 转 OpenAI chat.completion 响应 VO。
     * 厂商 SDK / 网络异常收敛为远程服务错误；已知业务异常原样上抛。
     * </p>
     *
     * @param request OpenAI 风格 chat.completions 请求
     * @return OpenAI 风格 chat.completion 响应
     */
    @Override
    public ChatCompletionResponse chat(ChatCompletionRequest request) {
        // 1. 按 model 找到启用中的接入配置（含 baseUrl / apiKey / protocol 等）
        ModelEndpoint endpoint = requireActiveEndpoint(
            ModelType.LLM, request.getModel()
        );

        // 2. 按协议拿到厂商客户端，并创建可调用的 ChatModel
        ModelClient modelClient = modelClientRegistry.require(
            endpoint.protocol()
        );
        ChatModel chatModel = modelClient.createChatModel(endpoint);

        try {
            // 3. OpenAI messages -> Spring AI Message 列表，再按是否带 tools 组装 Prompt
            List<Message> messages = OpenAiChatServerConverter
                .toPrompt(request.getMessages())
                .getInstructions();

            Prompt prompt = buildPrompt(chatModel, messages, request);

            // 4. 同步调用大模型
            ChatResponse chatResponse = chatModel.call(prompt);

            // 5. 记录本次 prompt/completion token 用量（失败不影响返回）
            recordUsage(endpoint, chatResponse.getMetadata().getUsage());

            // 6. Spring AI 响应 -> OpenAI chat.completion 形态 VO
            return OpenAiChatServerConverter.fromChatResponse(
                chatResponse, request.getModel()
            );
        } catch (BusinessException ex) {
            // 已知业务异常（如协议不支持）直接向上抛
            throw ex;
        } catch (Exception ex) {
            log.error("模型调用失败, model={}", endpoint.modelName(), ex);
            throw remoteFailure(endpoint, ex);
        }
    }

    /**
     * 执行文本向量化。
     * <p>
     * 主流程：解析 ACTIVE 的 VECTOR 明细 → 创建 EmbeddingModel → 归一 input →
     * 同步 call → 尽力落库用量 → 组装 OpenAI embeddings 响应。
     * 不支持 Embedding 的协议会在创建阶段抛业务异常。
     * </p>
     *
     * @param request OpenAI 风格 embeddings 请求
     * @return OpenAI 风格 embeddings 响应
     */
    @Override
    public EmbeddingsResponse embed(EmbeddingsRequest request) {
        // 1. 解析启用配置并创建 EmbeddingModel（不支持 Embedding 的协议会在创建阶段抛错）
        ModelEndpoint endpoint = requireActiveEndpoint(
            ModelType.VECTOR, request.getModel()
        );

        ModelClient modelClient = modelClientRegistry.require(
            endpoint.protocol()
        );

        EmbeddingModel embeddingModel = modelClient.createEmbeddingModel(
            endpoint
        );

        try {
            // 2. 归一化 input（兼容 string / string[]），并按需附加 dimensions 与 encodingFormat
            List<String> inputs = request.resolveInputs();
            EmbeddingRequest embeddingRequest = buildRequest(
                inputs, request.getDimensions(), request.getEncodingFormat(), embeddingModel
            );

            // 3. 调用向量模型
            EmbeddingResponse response = embeddingModel.call(
                embeddingRequest
            );

            // 4. 落库用量
            Usage usage = response.getMetadata().getUsage();
            recordUsage(endpoint, usage);

            // 5. 将每条向量结果转为 OpenAI embeddings.data 元素
            List<EmbeddingData> data = new ArrayList<>();
            int index = 0;
            for (Embedding embedding : response.getResults()) {
                EmbeddingData item = new EmbeddingData();
                // OpenAI 约定：单条对象类型固定为 "embedding"
                item.setObject("embedding");
                item.setIndex(index++);
                item.setEmbedding(embedding.getOutput());
                data.add(item);
            }

            // 6. 组装顶层响应：object=list + model + data + usage
            EmbeddingsResponse vo = new EmbeddingsResponse();
            vo.setObject("list");
            vo.setModel(request.getModel());
            vo.setData(data);
            EmbeddingUsage usageVo = new EmbeddingUsage();
            usageVo.setPromptTokens(usage.getPromptTokens());
            usageVo.setTotalTokens(usage.getTotalTokens());
            vo.setUsage(usageVo);
            return vo;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Embedding 调用失败, model={}", endpoint.modelName(), ex);
            throw remoteFailure(endpoint, ex);
        }
    }

    /**
     * 执行文档重排序。
     * <p>
     * 主流程：解析 ACTIVE 的 RERANK 明细 → 创建 RerankModel →
     * 同步 call → 尽力落库用量 → 返回兼容响应。
     * 不支持 Rerank 的协议会在创建阶段抛业务异常。
     * </p>
     *
     * @param request OpenAI 兼容风格 rerank 请求
     * @return OpenAI 兼容风格 rerank 响应
     */
    @Override
    public RerankResponse rerank(RerankRequest request) {
        // 1. 解析启用配置并创建 RerankModel（不支持 Rerank 的协议会在创建阶段抛错）
        ModelEndpoint endpoint = requireActiveEndpoint(
            ModelType.RERANK, request.getModel()
        );

        ModelClient modelClient = modelClientRegistry.require(
            endpoint.protocol()
        );

        RerankModel rerankModel = modelClient.createRerankModel(endpoint);

        try {
            // 2. 调用重排序模型（协议层只回 results + usage）
            RerankCallResult callResult = rerankModel.call(request);

            // 3. 落库用量（厂商未回 usage 时为 null，recordUsage 直接跳过）
            recordUsage(endpoint, callResult.usage());

            // 4. 组装对外 VO；model 回显请求侧标识（路由键），与 embeddings 一致
            return OpenAiRerankConverter.toResponse(callResult, request.getModel());
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Rerank 调用失败, model={}", endpoint.modelName(), ex);
            throw remoteFailure(endpoint, ex);
        }
    }

    /**
     * 厂商调用失败：能识别上游状态码与错误码的原样透传，其余收敛为远程服务错误。
     *
     * @param endpoint 本次接入端点
     * @param ex       调用异常
     * @return 待抛出的异常
     */
    private static RuntimeException remoteFailure(ModelEndpoint endpoint, Exception ex) {
        UpstreamException upstream = UpstreamException.of(ex, endpoint.provider());
        if (Objects.nonNull(upstream)) {
            return upstream;
        }
        return new BusinessException(SystemErrorCode.REMOTE_SERVICE_ERROR, endpoint.provider(), ex.getMessage());
    }

    /**
     * 按模型类型与名称查询当前租户下启用中的接入端点。
     * <p>
     * 同时要求 profile 与 catalog 均为 {@link ModelStatus#ACTIVE}；未指定模型名时优先默认配置，否则取第一条；
     * 查无结果时抛出资源不存在。
     *
     * @param modelType 模型类型（LLM / VECTOR / RERANK）
     * @param modelName 请求中的 model 标识（对应目录 model_name），为空时不限定
     * @return 默认或首条匹配的接入端点（含 apiKey）
     */
    private ModelEndpoint requireActiveEndpoint(ModelType modelType, String modelName) {
        // 构造查询条件：精确 modelName + 仅 ACTIVE
        ModelServiceProfileSelectDto selectDto = new ModelServiceProfileSelectDto();
        if (Strings.isNotBlank(modelName)) {
            selectDto.setModelName(modelName);
        }

        selectDto.setModelType(modelType.name());
        selectDto.setStatus(ModelStatus.ACTIVE.name());
        selectDto.setCatalogStatus(ModelStatus.ACTIVE.name());
        selectDto.setOrganId(PrincipalContextHolder.getOrganId());
        List<ModelEndpoint> endpoints = modelServiceProfileService.selectEndpoints(selectDto);
        // 未配置或未启用则视为资源不存在
        Asserts.notEmpty(endpoints, SystemErrorCode.RESOURCE_NOT_FOUND, "model", modelName);
        if (Strings.isNotBlank(modelName)) {
            return endpoints.getFirst();
        }

        // 未指定模型：优先默认配置，没有默认则取第一条
        return endpoints.stream()
            .filter(ModelEndpoint::defaultFlag)
            .findFirst()
            .orElse(endpoints.getFirst());
    }

    /**
     * 将消息列表组装为 Spring AI {@link Prompt}；若请求携带 tools，则挂到 ToolCallingChatOptions。
     * <p>
     * tools 经 {@link OpenAiChatServerConverter#toToolCallbacks} 转为仅 schema 的回调；
     * 模型默认 options 必须实现 {@link ToolCallingChatOptions}，否则无法下发工具定义。
     *
     * @param chatModel 当前协议创建的 ChatModel
     * @param messages  已转换的 Spring AI 消息列表
     * @param request   原始 OpenAI 请求（取 tools）
     * @return 可直接 {@code call} 的 Prompt
     */
    private static Prompt buildPrompt(ChatModel chatModel, List<Message> messages, ChatCompletionRequest request) {
        // OpenAI tools[] -> 仅携带 schema 的 ToolCallback（本进程不执行工具）
        List<ToolCallback> toolCallbacks = OpenAiChatServerConverter.toToolCallbacks(request.getTools());
        if (CollectionUtils.isEmpty(toolCallbacks)) {
            // 无工具定义：纯消息 Prompt
            return new Prompt(messages);
        }

        // 有工具：必须能 mutate ToolCallingChatOptions，否则无法下发 tool schema
        ChatOptions options = chatModel.getOptions();
        if (!(options instanceof ToolCallingChatOptions opts)) {
            throw new BusinessException(
                SystemErrorCode.REMOTE_SERVICE_ERROR,
                "tools require ToolCallingChatOptions",
                chatModel.getClass().getName()
            );
        }

        // 在模型默认 options 上叠加本次请求的 toolCallbacks
        return new Prompt(messages, opts.mutate().toolCallbacks(toolCallbacks).build());
    }

    /**
     * 组装 Spring AI {@link EmbeddingRequest}。
     * <p>
     * 仅 {@link OpenAiEmbeddingModel} 会附加自定义 options：正数 dimensions 时覆盖维度，
     * 并写入编码格式。编码格式缺省为 {@code base64}。其它实现不附加自定义 options。
     *
     * @param inputs         已归一的文本列表
     * @param dimensions     可选维度覆盖
     * @param encodingFormat 可选编码格式，{@code float} 或 {@code base64}
     * @param embeddingModel 当前 Embedding 客户端
     * @return EmbeddingRequest
     */
    private static EmbeddingRequest buildRequest(List<String> inputs, Integer dimensions, String encodingFormat, EmbeddingModel embeddingModel) {
        if (!(embeddingModel instanceof OpenAiEmbeddingModel openAi)) {
            return new EmbeddingRequest(inputs, null);
        }

        OpenAiEmbeddingOptions.Builder builder = OpenAiEmbeddingOptions.builder();
        builder.from(openAi.getOptions());
        if (Objects.nonNull(dimensions) && dimensions > 0) {
            builder.dimensions(dimensions);
        }
        builder.encodingFormat(resolveEncodingFormat(encodingFormat));
        return new EmbeddingRequest(inputs, builder.build());
    }

    /**
     * 将请求中的编码格式解析为 Spring AI 枚举。空白视为缺省 {@code base64}。
     *
     * @param encodingFormat 请求字段，可为 null
     * @return float 或 base64
     */
    static OpenAiEmbeddingOptions.EncodingFormat resolveEncodingFormat(String encodingFormat) {
        if (Strings.isBlank(encodingFormat)) {
            return OpenAiEmbeddingOptions.EncodingFormat.BASE64;
        }
        if ("float".equalsIgnoreCase(encodingFormat.trim())) {
            return OpenAiEmbeddingOptions.EncodingFormat.FLOAT;
        }
        if ("base64".equalsIgnoreCase(encodingFormat.trim())) {
            return OpenAiEmbeddingOptions.EncodingFormat.BASE64;
        }
        throw new BusinessException(SystemErrorCode.PARAM_VAL_INVALID, "encodingFormat");
    }

    /**
     * 将本次调用的 token 用量写入用量表。
     * <p>
     * {@code usage} 为空时直接返回；落库异常只打 warn，不抛出，避免影响主调用结果。
     *
     * @param endpoint 本次使用的接入端点
     * @param usage    Spring AI 用量元数据，可为 null
     */
    private void recordUsage(ModelEndpoint endpoint, Usage usage) {
        if (Objects.isNull(usage)) {
            // 部分厂商/兼容网关可能不回 usage
            return;
        }

        try {
            // 配置字段 + usage token -> 用量 DTO，再补当前租户 organId
            ModelUsageRecordConverter instance = ModelUsageRecordConverter.INSTANCE;
            ModelUsageRecordDto usageRecord = instance.fromProfileAndUsage(
                endpoint, usage
            );
            usageRecord.setOrganId(PrincipalContextHolder.getOrganId());
            modelUsageRecordService.save(usageRecord);
        } catch (Exception ex) {
            // 用量落库失败只告警，避免拖垮主调用
            log.warn("模型用量记录落库失败, model={}", endpoint.modelName(), ex);
        }
    }
}
