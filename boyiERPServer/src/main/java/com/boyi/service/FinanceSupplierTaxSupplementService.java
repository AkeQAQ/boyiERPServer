package com.boyi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.FinanceSupplierFine;
import com.boyi.entity.FinanceSupplierTaxSupplement;
import com.baomidou.mybatisplus.extension.service.IService;

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
public interface FinanceSupplierTaxSupplementService extends IService<FinanceSupplierTaxSupplement> {

    void updateNullWithField(FinanceSupplierTaxSupplement ppc, String picUrlFieldname);
    Page<FinanceSupplierTaxSupplement> innerQuery(Page page, QueryWrapper<FinanceSupplierTaxSupplement> eq) ;

    Page<FinanceSupplierTaxSupplement> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, List<Long> searchStatusList, Map<String, String> queryMap, String searchStartDate, String searchEndDate, List<Long> searchPayStatusList);
}
