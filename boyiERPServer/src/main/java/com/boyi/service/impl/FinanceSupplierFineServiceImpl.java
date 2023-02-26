package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.FinanceSupplierFine;
import com.boyi.entity.FinanceSupplierFine;
import com.boyi.entity.FinanceSupplierPayshoes;
import com.boyi.mapper.FinanceSupplierFineMapper;
import com.boyi.mapper.FinanceSupplierFineMapper;
import com.boyi.service.FinanceSupplierFineService;
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
public class FinanceSupplierFineServiceImpl extends ServiceImpl<FinanceSupplierFineMapper, FinanceSupplierFine> implements FinanceSupplierFineService {

    @Autowired
    public FinanceSupplierFineMapper financeSupplierFineMapper;

    @Override
    public Page<FinanceSupplierFine> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, List<Long> searchStatus, Map<String, String> otherSearch, String searchStartDate, String searchEndDate) {
        QueryWrapper<FinanceSupplierFine> queryWrapper = new QueryWrapper<>();
        for (String key : otherSearch.keySet()){
            String val = otherSearch.get(key);
            queryWrapper.like(StrUtil.isNotBlank(val) && !val.equals("null")
                    && StrUtil.isNotBlank(key),key,val);
        }
        return this.innerQuery(page,
                queryWrapper.
                        like(StrUtil.isNotBlank(searchStr) &&!searchStr.equals("null")
                                && StrUtil.isNotBlank(searchField),queryField,searchStr)
                        .in(searchStatus != null && searchStatus.size() > 0, DBConstant.TABLE_FINANCE_SUPPLIER_FINE.STATUS_FIELDNAME,searchStatus)
                        .ge(StrUtil.isNotBlank(searchStartDate)&& !searchStartDate.equals("null"),DBConstant.TABLE_FINANCE_SUPPLIER_FINE.FINE_DATE_FIELDNAME,searchStartDate)
                        .le(StrUtil.isNotBlank(searchEndDate)&& !searchEndDate.equals("null"),DBConstant.TABLE_FINANCE_SUPPLIER_FINE.FINE_DATE_FIELDNAME,searchEndDate)

                        .orderByDesc(DBConstant.TABLE_FINANCE_SUPPLIER_FINE.ID_FIELDNAME)

        );
    }

    @Override
    public Page<FinanceSupplierFine> innerQuery(Page page, QueryWrapper<FinanceSupplierFine> eq) {
        return financeSupplierFineMapper.page(page,eq);

    }

    @Override
    public void updateNullWithField(FinanceSupplierFine ppc, String picName) {
        this.update(new UpdateWrapper<FinanceSupplierFine>()
                .set(picName,null)
                .eq(DBConstant.TABLE_FINANCE_SUPPLIER_FINE.ID_FIELDNAME,ppc.getId()));
    }
}
