package com.boyi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.FinanceSupplierRoundDown;
import com.boyi.entity.FinanceSupplierTest;
import com.baomidou.mybatisplus.extension.service.IService;

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
public interface FinanceSupplierTestService extends IService<FinanceSupplierTest> {

    Page<FinanceSupplierTest> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, List<Long> searchStatusList, Map<String, String> queryMap, String searchStartDate, String searchEndDate);

    Page<FinanceSupplierTest> innerQuery(Page page, QueryWrapper<FinanceSupplierTest> eq);
}
