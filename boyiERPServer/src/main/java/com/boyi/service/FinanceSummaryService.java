package com.boyi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.FinanceSummary;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boyi.entity.FinanceSummary;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunke
 * @since 2023-03-06
 */
public interface FinanceSummaryService extends IService<FinanceSummary> {

    Page<FinanceSummary> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, List<Long> searchStatusList,List<Long> payStatusList, Map<String, String> queryMap, String searchStartDate, String searchEndDate,String searchStartSettleDate,String searchEndSettleDate);

    Page<FinanceSummary> innerQuery(Page page, QueryWrapper<FinanceSummary> like);

    void updateNullWithField(FinanceSummary ppc, String picUrlFieldname);

    List<FinanceSummary> countLTByCloseDate(LocalDate closeDate);

    Integer countByDate(String addDate);
}
