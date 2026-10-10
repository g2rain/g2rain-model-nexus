package com.g2rain.model.nexus.converter;

import com.g2rain.common.converter.CommonConverter;
import com.g2rain.model.nexus.dao.po.ModelServiceDetailPo;
import com.g2rain.model.nexus.dao.po.ModelServiceProfilePo;
import com.g2rain.model.nexus.dto.ModelServiceProfileDto;
import com.g2rain.model.nexus.vo.ModelServiceDetailVO;
import com.g2rain.model.nexus.vo.ModelServiceOptionVo;
import com.g2rain.model.nexus.vo.ModelServiceProfileVo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

/**
 * 模型服务配置转换器
 * 用于Po、Vo、Dto之间的相互转换
 * 表名: model_service_profile
 *
 * @author G2rain Generator
 */
@Mapper(uses = CommonConverter.class)
public interface ModelServiceProfileConverter {

    /**
     * 单例实例，通过 {@link Mappers#getMapper(Class)} 获取 MapStruct 自动生成的实现。
     */
    ModelServiceProfileConverter INSTANCE = Mappers.getMapper(ModelServiceProfileConverter.class);

    /**
     * Po -> Vo
     * 自动将 createTime 和 updateTime 从 {@link LocalDateTime} 转换为 {@link String}
     */
    @Mapping(target = "createTime", source = "createTime", qualifiedByName = "localDateTimeToString")
    @Mapping(target = "updateTime", source = "updateTime", qualifiedByName = "localDateTimeToString")
    ModelServiceProfileVo po2vo(ModelServiceProfilePo po);

    /**
     * DetailPo -> DetailVO
     * 自动将 createTime 和 updateTime 从 {@link LocalDateTime} 转换为 {@link String}
     */
    @Mapping(target = "createTime", source = "createTime", qualifiedByName = "localDateTimeToString")
    @Mapping(target = "updateTime", source = "updateTime", qualifiedByName = "localDateTimeToString")
    ModelServiceDetailVO po2detailVo(ModelServiceDetailPo po);

    /**
     * DetailPo → 下拉选项（仅 id + modelName）
     */
    ModelServiceOptionVo po2option(ModelServiceDetailPo po);

    /**
     * Dto -> Po
     * 自动将 createTime 和 updateTime 从 {@link String} 转换为 {@link LocalDateTime}
     * 忽略 version 字段
     * 忽略 deleteFlag 字段
     */
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "deleteFlag", ignore = true)
    @Mapping(target = "createTime", source = "createTime", qualifiedByName = "stringToLocalDateTime")
    @Mapping(target = "updateTime", source = "updateTime", qualifiedByName = "stringToLocalDateTime")
    ModelServiceProfilePo dto2po(ModelServiceProfileDto dto);
}
