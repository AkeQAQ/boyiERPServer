package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.RepositoryPickMaterial;
import com.boyi.entity.RepositoryReturnMaterial;
import com.boyi.mapper.RepositoryReturnMaterialMapper;
import com.boyi.service.RepositoryReturnMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    private SimpleDateFormat sdf_yy = new SimpleDateFormat("yy");


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
    public Page<RepositoryReturnMaterial> innerQueryBySearch(Page page,String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate,List<Long> searchStatus) {
        return this.innerQuery(page,
                new QueryWrapper<RepositoryReturnMaterial>().
                        like(StrUtil.isNotBlank(searchStr)
                                && StrUtil.isNotBlank(searchField),queryField,searchStr)
                        .ge(StrUtil.isNotBlank(searchStartDate),DBConstant.TABLE_REPOSITORY_RETURN_MATERIAL.RETURN_DATE_FIELDNAME,searchStartDate)
                        .le(StrUtil.isNotBlank(searchEndDate),DBConstant.TABLE_REPOSITORY_RETURN_MATERIAL.RETURN_DATE_FIELDNAME,searchEndDate)
                        .in(searchStatus != null && searchStatus.size() > 0,DBConstant.TABLE_REPOSITORY_RETURN_MATERIAL.STATUS_FIELDNAME,searchStatus)

        );
    }

    @Override
    public Page<RepositoryReturnMaterial> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate, List<Long> searchStatus, Map<String, String> otherSearch) {
        QueryWrapper<RepositoryReturnMaterial> queryWrapper = new QueryWrapper<>();
        for (String key : otherSearch.keySet()){
            String val = otherSearch.get(key);
            queryWrapper.like(StrUtil.isNotBlank(val) && !val.equals("null")
                    && StrUtil.isNotBlank(key),key,val);
        }
        return this.innerQuery(page,
                        queryWrapper.
                        like(StrUtil.isNotBlank(searchStr) &&!searchStr.equals("null")
                                && StrUtil.isNotBlank(searchField),queryField,searchStr)
                        .ge(StrUtil.isNotBlank(searchStartDate)&&!searchStartDate.equals("null"),DBConstant.TABLE_REPOSITORY_RETURN_MATERIAL.RETURN_DATE_FIELDNAME,searchStartDate)
                        .le(StrUtil.isNotBlank(searchEndDate)&&!searchEndDate.equals("null"),DBConstant.TABLE_REPOSITORY_RETURN_MATERIAL.RETURN_DATE_FIELDNAME,searchEndDate)
                        .in(searchStatus != null && searchStatus.size() > 0,DBConstant.TABLE_REPOSITORY_RETURN_MATERIAL.STATUS_FIELDNAME,searchStatus)

        );
    }

    @Override
    public Double countByDepartmentIdMaterialId(Long departmentId, String materialId) {
        Double returnCount = repositoryReturnMaterialMapper.countByDepartmentAndMaterial(departmentId, materialId);
        returnCount  = returnCount==null?0L:returnCount;
        return returnCount;

    }

    @Override
    public List<RepositoryReturnMaterial> countLTByCloseDate(LocalDate closeDate) {
        return this.list(new QueryWrapper<RepositoryReturnMaterial>()
                .le(DBConstant.TABLE_REPOSITORY_RETURN_MATERIAL.RETURN_DATE_FIELDNAME, closeDate)
                .ne(DBConstant.TABLE_REPOSITORY_RETURN_MATERIAL.STATUS_FIELDNAME,
                        DBConstant.TABLE_REPOSITORY_RETURN_MATERIAL.STATUS_FIELDVALUE_0));
    }

    @Override
    public List<RepositoryReturnMaterial> getSameBatch(Long id, String batchId) {
        Date today = new Date();
        String year = sdf_yy.format(today);

        return this.list(new QueryWrapper<RepositoryReturnMaterial>()
                .ne(id!=null,DBConstant.TABLE_REPOSITORY_RETURN_MATERIAL.ID_FIELDNAME,id)
                .gt(DBConstant.TABLE_REPOSITORY_RETURN_MATERIAL.ID_FIELDNAME,Long.valueOf(year+"01010000"))
                .eq(DBConstant.TABLE_REPOSITORY_RETURN_MATERIAL.BATCH_ID_FIELDNAME,batchId));
    }

    @Override
    public void updateBatchIdNull(Long id) {
        this.repositoryReturnMaterialMapper.updateBatchIdNull(id);
    }

    @Override
    public List<RepositoryReturnMaterial> listByBatchIds(ArrayList<String> batchIds) {
        return this.list(new QueryWrapper<RepositoryReturnMaterial>()
                .in(DBConstant.TABLE_PRODUCE_BATCH.BATCH_ID_FIELDNAME,batchIds));
    }

    @Override
    public void updateBatchIdAppendYearById(int year, List<String> batchIds) {
        this.repositoryReturnMaterialMapper.updateBatchIdAppendYearById(year,batchIds);
    }

}
