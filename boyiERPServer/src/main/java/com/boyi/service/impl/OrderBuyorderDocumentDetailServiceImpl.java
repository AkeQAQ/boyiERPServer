package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.OrderBuyorderDocument;
import com.boyi.entity.OrderBuyorderDocumentDetail;
import com.boyi.entity.OrderBuyorderDocumentDetail;
import com.boyi.entity.RepositoryBuyinDocument;
import com.boyi.mapper.OrderBuyorderDocumentDetailMapper;
import com.boyi.service.OrderBuyorderDocumentDetailService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 订单模块-采购订单-详情内容 服务实现类
 * </p>
 *
 * @author sunke
 * @since 2021-09-04
 */
@Service
public class OrderBuyorderDocumentDetailServiceImpl extends ServiceImpl<OrderBuyorderDocumentDetailMapper, OrderBuyorderDocumentDetail> implements OrderBuyorderDocumentDetailService {
    @Override
    public boolean delByDocumentIds(Long[] ids) {
        return this.remove(new QueryWrapper<OrderBuyorderDocumentDetail>()
                .in(DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT_DETAIL.DOCUMENT_ID_FIELDNAME, ids));
    }

    @Override
    public List<OrderBuyorderDocumentDetail> listByDocumentId(Long id) {
        return this.list(new QueryWrapper<OrderBuyorderDocumentDetail>()
                .eq(DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT_DETAIL.DOCUMENT_ID_FIELDNAME, id)
                .orderByAsc(DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT_DETAIL.ID_FIELDNAME));
    }

    @Override
    public boolean removeByDocId(Long docId) {
        return this.remove(new QueryWrapper<OrderBuyorderDocumentDetail>().eq(DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT_DETAIL.DOCUMENT_ID_FIELDNAME, docId));
    }

    @Override
    public int countByMaterialId(String[] ids) {
        return this.count(new QueryWrapper<OrderBuyorderDocumentDetail>()
                .in(DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT_DETAIL.MATERIAL_ID_FIELDNAME, ids));
    }

    @Override
    public int countBySupplierId(String[] ids) {
        return this.count(new QueryWrapper<OrderBuyorderDocumentDetail>()
                .in(DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT_DETAIL.SUPPLIER_ID_FIELDNAME, ids));
    }

    @Override
    public void statusSuccess(Long[] orderDetailIds) {
        UpdateWrapper<OrderBuyorderDocumentDetail> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set(DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT_DETAIL.STATUS_FIELDNAME
                ,DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT_DETAIL.STATUS_FIELDVALUE_0)
                .in(DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT_DETAIL.ID_FIELDNAME,orderDetailIds);
        this.update(updateWrapper);
    }


    @Override
    public void statusNotSuccess(List<Long> orderDetailIds) {
        UpdateWrapper<OrderBuyorderDocumentDetail> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set(DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT_DETAIL.STATUS_FIELDNAME
                        ,DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT_DETAIL.STATUS_FIELDVALUE_1)
                .in(DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT_DETAIL.ID_FIELDNAME,orderDetailIds);
        this.update(updateWrapper);
    }

    @Override
    public List<OrderBuyorderDocumentDetail> getByMaterialIdAndOrderSeq(String materialId, String docNum) {
        return this.list(new QueryWrapper<OrderBuyorderDocumentDetail>()
                .eq(DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT_DETAIL.MATERIAL_ID_FIELDNAME,materialId)
                .eq(DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT_DETAIL.ORDER_SEQ_FIELDNAME,docNum));
    }
}
