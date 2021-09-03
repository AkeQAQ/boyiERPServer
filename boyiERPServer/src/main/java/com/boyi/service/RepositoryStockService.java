package com.boyi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.BaseSupplierMaterial;
import com.boyi.entity.RepositoryStock;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 库存表 服务类
 * </p>
 *
 * @author sunke
 * @since 2021-09-02
 */
public interface RepositoryStockService extends IService<RepositoryStock> {

    void addNumBySupplierIdAndMaterialId(String supplierId, String materialId, Double num);

    void subNumBySupplierIdAndMaterialId(String supplierId, String materialId, Double num) throws Exception;

    RepositoryStock getBySupplierIdAndMaterialId(String supplierId, String materialId);

    void removeByMaterialId(String[] ids);

    void removeBySupplierId(String[] ids);

    Page<RepositoryStock> pageBySearch(Page page, String queryField, String searchField, String searchStr);
}
