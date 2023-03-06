package com.boyi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.ExternalAccountBaseSupplierMaterial;
import com.boyi.entity.ExternalAccountBaseSupplierMaterial;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 供应商-物料报价表 服务类
 * </p>
 *
 * @author sunke
 * @since 2023-03-04
 */
public interface ExternalAccountBaseSupplierMaterialService extends IService<ExternalAccountBaseSupplierMaterial> {

    Page<ExternalAccountBaseSupplierMaterial> innerQuery(Page page, QueryWrapper<ExternalAccountBaseSupplierMaterial> eq);
    ExternalAccountBaseSupplierMaterial one( QueryWrapper<ExternalAccountBaseSupplierMaterial> eq);
    List<ExternalAccountBaseSupplierMaterial> myList(QueryWrapper<ExternalAccountBaseSupplierMaterial> eq);

    int isRigionExcludeSelf( ExternalAccountBaseSupplierMaterial baseSupplierMaterial);

    ExternalAccountBaseSupplierMaterial getSuccessPrice(String supplierId, String materialId, LocalDate buyInDate);

    int countSuccessByMaterialId(String id);

    int countByMaterialId(String[] ids);

    int countSuccessBySupplierId(String id);

    int countBySupplierId(String[] ids);

    Page<ExternalAccountBaseSupplierMaterial> innerQueryBySearch(Page page, String queryField, String searchField, String searchStr);
    Page<ExternalAccountBaseSupplierMaterial> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr,  List<Long> searchStatus, Map<String,String> otherSearch);

    int isRigion(ExternalAccountBaseSupplierMaterial baseSupplierMaterial);

    List<ExternalAccountBaseSupplierMaterial> listByMaterialId(String materialId);

    List<ExternalAccountBaseSupplierMaterial> listByMaterialIdWithSuccessDate(String innerMaterialId, LocalDate now);
}
