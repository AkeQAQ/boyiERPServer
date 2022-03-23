package com.boyi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boyi.entity.BaseSupplierMaterial;
import com.boyi.entity.BaseSupplierMaterialCopy;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 供应商-物料报价表 服务类
 * </p>
 *
 * @author sunke
 * @since 2021-08-24
 */
public interface BaseSupplierMaterialCopyService extends IService<BaseSupplierMaterialCopy> {

    Page<BaseSupplierMaterialCopy> innerQuery(Page page, QueryWrapper<BaseSupplierMaterialCopy> eq);
    BaseSupplierMaterialCopy one( QueryWrapper<BaseSupplierMaterialCopy> eq);
    List<BaseSupplierMaterialCopy> myList(QueryWrapper<BaseSupplierMaterialCopy> eq);

    int isRigionExcludeSelf( BaseSupplierMaterialCopy baseSupplierMaterial);

    BaseSupplierMaterialCopy getSuccessPrice(String supplierId, String materialId, LocalDate buyInDate);

    int countSuccessByMaterialId(String id);

    int countByMaterialId(String[] ids);

    int countSuccessBySupplierId(String id);

    int countBySupplierId(String[] ids);

    Page<BaseSupplierMaterialCopy> innerQueryBySearch(Page page, String queryField, String searchField, String searchStr);
    Page<BaseSupplierMaterialCopy> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr,  List<Long> searchStatus, Map<String,String> otherSearch);

    int isRigion(BaseSupplierMaterialCopy baseSupplierMaterial);

}
