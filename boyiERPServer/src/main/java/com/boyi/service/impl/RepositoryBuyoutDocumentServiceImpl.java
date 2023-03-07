package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.BaseSupplierMaterial;
import com.boyi.entity.RepositoryBuyinDocument;
import com.boyi.entity.RepositoryBuyoutDocument;
import com.boyi.mapper.RepositoryBuyoutDocumentMapper;
import com.boyi.mapper.RepositoryBuyoutDocumentMapper;
import com.boyi.service.RepositoryBuyoutDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 仓库模块-采购退料单据表 服务实现类
 * </p>
 *
 * @author sunke
 * @since 2021-08-26
 */
@Service
public class RepositoryBuyoutDocumentServiceImpl extends ServiceImpl<RepositoryBuyoutDocumentMapper, RepositoryBuyoutDocument> implements RepositoryBuyoutDocumentService {
    @Autowired
    RepositoryBuyoutDocumentMapper repositoryBuyoutDocumentMapper;
    public Page<RepositoryBuyoutDocument> innerQuery(Page page, QueryWrapper<RepositoryBuyoutDocument> eq) {
        return repositoryBuyoutDocumentMapper.page(page,eq);
    }

    @Override
    public RepositoryBuyoutDocument one(QueryWrapper<RepositoryBuyoutDocument> id) {
        return repositoryBuyoutDocumentMapper.one(id);
    }

    @Override
    public Page<RepositoryBuyoutDocument> innerQueryBySearch(Page page,String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate,List<Long> searchStatus) {
        return this.innerQuery(page,
                new QueryWrapper<RepositoryBuyoutDocument>().
                        like(StrUtil.isNotBlank(searchStr)
                                && StrUtil.isNotBlank(searchField),queryField,searchStr)
                        .ge(StrUtil.isNotBlank(searchStartDate),DBConstant.TABLE_REPOSITORY_BUYOUT_DOCUMENT.BUY_OUT_DATE_FIELDNAME,searchStartDate)
                        .le(StrUtil.isNotBlank(searchEndDate),DBConstant.TABLE_REPOSITORY_BUYOUT_DOCUMENT.BUY_OUT_DATE_FIELDNAME,searchEndDate)
                        .in(searchStatus != null && searchStatus.size() > 0,DBConstant.TABLE_REPOSITORY_BUYOUT_DOCUMENT.STATUS_FIELDNAME,searchStatus)
        );

    }

    @Override
    public Page<RepositoryBuyoutDocument> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate, List<Long> searchStatus, Map<String, String> otherSearch) {
        QueryWrapper<RepositoryBuyoutDocument> queryWrapper = new QueryWrapper<>();
        for (String key : otherSearch.keySet()){
            String val = otherSearch.get(key);
            queryWrapper.like(StrUtil.isNotBlank(val) && !val.equals("null")
                    && StrUtil.isNotBlank(key),key,val);
        }
        return this.innerQuery(page,
                queryWrapper
                        .like(StrUtil.isNotBlank(searchStr) &&!searchStr.equals("null")
                                && StrUtil.isNotBlank(searchField),queryField,searchStr)
                        .ge(StrUtil.isNotBlank(searchStartDate)&& !searchStartDate.equals("null"),DBConstant.TABLE_REPOSITORY_BUYOUT_DOCUMENT.BUY_OUT_DATE_FIELDNAME,searchStartDate)
                        .le(StrUtil.isNotBlank(searchEndDate)&& !searchEndDate.equals("null"),DBConstant.TABLE_REPOSITORY_BUYOUT_DOCUMENT.BUY_OUT_DATE_FIELDNAME,searchEndDate)
                        .in(searchStatus != null && searchStatus.size() > 0,DBConstant.TABLE_REPOSITORY_BUYOUT_DOCUMENT.STATUS_FIELDNAME,searchStatus)
        );
    }

    @Override
    public Double countBySupplierIdAndMaterialId(String supplierId, String materialId) {
        Double returnCount = repositoryBuyoutDocumentMapper.getSumNumBySupplierIdAndMaterialId(supplierId, materialId);
        returnCount  = returnCount==null?0L:returnCount;
        return returnCount;
    }

    @Override
    public List<RepositoryBuyoutDocument> countLTByCloseDate(LocalDate closeDate) {
        return this.list(new QueryWrapper<RepositoryBuyoutDocument>()
                .le(DBConstant.TABLE_REPOSITORY_BUYOUT_DOCUMENT.BUY_OUT_DATE_FIELDNAME, closeDate)
                .ne(DBConstant.TABLE_REPOSITORY_BUYOUT_DOCUMENT.STATUS_FIELDNAME,
                        DBConstant.TABLE_REPOSITORY_BUYOUT_DOCUMENT.STATUS_FIELDVALUE_0));
    }

    @Override
    public List<RepositoryBuyoutDocument> listGTEndDate(String endDate) {
        return this.repositoryBuyoutDocumentMapper.listGTEndDate(endDate);
    }

    @Override
    public List<RepositoryBuyoutDocument> getSupplierTotalAmountBetweenDate(LocalDate startDateTime, LocalDate endDateTime) {
        return this.repositoryBuyoutDocumentMapper.getSupplierTotalAmountBetweenDate(startDateTime,endDateTime);
    }

}
