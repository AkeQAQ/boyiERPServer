package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.BaseMaterial;
import com.boyi.entity.BaseSupplier;
import com.boyi.mapper.BaseMaterialMapper;
import com.boyi.mapper.BaseSupplierMapper;
import com.boyi.service.BaseMaterialService;
import com.boyi.service.BaseSupplierService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sunke
 * @since 2021-08-21
 */
@Service
public class BaseSupplierServiceImpl extends ServiceImpl<BaseSupplierMapper, BaseSupplier> implements BaseSupplierService {

    @Override
    public Integer countByGroupCode(String groupCode) {
        return this.count(new QueryWrapper<BaseSupplier>().eq(DBConstant.TABLE_BASE_SUPPLIER.GROUP_CODE_FIELDNAME, groupCode));
    }


    @Override
    public Page<BaseSupplier> pageByGroupCode(Page page, String searchStr) {
        return this.page(page,new QueryWrapper<BaseSupplier>().eq(DBConstant.TABLE_BASE_SUPPLIER.GROUP_CODE_FIELDNAME,searchStr));
    }
    @Override
    public Page<BaseSupplier> pageBySearch(Page page,String queryField, String searchStr) {
        return this.page(page, new QueryWrapper<BaseSupplier>()
                .like(StrUtil.isNotBlank(searchStr), queryField, searchStr));
    }

}
