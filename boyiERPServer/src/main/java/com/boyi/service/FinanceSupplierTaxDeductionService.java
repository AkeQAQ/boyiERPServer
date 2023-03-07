package com.boyi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.FinanceSupplierTaxDeduction;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boyi.entity.FinanceSupplierTaxSupplement;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunke
 * @since 2023-02-27
 */
public interface FinanceSupplierTaxDeductionService extends IService<FinanceSupplierTaxDeduction> {

    void updateNullWithField(FinanceSupplierTaxDeduction ppc, String picUrlFieldname);
    Page<FinanceSupplierTaxDeduction> innerQuery(Page page, QueryWrapper<FinanceSupplierTaxDeduction> eq) ;

    Page<FinanceSupplierTaxDeduction> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, List<Long> searchStatusList, Map<String, String> queryMap, String searchStartDate, String searchEndDate, List<Long> searchPayStatusList);

    List<FinanceSupplierTaxDeduction> countLTByCloseDate(LocalDate closeDate);

    List<FinanceSupplierTaxDeduction> getSupplierTotalAmountBetweenDate(LocalDate startDateTime, LocalDate endDateTime);
}
