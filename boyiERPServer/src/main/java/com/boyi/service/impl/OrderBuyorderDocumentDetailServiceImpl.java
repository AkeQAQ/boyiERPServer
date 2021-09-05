package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.OrderBuyorderDocumentDetail;
import com.boyi.entity.OrderBuyorderDocumentDetail;
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
        return this.list(new QueryWrapper<OrderBuyorderDocumentDetail>().eq(DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT_DETAIL.DOCUMENT_ID_FIELDNAME, id)
                .orderByAsc(DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT_DETAIL.ORDER_SEQ_FIELDNAME));
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
}
