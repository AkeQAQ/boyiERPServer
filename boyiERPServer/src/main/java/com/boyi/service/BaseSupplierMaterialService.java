package com.boyi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.BaseSupplierMaterial;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 供应商-物料报价表 服务类
 * </p>
 *
 * @author sunke
 * @since 2021-08-24
 */
public interface BaseSupplierMaterialService extends IService<BaseSupplierMaterial> {

    Page<BaseSupplierMaterial> innerQuery(Page page, QueryWrapper<BaseSupplierMaterial> eq);
    BaseSupplierMaterial one( QueryWrapper<BaseSupplierMaterial> eq);
    List<BaseSupplierMaterial> myList(QueryWrapper<BaseSupplierMaterial> eq);

    int isRigionExcludeSelf( BaseSupplierMaterial baseSupplierMaterial);

    BaseSupplierMaterial getSuccessPrice(String supplierId, String materialId, LocalDate buyInDate);

    int countSuccessByMaterialId(String id);

    int countByMaterialId(String[] ids);

    int countSuccessBySupplierId(String id);

    int countBySupplierId(String[] ids);

    Page<BaseSupplierMaterial> innerQueryBySearch(Page page, String queryField, String searchField, String searchStr);

    int isRigion(BaseSupplierMaterial baseSupplierMaterial);
}
