package com.g2rain.model.nexus.controller;

import com.g2rain.common.model.PageData;
import com.g2rain.common.model.PageSelectListDto;
import com.g2rain.common.model.Result;
import com.g2rain.model.nexus.api.ModelUsageRecordApi;
import com.g2rain.model.nexus.dto.ModelUsageRecordDto;
import com.g2rain.model.nexus.dto.ModelUsageRecordSelectDto;
import com.g2rain.model.nexus.service.ModelUsageRecordService;
import com.g2rain.model.nexus.vo.ModelUsageRecordVo;
import com.g2rain.web.interceptors.annotations.IdentityInject;
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
 * 模型用量记录控制器
 * 表名: model_usage_record
 *
 * @author G2rain Generator
 */
@RestController
@RequestMapping("/model_usage_record")
public class ModelUsageRecordController implements ModelUsageRecordApi {

    @Resource(name = "modelUsageRecordServiceImpl")
    private ModelUsageRecordService modelUsageRecordService;

    @Override
    @IdentityInject(organIdPropertyName = "organId")
    public Result<List<ModelUsageRecordVo>> selectList(ModelUsageRecordSelectDto selectDto) {
        return Result.success(modelUsageRecordService.selectList(selectDto));
    }

    @Override
    @IdentityInject(organIdPropertyName = "organId")
    public Result<PageData<ModelUsageRecordVo>> selectPage(PageSelectListDto<ModelUsageRecordSelectDto> selectDto) {
        return Result.successPage(modelUsageRecordService.selectPage(selectDto));
    }

    @PostMapping("/save")
    @Operation(summary = "新增或更新模型用量记录信息", description = "新增或更新模型用量记录基础信息")
    public Result<Long> save(@RequestBody ModelUsageRecordDto dto) {
        return Result.success(modelUsageRecordService.save(dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除模型用量记录记录", description = "根据主键删除模型用量记录记录")
    public Result<Integer> delete(@Parameter(description = "模型用量记录标识") @PathVariable Long id) {
        return Result.success(modelUsageRecordService.delete(id));
    }
}
