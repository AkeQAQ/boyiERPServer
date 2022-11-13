package com.boyi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boyi.entity.RepositoryReturnMaterial;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 仓库模块-退料模块 服务类
 * </p>
 *
 * @author sunke
 * @since 2021-09-05
 */
public interface RepositoryReturnMaterialService extends IService<RepositoryReturnMaterial> {

    Page<RepositoryReturnMaterial> innerQuery(Page page, QueryWrapper<RepositoryReturnMaterial> like);

    RepositoryReturnMaterial one(QueryWrapper<RepositoryReturnMaterial> id);

    Page<RepositoryReturnMaterial> innerQueryBySearch(Page page,String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate,List<Long> searchStatus);
    Page<RepositoryReturnMaterial> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate, List<Long> searchStatus, Map<String,String> otherSearch);

    Double countByDepartmentIdMaterialId(Long departmentId, String materialId);

    List<RepositoryReturnMaterial> countLTByCloseDate(LocalDate closeDate);

    List<RepositoryReturnMaterial> getSameBatch(Long id, String batchId);

    void updateBatchIdNull(Long id);

    List<RepositoryReturnMaterial> listByBatchIds(ArrayList<String> batchIds);

    void updateBatchIdAppendYearById(String year, List<String> batchIds);

    List<RepositoryReturnMaterial> listGTEndDate(String endDate);

}
