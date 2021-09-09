package com.boyi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boyi.entity.RepositoryBuyoutDocument;

/**
 * <p>
 * 仓库模块-采购退料单据表 服务类
 * </p>
 *
 * @author sunke
 * @since 2021-08-26
 */
public interface RepositoryBuyoutDocumentService extends IService<RepositoryBuyoutDocument> {

    Page<RepositoryBuyoutDocument> innerQuery(Page page, QueryWrapper<RepositoryBuyoutDocument> like);

    RepositoryBuyoutDocument one(QueryWrapper<RepositoryBuyoutDocument> id);

    Page<RepositoryBuyoutDocument> innerQueryBySearch(Page page,String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate);

    Double countBySupplierIdAndMaterialId(String supplierId, String materialId);
}
