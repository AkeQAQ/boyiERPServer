package com.boyi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.*;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 库存表 服务类
 * </p>
 *
 * @author sunke
 * @since 2021-09-02
 */
public interface RepositoryStockService extends IService<RepositoryStock> {

    void addNumByMaterialId( String materialId, Double num);

    void subNumBySupplierIdAndMaterialId(List<RepositoryPickMaterialDetail> stocks) throws Exception;
    void subNumByMaterialId(Map<String, Double> details) throws Exception;
    void subNumReturnMaterialId(List<RepositoryReturnMaterialDetail> stocks) throws Exception;

    RepositoryStock getByMaterialId(String materialId);

    void removeByMaterialId(String[] ids);

    Page<RepositoryStock> pageBySearch(Page page, String queryField, String searchField, String searchStr);
}
