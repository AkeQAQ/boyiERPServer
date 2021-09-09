package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.BaseSupplierMaterial;
import com.boyi.entity.RepositoryBuyoutDocument;
import com.boyi.mapper.RepositoryBuyoutDocumentMapper;
import com.boyi.mapper.RepositoryBuyoutDocumentMapper;
import com.boyi.service.RepositoryBuyoutDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public Page<RepositoryBuyoutDocument> innerQueryBySearch(Page page,String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate) {
        return this.innerQuery(page,
                new QueryWrapper<RepositoryBuyoutDocument>().
                        like(StrUtil.isNotBlank(searchStr)
                                && StrUtil.isNotBlank(searchField),queryField,searchStr)
                        .ge(StrUtil.isNotBlank(searchStartDate),DBConstant.TABLE_REPOSITORY_BUYOUT_DOCUMENT.BUY_OUT_DATE_FIELDNAME,searchStartDate)
                        .le(StrUtil.isNotBlank(searchEndDate),DBConstant.TABLE_REPOSITORY_BUYOUT_DOCUMENT.BUY_OUT_DATE_FIELDNAME,searchEndDate));
    }

    @Override
    public Double countBySupplierIdAndMaterialId(String supplierId, String materialId) {
        return repositoryBuyoutDocumentMapper.getSumNumBySupplierIdAndMaterialId(supplierId,materialId);
    }

}
