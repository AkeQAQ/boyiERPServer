package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.RepositoryBuyoutDocumentDetail;
import com.boyi.mapper.RepositoryBuyoutDocumentDetailMapper;
import com.boyi.mapper.RepositoryBuyoutDocumentDetailMapper;
import com.boyi.service.RepositoryBuyoutDocumentDetailService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 仓库模块-采购退料单-详情内容 服务实现类
 * </p>
 *
 * @author sunke
 * @since 2021-08-26
 */
@Service
public class RepositoryBuyoutDocumentDetailServiceImpl extends ServiceImpl<RepositoryBuyoutDocumentDetailMapper, RepositoryBuyoutDocumentDetail> implements RepositoryBuyoutDocumentDetailService {

    @Override
    public boolean delByDocumentIds(Long[] ids) {
        return this.remove(new QueryWrapper<RepositoryBuyoutDocumentDetail>()
                .in(DBConstant.TABLE_REPOSITORY_BUYOUT_DOCUMENT_DETAIL.DOCUMENT_ID_FIELDNAME, ids));
    }

    @Override
    public List<RepositoryBuyoutDocumentDetail> listByDocumentId(Long id) {
        return this.list(new QueryWrapper<RepositoryBuyoutDocumentDetail>()
                .eq(DBConstant.TABLE_REPOSITORY_BUYOUT_DOCUMENT_DETAIL.DOCUMENT_ID_FIELDNAME, id)
                .orderByAsc(DBConstant.TABLE_REPOSITORY_BUYOUT_DOCUMENT_DETAIL.ID_FIELDNAME)
               );
    }

    @Override
    public boolean removeByDocId(Long docId) {
        return this.remove(new QueryWrapper<RepositoryBuyoutDocumentDetail>().eq(DBConstant.TABLE_REPOSITORY_BUYOUT_DOCUMENT_DETAIL.DOCUMENT_ID_FIELDNAME, docId));
    }

    @Override
    public int countByMaterialId(String[] ids) {
        return this.count(new QueryWrapper<RepositoryBuyoutDocumentDetail>()
                .in(DBConstant.TABLE_REPOSITORY_BUYOUT_DOCUMENT_DETAIL.MATERIAL_ID_FIELDNAME, ids));
    }
}
