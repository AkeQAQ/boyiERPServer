package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.RepositoryReturnMaterialDetail;
import com.boyi.mapper.RepositoryReturnMaterialDetailMapper;
import com.boyi.mapper.RepositoryReturnMaterialDetailMapper;
import com.boyi.service.RepositoryReturnMaterialDetailService;
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
public class RepositoryReturnMaterialDetailServiceImpl extends ServiceImpl<RepositoryReturnMaterialDetailMapper, RepositoryReturnMaterialDetail> implements RepositoryReturnMaterialDetailService {

    @Override
    public boolean delByDocumentIds(Long[] ids) {
        return this.remove(new QueryWrapper<RepositoryReturnMaterialDetail>()
                .in(DBConstant.TABLE_REPOSITORY_RETURN_MATERIAL_DETAIL.DOCUMENT_ID_FIELDNAME, ids));
    }

    @Override
    public List<RepositoryReturnMaterialDetail> listByDocumentId(Long id) {
        return this.list(new QueryWrapper<RepositoryReturnMaterialDetail>().eq(DBConstant.TABLE_REPOSITORY_RETURN_MATERIAL_DETAIL.DOCUMENT_ID_FIELDNAME, id)
               );
    }

    @Override
    public boolean removeByDocId(Long docId) {
        return this.remove(new QueryWrapper<RepositoryReturnMaterialDetail>().eq(DBConstant.TABLE_REPOSITORY_RETURN_MATERIAL_DETAIL.DOCUMENT_ID_FIELDNAME, docId));
    }

    @Override
    public int countByMaterialId(String[] ids) {
        return this.count(new QueryWrapper<RepositoryReturnMaterialDetail>()
                .in(DBConstant.TABLE_REPOSITORY_RETURN_MATERIAL_DETAIL.MATERIAL_ID_FIELDNAME, ids));
    }
}
