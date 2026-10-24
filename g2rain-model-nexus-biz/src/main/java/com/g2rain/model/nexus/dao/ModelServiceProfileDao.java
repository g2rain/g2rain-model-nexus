package com.g2rain.model.nexus.dao;

import com.g2rain.data.isolation.annotations.DataIsolation;
import com.g2rain.data.isolation.annotations.IgnoreIsolation;
import com.g2rain.model.nexus.dao.po.ModelServiceDetailPo;
import com.g2rain.model.nexus.dao.po.ModelServiceProfilePo;
import com.g2rain.model.nexus.dto.ModelServiceProfileSelectDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模型服务配置数据访问接口
 * 表名: model_service_profile
 *
 * @author G2rain Generator
 */
@Mapper
@DataIsolation(organIdPropertyName = "organId", organIdColumnName = "organ_id")
public interface ModelServiceProfileDao {

    /**
     * 插入单条记录
     *
     * @param entity 实体对象
     * @return 影响行数
     */
    int insert(ModelServiceProfilePo entity);

    /**
     * 批量插入记录
     *
     * @param list 实体对象列表
     * @return 影响行数
     */
    int insertMultiple(List<ModelServiceProfilePo> list);

    /**
     * 根据 ID 更新记录
     *
     * @param entity 实体对象
     * @return 影响行数
     */
    int update(ModelServiceProfilePo entity);

    /**
     * 根据 ID 删除记录
     *
     * @param id 主键 ID
     * @return 影响行数
     */
    int delete(Long id);

    /**
     * 根据ID和Version更新记录（乐观锁更新）
     *
     * @param entity 实体对象（必须包含version字段）
     * @return 影响行数
     */
    int updateByVersion(ModelServiceProfilePo entity);

    /**
     * 根据 ID 查询记录
     *
     * @param id 主键 ID
     * @return 实体对象
     */
    ModelServiceDetailPo selectById(Long id);

    /**
     * 根据查询入参 DTO 筛选列表
     *
     * @param selectDto 查询条件 DTO
     * @return 实体对象列表
     */
    List<ModelServiceDetailPo> selectList(ModelServiceProfileSelectDto selectDto);

    /**
     * 插入单条记录（忽略数据隔离）
     *
     * @param entity 实体对象
     * @return 影响行数
     */
    @IgnoreIsolation
    int insertWithoutIsolation(ModelServiceProfilePo entity);

    /**
     * 根据ID更新记录（忽略数据隔离）
     *
     * @param entity 实体对象
     * @return 影响行数
     */
    @IgnoreIsolation
    int updateWithoutIsolation(ModelServiceProfilePo entity);

    /**
     * 根据ID查询记录（忽略数据隔离）
     *
     * @param id 主键 ID
     * @return 实体对象
     */
    @IgnoreIsolation
    ModelServiceDetailPo selectByIdWithoutIsolation(Long id);

    /**
     * 根据查询入参DTO筛选列表（忽略数据隔离）
     *
     * @param selectDto 查询条件 DTO
     * @return 实体对象列表
     */
    @IgnoreIsolation
    List<ModelServiceDetailPo> selectListWithoutIsolation(ModelServiceProfileSelectDto selectDto);

    /**
     * 按条件查询下拉选项字段（仅 id / model_name），本租户隔离。
     *
     * @param selectDto 查询条件 DTO（常用 status）
     * @return 短字段明细列表
     */
    List<ModelServiceDetailPo> selectOptionsList(ModelServiceProfileSelectDto selectDto);

    /**
     * 按 catalogId 统计有效配置数（忽略机构隔离）。
     *
     * @param catalogId 模型目录 ID
     * @return 引用该目录的 profile 数量
     */
    @IgnoreIsolation
    int countByCatalogIdWithoutIsolation(@Param("catalogId") Long catalogId);

    /**
     * 按 catalogId 统计有效配置数（本机构隔离）。
     *
     * @param catalogId 模型目录 ID
     * @return 当前机构引用该目录的 profile 数量
     */
    int countByCatalogId(@Param("catalogId") Long catalogId);

    /**
     * 同机构下同一 catalog 的有效配置数（显式 organId，用于 MARKET 唯一性）。
     *
     * @param organId   机构标识
     * @param catalogId 模型目录 ID
     * @return 数量
     */
    @IgnoreIsolation
    int countByOrganIdAndCatalogId(@Param("organId") Long organId, @Param("catalogId") Long catalogId);

    /**
     * 同机构同类型的默认配置数。
     *
     * @param organId   机构标识
     * @param modelType 模型类型
     * @return 数量
     */
    @IgnoreIsolation
    int countDefault(@Param("organId") Long organId, @Param("modelType") String modelType);

    /**
     * 清除同机构同类型的默认标识。
     *
     * @param organId   机构标识
     * @param modelType 模型类型
     * @return 影响行数
     */
    @IgnoreIsolation
    @SuppressWarnings("UnusedReturnValue")
    int clearDefault(@Param("organId") Long organId, @Param("modelType") String modelType);
}
