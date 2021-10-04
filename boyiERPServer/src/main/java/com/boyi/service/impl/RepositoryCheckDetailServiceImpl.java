package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.RepositoryCheckDetail;
import com.boyi.mapper.RepositoryCheckDetailMapper;
import com.boyi.service.RepositoryCheckDetailService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RepositoryCheckDetailServiceImpl extends ServiceImpl<RepositoryCheckDetailMapper, RepositoryCheckDetail> implements RepositoryCheckDetailService {

    @Override
    public boolean delByDocumentIds(Long[] ids) {
        return this.remove(new QueryWrapper<RepositoryCheckDetail>()
                .in(DBConstant.TABLE_REPOSITORY_CHECK_DETAIL.DOCUMENT_ID_FIELDNAME, ids));
    }

    @Override
    public List<RepositoryCheckDetail> listByDocumentId(Long id) {
        return this.list(new QueryWrapper<RepositoryCheckDetail>()
                .eq(DBConstant.TABLE_REPOSITORY_CHECK_DETAIL.DOCUMENT_ID_FIELDNAME, id)
                .orderByAsc(DBConstant.TABLE_REPOSITORY_CHECK_DETAIL.ID_FIELDNAME)
        );
    }

    @Override
    public boolean removeByDocId(Long docId) {
        return this.remove(new QueryWrapper<RepositoryCheckDetail>().eq(DBConstant.TABLE_REPOSITORY_CHECK_DETAIL.DOCUMENT_ID_FIELDNAME, docId));
    }

    @Override
    public int countByMaterialId(String[] ids) {
        return this.count(new QueryWrapper<RepositoryCheckDetail>()
                .in(DBConstant.TABLE_REPOSITORY_CHECK_DETAIL.MATERIAL_ID_FIELDNAME, ids));
    }
}
