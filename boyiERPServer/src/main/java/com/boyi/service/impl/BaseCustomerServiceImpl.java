package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.BaseCustomer;
import com.boyi.entity.BaseDepartment;
import com.boyi.mapper.BaseCustomerMapper;
import com.boyi.service.BaseCustomerService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sunke
 * @since 2022-11-13
 */
@Service
public class BaseCustomerServiceImpl extends ServiceImpl<BaseCustomerMapper, BaseCustomer> implements BaseCustomerService {

    @Override
    public Page<BaseCustomer> pageBySearch(Page page, String searchName) {
        return this.page(page, new QueryWrapper<BaseCustomer>()
                .like(StrUtil.isNotBlank(searchName), DBConstant.TABLE_BASE_CUSTOMER.NAME_FIELDNAME, searchName)
                .orderByDesc(DBConstant.TABLE_BASE_CUSTOMER.ID_FIELDNAME))
                ;
    }
}
