package com.g2rain.model.nexus.api;

import com.g2rain.common.model.PageData;
import com.g2rain.common.model.PageSelectListDto;
import com.g2rain.common.model.Result;
import com.g2rain.model.nexus.dto.ModelServiceProfileSelectDto;
import com.g2rain.model.nexus.vo.ModelServiceDetailVO;
import com.g2rain.model.nexus.vo.ModelServiceOptionVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;


/**
 * 模型服务配置API接口
 * 表名: model_service_profile
 *
 * @author G2rain Generator
 */
@Tag(name = "模型服务配置", description = "模型服务配置相关接口")
public interface ModelServiceProfileApi {

    /**
     * 根据条件查询列表
     *
     * @param selectDto 查询条件 DTO
     * @return 数据列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询模型服务配置列表", description = "根据查询条件返回模型服务配置列表")
    Result<List<ModelServiceDetailVO>> selectList(ModelServiceProfileSelectDto selectDto);

    /**
     * 查询 ACTIVE 模型下拉选项（仅 id + modelName）。
     *
     * @return ACTIVE 模型选项列表
     */
    @GetMapping("/options")
    @Operation(summary = "查询 ACTIVE 模型下拉选项", description = "按租户隔离查询 ACTIVE 配置，仅返回 id 与 modelName")
    Result<List<ModelServiceOptionVo>> selectActiveOptions();

    /**
     * 根据条件分页查询
     *
     * @param selectDto 查询条件DTO（包含分页参数）
     * @return 分页数据
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询模型服务配置列表", description = "分页查询模型服务配置列表")
    Result<PageData<ModelServiceDetailVO>> selectPage(PageSelectListDto<ModelServiceProfileSelectDto> selectDto);
}
