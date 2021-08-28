package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.BaseSupplierMaterial;
import com.boyi.entity.RepositoryBuyinDocument;
import com.boyi.mapper.RepositoryBuyinDocumentMapper;
import com.boyi.service.RepositoryBuyinDocumentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 仓库模块-采购入库单据表 服务实现类
 * </p>
 *
 * @author sunke
 * @since 2021-08-26
 */
@Service
public class RepositoryBuyinDocumentServiceImpl extends ServiceImpl<RepositoryBuyinDocumentMapper, RepositoryBuyinDocument> implements RepositoryBuyinDocumentService {
    @Autowired
    RepositoryBuyinDocumentMapper repositoryBuyinDocumentMapper;
    public Page<RepositoryBuyinDocument> innerQuery(Page page, QueryWrapper<RepositoryBuyinDocument> eq) {
        return repositoryBuyinDocumentMapper.page(page,eq);
    }

    @Override
    public RepositoryBuyinDocument one(QueryWrapper<RepositoryBuyinDocument> id) {
        return repositoryBuyinDocumentMapper.one(id);
    }

    @Override
    public Integer getBySupplierMaterial(BaseSupplierMaterial baseSupplierMaterial){
        return repositoryBuyinDocumentMapper.getBySupplierMaterial(baseSupplierMaterial);
    }
}
