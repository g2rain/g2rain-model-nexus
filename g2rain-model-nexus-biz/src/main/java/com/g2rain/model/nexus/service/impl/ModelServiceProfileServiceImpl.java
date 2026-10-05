package com.g2rain.model.nexus.service.impl;

import com.g2rain.common.exception.SystemErrorCode;
import com.g2rain.common.id.IdGenerator;
import com.g2rain.common.model.PageData;
import com.g2rain.common.model.PageSelectListDto;
import com.g2rain.common.utils.Asserts;
import com.g2rain.common.utils.Moments;
import com.g2rain.common.validation.Validations;
import com.g2rain.model.nexus.converter.ModelServiceProfileConverter;
import com.g2rain.model.nexus.dao.ModelServiceProfileDao;
import com.g2rain.model.nexus.dao.po.ModelServiceDetailPo;
import com.g2rain.model.nexus.dao.po.ModelServiceProfilePo;
import com.g2rain.model.nexus.dto.ModelServiceProfileDto;
import com.g2rain.model.nexus.dto.ModelServiceProfileSelectDto;
import com.g2rain.model.nexus.service.ModelServiceProfileService;
import com.g2rain.model.nexus.vo.ModelServiceDetailVO;
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
 * 模型服务配置服务实现类
 * 表名: model_service_profile
 *
 * @author G2rain Generator
 */
@Service(value = "modelServiceProfileServiceImpl")
public class ModelServiceProfileServiceImpl implements ModelServiceProfileService {

    @Resource(name = "modelServiceProfileDao")
    private ModelServiceProfileDao modelServiceProfileDao;

    private IdGenerator idGenerator;

    @Qualifier("idGenerator")
    @Autowired(required = false)
    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    public List<ModelServiceDetailVO> selectList(ModelServiceProfileSelectDto selectDto) {
        return modelServiceProfileDao.selectList(selectDto)
            .stream()
            .map(ModelServiceProfileConverter.INSTANCE::po2detailVo)
            .toList();
    }

    @Override
    public PageData<ModelServiceDetailVO> selectPage(PageSelectListDto<ModelServiceProfileSelectDto> selectDto) {
        Page<ModelServiceDetailPo> page = PageContext.of(selectDto.getPageNum(), selectDto.getPageSize(), () ->
            modelServiceProfileDao.selectList(selectDto.getQuery())
        );

        List<ModelServiceDetailVO> result = page.getResult()
            .stream()
            .map(ModelServiceProfileConverter.INSTANCE::po2detailVo)
            .toList();
        return PageData.of(page.getPageNum(), page.getPageSize(), page.getTotal(), result);
    }

    @Override
    public Long save(ModelServiceProfileDto dto) {
        Validations.validateSave(dto);

        // 转换 DTO 为 PO
        ModelServiceProfilePo entity = ModelServiceProfileConverter.INSTANCE.dto2po(dto);

        // 判断是新增还是更新
        Long id = entity.getId();
        if (Objects.isNull(id) || id == 0) {
            // 新增：使用IdGenerator生成主键
            entity.setId(idGenerator.generateId());
            LocalDateTime now = Moments.now();
            entity.setUpdateTime(now);
            entity.setCreateTime(now);
            int success = modelServiceProfileDao.insert(entity);
            Asserts.greaterThan(success, 0, SystemErrorCode.CREATE_DATA_ERROR);
        } else {
            ModelServiceDetailPo existing = modelServiceProfileDao.selectById(id);
            Asserts.isTrue(Objects.nonNull(existing), SystemErrorCode.DATA_NOT_EXISTS, id);
            // 更新：直接更新
            entity.setUpdateTime(Moments.now());
            int success = modelServiceProfileDao.update(entity);
            Asserts.greaterThan(success, 0, SystemErrorCode.UPDATE_DATA_ERROR, id);
        }

        return entity.getId();
    }

    @Override
    public int delete(Long id) {
        ModelServiceDetailPo existing = modelServiceProfileDao.selectById(id);
        Asserts.isTrue(Objects.nonNull(existing), SystemErrorCode.DATA_NOT_EXISTS, id);
        return modelServiceProfileDao.delete(id);
    }
}
