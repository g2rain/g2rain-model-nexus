package com.g2rain.model.nexus.service;

import com.g2rain.common.model.PageData;
import com.g2rain.common.model.PageSelectListDto;
import com.g2rain.model.nexus.dto.ModelServiceCatalogDto;
import com.g2rain.model.nexus.dto.ModelServiceCatalogSelectDto;
import com.g2rain.model.nexus.dto.UpdateStatusDto;
import com.g2rain.model.nexus.enums.ResourceType;
import com.g2rain.model.nexus.vo.ModelServiceCatalogVo;

import java.util.List;

/**
 * 模型服务目录服务接口
 * 表名: model_service_catalog
 *
 * @author G2rain Generator
 */
public interface ModelServiceCatalogService {

    /**
     * 根据条件查询列表
     *
     * @param selectDto 查询条件 DTO
     * @return VO 对象列表
     */
    List<ModelServiceCatalogVo> selectList(ModelServiceCatalogSelectDto selectDto);

    /**
     * 根据条件分页查询
     *
     * @param selectDto 查询条件DTO（包含分页参数）
     * @return 分页 VO 数据
     */
    PageData<ModelServiceCatalogVo> selectPage(PageSelectListDto<ModelServiceCatalogSelectDto> selectDto);

    /**
     * 新增或更新数据。
     * <p>
     * 调用方传入期望 {@code resourceType}；更新时库内类型必须一致，否则视为不存在。
     *
     * @param dto          数据传输对象
     * @param resourceType 期望类型（目录接口传 MARKET，profile 传 CUSTOM）
     * @return 主键 ID
     */
    Long save(ModelServiceCatalogDto dto, ResourceType resourceType);

    /**
     * 按期望资源类型删除。
     * <p>
     * 库内 {@code resourceType} 必须与 {@code resourceType} 一致。
     *
     * @param id           主键 ID
     * @param resourceType 期望类型（目录接口传 MARKET，profile 传 CUSTOM）
     * @return 影响行数
     */
    int delete(Long id, ResourceType resourceType);

    /**
     * 按期望资源类型修改状态。
     *
     * @param id           主键 ID
     * @param dto          状态
     * @param resourceType 期望类型
     * @return 影响行数
     */
    int updateStatus(Long id, UpdateStatusDto dto, ResourceType resourceType);
}