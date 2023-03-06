package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.ExternalAccountRepositoryPickMaterialDetail;
import com.boyi.entity.ExternalAccountRepositoryPickMaterialDetail;
import com.boyi.mapper.ExternalAccountRepositoryPickMaterialDetailMapper;
import com.boyi.service.ExternalAccountRepositoryPickMaterialDetailService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 仓库模块-领料模块-详情表 服务实现类
 * </p>
 *
 * @author sunke
 * @since 2023-03-04
 */
@Service
public class ExternalAccountRepositoryPickMaterialDetailServiceImpl extends ServiceImpl<ExternalAccountRepositoryPickMaterialDetailMapper, ExternalAccountRepositoryPickMaterialDetail> implements ExternalAccountRepositoryPickMaterialDetailService {

    @Override
    public boolean delByDocumentIds(Long[] ids) {
        return this.remove(new QueryWrapper<ExternalAccountRepositoryPickMaterialDetail>()
                .in(DBConstant.TABLE_EA_REPOSITORY_PICK_MATERIAL_DETAIL.DOCUMENT_ID_FIELDNAME, ids));
    }

    @Override
    public List<ExternalAccountRepositoryPickMaterialDetail> listByDocumentId(Long id) {
        return this.list(new QueryWrapper<ExternalAccountRepositoryPickMaterialDetail>()
                .eq(DBConstant.TABLE_EA_REPOSITORY_PICK_MATERIAL_DETAIL.DOCUMENT_ID_FIELDNAME, id)
                .orderByAsc(DBConstant.TABLE_EA_REPOSITORY_PICK_MATERIAL_DETAIL.ID_FIELDNAME)

        );
    }

    @Override
    public boolean removeByDocId(Long docId) {
        return this.remove(new QueryWrapper<ExternalAccountRepositoryPickMaterialDetail>().eq(DBConstant.TABLE_EA_REPOSITORY_PICK_MATERIAL_DETAIL.DOCUMENT_ID_FIELDNAME, docId));
    }

    @Override
    public int countByMaterialId(String[] ids) {
        return this.count(new QueryWrapper<ExternalAccountRepositoryPickMaterialDetail>()
                .in(DBConstant.TABLE_EA_REPOSITORY_PICK_MATERIAL_DETAIL.MATERIAL_ID_FIELDNAME, ids));
    }
}
