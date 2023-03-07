package com.boyi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.FinanceSupplierFine;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunke
 * @since 2023-02-26
 */
public interface FinanceSupplierFineService extends IService<FinanceSupplierFine> {

    Page<FinanceSupplierFine> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, List<Long> searchStatusList, Map<String, String> queryMap, String searchStartDate, String searchEndDate);
    Page<FinanceSupplierFine> innerQuery(Page page, QueryWrapper<FinanceSupplierFine> eq) ;

    void updateNullWithField(FinanceSupplierFine ppc, String picUrlFieldname);

    List<FinanceSupplierFine> countLTByCloseDate(LocalDate closeDate);

    List<FinanceSupplierFine> getSupplierTotalAmountBetweenDate(LocalDate startDateTime, LocalDate endDateTime);
}
