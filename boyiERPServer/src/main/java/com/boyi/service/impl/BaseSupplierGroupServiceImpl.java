package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.BaseMaterialGroup;
import com.boyi.entity.BaseSupplierGroup;
import com.boyi.mapper.BaseMaterialGroupMapper;
import com.boyi.mapper.BaseSupplierGroupMapper;
import com.boyi.service.BaseMaterialGroupService;
import com.boyi.service.BaseSupplierGroupService;
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
public class BaseSupplierGroupServiceImpl extends ServiceImpl<BaseSupplierGroupMapper, BaseSupplierGroup> implements BaseSupplierGroupService {

    @Override
    public List<BaseSupplierGroup> getListByParentId(Long id) {
        List<BaseSupplierGroup> list = this.list(new QueryWrapper<BaseSupplierGroup>().eq(DBConstant.TABLE_BASE_SUPPLIER_GROUP.PARENT_ID_FIELDNAME, id));
        return list;
    }

    @Override
    public BaseSupplierGroup getByCode(String groupCode) {
        return this.getOne(new QueryWrapper<BaseSupplierGroup>().eq(DBConstant.TABLE_BASE_SUPPLIER_GROUP.NAME_FIELDNAME, groupCode));
    }
}
