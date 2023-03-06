package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.BaseMaterialGroup;
import com.boyi.entity.ExternalAccountBaseMaterialGroup;
import com.boyi.mapper.ExternalAccountBaseMaterialGroupMapper;
import com.boyi.service.ExternalAccountBaseMaterialGroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 基础模块-物料分组表 服务实现类
 * </p>
 *
 * @author sunke
 * @since 2023-03-04
 */
@Service
public class ExternalAccountBaseMaterialGroupServiceImpl extends ServiceImpl<ExternalAccountBaseMaterialGroupMapper, ExternalAccountBaseMaterialGroup> implements ExternalAccountBaseMaterialGroupService {

    @Override
    public List<ExternalAccountBaseMaterialGroup> getListByParentId(Long id) {
        List<ExternalAccountBaseMaterialGroup> list = this.list(new QueryWrapper<ExternalAccountBaseMaterialGroup>().eq(DBConstant.TABLE_EA_BASE_MATERIAL_GROUP.PARENT_ID_FIELDNAME, id));
        return list;
    }

    @Override
    public ExternalAccountBaseMaterialGroup getByCode(String groupCode) {
        return this.getOne(new QueryWrapper<ExternalAccountBaseMaterialGroup>().eq(DBConstant.TABLE_EA_BASE_MATERIAL_GROUP.CODE_FIELDNAME,groupCode ));
    }
}
