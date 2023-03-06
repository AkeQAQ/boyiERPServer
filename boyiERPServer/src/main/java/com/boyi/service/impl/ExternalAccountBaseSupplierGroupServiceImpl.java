package com.boyi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.ExternalAccountBaseSupplierGroup;
import com.boyi.entity.ExternalAccountBaseSupplierGroup;
import com.boyi.mapper.ExternalAccountBaseSupplierGroupMapper;
import com.boyi.service.ExternalAccountBaseSupplierGroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 基础模块-供应商分组表 服务实现类
 * </p>
 *
 * @author sunke
 * @since 2023-03-04
 */
@Service
public class ExternalAccountBaseSupplierGroupServiceImpl extends ServiceImpl<ExternalAccountBaseSupplierGroupMapper, ExternalAccountBaseSupplierGroup> implements ExternalAccountBaseSupplierGroupService {

    @Override
    public List<ExternalAccountBaseSupplierGroup> getListByParentId(Long id) {
        List<ExternalAccountBaseSupplierGroup> list = this.list(new QueryWrapper<ExternalAccountBaseSupplierGroup>().eq(DBConstant.TABLE_BASE_SUPPLIER_GROUP.PARENT_ID_FIELDNAME, id));
        return list;
    }

    @Override
    public ExternalAccountBaseSupplierGroup getByCode(String groupCode) {
        return this.getOne(new QueryWrapper<ExternalAccountBaseSupplierGroup>().eq(DBConstant.TABLE_BASE_SUPPLIER_GROUP.CODE_FIELDNAME, groupCode));
    }
}
