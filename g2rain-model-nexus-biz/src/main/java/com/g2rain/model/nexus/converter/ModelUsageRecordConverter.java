package com.g2rain.model.nexus.converter;

import com.g2rain.common.converter.CommonConverter;
import com.g2rain.model.nexus.dao.po.ModelUsageRecordPo;
import com.g2rain.model.nexus.dto.ModelUsageRecordDto;
import com.g2rain.model.nexus.protocol.ModelEndpoint;
import com.g2rain.model.nexus.vo.ModelUsageRecordVo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.ai.chat.metadata.Usage;

import java.time.LocalDateTime;

/**
 * 模型用量记录转换器
 * 用于Po、Vo、Dto之间的相互转换
 * 表名: model_usage_record
 *
 * @author G2rain Generator
 */
@Mapper(uses = CommonConverter.class)
public interface ModelUsageRecordConverter {

    /**
     * 单例实例，通过 {@link Mappers#getMapper(Class)} 获取 MapStruct 自动生成的实现。
     */
    ModelUsageRecordConverter INSTANCE = Mappers.getMapper(ModelUsageRecordConverter.class);

    /**
     * Po -> Vo
     * 自动将 createTime 和 updateTime 从 {@link LocalDateTime} 转换为 {@link String}
     */
    @Mapping(target = "createTime", source = "createTime", qualifiedByName = "localDateTimeToString")
    @Mapping(target = "updateTime", source = "updateTime", qualifiedByName = "localDateTimeToString")
    ModelUsageRecordVo po2vo(ModelUsageRecordPo po);

    /**
     * Dto -> Po
     * 自动将 createTime 和 updateTime 从 {@link String} 转换为 {@link LocalDateTime}
     * 忽略 version 字段
     */
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createTime", source = "createTime", qualifiedByName = "stringToLocalDateTime")
    @Mapping(target = "updateTime", source = "updateTime", qualifiedByName = "stringToLocalDateTime")
    ModelUsageRecordPo dto2po(ModelUsageRecordDto dto);

    /**
     * 将接入配置与一次调用的 token 用量组装为待落库的用量 DTO。
     * <p>
     * 映射关系：
     * <ul>
     *   <li>endpoint.profileId -> profileId</li>
     *   <li>endpoint.catalogId -> catalogId</li>
     *   <li>endpoint.provider / protocol / modelName 原样带入</li>
     *   <li>usage 的 prompt / completion / total tokens 原样带入</li>
     * </ul>
     * 忽略 id、时间戳与 organId：id/时间由保存流程生成，organId 由调用方从
     * {@code PrincipalContextHolder} 写入，避免 Converter 隐式依赖 Web 上下文。
     *
     * @param endpoint 本次调用使用的模型接入端点
     * @param usage    Spring AI 返回的用量信息
     * @return 用量记录 DTO（尚未持久化）
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "organId", ignore = true)
    @Mapping(target = "profileId", source = "endpoint.profileId")
    @Mapping(target = "catalogId", source = "endpoint.catalogId")
    @Mapping(target = "provider", source = "endpoint.provider")
    @Mapping(target = "protocol", source = "endpoint.protocol")
    @Mapping(target = "modelName", source = "endpoint.modelName")
    @Mapping(target = "promptTokens", source = "usage.promptTokens")
    @Mapping(target = "completionTokens", source = "usage.completionTokens")
    @Mapping(target = "totalTokens", source = "usage.totalTokens")
    ModelUsageRecordDto fromProfileAndUsage(ModelEndpoint endpoint, Usage usage);
}
