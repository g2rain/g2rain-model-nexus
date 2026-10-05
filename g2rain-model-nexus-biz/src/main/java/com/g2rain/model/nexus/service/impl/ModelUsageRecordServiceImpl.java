package com.g2rain.model.nexus.service.impl;

import com.g2rain.common.exception.SystemErrorCode;
import com.g2rain.common.id.IdGenerator;
import com.g2rain.common.model.PageData;
import com.g2rain.common.model.PageSelectListDto;
import com.g2rain.common.utils.Asserts;
import com.g2rain.common.utils.Moments;
import com.g2rain.common.validation.Validations;
import com.g2rain.model.nexus.converter.ModelUsageRecordConverter;
import com.g2rain.model.nexus.dao.ModelUsageRecordDao;
import com.g2rain.model.nexus.dao.po.ModelUsageRecordPo;
import com.g2rain.model.nexus.dto.ModelUsageRecordDto;
import com.g2rain.model.nexus.dto.ModelUsageRecordSelectDto;
import com.g2rain.model.nexus.service.ModelUsageRecordService;
import com.g2rain.model.nexus.vo.ModelUsageRecordVo;
import com.g2rain.mybatis.pagination.PageContext;
import com.g2rain.mybatis.pagination.model.Page;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 模型用量记录服务实现类
 * 表名: model_usage_record
 *
 * @author G2rain Generator
 */
@Service(value = "modelUsageRecordServiceImpl")
public class ModelUsageRecordServiceImpl implements ModelUsageRecordService {

    @Resource(name = "modelUsageRecordDao")
    private ModelUsageRecordDao modelUsageRecordDao;

    private IdGenerator idGenerator;

    @Qualifier("idGenerator")
    @Autowired(required = false)
    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    public List<ModelUsageRecordVo> selectList(ModelUsageRecordSelectDto selectDto) {
        return modelUsageRecordDao.selectList(selectDto)
            .stream()
            .map(ModelUsageRecordConverter.INSTANCE::po2vo)
            .toList();
    }

    @Override
    public PageData<ModelUsageRecordVo> selectPage(PageSelectListDto<ModelUsageRecordSelectDto> selectDto) {
        Page<ModelUsageRecordPo> page = PageContext.of(selectDto.getPageNum(), selectDto.getPageSize(), () -> {
            modelUsageRecordDao.selectList(selectDto.getQuery());
        });
        List<ModelUsageRecordVo> result = page.getResult()
            .stream()
            .map(ModelUsageRecordConverter.INSTANCE::po2vo)
            .toList();
        return PageData.of(page.getPageNum(), page.getPageSize(), page.getTotal(), result);
    }

    @Override
    public Long save(ModelUsageRecordDto dto) {
        Validations.validateSave(dto);

        // 转换 DTO 为 PO
        ModelUsageRecordPo entity = ModelUsageRecordConverter.INSTANCE.dto2po(dto);

        // 判断是新增还是更新
        Long id = entity.getId();
        if (Objects.isNull(id) || id == 0) {
            // 新增：使用IdGenerator生成主键
            entity.setId(idGenerator.generateId());
            LocalDateTime now = Moments.now();
            entity.setUpdateTime(now);
            entity.setCreateTime(now);
            int success = modelUsageRecordDao.insert(entity);
            Asserts.greaterThan(success, 0, SystemErrorCode.CREATE_DATA_ERROR);
        } else {
            ModelUsageRecordPo existing = modelUsageRecordDao.selectById(id);
            Asserts.isTrue(Objects.nonNull(existing), SystemErrorCode.DATA_NOT_EXISTS, id);
            // 更新：直接更新
            entity.setUpdateTime(Moments.now());
            int success = modelUsageRecordDao.update(entity);
            Asserts.greaterThan(success, 0, SystemErrorCode.UPDATE_DATA_ERROR, id);
        }

        return entity.getId();
    }

    @Override
    public int delete(Long id) {
        ModelUsageRecordPo existing = modelUsageRecordDao.selectById(id);
        Asserts.isTrue(Objects.nonNull(existing), SystemErrorCode.DATA_NOT_EXISTS, id);
        return modelUsageRecordDao.delete(id);
    }
}
