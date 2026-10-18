package com.g2rain.model.nexus.converter;

import com.g2rain.common.converter.CommonConverter;
import com.g2rain.common.utils.Strings;
import com.g2rain.model.nexus.dao.po.ModelServiceCatalogPo;
import com.g2rain.model.nexus.dto.ModelServiceCatalogDto;
import com.g2rain.model.nexus.vo.ModelServiceCatalogVo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

/**
 * 模型服务目录转换器
 * 用于Po、Vo、Dto之间的相互转换
 * 表名: model_service_catalog
 *
 * @author G2rain Generator
 */
@Mapper(uses = CommonConverter.class)
public interface ModelServiceCatalogConverter {

    /**
     * 单例实例，通过 {@link Mappers#getMapper(Class)} 获取 MapStruct 自动生成的实现。
     */
    ModelServiceCatalogConverter INSTANCE = Mappers.getMapper(ModelServiceCatalogConverter.class);

    /**
     * Po -> Vo
     * 自动将 createTime 和 updateTime 从 {@link LocalDateTime} 转换为 {@link String}
     * apiKey 不落 VO，仅映射是否已配置
     */
    @Mapping(target = "apiKeyConfigured", source = "apiKey", qualifiedByName = "apiKeyToConfigured")
    @Mapping(target = "createTime", source = "createTime", qualifiedByName = "localDateTimeToString")
    @Mapping(target = "updateTime", source = "updateTime", qualifiedByName = "localDateTimeToString")
    ModelServiceCatalogVo po2vo(ModelServiceCatalogPo po);

    /**
     * 访问密钥是否已配置（不回传明文）
     */
    @Named("apiKeyToConfigured")
    default Boolean apiKeyToConfigured(String apiKey) {
        return Strings.isNotBlank(apiKey);
    }

    /**
     * Dto -> Po
     * 自动将 createTime 和 updateTime 从 {@link String} 转换为 {@link LocalDateTime}
     * 忽略 version 字段
     * 忽略 deleteFlag 字段
     */
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "deleteFlag", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createTime", source = "createTime", qualifiedByName = "stringToLocalDateTime")
    @Mapping(target = "updateTime", source = "updateTime", qualifiedByName = "stringToLocalDateTime")
    ModelServiceCatalogPo dto2po(ModelServiceCatalogDto dto);
}
