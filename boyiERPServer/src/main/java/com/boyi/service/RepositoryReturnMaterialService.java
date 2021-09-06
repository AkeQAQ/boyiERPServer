package com.boyi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boyi.entity.RepositoryReturnMaterial;

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

    Page<RepositoryReturnMaterial> innerQueryBySearch(Page page,String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate);

    Double countByDepartmentIdMaterialId(Long departmentId, String materialId);
}
