package com.g2rain.model.nexus.api;

import com.g2rain.common.model.PageData;
import com.g2rain.common.model.PageSelectListDto;
import com.g2rain.common.model.Result;
import com.g2rain.model.nexus.dto.ModelServiceCatalogSelectDto;
import com.g2rain.model.nexus.vo.ModelServiceCatalogVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;


/**
 * 模型服务目录API接口
 * 表名: model_service_catalog
 *
 * @author G2rain Generator
 */
@Tag(name = "模型服务目录", description = "模型服务目录相关接口")
public interface ModelServiceCatalogApi {

    /**
     * 根据条件查询列表
     *
     * @param selectDto 查询条件 DTO
     * @return 数据列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询模型服务目录列表", description = "根据查询条件返回模型服务目录列表")
    Result<List<ModelServiceCatalogVo>> selectList(ModelServiceCatalogSelectDto selectDto);

    /**
     * 根据条件分页查询
     *
     * @param selectDto 查询条件DTO（包含分页参数）
     * @return 分页数据
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询模型服务目录列表", description = "分页查询模型服务目录列表")
    Result<PageData<ModelServiceCatalogVo>> selectPage(PageSelectListDto<ModelServiceCatalogSelectDto> selectDto);
}