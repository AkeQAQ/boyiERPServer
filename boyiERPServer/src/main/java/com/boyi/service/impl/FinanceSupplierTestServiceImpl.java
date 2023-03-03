package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.FinanceSupplierRoundDown;
import com.boyi.entity.FinanceSupplierTest;
import com.boyi.mapper.FinanceSupplierRoundDownMapper;
import com.boyi.mapper.FinanceSupplierTestMapper;
import com.boyi.service.FinanceSupplierTestService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
public class FinanceSupplierTestServiceImpl extends ServiceImpl<FinanceSupplierTestMapper, FinanceSupplierTest> implements FinanceSupplierTestService {

    @Autowired
    public FinanceSupplierTestMapper financeSupplierTestMapper;

    @Override
    public Page<FinanceSupplierTest> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, List<Long> searchStatus, Map<String, String> otherSearch, String searchStartDate, String searchEndDate) {
        QueryWrapper<FinanceSupplierTest> queryWrapper = new QueryWrapper<>();
        for (String key : otherSearch.keySet()){
            String val = otherSearch.get(key);
            queryWrapper.like(StrUtil.isNotBlank(val) && !val.equals("null")
                    && StrUtil.isNotBlank(key),key,val);
        }
        return this.innerQuery(page,
                queryWrapper.
                        like(StrUtil.isNotBlank(searchStr) &&!searchStr.equals("null")
                                && StrUtil.isNotBlank(searchField),queryField,searchStr)
                        .in(searchStatus != null && searchStatus.size() > 0, DBConstant.TABLE_FINANCE_SUPPLIER_TEST.STATUS_FIELDNAME,searchStatus)
                        .ge(StrUtil.isNotBlank(searchStartDate)&& !searchStartDate.equals("null"),DBConstant.TABLE_FINANCE_SUPPLIER_TEST.TEST_DATE_FIELDNAME,searchStartDate)
                        .le(StrUtil.isNotBlank(searchEndDate)&& !searchEndDate.equals("null"),DBConstant.TABLE_FINANCE_SUPPLIER_TEST.TEST_DATE_FIELDNAME,searchEndDate)

                        .orderByDesc(DBConstant.TABLE_FINANCE_SUPPLIER_TEST.ID_FIELDNAME)

        );
    }

    @Override
    public Page<FinanceSupplierTest> innerQuery(Page page, QueryWrapper<FinanceSupplierTest> eq) {
        return financeSupplierTestMapper.page(page,eq);

    }

    @Override
    public List<FinanceSupplierTest> countLTByCloseDate(LocalDate closeDate) {
        return this.list(new QueryWrapper<FinanceSupplierTest>()
                .le(DBConstant.TABLE_FINANCE_SUPPLIER_TEST.TEST_DATE_FIELDNAME, closeDate)
                .ne(DBConstant.TABLE_FINANCE_SUPPLIER_TEST.STATUS_FIELDNAME,
                        DBConstant.TABLE_FINANCE_SUPPLIER_TEST.STATUS_FIELDVALUE_0));
    }
}
