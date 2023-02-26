package com.boyi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.FinanceSupplierRoundDown;
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
public interface FinanceSupplierRoundDownService extends IService<FinanceSupplierRoundDown> {

    Page<FinanceSupplierRoundDown> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, List<Long> searchStatusList, Map<String, String> queryMap, String searchStartDate, String searchEndDate);

    Page<FinanceSupplierRoundDown> innerQuery(Page page, QueryWrapper<FinanceSupplierRoundDown> eq);

}
