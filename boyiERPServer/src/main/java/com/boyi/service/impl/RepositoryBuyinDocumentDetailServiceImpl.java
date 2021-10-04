package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.RepositoryBuyinDocumentDetail;
import com.boyi.entity.RepositoryStock;
import com.boyi.mapper.RepositoryBuyinDocumentDetailMapper;
import com.boyi.service.RepositoryBuyinDocumentDetailService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyi.service.RepositoryStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 仓库模块-采购入库单-详情内容 服务实现类
 * </p>
 *
 * @author sunke
 * @since 2021-08-26
 */
@Service
public class RepositoryBuyinDocumentDetailServiceImpl extends ServiceImpl<RepositoryBuyinDocumentDetailMapper, RepositoryBuyinDocumentDetail> implements RepositoryBuyinDocumentDetailService {

    @Autowired
    RepositoryStockService repositoryStockService;

    @Override
    public boolean delByDocumentIds(Long[] ids) {
        return this.remove(new QueryWrapper<RepositoryBuyinDocumentDetail>()
                .in(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT_DETAIL.DOCUMENT_ID_FIELDNAME, ids));
    }

    @Override
    public List<RepositoryBuyinDocumentDetail> listByDocumentId(Long id) {
        return this.list(new QueryWrapper<RepositoryBuyinDocumentDetail>().eq(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT_DETAIL.DOCUMENT_ID_FIELDNAME, id)
                .orderByAsc(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT_DETAIL.ORDER_SEQ_FIELDNAME)
                .orderByAsc(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT_DETAIL.ID_FIELDNAME));
    }

    @Override
    public boolean removeByDocId(Long docId) {
        return this.remove(new QueryWrapper<RepositoryBuyinDocumentDetail>().eq(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT_DETAIL.DOCUMENT_ID_FIELDNAME, docId));

    }

    @Override
    public int countByMaterialId(String[] ids) {
        return this.count(new QueryWrapper<RepositoryBuyinDocumentDetail>()
                .in(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT_DETAIL.MATERIAL_ID_FIELDNAME, ids));
    }

    @Override
    public List<RepositoryBuyinDocumentDetail> listByOrderDetailId(Long[] orderDetailIds) {
        return this.list(new QueryWrapper<RepositoryBuyinDocumentDetail>()
                .in(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT_DETAIL.ORDER_DETAIL_ID_FIELDNAME, orderDetailIds)
                .orderByAsc(DBConstant.TABLE_REPOSITORY_BUYIN_DOCUMENT_DETAIL.ORDER_SEQ_FIELDNAME));
    }
    @Override
    public void removeByDocIdAndInIds(Long id, List<Long> detailIds) {
        this.remove(new QueryWrapper<RepositoryBuyinDocumentDetail>()
                .in(DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT_DETAIL.ID_FIELDNAME,detailIds)
                .eq(DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT_DETAIL.DOCUMENT_ID_FIELDNAME,id));
    }
}
