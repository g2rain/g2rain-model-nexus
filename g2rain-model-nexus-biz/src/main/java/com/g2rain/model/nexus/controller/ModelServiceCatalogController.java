package com.g2rain.model.nexus.controller;

import com.g2rain.common.model.PageData;
import com.g2rain.common.model.PageSelectListDto;
import com.g2rain.common.model.Result;
import com.g2rain.model.nexus.api.ModelServiceCatalogApi;
import com.g2rain.model.nexus.dto.ModelServiceCatalogDto;
import com.g2rain.model.nexus.dto.ModelServiceCatalogSelectDto;
import com.g2rain.model.nexus.service.ModelServiceCatalogService;
import com.g2rain.model.nexus.vo.ModelServiceCatalogVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 模型服务目录控制器
 * 表名: model_service_catalog
 *
 * @author G2rain Generator
 */
@RestController
@RequestMapping("/model_service_catalog")
public class ModelServiceCatalogController implements ModelServiceCatalogApi {

    @Resource(name = "modelServiceCatalogServiceImpl")
    private ModelServiceCatalogService modelServiceCatalogService;

    @Override
    public Result<List<ModelServiceCatalogVo>> selectList(ModelServiceCatalogSelectDto selectDto) {
        return Result.success(modelServiceCatalogService.selectList(selectDto));
    }

    @Override
    public Result<PageData<ModelServiceCatalogVo>> selectPage(PageSelectListDto<ModelServiceCatalogSelectDto> selectDto) {
        return Result.successPage(modelServiceCatalogService.selectPage(selectDto));
    }

    @PostMapping("/save")
    @Operation(summary = "新增或更新模型服务目录信息", description = "新增或更新模型服务目录基础信息")
    public Result<Long> save(@RequestBody ModelServiceCatalogDto dto) {
        return Result.success(modelServiceCatalogService.save(dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除模型服务目录记录", description = "根据主键删除模型服务目录记录")
    public Result<Integer> delete(@Parameter(description = "模型服务目录标识") @PathVariable Long id) {
        return Result.success(modelServiceCatalogService.delete(id));
    }
}