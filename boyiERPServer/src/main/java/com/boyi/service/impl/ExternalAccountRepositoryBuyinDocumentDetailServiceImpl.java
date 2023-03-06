package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.ExternalAccountRepositoryBuyinDocumentDetail;
import com.boyi.entity.ExternalAccountRepositoryBuyinDocumentDetail;
import com.boyi.mapper.ExternalAccountRepositoryBuyinDocumentDetailMapper;
import com.boyi.service.ExternalAccountRepositoryBuyinDocumentDetailService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyi.service.RepositoryStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 仓库模块-采购入库单-详情内容 服务实现类
 * </p>
 *
 * @author sunke
 * @since 2023-03-04
 */
@Service
public class ExternalAccountRepositoryBuyinDocumentDetailServiceImpl extends ServiceImpl<ExternalAccountRepositoryBuyinDocumentDetailMapper, ExternalAccountRepositoryBuyinDocumentDetail> implements ExternalAccountRepositoryBuyinDocumentDetailService {

    @Override
    public boolean delByDocumentIds(Long[] ids) {
        return this.remove(new QueryWrapper<ExternalAccountRepositoryBuyinDocumentDetail>()
                .in(DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT_DETAIL.DOCUMENT_ID_FIELDNAME, ids));
    }

    @Override
    public List<ExternalAccountRepositoryBuyinDocumentDetail> listByDocumentId(Long id) {
        return this.list(new QueryWrapper<ExternalAccountRepositoryBuyinDocumentDetail>().eq(DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT_DETAIL.DOCUMENT_ID_FIELDNAME, id)
                .orderByAsc(DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT_DETAIL.ORDER_SEQ_FIELDNAME)
                .orderByAsc(DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT_DETAIL.ID_FIELDNAME));
    }

    @Override
    public boolean removeByDocId(Long docId) {
        return this.remove(new QueryWrapper<ExternalAccountRepositoryBuyinDocumentDetail>().eq(DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT_DETAIL.DOCUMENT_ID_FIELDNAME, docId));
    }

    @Override
    public int countByMaterialId(String[] ids) {
        return this.count(new QueryWrapper<ExternalAccountRepositoryBuyinDocumentDetail>()
                .in(DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT_DETAIL.MATERIAL_ID_FIELDNAME, ids));
    }

    @Override
    public List<ExternalAccountRepositoryBuyinDocumentDetail> listByOrderDetailId(Long[] orderDetailIds) {
        return this.list(new QueryWrapper<ExternalAccountRepositoryBuyinDocumentDetail>()
                .in(DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT_DETAIL.ORDER_DETAIL_ID_FIELDNAME, orderDetailIds)
                .orderByAsc(DBConstant.TABLE_EA_REPOSITORY_BUYIN_DOCUMENT_DETAIL.ORDER_SEQ_FIELDNAME));
    }
    @Override
    public void removeByDocIdAndInIds(Long id, List<Long> detailIds) {
        this.remove(new QueryWrapper<ExternalAccountRepositoryBuyinDocumentDetail>()
                .in(DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT_DETAIL.ID_FIELDNAME,detailIds)
                .eq(DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT_DETAIL.DOCUMENT_ID_FIELDNAME,id));
    }
}
