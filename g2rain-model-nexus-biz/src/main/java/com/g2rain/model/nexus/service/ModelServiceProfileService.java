package com.g2rain.model.nexus.service;

import com.g2rain.common.model.PageData;
import com.g2rain.common.model.PageSelectListDto;
import com.g2rain.model.nexus.dto.ModelServiceProfileDto;
import com.g2rain.model.nexus.dto.ModelServiceProfileSelectDto;
import com.g2rain.model.nexus.dto.UpdateStatusDto;
import com.g2rain.model.nexus.protocol.ModelEndpoint;
import com.g2rain.model.nexus.vo.ModelServiceDetailVO;
import com.g2rain.model.nexus.vo.ModelServiceOptionVo;

import java.util.List;

/**
 * 模型服务配置服务接口
 * 表名: model_service_profile
 *
 * @author G2rain Generator
 */
public interface ModelServiceProfileService {

    /**
     * 根据条件查询列表
     *
     * @param selectDto 查询条件 DTO
     * @return VO 对象列表
     */
    List<ModelServiceDetailVO> selectList(ModelServiceProfileSelectDto selectDto);

    /**
     * 内部调用：按条件查询接入端点（含 apiKey），不对外暴露。
     *
     * @param selectDto 查询条件 DTO
     * @return 接入端点列表
     */
    List<ModelEndpoint> selectEndpoints(ModelServiceProfileSelectDto selectDto);

    /**
     * 查询 ACTIVE 模型下拉选项（仅 id + modelName）。
     *
     * @return ACTIVE 模型选项列表
     */
    List<ModelServiceOptionVo> selectActiveOptions();

    /**
     * 根据条件分页查询
     *
     * @param selectDto 查询条件DTO（包含分页参数）
     * @return 分页 VO 数据
     */
    PageData<ModelServiceDetailVO> selectPage(PageSelectListDto<ModelServiceProfileSelectDto> selectDto);

    /**
     * 新增或更新数据
     *
     * @param dto 数据传输对象
     * @return 操作结果（影响行数）
     */
    Long save(ModelServiceProfileDto dto);

    /**
     * 根据 ID 删除数据
     *
     * @param id 主键 ID
     * @return 操作结果（影响行数）
     */
    int delete(Long id);

    /**
     * 修改配置状态
     *
     * @param id  主键 ID
     * @param dto 状态
     * @return 影响行数
     */
    int updateStatus(Long id, UpdateStatusDto dto);
}
