package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.ExternalAccountRepositoryPickMaterial;
import com.boyi.entity.ExternalAccountRepositoryPickMaterial;
import com.boyi.mapper.ExternalAccountRepositoryPickMaterialMapper;
import com.boyi.mapper.ExternalAccountRepositoryPickMaterialMapper;
import com.boyi.service.ExternalAccountRepositoryPickMaterialService;
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
 * @since 2023-03-04
 */
@Service
public class ExternalAccountRepositoryPickMaterialServiceImpl extends ServiceImpl<ExternalAccountRepositoryPickMaterialMapper, ExternalAccountRepositoryPickMaterial> implements ExternalAccountRepositoryPickMaterialService {
    private SimpleDateFormat sdf_yy = new SimpleDateFormat("yy");

    @Autowired
    ExternalAccountRepositoryPickMaterialMapper repositoryPickMaterialMapper;
    public Page<ExternalAccountRepositoryPickMaterial> innerQuery(Page page, QueryWrapper<ExternalAccountRepositoryPickMaterial> eq) {
        return repositoryPickMaterialMapper.page(page,eq);
    }

    @Override
    public ExternalAccountRepositoryPickMaterial one(QueryWrapper<ExternalAccountRepositoryPickMaterial> id) {
        return repositoryPickMaterialMapper.one(id);
    }

    @Override
    public Page<ExternalAccountRepositoryPickMaterial> innerQueryBySearch(Page page, String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate, List<Long> searchStatus) {
        return this.innerQuery(page,
                new QueryWrapper<ExternalAccountRepositoryPickMaterial>().
                        like(StrUtil.isNotBlank(searchStr)
                                && StrUtil.isNotBlank(searchField),queryField,searchStr)
                        .ge(StrUtil.isNotBlank(searchStartDate), DBConstant.TABLE_REPOSITORY_PICK_MATERIAL.PICK_DATE_FIELDNAME,searchStartDate)
                        .le(StrUtil.isNotBlank(searchEndDate),DBConstant.TABLE_REPOSITORY_PICK_MATERIAL.PICK_DATE_FIELDNAME,searchEndDate)
                        .in(searchStatus != null && searchStatus.size() > 0,DBConstant.TABLE_REPOSITORY_PICK_MATERIAL.STATUS_FIELDNAME,searchStatus)

        );
    }

    @Override
    public Page<ExternalAccountRepositoryPickMaterial> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate, List<Long> searchStatus, Map<String, String> otherSearch) {
        QueryWrapper<ExternalAccountRepositoryPickMaterial> queryWrapper = new QueryWrapper<>();
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
    public List<ExternalAccountRepositoryPickMaterial> countLTByCloseDate(LocalDate closeDate) {
        return this.list(new QueryWrapper<ExternalAccountRepositoryPickMaterial>()
                .le(DBConstant.TABLE_REPOSITORY_PICK_MATERIAL.PICK_DATE_FIELDNAME, closeDate)
                .ne(DBConstant.TABLE_REPOSITORY_PICK_MATERIAL.STATUS_FIELDNAME,
                        DBConstant.TABLE_REPOSITORY_PICK_MATERIAL.STATUS_FIELDVALUE_0));
    }

    // 查看批次号的内容，ID!=自己的，ID> 当年年份+月份+日+0000，并且batchId!=''并且comment =当前comment内容，存在则不允许创建
    @Override
    public List<ExternalAccountRepositoryPickMaterial> getSameBatch(Long id ,String batchId,Long departmentId) {
        Date today = new Date();
        String year = sdf_yy.format(today);

        return this.list(new QueryWrapper<ExternalAccountRepositoryPickMaterial>()
                .ne(id!=null,DBConstant.TABLE_REPOSITORY_PICK_MATERIAL.ID_FIELDNAME,id)
                .gt(DBConstant.TABLE_REPOSITORY_PICK_MATERIAL.ID_FIELDNAME,Long.valueOf(year+"01010000"))
                .eq(DBConstant.TABLE_REPOSITORY_PICK_MATERIAL.BATCH_ID_FIELDNAME,batchId)
                .eq(departmentId!=null,DBConstant.TABLE_REPOSITORY_PICK_MATERIAL.DEPARTMENT_ID_FIELDNAME,departmentId));
    }

    @Override
    public List<ExternalAccountRepositoryPickMaterial> listByBatchIds(ArrayList<String> batchIds) {
        return this.list(new QueryWrapper<ExternalAccountRepositoryPickMaterial>()
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
    public List<ExternalAccountRepositoryPickMaterial> listGTEndDate(String endDate) {
        return this.repositoryPickMaterialMapper.listGTEndDate(endDate);
    }

    @Override
    public void updateBatchIdAppendYearByOneId(String year, String batchId) {
        this.repositoryPickMaterialMapper.updateBatchIdAppendYearByOneId(year,batchId);
    }
}
