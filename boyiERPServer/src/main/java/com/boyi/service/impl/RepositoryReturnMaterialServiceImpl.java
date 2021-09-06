package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.RepositoryReturnMaterial;
import com.boyi.mapper.RepositoryReturnMaterialMapper;
import com.boyi.service.RepositoryReturnMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 仓库模块-领料模块 服务实现类
 * </p>
 *
 * @author sunke
 * @since 2021-09-05
 */
@Service
public class RepositoryReturnMaterialServiceImpl extends ServiceImpl<RepositoryReturnMaterialMapper, RepositoryReturnMaterial> implements RepositoryReturnMaterialService {
    @Autowired
    RepositoryReturnMaterialMapper repositoryReturnMaterialMapper;
    public Page<RepositoryReturnMaterial> innerQuery(Page page, QueryWrapper<RepositoryReturnMaterial> eq) {
        return repositoryReturnMaterialMapper.page(page,eq);
    }

    @Override
    public RepositoryReturnMaterial one(QueryWrapper<RepositoryReturnMaterial> id) {
        return repositoryReturnMaterialMapper.one(id);
    }

    @Override
    public Page<RepositoryReturnMaterial> innerQueryBySearch(Page page,String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate) {
        return this.innerQuery(page,
                new QueryWrapper<RepositoryReturnMaterial>().
                        like(StrUtil.isNotBlank(searchStr)
                                && StrUtil.isNotBlank(searchField),queryField,searchStr)
                        .ge(StrUtil.isNotBlank(searchStartDate),DBConstant.TABLE_REPOSITORY_RETURN_MATERIAL.RETURN_DATE_FIELDNAME,searchStartDate)
                        .le(StrUtil.isNotBlank(searchEndDate),DBConstant.TABLE_REPOSITORY_RETURN_MATERIAL.RETURN_DATE_FIELDNAME,searchEndDate));
    }

    @Override
    public Double countByDepartmentIdMaterialId(Long departmentId, String materialId) {
        return repositoryReturnMaterialMapper.countByDepartmentAndMaterial(departmentId,materialId);

    }

}
