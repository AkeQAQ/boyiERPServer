package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.BaseMaterialGroup;
import com.boyi.mapper.BaseMaterialGroupMapper;
import com.boyi.service.BaseMaterialGroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 基础模块-物料分组表 服务实现类
 * </p>
 *
 * @author sunke
 * @since 2021-08-20
 */
@Service
public class BaseMaterialGroupServiceImpl extends ServiceImpl<BaseMaterialGroupMapper, BaseMaterialGroup> implements BaseMaterialGroupService {

    @Override
    public List<BaseMaterialGroup> getListByParentId(Long id) {
        List<BaseMaterialGroup> list = this.list(new QueryWrapper<BaseMaterialGroup>().eq(DBConstant.TABLE_BASE_MATERIAL_GROUP.PARENT_ID_FIELDNAME, id));
        return list;
    }

    @Override
    public BaseMaterialGroup getByCode(String groupCode) {
        return this.getOne(new QueryWrapper<BaseMaterialGroup>().eq(DBConstant.TABLE_BASE_MATERIAL_GROUP.CODE_FIELDNAME,groupCode ));
    }
}
