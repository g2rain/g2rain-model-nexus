package com.g2rain.model.nexus.controller;

import com.g2rain.common.model.PageData;
import com.g2rain.common.model.PageSelectListDto;
import com.g2rain.common.model.Result;
import com.g2rain.model.nexus.api.ModelServiceProfileApi;
import com.g2rain.model.nexus.dto.ModelServiceProfileDto;
import com.g2rain.model.nexus.dto.ModelServiceProfileSelectDto;
import com.g2rain.model.nexus.dto.UpdateStatusDto;
import com.g2rain.model.nexus.service.ModelServiceProfileService;
import com.g2rain.model.nexus.vo.ModelServiceDetailVO;
import com.g2rain.model.nexus.vo.ModelServiceOptionVo;
import com.g2rain.web.interceptors.annotations.IdentityInject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 模型服务配置控制器
 * 表名: model_service_profile
 *
 * @author G2rain Generator
 */
@RestController
@RequestMapping("/model_service_profile")
public class ModelServiceProfileController implements ModelServiceProfileApi {

    @Resource(name = "modelServiceProfileServiceImpl")
    private ModelServiceProfileService modelServiceProfileService;

    @Override
    @IdentityInject(organIdPropertyName = "organId")
    public Result<List<ModelServiceDetailVO>> selectList(ModelServiceProfileSelectDto selectDto) {
        return Result.success(modelServiceProfileService.selectList(selectDto));
    }

    @Override
    @IdentityInject(organIdPropertyName = "organId")
    public Result<List<ModelServiceOptionVo>> selectActiveOptions() {
        return Result.success(modelServiceProfileService.selectActiveOptions());
    }

    @Override
    @IdentityInject(organIdPropertyName = "organId")
    public Result<PageData<ModelServiceDetailVO>> selectPage(PageSelectListDto<ModelServiceProfileSelectDto> selectDto) {
        return Result.successPage(modelServiceProfileService.selectPage(selectDto));
    }

    @PostMapping("/save")
    @IdentityInject(organIdPropertyName = "organId")
    @Operation(summary = "新增或更新模型服务配置", description = "引用模型广场或同事务接入私有模型")
    public Result<Long> save(@RequestBody ModelServiceProfileDto dto) {
        return Result.success(modelServiceProfileService.save(dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除模型服务配置记录", description = "私有来源会级联删除 CUSTOM 目录")
    public Result<Integer> delete(@Parameter(description = "模型服务配置标识") @PathVariable Long id) {
        return Result.success(modelServiceProfileService.delete(id));
    }

    @PostMapping("/{id}/status")
    @Operation(summary = "修改模型配置状态", description = "启用或禁用模型配置")
    public Result<Integer> updateStatus(@Parameter(description = "模型服务配置标识") @PathVariable Long id, @RequestBody @Validated UpdateStatusDto dto) {
        return Result.success(modelServiceProfileService.updateStatus(id, dto));
    }
}
