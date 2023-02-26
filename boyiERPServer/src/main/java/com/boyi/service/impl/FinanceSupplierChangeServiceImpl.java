package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.FinanceSupplierChange;
import com.boyi.entity.FinanceSupplierChangeDetails;
import com.boyi.entity.FinanceSupplierPayshoes;
import com.boyi.entity.FinanceSupplierPayshoesDetails;
import com.boyi.mapper.FinanceSupplierChangeMapper;
import com.boyi.service.FinanceSupplierChangeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sunke
 * @since 2023-02-25
 */
@Service
public class FinanceSupplierChangeServiceImpl extends ServiceImpl<FinanceSupplierChangeMapper, FinanceSupplierChange> implements FinanceSupplierChangeService {

    @Autowired
    public FinanceSupplierChangeMapper financeSupplierChangeMapper;

    @Override
    public Page<FinanceSupplierChange> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, List<Long> searchStatus, Map<String, String> otherSearch, String searchStartDate, String searchEndDate) {
        QueryWrapper<FinanceSupplierChange> queryWrapper = new QueryWrapper<>();
        for (String key : otherSearch.keySet()){
            String val = otherSearch.get(key);
            queryWrapper.like(StrUtil.isNotBlank(val) && !val.equals("null")
                    && StrUtil.isNotBlank(key),key,val);
        }
        return this.innerQuery(page,
                queryWrapper.
                        like(StrUtil.isNotBlank(searchStr) &&!searchStr.equals("null")
                                && StrUtil.isNotBlank(searchField),queryField,searchStr)
                        .in(searchStatus != null && searchStatus.size() > 0, DBConstant.TABLE_FINANCE_SUPPLIER_CHANGE.STATUS_FIELDNAME,searchStatus)
                        .ge(StrUtil.isNotBlank(searchStartDate)&& !searchStartDate.equals("null"),DBConstant.TABLE_FINANCE_SUPPLIER_CHANGE.CHANGE_DATE_FIELDNAME,searchStartDate)
                        .le(StrUtil.isNotBlank(searchEndDate)&& !searchEndDate.equals("null"),DBConstant.TABLE_FINANCE_SUPPLIER_CHANGE.CHANGE_DATE_FIELDNAME,searchEndDate)

                        .orderByDesc(DBConstant.TABLE_FINANCE_SUPPLIER_CHANGE.ID_FIELDNAME)

        );
    }

    @Override
    public Page<FinanceSupplierChange> innerQuery(Page page, QueryWrapper<FinanceSupplierChange> eq) {
        return financeSupplierChangeMapper.page(page,eq);

    }

}
