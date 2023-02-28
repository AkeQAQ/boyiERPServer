package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.FinanceSupplierTaxDeduction;
import com.boyi.entity.FinanceSupplierTaxSupplement;
import com.boyi.mapper.FinanceSupplierTaxDeductionMapper;
import com.boyi.mapper.FinanceSupplierTaxSupplementMapper;
import com.boyi.service.FinanceSupplierTaxDeductionService;
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
 * @since 2023-02-27
 */
@Service
public class FinanceSupplierTaxDeductionServiceImpl extends ServiceImpl<FinanceSupplierTaxDeductionMapper, FinanceSupplierTaxDeduction> implements FinanceSupplierTaxDeductionService {

    @Autowired
    public FinanceSupplierTaxDeductionMapper financeSupplierTaxDeductionMapper;

    @Override
    public Page<FinanceSupplierTaxDeduction> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, List<Long> searchStatus, Map<String, String> otherSearch, String searchStartDate, String searchEndDate, List<Long> searchPayStatus) {
        QueryWrapper<FinanceSupplierTaxDeduction> queryWrapper = new QueryWrapper<>();
        for (String key : otherSearch.keySet()){
            String val = otherSearch.get(key);
            queryWrapper.like(StrUtil.isNotBlank(val) && !val.equals("null")
                    && StrUtil.isNotBlank(key),key,val);
        }
        return this.innerQuery(page,
                queryWrapper.
                        like(StrUtil.isNotBlank(searchStr) &&!searchStr.equals("null")
                                && StrUtil.isNotBlank(searchField),queryField,searchStr)
                        .in(searchStatus != null && searchStatus.size() > 0, DBConstant.TABLE_FINANCE_SUPPLIER_TAX_DEDUCTION.STATUS_FIELDNAME,searchStatus)
                        .ge(StrUtil.isNotBlank(searchStartDate)&& !searchStartDate.equals("null"),DBConstant.TABLE_FINANCE_SUPPLIER_TAX_DEDUCTION.DOCUMENT_DATE_FIELDNAME,searchStartDate)
                        .le(StrUtil.isNotBlank(searchEndDate)&& !searchEndDate.equals("null"),DBConstant.TABLE_FINANCE_SUPPLIER_TAX_DEDUCTION.DOCUMENT_DATE_FIELDNAME,searchEndDate)
                        .in(searchPayStatus != null && searchPayStatus.size() > 0, DBConstant.TABLE_FINANCE_SUPPLIER_TAX_DEDUCTION.PAY_STATUS_FIELDNAME,searchPayStatus)
                        .orderByDesc(DBConstant.TABLE_FINANCE_SUPPLIER_TAX_DEDUCTION.ID_FIELDNAME)

        );
    }

    @Override
    public Page<FinanceSupplierTaxDeduction> innerQuery(Page page, QueryWrapper<FinanceSupplierTaxDeduction> eq) {
        return financeSupplierTaxDeductionMapper.page(page,eq);

    }

    @Override
    public void updateNullWithField(FinanceSupplierTaxDeduction ppc, String picName) {
        this.update(new UpdateWrapper<FinanceSupplierTaxDeduction>()
                .set(picName,null)
                .eq(DBConstant.TABLE_FINANCE_SUPPLIER_TAX_DEDUCTION.ID_FIELDNAME,ppc.getId()));
    }
}
