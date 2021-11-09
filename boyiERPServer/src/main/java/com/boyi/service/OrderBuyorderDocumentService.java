package com.boyi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.BaseSupplierMaterial;
import com.boyi.entity.OrderBuyorderDocument;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boyi.entity.RepositoryBuyinDocument;

import java.time.LocalDate;
import java.util.Map;

/**
 * <p>
 * 订单模块-采购订单单据表 服务类
 * </p>
 *
 * @author sunke
 * @since 2021-09-04
 */
public interface OrderBuyorderDocumentService extends IService<OrderBuyorderDocument> {
    Page<OrderBuyorderDocument> innerQuery(Page page, QueryWrapper<OrderBuyorderDocument> like);

    OrderBuyorderDocument one(QueryWrapper<OrderBuyorderDocument> id);

    Integer getBySupplierMaterial(BaseSupplierMaterial baseSupplierMaterial);

    Page<OrderBuyorderDocument> innerQueryBySearch(Page page,String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate);
    Page<OrderBuyorderDocument> innerQueryByManySearch(Page page,String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate, Map<String,String> otherSearch);

    int countBySupplierId(String ids[]);

    void statusSuccess(Long id);

    void statusNotSuccess(Long id);
}
