package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.BaseSupplierMaterial;
import com.boyi.entity.RepositoryBuyinDocument;
import com.boyi.mapper.RepositoryBuyinDocumentMapper;
import com.boyi.service.RepositoryBuyinDocumentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 仓库模块-采购入库单据表 服务实现类
 * </p>
 *
 * @author sunke
 * @since 2021-08-26
 */
@Service
public class RepositoryBuyinDocumentServiceImpl extends ServiceImpl<RepositoryBuyinDocumentMapper, RepositoryBuyinDocument> implements RepositoryBuyinDocumentService {
    @Autowired
    RepositoryBuyinDocumentMapper repositoryBuyinDocumentMapper;
    public Page<RepositoryBuyinDocument> innerQuery(Page page, QueryWrapper<RepositoryBuyinDocument> eq) {
        return repositoryBuyinDocumentMapper.page(page,eq);
    }

    @Override
    public RepositoryBuyinDocument one(QueryWrapper<RepositoryBuyinDocument> id) {
        return repositoryBuyinDocumentMapper.one(id);
    }

    @Override
    public Integer getBySupplierMaterial(BaseSupplierMaterial baseSupplierMaterial){
        return repositoryBuyinDocumentMapper.getBySupplierMaterial(baseSupplierMaterial);
    }

    @Override
    public int countSupplierOneDocNumExcludSelf(String supplierDocumentNum, String supplierId, Long id) {
        return this.count(new QueryWrapper<RepositoryBuyinDocument>().
                eq(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.SUPPLIER_DOCUMENT_NUM_FIELDNAME, supplierDocumentNum)
                .eq(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.SUPPLIER_ID_FIELDNAME,supplierId)
                .ne(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.ID_FIELDNAME,id)
        );
    }

    @Override
    public int countSupplierOneDocNum(String supplierDocumentNum, String supplierId) {
        return this.count(new QueryWrapper<RepositoryBuyinDocument>().
                eq(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.SUPPLIER_DOCUMENT_NUM_FIELDNAME, supplierDocumentNum)
                .eq(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.SUPPLIER_ID_FIELDNAME,supplierId)
        );
    }

    @Override
    public Page<RepositoryBuyinDocument> innerQueryBySearch(Page page,String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate) {
        return this.innerQuery(page,
                new QueryWrapper<RepositoryBuyinDocument>().
                        like(StrUtil.isNotBlank(searchStr)
                                && StrUtil.isNotBlank(searchField),queryField,searchStr)
                        .ge(StrUtil.isNotBlank(searchStartDate),DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.BUY_IN_DATE_FIELDNAME,searchStartDate)
                        .le(StrUtil.isNotBlank(searchEndDate),DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.BUY_IN_DATE_FIELDNAME,searchEndDate));
    }

    @Override
    public int countBySupplierId(String ids[]) {
        return this.count(new QueryWrapper<RepositoryBuyinDocument>()
                .in(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.SUPPLIER_ID_FIELDNAME, ids));
    }

    @Override
    public RepositoryBuyinDocument getByOrderId(Long id) {
        return this.getOne(new QueryWrapper<RepositoryBuyinDocument>()
                .eq(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT.ORDER_ID_FIELDNAME, id));
    }

}
