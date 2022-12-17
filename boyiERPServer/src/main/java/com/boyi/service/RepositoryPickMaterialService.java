package com.boyi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.RepositoryPickMaterial;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 仓库模块-领料模块 服务类
 * </p>
 *
 * @author sunke
 * @since 2021-09-05
 */
public interface RepositoryPickMaterialService extends IService<RepositoryPickMaterial> {

    Page<RepositoryPickMaterial> innerQuery(Page page, QueryWrapper<RepositoryPickMaterial> like);

    RepositoryPickMaterial one(QueryWrapper<RepositoryPickMaterial> id);

    Page<RepositoryPickMaterial> innerQueryBySearch(Page page,String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate,List<Long> searchStatus);
    Page<RepositoryPickMaterial> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate, List<Long> searchStatus, Map<String,String> otherSearch);

    Double countByDepartmentIdMaterialId(Long departmentId, String materialId);

    List<RepositoryPickMaterial> countLTByCloseDate(LocalDate closeDate);

    List<RepositoryPickMaterial> getSameBatch(Long id,String comment,Long departmentId);

    List<RepositoryPickMaterial> listByBatchIds(ArrayList<String> batchIds);

    void updateBatchIdNull(Long id);

    void updateBatchIdAppendYearById(String year, List<String> batchIds);

    List<RepositoryPickMaterial> listGTEndDate(String endDate);

    void updateBatchIdAppendYearByOneId(String year, String batchId);
}
