package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.RepositoryPickMaterial;
import com.boyi.mapper.RepositoryPickMaterialMapper;
import com.boyi.service.RepositoryPickMaterialService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
public class RepositoryPickMaterialServiceImpl extends ServiceImpl<RepositoryPickMaterialMapper, RepositoryPickMaterial> implements RepositoryPickMaterialService {
    private SimpleDateFormat sdf_yy = new SimpleDateFormat("yy");

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
    public Page<RepositoryPickMaterial> innerQueryBySearch(Page page,String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate,List<Long> searchStatus) {
        return this.innerQuery(page,
                new QueryWrapper<RepositoryPickMaterial>().
                        like(StrUtil.isNotBlank(searchStr)
                                && StrUtil.isNotBlank(searchField),queryField,searchStr)
                        .ge(StrUtil.isNotBlank(searchStartDate),DBConstant.TABLE_REPOSITORY_PICK_MATERIAL.PICK_DATE_FIELDNAME,searchStartDate)
                        .le(StrUtil.isNotBlank(searchEndDate),DBConstant.TABLE_REPOSITORY_PICK_MATERIAL.PICK_DATE_FIELDNAME,searchEndDate)
                        .in(searchStatus != null && searchStatus.size() > 0,DBConstant.TABLE_REPOSITORY_PICK_MATERIAL.STATUS_FIELDNAME,searchStatus)

        );
    }

    @Override
    public Page<RepositoryPickMaterial> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate, List<Long> searchStatus, Map<String, String> otherSearch) {
        QueryWrapper<RepositoryPickMaterial> queryWrapper = new QueryWrapper<>();
        for (String key : otherSearch.keySet()){
            String val = otherSearch.get(key);
            queryWrapper.like(StrUtil.isNotBlank(val) && !val.equals("null")
                    && StrUtil.isNotBlank(key),key,val);
        }
        return this.innerQuery(page,
                queryWrapper.
                        like(StrUtil.isNotBlank(searchStr) &&!searchStr.equals("null")
                                && StrUtil.isNotBlank(searchField),queryField,searchStr)
                        .ge(StrUtil.isNotBlank(searchStartDate) &&!searchStartDate.equals("null"),DBConstant.TABLE_REPOSITORY_PICK_MATERIAL.PICK_DATE_FIELDNAME,searchStartDate)
                        .le(StrUtil.isNotBlank(searchEndDate) &&!searchEndDate.equals("null"),DBConstant.TABLE_REPOSITORY_PICK_MATERIAL.PICK_DATE_FIELDNAME,searchEndDate)
                        .in(searchStatus != null && searchStatus.size() > 0,DBConstant.TABLE_REPOSITORY_PICK_MATERIAL.STATUS_FIELDNAME,searchStatus).orderByDesc(DBConstant.TABLE_REPOSITORY_PICK_MATERIAL.ID_FIELDNAME)

        );
    }

    @Override
    public Double countByDepartmentIdMaterialId(Long departmentId, String materialId) {
        Double count = repositoryPickMaterialMapper.countByDepartmentAndMaterial(departmentId, materialId);
        count = count ==null ? 0D : count;
        return count;
    }

    @Override
    public List<RepositoryPickMaterial> countLTByCloseDate(LocalDate closeDate) {
        return this.list(new QueryWrapper<RepositoryPickMaterial>()
                .le(DBConstant.TABLE_REPOSITORY_PICK_MATERIAL.PICK_DATE_FIELDNAME, closeDate)
                .ne(DBConstant.TABLE_REPOSITORY_PICK_MATERIAL.STATUS_FIELDNAME,
                        DBConstant.TABLE_REPOSITORY_PICK_MATERIAL.STATUS_FIELDVALUE_0));
    }

    // 查看批次号的内容，ID!=自己的，ID> 当年年份+月份+日+0000，并且batchId!=''并且comment =当前comment内容，存在则不允许创建
    @Override
    public List<RepositoryPickMaterial> getSameBatch(Long id ,String batchId) {
        Date today = new Date();
        String year = sdf_yy.format(today);

        return this.list(new QueryWrapper<RepositoryPickMaterial>()
                        .ne(id!=null,DBConstant.TABLE_REPOSITORY_PICK_MATERIAL.ID_FIELDNAME,id)
                .gt(DBConstant.TABLE_REPOSITORY_PICK_MATERIAL.ID_FIELDNAME,Long.valueOf(year+"01010000"))
                .eq(DBConstant.TABLE_REPOSITORY_PICK_MATERIAL.BATCH_ID_FIELDNAME,batchId));
    }

    @Override
    public List<RepositoryPickMaterial> listByBatchIds(ArrayList<String> batchIds) {
        return this.list(new QueryWrapper<RepositoryPickMaterial>()
                .in(DBConstant.TABLE_PRODUCE_BATCH.BATCH_ID_FIELDNAME,batchIds));
    }

    @Override
    public void updateBatchIdNull(Long id) {
        this.repositoryPickMaterialMapper.updateBatchIdNull(id);
    }

    @Override
    public void updateBatchIdAppendYearById(String year, List<String> batchIds) {
        this.repositoryPickMaterialMapper.updateBatchIdAppendYearById(year,batchIds);
    }

    @Override
    public List<RepositoryPickMaterial> listGTEndDate(String endDate) {
        return this.repositoryPickMaterialMapper.listGTEndDate(endDate);
    }

}
