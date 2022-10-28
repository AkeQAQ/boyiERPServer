package com.boyi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boyi.entity.CostOfLabour;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boyi.entity.RepositoryReturnMaterial;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sunke
 * @since 2022-10-27
 */
public interface CostOfLabourService extends IService<CostOfLabour> {

    Page<CostOfLabour> innerQuery(Page page, QueryWrapper<CostOfLabour> like);

    Page<CostOfLabour> innerQueryByManySearch(Page page, String searchField, String queryField, String searchStr, String searchStartDate, String searchEndDate, List<Long> searchStatusList, Map<String, String> queryMap);

}
