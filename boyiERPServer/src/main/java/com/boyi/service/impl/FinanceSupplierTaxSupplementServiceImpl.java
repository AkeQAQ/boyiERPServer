package com.boyi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.common.constant.DBConstant;
import com.boyi.entity.FinanceSupplierTaxSupplement;
import com.boyi.entity.FinanceSupplierTaxSupplement;
import com.boyi.mapper.FinanceSupplierTaxSupplementMapper;
import com.boyi.mapper.FinanceSupplierTaxSupplementMapper;
import com.boyi.service.FinanceSupplierTaxSupplementService;
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
 * @since 2023-02-27
 */
@Service
public class FinanceSupplierTaxSupplementServiceImpl extends ServiceImpl<FinanceSupplierTaxSupplementMapper, FinanceSupplierTaxSupplement> implements FinanceSupplierTaxSupplementService {

    @Autowired
    public FinanceSupplierTaxSupplementMapper FinanceSupplierTaxSupplementMapper;

    @Override
    public Page<FinanceSupplierTaxSupplement> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, List<Long> searchStatus, Map<String, String> otherSearch, String searchStartDate, String searchEndDate, List<Long> searchPayStatus) {
        QueryWrapper<FinanceSupplierTaxSupplement> queryWrapper = new QueryWrapper<>();
        for (String key : otherSearch.keySet()){
            String val = otherSearch.get(key);
            queryWrapper.like(StrUtil.isNotBlank(val) && !val.equals("null")
                    && StrUtil.isNotBlank(key),key,val);
        }
        return this.innerQuery(page,
                queryWrapper.
                        like(StrUtil.isNotBlank(searchStr) &&!searchStr.equals("null")
                                && StrUtil.isNotBlank(searchField),queryField,searchStr)
                        .in(searchStatus != null && searchStatus.size() > 0, DBConstant.TABLE_FINANCE_SUPPLIER_TAX_SUPPLEMENT.STATUS_FIELDNAME,searchStatus)
                        .ge(StrUtil.isNotBlank(searchStartDate)&& !searchStartDate.equals("null"),DBConstant.TABLE_FINANCE_SUPPLIER_TAX_SUPPLEMENT.DOCUMENT_DATE_FIELDNAME,searchStartDate)
                        .le(StrUtil.isNotBlank(searchEndDate)&& !searchEndDate.equals("null"),DBConstant.TABLE_FINANCE_SUPPLIER_TAX_SUPPLEMENT.DOCUMENT_DATE_FIELDNAME,searchEndDate)
                        .in(searchPayStatus != null && searchPayStatus.size() > 0, DBConstant.TABLE_FINANCE_SUPPLIER_TAX_SUPPLEMENT.PAY_STATUS_FIELDNAME,searchPayStatus)
                        .orderByDesc(DBConstant.TABLE_FINANCE_SUPPLIER_TAX_SUPPLEMENT.ID_FIELDNAME)

        );
    }

    @Override
    public List<FinanceSupplierTaxSupplement> countLTByCloseDate(LocalDate closeDate) {
        return this.list(new QueryWrapper<FinanceSupplierTaxSupplement>()
                .le(DBConstant.TABLE_FINANCE_SUPPLIER_TAX_SUPPLEMENT.DOCUMENT_DATE_FIELDNAME, closeDate)
                .ne(DBConstant.TABLE_FINANCE_SUPPLIER_TAX_SUPPLEMENT.STATUS_FIELDNAME,
                        DBConstant.TABLE_FINANCE_SUPPLIER_TAX_SUPPLEMENT.STATUS_FIELDVALUE_0));
    }

    @Override
    public Page<FinanceSupplierTaxSupplement> innerQuery(Page page, QueryWrapper<FinanceSupplierTaxSupplement> eq) {
        return FinanceSupplierTaxSupplementMapper.page(page,eq);

    }

    @Override
    public void updateNullWithField(FinanceSupplierTaxSupplement ppc, String picName) {
        this.update(new UpdateWrapper<FinanceSupplierTaxSupplement>()
                .set(picName,null)
                .eq(DBConstant.TABLE_FINANCE_SUPPLIER_TAX_SUPPLEMENT.ID_FIELDNAME,ppc.getId()));
    }
}
