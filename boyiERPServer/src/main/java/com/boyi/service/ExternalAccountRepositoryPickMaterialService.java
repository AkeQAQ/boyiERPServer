package com.boyi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.ExternalAccountRepositoryPickMaterial;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boyi.entity.ExternalAccountRepositoryPickMaterial;

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
 * @since 2023-03-04
 */
public interface ExternalAccountRepositoryPickMaterialService extends IService<ExternalAccountRepositoryPickMaterial> {

    Page<ExternalAccountRepositoryPickMaterial> innerQuery(Page page, QueryWrapper<ExternalAccountRepositoryPickMaterial> like);

    ExternalAccountRepositoryPickMaterial one(QueryWrapper<ExternalAccountRepositoryPickMaterial> id);

    Page<ExternalAccountRepositoryPickMaterial> innerQueryBySearch(Page page, String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate, List<Long> searchStatus);
    Page<ExternalAccountRepositoryPickMaterial> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate, List<Long> searchStatus, Map<String,String> otherSearch);

    Double countByDepartmentIdMaterialId(Long departmentId, String materialId);

    List<ExternalAccountRepositoryPickMaterial> countLTByCloseDate(LocalDate closeDate);

    List<ExternalAccountRepositoryPickMaterial> getSameBatch(Long id,String comment,Long departmentId);

    List<ExternalAccountRepositoryPickMaterial> listByBatchIds(ArrayList<String> batchIds);

    void updateBatchIdNull(Long id);

    void updateBatchIdAppendYearById(String year, List<String> batchIds);

    List<ExternalAccountRepositoryPickMaterial> listGTEndDate(String endDate);

    void updateBatchIdAppendYearByOneId(String year, String batchId);
}
