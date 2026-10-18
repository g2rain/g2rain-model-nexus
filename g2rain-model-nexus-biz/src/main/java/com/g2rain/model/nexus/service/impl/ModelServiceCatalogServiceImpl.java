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
import com.g2rain.model.nexus.dao.ModelServiceProfileDao;
import com.g2rain.model.nexus.dao.po.ModelServiceCatalogPo;
import com.g2rain.model.nexus.dto.ModelServiceCatalogDto;
import com.g2rain.model.nexus.dto.ModelServiceCatalogSelectDto;
import com.g2rain.model.nexus.dto.UpdateStatusDto;
import com.g2rain.model.nexus.enums.ModelStatus;
import com.g2rain.model.nexus.enums.NexusErrorCode;
import com.g2rain.model.nexus.enums.ResourceType;
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

    @Resource(name = "modelServiceProfileDao")
    private ModelServiceProfileDao modelServiceProfileDao;

    private IdGenerator idGenerator;

    @Qualifier("idGenerator")
    @Autowired(required = false)
    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    public List<ModelServiceCatalogVo> selectList(ModelServiceCatalogSelectDto selectDto) {
        // 目录 list 只查模型广场（MARKET）；私有目录随模型配置维护
        ModelServiceCatalogSelectDto query = selectDto != null ? selectDto : new ModelServiceCatalogSelectDto();
        query.setResourceType(ResourceType.MARKET.name());
        return modelServiceCatalogDao.selectList(query)
            .stream()
            .map(ModelServiceCatalogConverter.INSTANCE::po2vo)
            .toList();
    }

    @Override
    public PageData<ModelServiceCatalogVo> selectPage(PageSelectListDto<ModelServiceCatalogSelectDto> selectDto) {
        ModelServiceCatalogSelectDto query = selectDto.getQuery();
        if (Objects.isNull(query)) {
            query = new ModelServiceCatalogSelectDto();
            selectDto.setQuery(query);
        }

        // 目录 page 只查模型广场（MARKET）；私有目录随模型配置维护
        query.setResourceType(ResourceType.MARKET.name());
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
    public Long save(ModelServiceCatalogDto dto, ResourceType resourceType) {
        Asserts.isTrue(Objects.nonNull(resourceType), SystemErrorCode.PARAM_REQUIRED, "resourceType");
        // 类型由入口写死（广场 MARKET / 私有 CUSTOM），校验前覆盖，不信客户端
        dto.setResourceType(resourceType.name());
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

            // 模型广场：默认无效, 因为默认有效, 保存后可能数据有问题, 导致租户直接使用模型
            if (ResourceType.MARKET == resourceType) {
                entity.setStatus(ModelStatus.INACTIVE.name());
            } else {
                entity.setStatus(ModelStatus.ACTIVE.name());
            }

            int success = modelServiceCatalogDao.insert(entity);
            Asserts.greaterThan(success, 0, SystemErrorCode.CREATE_DATA_ERROR);
        } else {
            ModelServiceCatalogPo existing = modelServiceCatalogDao.selectById(id);
            Asserts.isTrue(Objects.nonNull(existing), SystemErrorCode.DATA_NOT_EXISTS, id);
            assertTypeMatch(existing, resourceType, id);
            if (ResourceType.CUSTOM == resourceType) {
                assertCustomReferencedByCurrentOrgan(id);
            }

            // 更新：不允许改类型与状态
            entity.setResourceType(null);
            entity.setStatus(null);
            entity.setUpdateTime(Moments.now());
            int success = modelServiceCatalogDao.update(entity);
            Asserts.greaterThan(success, 0, SystemErrorCode.UPDATE_DATA_ERROR, id);
        }

        return entity.getId();
    }

    @Override
    public int delete(Long id, ResourceType resourceType) {
        Asserts.isTrue(Objects.nonNull(resourceType), SystemErrorCode.PARAM_REQUIRED, "resourceType");
        ModelServiceCatalogPo existing = modelServiceCatalogDao.selectById(id);
        Asserts.isTrue(Objects.nonNull(existing), SystemErrorCode.DATA_NOT_EXISTS, id);
        assertTypeMatch(existing, resourceType, id);

        if (ResourceType.CUSTOM == resourceType) {
            // 私有目录：由 profile 级联删除；类型已对齐即可
            return modelServiceCatalogDao.delete(id);
        }

        // 广场目录：不能仍被任一租户配置引用（运营方由 Controller 入口校验）
        int usedCount = modelServiceProfileDao.countByCatalogIdWithoutIsolation(id);
        Asserts.isTrue(usedCount == 0, NexusErrorCode.CATALOG_IN_USE, id);
        return modelServiceCatalogDao.delete(id);
    }

    @Override
    public int updateStatus(Long id, UpdateStatusDto dto, ResourceType resourceType) {
        Asserts.isTrue(Objects.nonNull(resourceType), SystemErrorCode.PARAM_REQUIRED, "resourceType");
        ModelStatus.validate(dto.getStatus());

        ModelServiceCatalogPo existing = modelServiceCatalogDao.selectById(id);
        Asserts.isTrue(Objects.nonNull(existing), SystemErrorCode.DATA_NOT_EXISTS, id);
        assertTypeMatch(existing, resourceType, id);

        ModelServiceCatalogPo entity = new ModelServiceCatalogPo();
        entity.setId(id);
        entity.setStatus(dto.getStatus());
        entity.setUpdateTime(Moments.now());
        int success = modelServiceCatalogDao.update(entity);
        Asserts.greaterThan(success, 0, SystemErrorCode.UPDATE_DATA_ERROR, id);
        return success;
    }

    private static void assertTypeMatch(ModelServiceCatalogPo existing, ResourceType expected, Long id) {
        Asserts.isTrue(expected.name().equals(existing.getResourceType()), SystemErrorCode.DATA_NOT_EXISTS, id);
    }

    /**
     * CUSTOM 更新：当前机构必须已有 profile 引用该目录（隔离 count），防止改别家私有目录。
     */
    private void assertCustomReferencedByCurrentOrgan(Long catalogId) {
        Asserts.isTrue(modelServiceProfileDao.countByCatalogId(catalogId) > 0,
            SystemErrorCode.DATA_NOT_EXISTS, catalogId);
    }
}
