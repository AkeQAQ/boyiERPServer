package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.BaseMaterialSameGroup;
import com.boyi.entity.BaseMaterialSameGroupDetail;
import com.boyi.mapper.BaseMaterialSameGroupDetailMapper;
import com.boyi.service.BaseMaterialSameGroupDetailService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sunke
 * @since 2022-11-14
 */
@Service
public class BaseMaterialSameGroupDetailServiceImpl extends ServiceImpl<BaseMaterialSameGroupDetailMapper, BaseMaterialSameGroupDetail> implements BaseMaterialSameGroupDetailService {

    @Override
    public void removeByGroupId(Long id) {
        this.remove(new QueryWrapper<BaseMaterialSameGroupDetail>()
                .eq(DBConstant.TABLE_BASE_MATERIAL_SAME_GROUP_DETAIL.GROUP_ID_FIELDNAME,id));
    }

    @Override
    public List<BaseMaterialSameGroupDetail> listByGroupId(Long id) {
        return this.list(new QueryWrapper<BaseMaterialSameGroupDetail>()
                .eq(DBConstant.TABLE_BASE_MATERIAL_SAME_GROUP_DETAIL.GROUP_ID_FIELDNAME,id));
    }
}
