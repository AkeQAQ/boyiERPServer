package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.RepositoryPickMaterialDetail;
import com.boyi.entity.RepositoryPickMaterialDetail;
import com.boyi.mapper.RepositoryPickMaterialDetailMapper;
import com.boyi.service.RepositoryPickMaterialDetailService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 仓库模块-领料模块-详情表 服务实现类
 * </p>
 *
 * @author sunke
 * @since 2021-09-05
 */
@Service
public class RepositoryPickMaterialDetailServiceImpl extends ServiceImpl<RepositoryPickMaterialDetailMapper, RepositoryPickMaterialDetail> implements RepositoryPickMaterialDetailService {

    @Override
    public boolean delByDocumentIds(Long[] ids) {
        return this.remove(new QueryWrapper<RepositoryPickMaterialDetail>()
                .in(DBConstant.TABLE_REPOSITORY_PICK_MATERIAL_DETAIL.DOCUMENT_ID_FIELDNAME, ids));
    }

    @Override
    public List<RepositoryPickMaterialDetail> listByDocumentId(Long id) {
        return this.list(new QueryWrapper<RepositoryPickMaterialDetail>().eq(DBConstant.TABLE_REPOSITORY_PICK_MATERIAL_DETAIL.DOCUMENT_ID_FIELDNAME, id)
               );
    }

    @Override
    public boolean removeByDocId(Long docId) {
        return this.remove(new QueryWrapper<RepositoryPickMaterialDetail>().eq(DBConstant.TABLE_REPOSITORY_PICK_MATERIAL_DETAIL.DOCUMENT_ID_FIELDNAME, docId));
    }

    @Override
    public int countByMaterialId(String[] ids) {
        return this.count(new QueryWrapper<RepositoryPickMaterialDetail>()
                .in(DBConstant.TABLE_REPOSITORY_PICK_MATERIAL_DETAIL.MATERIAL_ID_FIELDNAME, ids));
    }
}
