package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.FinanceSupplierChange;
import com.boyi.entity.FinanceSupplierRoundDown;
import com.boyi.mapper.FinanceSupplierChangeMapper;
import com.boyi.mapper.FinanceSupplierRoundDownMapper;
import com.boyi.service.FinanceSupplierRoundDownService;
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
 * @since 2023-02-26
 */
@Service
public class FinanceSupplierRoundDownServiceImpl extends ServiceImpl<FinanceSupplierRoundDownMapper, FinanceSupplierRoundDown> implements FinanceSupplierRoundDownService {

    @Autowired
    public FinanceSupplierRoundDownMapper financeSupplierRoundDownMapper;

    @Override
    public Page<FinanceSupplierRoundDown> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, List<Long> searchStatus, Map<String, String> otherSearch, String searchStartDate, String searchEndDate) {
        QueryWrapper<FinanceSupplierRoundDown> queryWrapper = new QueryWrapper<>();
        for (String key : otherSearch.keySet()){
            String val = otherSearch.get(key);
            queryWrapper.like(StrUtil.isNotBlank(val) && !val.equals("null")
                    && StrUtil.isNotBlank(key),key,val);
        }
        return this.innerQuery(page,
                queryWrapper.
                        like(StrUtil.isNotBlank(searchStr) &&!searchStr.equals("null")
                                && StrUtil.isNotBlank(searchField),queryField,searchStr)
                        .in(searchStatus != null && searchStatus.size() > 0, DBConstant.TABLE_FINANCE_SUPPLIER_ROUND_DOWN.STATUS_FIELDNAME,searchStatus)
                        .ge(StrUtil.isNotBlank(searchStartDate)&& !searchStartDate.equals("null"),DBConstant.TABLE_FINANCE_SUPPLIER_ROUND_DOWN.ROUND_DOWN_DATE_FIELDNAME,searchStartDate)
                        .le(StrUtil.isNotBlank(searchEndDate)&& !searchEndDate.equals("null"),DBConstant.TABLE_FINANCE_SUPPLIER_ROUND_DOWN.ROUND_DOWN_DATE_FIELDNAME,searchEndDate)

                        .orderByDesc(DBConstant.TABLE_FINANCE_SUPPLIER_ROUND_DOWN.ID_FIELDNAME)

        );
    }

    @Override
    public Page<FinanceSupplierRoundDown> innerQuery(Page page, QueryWrapper<FinanceSupplierRoundDown> eq) {
        return financeSupplierRoundDownMapper.page(page,eq);

    }
}
