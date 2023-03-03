package com.boyi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.FinanceSupplierChange;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boyi.entity.FinanceSupplierChangeDetails;
import com.boyi.entity.FinanceSupplierPayshoes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunke
 * @since 2023-02-25
 */
public interface FinanceSupplierChangeService extends IService<FinanceSupplierChange> {

    Page<FinanceSupplierChange> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, List<Long> searchStatusList, Map<String, String> queryMap, String searchStartDate, String searchEndDate);
    Page<FinanceSupplierChange> innerQuery(Page page, QueryWrapper<FinanceSupplierChange> like);

    List<FinanceSupplierChange> countLTByCloseDate(LocalDate closeDate);
}
