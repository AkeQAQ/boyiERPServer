package com.boyi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.BaseSupplierMaterial;
import com.boyi.entity.RepositoryBuyinDocument;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 仓库模块-采购入库单据表 服务类
 * </p>
 *
 * @author sunke
 * @since 2021-08-26
 */
public interface RepositoryBuyinDocumentService extends IService<RepositoryBuyinDocument> {

    Page<RepositoryBuyinDocument> innerQuery(Page page, QueryWrapper<RepositoryBuyinDocument> like);

    RepositoryBuyinDocument one(QueryWrapper<RepositoryBuyinDocument> id);

    Integer getBySupplierMaterial(BaseSupplierMaterial baseSupplierMaterial);
}
