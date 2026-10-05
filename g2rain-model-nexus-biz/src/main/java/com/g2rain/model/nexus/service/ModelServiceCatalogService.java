package com.g2rain.model.nexus.service;

import com.g2rain.common.model.PageData;
import com.g2rain.common.model.PageSelectListDto;
import com.g2rain.model.nexus.dto.ModelServiceCatalogDto;
import com.g2rain.model.nexus.dto.ModelServiceCatalogSelectDto;
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
     * 新增或更新数据
     *
     * @param dto 数据传输对象
     * @return 操作结果（影响行数）
     */
    Long save(ModelServiceCatalogDto dto);

    /**
     * 根据 ID 删除数据
     *
     * @param id 主键 ID
     * @return 操作结果（影响行数）
     */
    int delete(Long id);
}