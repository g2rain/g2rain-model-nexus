package com.g2rain.model.nexus.service.impl;

import com.g2rain.common.exception.SystemErrorCode;
import com.g2rain.common.id.IdGenerator;
import com.g2rain.common.model.PageData;
import com.g2rain.common.model.PageSelectListDto;
import com.g2rain.common.utils.Asserts;
import com.g2rain.common.utils.Moments;
import com.g2rain.common.validation.Validations;
import com.g2rain.model.nexus.converter.ModelServiceCatalogConverter;
import com.g2rain.model.nexus.dao.ModelServiceCatalogDao;
import com.g2rain.model.nexus.dao.po.ModelServiceCatalogPo;
import com.g2rain.model.nexus.dto.ModelServiceCatalogDto;
import com.g2rain.model.nexus.dto.ModelServiceCatalogSelectDto;
import com.g2rain.model.nexus.service.ModelServiceCatalogService;
import com.g2rain.model.nexus.vo.ModelServiceCatalogVo;
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
 * 模型服务目录服务实现类
 * 表名: model_service_catalog
 *
 * @author G2rain Generator
 */
@Service(value = "modelServiceCatalogServiceImpl")
public class ModelServiceCatalogServiceImpl implements ModelServiceCatalogService {

    @Resource(name = "modelServiceCatalogDao")
    private ModelServiceCatalogDao modelServiceCatalogDao;

    private IdGenerator idGenerator;

    @Qualifier("idGenerator")
    @Autowired(required = false)
    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    public List<ModelServiceCatalogVo> selectList(ModelServiceCatalogSelectDto selectDto) {
        return modelServiceCatalogDao.selectList(selectDto)
            .stream()
            .map(ModelServiceCatalogConverter.INSTANCE::po2vo)
            .toList();
    }

    @Override
    public PageData<ModelServiceCatalogVo> selectPage(PageSelectListDto<ModelServiceCatalogSelectDto> selectDto) {
        Page<ModelServiceCatalogPo> page = PageContext.of(selectDto.getPageNum(), selectDto.getPageSize(), () -> {
            modelServiceCatalogDao.selectList(selectDto.getQuery());
        });
        List<ModelServiceCatalogVo> result = page.getResult()
            .stream()
            .map(ModelServiceCatalogConverter.INSTANCE::po2vo)
            .toList();
        return PageData.of(page.getPageNum(), page.getPageSize(), page.getTotal(), result);
    }

    @Override
    public Long save(ModelServiceCatalogDto dto) {
        Validations.validateSave(dto);

        // 转换 DTO 为 PO
        ModelServiceCatalogPo entity = ModelServiceCatalogConverter.INSTANCE.dto2po(dto);

        // 判断是新增还是更新
        Long id = entity.getId();
        if (Objects.isNull(id) || id == 0) {
            // 新增：使用IdGenerator生成主键
            entity.setId(idGenerator.generateId());
            LocalDateTime now = Moments.now();
            entity.setUpdateTime(now);
            entity.setCreateTime(now);
            int success = modelServiceCatalogDao.insert(entity);
            Asserts.greaterThan(success, 0, SystemErrorCode.CREATE_DATA_ERROR);
        } else {
            ModelServiceCatalogPo existing = modelServiceCatalogDao.selectById(id);
            Asserts.isTrue(Objects.nonNull(existing), SystemErrorCode.DATA_NOT_EXISTS, id);
            // 更新：直接更新
            entity.setUpdateTime(Moments.now());
            int success = modelServiceCatalogDao.update(entity);
            Asserts.greaterThan(success, 0, SystemErrorCode.UPDATE_DATA_ERROR, id);
        }

        return entity.getId();
    }

    @Override
    public int delete(Long id) {
        ModelServiceCatalogPo existing = modelServiceCatalogDao.selectById(id);
        Asserts.isTrue(Objects.nonNull(existing), SystemErrorCode.DATA_NOT_EXISTS, id);
        return modelServiceCatalogDao.delete(id);
    }
}
