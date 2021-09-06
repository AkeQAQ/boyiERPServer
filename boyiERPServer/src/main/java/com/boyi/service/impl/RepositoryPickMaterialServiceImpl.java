package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.BaseSupplierMaterial;
import com.boyi.entity.OrderBuyorderDocument;
import com.boyi.entity.RepositoryPickMaterial;
import com.boyi.entity.RepositoryPickMaterial;
import com.boyi.mapper.OrderBuyorderDocumentMapper;
import com.boyi.mapper.RepositoryPickMaterialMapper;
import com.boyi.mapper.RepositoryPickMaterialMapper;
import com.boyi.service.RepositoryPickMaterialService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * <p>
 * 仓库模块-领料模块 服务实现类
 * </p>
 *
 * @author sunke
 * @since 2021-09-05
 */
@Service
public class RepositoryPickMaterialServiceImpl extends ServiceImpl<RepositoryPickMaterialMapper, RepositoryPickMaterial> implements RepositoryPickMaterialService {
    @Autowired
    RepositoryPickMaterialMapper repositoryPickMaterialMapper;
    public Page<RepositoryPickMaterial> innerQuery(Page page, QueryWrapper<RepositoryPickMaterial> eq) {
        return repositoryPickMaterialMapper.page(page,eq);
    }

    @Override
    public RepositoryPickMaterial one(QueryWrapper<RepositoryPickMaterial> id) {
        return repositoryPickMaterialMapper.one(id);
    }

    @Override
    public Page<RepositoryPickMaterial> innerQueryBySearch(Page page,String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate) {
        return this.innerQuery(page,
                new QueryWrapper<RepositoryPickMaterial>().
                        like(StrUtil.isNotBlank(searchStr)
                                && StrUtil.isNotBlank(searchField),queryField,searchStr)
                        .ge(StrUtil.isNotBlank(searchStartDate),DBConstant.TABLE_REPOSITORY_PICK_MATERIAL.PICK_DATE_FIELDNAME,searchStartDate)
                        .le(StrUtil.isNotBlank(searchEndDate),DBConstant.TABLE_REPOSITORY_PICK_MATERIAL.PICK_DATE_FIELDNAME,searchEndDate));
    }

    @Override
    public Double countByDepartmentIdMaterialId(Long departmentId, String materialId) {
        return repositoryPickMaterialMapper.countByDepartmentAndMaterial(departmentId,materialId);
    }

}
