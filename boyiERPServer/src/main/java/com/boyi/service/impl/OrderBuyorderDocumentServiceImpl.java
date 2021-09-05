package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.BaseSupplierMaterial;
import com.boyi.entity.OrderBuyorderDocument;
import com.boyi.entity.OrderBuyorderDocument;
import com.boyi.mapper.OrderBuyorderDocumentMapper;
import com.boyi.mapper.OrderBuyorderDocumentMapper;
import com.boyi.service.OrderBuyorderDocumentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * <p>
 * 订单模块-采购订单单据表 服务实现类
 * </p>
 *
 * @author sunke
 * @since 2021-09-04
 */
@Service
public class OrderBuyorderDocumentServiceImpl extends ServiceImpl<OrderBuyorderDocumentMapper, OrderBuyorderDocument> implements OrderBuyorderDocumentService {
    @Autowired
    OrderBuyorderDocumentMapper orderBuyorderDocumentMapper;
    public Page<OrderBuyorderDocument> innerQuery(Page page, QueryWrapper<OrderBuyorderDocument> eq) {
        return orderBuyorderDocumentMapper.page(page,eq);
    }

    @Override
    public OrderBuyorderDocument one(QueryWrapper<OrderBuyorderDocument> id) {
        return orderBuyorderDocumentMapper.one(id);
    }

    @Override
    public Integer getBySupplierMaterial(BaseSupplierMaterial baseSupplierMaterial){
        return orderBuyorderDocumentMapper.getBySupplierMaterial(baseSupplierMaterial);
    }

    @Override
    public Page<OrderBuyorderDocument> innerQueryBySearch(Page page,String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate) {
        return this.innerQuery(page,
                new QueryWrapper<OrderBuyorderDocument>().
                        like(StrUtil.isNotBlank(searchStr)
                                && StrUtil.isNotBlank(searchField),queryField,searchStr)
                        .ge(StrUtil.isNotBlank(searchStartDate),DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT.ORDER_DATE_FIELDNAME,searchStartDate)
                        .le(StrUtil.isNotBlank(searchEndDate),DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT.ORDER_DATE_FIELDNAME,searchEndDate));
    }

    @Override
    public int countBySupplierId(String ids[]) {
        return this.count(new QueryWrapper<OrderBuyorderDocument>()
                .in(DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT.SUPPLIER_ID_FIELDNAME, ids));
    }

    @Override
    public void statusSuccess(Long id, String supplierDocumentNum, LocalDate buyInDate) {
        OrderBuyorderDocument orderBuyorderDocument = new OrderBuyorderDocument();
        orderBuyorderDocument.setId(id);
        orderBuyorderDocument.setBuyInDate(buyInDate);
        orderBuyorderDocument.setSupplierDocumentNum(supplierDocumentNum);
        orderBuyorderDocument.setStatus(DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT.STATUS_FIELDVALUE_0);
        this.updateById(orderBuyorderDocument);
    }

    @Override
    public void statusNotSuccess(Long id) {
        /*OrderBuyorderDocument orderBuyorderDocument = new OrderBuyorderDocument();
        orderBuyorderDocument.setId(id);
        orderBuyorderDocument.setSupplierDocumentNum(null);
        orderBuyorderDocument.setBuyInDate(null);
        orderBuyorderDocument.setStatus(DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT.STATUS_FIELDVALUE_1);
        this.updateById(orderBuyorderDocument);*/
        UpdateWrapper<OrderBuyorderDocument> order = new UpdateWrapper<>();
        UpdateWrapper<OrderBuyorderDocument> updateWrapper = order.set(DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT.SUPPLIER_DOCUMENT_NUM_FIELDNAME, null)
                .set(DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT.BUY_IN_DATE_FIELDNAME, null)
                .set(DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT.STATUS_FIELDNAME, DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT.STATUS_FIELDVALUE_1)
                .eq(DBConstant.TABLE_ORDER_BUYORDER_DOCUMENT.ID_FIELDNAME, id);
        this.update(updateWrapper);
    }
}
