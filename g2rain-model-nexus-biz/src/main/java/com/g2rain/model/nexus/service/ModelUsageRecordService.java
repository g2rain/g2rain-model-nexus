package com.g2rain.model.nexus.service;

import com.g2rain.common.model.PageData;
import com.g2rain.common.model.PageSelectListDto;
import com.g2rain.model.nexus.dto.ModelUsageRecordDto;
import com.g2rain.model.nexus.dto.ModelUsageRecordSelectDto;
import com.g2rain.model.nexus.vo.ModelUsageRecordVo;

import java.util.List;

/**
 * 模型用量记录服务接口
 * 表名: model_usage_record
 *
 * @author G2rain Generator
 */
public interface ModelUsageRecordService {

    /**
     * 根据条件查询列表
     *
     * @param selectDto 查询条件 DTO
     * @return VO 对象列表
     */
    List<ModelUsageRecordVo> selectList(ModelUsageRecordSelectDto selectDto);

    /**
     * 根据条件分页查询
     *
     * @param selectDto 查询条件DTO（包含分页参数）
     * @return 分页 VO 数据
     */
    PageData<ModelUsageRecordVo> selectPage(PageSelectListDto<ModelUsageRecordSelectDto> selectDto);

    /**
     * 新增或更新数据
     *
     * @param dto 数据传输对象
     * @return 操作结果（影响行数）
     */
    Long save(ModelUsageRecordDto dto);

    /**
     * 根据 ID 删除数据
     *
     * @param id 主键 ID
     * @return 操作结果（影响行数）
     */
    int delete(Long id);
}