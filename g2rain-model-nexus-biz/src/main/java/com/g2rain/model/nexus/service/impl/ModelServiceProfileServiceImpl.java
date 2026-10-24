package com.g2rain.model.nexus.service.impl;

import com.g2rain.common.exception.SystemErrorCode;
import com.g2rain.common.id.IdGenerator;
import com.g2rain.common.model.PageData;
import com.g2rain.common.model.PageSelectListDto;
import com.g2rain.common.utils.Asserts;
import com.g2rain.common.utils.Moments;
import com.g2rain.common.utils.Strings;
import com.g2rain.common.validation.Validations;
import com.g2rain.model.nexus.converter.ModelServiceProfileConverter;
import com.g2rain.model.nexus.dao.ModelServiceCatalogDao;
import com.g2rain.model.nexus.dao.ModelServiceProfileDao;
import com.g2rain.model.nexus.dao.po.ModelServiceDetailPo;
import com.g2rain.model.nexus.dao.po.ModelServiceProfilePo;
import com.g2rain.model.nexus.dto.ModelServiceCatalogDto;
import com.g2rain.model.nexus.dto.ModelServiceProfileDto;
import com.g2rain.model.nexus.dto.ModelServiceProfileSelectDto;
import com.g2rain.model.nexus.dto.UpdateStatusDto;
import com.g2rain.model.nexus.enums.ModelStatus;
import com.g2rain.model.nexus.enums.ModelType;
import com.g2rain.model.nexus.enums.NexusErrorCode;
import com.g2rain.model.nexus.enums.ResourceType;
import com.g2rain.model.nexus.protocol.ModelEndpoint;
import com.g2rain.model.nexus.service.ModelServiceCatalogService;
import com.g2rain.model.nexus.service.ModelServiceProfileService;
import com.g2rain.model.nexus.vo.ModelServiceDetailVO;
import com.g2rain.model.nexus.vo.ModelServiceOptionVo;
import com.g2rain.mybatis.pagination.PageContext;
import com.g2rain.mybatis.pagination.model.Page;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Resource(name = "modelServiceCatalogDao")
    private ModelServiceCatalogDao modelServiceCatalogDao;

    @Resource(name = "modelServiceCatalogServiceImpl")
    private ModelServiceCatalogService modelServiceCatalogService;

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
    public List<ModelEndpoint> selectEndpoints(ModelServiceProfileSelectDto selectDto) {
        return modelServiceProfileDao.selectList(selectDto)
            .stream()
            .map(ModelServiceProfileConverter.INSTANCE::po2endpoint)
            .toList();
    }

    @Override
    public List<ModelServiceOptionVo> selectActiveOptions() {
        ModelServiceProfileSelectDto selectDto = new ModelServiceProfileSelectDto();
        selectDto.setModelType(ModelType.LLM.name());
        selectDto.setStatus(ModelStatus.ACTIVE.name());
        selectDto.setCatalogStatus(ModelStatus.ACTIVE.name());
        selectDto.setSelectable(Boolean.TRUE);
        return modelServiceProfileDao.selectOptionsList(selectDto)
            .stream()
            .map(ModelServiceProfileConverter.INSTANCE::po2option)
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
    @Transactional(rollbackFor = Exception.class)
    public Long save(ModelServiceProfileDto dto) {
        Validations.validateSave(dto);

        Long id = dto.getId();
        boolean isUpdate = Objects.nonNull(id) && id != 0;
        String catalogResourceType;
        String modelType = dto.getModelType();
        ModelServiceDetailPo existing;
        if (isUpdate) {
            existing = modelServiceProfileDao.selectById(id);
            Asserts.isTrue(Objects.nonNull(existing), SystemErrorCode.DATA_NOT_EXISTS, id);
            dto.setCatalogId(existing.getCatalogId());
            modelType = existing.getModelType();
            var catalog = modelServiceCatalogDao.selectById(existing.getCatalogId());
            Asserts.isTrue(Objects.nonNull(catalog), SystemErrorCode.DATA_NOT_EXISTS, existing.getCatalogId());
            catalogResourceType = catalog.getResourceType();
            if (ResourceType.CUSTOM.name().equals(catalogResourceType)) {
                saveCustomCatalog(dto);
            }
        } else if (Objects.isNull(dto.getCatalogId())) {
            dto.setCatalogId(saveCustomCatalog(dto));
            catalogResourceType = ResourceType.CUSTOM.name();
        } else {
            var catalog = modelServiceCatalogDao.selectById(dto.getCatalogId());
            Asserts.isTrue(Objects.nonNull(catalog)
                    && ResourceType.MARKET.name().equals(catalog.getResourceType()),
                SystemErrorCode.DATA_NOT_EXISTS, dto.getCatalogId());
            catalogResourceType = ResourceType.MARKET.name();
            modelType = catalog.getModelType();
        }

        ModelServiceProfilePo entity = ModelServiceProfileConverter.INSTANCE.dto2po(dto);
        entity.setDisplayName(Strings.blankToNull(Strings.trim(dto.getDisplayName())));
        // 仅大模型可在对话中选择，向量 / 重排序固定为否
        if (!ModelType.LLM.name().equals(modelType)) {
            entity.setSelectable(Boolean.FALSE);
        }
        if (!isUpdate) {
            Asserts.isTrue(Objects.nonNull(dto.getOrganId()), SystemErrorCode.PARAM_REQUIRED, "organId");
            assertMarketCatalogUnique(dto.getOrganId(), dto.getCatalogId(), catalogResourceType);
            entity.setId(idGenerator.generateId());
            entity.setStatus(ModelStatus.ACTIVE.name());
            entity.setSelectable(Objects.requireNonNullElse(entity.getSelectable(), Boolean.TRUE));
            entity.setModelType(modelType);
            // 同机构同类型的第一条配置自动成为默认
            entity.setDefaultFlag(modelServiceProfileDao.countDefault(dto.getOrganId(), modelType) == 0);
            LocalDateTime now = Moments.now();
            entity.setUpdateTime(now);
            entity.setCreateTime(now);
            int success = modelServiceProfileDao.insert(entity);
            Asserts.greaterThan(success, 0, SystemErrorCode.CREATE_DATA_ERROR);
        } else {
            // catalogId 更新不可变，MARKET 唯一性无需再查
            entity.setCatalogId(null);
            entity.setStatus(null);
            entity.setUpdateTime(Moments.now());
            int success = modelServiceProfileDao.update(entity);
            Asserts.greaterThan(success, 0, SystemErrorCode.UPDATE_DATA_ERROR, id);
        }

        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(Long id) {
        ModelServiceDetailPo existing = modelServiceProfileDao.selectById(id);
        Asserts.isTrue(Objects.nonNull(existing), SystemErrorCode.DATA_NOT_EXISTS, id);
        Asserts.isTrue(!Boolean.TRUE.equals(existing.getDefaultFlag()), NexusErrorCode.DEFAULT_PROFILE_LOCKED, id);

        int deleted = modelServiceProfileDao.delete(id);
        if (ResourceType.CUSTOM.name().equals(existing.getResourceType())
            && Objects.nonNull(existing.getCatalogId())) {
            modelServiceCatalogService.delete(existing.getCatalogId(), ResourceType.CUSTOM);
        }
        return deleted;
    }

    @Override
    public int updateStatus(Long id, UpdateStatusDto dto) {
        ModelStatus.validate(dto.getStatus());
        ModelServiceDetailPo existing = modelServiceProfileDao.selectById(id);
        Asserts.isTrue(Objects.nonNull(existing), SystemErrorCode.DATA_NOT_EXISTS, id);
        Asserts.isTrue(ModelStatus.ACTIVE.name().equals(dto.getStatus())
            || !Boolean.TRUE.equals(existing.getDefaultFlag()), NexusErrorCode.DEFAULT_PROFILE_LOCKED, id);

        ModelServiceProfilePo entity = new ModelServiceProfilePo();
        entity.setId(id);
        entity.setStatus(dto.getStatus());
        entity.setUpdateTime(Moments.now());
        int success = modelServiceProfileDao.update(entity);
        Asserts.greaterThan(success, 0, SystemErrorCode.UPDATE_DATA_ERROR, id);
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDefault(Long id) {
        ModelServiceDetailPo existing = modelServiceProfileDao.selectById(id);
        Asserts.isTrue(Objects.nonNull(existing), SystemErrorCode.DATA_NOT_EXISTS, id);
        Asserts.isTrue(ModelStatus.ACTIVE.name().equals(existing.getStatus()), NexusErrorCode.MODEL_INACTIVE, id);
        if (Boolean.TRUE.equals(existing.getDefaultFlag())) {
            return 0;
        }

        // 先清同类型旧默认，再设当前；唯一索引兜底并发
        modelServiceProfileDao.clearDefault(existing.getOrganId(), existing.getModelType());
        ModelServiceProfilePo entity = new ModelServiceProfilePo();
        entity.setId(id);
        entity.setDefaultFlag(Boolean.TRUE);
        entity.setUpdateTime(Moments.now());
        int success = modelServiceProfileDao.update(entity);
        Asserts.greaterThan(success, 0, SystemErrorCode.UPDATE_DATA_ERROR, id);
        return success;
    }

    /**
     * 同机构下同一 MARKET 目录只允许一份 profile（仅新增时调用）。
     */
    private void assertMarketCatalogUnique(Long organId, Long catalogId, String resourceType) {
        if (!ResourceType.MARKET.name().equals(resourceType) || Objects.isNull(catalogId)) {
            return;
        }
        Asserts.isTrue(Objects.nonNull(organId), SystemErrorCode.PARAM_REQUIRED, "organId");
        Asserts.isTrue(modelServiceProfileDao.countByOrganIdAndCatalogId(organId, catalogId) == 0,
            SystemErrorCode.DATA_EXISTS, catalogId);
    }

    private Long saveCustomCatalog(ModelServiceProfileDto dto) {
        Asserts.isTrue(Strings.isNotBlank(dto.getModelName()), SystemErrorCode.PARAM_REQUIRED, "modelName");
        Asserts.isTrue(Strings.isNotBlank(dto.getModelType()), SystemErrorCode.PARAM_REQUIRED, "modelType");
        Asserts.isTrue(Strings.isNotBlank(dto.getProvider()), SystemErrorCode.PARAM_REQUIRED, "provider");
        Asserts.isTrue(Strings.isNotBlank(dto.getProtocol()), SystemErrorCode.PARAM_REQUIRED, "protocol");
        Asserts.isTrue(Strings.isNotBlank(dto.getBaseUrl()), SystemErrorCode.PARAM_REQUIRED, "baseUrl");

        ModelServiceCatalogDto catalogDto = new ModelServiceCatalogDto();
        catalogDto.setId(dto.getCatalogId());
        catalogDto.setModelName(dto.getModelName());
        catalogDto.setModelType(dto.getModelType());
        catalogDto.setProvider(dto.getProvider());
        catalogDto.setProtocol(dto.getProtocol());
        catalogDto.setBaseUrl(dto.getBaseUrl());
        catalogDto.setDescription(dto.getDescription());
        if (Strings.isNotBlank(dto.getApiKey())) {
            catalogDto.setApiKey(dto.getApiKey().trim());
        }

        return modelServiceCatalogService.save(catalogDto, ResourceType.CUSTOM);
    }
}
