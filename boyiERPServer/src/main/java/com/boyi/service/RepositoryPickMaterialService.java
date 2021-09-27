package com.boyi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.BaseSupplierMaterial;
import com.boyi.entity.RepositoryPickMaterial;
import com.boyi.entity.RepositoryPickMaterial;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDate;
import java.util.List;

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

    Page<RepositoryPickMaterial> innerQueryBySearch(Page page,String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate);

    Double countByDepartmentIdMaterialId(Long departmentId, String materialId);

    List<RepositoryPickMaterial> countLTByCloseDate(LocalDate closeDate);
}
