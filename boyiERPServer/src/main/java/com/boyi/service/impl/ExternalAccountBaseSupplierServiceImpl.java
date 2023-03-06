package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.ExternalAccountBaseSupplier;
import com.boyi.entity.ExternalAccountBaseSupplier;
import com.boyi.mapper.ExternalAccountBaseSupplierMapper;
import com.boyi.service.ExternalAccountBaseSupplierService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sunke
 * @since 2023-03-04
 */
@Service
public class ExternalAccountBaseSupplierServiceImpl extends ServiceImpl<ExternalAccountBaseSupplierMapper, ExternalAccountBaseSupplier> implements ExternalAccountBaseSupplierService {

    @Override
    public Integer countByGroupCode(String groupCode) {
        return this.count(new QueryWrapper<ExternalAccountBaseSupplier>().eq(DBConstant.TABLE_EA_BASE_SUPPLIER.GROUP_CODE_FIELDNAME, groupCode));
    }


    @Override
    public Page<ExternalAccountBaseSupplier> pageByGroupCode(Page page, String searchStr) {
        return this.page(page,new QueryWrapper<ExternalAccountBaseSupplier>().eq(DBConstant.TABLE_EA_BASE_SUPPLIER.GROUP_CODE_FIELDNAME,searchStr));
    }
    @Override
    public Page<ExternalAccountBaseSupplier> pageBySearch(Page page,String queryField, String searchStr) {
        return this.page(page, new QueryWrapper<ExternalAccountBaseSupplier>()
                .like(StrUtil.isNotBlank(searchStr), queryField, searchStr));
    }

}
